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

import org.omg.PortableInterceptor.*;

import java.util.*;
import org.omg.CORBA.LocalObject;

public abstract class RecursionAwareCI
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    private Hashtable thread_stacks = null;
    
    private Hashtable ignore_operations = null;

    /**
     * @param ignore_special_ops If set to true, calls to
     * methods from the CORBA.Object interface like _is_a 
     * will be ignored.
     */
    public RecursionAwareCI( boolean ignore_special_ops ) 
    {
        thread_stacks = new Hashtable();
        ignore_operations = new Hashtable();

        if ( ignore_special_ops )
        {
            ignore_operations.put( "_is_a", "" );
            ignore_operations.put( "_get_interface", "" );
            ignore_operations.put( "_non_existent", "" );            
            ignore_operations.put( "_get_policy", "" );
            ignore_operations.put( "_get_domain_managers", "" );
            ignore_operations.put( "_set_policy_overrides", "" );
        }
    }

    public void addIgnoreOperation( String operation_name )
    {
        ignore_operations.put( operation_name, operation_name );
    }

    private boolean enterCall( String operation )
    {
        if ( ignore_operations.containsKey( operation ) )
            return false;

        Thread current = Thread.currentThread();

        if ( thread_stacks.containsKey( current ))
            return false;

        thread_stacks.put( current, current );

        return true;
    }

    private void exitCall()
    {
        thread_stacks.remove( Thread.currentThread() );
    }

    // implementation InterceptorOperations interface
    public final void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        if( enterCall( ri.operation() ))
        {
            try
            {
                do_send_request( ri );
            }
            finally
            {
                exitCall();
            }
        }
    }

    public final void send_poll(ClientRequestInfo ri)
    {
        if( enterCall( ri.operation() ))
        {
            try
            {
                do_send_poll( ri );
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











