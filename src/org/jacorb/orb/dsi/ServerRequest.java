package org.jacorb.orb.dsi;

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

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.giop.GIOPConnection;
import org.jacorb.orb.giop.Messages;
import org.jacorb.orb.giop.ReplyOutputStream;
import org.jacorb.orb.giop.RequestInputStream;
import org.jacorb.orb.portableInterceptor.ServerInterceptorIterator;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.util.Time;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.IOP.INVOCATION_POLICIES;
import org.omg.IOP.ServiceContext;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueSeqHelper;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.TimeBase.UtcT;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ServerRequest
    extends org.omg.CORBA.ServerRequest
    implements org.omg.CORBA.portable.ResponseHandler
{
    private final RequestInputStream inputStream;
    private ReplyOutputStream out;
    private final GIOPConnection connection;

    private UtcT requestStartTime = null,
                 requestEndTime   = null,
                 replyEndTime     = null;

    /**
     * <code>scopes</code> caches the scoped poa names.
     */
    private List scopes;

    /** config property */
    private final boolean cachePoaNames;

    private int replyStatus = ReplyStatusType_1_2._NO_EXCEPTION;
    private final byte[] oid;
    private final byte[] object_key;
    private org.omg.CORBA.Object reference = null;
    /**
     * <code>rest_of_name</code> is target poa's name in relation to parent.
     */
    private String[] rest_of_name = null;

    /* is this request stream or DSI-based ? */
    private boolean isStreamBased;

    private org.omg.CORBA.SystemException sys_ex;
    private org.omg.PortableServer.ForwardRequest location_forward;
    private org.omg.CORBA.Any exception;
    private org.omg.CORBA.Any result;
    private org.jacorb.orb.NVList argList;

    private final org.jacorb.orb.ORB orb;

    private boolean usePreconstructedReply = false; //for appligator

    private ServerRequestInfoImpl info = null;

    private final Logger logger;


    public ServerRequest( org.jacorb.orb.ORB orb,
                          RequestInputStream inStream,
                          GIOPConnection _connection )
    {
        super();

        this.orb = orb;
        Configuration config = orb.getConfiguration();
        this.logger = config.getNamedLogger("jacorb.org.giop");
        this.cachePoaNames = config.getAttribute("jacorb.cachePoaNames","off").equals("on");

        this.inputStream = inStream;
        connection = _connection;

        calcTimingPolicies();

        object_key =
            orb.mapObjectKey(org.jacorb.orb.ParsedIOR.extractObjectKey(inStream.req_hdr.target, orb));

        oid = org.jacorb.poa.util.POAUtil.extractOID( object_key );
    }

    /*
     * if this request could not be delivered directly to the correct
     * POA because the POA's adapter activator could not be called
     * when the parent POA was in holding state, the parent will queue
     * the request and later return it to the adapter layer. In order
     * to be able to find the right POA when trying to deliver again,
     * we have to remember the target POA's name
     */

    public void setRemainingPOAName(String [] rest_of_name)
    {
        this.rest_of_name = rest_of_name;
    }

    /**
     * <code>remainingPOAName</code> retrieves (if any) the target poa's
     * name in relation to parent.
     * @return a <code>String[]</code> value
     */
    public String[] remainingPOAName()
    {
        return rest_of_name;
    }

    public String operation()
    {
        return inputStream.req_hdr.operation;
    }

    /**
     * The resulting any must be used to create an input stream from
     * which the result value can be read.
     */

    public org.omg.CORBA.Any result()
    {
        if( isStreamBased )
        {
            org.omg.CORBA.Any any = orb.create_any();

            // create the output stream for the result

            CDROutputStream _out = ((CDROutputStream)any.create_output_stream());

            // get a copy of the content of this reply
            byte[] result_buf = out.getBody();

            // ... and insert it
            _out.setBuffer( result_buf  );
            // important: set the _out buffer's position to the end of the contents!
            _out.skip( result_buf.length );
            return any;
        }
        return result;
    }

    public org.omg.CORBA.NVList arguments()
    {
        if( isStreamBased )
        {
            throw new BAD_INV_ORDER("This ServerRequest is stream-based!");
        }
        return argList;
    }

    public org.omg.CORBA.Any except()
    {
        if( isStreamBased )
        {
            throw new BAD_INV_ORDER("This ServerRequest is stream-based!");
        }
        return exception;
    }

    public ReplyStatusType_1_2 status()
    {
        return ReplyStatusType_1_2.from_int( replyStatus );
    }

    public org.omg.CORBA.Context ctx()
    {
        return null;
    }

    public void arguments(org.omg.CORBA.NVList list)
    {
        argList = (org.jacorb.orb.NVList)list;
        // unmarshal

        if( argList != null )
        {
            inputStream.mark(0);
            for( Iterator e = argList.iterator();
                 e.hasNext(); )
            {
                org.omg.CORBA.NamedValue namedValue =
                    (org.omg.CORBA.NamedValue)e.next();

                if( namedValue.flags() != org.omg.CORBA.ARG_OUT.value )
                {
                    // out parameters are not received
                    try
                    {
                        namedValue.value().read_value( inputStream, namedValue.value().type() );
                    }
                    catch (Exception e1)
                    {
                        throw new org.omg.CORBA.MARSHAL("Couldn't unmarshal object of type "
                                                        + namedValue.value().type() + " in ServerRequest.");
                    }
                }
            }
            try
            {
                inputStream.reset();
            }
            catch (Exception e1)
            {
                throw new org.omg.CORBA.UNKNOWN("Could not reset input stream");
            }

            if (info != null)
            {
                //invoke interceptors
                org.omg.Dynamic.Parameter[] params = new org.omg.Dynamic.Parameter[argList.count()];
                for (int i = 0; i < params.length; i++)
                {
                    try
                    {
                        org.omg.CORBA.NamedValue value = argList.item(i);

                        org.omg.CORBA.ParameterMode mode = null;
                        if (value.flags() == org.omg.CORBA.ARG_IN.value)
                        {
                            mode = org.omg.CORBA.ParameterMode.PARAM_IN;
                        }
                        else if (value.flags() == org.omg.CORBA.ARG_OUT.value)
                        {
                            mode = org.omg.CORBA.ParameterMode.PARAM_OUT;
                        }
                        else if (value.flags() == org.omg.CORBA.ARG_INOUT.value)
                        {
                            mode = org.omg.CORBA.ParameterMode.PARAM_INOUT;
                        }

                        params[i] = new org.omg.Dynamic.Parameter(value.value(), mode);
                    }
                    catch (Exception e)
                    {
                        logger.info("Caught exception ", e);
                    }
                }

                info.setArguments (params);

                ServerInterceptorIterator intercept_iter =
                    orb.getInterceptorManager().getServerIterator();

                try
                {
                    intercept_iter.iterate(info, ServerInterceptorIterator.RECEIVE_REQUEST);
                }
                catch (ForwardRequest e)
                {
                    setLocationForward(new org.omg.PortableServer.ForwardRequest(e.forward));
                }
                catch(org.omg.CORBA.UserException e)
                {
                    logger.error("uncaught userexception", e);
                }
                catch (org.omg.CORBA.SystemException _sys_ex)
                {
                    setSystemException(_sys_ex);
                }

                info = null;
            }
        }
    }

    public void set_result(org.omg.CORBA.Any res)
    {
        if( isStreamBased )
        {
            throw new BAD_INV_ORDER("This ServerRequest is stream-based!");
        }
        result = res;
    }

    public void set_exception(org.omg.CORBA.Any exception)
    {
        if( isStreamBased )
        {
            throw new BAD_INV_ORDER("This ServerRequest is stream-based!");
        }
        this.exception = exception;
        replyStatus = ReplyStatusType_1_2._USER_EXCEPTION;
    }


    public void reply()
    {
        if( responseExpected() )
        {
            //shortcut for appligator
            if (usePreconstructedReply)
            {
                try
                {
                    connection.sendReply( out );
                }
                catch( Exception ioe )
                {
                    logger.info("Error replying to request!", ioe);
                }

                return;
            }

            if( logger.isDebugEnabled() )
            {
                logger.debug( "ServerRequest: reply to " + operation() );
            }

            try
            {
                if( out == null )
                {
                    out =
                        new ReplyOutputStream(requestId(),
                                              ReplyStatusType_1_2.from_int(replyStatus),
                                              inputStream.getGIOPMinor(),
                                              inputStream.isLocateRequest(),
                                              logger);
                }

                /*
                 * DSI-based servers set results and user exceptions
                 * using anys, so we have to treat this differently
                 */
                if( !isStreamBased )
                {
                    if( replyStatus == ReplyStatusType_1_2._USER_EXCEPTION )
                    {
                        exception.write_value( out );
                    }
                    else if( replyStatus == ReplyStatusType_1_2._NO_EXCEPTION )
                    {
                        if( result != null )
                        {
                            result.write_value( out );
                        }

                        if( argList != null )
                        {
                            for( Iterator e = argList.iterator();
                                 e.hasNext(); )
                            {
                                org.jacorb.orb.NamedValue namedValue =
                                    (org.jacorb.orb.NamedValue)e.next();

                                if( namedValue.flags() != org.omg.CORBA.ARG_IN.value )
                                {
                                    // in parameters are not returnd
                                    try
                                    {
                                        namedValue.send( out );
                                    }
                                    catch (Exception e1)
                                    {
                                        throw new org.omg.CORBA.MARSHAL("Couldn't return (in)out arg of type "
                                                                        + namedValue.value().type() + " in ServerRequest.");
                                    }
                                }
                            }
                        }
                    }
                }

                /*
                 * these two exceptions are set in the same way for
                 * both stream-based and DSI-based servers
                 */
                if( replyStatus == ReplyStatusType_1_2._LOCATION_FORWARD )
                {
                    out.write_Object( location_forward.forward_reference );
                }
                else if( replyStatus == ReplyStatusType_1_2._SYSTEM_EXCEPTION )
                {
                    org.jacorb.orb.SystemExceptionHelper.write( out, sys_ex );
                }

                /*
                 * everything is written to out by now, be it results
                 * or exceptions.
                 */

                connection.sendReply( out );
            }
            catch ( Exception ioe )
            {
                logger.info("Error replying to request!", ioe);
            }
        }
    }

    /* ResponseHandler */

    public org.omg.CORBA.portable.OutputStream createReply()
    {
        isStreamBased = true;

        if( out != null )
        {
            // The reply was already created.  This happens in oneway
            // operations using SyncScope SYNC_WITH_SERVER, and does
            // not do any harm.
            return out;
        }

        out =
            new ReplyOutputStream(orb,
                                  requestId(),
                                  ReplyStatusType_1_2.NO_EXCEPTION,
                                  inputStream.getGIOPMinor(),
                                  inputStream.isLocateRequest(),
                                  logger );

        out.updateMutatorConnection(connection);

        out.configure(orb.getConfiguration());

        return out;
    }

    public org.omg.CORBA.portable.OutputStream createExceptionReply()
    {
        isStreamBased = true;

        replyStatus = ReplyStatusType_1_2._USER_EXCEPTION;

        out =
            new ReplyOutputStream(requestId(),
                                  ReplyStatusType_1_2.USER_EXCEPTION,
                                  inputStream.getGIOPMinor(),
                                  inputStream.isLocateRequest(),
                                  logger );

        return out;
    }

    /** our own: */

    public void setSystemException(org.omg.CORBA.SystemException exception)
    {
        replyStatus = ReplyStatusType_1_2._SYSTEM_EXCEPTION;

        /* we need to create a new output stream here because a system exception may
           have occurred *after* a no_exception request header was written onto the
           original output stream*/


        out = new ReplyOutputStream(requestId(),
                                    ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                    inputStream.getGIOPMinor(),
                                    inputStream.isLocateRequest(),
                                    logger);

        String msg = exception.getMessage();
        if (msg != null)
        {
            out.addServiceContext (createExceptionDetailMessage (msg));
        }

        sys_ex = exception;
    }

    /**
     * Creates a ServiceContext for transmitting an exception detail message,
     * as per section 1.15.2 of the Java Mapping.
     */
    private ServiceContext createExceptionDetailMessage (String message)
    {
        final CDROutputStream out = new CDROutputStream(orb);

        try
        {
            out.beginEncapsulatedArray();
            out.write_wstring(message);
            return new ServiceContext(org.omg.IOP.ExceptionDetailMessage.value,
                    out.getBufferCopy());
        }
        finally
        {
            out.close();
        }
    }

    public void setLocationForward(org.omg.PortableServer.ForwardRequest request)
    {
        replyStatus = ReplyStatusType_1_2._LOCATION_FORWARD;

        out = new ReplyOutputStream(requestId(),
                                    ReplyStatusType_1_2.LOCATION_FORWARD,
                                    inputStream.getGIOPMinor(),
                                    inputStream.isLocateRequest(),
                                    logger );
        location_forward = request;
    }

    /**
     * @return the InputStream. This operation sets the
     * request be stream-based, ie. all attempts to extract
     * data using DII-based operations will throw exceptions
     * For internal access to the stream use get_in()
     *
     */

    public org.jacorb.orb.CDRInputStream getInputStream()
    {
        isStreamBased = true;
        return inputStream;
    }

    public ReplyOutputStream getReplyOutputStream()
    {
        if (out == null)
        {
            createReply();
        }

        isStreamBased = true;
        return out;
    }

    public boolean responseExpected()
    {
        return Messages.responseExpected(inputStream.req_hdr.response_flags);
    }

    /**
     * Returns the SyncScope of this request, as expressed in the
     * header's response_flags.  Note that here, on the server side,
     * this no longer differentiates between SYNC_NONE and SYNC_WITH_TRANSPORT.
     * The former is returned in both cases.
     */
    public short syncScope()
    {
        final short result;

        switch (inputStream.req_hdr.response_flags)
        {
            case 0x00:
                result = org.omg.Messaging.SYNC_NONE.value;
                break;
            case 0x01:
                result = org.omg.Messaging.SYNC_WITH_SERVER.value;
                break;
            case 0x03:
                result = org.omg.Messaging.SYNC_WITH_TARGET.value;
                break;
            default:
                throw new BAD_PARAM("Illegal SYNC_SCOPE: " + inputStream.req_hdr.response_flags);
        }

        return result;
    }

    public org.omg.CORBA.SystemException getSystemException()
    {
        return sys_ex;
    }

    public int requestId()
    {
        return inputStream.req_hdr.request_id;
    }

    public byte[] objectKey()
    {
        return object_key; // NOPMD
    }

    /**
     * <code>getScopes</code> returns the cached list of poa_names.
     *
     * @return a <code>List</code> value containing Strings separated by
     * {@link org.jacorb.poa.POAConstants#OBJECT_KEY_SEPARATOR OBJECT_KEY_SEPARATOR}
     */
    public List getScopes ()
    {
        if (scopes == null || !cachePoaNames )
        {
            scopes = POAUtil.extractScopedPOANames
                (POAUtil.extractPOAName (object_key));
        }
        return scopes;
    }


    public org.omg.IOP.ServiceContext[] getServiceContext()
    {
        return inputStream.req_hdr.service_context;
    }

    public byte[] objectId()
    {
        return oid; // NOPMD
    }

    public boolean streamBased()
    {
        return isStreamBased;
    }


    public void setReference(org.omg.CORBA.Object obj)
    {
        reference = obj;
    }

    public org.omg.CORBA.Object getReference()
    {
        return reference;
    }

    public RequestInputStream get_in()
    {
        return inputStream;
    }

    /**
     * If a new output stream has to be created, the request itself isn't fixed
     * to stream-based.
     */
    public ReplyOutputStream get_out()
    {
        if (out == null)
        {
            out = new ReplyOutputStream(requestId(),
                                        status(),
                                        inputStream.getGIOPMinor(),
                                        inputStream.isLocateRequest(),
                                        logger );
        }

        return out;
    }

    public void setServerRequestInfo(ServerRequestInfoImpl info)
    {
        this.info = info;
    }

    public org.omg.CORBA.Object getForwardReference()
    {
        if (location_forward != null)
        {
            return location_forward.forward_reference;
        }
        return null;
    }

    public GIOPConnection getConnection()
    {
        return connection;
    }

    public void setUsePreconstructedReply(boolean use)
    {
        usePreconstructedReply = use;
    }

    /**
     * If this request has a service context with timing policies,
     * this method decodes them and puts them into the corresponding
     * instance variables (requestStartTime, requestEndTime, replyEndTime).
     */
    private void calcTimingPolicies()
    {
        ServiceContext ctx = inputStream.getServiceContext(INVOCATION_POLICIES.value);
        if (ctx != null)
        {
            final CDRInputStream input = new CDRInputStream (null, ctx.context_data);

            try
            {
                input.openEncapsulatedArray();
                PolicyValue[] policy = PolicyValueSeqHelper.read (input);
                for (int i=0; i < policy.length; i++)
                {
                    if (policy[i].ptype == REQUEST_START_TIME_POLICY_TYPE.value)
                    {
                        requestStartTime = Time.fromCDR (policy[i].pvalue);
                    }
                    else if (policy[i].ptype == REQUEST_END_TIME_POLICY_TYPE.value)
                    {
                        requestEndTime = Time.fromCDR (policy[i].pvalue);
                    }
                    else if (policy[i].ptype == REPLY_END_TIME_POLICY_TYPE.value)
                    {
                        replyEndTime = Time.fromCDR (policy[i].pvalue);
                    }
                }
            }
            finally
            {
                input.close();
            }
        }
    }

    /**
     * Returns the time after which a reply to this request may no longer
     * be obtained or returned to the client; null if no such time has
     * been specified.
     */
    public UtcT getReplyEndTime()
    {
        return replyEndTime;
    }

    /**
     * Returns the time after which this request may no longer be
     * delivered to its target; null if no such time has been specified.
     */
    public UtcT getRequestEndTime()
    {
        return requestEndTime;
    }

    /**
     * Returns the time after which this request may be delivered to
     * its target; null if no such time has been specified.
     */
    public UtcT getRequestStartTime()
    {
        return requestStartTime;
    }
}
