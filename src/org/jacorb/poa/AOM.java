package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.jacorb.poa.except.POAInternalError;
import org.jacorb.poa.util.ByteArrayKey;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.poa.util.StringPair;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.slf4j.Logger;

/**
 * This class maps object id's to servants and vice versa.
 * A oid/servant pair can be added/removed using add(),remove().
 * The data can be retrieved using getServant() or getObjectId().
 *
 * @author Reimo Tiedemann, FU Berlin
 */

public class AOM
{
    private AOMListener         aomListener;

    private final boolean             unique;
    private final Logger            logger;

    // an ObjectID can appear only once, but an servant can have multiple ObjectId's
    // if MULTIPLE_ID is set
    private final Map           objectMap = new HashMap(); // oid -> servant

    // only meaningful if UNIQUE_ID is set
    // only for performance improvements (brose: is that still true?)
    private final Map           servantMap;             // servant -> oid

    // for synchronisation of servant activator calls
    private final HashSet              etherealisationList = new HashSet();
    private final HashSet              incarnationList = new HashSet();
    private final HashSet              deactivationList = new HashSet();

    private BlockingQueue removalQueue = new LinkedBlockingQueue();

    /**
     * AOMRemoval thread - accessed from POA to signal shutdown.
     */
    AOMRemoval aomRemoval;

    /**
     * Counter for AOMRemoval threads
     */
    private static int count = 0;

    /**
     * Marker object used to signal the end of the removalQueue.
     */
    private static final Object END = new Object();


    protected AOM (boolean _unique, Logger _logger)
    {
        unique = _unique;
        logger = _logger;
        aomRemoval = new AOMRemoval ();

        if (unique)
        {
            servantMap = new HashMap();
        }
        else
        {
            servantMap = Collections.EMPTY_MAP;
        }
        aomRemoval.setDaemon (true);
        aomRemoval.start ();
    }


    /**
     * <code>add</code> is called by the POA when activating an object
     * to add a Servant into the Active Object Map.
     *
     * @param oid a <code>byte[]</code>, the id to use.
     * @param servant a <code>Servant</code>, the servant to store.
     * @exception ObjectAlreadyActive if an error occurs
     * @exception ServantAlreadyActive if an error occurs
     */
    protected void add( byte[] oid, Servant servant )
        throws ObjectAlreadyActive, ServantAlreadyActive
    {
        ByteArrayKey oidbak = new ByteArrayKey (oid);

        add(oidbak, servant);
    }


    protected synchronized void add(ByteArrayKey oidbak, Servant servant) throws ObjectAlreadyActive, ServantAlreadyActive
    {
        final byte[] oid = oidbak.getBytes();
        /* an inCarnation and activation with the same oid has
           priority, a reactivation for the same oid blocks until
           etherealization is complete */

        while (incarnationList.contains(oidbak)     ||
               etherealisationList.contains(oidbak) ||
               // This is to check whether we are currently deactivating an
               // object with the same id - if so, wait for the deactivate
               // thread to complete.
               deactivationList.contains( oidbak )  ||
               // This is to check whether we are attempting to reactivate
               // a servant that is currently being deactivated with another ID.
               (
                   servantMap.containsKey( servant ) &&
                   deactivationList.contains(servantMap.get( servant ))
               ))
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }

        if (objectMap.containsKey(oidbak))
        {
            throw new ObjectAlreadyActive();
        }

        if (unique && servantMap.containsKey(servant))
        {
            throw new ServantAlreadyActive();
        }

        /* this is the actual object activation: */

        objectMap.put(oidbak, servant);

        if ( unique )
        {
            servantMap.put(servant, oidbak);
        }

        if (logger.isInfoEnabled())
        {
            logger.info("oid: " + POAUtil.convert(oid) +
                        "object is activated");
        }

        // notify an aom listener
        if (aomListener != null)
        {
            aomListener.objectActivated(oid, servant, objectMap.size());
        }
    }


    protected synchronized void addAOMListener(AOMListener listener)
    {
        aomListener = EventMulticaster.add(aomListener, listener);
    }


    synchronized boolean isDeactivating(ByteArrayKey oid)
    {
        return deactivationList.contains (oid);
    }


    protected synchronized boolean contains(Servant servant)
    {
        return _contains(servant);
    }


    private boolean _contains(Servant servant)
    {
        if (unique)
        {
            return servantMap.containsKey(servant);
        }
        else
        {
            return objectMap.containsValue(servant);
        }
    }


    protected synchronized StringPair[] deliverContent()
    {
        final StringPair[] result = new StringPair[objectMap.size()];
        Iterator en = objectMap.keySet().iterator();

        for ( int i = 0; i < result.length; i++ )
        {
            final ByteArrayKey oidbak = (ByteArrayKey) en.next();
            result[i] = new StringPair
            (
                oidbak.toString(),
                objectMap.get(oidbak).getClass().getName()
            );
        }
        return result;

    }

    protected synchronized byte[] getObjectId(Servant servant)
    {
        if (!unique)
        {
            throw new POAInternalError("error: not UNIQUE_ID policy (getObjectId)");
        }

        ByteArrayKey oidbak = (ByteArrayKey)servantMap.get(servant);

        if (oidbak != null)
        {
            return oidbak.getBytes();
        }

        return null;
    }

    protected Servant getServant(byte[] oid)
    {
        return getServant( new ByteArrayKey( oid ) );
    }

    protected synchronized Servant getServant(ByteArrayKey oid)
    {
        return (Servant) objectMap.get(oid);
    }

    protected synchronized Servant incarnate( ByteArrayKey oidbak,
                                              ServantActivator servant_activator,
                                              org.omg.PortableServer.POA poa )
        throws org.omg.PortableServer.ForwardRequest
    {
        final byte[] oid = oidbak.getBytes();
        Servant servant = null;

        if (logger.isInfoEnabled())
        {
            logger.info( "oid: " + POAUtil.convert(oid) +
                        "incarnate");
        }

        /* all invocations of incarnate on the servant manager are serialized */
        /* all invocations of etherealize on the servant manager are serialized */
        /* invocations of incarnate and etherialize are mutually exclusive */

        while (!incarnationList.isEmpty() || !etherealisationList.isEmpty())
        {
            try
            {
                wait();
            }
            catch (InterruptedException e) {
            }
        }

        /* another thread was faster, the incarnation is unnecessary now */
        if (objectMap.containsKey(oidbak))
        {
            return (Servant) objectMap.get(oidbak);
        }

        /* servant incarnation */
        if (servant_activator == null)
        {
            // This might be thrown if they failed to set implname
            throw new OBJ_ADAPTER("Servant Activator for " + POAUtil.convert(oid) + " was null.");
        }

        incarnationList.add(oidbak);
        try
        {
            servant = servant_activator.incarnate(oid, poa);
        }
        finally
        {
            incarnationList.remove(oidbak);
            notifyAll();
        }

        if (servant == null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("oid: " + POAUtil.convert(oid) +
                            "servant is not incarnated (incarnate returns null)");
            }
        }
        else
        {
            if (unique && servantMap.containsKey(servant))
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("oid: " + POAUtil.convert(oid) +
                                "servant is not incarnated (unique_id policy is violated)");
                }
                servant = null;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("oid: " + POAUtil.convert(oid) +
                                 "servant is incarnated");
                }

                // notify an aom listener
                if (aomListener != null)
                {
                    aomListener.servantIncarnated(oid, servant);
                }

                /* object activation */

                try
                {
                    add(oidbak, servant);
                }
                catch (ObjectAlreadyActive e)
                {
                    throw new POAInternalError("error: object already active (AOM.incarnate)");
                }
                catch (ServantAlreadyActive e)
                {
                    throw new POAInternalError("error: servant already active (AOM.incarnate)");
                }
            }
        }

        ((org.jacorb.poa.POA)poa).getORB().set_delegate(servant);

        return servant;
    }

    synchronized void remove( ByteArrayKey oidbak,
                 RequestController requestController,
                 ServantActivator servantActivator,
                 POA poa,
                 boolean cleanupInProgress)
        throws ObjectNotActive
    {
        if ( !objectMap.containsKey( oidbak ) ||
             deactivationList.contains( oidbak ) )
        {
            throw new ObjectNotActive();
        }

        deactivationList.add(oidbak);

        RemovalStruct rs = new RemovalStruct (oidbak,
                                              requestController,
                                              servantActivator,
                                              poa,
                                              cleanupInProgress);

        try
        {
            removalQueue.put (rs);
        }
        catch (InterruptedException ie)
        {
        }
    }


    /**
     * <code>_remove</code> is spawned by remove to allow deactivate_object
     * to return immediately.
     *
     * @param oid a <code>byte[]</code>, the id to use.
     * @param requestController a <code>RequestController</code> value
     * @param servantActivator a <code>ServantActivator</code> value
     * @param poa a <code>POA</code> value
     * @param cleanupInProgress a <code>boolean</code> value
     */
    private void _remove( ByteArrayKey oidbak,
                          RequestController requestController,
                          ServantActivator servantActivator,
                          POA poa,
                          boolean cleanupInProgress)
    {
        final byte[] oid = oidbak.getBytes();

        if (!objectMap.containsKey(oidbak))
        {
            // should not happen but ...
            deactivationList.remove(oidbak);
            return;
        }

        // wait for request completion on this object (see freeObject below)
        if ( requestController != null)
        {
            requestController.waitForObjectCompletion(oidbak);
        }

        try
        {
            actualRemove(oidbak, servantActivator, poa, cleanupInProgress, oid);
        }
        finally
        {
            if (requestController != null)
            {
                requestController.freeObject(oidbak);
            }
        }
    }


    private synchronized void actualRemove(ByteArrayKey oidbak, ServantActivator servantActivator, POA poa, boolean cleanupInProgress, final byte[] oid)
    {
        Servant servant;

        if ((servant = (Servant)objectMap.get(oidbak)) == null)
        {
            deactivationList.remove(oidbak);
            return;
        }

        /* object deactivation */

        objectMap.remove(oidbak);

        if (unique)
        {
            servantMap.remove(servant);
        }

        // Wait to remove the oid from the deactivationList here so that the
        // object map can be cleared out first. This ensures we don't
        // reactivate an object we're currently deactivating.
        deactivationList.remove(oidbak);

        if (logger.isInfoEnabled())
        {
            logger.info("oid: " + POAUtil.convert(oid) +
                        "object is deactivated");
        }

        // notify an aom listener
        if (aomListener != null)
        {
            aomListener.objectDeactivated(oid, servant, objectMap.size());
        }

        if (servantActivator == null)
        {
            // Tell anyone waiting we're done now.
            notifyAll();
            return;
        }

        /* servant etherealization */

        /* all invocations of incarnate on the servant manager are
           serialized,  all  invocations   of  etherealize  on  the
           servant manager are serialized, invocations of incarnate
           and etherialize are mutually exclusive */

        while (!incarnationList.isEmpty() || !etherealisationList.isEmpty())
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
        etherealisationList.add(oidbak);

        try
        {
            servantActivator.etherealize
            (
                oid,
                poa,
                servant,
                cleanupInProgress,
                _contains(servant)
            );

            if (logger.isInfoEnabled())
            {
                logger.info("oid: " + POAUtil.convert(oid) +
                            "servant is etherealized");
            }

            // notify an aom listener

            if (aomListener != null)
            {
                aomListener.servantEtherialized(oid, servant);
            }
        }
        catch (org.omg.CORBA.SystemException e)
        {
            if (logger.isWarnEnabled())
            {
                logger.info("oid: " + POAUtil.convert(oid) +
                            "exception occurred during servant etherialisation: " + e.getMessage()
                           );
            }
        }
        finally
        {
            etherealisationList.remove(oidbak);
            notifyAll();
        }
    }

    protected synchronized void removeAll( ServantActivator servant_activator,
                                           POA poa,
                                           boolean cleanup_in_progress )
    {
        final Iterator i = new HashSet(objectMap.keySet()).iterator();
        while (i.hasNext())
        {
            final ByteArrayKey oid = (ByteArrayKey) i.next();
            _remove(oid, null, servant_activator, poa, cleanup_in_progress);
        }
    }

    protected synchronized void removeAOMListener(AOMListener listener)
    {
        aomListener = EventMulticaster.remove(aomListener, listener);
    }

    protected synchronized int size()
    {
        return objectMap.size();
    }


    class AOMRemoval extends Thread
    {
        private boolean run = true;

        public AOMRemoval ()
        {
            super ("AOMRemoval-" + (++count));
        }

        public void end()
        {
            run = false;
            try
            {
               removalQueue.put (END);
            }
            catch (InterruptedException ex)
            {
            }
        }

        public void run()
        {
            while (run)
            {
                try
                {
                    Object rso = removalQueue.take();
                    if (rso != END)
                    {
                       RemovalStruct rs = (RemovalStruct)rso;
                        _remove (rs.oidbak,
                                 rs.requestController,
                                 rs.servantActivator,
                                 rs.poa,
                                 rs.cleanupInProgress);
                    }
                }
                catch (InterruptedException ie)
                {
                }
            }
        }
    }

    class RemovalStruct
    {
        ByteArrayKey oidbak;
        RequestController requestController;
        ServantActivator servantActivator;
        POA poa;
        boolean cleanupInProgress;

        public RemovalStruct (ByteArrayKey oidbak,
                              RequestController requestController,
                              ServantActivator servantActivator,
                              POA poa,
                              boolean cleanupInProgress)
        {
            this.oidbak = oidbak;
            this.requestController = requestController;
            this.servantActivator = servantActivator;
            this.poa = poa;
            this.cleanupInProgress = cleanupInProgress;
        }
    }
}
