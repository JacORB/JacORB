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

import org.jacorb.poa.except.*;
 
import org.jacorb.util.*;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.orb.connection.ReplyOutputStream;


import java.util.Hashtable;
import java.util.Enumeration;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import org.omg.CORBA.portable.InvokeHandler;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.ServiceContext;

/**
 * This thread performs the request processing, the actual method invocation and
 * it returns the ServerRequest object to the ORB.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class RequestProcessor 
    extends Thread 
    implements InvocationContext 
{       
    private boolean start;
    private boolean terminate;
    private Thread[] remainingThreads;  
    private RPPoolManager poolManager;

    private RequestController controller;
    private ServerRequest request;
    private Servant servant;
    private ServantManager servantManager;
    private CookieHolder cookieHolder;

    private static Hashtable specialOperations;
    private static int count = 0;

    static
    {
        specialOperations = new Hashtable(50);
        specialOperations.put("_is_a", "");
        specialOperations.put("_interface", "");
        specialOperations.put("_non_existent", "");

        specialOperations.put("_get_policy", "");
        specialOperations.put("_set_policy_overrides", "");
    }

    RequestProcessor (RPPoolManager _poolManager) 
    {
        super ("RequestProcessor-" + (++count));
        poolManager = _poolManager;
    }

    /**
     * starts the request processor
     */

    synchronized void begin() 
    {
        start = true;        
        notify();
    }

    /**
     * terminates the request processor
     */

    synchronized void end() 
    {
        terminate = true;
        notify();
    }

    /**
     * returns the oid associated current servant invocation
     */

    public byte[] getObjectId() 
    {
        if (!start) throw new POAInternalError("error: RequestProcessor not started (getObjectId)");
        return request.objectId();
    }

    /**
     * returns the orb that has received the request
     */

    public org.omg.CORBA.ORB getORB() 
    {
        if (!start) throw new POAInternalError("error: RequestProcessor not started (getORB)");
        return controller.getORB();
    }

    /**
     * returns the poa that has dispatched the request
     */

    public POA getPOA() 
    {
        if (!start) throw new POAInternalError("error: RequestProcessor not started (getPOA)");
        return controller.getPOA();
    }

    /**
     * returns the actual servant
     */

    public Servant getServant() 
    {
        if (!start) 
            throw new POAInternalError("error: RequestProcessor not started (getServant)");
        return servant;
    }

    /**
     * initializes the request processor
     */

    void init( RequestController _controller, 
               ServerRequest _request, 
               Servant _servant, 
               ServantManager _servantManager) 
    {
        controller = _controller;
        request = _request;
        servant = _servant;
        servantManager = _servantManager;
        cookieHolder = null;
    }

    private void clear()
    {
        controller = null;
        request = null;
        servant = null;
        servantManager = null;
        cookieHolder = null;
    }


    /**
     * causes the aom to perform the incarnate call on a servant activator
     */

    private void invokeIncarnate() 
    {
    	if (controller.getLogTrace().test(3))
            controller.getLogTrace().printLog(request, "invoke incarnate on servant activator");
        try 
        {
                                
            servant = controller.getAOM().incarnate( request.objectId(), 
                                                     (ServantActivator) servantManager,
                                                     controller.getPOA());
            if (servant == null) 
            {
            	if (controller.getLogTrace().test(0))
                    controller.getLogTrace().printLog(request, "incarnate: returns null");
                request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER());    
            }

            controller.getORB().set_delegate( servant );        // set the orb
                                
        } 
        catch (org.omg.CORBA.SystemException e) 
        {
            if (controller.getLogTrace().test(0))
                controller.getLogTrace().printLog(request, "incarnate: system exception was thrown ("+e+")");
            request.setSystemException(e);

        } 
        catch (org.omg.PortableServer.ForwardRequest e) 
        {
            if (controller.getLogTrace().test(0))
                controller.getLogTrace().printLog(request, "incarnate: forward exception was thrown ("+e+")");
            request.setLocationForward(e);
                                
        } 
        catch (Throwable e) { /* not spec. */
            if (controller.getLogTrace().test(0)) {
                controller.getLogTrace().printLog(request, "incarnate: throwable was thrown");
                controller.getLogTrace().printLog(e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER(e.getMessage())); 
            /* which system exception I should raise? */                        
        }
    }


    /**
     * invokes the operation on servant,
     */

    private void invokeOperation() 
    {
        try 
        {                       
            if (servant instanceof org.omg.CORBA.portable.InvokeHandler) 
            {                          
            	if (controller.getLogTrace().test(3)) 
                    controller.getLogTrace().printLog(request, "invoke operation on servant (stream based)");
                if( specialOperations.containsKey(request.operation()))
                {
                    ((org.jacorb.orb.ServantDelegate)servant._get_delegate())._invoke(servant, 
                                                                                      request.operation(), 
                                                                                      request.getInputStream(), 
                                                                                      request);
                }
                else
                {
                    ((InvokeHandler) servant)._invoke(request.operation(), 
                                                      request.getInputStream(), 
                                                      request);
                }

            } 
            else if (servant instanceof org.omg.PortableServer.DynamicImplementation) 
            {          
            	if (controller.getLogTrace().test(3))               
                    controller.getLogTrace().printLog(request, 
                                                      "invoke operation on servant (dsi based)");
                if( specialOperations.containsKey(request.operation()) && 
                    !(servant instanceof org.jacorb.orb.Forwarder) )
                {
                    ((org.jacorb.orb.ServantDelegate)servant._get_delegate())._invoke(
                                                                                      servant, 
                                                                                      request.operation(), 
                                                                                      request.getInputStream(),
                                                                                      request);
                }
                else
                {
                    ((DynamicImplementation) servant).invoke(request);
                }
            } 
            else 
            {
            	if (controller.getLogTrace().test(0))                           
                    controller.getLogTrace().printLog(request, 
                                                      "unknown servant type (neither stream nor dsi based)");
                                        
            }
                                
        } 
        catch (org.omg.CORBA.SystemException e) 
        {
            if (controller.getLogTrace().test(1))
                controller.getLogTrace().printLog(request, "invocation: system exception was thrown ("+e+")");
            request.setSystemException(e);
        } 
        catch (Throwable e)             
        {         /* not spec. */
            if (controller.getLogTrace().test(0)) {
                controller.getLogTrace().printLog(request, "invocation: throwable was thrown");
                controller.getLogTrace().printLog(e);
            }
            request.setSystemException(new org.omg.CORBA.UNKNOWN()); /* which system exception I should raise? */
        }
    }


    /**
     * performs the postinvoke call on a servant locator
     */

    private void invokePostInvoke() 
    {
        try 
        {
            if (controller.getLogTrace().test(3))
                controller.getLogTrace().printLog(request, 
                                                  "invoke postinvoke on servant locator");

            ((ServantLocator) servantManager).postinvoke(request.objectId(),
                                                         controller.getPOA(),
                                                         request.operation(),
                                                         cookieHolder.value,
                                                         servant);
        } 
        catch (org.omg.CORBA.SystemException e) 
        {
            if (controller.getLogTrace().test(1))
                controller.getLogTrace().printLog(request, "postinvoke: system exception was thrown ("+e+")");
            request.setSystemException(e);
                        
        } 
        catch (Throwable e) {         /* not spec. */
            if (controller.getLogTrace().test(0)) {
                controller.getLogTrace().printLog(request, "postinvoke: throwable was thrown");
                controller.getLogTrace().printLog(e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER()); 
            /* which system exception I should raise? */
        }
    }


    /**
     * performs the preinvoke call on a servant locator
     */

    private void invokePreInvoke() 
    {
    	if (controller.getLogTrace().test(3))
            controller.getLogTrace().printLog(request, "invoke preinvoke on servant locator");
        try 
        {
            cookieHolder = new CookieHolder();
            servant = ((ServantLocator) servantManager).preinvoke(request.objectId(),
                                                                  controller.getPOA(),
                                                                  request.operation(),
                                                                  cookieHolder);
            if (servant == null) 
            {
            	if (controller.getLogTrace().test(0))
                    controller.getLogTrace().printLog(request, "preinvoke: returns null");
                request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER());    
            }
            controller.getORB().set_delegate( servant );        // set the orb

        } 
        catch (org.omg.CORBA.SystemException e) 
        {
            if (controller.getLogTrace().test(1))
                controller.getLogTrace().printLog(request, "preinvoke: system exception was thrown ("+e+")");
            request.setSystemException(e);

        } 
        catch (org.omg.PortableServer.ForwardRequest e) 
        {
            if (controller.getLogTrace().test(1))
                controller.getLogTrace().printLog(request, "preinvoke: forward exception was thrown ("+e+")");
            request.setLocationForward(e);
                                                
        } 
        catch (Throwable e) {         /* not spec. */
            if (controller.getLogTrace().test(0)) {
                controller.getLogTrace().printLog(request, "preinvoke: throwable was thrown");
                controller.getLogTrace().printLog(e);
            }
            request.setSystemException(new org.omg.CORBA.OBJ_ADAPTER(e.getMessage())); 
            /* which system exception I should raise? */
        }
    }

    boolean isActive() 
    {
        return start;
    }


    /**
     * the main request processing routine
     */

    private void process() 
    {
        ServerRequestInfoImpl info = null;

        if (controller.getORB().hasServerRequestInterceptors())
        {
            //RequestInfo attributes
            info = new ServerRequestInfoImpl(controller.getORB(), 
                                             request, 
                                             servant);

            InterceptorManager manager = controller.getORB().getInterceptorManager();
            info.current = manager.getEmptyCurrent();

            if(! invokeInterceptors( info, 
                                     ServerInterceptorIterator.
                                     RECEIVE_REQUEST_SERVICE_CONTEXTS))
            {   
                //an interceptor bailed out, so don't continue request
                //processing and return here. The service contexts for
                //the result have to be set, of course.
                ReplyOutputStream out = request.getReplyOutputStream();
                Enumeration ctx = info.getReplyServiceContexts();
                                
                while( ctx.hasMoreElements() )
                {
                    out.addServiceContext( (ServiceContext) ctx.nextElement() );
                }

                return;
            }

            manager.setTSCurrent(info.current);
        }

        //org.jacorb.util.Debug.output(2, ">>>>>>>>>>>> process req pre invoke");

        if (servantManager != null)
        {
            if (servantManager instanceof org.omg.PortableServer.ServantActivator)
                invokeIncarnate();
            else
                invokePreInvoke();                                    
        }
    
        if (servant != null) 
        {
            if (info != null)
            {
                info.setServant(servant);

                if (servant instanceof org.omg.CORBA.portable.InvokeHandler)
                {
                    if(! invokeInterceptors(info, 
                                            ServerInterceptorIterator.RECEIVE_REQUEST )) 
                    {
                        //an interceptor bailed out, so don't continue
                        //request processing and return here. The
                        //service contexts for the result have to be
                        //set, of course.

                        if( cookieHolder != null )
                        {
                            invokePostInvoke();
                        }

                        ReplyOutputStream out = 
                            request.getReplyOutputStream();
                        Enumeration ctx = 
                            info.getReplyServiceContexts();
                                
                        while( ctx.hasMoreElements() )
                        {
                            out.addServiceContext( (ServiceContext) ctx.nextElement() );
                        }

                        return;
                    }
                }
                else if (servant instanceof org.omg.PortableServer.DynamicImplementation) 
                    request.setServerRequestInfo(info);

            }
            //org.jacorb.util.Debug.output(2, ">>>>>>>>>>>> process req pre invoke");

            invokeOperation();  
        }

        // preinvoke and postinvoke are always called in pairs
        // but what happens if the servant is null

        if (cookieHolder != null) 
        {
            //org.jacorb.util.Debug.output(2, ">>>>>>>>>>>> process req pre post invoke");
            invokePostInvoke();
        }
   
        if (info != null)
        {
            InterceptorManager manager = 
                controller.getORB().getInterceptorManager();
            info.current = manager.getCurrent();

            short op = 0;
            switch(request.status().value())
            {
                case ReplyStatusType_1_2._NO_EXCEPTION :
                    op = ServerInterceptorIterator.SEND_REPLY;
                    info.reply_status = SUCCESSFUL.value;
                    break;

                case ReplyStatusType_1_2._USER_EXCEPTION :
                    info.reply_status = USER_EXCEPTION.value;
                    SystemExceptionHelper.insert(info.sending_exception, 
                                                 new org.omg.CORBA.UNKNOWN("Stream-based UserExceptions are not available!"));
                    op = ServerInterceptorIterator.SEND_EXCEPTION;
                    break;

                case ReplyStatusType_1_2._SYSTEM_EXCEPTION :
                    info.reply_status = SYSTEM_EXCEPTION.value;
                    SystemExceptionHelper.insert(info.sending_exception, 
                                                 request.getSystemException());
                    op = ServerInterceptorIterator.SEND_EXCEPTION;
                    break;

                case ReplyStatusType_1_2._LOCATION_FORWARD :
                    info.reply_status = LOCATION_FORWARD.value;
                    op = ServerInterceptorIterator.SEND_OTHER;
                    break;
            }
      
            invokeInterceptors(info, op);

            ReplyOutputStream out = 
                request.get_out();
            Enumeration ctx = 
                info.getReplyServiceContexts();
                                
            while( ctx.hasMoreElements() )
            {
                out.addServiceContext( (ServiceContext) ctx.nextElement() );
            }

            manager.removeTSCurrent();
        }
        //org.jacorb.util.Debug.output(2, ">>>>>>>>>>>> process req end");
    }

    private boolean invokeInterceptors( ServerRequestInfoImpl info,
                                        short op )
    {

        ServerInterceptorIterator intercept_iter = 
            controller.getORB().getInterceptorManager().getServerIterator();
      
        try
        {
            intercept_iter.iterate(info, op);
        } 
        catch(org.omg.CORBA.UserException ue)
        {
            if (ue instanceof org.omg.PortableInterceptor.ForwardRequest)
            {
                org.omg.PortableInterceptor.ForwardRequest fwd =
                    (org.omg.PortableInterceptor.ForwardRequest) ue;

                request.setLocationForward( new org.omg.PortableServer.
                    ForwardRequest(fwd.forward) );
            }
            return false;

        } 
        catch (org.omg.CORBA.SystemException _sys_ex) 
        {
            request.setSystemException(_sys_ex);
            return false;
        }
        return true;
    }

  
    /**
     * the main loop for request processing
     */

    public void run() 
    {
        while (!terminate) 
        {
            synchronized (this) 
            {
                try 
                {
                    while( ! start )
                    {
                        wait(); /* waits for the next task */
                        
                        if(terminate) 
                        {
                            return;
                        }
                    } 
                }
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }
            
            if (controller.getLogTrace().test(2))
                controller.getLogTrace().printLog(request, "process request");
                        
            process();
                    
            // return the request to the request controller
            if (controller.getLogTrace().test(3))
                controller.getLogTrace().printLog(request, "ends with request processing");
            controller.returnResult(request);
                        
            start = false;
            clear();

            // give back the processor into the pool
            poolManager.releaseProcessor(this);
        }
    }

    //      private RequestProcessor() {
    //      }

    /*
      org.jacorb.orb.connection.ServerConnection getConnection()
      {
      return request.getConnection();
      }
    */
}




