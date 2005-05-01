package org.jacorb.notification.servant;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.FilterManager;
import org.jacorb.notification.IContainer;
import org.jacorb.notification.OfferManager;
import org.jacorb.notification.SubscriptionManager;
import org.jacorb.notification.conf.Attributes;
import org.jacorb.notification.conf.Default;
import org.jacorb.notification.engine.TaskProcessor;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.interfaces.FilterStage;
import org.jacorb.notification.util.DisposableManager;
import org.jacorb.notification.util.QoSPropertySet;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.NamedPropertyRangeSeqHolder;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.QoSAdminOperations;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyActive;
import org.omg.CosNotifyChannelAdmin.ConnectionAlreadyInactive;
import org.omg.CosNotifyChannelAdmin.NotConnected;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterAdminOperations;
import org.omg.CosNotifyFilter.FilterNotFound;
import org.omg.CosNotifyFilter.MappingFilter;
import org.omg.CosNotifyFilter.MappingFilterHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.picocontainer.PicoContainer;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxy implements FilterAdminOperations, QoSAdminOperations,
        FilterStage, Disposable, ManageableServant, Configurable
{
    private final MappingFilter nullMappingFilterRef_;

    protected final boolean isIDPublic_;

    protected final Logger logger_;

    private final SynchronizedBoolean connected_ = new SynchronizedBoolean(false);

    protected final QoSPropertySet qosSettings_;

    protected final Integer id_;

    protected final OfferManager offerManager_;

    protected final SubscriptionManager subscriptionManager_;

    protected Servant thisServant_;

    protected MappingFilter lifetimeFilter_;

    protected MappingFilter priorityFilter_;

    /**
     * delegate for FilterAdminOperations
     */
    private final FilterManager filterManager_;

    private final SynchronizedBoolean destroyed_ = new SynchronizedBoolean(false);

    private final SynchronizedBoolean disposeInProgress_ = new SynchronizedBoolean(false);

    private final SynchronizedInt errorCounter_ = new SynchronizedInt(0);

    private final POA poa_;

    private final ORB orb_;

    private final TaskProcessor taskProcessor_;

    private boolean isInterFilterGroupOperatorOR_;

    private final boolean disposedProxyDisconnectsClient_;

    private final SynchronizedBoolean active_ = new SynchronizedBoolean(true);

    private final DisposableManager disposables_ = new DisposableManager();

    private final PicoContainer container_;

    ////////////////////////////////////////

    protected AbstractProxy(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        id_ = new Integer(admin.getProxyID());
        isIDPublic_ = admin.isIDPublic();
        container_ = admin.getContainer();

        orb_ = orb;
        poa_ = poa;
        taskProcessor_ = taskProcessor;

        offerManager_ = offerManager;
        subscriptionManager_ = subscriptionManager;

        filterManager_ = new FilterManager();

        nullMappingFilterRef_ = MappingFilterHelper.narrow(orb.string_to_object(orb
                .object_to_string(null)));

        logger_ = ((org.jacorb.config.Configuration) conf).getNamedLogger(getClass().getName());

        disposedProxyDisconnectsClient_ = conf.getAttribute(
                Attributes.DISPOSE_PROXY_CALLS_DISCONNECT,
                Default.DEFAULT_DISPOSE_PROXY_CALLS_DISCONNECT).equals("on");

        qosSettings_ = new QoSPropertySet(conf, QoSPropertySet.PROXY_QOS);

        configure(conf);
    }

    public void configure(Configuration conf)
    {
        // no op
    }

    ////////////////////////////////////////

    public void addDisposeHook(Disposable d)
    {
        disposables_.addDisposable(d);
    }

    public boolean isIDPublic()
    {
        return isIDPublic_;
    }

    protected POA getPOA()
    {
        return poa_;
    }

    protected ORB getORB()
    {
        return orb_;
    }

    protected TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }

    //////////////////////////////////////////////////////
    // delegate FilterAdmin Operations to FilterManager //
    //////////////////////////////////////////////////////

    public final int add_filter(Filter filter)
    {
        return filterManager_.add_filter(filter);
    }

    public final void remove_filter(int n) throws FilterNotFound
    {
        filterManager_.remove_filter(n);
    }

    public final Filter get_filter(int n) throws FilterNotFound
    {
        return filterManager_.get_filter(n);
    }

    public final int[] get_all_filters()
    {
        return filterManager_.get_all_filters();
    }

    public final void remove_all_filters()
    {
        filterManager_.remove_all_filters();
    }

    ////////////////////////////////////////

    // TODO implement
    public void validate_event_qos(Property[] qosProps, NamedPropertyRangeSeqHolder propSeqHolder)
    {
        throw new NO_IMPLEMENT();
    }

    public final void validate_qos(Property[] props, NamedPropertyRangeSeqHolder propertyRange)
            throws UnsupportedQoS
    {
        qosSettings_.validate_qos(props, propertyRange);
    }

    public final void set_qos(Property[] qosProps) throws UnsupportedQoS
    {
        if (qosSettings_ != null)
        {
            qosSettings_.set_qos(qosProps);
        }
    }

    public final Property[] get_qos()
    {
        return qosSettings_.get_qos();
    }

    public final void priority_filter(MappingFilter filter)
    {
        priorityFilter_ = filter;
    }

    public final MappingFilter priority_filter()
    {
        if (priorityFilter_ == null)
        {
            return nullMappingFilterRef_;
        }

        return priorityFilter_;
    }

    public final MappingFilter lifetime_filter()
    {
        if (lifetimeFilter_ == null)
        {
            return nullMappingFilterRef_;
        }

        return lifetimeFilter_;
    }

    public final void lifetime_filter(MappingFilter filter)
    {
        lifetimeFilter_ = filter;
    }

    public final Integer getID()
    {
        return id_;
    }

    /**
     * Override this method from the Servant baseclass. Fintan Bolton in his book "Pure CORBA"
     * suggests that you override this method to avoid the risk that a servant object (like this
     * one) could be activated by the <b>wrong </b> POA object.
     */
    public final POA _default_POA()
    {
        return getPOA();
    }

    public final List getFilters()
    {
        return filterManager_.getFilters();
    }

    public final void deactivate()
    {
        logger_.info("deactivate Proxy");

        try
        {
            byte[] _oid = getPOA().servant_to_id(getServant());
            getPOA().deactivate_object(_oid);
        } catch (Exception e)
        {
            logger_.fatalError("Couldn't deactivate Proxy", e);
        }
    }

    private void tryDisconnectClient()
    {
        try
        {
            if (disposedProxyDisconnectsClient_ && connected_.get())
            {
                logger_.info("disconnect_client");

                disconnectClient();
            }
        } catch (Exception e)
        {
            logger_.error("disconnect_client raised an unexpected error: " + "ignore", e);
        } finally
        {
            connected_.set(false);
        }
    }

    public final boolean isDisposed()
    {
        return destroyed_.get();
    }

    protected void checkDestroyStatus() throws OBJECT_NOT_EXIST
    {
        if (!destroyed_.commit(false, true))
        {
            logger_.fatalError("dispose has been called twice");

            throw new OBJECT_NOT_EXIST();
        }
    }

    public final void destroy()
    {
        checkDestroyStatus();

        container_.dispose();
        
        List list = container_.getComponentInstancesOfType(IContainer.class);
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            IContainer element = (IContainer) i.next();
            element.destroy();
        }
    }

    public void dispose()
    {
        logger_.info("Destroy Proxy " + id_);

        disposeInProgress_.set(true);

        //////////////////////////////

        tryDisconnectClient();

        //////////////////////////////

        deactivate();

        //////////////////////////////

        removeListener();

        //////////////////////////////

        remove_all_filters();

        //////////////////////////////

        disposables_.dispose();
    }

    public abstract ProxyType MyType();

    void setInterFilterGroupOperatorOR(boolean b)
    {
        isInterFilterGroupOperatorOR_ = b;
    }

    public final boolean hasInterFilterGroupOperatorOR()
    {
        return isInterFilterGroupOperatorOR_;
    }

    public final boolean isConnected()
    {
        return !disposeInProgress_.get() && connected_.get();
    }

    public final boolean hasLifetimeFilter()
    {
        return lifetimeFilter_ != null;
    }

    public final boolean hasPriorityFilter()
    {
        return priorityFilter_ != null;
    }

    public final MappingFilter getLifetimeFilter()
    {
        return lifetimeFilter_;
    }

    public final MappingFilter getPriorityFilter()
    {
        return priorityFilter_;
    }

    public void resetErrorCounter()
    {
        errorCounter_.set(0);
    }

    public final int getErrorCounter()
    {
        return errorCounter_.get();
    }

    public final int incErrorCounter()
    {
        return errorCounter_.increment();
    }

    protected boolean isSuspended()
    {
        return !active_.get();
    }

    public final void suspend_connection() throws NotConnected, ConnectionAlreadyInactive
    {
        checkIsConnected();

        if (!active_.commit(true, false))
        {
            throw new ConnectionAlreadyInactive();
        }

        connectionSuspended();
    }

    /**
     * this is an extension point.
     */
    protected void connectionSuspended()
    {
        // No Op
    }

    public final void resume_connection() throws NotConnected, ConnectionAlreadyActive
    {
        checkIsConnected();

        if (!active_.commit(false, true))
        {
            throw new ConnectionAlreadyActive();
        }

        connectionResumed();
    }

    /**
     * this is an extension point.
     * invoked when resume_connection was called successfully.
     */
    protected void connectionResumed()
    {
        // NO OP
    }

    protected void checkIsConnected() throws NotConnected
    {
        if (!connected_.get())
        {
            throw new NotConnected();
        }
    }

    protected void checkIsNotConnected() throws AlreadyConnected
    {
        if (connected_.get())
        {
            throw new AlreadyConnected();
        }
    }

    protected void checkStillConnected() throws Disconnected
    {
        if (!connected_.get())
        {
            logger_.fatalError("access on a not connected proxy");

            destroy();

            throw new Disconnected();
        }
    }

    protected void connectClient(org.omg.CORBA.Object client)
    {
        connected_.set(true);
    }

    /**
     * invoke the proxy specific disconnect method.
     */
    protected abstract void disconnectClient();

    protected abstract Servant getServant();

    protected void handleDisconnected(Disconnected e)
    {
        logger_.fatalError("Illegal state: Client think it's disconnected. "
                + "Proxy thinks Client is still connected. The Proxy will be destroyed.", e);

        destroy();
        //container_.dispose();
    }

    protected abstract void removeListener();
}