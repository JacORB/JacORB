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
package org.jacorb.orb.portableInterceptor;

import java.util.HashSet;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

public abstract class RecursionAwareCI
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private final HashSet thread_stacks;
    private final HashSet ignore_operations;

    /**
     * @param ignore_special_ops If set to true, calls to
     * methods from the CORBA.Object interface like _is_a
     * will be ignored.
     */
    public RecursionAwareCI( boolean ignore_special_ops )
    {
        super();

        thread_stacks = new HashSet();
        ignore_operations = new HashSet();

        if ( ignore_special_ops )
        {
            ignore_operations.add( "_is_a" );
            ignore_operations.add( "_get_interface" );
            ignore_operations.add( "_non_existent" );
            ignore_operations.add( "_get_policy" );
            ignore_operations.add( "_get_domain_managers" );
            ignore_operations.add( "_set_policy_overrides" );
        }
    }

    private boolean enterCall( String operation )
    {
        if ( ignore_operations.contains( operation ) )
        {
            return false;
        }

        Thread currentThread = Thread.currentThread();

        if ( thread_stacks.contains( currentThread ))
        {
            return false;
        }

        thread_stacks.add( currentThread);

        return true;
    }

    private void exitCall()
    {
        thread_stacks.remove( Thread.currentThread() );
    }

    // implementation InterceptorOperations interface
    public final void send_request( ClientRequestInfo requestInfo )
        throws ForwardRequest
    {
        if( enterCall( requestInfo.operation() ))
        {
            try
            {
                do_send_request( requestInfo );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public final void send_poll(ClientRequestInfo requestInfo)
    {
        if( enterCall( requestInfo.operation() ))
        {
            try
            {
                do_send_poll( requestInfo );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public final void receive_reply(ClientRequestInfo ri)
    {
        if( enterCall( ri.operation() ))
        {
            try
            {
                do_receive_reply( ri );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public final void receive_exception(ClientRequestInfo ri)
        throws ForwardRequest
    {
        if( enterCall( ri.operation() ))
        {
            try
            {
                do_receive_exception( ri );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public final void receive_other(ClientRequestInfo ri)
        throws ForwardRequest
    {
        if( enterCall( ri.operation() ))
        {
            try
            {
                do_receive_other( ri );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public abstract void do_send_request( ClientRequestInfo ri )
        throws ForwardRequest;

    public abstract void do_send_poll(ClientRequestInfo ri);

    public abstract void do_receive_reply(ClientRequestInfo ri);

    public abstract void do_receive_exception(ClientRequestInfo ri)
        throws ForwardRequest;

    public abstract void do_receive_other(ClientRequestInfo ri)
        throws ForwardRequest;
}
