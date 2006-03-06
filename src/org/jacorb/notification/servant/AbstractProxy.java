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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.jacorb.notification.interfaces.JMXManageable;
import org.jacorb.notification.lifecycle.IServantLifecyle;
import org.jacorb.notification.lifecycle.ServantLifecyleControl;
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
import org.picocontainer.PicoContainer;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * @jmx.mbean
 * @jboss.xmbean 
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractProxy implements FilterAdminOperations, QoSAdminOperations,
        FilterStage, IServantLifecyle, Configurable, JMXManageable, AbstractProxyMBean
{
    private final MappingFilter nullMappingFilterRef_;

    private final boolean isIDPublic_;

    protected final Logger logger_;

    private final AtomicBoolean connected_ = new AtomicBoolean(false);

    protected final QoSPropertySet qosSettings_;

    private final Integer id_;

    protected final OfferManager offerManager_;

    protected final SubscriptionManager subscriptionManager_;

    private MappingFilter lifetimeFilter_;

    private MappingFilter priorityFilter_;

    /**
     * delegate for FilterAdminOperations
     */
    private final FilterManager filterManager_;

    private final AtomicBoolean destroyed_ = new AtomicBoolean(false);

    private final AtomicBoolean disposeInProgress_ = new AtomicBoolean(false);

    private final AtomicInteger errorCounter_ = new AtomicInteger(0);

    private final POA poa_;

    private final ORB orb_;

    private final TaskProcessor taskProcessor_;

    private boolean isInterFilterGroupOperatorOR_;

    private final boolean disposedProxyDisconnectsClient_;

    private final AtomicBoolean active_ = new AtomicBoolean(true);

    private final DisposableManager disposables_ = new DisposableManager();

    private final PicoContainer container_;

    private org.omg.CORBA.Object client_;

    private final String parentMBean_;

    protected final Set eventTypes_ = new HashSet();
    
    private JMXManageable.JMXCallback jmxCallback_;

    protected Configuration config_;
    
    private final ServantLifecyleControl servantLifecycle_;
    
    // //////////////////////////////////////

    protected AbstractProxy(IAdmin admin, ORB orb, POA poa, Configuration conf,
            TaskProcessor taskProcessor, OfferManager offerManager,
            SubscriptionManager subscriptionManager)
    {
        parentMBean_ = admin.getAdminMBean();
        id_ = new Integer(admin.getProxyID());
        isIDPublic_ = admin.isIDPublic();
        container_ = admin.getContainer();

        orb_ = orb;
        poa_ = poa;
        taskProcessor_ = taskProcessor;
        
        offerManager_ = offerManager;
        subscriptionManager_ = subscriptionManager;

        filterManager_ = new FilterManager();

        nullMappingFilterRef_ = MappingFilterHelper.narrow(orb.string_to_object(orb.object_to_string(null)));

        logger_ = ((org.jacorb.config.Configuration) conf).getNamedLogger(getClass().getName());

        disposedProxyDisconnectsClient_ = conf.getAttribute(
                Attributes.DISPOSE_PROXY_CALLS_DISCONNECT,
                Default.DEFAULT_DISPOSE_PROXY_CALLS_DISCONNECT).equals("on");

        qosSettings_ = new QoSPropertySet(conf, QoSPropertySet.PROXY_QOS);

        servantLifecycle_ = new ServantLifecyleControl(this, conf);
        
        configure(conf);
    }

    public void configure(Configuration conf)
    {
        config_ = conf;
    }

    // //////////////////////////////////////

    public void registerDisposable(Disposable d)
    {
        disposables_.addDisposable(d);
    }

    public boolean isIDPublic()
    {
        return isIDPublic_;
    }

    public final POA getPOA()
    {
        return poa_;
    }

    protected ORB getORB()
    {
        return orb_;
    }

    public final org.omg.CORBA.Object activate()
    {
        return servantLifecycle_.activate();
    }
    
    protected TaskProcessor getTaskProcessor()
    {
        return taskProcessor_;
    }

    // ////////////////////////////////////////////////////
    // delegate FilterAdmin Operations to FilterManager //
    // ////////////////////////////////////////////////////

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

    // //////////////////////////////////////

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
        qosSettings_.set_qos(qosProps);
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

    public final List getFilters()
    {
        return filterManager_.getFilters();
    }

    public final void deactivate()
    {
        servantLifecycle_.deactivate();
    }

    private void tryDisconnectClient()
    {
        try
        {
            if (disposedProxyDisconnectsClient_ && connected_.get())
            {
                logger_.info("disconnect_client");

                disconnectClient();
                
                client_._release();
            }
        } catch (Exception e)
        {
            logger_.info("disconnect_client raised an unexpected error: will be ignored", e);
        } finally
        {
            connected_.set(false);
            client_ = null;
        }
    }

    public final boolean isDestroyed()
    {
        return destroyed_.get();
    }

    protected void checkDestroyStatus() throws OBJECT_NOT_EXIST
    {
        if (!destroyed_.compareAndSet(false, true))
        {
            logger_.error("Already destroyed");

            throw new OBJECT_NOT_EXIST();
        }
    }

    /**
     * @jmx.managed-operation description = "Destroy this Proxy" impact = "ACTION" 
     */
    public final void destroy()
    {
        checkDestroyStatus();

        container_.dispose();

        final List list = container_.getComponentInstancesOfType(IContainer.class);
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

        // ////////////////////////////

        tryDisconnectClient();

        clientDisconnected();
        
        // ////////////////////////////

        removeListener();

        // ////////////////////////////

        remove_all_filters();

        // ////////////////////////////

        disposables_.dispose();

        deactivate();
    }

    protected abstract void clientDisconnected();
    
    public abstract ProxyType MyType();

    void setInterFilterGroupOperatorOR(boolean b)
    {
        isInterFilterGroupOperatorOR_ = b;
    }

    public final boolean hasInterFilterGroupOperatorOR()
    {
        return isInterFilterGroupOperatorOR_;
    }

    /**
     * @jmx.managed-attribute description = "Connection Status."
     *                        access = "read-only"
     */
    public final boolean getConnected()
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

    /**
     * @jmx.managed-operation impact = "ACTION" 
     *                        description = "reset the error counter to its initial value" 
     */
    public void resetErrorCounter()
    {
        errorCounter_.set(0);
    }
 
    /**
     * @jmx.managed-attribute description = "error counter" 
     *                        access = "read-only"
     */
    public final int getErrorCounter()
    {
        return errorCounter_.get();
    }

    public final int incErrorCounter()
    {
        return errorCounter_.getAndIncrement();
    }

    public boolean isSuspended()
    {
        return !active_.get();
    }

    public final void suspend_connection() throws NotConnected, ConnectionAlreadyInactive
    {
        checkIsConnected();

        if (!active_.compareAndSet(true, false))
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

        if (!active_.compareAndSet(false, true))
        {
            throw new ConnectionAlreadyActive();
        }

        connectionResumed();
    }

    /**
     * this is an extension point. invoked when resume_connection was called successfully.
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
        
        client_ = client;
    }

    /**
     * invoke the proxy specific disconnect method.
     */
    protected abstract void disconnectClient();

    
    protected void handleDisconnected(Disconnected e)
    {
        logger_.fatalError("Illegal state: Client think it's disconnected. "
                + "Proxy thinks Client is still connected. The Proxy will be destroyed.", e);

        destroy();
    }

    protected abstract void removeListener();

    public final String getJMXObjectName()
    {
        return parentMBean_ + ", proxy=" + getMBeanName();
    }
    
    public final String getMBeanName()
    {
        return getMBeanType() + "-" + getID();
    }

    protected String getMBeanType()
    {
        String clazzName = getClass().getName();
        
        String rawClazz = clazzName.substring(clazzName.lastIndexOf('.') + 1);
        
        return rawClazz.substring(0, rawClazz.length() - "Impl".length());
    }
    
    public String[] getJMXNotificationTypes()
    {
        return (String[]) eventTypes_.toArray(new String[eventTypes_.size()]);
    }
    
    public void setJMXCallback(JMXManageable.JMXCallback callback)
    {
        jmxCallback_ = callback;
    }
    
    protected void sendNotification(String type, String message)
    {
        if (jmxCallback_ != null)
        {
            jmxCallback_.sendJMXNotification(type, message);
        }
    }
    
    protected void sendNotification(String type, String message, Object payload)
    {
        if (jmxCallback_ != null)
        {
            jmxCallback_.sendJMXNotification(type, message, payload);
        }
    }
    
    /**
     * @jmx.managed-attribute description = "current Status for this Proxy (NOT CONNECTED|ACTIVE|SUSPENDED|DESTROYED)"
     *                        access = "read-only"
     */
    public String getStatus()
    {
        final String _status;
        
        if (destroyed_.get())
        {
            _status =  "DESTROYED";
        } 
        else if (!connected_.get())
        {
            _status = "NOT CONNECTED";
        } else
        {
            _status = active_.get() ? "ACTIVE" : "SUSPENDED";
        }
        
        return _status;
    }
    
    /**
     * @jmx.managed-attribute description = "IOR of the connected client" 
     *                        access = "read-only"
     */
    public String getClientIOR()
    {
        return (client_ != null) ? orb_.object_to_string(client_) : "";
    }
    
    /**
     * @jmx.managed-attribute description = "InterFilterGroupOperator used for this proxy" 
     *                        access = "read-only" 
     *                        currencyTimeLimit = "2147483647"
     */
    public String getInterFilterGroupOperator()
    {
        return isInterFilterGroupOperatorOR_ ? "OR_OP" : "AND_OP";
    }
}