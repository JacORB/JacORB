package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.jacorb.poa.util.*;
import org.jacorb.poa.except.*;

import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.ssl.SSLPolicy;
import org.jacorb.ssl.SSLPolicyValue;
import org.jacorb.ssl.SSL_POLICY_TYPE;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.POAManagerPackage.State;

import org.omg.BiDirPolicy.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import java.util.*;

/**
 * The main POA class, an implementation of
 * <code>org.omg.PortableServer.POA</code>
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class POA
    extends _POALocalBase
    implements Configurable
{
    // my orb instance
    private org.jacorb.orb.ORB        orb;

    /** the configuration object for this POA instance */
    private org.jacorb.config.Configuration configuration = null;

    /** the POA logger instance */
    private Logger logger = null;
    private byte[] implName = null;
    private byte[] serverId = null;

    /** used to hold the POA name for logging */
    private String logPrefix = "<unset>";

    // for listening POA Events
    private POAListener           poaListener;
    // for monitoring
    private POAMonitor            monitor;

    private POAManager            poaManager;
    private POA                   parent;
    private String                name;
    private String                qualifiedName;

    // name -> child POA's
    private Hashtable             childs = new Hashtable();

    Servant                       defaultServant;
    ServantManager                servantManager;
    private AdapterActivator      adapterActivator;

    private AOM                   aom;
    // thread for handle Requests
    private RequestController     requestController;

    // poa identity and a counter for oid generation
    private byte[]                poaId;
    private byte[]                watermark;
    private long                  objectIdCount;

    /* policies */

    // default: ORB_CTRL_MODEL
    protected ThreadPolicy        threadPolicy;

    // default: TRANSIENT
    protected LifespanPolicy      lifespanPolicy;

    // default: UNIQUE_ID
    protected IdUniquenessPolicy  idUniquenessPolicy;

    // default: SYSTEM_ID
    protected IdAssignmentPolicy  idAssignmentPolicy;

    // default: RETAIN
    protected ServantRetentionPolicy    servantRetentionPolicy;

    // default: USE_ACTIVE_OBJECT_MAP_ONLY
    protected RequestProcessingPolicy   requestProcessingPolicy;

    // default: NO_IMPLICIT_ACTIVATION
    protected ImplicitActivationPolicy  implicitActivationPolicy;

    // default: NORMAL
    protected BidirectionalPolicy       bidirectionalPolicy;

    // default: SSL_NOT_REQUIRED
    protected SSLPolicy                 sslPolicy;

    private Hashtable all_policies = null;

    /** key: , value: CORBA.Object */
    private Hashtable createdReferences;

    // stores the etherealize_objects value from the first call of destroy
    private boolean               etherealize;

    // synchronisation stuff
    private int                   shutdownState = POAConstants.NOT_CALLED;
    private java.lang.Object      poaCreationLog = new java.lang.Object();
    private java.lang.Object      poaDestructionLog = new java.lang.Object();
    private java.lang.Object      unknownAdapterLog = new java.lang.Object();
    private boolean               unknownAdapterCalled;

    private boolean configured = false;

//     private POA()
//     {
//     }

    private POA(org.jacorb.orb.ORB _orb,
                String _name,
                POA _parent,
                POAManager _poaManager,
                org.omg.CORBA.Policy[] policies)
    {
        orb = _orb;
        name = _name;
        parent = _parent;
        poaManager = _poaManager;
        logPrefix = "POA " + name;

        all_policies = new Hashtable();
        createdReferences = new Hashtable();

        if (policies != null)
        {
            for (int i=0; i<policies.length; i++)
            {
                all_policies.put( new Integer( policies[i].policy_type() ),
                                  policies[i]);

                switch (policies[i].policy_type())
                {
                    case THREAD_POLICY_ID.value :
                    threadPolicy =
                        (org.omg.PortableServer.ThreadPolicy) policies[i];
                    break;
                    case LIFESPAN_POLICY_ID.value :
                    lifespanPolicy =
                        (org.omg.PortableServer.LifespanPolicy) policies[i];
                    break;
                    case ID_UNIQUENESS_POLICY_ID.value :
                    idUniquenessPolicy =
                        (org.omg.PortableServer.IdUniquenessPolicy) policies[i];
                    break;
                    case ID_ASSIGNMENT_POLICY_ID.value :
                    idAssignmentPolicy =
                        (org.omg.PortableServer.IdAssignmentPolicy) policies[i];
                    break;
                    case SERVANT_RETENTION_POLICY_ID.value :
                    servantRetentionPolicy =
                        (org.omg.PortableServer.ServantRetentionPolicy) policies[i];
                    break;
                    case REQUEST_PROCESSING_POLICY_ID.value :
                    requestProcessingPolicy =
                        (org.omg.PortableServer.RequestProcessingPolicy) policies[i];
                    break;
                    case IMPLICIT_ACTIVATION_POLICY_ID.value :
                    implicitActivationPolicy =
                        (org.omg.PortableServer.ImplicitActivationPolicy) policies[i];
                    break;
                    case BIDIRECTIONAL_POLICY_TYPE.value :
                    bidirectionalPolicy =
                        (org.omg.BiDirPolicy.BidirectionalPolicy) policies[i];
                    break;
                    case SSL_POLICY_TYPE.value :
                    sslPolicy = (SSLPolicy) policies[i];
                    break;
                }
            }
        }

        //check if BiDir policy tell us to use BiDirGIOP (for the
        //whole ORB)
        if( bidirectionalPolicy != null )
        {
            org.jacorb.orb.giop.BiDirPolicy bdp =
                (org.jacorb.orb.giop.BiDirPolicy) bidirectionalPolicy;

            if( bdp.useBiDirGIOP() )
            {
                _orb.turnOnBiDirGIOP();
            }
        }

//         watermark = generateWatermark();

//         aom = isRetain() ? new AOM( isUniqueId(), isSingleThreadModel(), logger) : null;

//         // GB: modified
//         requestController = new RequestController(this, orb, aom);
//         poaManager.registerPOA(this);
//         monitor = new POAMonitorLightImpl();

//         monitor.init( this, aom,
//                       requestController.getRequestQueue(),
//                       requestController.getPoolManager(),
//                       "POA " + name );

//         monitor.openMonitor();
//         if (poaListener != null)
//             poaListener.poaCreated(this);
    }


    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.poa");

        implName = 
            configuration.getAttribute("jacorb.implname","standardImplName").getBytes();
        serverId = 
            String.valueOf((long)(Math.random()*9999999999L)).getBytes();      

        watermark = generateWatermark();

        aom = isRetain() ? new AOM( isUniqueId(), isSingleThreadModel(), logger) : null;

        // GB: modified
        requestController = new RequestController(this, orb, aom);
        requestController.configure(configuration);

        poaManager.registerPOA(this);
        monitor = new POAMonitorLightImpl();
        monitor.init( this, aom,
                      requestController.getRequestQueue(),
                      requestController.getPoolManager(),
                      "POA " + name );

        monitor.openMonitor();
        if (poaListener != null)
            poaListener.poaCreated(this);

        monitor.configure(configuration);

        if (logger.isDebugEnabled())
        {
            logger.debug("POA " + name + " ready");
        }
        configured = true;
    }


    /**
     * Everybody who is interested in poa events can use this method
     * to register an event listener. The poa will pass the register calls
     * to the right components
     */

    public void _addPOAEventListener(EventListener listener)
    {
        if (!configured)
            throw new Error("POA: not configured!");

        if (listener instanceof POAListener)
            addPOAListener((POAListener)listener);

        if (listener instanceof AOMListener && aom != null)
            aom.addAOMListener((AOMListener)listener);

        if (listener instanceof RequestQueueListener)
            requestController.getRequestQueue().addRequestQueueListener((RequestQueueListener)listener);

        if (listener instanceof RPPoolManagerListener)
            requestController.getPoolManager().addRPPoolManagerListener((RPPoolManagerListener)listener);
    }

    /**
     * called from orb, returns a registered child poa,
     * if no child poa exists a adapter activator will used
     * to create a new poa unter this name
     */

    public org.jacorb.poa.POA _getChildPOA( String adapter_name )
        throws ParentIsHolding
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        POA child = (POA) childs.get(adapter_name);

        if (child == null || child.isDestructionApparent())
        {

            if (adapterActivator == null)
                throw new org.omg.CORBA.OBJECT_NOT_EXIST("no adapter activator exists for " +
                                                         adapter_name);

            if (isHolding())
                throw new ParentIsHolding();

            if (isDiscarding())
                throw new org.omg.CORBA.TRANSIENT("a parent poa is in discarding state");

            if (isInactive())
                throw new org.omg.CORBA.OBJ_ADAPTER("a parent poa is in inactive state");

            /* poa should be active */

            boolean successful = false;

            if (isSingleThreadModel())
            {
                /* all invocations an adapter activator are serialized
                   if the single thread model is in use */
                synchronized (unknownAdapterLog)
                {
                    while (unknownAdapterCalled)
                    {
                        try
                        {
                            unknownAdapterLog.wait();
                        }
                        catch (InterruptedException e) {
                        }
                    }
                    unknownAdapterCalled = true;
                    try
                    {
                        successful =
                        the_activator().unknown_adapter(this, POAUtil.unmaskStr(adapter_name));
                    }
                    finally
                    {
                        unknownAdapterCalled = false;
                        unknownAdapterLog.notifyAll();
                    }
                }
            }
            else
            {
                /* ORB_CTRL_MODEL */
                successful = the_activator().unknown_adapter(this, POAUtil.unmaskStr(adapter_name));
            }

            /* unknown_adapter doesn't return until the
               poa is created and initialized */
            if (successful)
            {
                if ((child = (POA) childs.get(adapter_name)) == null)
                {
                    throw new POAInternalError("error: unknown_adapter returns true, but the child poa doesn't extist");
                }

            }
            else
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST("poa activation is failed");
            }
        }
        return child;
    }

    /**
     * returns the complete poa name
     */

    public String _getQualifiedName()
    {
        if (qualifiedName == null)
        {
            if (parent == null)
            {
                qualifiedName = "";
            }
            else if (parent.the_parent() == null)
            {
                qualifiedName = name;
            }
            else
            {
                qualifiedName = parent._getQualifiedName() +
                POAConstants.OBJECT_KEY_SEPARATOR + name;
            }
        }
        return qualifiedName;
    }

    /**
     * called from orb for handing over a request
     */

    public void _invoke(ServerRequest request)
        throws WrongAdapter
    {
        if (!configured)
            throw new Error("POA: not configured!");

        synchronized(poaDestructionLog)
        {
            checkDestructionApparent ();

            // if the request is for this poa check whether the object
            // key is generated from him
            if (request.remainingPOAName() == null)
            {
                if (!previouslyGeneratedObjectKey(request.objectKey()))
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn(logPrefix +
                                    " rid: " + request.requestId() +
                                    " opname: " + request.operation() +
                                    " _invoke: object key not previously generated!");
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(logPrefix + 
                                         " ObjectKey : " + org.jacorb.orb.util.CorbaLoc.parseKey(request.objectKey()) +
                                         " to POA Id : " + org.jacorb.orb.util.CorbaLoc.parseKey(getPOAId()) + " mismatch.");
                        }
                    }
                    throw new WrongAdapter();
                }
                if (isSystemId() && !previouslyGeneratedObjectId(request.objectId()) )
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn(logPrefix +
                                    " rid: " + request.requestId() +
                                    " opname: " + request.operation() +
                                    " _invoke: object id not previously generated!");
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(logPrefix + 
                                         " ObjectId : " + org.jacorb.orb.util.CorbaLoc.parseKey(request.objectId()) +
                                         " to POA watermark : " + org.jacorb.orb.util.CorbaLoc.parseKey(watermark) + " mismatch.");
                        }
                    }
                    throw new WrongAdapter();
                }
            }


            try
            {
                //  pass the  request  to the  request controller  the
                // operation returns  immediately after the request is
                // queued
                if (logger.isDebugEnabled())
                {
                    logger.debug(logPrefix +
                                 " rid: " + request.requestId() +
                                 " opname: " + request.operation() +
                                 " _invoke: queuing request");
                }
                requestController.queueRequest(request);
            }
            catch (ResourceLimitReachedException e)
            {
                throw new org.omg.CORBA.TRANSIENT("resource limit reached");
            }
        }
    }

    /**
     * called from orb to obtain the RootPOA
     */

    static public POA _POA_init(org.jacorb.orb.ORB orb)
    {
        POAManager poaMgr = new POAManager(orb);

        /* the only policy value that differs from default */
        org.omg.CORBA.Policy [] policies= null;

        policies = new org.omg.CORBA.Policy[1];

        policies[0] =
            new org.jacorb.poa.policy.ImplicitActivationPolicy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

        POA rootPOA =
            new POA(orb, POAConstants.ROOT_POA_NAME, null, poaMgr, policies);

        return rootPOA;
    }


    /**
     * Unregister an event listener. The poa will pass the
     * unregister calls to the right components
     */

    public void _removePOAEventListener(EventListener listener)
    {
        if (listener instanceof POAListener)
            removePOAListener((POAListener)listener);
        if (listener instanceof AOMListener && aom != null)
            aom.removeAOMListener((AOMListener)listener);
        if (listener instanceof RequestQueueListener)
            requestController.getRequestQueue().removeRequestQueueListener((RequestQueueListener)listener);
        if (listener instanceof RPPoolManagerListener)
            requestController.getPoolManager().removeRPPoolManagerListener((RPPoolManagerListener)listener);
    }


    public byte[] activate_object(Servant servant)
        throws ServantAlreadyActive, WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent ();

        if (!isRetain() || !isSystemId())
            throw new WrongPolicy();

        byte[] objectId = generateObjectId();

        try
        {
            aom.add(objectId, servant);

            orb.set_delegate( servant );
        }
        catch (ObjectAlreadyActive e)
        {
            throw new POAInternalError("error: object already active (activate_object)");
        }

        return objectId;
    }

    public void activate_object_with_id( byte[] oid, Servant servant )
        throws  ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent ();

        if( oid == null )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Cannot activate_object_with_id with null ID." );
        }

        if ( !isRetain() )
            throw new WrongPolicy();

        if ( isSystemId() && !previouslyGeneratedObjectId(oid) )
        {
            if (logger.isWarnEnabled())
            {
                logger.warn(logPrefix + "oid: " + POAUtil.convert(oid) +
                            " - activate_object_with_id: oid not previously generated!");
            }
            throw new org.omg.CORBA.BAD_PARAM();
        }

        aom.add( oid, servant );

        orb.set_delegate( servant );
    }

    protected synchronized void addPOAListener( POAListener listener )
    {
        poaListener = EventMulticaster.add(poaListener, listener);
    }

    protected void changeToActive()
    {
        if (poaListener != null)
            poaListener.poaStateChanged(this, POAConstants.ACTIVE);
        monitor.changeState("changed to active...");

        // notify everybody who is waiting for request completion
        requestController.resetPreviousCompletionCall();
        // continue the request dispatching
        requestController.continueToWork();

        monitor.changeState("active");
    }

    protected void changeToDiscarding() {

        if (poaListener != null) poaListener.poaStateChanged(this, POAConstants.DISCARDING);
        monitor.changeState("changed to discarding ...");

        // notify everybody who is waiting for request completion
        requestController.resetPreviousCompletionCall();
        // continue the request dispatching
        requestController.continueToWork();
        // wait for completion of all active requests
        requestController.waitForCompletion();

        monitor.changeState("discarding");
    }

    protected void changeToHolding()
    {
        if (poaListener != null)
            poaListener.poaStateChanged(this, POAConstants.HOLDING);
        monitor.changeState("changed to holding ...");

        // notify everybody who is waiting for request completion
        requestController.resetPreviousCompletionCall();
        // wait for completion of all active requests
        requestController.waitForCompletion();

        monitor.changeState("holding");
    }


    protected void changeToInactive(boolean etherealize_objects)
    {
        if (poaListener != null)
            poaListener.poaStateChanged(this, POAConstants.INACTIVE);

        monitor.changeState("changed to inactive ...");

        // notify everybody who is waiting for request completion
        requestController.resetPreviousCompletionCall();

        // continue the request dispatching
        requestController.continueToWork();

        // wait for completion of all active requests
        requestController.waitForCompletion();

        /* etherialize all active objects */
        if (etherealize && isRetain() && useServantManager())
        {
            if (logger.isInfoEnabled())
                logger.info(logPrefix + "etherialize all servants ...");

            aom.removeAll((ServantActivator) servantManager, this, true);

            if (logger.isInfoEnabled())
                logger.info(logPrefix + "etherialize all servants ...");

            if (monitor != null)
                monitor.changeState("inactive (etherialization completed)");

        }
        else
        {
            if (monitor != null)
                monitor.changeState("inactive (no etherialization)");
        }
    }


    public IdAssignmentPolicy create_id_assignment_policy (IdAssignmentPolicyValue value)
    {
        checkDestructionApparent ();
        return new org.jacorb.poa.policy.IdAssignmentPolicy (value);
    }

    public IdUniquenessPolicy create_id_uniqueness_policy (IdUniquenessPolicyValue value)
    {
        checkDestructionApparent ();
        return new org.jacorb.poa.policy.IdUniquenessPolicy (value);
    }

    public ImplicitActivationPolicy create_implicit_activation_policy
        (ImplicitActivationPolicyValue value)
    {
        checkDestructionApparent ();
        return new org.jacorb.poa.policy.ImplicitActivationPolicy (value);
    }

    public LifespanPolicy create_lifespan_policy (LifespanPolicyValue value)
    {
        checkDestructionApparent ();
        return new org.jacorb.poa.policy.LifespanPolicy (value);
    }

    /**
     * additionally raises an org.omg.CORBA.BAD_INV_ORDER exception if the poa
     * goes shutdown and this method will called (not spec.)
     */

    public org.omg.PortableServer.POA create_POA( String adapter_name,
                                                  org.omg.PortableServer.POAManager a_POAManager,
                                                  org.omg.CORBA.Policy[] policies)
        throws AdapterAlreadyExists, InvalidPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        String poa_name = POAUtil.maskStr(adapter_name);

        /* this implementation works only with a instance of org.jacorb.poa.POAManager */
        if (a_POAManager != null &&
            !(a_POAManager instanceof org.jacorb.poa.POAManager))
            throw new ApplicationError("error: the POAManager is incompatible with type \"jacorb.poa.POAManager\"!" );

        org.omg.CORBA.Policy[] policyList = null;
        if (policies != null)
        {
            // check the policy list for inconstancies
            short index = verifyPolicyList(policies);

            if (index != -1)
                throw new InvalidPolicy(index);

            // copy the policy list
            policyList = new org.omg.CORBA.Policy[policies.length];

            for (int i=0; i<policies.length; i++)
            {
                policyList[i] = policies[i].copy();
            }
        }

        POA child;

        synchronized (poaCreationLog)
        {
            if ((child = (POA)childs.get(poa_name)) != null &&
                !child.isDestructionApparent()) 
            {
                throw new AdapterAlreadyExists();
            }
            // wait for completion of a concurrent destruction process
            if (child != null)
            {
                POA aChild;
                while ((aChild = (POA)childs.get(poa_name)) != null)
                {
                    try 
                    {
                        poaCreationLog.wait();  // notification is in unregisterChild
                    } 
                    catch (InterruptedException e) {
                    }
                    // anyone else has won the race
                    if (child != aChild) 
                        throw new AdapterAlreadyExists();
                }
            }

            if (isShutdownInProgress())
                throw new org.omg.CORBA.BAD_INV_ORDER();

            POAManager aPOAManager =
                a_POAManager == null ? new POAManager(orb) : (POAManager) a_POAManager;

            child = 
                new POA(orb, poa_name, this, aPOAManager, policyList);

            try
            {
                child.configure(configuration);
            }
            catch (ConfigurationException e)
            {
                throw new org.omg.CORBA.INTERNAL(e.getMessage());
            }

            // notify a poa listener
            try
            {
                if (poaListener != null) 
                    poaListener.poaCreated(child);
            }
            catch (org.omg.CORBA.INTERNAL e)
            {
                aPOAManager.poaCreationFailed = true;
                throw e;
            }
            // register the child poa
            childs.put(poa_name, child);
        }
        return child;
    }


    /**
     * The specified repository id, which may be a null string, will become the
     * type_id of the generated object reference
     */

    public org.omg.CORBA.Object create_reference (String intf_rep_id)
        throws WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        if (!isSystemId())
        {
            throw new WrongPolicy();
        }

        return (getReference (generateObjectId (), intf_rep_id, false));
    }


    /**
     * The specified repository id, which may be a null string, will become the
     * type_id of the generated object reference
     */

    public org.omg.CORBA.Object create_reference_with_id(byte[] oid,
                                                         String intf_rep_id)
        throws WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        if (isSystemId() && !previouslyGeneratedObjectId (oid))
        {
            if (logger.isWarnEnabled())
            {
                logger.warn(logPrefix + "oid: " + POAUtil.convert(oid) +
                            "create_reference_with_id : object key not previously generated!");
            }

            throw new org.omg.CORBA.BAD_PARAM ();
        }

        return getReference (oid, intf_rep_id, false);
    }


    public RequestProcessingPolicy create_request_processing_policy(RequestProcessingPolicyValue value)
    {
        checkDestructionApparent();
        return new org.jacorb.poa.policy.RequestProcessingPolicy (value);
    }


    public ServantRetentionPolicy create_servant_retention_policy(ServantRetentionPolicyValue value)
    {
        checkDestructionApparent();
        return new org.jacorb.poa.policy.ServantRetentionPolicy(value);
    }


    public ThreadPolicy create_thread_policy(ThreadPolicyValue value)
    {
        checkDestructionApparent();
        return new org.jacorb.poa.policy.ThreadPolicy(value);
    }


    /**
     * The operation does not wait for requests or etherealization to complete
     * and always returns immediately (after deactivating the oid?)
     */


    public synchronized void deactivate_object(byte[] oid)
        throws ObjectNotActive, WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        if (!isRetain())
            throw new WrongPolicy();

        aom.remove(
            oid,
            requestController,
            useServantManager() ? (ServantActivator)servantManager : null,
            this,
            false
                  );

        createdReferences.remove( new ByteArrayKey(oid));
    }


    public void destroy( boolean etherealize_objects,
                         boolean wait_for_completion )
    {
        if (!configured)
            throw new Error("POA: not configured!");

        if (wait_for_completion && isInInvocationContext())
            throw new org.omg.CORBA.BAD_INV_ORDER();

        makeShutdownInProgress(etherealize_objects);

        /* synchronized with creationLog */
        /* child poa creations are impossible now */
        // destroy all childs first

        Enumeration en = childs.elements();
        while (en.hasMoreElements())
        {
            POA child = (POA) en.nextElement();
            child.destroy(etherealize, wait_for_completion);
        }

        Thread thread = new Thread()
        {
            public void run()
            {
                /* The apparent destruction of the POA
                   occurs  only  after  all  executing
                   requests in the POA have completed,
                   but before any calls to etherealize
                   are made. */
                requestController.waitForShutdown();
                /*  poa behaves  as  if he  is in  the
                    holding state  now and blocks until
                    all active request have comleted */
                makeDestructionApparent();
                /* unregister poa from the POAManager,
                   any calls on the poa are impossible
                   now especially  you cannot activate
                   or  deactivate  objects  (raises  a
                   OBJ_NOT_EXIST  exception),  but you
                   have   a    race   condition   with
                   currently       running      object
                   (de)activation processes */
                makeDestructionComplete();
            }
        };
        thread.start();
        if (wait_for_completion)
        {
            try
            {
                thread.join();
            } catch (InterruptedException e) {}
        }
    }


    private byte[] extractWatermark(byte[] id)
    {
        if( id.length < watermark.length )
            return new byte[0];
        else
            return IdUtil.extract(id, id.length-watermark.length, watermark.length);
    }


    /**
     * If the intended child poa is not found and activate_it is TRUE,
     * it is  possible for another thread to create  the same poa with
     * create_POA at the same  time in a race condition.  Applications
     *  should be  prepared  to  deal with  failures  from the  manual
     *  (create_POA)  or automatic  (findPOA  or unknown_adapter  from
     * AdapterActivator) POA  creation.  Another possible situation is
     * that  the poa  returned goes shutdown  but the orb  will notice
     * this situation if he will proceed with request processing.
     */
    public org.omg.PortableServer.POA find_POA( String adapter_name,
                                                boolean activate_it)
        throws AdapterNonExistent
    {
        checkDestructionApparent();

        String poa_name = POAUtil.maskStr(adapter_name);

        POA child = (POA)childs.get(poa_name);

        if (child == null || child.isDestructionApparent()) 
        {

            boolean successful = false;

            if (activate_it && the_activator() != null) {
                /* all invocations an adapter activator are serialized
                   if the single thread model is in use */
                if (isSingleThreadModel()) {
                    synchronized (unknownAdapterLog) {
                        while (unknownAdapterCalled) {
                            try {
                                unknownAdapterLog.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        unknownAdapterCalled = true;
                        try
                        {
                            successful =
                            the_activator().unknown_adapter(this, adapter_name);
                        }
                        finally {
                            unknownAdapterCalled = false;
                            unknownAdapterLog.notifyAll();
                        }
                    }

                }  else { /* ORB_CTRL_MODEL */
                    successful = the_activator().unknown_adapter(this, adapter_name);
                }
            }

            /* unknown_adapter returns not until the poa is created and initialized */
            if (successful)
            {
                if ((child = (POA) childs.get(poa_name)) == null)
                {
                    throw new POAInternalError("error: unknown_adapter returns true, but the child poa does'n extist");
                }

            }
            else
            {
                throw new AdapterNonExistent();
            }
        }
        return child;
    }


    /**
     * <code>generateObjectId</code> creates a new ObjectId for an object.
     *
     * @return a <code>byte[]</code> value.
     */
    private synchronized byte[] generateObjectId()
    {
        if (isPersistent())
        {
            return IdUtil.concat(IdUtil.createId(4), watermark);
        }
        else
        {
            // Synchonize as the increment is not an atomic operation.
            return IdUtil.concat(IdUtil.toId(objectIdCount++), watermark);
        }
    }


    private byte[] generateWatermark()
    {
        if (watermark == null)
        {
            if (isPersistent())
                watermark = IdUtil.toId(new String(getPOAId()).hashCode());
            else
                watermark = IdUtil.createId(4);
        }
        return watermark;
    }


    public Servant get_servant()
        throws NoServant, WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        if (!isUseDefaultServant())
            throw new WrongPolicy();

        if (defaultServant == null)
            throw new NoServant();
        return defaultServant;
    }


    public org.omg.PortableServer.ServantManager get_servant_manager()
        throws WrongPolicy
    {
        if (!configured)
            throw new Error("POA: not configured!");

        checkDestructionApparent();

        if (!isUseServantManager())
            throw new WrongPolicy();

        return servantManager;
    }


    protected POAMonitor getMonitor()
    {
        return monitor;
    }

    protected org.jacorb.orb.ORB getORB()
    {
        return orb;
    }

    public byte[] getPOAId()
    {
        if (poaId == null)
        {
            byte[] impl_name =
                POAUtil.maskId( (implName != null) ?
                                implName :
                                serverId );
            int in_length = impl_name.length;

            byte[] poa_name = _getQualifiedName().getBytes();
            int pn_length = poa_name.length;

            int offset = 0;
            if (pn_length == 0)
            {
                poaId = new byte[in_length];
                System.arraycopy (impl_name, 0, poaId, 0, in_length);
            }
            else
            {
                poaId = new byte[in_length + pn_length + 1];
                System.arraycopy (impl_name, 0, poaId, 0, in_length);
                offset += in_length;
                poaId[offset] = POAConstants.OBJECT_KEY_SEP_BYTE;
                offset++;
                System.arraycopy (poa_name, 0, poaId, offset, pn_length);
            }
        }
        return poaId;
    }

    protected org.omg.CORBA.Object getReference
        (byte[] oid, String intf_rep_id, boolean cache)
    {
        byte[] object_id = POAUtil.maskId (oid);
        int pid_length = getPOAId().length;
        int oid_length = object_id.length;
        byte [] object_key = new byte[pid_length + oid_length + 1];
        int offset = 0;

        System.arraycopy (getPOAId(), 0, object_key, offset, pid_length);
        offset += pid_length;
        object_key[offset] = POAConstants.OBJECT_KEY_SEP_BYTE;
        offset++;
        System.arraycopy (object_id, 0, object_key, offset, oid_length);

        ByteArrayKey key = new ByteArrayKey (oid);

        org.omg.CORBA.Object result =
            (org.omg.CORBA.Object) createdReferences.get (key);

        if (result == null)
        {
            result = ((org.jacorb.orb.ORB)orb).getReference
                (this, object_key, intf_rep_id, !isPersistent());

            if (cache)
            {
                createdReferences.put (key, result);
            }

            if ( poaListener != null )
                poaListener.referenceCreated( result );
        }

        return result;
    }

    protected RequestController getRequestController()
    {
        return requestController;
    }

    protected State getState()
    {
        return poaManager.get_state();
    }

    public org.omg.CORBA.Object id_to_reference (byte[] oid)
        throws ObjectNotActive, WrongPolicy
    {
        checkDestructionApparent ();

        if ( !isRetain() )
            throw new WrongPolicy();

        Servant servant = null;
        /* objectId is not active */
        if ((servant = aom.getServant(oid)) == null)
            throw new ObjectNotActive();

        /* If the object with the specified ObjectId currently active,
           a reference encapsulating  the information used to activate
           the object is returned.  */
        return getReference (oid, servant._all_interfaces (this, oid)[0], true);
    }


    public Servant id_to_servant(byte[] oid)
        throws ObjectNotActive, WrongPolicy
    {
        checkDestructionApparent ();

        if (!isRetain() && !isUseDefaultServant()) throw new WrongPolicy();

        Servant servant = null;
        /* servant is active */
        if (isRetain() && (servant = aom.getServant(oid)) != null) {
            return servant;
        }
        if (useDefaultServant()) {
            return defaultServant;
        }

        throw new ObjectNotActive();
    }


    protected boolean isActive()
    {
        return poaManager.get_state().value() ==
        org.omg.PortableServer.POAManagerPackage.State._ACTIVE ? true : false;
    }

    protected void checkDestructionApparent()
    {
        if (isDestructionApparent())
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST ("POA destroyed");
        }
    }

    protected static void checkNotLocal(org.omg.CORBA.Object obj)
        throws WrongAdapter
    {
        if (obj instanceof org.omg.CORBA.LocalObject)
        {
            throw new WrongAdapter ("Local object");
        }
    }

    protected boolean isDestructionApparent()
    {
        return shutdownState >= POAConstants.DESTRUCTION_APPARENT;
    }

    protected boolean isDestructionComplete()
    {
        return shutdownState >= POAConstants.DESTRUCTION_COMPLETE;
    }

    protected boolean isDiscarding()
    {
        return poaManager.get_state().value() == 
            org.omg.PortableServer.POAManagerPackage.State._DISCARDING ? true : false;
    }

    protected boolean isHolding() 
    {
        return poaManager.get_state().value() == 
            org.omg.PortableServer.POAManagerPackage.State._HOLDING ? true : false;
    }

    protected boolean isImplicitActivation() 
    {
        return implicitActivationPolicy != null && 
            implicitActivationPolicy.value() == ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION;
    }

    protected boolean isInactive() 
    {
        return poaManager.get_state().value() == 
            org.omg.PortableServer.POAManagerPackage.State._INACTIVE ? true : false;

    }

    /**
     *  returns  true if  the  current thread  is  in  the context  of
     * executing a request from some  POA belonging to the same ORB as
     * this POA */

    private boolean isInInvocationContext()
    {
        try
        {
            if (orb.getPOACurrent().getORB() == orb)
                return true;
        }
        catch (org.omg.PortableServer.CurrentPackage.NoContext e)
        {
        }
        return false;
    }

    /**
     * returns true if the current thread is in the context of executing
     * a request on the specified servant from this POA,
     * if the specified servant is null, it returns true if the current
     * thread is in an invocation context from this POA.
     */

    private boolean isInInvocationContext(Servant servant)
    {
        try
        {
            if( orb.getPOACurrent().get_POA() == this &&
                (servant == null || orb.getPOACurrent().getServant() == servant))
            {
                return true;
            }
        }
        catch (org.omg.PortableServer.CurrentPackage.NoContext e)
        {
        }
        return false;
    }

    protected boolean isMultipleId()
    {
        return idUniquenessPolicy != null &&
        idUniquenessPolicy.value() == IdUniquenessPolicyValue.MULTIPLE_ID;
    }

    public boolean isPersistent()
    {
        return lifespanPolicy != null &&
        lifespanPolicy.value() == LifespanPolicyValue.PERSISTENT;
    }

    /**
     * also called from Delegate
     */

    public boolean isRetain()
    {
        return servantRetentionPolicy == null ||
        servantRetentionPolicy.value() == ServantRetentionPolicyValue.RETAIN;
    }

    protected boolean isShutdownInProgress()
    {
        return shutdownState >= POAConstants.SHUTDOWN_IN_PROGRESS;
    }

    protected boolean isSingleThreadModel()
    {
        return threadPolicy != null &&
        threadPolicy.value() == ThreadPolicyValue.SINGLE_THREAD_MODEL;
    }

    public boolean isSystemId()
    {
        return idAssignmentPolicy == null ||
        idAssignmentPolicy.value() == IdAssignmentPolicyValue.SYSTEM_ID;
    }

    protected boolean isUniqueId()
    {
        return idUniquenessPolicy == null ||
        idUniquenessPolicy.value() == IdUniquenessPolicyValue.UNIQUE_ID;
    }

    /**
     */

    protected boolean isUseDefaultServant()
    {
        return requestProcessingPolicy != null &&
        requestProcessingPolicy.value() == RequestProcessingPolicyValue.USE_DEFAULT_SERVANT;
    }

    public boolean isUseServantManager()
    {
        return requestProcessingPolicy != null &&
        requestProcessingPolicy.value() == RequestProcessingPolicyValue.USE_SERVANT_MANAGER;
    }

    public boolean isSSLRequired()
    {
        return sslPolicy != null &&
        sslPolicy.value() == SSLPolicyValue.SSL_REQUIRED;
    }

    /**
     * Any calls on the  poa are impossible now, especially you cannot
     *  activate   or  deactivate  objects   (raises  a  OBJ_NOT_EXIST
     *  exception). The  poa  will unregistered  with the  POAManager.
     *  After  destruction  has   become  apparent,  the  POA  may  be
     *  re-created  via  either  the  AdapterActivator or  a  call  to
     * create_POA
     */

    private void makeDestructionApparent()
    {
        synchronized (poaDestructionLog)
        {
            if (shutdownState < POAConstants.DESTRUCTION_APPARENT)
            {
                /* do */
                poaManager.unregisterPOA(this);

                /* set */
                shutdownState = POAConstants.DESTRUCTION_APPARENT;

                /* announce */
                if (poaListener != null)
                    poaListener.poaStateChanged(this, POAConstants.DESTROYED);

                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "destruction is apparent");

                monitor.changeState("destruction is apparent ...");
            }
        }
    }

    /**
     * After destruction has become complete, a poa creation process
     * under the same poa name can continue now.
     */

    private void makeDestructionComplete()
    {
        if (shutdownState < POAConstants.DESTRUCTION_COMPLETE)
        {
            /* do */
            /* clear up the queue */
            if (logger.isDebugEnabled())
                logger.debug(logPrefix + "clear up the queue ...");

            requestController.clearUpQueue(new org.omg.CORBA.OBJECT_NOT_EXIST("adapter destroyed"));

            if (logger.isDebugEnabled())
                logger.debug(logPrefix + "... done");

            /* etherialize all active objects */
            if (etherealize && isRetain() && useServantManager())
            {
                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "etherialize all servants ...");

                aom.removeAll((ServantActivator) servantManager, this, true);

                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "... done");
            }

            /* stop the request processor threads */
            if (!isSingleThreadModel())
            {
                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "remove all processors from the pool ...");
                requestController.clearUpPool();

                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "... done");
            }

            /* stop the request controller */
            if (logger.isDebugEnabled())
                logger.debug(logPrefix + "stop the request controller ...");

            requestController.end();

            if (logger.isDebugEnabled())
                logger.debug(logPrefix + "... done");

            /* set */
            shutdownState = POAConstants.DESTRUCTION_COMPLETE;
            if (parent != null)
            {
                // I am not the RootPOA
                // unregister the poa with the parent and
                // notify a concurrent creation process
                parent.unregisterChild(name);
            }

            /* annouce */
            if (logger.isInfoEnabled())
                logger.info(logPrefix + " destroyed");

            monitor.changeState("destroyed");

            /* clear tables */
            createdReferences.clear();
            all_policies.clear();
        }
    }

    /**
     * The etherealize_objects parameter  from the destroy method will
     * saved in the  field etherealize because the etherealize_objects
     * parameter  applies only the  first call of  destroy. Subsequent
     * calls use  the field etherealize.  Any calls  to create_POA are
     * impossible now (receives a BAD_INV_ORDER exception).
     */

    private void makeShutdownInProgress(boolean etherealize_objects)
    {
        synchronized (poaCreationLog)
        {
            if (shutdownState < POAConstants.SHUTDOWN_IN_PROGRESS)
            {
                /* do */
                etherealize = etherealize_objects;

                /* set */
                shutdownState = POAConstants.SHUTDOWN_IN_PROGRESS;

                /* annouce */
                if (logger.isDebugEnabled())
                    logger.debug(logPrefix + "shutdown is in progress");

                monitor.changeState("shutdown is in progress ...");
            }
        }
    }

    public boolean previouslyGeneratedObjectId(byte[] oid)
    {
        return IdUtil.equals(watermark, extractWatermark(oid));
    }

    public boolean previouslyGeneratedObjectKey(byte[] object_key)
    {
        return IdUtil.equals(object_key, getPOAId(), getPOAId().length);
    }

    public byte[] reference_to_id (org.omg.CORBA.Object reference)
        throws WrongAdapter, WrongPolicy
    {
        checkDestructionApparent ();
        checkNotLocal (reference);

        byte[] objectId = POAUtil.extractOID(reference);

        /* not spec (isSystemId) */
        if (isSystemId() && !previouslyGeneratedObjectId(objectId))
        {
            if (logger.isWarnEnabled())
                logger.warn(logPrefix + "oid: " + POAUtil.convert(objectId) +
                            "reference_to_id: oid not previously generated!");
            throw new WrongAdapter();
        }

        return objectId;
    }

    public Servant reference_to_servant(org.omg.CORBA.Object reference)
        throws ObjectNotActive, WrongAdapter, WrongPolicy
    {
        checkDestructionApparent();
        checkNotLocal(reference);

        if (!isRetain() && !isUseDefaultServant())
            throw new WrongPolicy();

        byte[] objectId = POAUtil.extractOID(reference);

        /* not spec (isSystemId) */
        if (isSystemId() && !previouslyGeneratedObjectId(objectId))
        {
            if (logger.isWarnEnabled())
                logger.warn(logPrefix + "oid: " + POAUtil.convert(objectId) +
                            "reference_to_servant: oid not previously generated!");
            throw new WrongAdapter();
        }

        ByteArrayKey oid = new ByteArrayKey (objectId);

        if ( ( aom != null && aom.isDeactivating (oid) )
             || requestController.isDeactivating (oid))
        {
            if (logger.isWarnEnabled())
                logger.warn(logPrefix + "oid: " + POAUtil.convert(objectId) +
                            "cannot process request, because object is already in the deactivation process");

            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }

        Servant servant = null;

        /* is active servant */
        if (isRetain() && (servant = aom.getServant(objectId)) != null)
        {
            return servant;
        }
        else if (useDefaultServant())
        {
            return defaultServant;
        }

        throw new ObjectNotActive();
    }

    protected synchronized void removePOAListener(POAListener listener)
    {
        poaListener = EventMulticaster.remove(poaListener, listener);
    }


    public byte[] servant_to_id(Servant servant)
        throws ServantNotActive, WrongPolicy
    {
        checkDestructionApparent ();

        if ((!isUseDefaultServant())            &&
            (!isRetain() || !isUniqueId())      &&
            (!isRetain() || !isImplicitActivation()))
            throw new WrongPolicy();

        byte[] objectId = null;

        if (isRetain())
        {
            if (isUniqueId() && (objectId = aom.getObjectId(servant)) != null)
            {
                return objectId;
            }

            if ( isImplicitActivation() &&
                 ( isMultipleId() || !aom.contains(servant)) )
            {
                objectId = generateObjectId();
                /* activate the servant using the generated objectId and the
                   intfRepId associated with the servant */
                try
                {
                    aom.add(objectId, servant);
                }
                catch (ObjectAlreadyActive e)
                {
                    throw new POAInternalError("error: object already active (servant_to_id)");
                }
                catch (ServantAlreadyActive e)
                {
                    /*  it's   ok,  a  nother  one   was  faster  with
                        activation (only occurs if unique_id is set) */
                    objectId = aom.getObjectId(servant);
                }
                return objectId;
            }
        }
        if (isUseDefaultServant() && servant == defaultServant &&
            isInInvocationContext(servant))
        {
            /* objectId associated with the current invocation */
            try
            {
                objectId = orb.getPOACurrent().get_object_id();
            }
            catch (org.omg.PortableServer.CurrentPackage.NoContext e)
            {
                throw new POAInternalError("error: not in invocation context (servant_to_id)");
            }
            return objectId;
        }

        throw new ServantNotActive();
    }

    /**
     */

    public org.omg.CORBA.Object servant_to_reference(Servant servant)
        throws ServantNotActive, WrongPolicy
    {
        checkDestructionApparent ();

        boolean isInInvocationContext = isInInvocationContext(servant);

        if ( (!isRetain() || !isUniqueId()) &&
             (!isRetain() || !isImplicitActivation()) &&
             !isInInvocationContext )
            throw new WrongPolicy();

        byte[] objectId = null;

        if (isInInvocationContext)
        {
            /* reference = Reference associated with the current invocation */
            try
            {
                objectId = orb.getPOACurrent().get_object_id();
            }
            catch (org.omg.PortableServer.CurrentPackage.NoContext e)
            {
                throw new POAInternalError("error: not in invocation context (servant_to_reference)");
            }
            return getReference(objectId,
                                servant._all_interfaces(this, objectId)[0],
                                true);
        }

        if (isRetain())
        {
            if (isUniqueId())
            {
                /* the object reference encapsulating the information
                   used to activate the servant is returned */

                if ((objectId = aom.getObjectId(servant)) != null)
                {
                    return getReference(objectId, servant._all_interfaces(this, objectId)[0], true);
                }
            }

            if (isImplicitActivation() && (isMultipleId() || !aom.contains(servant)) )
            {
                objectId = generateObjectId();

                /* activate the servant using a generated objectId and
                   the intfRepId associated with the servant
                   and a corresponding object reference is returned */

                try
                {
                    aom.add(objectId, servant);
                }
                catch (ObjectAlreadyActive e)
                {
                    throw new POAInternalError("error: object already active (servant_to_reference)");
                }
                catch (ServantAlreadyActive e)
                {
                    /* it's ok, another one was faster with activation
                       (only occurs if unique_id is set) */
                    objectId = aom.getObjectId(servant);
                }

                orb.set_delegate(servant);

                return getReference(objectId,
                                    servant._all_interfaces(this, objectId)[0],
                                    true);
            }
        }
        throw new ServantNotActive();
    }


    public void set_servant(Servant _defaultServant)
        throws WrongPolicy
    {
        checkDestructionApparent();

        if (!isUseDefaultServant())
            throw new WrongPolicy();

        defaultServant = _defaultServant;

        if (defaultServant != null)
            orb.set_delegate(defaultServant);   // set the orb
    }

    /**
     * this method makes a additional check: if the POA has the RETAIN
     *   policy   and   _servantManager   is   not   a   instance   of
     * ServantActivator  or if the  POA has the NON_RETAIN  policy and
     * _servantManager is not a instance of ServantLocator this method
     * raises also the WrongPolicy Exception (not spec.)
     */

    public void set_servant_manager(org.omg.PortableServer.ServantManager servant_manager)
        throws WrongPolicy
    {
        checkDestructionApparent ();

        if (!isUseServantManager())
            throw new WrongPolicy();

        if (servantManager != null)
            throw new org.omg.CORBA.BAD_INV_ORDER();

        /* not spec. */
        if( isRetain() &&
            !(servant_manager instanceof org.omg.PortableServer.ServantActivator))
            throw new WrongPolicy();

        if (!isRetain() &&
            !(servant_manager instanceof org.omg.PortableServer.ServantLocator))
            throw new WrongPolicy();

        servantManager = servant_manager;
    }


    protected void setMonitor(POAMonitor _monitor)
    {
        monitor = _monitor;
    }

    /**
     * it is system-dependent whether the root POA initially has an adapter
     * activator. a newly created POA has not an adapter activator (null)
     */

    public org.omg.PortableServer.AdapterActivator the_activator()
    {
        checkDestructionApparent ();
        return adapterActivator;
    }

    public void the_activator(org.omg.PortableServer.AdapterActivator adapter_activator)
    {
        checkDestructionApparent();
        adapterActivator = adapter_activator;
    }

    public String the_name()
    {
        checkDestructionApparent();
        return POAUtil.unmaskStr(name);
    }

    public org.omg.PortableServer.POA the_parent()
    {
        checkDestructionApparent();
        return parent;
    }

    public org.omg.PortableServer.POA[] the_children()
    {
        checkDestructionApparent();

        int i = 0;
        Enumeration enumeration;
        org.omg.PortableServer.POA[] children;

        synchronized (poaCreationLog)
        {
            children = new org.omg.PortableServer.POA[childs.size()];
            enumeration = childs.elements();
            while (enumeration.hasMoreElements())
            {
                children[i] = (org.omg.PortableServer.POA) enumeration.nextElement();
                i++;
            }
        }

        return children;
    }

    public org.omg.PortableServer.POAManager the_POAManager()
    {
        checkDestructionApparent();
        return poaManager;
    }

    /**
     * notified the completion of a child destruction
     */

    protected void unregisterChild(String name)
    {
        synchronized (poaCreationLog)
        {
            childs.remove(name);
            poaCreationLog.notifyAll();
        }
    }

    /**
     * called from Delegate
     */

    public boolean useDefaultServant()
    {
        return isUseDefaultServant() && defaultServant != null;
    }

    protected boolean useServantManager()
    {
        return isUseServantManager() && servantManager != null;
    }


    /**
     * If any of the policies specified are not valid, or if conflicting
     * policies are specified, or if any of the specified policies require
     * prior administrative action that has not been performed, or if the
     * policy type is unknown, this method returns the index in the policy list
     * of the first offending policy object. By the creation of a new POA object
     * this lead to an InvalidPolicy exception being raised containing this index.
     *
     * If everything's fine, this method returns -1;
     */

    private short verifyPolicyList(org.omg.CORBA.Policy[] policies)
    {
        org.omg.CORBA.Policy policy;
        org.omg.CORBA.Policy policy2;

        for (short i = 0; i < policies.length; i++)
        {
            switch (policies[i].policy_type())
            {
                case THREAD_POLICY_ID.value :
                /* no dependencies */
                break;
                case LIFESPAN_POLICY_ID.value :
                // PERSISTENT -> ImplName is set
                if (((LifespanPolicy) policies[i]).value() ==
                    LifespanPolicyValue.PERSISTENT)
                {
                    if ( implName == null )
                    {
                        logger.fatalError("cannot create a persistent poa! (implname property is not used)");
                        return i;
                    }
                }

                /* no dependencies */
                break;
                case ID_UNIQUENESS_POLICY_ID.value :
                /* no dependencies */
                /* if you set NON_RETAIN the poa doesn't take any
                   notice of the IdUniquenesPolicyValue */
                break;
                case ID_ASSIGNMENT_POLICY_ID.value :
                //   SYSTEM_ID   ->   no   dependencies   USER_ID   ->
                // NO_IMPLICIT_ACTIVATION, but  an error will detected
                //  if  we  have  considered  the  IMPLICIT_ACTIVATION
                // policy
                break;

                case SERVANT_RETENTION_POLICY_ID.value :
                // RETAIN -> no dependencies
                // NON_RETAIN -> (USE_DEFAULT_SERVANT || USE_SERVANT_MANAGER)
                if (((ServantRetentionPolicy) policies[i]).value() ==
                    ServantRetentionPolicyValue.NON_RETAIN) {

                    policy =
                    POAUtil.getPolicy(policies, REQUEST_PROCESSING_POLICY_ID.value);
                    if (policy != null)
                    {
                        if ( ((RequestProcessingPolicy) policy).value() !=
                             RequestProcessingPolicyValue.USE_DEFAULT_SERVANT &&
                             ((RequestProcessingPolicy) policy).value() !=
                             RequestProcessingPolicyValue.USE_SERVANT_MANAGER)
                        {
                            return i;
                        }
                    }
                    else
                    {
                        return i; // default (USE_ACTIVE_OBJECT_MAP_ONLY) is forbidden
                    }
                    // NON_RETAIN -> NO_IMPLICIT_ACTIVATION, but an error will
                    // be detected if we have considered the IMPLICIT_ACTIVATION policy
                }
                break;

                case REQUEST_PROCESSING_POLICY_ID.value :
                // USE_SERVANT_MANAGER -> no dependencies
                // USE_ACTIVE_OBJECT_MAP_ONLY -> RETAIN
                if (((RequestProcessingPolicy) policies[i]).value() ==
                    RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY)
                {

                    policy = POAUtil.getPolicy( policies,
                                                SERVANT_RETENTION_POLICY_ID.value);
                    if (policy != null)
                    {
                        if (((ServantRetentionPolicy) policy).value() !=
                            ServantRetentionPolicyValue.RETAIN)
                        {
                            return i;
                        }
                    }
                    else
                    {
                        // do nothing, because default (RETAIN) is ok
                    }
                    // USE_DEFAULT_SERVANT -> (MULTIPLE_ID || NON_RETAIN)  /* not spec. (NON_RETAIN) */
                }
                else if (((RequestProcessingPolicy) policies[i]).value() == RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
                {
                    policy  =
                    POAUtil.getPolicy(policies, ID_UNIQUENESS_POLICY_ID.value);
                    policy2 =
                    POAUtil.getPolicy(policies, SERVANT_RETENTION_POLICY_ID.value);
                    if (policy == null && policy2 == null)
                    {
                        return i; // default (UNIQUE_ID && RETAIN) is forbidden
                    }
                    else if (policy != null && policy2 == null)
                    {
                        if (((IdUniquenessPolicy) policy).value() !=
                            IdUniquenessPolicyValue.MULTIPLE_ID )
                            return i;
                    }
                    else if (policy == null && policy2 != null)
                    {
                        if (((ServantRetentionPolicy) policy2).value() !=
                            ServantRetentionPolicyValue.NON_RETAIN )
                            return i;
                    }
                    else if (policy != null && policy2 != null)
                    {
                        if (((IdUniquenessPolicy) policy).value() !=
                            IdUniquenessPolicyValue.MULTIPLE_ID &&
                            ((ServantRetentionPolicy) policy2).value() !=
                            ServantRetentionPolicyValue.NON_RETAIN )
                            return i;
                    }
                }
                break;

                case IMPLICIT_ACTIVATION_POLICY_ID.value :
                // NO_IMPLICIT_ACTIVATION -> no dependencies
                // IMPLICIT_ACTIVATION -> (SYSTEM_ID && RETAIN)
                if (((ImplicitActivationPolicy) policies[i]).value() ==
                    ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)
                {
                    policy = POAUtil.getPolicy(policies,
                                               SERVANT_RETENTION_POLICY_ID.value);
                    if (policy != null)
                    {
                        if (((ServantRetentionPolicy) policy).value() !=
                            ServantRetentionPolicyValue.RETAIN)
                        {
                            return i;
                        }
                    }
                    else
                    {
                        // do nothing, because default (RETAIN) is ok
                    }

                    policy = POAUtil.getPolicy(policies, ID_ASSIGNMENT_POLICY_ID.value);
                    if (policy != null)
                    {
                        if (((IdAssignmentPolicy) policy).value() !=
                            IdAssignmentPolicyValue.SYSTEM_ID)
                        {
                            return i;
                        }
                    }
                    else
                    {
                        // do nothing, because default (SYSTEM_ID) is ok
                    }
                }
                break;
                case BIDIRECTIONAL_POLICY_TYPE.value :
                // nothing to do
                break;
                //ignore unknown policies

                //              // unknown policy type -> return i
                //          default :
                //              return i;
            }
        }
        return -1;
    }

    public org.omg.CORBA.Policy getPolicy(int type)
    {
        return (org.omg.CORBA.Policy) all_policies.get(new Integer(type));
    }

    public void addLocalRequest()
    {
        requestController.addLocalRequest();
    }

    public void removeLocalRequest()
    {
        requestController.removeLocalRequest();
    }

    public int getNumberOfObjects()
    {
        return aom.size ();
    }
}
