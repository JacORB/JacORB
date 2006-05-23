package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.container.BiDirGiopPOAComponentAdapter;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.lifecycle.ManageableServant;
import org.jacorb.notification.util.AdminPropertySet;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNotification.BestEffort;
import org.omg.CosNotification.ConnectionReliability;
import org.omg.CosNotification.EventReliability;
import org.omg.CosNotification.Persistent;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertyError;
import org.omg.CosNotification.PropertyRange;
import org.omg.CosNotification.QoSError_code;
import org.omg.CosNotification.UnsupportedAdmin;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ChannelNotFound;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractChannelFactory implements ManageableServant, Disposable
{
    interface ShutdownCallback
    {
        void needTime(int time);

        void shutdownComplete();
    }

    // //////////////////////////////////////

    private static final String STANDARD_IMPL_NAME = "JacORB-NotificationService";

    private static final long SHUTDOWN_INTERVAL = 1000;

    private static final String EVENTCHANNEL_FACTORY_POA_NAME = "EventChannelFactoryPOA";

    // //////////////////////////////////////

    private NameComponent[] registeredName_ = null;

    private NamingContext namingContext_;

    /**
     * the method that is executed when destroy is invoked.
     */
    private Runnable destroyMethod_ = new Runnable()
    {
        public void run()
        {
            dispose();
        }
    };

    // ///////

    protected final MutablePicoContainer container_;

    protected final Configuration config_;

    protected final org.omg.CORBA.Object thisRef_;

    protected final Logger logger_;

    private final String ior_;

    private final String corbaLoc_;

    private final POA eventChannelFactoryPOA_;

    private final byte[] oid_;

    private final ChannelManager channelManager_ = new ChannelManager();

    private final AtomicInteger eventChannelIDPool_ = new AtomicInteger(0);

    private final DisposableManager disposableManager_ = new DisposableManager();

    // //////////////////////////////////////

    protected AbstractChannelFactory(final MutablePicoContainer container, final ORB orb)
            throws UserException
    {
        container_ = PicoContainerFactory.createRootContainer(container, (org.jacorb.orb.ORB) orb);

        if (container != null)
        {
            disposableManager_.addDisposable(new Disposable()
            {
                public void dispose()
                {
                    container.removeChildContainer(container_);
                }
            });
        }

        disposableManager_.addDisposable(new Disposable() {
            public void dispose()
            {
                final POA _poa = (POA) container_.getComponentInstanceOfType(POA.class);

                _poa.destroy(true, false);
            }
        });

        config_ = (Configuration) container_.getComponentInstanceOfType(Configuration.class);

        logger_ = ((org.jacorb.config.Configuration) config_).getNamedLogger(getClass().getName());

        POA _rootPOA = (POA) container_.getComponentInstanceOfType(POA.class);

        List _ps = new ArrayList();

        _ps.add(_rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID));

        BiDirGiopPOAComponentAdapter.addBiDirGiopPolicy(_ps, orb, config_);

        org.omg.CORBA.Policy[] _policies = (org.omg.CORBA.Policy[]) _ps
                .toArray(new org.omg.CORBA.Policy[_ps.size()]);

        eventChannelFactoryPOA_ = _rootPOA.create_POA(EVENTCHANNEL_FACTORY_POA_NAME, _rootPOA
                .the_POAManager(), _policies);

        for (int x = 0; x < _policies.length; ++x)
        {
            _policies[x].destroy();
        }

        oid_ = (getObjectName().getBytes());

        eventChannelFactoryPOA_.activate_object_with_id(oid_, getServant());

        thisRef_ = eventChannelFactoryPOA_.id_to_reference(oid_);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("activated EventChannelFactory with OID '" + new String(oid_) + "' on '"
                    + eventChannelFactoryPOA_.the_name() + "'");
        }

        ior_ = orb.object_to_string(eventChannelFactoryPOA_.id_to_reference(oid_));

        corbaLoc_ = createCorbaLoc();

        ((org.jacorb.orb.ORB) orb).addObjectKey(getShortcut(), ior_);
    }

    // //////////////////////////////////////

    protected abstract AbstractEventChannel newEventChannel() throws ConfigurationException;

    protected abstract org.omg.CORBA.Object create_abstract_channel(Property[] admin,
            Property[] qos, IntHolder id) throws UnsupportedAdmin, UnsupportedQoS;

    protected abstract String getObjectName();

    protected abstract String getShortcut();

    protected abstract Servant getServant();

    // //////////////////////////////////////

    protected int getLocalPort()
    {
        org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB) getORB();

        return jorb.getBasicAdapter().getPort();
    }

    protected String getLocalAddress()
    {
        org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB) getORB();

        return jorb.getBasicAdapter().getAddress();
    }

    private String createCorbaLoc()
    {
        StringBuffer _corbaLoc = new StringBuffer("corbaloc::");

        _corbaLoc.append(getLocalAddress());
        _corbaLoc.append(":");
        _corbaLoc.append(getLocalPort());
        _corbaLoc.append("/");
        _corbaLoc.append(getShortcut());

        return _corbaLoc.toString();
    }

    public synchronized org.omg.CORBA.Object activate()
    {
        return thisRef_;
    }

    public void setDestroyMethod(Runnable destroyMethod)
    {
        destroyMethod_ = destroyMethod;
    }

    protected ORB getORB()
    {
        return (ORB) container_.getComponentInstance(ORB.class);
    }

    public final void deactivate()
    {
        try
        {
            eventChannelFactoryPOA_.deactivate_object(oid_);
        } catch (Exception e)
        {
            logger_.fatalError("unable to deactivate object", e);

            throw new RuntimeException();
        }
    }

    protected Configuration getConfiguration()
    {
        return config_;
    }

    public void dispose()
    {
        try
        {
            unregisterName();
        } catch (Exception e)
        {
            logger_.error("unable to unregister NameService registration", e);
        }

        deactivate();

        channelManager_.dispose();

        container_.dispose();

        disposableManager_.dispose();
    }

    protected void addToChannels(int id, AbstractEventChannel channel)
    {
        channelManager_.add_channel(id, channel);
    }

    protected int[] getAllChannels()
    {
        return channelManager_.get_all_channels();
    }

    protected AbstractEventChannel get_event_channel_servant(int id) throws ChannelNotFound
    {
        return channelManager_.get_channel_servant(id);
    }

    protected Iterator getChannelIterator()
    {
        return channelManager_.getChannelIterator();
    }

    protected AbstractEventChannel create_channel_servant(IntHolder id, Property[] qosProps,
            Property[] adminProps) throws UnsupportedAdmin, UnsupportedQoS, ConfigurationException
    {
        // check QoS and Admin Settings

        AdminPropertySet _adminSettings = new AdminPropertySet(config_);

        _adminSettings.set_admin(adminProps);

        QoSPropertySet _qosSettings = new QoSPropertySet(config_, QoSPropertySet.CHANNEL_QOS);

        _qosSettings.set_qos(qosProps);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("uniqueQoSProps: " + _qosSettings);
            logger_.debug("uniqueAdminProps: " + _adminSettings);
        }

        checkQoSSettings(_qosSettings);

        AbstractEventChannel _eventChannelServant = newEventChannel();

        id.value = _eventChannelServant.getID();

        _eventChannelServant.set_qos(_qosSettings.toArray());
        _eventChannelServant.set_admin(_adminSettings.toArray());

        channelCreated(_eventChannelServant);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("created channel_servant id=" + id.value);
        }

        return _eventChannelServant;
    }

    protected void channelCreated(AbstractEventChannel channel)
    {
        // empty
    }

    private int createChannelIdentifier()
    {
        return eventChannelIDPool_.getAndIncrement();
    }

    private void checkQoSSettings(PropertySet uniqueQoSProperties) throws UnsupportedQoS
    {
        if (uniqueQoSProperties.containsKey(EventReliability.value))
        {
            short _eventReliabilty = uniqueQoSProperties.get(EventReliability.value)
                    .extract_short();

            switch (_eventReliabilty) {
            case BestEffort.value:
                logger_.info("EventReliability=BestEffort");
                break;

            case Persistent.value:
                throwPersistentNotSupported(EventReliability.value);

            // fallthrough
            default:
                throwBadValue(EventReliability.value);
            }
        }

        short _connectionReliability = BestEffort.value;

        if (uniqueQoSProperties.containsKey(ConnectionReliability.value))
        {
            _connectionReliability = uniqueQoSProperties.get(ConnectionReliability.value)
                    .extract_short();

            switch (_connectionReliability) {
            case BestEffort.value:
                logger_.info("ConnectionReliability=BestEffort");
                break;

            case Persistent.value:
                throwPersistentNotSupported(ConnectionReliability.value);

                break; // to satisfy compiler
            default:
                throwBadValue(ConnectionReliability.value);
            }
        }
    }

    private void throwPersistentNotSupported(String property) throws UnsupportedQoS
    {
        Any _lowVal = getORB().create_any();
        Any _highVal = getORB().create_any();

        _lowVal.insert_short(BestEffort.value);
        _highVal.insert_short(BestEffort.value);

        UnsupportedQoS _e = new UnsupportedQoS(new PropertyError[] { new PropertyError(
                QoSError_code.UNSUPPORTED_VALUE, property, new PropertyRange(_lowVal, _highVal)) });

        throw _e;
    }

    private void throwBadValue(String property) throws UnsupportedQoS
    {
        Any _lowVal = getORB().create_any();
        Any _highVal = getORB().create_any();

        _lowVal.insert_short(BestEffort.value);
        _highVal.insert_short(BestEffort.value);

        UnsupportedQoS _e = new UnsupportedQoS("The specified Property Value is not supported",
                new PropertyError[] { new PropertyError(QoSError_code.BAD_VALUE, property,
                        new PropertyRange(_lowVal, _highVal)) });
        throw _e;
    }

    public void destroy()
    {
        // start extra thread to
        // shut down the Notification Service.
        // otherwise ORB.shutdown() would be called inside
        // a remote invocation which causes an exception.
        final Thread _shutdown = new Thread()
        {
            public void run()
            {
                try
                {
                    logger_.info("Notification Service is going down in " + SHUTDOWN_INTERVAL
                            + " ms");

                    Thread.sleep(SHUTDOWN_INTERVAL);
                } catch (InterruptedException e)
                {
                    // ignore
                }

                destroyMethod_.run();
            }
        };

        _shutdown.start();
    }

    /**
     * shutdown is called by the Java Wrapper
     */
    public void shutdown(ShutdownCallback cb)
    {
        // estimate shutdown time.
        // during shutdown disconnect must be called on every
        // connected client. in worst case the client is not
        // acccessible anymore and disconnect raises TRANSIENT. as
        // this could take some time request some more time from the
        // WrapperManager who is initiating the shutdown.

        int _numberOfClients = 0;

        Iterator i = getChannelIterator();

        while (i.hasNext())
        {
            AbstractEventChannel _channel = (AbstractEventChannel) ((Map.Entry) i.next())
                    .getValue();

            _numberOfClients += _channel.getNumberOfConnectedClients();
        }

        // TODO fetch this from somewhere?
        int _connectionTimeout = 4000;

        int _estimatedShutdowntime = 2000 + _numberOfClients * _connectionTimeout;

        if (logger_.isInfoEnabled())
        {
            logger_.info("Connected Clients: " + _numberOfClients);
            logger_.info("Connection Timeout: " + _connectionTimeout + " ms");
            logger_.info("Estimated Shutdowntime: " + _estimatedShutdowntime + " ms");
        }

        // estimate 4000ms shutdowntime per channel
        cb.needTime(_estimatedShutdowntime);

        logger_.info("NotificationService is going down");

        destroyMethod_.run();

        logger_.info("NotificationService down");

        cb.shutdownComplete();
    }

    public String getIOR()
    {
        return ior_;
    }

    public String getCorbaLoc()
    {
        return corbaLoc_;
    }

    private static AbstractChannelFactory newChannelFactory(MutablePicoContainer container,
            ORB orb, boolean typed) throws UserException
    {
        if (typed)
        {
            return new TypedEventChannelFactoryImpl(container, orb);
        }

        return new EventChannelFactoryImpl(container, orb);
    }

    private static AbstractChannelFactory newFactory(MutablePicoContainer container, final ORB orb,
            boolean startThread, Properties props) throws Exception
    {
        AbstractChannelFactory _factory = newChannelFactory(container, orb, "on".equals(props
                .get(Attributes.ENABLE_TYPED_CHANNEL)));

        // force activation
        _factory.activate();

        _factory.printIOR(props);

        _factory.printCorbaLoc(props);

        _factory.writeIOR(props);

        _factory.registerName(props);

        _factory.startChannels(props);

        if (startThread)
        {
            POA _poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            _poa.the_POAManager().activate();

            Thread _orbThread = new Thread(new Runnable()
            {
                public void run()
                {
                    orb.run();
                }
            });

            _orbThread.setName("Notification ORB Runner Thread");

            _orbThread.setDaemon(false);

            _orbThread.start();

            _factory.disposableManager_.addDisposable(new Disposable() {
                public void dispose() {
                    orb.shutdown(false);
                }
            });
        }

        return _factory;
    }

    public static AbstractChannelFactory newFactory(ORB optionalORB, MutablePicoContainer optionalContainer, Properties props)
            throws Exception
    {
        props.put("jacorb.implname", STANDARD_IMPL_NAME);

        final ORB _orb;
        if (optionalORB != null)
        {
            _orb = optionalORB;
        }
        else
        {
            _orb = ORB.init(new String[] {}, props);
        }

        AbstractChannelFactory factory = newFactory(optionalContainer, _orb, (optionalORB == null), props);

        return factory;
    }

    public static AbstractChannelFactory newFactory(Properties props) throws Exception
    {
        return newFactory(null, null, props);
    }

    public void registerName(Properties props) throws Exception
    {
        registerName(props.getProperty(Attributes.REGISTER_NAME_ID, null), props.getProperty(
                Attributes.REGISTER_NAME_KIND, ""));
    }

    public synchronized void registerName(String nameId, String nameKind) throws NotFound,
            CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName
    {
        if (nameId == null)
        {
            return;
        }

        namingContext_ = NamingContextHelper.narrow(getORB().resolve_initial_references("NameService"));

        NameComponent[] _name = new NameComponent[] { new NameComponent(nameId, nameKind) };

        if (logger_.isInfoEnabled())
        {
            logger_.info("namingContext.rebind(" + format(_name) + " => " + getCorbaLoc() + ")");
        }

        namingContext_.rebind(_name, thisRef_);

        registeredName_ = _name;
    }

    public synchronized void unregisterName() throws NotFound, CannotProceed, InvalidName
    {
        if (namingContext_ != null)
        {
            if (registeredName_ != null)
            {
                if (logger_.isInfoEnabled())
                {
                    logger_.info("namingContext.unbind(" + format(registeredName_) + ")");
                }

                namingContext_.unbind(registeredName_);

                registeredName_ = null;
            }
        }
    }

    private static String format(NameComponent[] name)
    {
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < name.length; ++i)
        {
            if (i != 0)
            {
                b.append('/');
            }

            format(name[i], b);
        }

        return b.toString();
    }

    private static void format(NameComponent name, StringBuffer b)
    {
        b.append(name.id);

        if (name.kind != null)
        {
            b.append('.');
            b.append(name.kind);
        }
    }

    private void startChannels(Properties props) throws UnsupportedQoS, UnsupportedAdmin
    {
        if (props.containsKey(Attributes.START_CHANNELS))
        {
            startChannels(Integer.parseInt((String) props.get(Attributes.START_CHANNELS)));
        }
    }

    private void startChannels(int channels) throws UnsupportedQoS, UnsupportedAdmin
    {
        for (int i = 0; i < channels; i++)
        {
            IntHolder ih = new IntHolder();
            create_abstract_channel(new Property[0], new Property[0], ih);
        }
    }

    private void printIOR(Properties props)
    {
        if ("on".equals(props.get(Attributes.PRINT_IOR)))
        {
            System.out.println(getIOR());
        }
    }

    private void printCorbaLoc(Properties props)
    {
        if ("on".equals(props.get(Attributes.PRINT_CORBALOC)))
        {
            System.out.println(getCorbaLoc());
        }
    }

    private void writeIOR(Properties props) throws IOException
    {
        String _iorFileName = (String) props.get(Attributes.IOR_FILE);

        if (_iorFileName != null)
        {
            writeIOR(_iorFileName);
        }
    }

    public void writeIOR(String fileName) throws IOException
    {
        FileWriter out = new FileWriter(fileName);

        try
        {
            writeIOR(out);
            out.flush();
        } finally
        {
            out.close();
        }
    }

    private void writeIOR(Writer out)
    {
        PrintWriter writer = new PrintWriter(out);
        writer.println(getIOR());
    }

    public POA _default_POA()
    {
        return eventChannelFactoryPOA_;
    }

    protected MutablePicoContainer newContainerForChannel()
    {
        final MutablePicoContainer _channelContainer = PicoContainerFactory
                .createChildContainer(container_);

        // create identifier
        final int _channelID = createChannelIdentifier();
        IFactory _factory = new IFactory()
        {
            public MutablePicoContainer getContainer()
            {
                return _channelContainer;
            }

            public int getChannelID()
            {
                return _channelID;
            }

            public void destroy()
            {
                container_.removeChildContainer(_channelContainer);
            }
        };

        _channelContainer.registerComponentInstance(IFactory.class, _factory);
        return _channelContainer;
    }
}