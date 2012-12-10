package org.jacorb.orb.portableInterceptor;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.util.ArrayList;
import java.util.Collection;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.poa.POA;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.UserException;
import org.omg.Dynamic.Parameter;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.UNKNOWN;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableServer.Servant;

/**
 * This class represents the type of info object
 * that will be passed to the ServerRequestInterceptors. <br>
 * See PI Spec p.5-50ff
 *
 * @author Nicolas Noffke
 */

public class ServerRequestInfoImpl
    extends RequestInfoImpl
    implements ServerRequestInfo
{
    //from ServerRequestInfo
    private byte[] adapter_id = null;
    private String [] adapter_name = null;
    private String target_most_derived_interface = null;

    private Servant servant = null;
    private final org.jacorb.orb.ORB orb;

    private final String operation;

    private final boolean response_expected;

    private final int requestId;

    private final byte [] objectId;

    public final ServerRequest request;

    public Any sending_exception = null;


    public ServerRequestInfoImpl( org.jacorb.orb.ORB orb,
                                  ServerRequest request,
                                  Servant servant)
    {
        super();

        this.orb = orb;
        this.request = request;
        operation = request.operation();
        response_expected = request.responseExpected();
        sync_scope = request.syncScope();
        requestId = request.requestId();
        objectId = request.objectId();

        if (servant != null)
        {
            setServant(servant);
        }

        setRequestServiceContexts(request.getServiceContext());

        sending_exception = orb.create_any();
    }

    /**
     * This constructor is to cater for local calls where portable
     * interceptors are involved.  These calls are now handled
     * locally rather than via the remote request mechanism so there
     * will be no request from which to obtain information
     * @param bs
     */
    public ServerRequestInfoImpl( org.jacorb.orb.ORB orb,
                                  Collection<ServiceContext> contexts,
                                  Servant servant,
                                  byte[] objectId,
                                  String operation,
                                  boolean response_expected,
                                  short sync_scope)
    {
        super();

        this.orb = orb;
        this.operation = operation;
        this.response_expected = response_expected;
        this.sync_scope = sync_scope;
        this.objectId = objectId;

        requestId = 0;
        request = null;


        if (servant != null)
        {
            setServant(servant);
        }

        if (contexts != null)
        {
           setRequestServiceContexts(contexts.toArray (new ServiceContext[contexts.size ()]));
        }

        sending_exception = orb.create_any();
    }

    /**
     * The servant is sometimes not available on calling
     * receive_request_service_contexts (e.g. in case of
     * ServantLocators or ServantActivators).
     */

    public final void setServant(Servant servant)
    {
        this.servant = servant;
        org.jacorb.poa.POA poa = (org.jacorb.poa.POA) servant._poa();
        adapter_id = poa.getPOAId();
        String[] all_ifs = servant._all_interfaces(poa, servant._object_id());
        target_most_derived_interface = all_ifs[0];

        POA parent = poa;
        ArrayList<String> al = new ArrayList<String> ();

        while (parent != null)
        {
           al.add (parent.the_name ());
           parent = (POA) parent.the_parent ();
         }
         int size = al.size ();
         adapter_name = new String [size];

         // We can't just do toArray as otherwise adapter_name would be in
         // reverse order.
         for (int j = 0, i = (size-1); i >= 0; i--, j++)
         {
            adapter_name [j] = (String)al.get (i);
         }
    }

    /**
     * Set the sending_exception attribute.
     */

    public void update()
    {
        if (request != null)
        {
            if (! request.streamBased())
            {
                Any user_ex = request.except();
                if (user_ex != null)
                {
                    sending_exception = user_ex;
                }
            }

            SystemException sys_ex = request.getSystemException();
            if (sys_ex != null)
            {
                org.jacorb.orb.SystemExceptionHelper.insert(sending_exception, sys_ex);
            }

            forward_reference = request.getForwardReference();
        }
    }

    /**
     * Method introduced for local invocations as there is no Reply or Stream
     * involved
     */
    public void updateException (Throwable ex)
    {
        if (ex instanceof org.omg.CORBA.SystemException)
        {
            org.jacorb.orb.SystemExceptionHelper.insert (sending_exception,
                                                         ( org.omg.CORBA.SystemException) ex);
            setReplyStatus (SYSTEM_EXCEPTION.value);
        }
        else if (ex instanceof org.omg.PortableInterceptor.ForwardRequest)
        {
            forward_reference = (( org.omg.PortableInterceptor.ForwardRequest) ex).forward;
            setReplyStatus (LOCATION_FORWARD.value);
        }
        else if (ex instanceof UserException)
        {
            org.jacorb.orb.SystemExceptionHelper.insert
            (
                sending_exception,
                new org.omg.CORBA.UNKNOWN ("Received UserException " + ex)
            );

            setReplyStatus (USER_EXCEPTION.value);
        }
    }


    /**
     * returns a reference to the calls target.
     */

    public org.omg.CORBA.Object target()
    {
        return servant._this_object();
    }

    // implementation of RequestInfoOperations interface

    public Parameter[] arguments()
    {
        if (!(caller_op == ServerInterceptorIterator.RECEIVE_REQUEST) &&
            !(caller_op == ServerInterceptorIterator.SEND_REPLY))
        {
            throw new BAD_INV_ORDER("The attribute \"arguments\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        if (arguments == null)
        {
            throw new NO_RESOURCES("Stream-based skeletons/stubs do not support this op",
                                   1, CompletionStatus.COMPLETED_MAYBE);
        }

        return arguments;
    }

    public TypeCode[] exceptions()
    {
        throw new NO_RESOURCES("This feature is not supported on the server side",
                               1, CompletionStatus.COMPLETED_MAYBE);
    }

    public Any result()
    {
        if ( caller_op != ServerInterceptorIterator.SEND_REPLY )
        {
            throw new BAD_INV_ORDER("The attribute \"result\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        Any result = null;

        try
        {
            if (request != null)
            {
                result = request.result();
            }
        }
        catch(Exception e)
        {
        }

        if (result == null)
        {
            throw new NO_RESOURCES("Stream-based skeletons/stubs do not support this op",
                                   1, CompletionStatus.COMPLETED_MAYBE);
        }

        return result;
    }

    public short sync_scope()
    {
        return sync_scope;
    }

    public short reply_status()
    {
        if ((caller_op == ServerInterceptorIterator.RECEIVE_REQUEST ||
             caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS) &&
            reply_status != UNKNOWN.value)
        {
            throw new BAD_INV_ORDER("The attribute \"reply_status\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return reply_status;
    }

    public org.omg.CORBA.Object forward_reference()
    {
        if (! (caller_op != ServerInterceptorIterator.SEND_OTHER) ||
            (reply_status != LOCATION_FORWARD.value))
        {
            throw new BAD_INV_ORDER("The attribute \"forward_reference\" is currently " +
                                    "invalid!", 10, CompletionStatus.COMPLETED_MAYBE);
        }

        return forward_reference;
    }

    public ServiceContext get_reply_service_context(int id)
    {
        if ((caller_op == ServerInterceptorIterator.RECEIVE_REQUEST) ||
            (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS))
        {
            throw new BAD_INV_ORDER("The operation \"get_reply_service_context\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        return super.get_reply_service_context(id);
    }

    public String operation()
    {
        return operation;
    }

    public int request_id()
    {
        return requestId;
    }

    public boolean response_expected()
    {
        return response_expected;
    }

    // implementation of ServerRequestInfoOperations interface
    public Any sending_exception()
    {
        if (caller_op != ServerInterceptorIterator.SEND_EXCEPTION)
        {
            throw new BAD_INV_ORDER("The attribute \"sending_exception\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        return sending_exception;
    }

    public byte[] object_id()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER("The attribute \"object_id\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return objectId;
    }

    public byte[] adapter_id()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER("The attribute \"adapter_id\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return adapter_id;
    }

    public String orb_id()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER ("The attribute \"orb_id\" is currently invalid!",
                                     14,
                                     CompletionStatus.COMPLETED_MAYBE);
        }

        return orb.id();
    }

    public String server_id()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new NO_RESOURCES ("The attribute \"server_id\" is not available",
                                    1,
                                    CompletionStatus.COMPLETED_MAYBE);
        }
        else
        {
            throw new BAD_INV_ORDER ("The attribute \"server_id\" is currently invalid!",
                                     14,
                                     CompletionStatus.COMPLETED_MAYBE);
        }
    }



    public String [] adapter_name()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER ("The attribute \"adapter_name\" is currently invalid!",
                                     14,
                                     CompletionStatus.COMPLETED_MAYBE);
        }

        return adapter_name;
    }


    public String target_most_derived_interface()
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER("The attribute \"target_most_derived_interface\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        return target_most_derived_interface;
    }

    /**
     * WARNING: This method relies on the DomainService to be available.
     * Make shure that the DS is running, if you want to call this method.
     */
    public Policy get_server_policy(int type) {
        if (! orb.hasPolicyFactoryForType(type))
        {
            throw new INV_POLICY("No PolicyFactory for type " + type +
                                 " has been registered!", 2,
                                 CompletionStatus.COMPLETED_MAYBE);
        }

        try
        {
            org.jacorb.orb.ServantDelegate delegate = (org.jacorb.orb.ServantDelegate) servant._get_delegate();
            return delegate._get_policy(servant._this_object(), type);
        }
        catch(INV_POLICY e)
        {
            e.minor = 2;
            throw e;
        }
    }

    public void set_slot(int id, Any data) throws InvalidSlot
    {
        current.set_slot(id, data);
    }

    public boolean target_is_a(String id)
    {
        if (caller_op == ServerInterceptorIterator.RECEIVE_REQUEST_SERVICE_CONTEXTS)
        {
            throw new BAD_INV_ORDER("The operation \"target_is_a\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return servant._is_a(id);
    }

    public void add_reply_service_context(ServiceContext service_context,
                                          boolean replace)
    {
        Integer _id = Integer.valueOf (service_context.context_id);

        if (! replace && reply_ctx.containsKey(_id))
        {
            throw new BAD_INV_ORDER("The ServiceContext with id " + _id.toString()
                                    + " has already been set!", 11,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        reply_ctx.put(_id, service_context);
    }
}
