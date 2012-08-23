package org.jacorb.orb.dii;

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

import java.util.Iterator;
import org.slf4j.Logger;
import org.jacorb.orb.ORB;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.RequestOutputStream;
import org.jacorb.orb.portableInterceptor.ClientInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.portableInterceptor.InterceptorManager;
import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;

/**
 * DII requests
 *
 * @author Gerald Brose, FU Berlin
 */
public class Request
    extends org.omg.CORBA.Request
{
    public final org.omg.CORBA.Object target;
    public final ClientConnection connection;
    public final byte[] object_key;
    public final NVList arguments;
    public final String operation;
    public final org.omg.CORBA.Environment env = new Environment();

    private final org.jacorb.orb.NamedValue result_value;
    private final org.omg.CORBA.ExceptionList exceptions;
    private final org.jacorb.orb.ORB orb;

    private final Logger logger;

    private final org.omg.CORBA.ContextList contexts = new ContextListImpl();
    private org.omg.CORBA.Context context;
    private Caller deferred_caller;
    private org.omg.CORBA.portable.InputStream reply;

    /* state of request object */
    private boolean immediate = false;
    private boolean deferred = false;
    private boolean finished = false;

    private ClientRequestInfoImpl info = null;

    public Request( org.omg.CORBA.Object target,
                    ORB orb,
                    ClientConnection connection,
                    byte[] obj_key,
                    String operationName)
    {
        this(target,
             orb,
             connection,
             obj_key,
             operationName,
             orb.create_list(10),
             null,
             createVoidResultValue(orb));
    }

    private static final NamedValue createVoidResultValue(ORB orb)
    {
        Any any = orb.create_any();
        any.type(orb.get_primitive_tc(TCKind.tk_void));
        org.jacorb.orb.NamedValue namedValue = new org.jacorb.orb.NamedValue(1);
        namedValue.set_value(any);
        return namedValue;
    }

    public Request( org.omg.CORBA.Object target,
                    ORB orb,
                    ClientConnection connection,
                    byte[] obj_key,
                    String op,
                    org.omg.CORBA.NVList args,
                    org.omg.CORBA.Context context,
                    org.omg.CORBA.NamedValue result)
    {
        super();

        this.target = target;
        this.orb = orb;
        this.connection = connection;
        this.object_key = obj_key;
        this.operation = op;
        exceptions = new ExceptionList();

        this.arguments = args;
        this.context = context;
        result_value = (org.jacorb.orb.NamedValue)result;

        logger = orb.getConfiguration().getLogger("jacorb.dii.request");
    }

    public Request( org.omg.CORBA.Object target,
                    ORB orb,
                    ClientConnection connection,
                    byte[] obj_key,
                    String op,
                    org.omg.CORBA.NVList args,
                    org.omg.CORBA.Context context,
                    org.omg.CORBA.NamedValue result,
                    org.omg.CORBA.ExceptionList exceptions,
                    org.omg.CORBA.ContextList contexts )
    {
        super();

        this.target = target;
        this.orb = orb;
        this.connection = connection;
        this.object_key = obj_key;
        this.operation = op;
        this.exceptions = exceptions;
        this.arguments = args;
        this.context = context;
        result_value = (org.jacorb.orb.NamedValue)result;

        if (contexts != null)
        {
            throw new NO_IMPLEMENT ("The contexts parameters has not yet been implemented - pass null for this");
        }

        logger = orb.getConfiguration().getLogger("jacorb.dii.request");
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

    /**
     * The <code>env</code> method returns a CORBA Environment object. This
     * allows a user to retrieve any exceptions that were thrown during
     * processing of this request.
     * If the remote process throws a known exception i.e. one that has been
     * provided within the ExceptionList parameter then actual exception may
     * be extracted via this function from the UnknownUserException Any that
     * is passed back.
     * If the remote process throws a CORBA system exception or a Java exception
     * then these will also be stored within this env parameter.
     * If the remote process throws a UserException that has not been defined
     * via the ExceptionList then a CORBA UNKNOWN will be thrown containing the
     * typecode of the unknown exception.
     *
     * @return an <code>org.omg.CORBA.Environment</code> value
     */
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
        return context;
    }

    public void ctx( org.omg.CORBA.Context ctx)
    {
        this.context = ctx;
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
        {
            result_value.value().read_value( reply, result_value.value().type() );
        }

        /** get out/inout parameters if any */
        for( Iterator e = ((org.jacorb.orb.NVList)arguments).iterator(); e.hasNext();)
        {
            org.jacorb.orb.NamedValue nv =
                (org.jacorb.orb.NamedValue)e.next();
            if( nv.flags() != org.omg.CORBA.ARG_IN.value )
            {
                nv.receive(reply);
            }
        }
    }

    private void _invoke( boolean response_expected )
    {
        if (logger.isDebugEnabled ())
        {
            logger.debug("DII::Request for " + target);
        }

        while (true)
        {
            org.jacorb.orb.Delegate delegate =
                (org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)target)._get_delegate();

            final RequestOutputStream out = (RequestOutputStream)delegate.request(target, operation, response_expected);

            try
            {
                out.setRequest(this);

                for( Iterator it = ((org.jacorb.orb.NVList)arguments).iterator(); it.hasNext();)
                {
                    org.jacorb.orb.NamedValue namedValue = (org.jacorb.orb.NamedValue)it.next();
                    if( namedValue.flags() != org.omg.CORBA.ARG_OUT.value )
                    {
                        namedValue.send(out);
                    }
                }

                try
                {
                    reply = delegate.invoke(target, out);

                    if( response_expected )
                    {
                        _read_result();

                        if (info != null)
                        {
                            info.setResult (result_value.value());
                            InterceptorManager manager = orb.getInterceptorManager();
                            info.setCurrent (manager.getCurrent());

                            try
                            {
                                delegate.invokeInterceptors(info,
                                        ClientInterceptorIterator.RECEIVE_REPLY);
                            }
                            catch(RemarshalException e)
                            {
                                //not allowed to happen here anyway
                                throw new INTERNAL("should not happen");
                            }
                            info = null;
                        }
                    }
                }
                catch (RemarshalException e)
                {
                    // Try again
                    continue;
                }
                catch (ApplicationException e)
                {
                   if (logger.isDebugEnabled ())
                   {
                      logger.debug("DII Request caught ApplicationException", e);
                   }

                    org.omg.CORBA.Any any;
                    org.omg.CORBA.TypeCode typeCode;
                    String id = e.getId ();
                    int count = (exceptions == null ? 0 : exceptions.count ());

                    // since count > 0, this means that we were provided a
                    // list of exceptions that can be thrown by the invoked
                    // operation.  see if the ApplicationException we
                    // received matches any item in the exception list and
                    // set it if it does
                    for (int i = 0; i < count; i++)
                    {
                        try
                        {
                            typeCode = exceptions.item (i);

                            if (id.equals (typeCode.id ()))
                            {
                               if (logger.isDebugEnabled ())
                               {
                                  logger.debug ("Found matching typecode of " + id + " to throw.");
                               }
                                any = orb.create_any ();
                                any.read_value (e.getInputStream (), typeCode);
                                env.exception (new org.omg.CORBA.UnknownUserException (any));
                                break;
                            }
                        }
                        catch (org.omg.CORBA.TypeCodePackage.BadKind ex) // NOPMD
                        {
                            // Ignored
                        }
                        catch (org.omg.CORBA.Bounds ex)
                        {
                            break;
                        }
                    }
                    if (count == 0)
                    {
                        env.exception
                        (
                            new UNKNOWN
                            (
                                "Caught an unknown exception with typecode id of " +
                                e.getInputStream ().read_string (),
                                0,
                                CompletionStatus.COMPLETED_YES
                            )
                        );
                    }

                    break;
                }
                catch (Exception e)
                {
                   if (logger.isDebugEnabled ())
                   {
                      logger.debug("DII Request caught Exception", e);
                   }
                    env.exception (e);
                    break;
                }

                break;
            }
            finally
            {
                out.close();
            }
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

    private static class Caller extends Thread
    {
        private final Request request;
        private boolean active = true;

        public Caller( Request client )
        {
            request = client;
        }

        public void run()
        {
            request._invoke(true);
            request.finish();

            synchronized(request)
            {
                active = false;
                request.notifyAll();
            }
        }

        public void joinWithCaller()
        {
            synchronized(request)
            {
                while(active)
                {
                    try
                    {
                        request.wait();
                    }
                    catch(InterruptedException e) // NOPMD
                    {
                        // ignored
                    }
                }
            }
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
           deferred_caller.joinWithCaller();
           deferred_caller = null;
           orb.removeRequest( this );
        }
    }

    public synchronized boolean poll_response()
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

    private synchronized void finish()
    {
        finished = true;
    }
}
