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

package org.jacorb.orb.portableInterceptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.TypeCode;
import org.omg.Dynamic.Parameter;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.UNKNOWN;

/**
 * This is the abstract base class of the two
 * Info classes, namely ClientRequestInfo and
 * ServerRequestInfo. <br>
 * See PI Spec p. 5-41ff
 *
 * @author Nicolas Noffke
 */

public abstract class RequestInfoImpl
    extends org.omg.CORBA.LocalObject
    implements RequestInfo
{
    protected int request_id;
    protected String operation;

    protected Parameter[] arguments = null;
    protected TypeCode[] exceptions = null;
    protected Any result = null;
    protected boolean response_expected;
    protected org.omg.CORBA.Object forward_reference = null;
    protected short reply_status = UNKNOWN.value;
    protected org.omg.PortableInterceptor.Current current = null;

    protected short sync_scope;

    protected final Map<Integer, ServiceContext> request_ctx;
    protected final Map<Integer, ServiceContext> reply_ctx;

    protected short caller_op = -1;

    public RequestInfoImpl()
    {
        super();

        request_ctx = new HashMap<Integer, ServiceContext>();
        reply_ctx = new HashMap<Integer, ServiceContext>();
    }

    /**
     * Make the existing request ServiceContexts available to
     * the interceptors. Only one ServiceContext per id
     * is allowed.
     */
    public void setRequestServiceContexts(ServiceContext[] ctx)
    {
        synchronized(request_ctx)
        {
            for (int i = 0; i < ctx.length; i++)
            {
                request_ctx.put(ctx[i].context_id, ctx[i]);
            }
        }
    }

    /**
     * This is a method to be used when handling local calls that involve
     * portable interceptors.  There is no request/stream so we need
     * to get the service contexts as an array
     */
    public Collection<ServiceContext> getRequestServiceContexts()
    {
       return request_ctx.values ();
    }

    /**
     * Make the existing reply ServiceContexts available to
     * the interceptors. Only one ServiceContext per id
     * is allowed.
     */
    public void setReplyServiceContexts(ServiceContext[] ctx)
    {
        synchronized(reply_ctx)
        {
            for (int i = 0; i < ctx.length; i++)
            {
                reply_ctx.put(ctx[i].context_id, ctx[i]);
            }
        }
    }

    /**
     * This is a method to be used when handling local calls that involve
     * portable interceptors.  There is no request/stream so we need
     * to get the service contexts as an array
     */
    public Collection<ServiceContext> getReplyServiceContexts()
    {
       return reply_ctx.values ();
    }

    public void setArguments (Parameter[] args)
    {
        this.arguments = args;
    }

    public void setResult (Any result)
    {
        this.result = result;
    }

    public org.omg.PortableInterceptor.Current current()
    {
        return current;
    }

    public void setCurrent (org.omg.PortableInterceptor.Current current)
    {
        this.current = current;
    }

    public void setReplyStatus (short reply_status)
    {
        this.reply_status = reply_status;
    }

    public void setForwardReference (org.omg.CORBA.Object forward_reference)
    {
        this.forward_reference = forward_reference;
    }

    // implementation of org.omg.PortableInterceptor.RequestInfoOperations interface
    public Parameter[] arguments()
    {
        return arguments;
    }

    public String[] contexts()
    {
        throw new NO_RESOURCES("JacORB does not support operation contexts",
                               1, CompletionStatus.COMPLETED_MAYBE);
    }

    public TypeCode[] exceptions()
    {
        return exceptions;
    }

    public org.omg.CORBA.Object forward_reference()
    {
        return forward_reference;
    }

    public ServiceContext get_reply_service_context(int id)
    {
        final ServiceContext result;

        synchronized(reply_ctx)
        {
            result = (ServiceContext) reply_ctx.get(id);
        }

        if (result == null)
        {
            throw new BAD_PARAM("No ServiceContext with id " + id, 23, CompletionStatus.COMPLETED_MAYBE);
        }

        return result;
    }

    public ServiceContext get_request_service_context(int id)
    {
        final ServiceContext result;

        synchronized(request_ctx)
        {
            result = (ServiceContext) request_ctx.get(Integer.valueOf(id));
        }

        if (result == null)
        {
            throw new BAD_PARAM("No ServiceContext with id " + id, 23, CompletionStatus.COMPLETED_MAYBE);
        }

        return result;
    }

    public Any get_slot(int id) throws InvalidSlot
    {
        return current.get_slot(id);
    }

    public String operation() {
        return operation;
    }

    public String[] operation_context()
    {
        throw new NO_RESOURCES("JacORB does not support operation contexts", 1,
                               CompletionStatus.COMPLETED_MAYBE);
    }

    public short reply_status()
    {
        return reply_status;
    }

    public int request_id()
    {
        return request_id;
    }

    public boolean response_expected()
    {
        return response_expected;
    }

    public Any result()
    {
        if (result == null)
        {
            throw new NO_RESOURCES("Stream-based skeletons/stubs do not support this op",
                                   1, CompletionStatus.COMPLETED_MAYBE);
        }

        return result;
    }

    /**
     * Return the sync_scope as set in the constructor
     */
    public short sync_scope()
    {
        return sync_scope;
    }
}
