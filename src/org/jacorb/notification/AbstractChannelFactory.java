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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.container.PicoContainerFactory;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.servant.ManageableServant;
import org.jacorb.notification.util.AdminPropertySet;
import org.jacorb.notification.util.PropertySet;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
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
import org.omg.PortableServer.Servant;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

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

    ////////////////////////////////////////

    private static final String STANDARD_IMPL_NAME = "JacORB-NotificationService";

    private static final long SHUTDOWN_INTERVAL = 1000;

    private static final String EVENTCHANNEL_FACTORY_POA_NAME = "EventChannelFactoryPOA";

    ////////////////////////////////////////

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

    /////////

    protected final MutablePicoContainer container_;

    protected final ComponentAdapterFactory componentAdapterFactory_;

    protected final Configuration config_;

    protected final org.omg.CORBA.Object thisRef_;

    protected final Logger logger_;

    private final String ior_;

    private final String corbaLoc_;

    private final POA eventChannelFactoryPOA_;

    private final ChannelManager channelManager_ = new ChannelManager();

    private final SynchronizedInt eventChannelIDPool_ = new SynchronizedInt(-1);

    ////////////////////////////////////////

    protected AbstractChannelFactory(PicoContainer container, final ORB orb) throws UserException
    {
        container_ = PicoContainerFactory.createRootContainer(container, (org.jacorb.orb.ORB) orb);

        config_ = (Configuration) container_.getComponentInstance(Configuration.class);

        logger_ = ((org.jacorb.config.Configuration) config_).getNamedLogger(getClass().getName());

        componentAdapterFactory_ = (ComponentAdapterFactory) container_
                .getComponentInstance(ComponentAdapterFactory.class);

        POA _rootPOA = (POA) container_.getComponentInstance(POA.class);

        org.omg.CORBA.Policy[] _policies = new org.omg.CORBA.Policy[] { _rootPOA
                .create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID) };

        eventChannelFactoryPOA_ = _rootPOA.create_POA(EVENTCHANNEL_FACTORY_POA_NAME, _rootPOA
                .the_POAManager(), _policies);

        for (int x = 0; x < _policies.length; ++x)
        {
            _policies[x].destroy();
        }

        _rootPOA.the_POAManager().activate();

        byte[] oid = (getObjectName().getBytes());

        eventChannelFactoryPOA_.activate_object_with_id(oid, getServant());

        thisRef_ = eventChannelFactoryPOA_.id_to_reference(oid);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("activated EventChannelFactory with OID '" + new String(oid) + "' on '"
                    + eventChannelFactoryPOA_.the_name() + "'");
        }

        ior_ = orb.object_to_string(eventChannelFactoryPOA_.id_to_reference(oid));

        corbaLoc_ = createCorbaLoc();

        ((org.jacorb.orb.ORB) orb).addObjectKey(getShortcut(), ior_);
    }

    ////////////////////////////////////////

    protected abstract AbstractEventChannel newEventChannel() throws ConfigurationException;

    protected abstract org.omg.CORBA.Object create_abstract_channel(Property[] admin,
            Property[] qos, IntHolder id) throws UnsupportedAdmin, UnsupportedQoS;

    protected abstract String getObjectName();

    protected abstract String getShortcut();

    protected abstract Servant getServant();

    ////////////////////////////////////////

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
            eventChannelFactoryPOA_.deactivate_object(eventChannelFactoryPOA_
                    .servant_to_id(getServant()));
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

        channelManager_.dispose();

        container_.dispose();

        getORB().shutdown(true);
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

        if (logger_.isDebugEnabled())
        {
            logger_.debug("created channel_servant id=" + id.value);
        }

        return _eventChannelServant;
    }

    protected int createChannelIdentifier()
    {
        return eventChannelIDPool_.increment();
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
        Thread _shutdown = new Thread()
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

        int _estimatedShutdowntime = _numberOfClients * _connectionTimeout;

        if (logger_.isInfoEnabled())
        {
            logger_.info("Connected Clients: " + _numberOfClients);
            logger_.info("Connection Timeout: " + _connectionTimeout + " ms");
            logger_.info("Estimated Shutdowntime: " + _estimatedShutdowntime + " ms");
        }

        // estimate 4000ms shutdowntime per channel
        cb.needTime(_estimatedShutdowntime);

        logger_.info("NotificationService is going down");

        dispose();

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

    private static AbstractChannelFactory newChannelFactory(PicoContainer container, ORB orb,
            boolean typed) throws UserException
    {
        if (typed)
        {
            return new TypedEventChannelFactoryImpl(container, orb);
        }

        return new EventChannelFactoryImpl(container, orb);
    }

    public static AbstractChannelFactory newFactory(PicoContainer container, final ORB orb,
            boolean startThread, Properties props) throws Exception
    {
        AbstractChannelFactory _factory = newChannelFactory(container, orb, "on".equals(props
                .get(Attributes.ENABLE_TYPED_CHANNEL)));

        // force activation
        _factory.activate();

        _factory.printIOR(props);

        _factory.printCorbaLoc(props);

        _factory.writeFile(props);

        _factory.registerName(props);

        _factory.startChannels(props);

        if (startThread)
        {
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
        }

        return _factory;
    }

    public static AbstractChannelFactory newFactory(final ORB orb, boolean startThread,
            Properties props) throws Exception
    {
        return newFactory(null, orb, startThread, props);
    }

    public static AbstractChannelFactory newFactory(PicoContainer container, Properties props)
            throws Exception
    {
        props.put("jacorb.implname", STANDARD_IMPL_NAME);

        ORB _orb = ORB.init(new String[] {}, props);

        AbstractChannelFactory factory = newFactory(container, _orb, true, props);

        // factory.startChannels(1);

        return factory;
    }

    public static AbstractChannelFactory newFactory(Properties props) throws Exception
    {
        return newFactory(null, props);
    }

    private void registerName(Properties props) throws Exception
    {
        registerName(props.getProperty(Attributes.REGISTER_NAME_ID), props.getProperty(
                Attributes.REGISTER_NAME_KIND, ""));
    }

    private synchronized void registerName(String nameId, String nameKind) throws Exception
    {
        if (nameId == null)
        {
            return;
        }

        namingContext_ = NamingContextHelper.narrow(getORB().resolve_initial_references(
                "NameService"));

        if (namingContext_ == null)
        {
            throw new ConfigurationException("could not resolve initial reference 'NameService'");
        }

        NameComponent[] _name = new NameComponent[] { new NameComponent(nameId, nameKind) };

        if (logger_.isInfoEnabled())
        {
            logger_.info("namingContext.rebind(" + nameId
                    + ((nameKind != null && nameKind.length() > 0) ? ("." + nameKind) : "")
                    + " => " + getCorbaLoc() + ")");
        }

        namingContext_.rebind(_name, thisRef_);

        registeredName_ = _name;
    }

    private synchronized void unregisterName() throws Exception
    {
        if (namingContext_ != null)
        {
            if (registeredName_ != null)
            {
                namingContext_.unbind(registeredName_);

                registeredName_ = null;
            }
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

    private void writeFile(Properties props)
    {
        String _iorFileName = (String) props.get(Attributes.IOR_FILE);

        if (_iorFileName != null)
        {
            try
            {
                PrintWriter out = new PrintWriter(new FileWriter(_iorFileName));
                try
                {
                    out.println(getIOR());
                    out.flush();
                } finally {
                    out.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public POA _default_POA()
    {
        return eventChannelFactoryPOA_;
    }
}