package org.jacorb.orb.dii;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.portable.*;

import org.jacorb.orb.portableInterceptor.*;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.giop.*;
import org.jacorb.orb.*;

import java.util.Enumeration;

/**
 * DII requests
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class Request
    extends org.omg.CORBA.Request
{
    private org.jacorb.orb.NamedValue result_value;
    private org.omg.CORBA.ExceptionList exceptions;
    private org.omg.CORBA.ContextList contexts;
    private org.omg.CORBA.Context ctx;
    private Thread deferred_caller;
    private org.jacorb.orb.ORB orb;
    private org.omg.CORBA.portable.InputStream reply;

    /* state of request object */
    private boolean immediate = false;
    private boolean deferred = false;
    private boolean finished = false;

    public org.omg.CORBA.Object target;
    public ClientConnection connection;
    public byte[] object_key;
    public NVList arguments;
    public String operation;
    public org.omg.CORBA.Environment env = new Environment();

    private ClientRequestInfoImpl info = null;

    public Request( org.omg.CORBA.Object t,
                    org.omg.CORBA.ORB _orb,
                    ClientConnection e,
                    byte[] obj_key,
                    String op)
    {
        target = t;
        orb = (org.jacorb.orb.ORB)_orb;
        connection = e;
        object_key = obj_key;
        operation = op;
        exceptions = new ExceptionList();
        arguments = (NVList)orb.create_list(10);
        Any a = orb.create_any();

        /* default return type is void */
        a.type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_void ) );
        result_value = new org.jacorb.orb.NamedValue(1);
        result_value.set_value(a);
    }

    public Request( org.omg.CORBA.Object t,
                    org.omg.CORBA.ORB _orb,
                    ClientConnection e,
                    byte[] obj_key,
                    String op,
                    org.omg.CORBA.NVList args,
                    org.omg.CORBA.Context c,
                    org.omg.CORBA.NamedValue result)
    {
        target = t;
        orb = (org.jacorb.orb.ORB)_orb;
        connection = e;
        object_key = obj_key;
        operation = op;
        exceptions = new ExceptionList();
        arguments = (NVList)args;
        ctx = c;
        result_value = (org.jacorb.orb.NamedValue)result;
    }

    public org.omg.CORBA.Object target()
    {
        return target;
    }

    public java.lang.String operation()
    {
        return operation;
    }

    public org.omg.CORBA.NVList arguments()
    {
        return arguments;
    }

    public org.omg.CORBA.NamedValue result()
    {
        return result_value;
    }

    public org.omg.CORBA.Environment env()
    {
        return env;
    }

    public org.omg.CORBA.ExceptionList exceptions()
    {
        return exceptions;
    }

    public org.omg.CORBA.ContextList contexts()
    {
        return contexts;
    }

    public org.omg.CORBA.Context ctx()
    {
        return ctx;
    }

    public void ctx( org.omg.CORBA.Context a)
    {
        ctx = a;
    }

    public Any add_in_arg()
    {
        NamedValue nv = arguments.add(org.omg.CORBA.ARG_IN.value);
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    public Any add_named_in_arg(java.lang.String name)
    {
        NamedValue nv = arguments.add_item(name,org.omg.CORBA.ARG_IN.value);
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    public Any add_inout_arg()
    {
        NamedValue nv = arguments.add(org.omg.CORBA.ARG_INOUT.value);
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    public Any add_named_inout_arg(java.lang.String name)
    {
        NamedValue nv = arguments.add_item(name,org.omg.CORBA.ARG_INOUT.value);
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    public Any add_out_arg()
    {
        NamedValue nv = arguments.add(org.omg.CORBA.ARG_OUT.value);
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    public Any add_named_out_arg(java.lang.String name)
    {
        NamedValue nv = arguments.add_item( name, org.omg.CORBA.ARG_OUT.value );
        ((org.jacorb.orb.NamedValue)nv).set_value( orb.create_any());
        return nv.value();
    }

    /**
     * default return type is void
     */

    public void set_return_type( org.omg.CORBA.TypeCode tc)
    {
        result_value.value().type(tc);
    }

    public Any return_value()
    {
        return result_value.value();
    }

    private void _read_result()
    {
        if( result_value.value().type().kind() != org.omg.CORBA.TCKind.tk_void )
            result_value.value().read_value( reply, result_value.value().type() );

        /** get out/inout parameters if any */
        for( Enumeration e = ((org.jacorb.orb.NVList)arguments).enumerate(); e.hasMoreElements();)
        {
            org.jacorb.orb.NamedValue nv =
                (org.jacorb.orb.NamedValue)e.nextElement();
            if( nv.flags() != org.omg.CORBA.ARG_IN.value )
                nv.receive(reply);
        }
    }

    private void _invoke( boolean response_expected )
    {
        while (true)
        {
            org.jacorb.orb.Delegate deleg =
                (org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)target)._get_delegate();

            RequestOutputStream ros = (RequestOutputStream)
                deleg.request(target, operation, response_expected);

            ros.setRequest(this);

            for( Enumeration e = ((org.jacorb.orb.NVList)arguments).enumerate(); e.hasMoreElements();)
            {
                org.jacorb.orb.NamedValue nv = (org.jacorb.orb.NamedValue)e.nextElement();
                if( nv.flags() != org.omg.CORBA.ARG_OUT.value )
                    nv.send(ros);
            }

            try
            {
                reply = deleg.invoke(target, ros);

                if( response_expected )
                {
                    _read_result();

                    if (info != null)
                    {
                        info.result = result_value.value();
                        InterceptorManager manager = orb.getInterceptorManager();
                        info.current = manager.getCurrent();

                        try{
                            deleg.invokeInterceptors(info,
                                                     ClientInterceptorIterator.RECEIVE_REPLY);
                        }catch(RemarshalException rem){
                            //not allowed to happen here anyway
                        }
                        info = null;
                    }
                }
            }
            catch (RemarshalException rem)
            {
                // Try again
                continue;
            }
            catch (ApplicationException ae)
            {
                org.omg.CORBA.Any any;
                org.omg.CORBA.TypeCode tc;
                String id = ae.getId ();
                int count = (exceptions == null) ? 0 : exceptions.count ();

                for (int i = 0; i < count; i++)
                {
                    try
                    {
                        tc = exceptions.item (i);
                        if (id.equals (tc.id ()))
                        {
                            any = orb.create_any ();
                            any.read_value (ae.getInputStream (), tc);
                            env.exception (new org.omg.CORBA.UnknownUserException (any));
                            break;
                        }
                    }
                    catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
                    {
                    }
                    catch (org.omg.CORBA.Bounds ex)
                    {
                       break;
                    }
                }

                break;
            }
            catch (Exception e)
            {
                env.exception (e);
                break;
            }

            break;
        }
    }

    public void setInfo(ClientRequestInfoImpl info)
    {
        this.info = info;
    }

    public void invoke()
    {
        start();
        _invoke(true);
        finish();
    }

    public void send_oneway()
    {
        start();
        _invoke(false);
        finish();
    }


    // Made static as does not depend upon Request instance information.
    static class Caller extends Thread
    {
        private Request r;

        public Caller( Request client )
        {
            r = client;
        }

        public void run()
        {
            r._invoke(true);
            r.finish();
        }
    }


    public synchronized void send_deferred()
    {
        defer();
        orb.addRequest( this );
        deferred_caller = new Caller( this );
        deferred_caller.start();
    }

    public synchronized void get_response()
    {
        if( ! immediate && ! deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 11, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        if ( immediate )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 13, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }

        if( deferred_caller != null )
        {
            if ( deferred_caller.isAlive() )
            {
                try
                {
                    deferred_caller.join();
                }
                catch ( InterruptedException i ){}
            }
            deferred_caller = null;
            orb.removeRequest( this );
        }
    }

    public boolean poll_response()
    {
        Thread.yield();
        if( ! immediate && ! deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 11, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        if ( immediate )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 13, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        if ( deferred_caller == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 12, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return finished;
    }

    private synchronized void start()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        if( immediate || deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 10, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        immediate = true;
    }

    private synchronized void defer()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        if( immediate || deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER
                ( 10, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        deferred = true;
    }

    private void finish()
    {
        finished = true;
    }
}
