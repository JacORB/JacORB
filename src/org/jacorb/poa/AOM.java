package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
import org.jacorb.util.Debug;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class maps object id's to servants and vice versa.
 * A oid/servant pair can be added/removed using add(),remove().
 * The data can be retrieved using getServant() or getObjectId().
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class AOM 
{
    private AOMListener         aomListener;
        
    private boolean             unique;
    private boolean             singleThreaded;
    private LogTrace            logTrace;

    // an ObjectID can appear only once, but an servant can have multiple ObjectId's
    // if MULTIPLE_ID is set
    private Hashtable           objectMap = new Hashtable(); // oid -> servant

    // only meaningful if UNIQUE_ID is set
    // only for performance improvements (brose: is that still true?)
    private Hashtable           servantMap;             // servant -> oid

    // for synchronisation of servant activator calls
    private Vector              etherealisationList = new Vector();
    private Vector              incarnationList = new Vector();

    private Vector              deactivationList = new Vector();

    private AOM() 
    {
    }

    protected AOM( boolean _unique, 
                   boolean single_threaded, 
                   LogTrace _logTrace
                   ) 
    {
        unique = _unique;
        singleThreaded = single_threaded;
        logTrace = _logTrace;

        if (unique) 
        {
            servantMap = new Hashtable();
        }
    }


    synchronized protected void add( byte[] oid, Servant servant ) 
        throws ObjectAlreadyActive, ServantAlreadyActive 
    {
        ByteArrayKey oidbak = POAUtil.oid_to_bak(oid);

        /* an inCarnation and activation with the same oid has
           priority, a reactivation for the same oid blocks until
           etherealization is complete */

        while (incarnationList.contains(oidbak) || 
               etherealisationList.contains(oidbak)) 
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
            throw new ObjectAlreadyActive();

        if (unique && servantMap.containsKey(servant)) 
            throw new ServantAlreadyActive();

        /* this is the actual object activation: */
                
        objectMap.put(oidbak, servant);

        if ( unique ) 
        {
            servantMap.put(servant, oidbak);
        }
                
        if (logTrace.test(2))
        	logTrace.printLog(oid, "object is activated");

        // notify an aom listener
        if ( aomListener != null ) 
            aomListener.objectActivated(oid, servant, objectMap.size());
    }


    protected synchronized void addAOMListener(AOMListener listener) 
    {
        aomListener = EventMulticaster.add(aomListener, listener);
    }
    

    protected boolean contains(Servant servant) 
    {
        if (unique) 
        {
            return servantMap.containsKey(servant);
        } 
        else 
        {
            return objectMap.contains(servant);
        }
    }


    synchronized protected StringPair[] deliverContent() 
    {               
        StringPair[] result = new StringPair[objectMap.size()];
        ByteArrayKey oidbak;
        Enumeration en = objectMap.keys();

        for ( int i = 0; i < result.length; i++ ) 
        {
            oidbak = (ByteArrayKey) en.nextElement();
            result[i] = 
                new StringPair(
                	POAUtil.oid_to_string(oidbak.getBytes()),
                	objectMap.get(oidbak).getClass().getName()
                ); 
        }
        return result;

    }

    protected byte[] getObjectId(Servant servant) 
    {
        if (!unique) 
            throw new POAInternalError("error: not UNIQUE_ID policy (getObjectId)");

        ByteArrayKey oidbak = (ByteArrayKey)servantMap.get(servant);

        if (oidbak != null) 
            return POAUtil.bak_to_oid(oidbak);

        return null;
    }



    protected Servant getServant(byte[] oid) 
    {  
        return (Servant) objectMap.get(POAUtil.oid_to_bak(oid));
    }




    synchronized protected Servant incarnate( byte[] oid, 
                                              ServantActivator servant_activator, 
                                              org.omg.PortableServer.POA poa ) 
        throws org.omg.PortableServer.ForwardRequest 
    {
        ByteArrayKey oidbak = POAUtil.oid_to_bak(oid);
        Servant servant = null;
                            
        if (logTrace.test(0))
            logTrace.printLog(oid, " incarnate");

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
                
        incarnationList.addElement(oidbak);     
        try 
        {
            servant = servant_activator.incarnate(oid, poa);                               
        } 
        finally 
        {
            incarnationList.removeElement(oidbak);
            notifyAll();
        }

        if (servant == null) 
        {
            if (logTrace.test(0))
            	logTrace.printLog(oid, "servant is not incarnated (incarnate returns null)");
            return null;        
        }

        if (unique && servantMap.containsKey(servant)) 
        {
        	if (logTrace.test(0))
	            logTrace.printLog(oid, "servant is not incarnated (unique_id policy is violated)");
            return null;
        }

        if (logTrace.test(2))
        	logTrace.printLog(oid, "servant is incarnated");
        // notify an aom listener
        if (aomListener != null) aomListener.servantIncarnated(oid, servant);

        /* object activation */
                
        try 
        {
            add(oid, servant);
        } 
        catch (ObjectAlreadyActive e) 
        {
            throw new POAInternalError("error: object already active (AOM.incarnate)");
        } 
        catch (ServantAlreadyActive e) 
        {
            throw new POAInternalError("error: servant already active (AOM.incarnate)");
        }               
        return servant;
    }

    synchronized protected void remove( byte[] oid,
                           RequestController requestController, 
                           ServantActivator servantActivator, 
                           POA poa,
                           boolean cleanupInProgress)
                           throws ObjectNotActive
    {
    	
    	ByteArrayKey oidbak = POAUtil.oid_to_bak(oid);
    	    	
        if ( !objectMap.containsKey( oidbak ) || deactivationList.contains(oidbak) ) {
        	throw new ObjectNotActive();
        }
    	
    	deactivationList.addElement(oidbak);
    	
    	final byte[] oid_ = oid;
    	final RequestController requestController_ = requestController;
    	final ServantActivator servantActivator_ = servantActivator;
        final POA poa_ = poa;
        final boolean cleanupInProgress_ = cleanupInProgress;
                
        Thread thread = new Thread() 
            {
                public void run() 
                {
                    _remove(
                    	oid_,
                    	requestController_,
                    	servantActivator_,
                    	poa_,
                    	cleanupInProgress_
                    );
                }
            };
            
        thread.start();
    }

    synchronized private void _remove( byte[] oid, 
                           RequestController requestController, 
                           ServantActivator servantActivator, 
                           POA poa,
                           boolean cleanupInProgress)
    {
        ByteArrayKey  oidbak = POAUtil.oid_to_bak(oid);
        Servant servant = null;
                
        if (!objectMap.containsKey(oidbak)) {
        	// should not happen but ...
        	deactivationList.removeElement(oidbak);
        	return;
        }

        // wait for request completion on this object (see freeObject below)
        if ( requestController != null) 
             requestController.waitForObjectCompletion(oid);

        deactivationList.removeElement(oidbak);

        if ((servant = (Servant)objectMap.get(oidbak)) == null) {
        	return;
        }
                    
        /* object deactivation */

        objectMap.remove(oidbak);

        if (unique) 
        {
            servantMap.remove(servant);
        }

        if (logTrace.test(2))
        	logTrace.printLog(oid, "object is deactivated");

        // notify an aom listener                   
        if (aomListener != null) 
            aomListener.objectDeactivated(oid, servant, objectMap.size());

        if (servantActivator == null) 
        {
            requestController.freeObject(oid);
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
        etherealisationList.addElement(oidbak);
            
        try 
        {
            servantActivator.etherealize
            (
                 oid,
                 poa, 
                 servant,
                 cleanupInProgress,
                 contains(servant)
            );
                            
            if (logTrace.test(2))
	            logTrace.printLog(oid, "servant is etherealized");
                      
            // notify an aom listener

            if (aomListener != null) 
                aomListener.servantEtherialized(oid, servant);
                            
        } 
        catch (org.omg.CORBA.SystemException e) 
        {
        	if (logTrace.test(1))        	
                logTrace.printLog(
                	oid, "exception occurred during servant etherialisation: "+e
                );
        } 
        finally 
        {
            etherealisationList.removeElement(oidbak);
            notifyAll();
        }
                    
        // unregister the object from deactivation list
        if (requestController != null) 
            requestController.freeObject(oid);
    }
    
    protected void removeAll( ServantActivator servant_activator, 
                              POA poa, 
                              boolean cleanup_in_progress ) 
    {
        byte[] oid;
        Enumeration en = objectMap.keys();
        while (en.hasMoreElements()) 
        {
            oid = POAUtil.bak_to_oid((ByteArrayKey) en.nextElement());
            _remove(oid, null, servant_activator, poa, cleanup_in_progress);
        }
    }

    protected synchronized void removeAOMListener(AOMListener listener) 
    {
        aomListener = EventMulticaster.remove(aomListener, listener);
    }

    protected int size() 
    {
        return objectMap.size();
    }

    public void printSizes()
    {
        System.out.println( "AOM: objectMap " + size() + 
                            " servantMap " + servantMap.size());
    }

}







