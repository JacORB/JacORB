/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2013 Gerald Brose / The JacORB Team.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jacorb.orb.Delegate;
import org.jacorb.orb.Delegate.INVOCATION_KEY;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.orb.giop.RequestOutputStream;
import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.ARG_INOUT;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ParameterMode;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TypeCode;
import org.omg.Dynamic.Parameter;
import org.omg.ETF.Profile;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.TimeBase.UtcT;
import org.slf4j.Logger;

/**
 * This class represents the type of info object,
 * that will be passed to the ClientRequestInterceptors. <br>
 * See PI Spec p.5-46ff
 *
 * @author Nicolas Noffke
 */

public class ClientRequestInfoImpl
    extends RequestInfoImpl
    implements ClientRequestInfo
{
    private final Logger logger;

    //from ClientRequestInfo
    private org.omg.CORBA.Object target = null;
    private final org.omg.CORBA.Object effective_target;
    private TaggedProfile effective_profile = null;

    private final TaggedComponent[] effective_components;
    private final Map<INVOCATION_KEY, UtcT> invocationContext;

    private final ClientConnection connection;

    protected final Any received_exception;
    protected Delegate delegate;
    protected RequestOutputStream request_os;
    protected String received_exception_id;
    protected ReplyInputStream reply_is;


    public ClientRequestInfoImpl
                      ( org.jacorb.orb.ORB orb,
                        ClientRequestInfoImpl original,
                        org.jacorb.orb.giop.RequestOutputStream ros,
                        org.omg.CORBA.Object self,
                        org.jacorb.orb.Delegate delegate,
                        org.jacorb.orb.ParsedIOR piorOriginal,
                        org.jacorb.orb.giop.ClientConnection connection,
                        Map<INVOCATION_KEY, UtcT> invocationContext)
    {
        super(orb);

        logger = orb.getConfiguration().getLogger("jacorb.orb.interceptors");

        this.operation = ros.operation();
        this.response_expected = ros.response_expected();
        this.received_exception = orb.create_any();

        sync_scope = ros.syncScope();

        if ( ros.getRequest() != null )
        {
            this.setRequest( ros.getRequest() );
        }

        this.effective_target = self;

        org.jacorb.orb.ParsedIOR pior = delegate.getParsedIOR();

        if ( piorOriginal == null )
        {
            this.target = self;
        }
        else
        {
            this.target = orb._getDelegate(piorOriginal);
        }

        Profile profile = pior.getEffectiveProfile();

        // If this ParsedIOR is using a profile that extends ProfileBase e.g. IIOPProfile
        // and WIOP (within the regression suite) then grab the effective profile and the
        // possibly null effective_components.
        if (profile instanceof ProfileBase)
        {
            this.effective_profile    = ((ProfileBase)profile).asTaggedProfile();
            this.effective_components =
                (
                        ((ProfileBase)profile).getComponents() == null ?
                                new org.omg.IOP.TaggedComponent[0]             :
                                    ((ProfileBase)profile).getComponents().asArray()
                );
        }
        else
        {
            this.effective_components = new org.omg.IOP.TaggedComponent[ 0 ];
        }

        this.delegate = delegate;

        this.request_id = ros.requestId();
        InterceptorManager manager = orb.getInterceptorManager();

        this.current = manager.getCurrent();

        //allow interceptors access to request output stream
        this.request_os = ros;

        //allow (BiDir) interceptor to inspect the connection
        this.connection = connection;

        // If the original ClientRequestInfo is not null and the forward_reference
        // is not null then copy it over.
        if (original != null && original.forward_reference != null)
        {
            forward_reference = original.forward_reference;
        }

        this.invocationContext = invocationContext;

        if (logger.isDebugEnabled())
        {
            logger.debug("created with invocationContext: " + invocationContext);
        }
    }

    /**
     * Constructor for local calls involving portable interceptors.  With
     * local calls there is no Request/streams so we need to set the data
     * directly
     */
    public ClientRequestInfoImpl ( org.jacorb.orb.ORB orb,
                                   String operation,
                                   boolean response_expected,
                                   short sync_scope,
                                   org.omg.CORBA.Object self,
                                   org.jacorb.orb.Delegate delegate,
                                   org.jacorb.orb.ParsedIOR piorOriginal,
                                   Map<INVOCATION_KEY, UtcT> invocationContext)
    {
        super(orb);

        logger = orb.getConfiguration().getLogger("jacorb.orb.interceptors");

        this.operation = operation;
        this.response_expected = response_expected;
        this.received_exception = orb.create_any();
        this.sync_scope = sync_scope;

        this.effective_target = self;

        org.jacorb.orb.ParsedIOR pior = delegate.getParsedIOR();

        if ( piorOriginal == null )
        {
            this.target = self;
        }
        else
        {
            this.target = orb._getDelegate (piorOriginal);
        }

        Profile profile = pior.getEffectiveProfile();

        // If this ParsedIOR is using a profile that extends ProfileBase e.g. IIOPProfile
        // and WIOP (within the regression suite) then grab the effective profile and the
        // possibly null effective_components.
        if (profile instanceof ProfileBase)
        {
            final ProfileBase profileBase = (ProfileBase)profile;

            this.effective_profile    = (profileBase).asTaggedProfile();
            this.effective_components =
                (
                        (profileBase).getComponents() == null ?
                                new org.omg.IOP.TaggedComponent[0]             :
                                    (profileBase).getComponents().asArray()
                );
        }
        else
        {
            this.effective_components = new org.omg.IOP.TaggedComponent[ 0 ];
            effective_profile = null;
        }

        this.delegate = delegate;
        this.invocationContext = invocationContext;

        InterceptorManager manager = orb.getInterceptorManager();

        this.current = manager.getCurrent();

        /* The following do not exist with a local call so nullify them for compilation
         * reasons because they are declared as final
         */
        connection = null;

    }

    public final void setRequest(org.jacorb.orb.dii.Request request)
    {
        arguments = new org.omg.Dynamic.Parameter[request.arguments.count()];
        for (int i = 0; i < arguments.length; i++)
        {
            try
            {
                NamedValue value = request.arguments.item(i);

                ParameterMode mode = null;
                if (value.flags() == ARG_IN.value)
                {
                    mode = ParameterMode.PARAM_IN;
                }
                else if (value.flags() == ARG_OUT.value)
                {
                    mode = ParameterMode.PARAM_OUT;
                }
                else if (value.flags() == ARG_INOUT.value)
                {
                    mode = ParameterMode.PARAM_INOUT;
                }

                arguments[i] = new org.omg.Dynamic.Parameter(value.value(), mode);
            }
            catch (Exception e)
            {
                logger.debug("unexpected exception", e);
            }
        }
        //exceptions will be set when available
    }


    // implementation                        of
    // org.omg.PortableInterceptor.RequestInfoOperations interface

    public Parameter[] arguments()
    {
        if (! (caller_op == ClientInterceptorIterator.SEND_REQUEST) &&
            ! (caller_op == ClientInterceptorIterator.RECEIVE_REPLY))
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
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The attribute \"exceptions\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        if (exceptions == null)
        {
            throw new NO_RESOURCES("Stream-based skeletons/stubs do not support this op",
                                   1, CompletionStatus.COMPLETED_MAYBE);
        }

        return exceptions;
    }

    public Any result()
    {
        if (caller_op != ClientInterceptorIterator.RECEIVE_REPLY)
        {
            throw new BAD_INV_ORDER("The attribute \"result\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
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
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The attribute \"sync_scope\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return sync_scope;
    }

    public short reply_status()
    {
        if ((caller_op == ClientInterceptorIterator.SEND_REQUEST) ||
            (caller_op == ClientInterceptorIterator.SEND_POLL))
        {
            throw new BAD_INV_ORDER("The attribute \"reply_status\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return reply_status;
    }

    /**
     * <code>forward_reference</code> returns the forward reference for the client request. Note
     * that the current version of the specification does not permit this to be accessed by
     * SendRequest; this modification is a PrismTech enhancement complying one of the suggested
     * portable solutions within http://www.omg.org/issues/issue5266.txt.
     *
     * @return an <code>org.omg.CORBA.Object</code> value
     */
    public org.omg.CORBA.Object forward_reference()
    {
        if (  caller_op != ClientInterceptorIterator.SEND_REQUEST &&
                (caller_op != ClientInterceptorIterator.RECEIVE_OTHER &&
                 (reply_status != LOCATION_FORWARD.value)) )
        {
            throw new BAD_INV_ORDER("The attribute \"forward_reference\" is currently " +
                                    "invalid!", 10, CompletionStatus.COMPLETED_MAYBE);
        }
        return forward_reference;
    }

    public ServiceContext get_request_service_context(int id)
    {
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The attribute \"operation_context\" is currently " +
                                    "invalid!", 10, CompletionStatus.COMPLETED_MAYBE);
        }

        return super.get_request_service_context(id);
    }

    public ServiceContext get_reply_service_context(int id)
    {
        if ((caller_op == ClientInterceptorIterator.SEND_REQUEST) ||
            (caller_op == ClientInterceptorIterator.SEND_POLL))
        {
            throw new BAD_INV_ORDER("The attribute \"reply_status\" is currently invalid!",
                                    10, CompletionStatus.COMPLETED_MAYBE);
        }

        return super.get_reply_service_context(id);
    }

    // implementation of ClientRequestInfoOperations interface
    public org.omg.CORBA.Object target()
    {
        return target;
    }

    public org.omg.CORBA.Object effective_target()
    {
        return effective_target;
    }

    public TaggedProfile effective_profile() {
        return effective_profile;
    }

    public Any received_exception()
    {
        if (caller_op != ClientInterceptorIterator.RECEIVE_EXCEPTION)
        {
            throw new BAD_INV_ORDER("The attribute \"received_exception\" is currently " +
                                    "invalid!", 10, CompletionStatus.COMPLETED_MAYBE);
        }

        return received_exception;
    }

    public String received_exception_id()
    {
        if (caller_op != ClientInterceptorIterator.RECEIVE_EXCEPTION)
        {
            throw new BAD_INV_ORDER("The attribute \"received_exception_id\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        return received_exception_id;
    }

    public TaggedComponent get_effective_component(int id)
    {
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The operation \"get_effective_component\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        for(int _i = 0; _i < effective_components.length; _i++)
        {
            if (effective_components[_i].tag == id)
            {
                return effective_components[_i];
            }
        }

        throw new BAD_PARAM("No TaggedComponent with id " + id + " found",
                            25, CompletionStatus.COMPLETED_MAYBE);
    }

    public TaggedComponent[] get_effective_components(int id)
    {
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The operation \"get_effective_components\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        List<TaggedComponent> _store = new ArrayList<TaggedComponent>();
        for(int _i = 0; _i < effective_components.length; _i++)
        {
            if (effective_components[_i].tag == id)
            {
                _store.add(effective_components[_i]);
            }
        }

        if (_store.size() == 0)
        {
            throw new BAD_PARAM("No TaggedComponents with id " + id + " found",
                                25, CompletionStatus.COMPLETED_MAYBE);
        }

        TaggedComponent[] _result = new TaggedComponent[_store.size()];
        for (int _i = 0; _i < _result.length; _i++)
        {
                _result[_i] = (TaggedComponent) _store.get(_i);
        }

        return _result;
    }

    /**
     * WARNING: This method relies on the DomainService to be available.
     * Make sure that the DS is running, if you want to call this method.
     */

    public Policy get_request_policy(int type)
    {
        if (caller_op == ClientInterceptorIterator.SEND_POLL)
        {
            throw new BAD_INV_ORDER("The operation \"get_request_policy\" is currently " +
                                    "invalid!", 10, CompletionStatus.COMPLETED_MAYBE);
        }

        if (!orb.hasPolicyFactoryForType(type))
        {
            throw new INV_POLICY("No PolicyFactory for type " + type +
                                 " has been registered!", 1,
                                 CompletionStatus.COMPLETED_MAYBE);
        }

        try
        {
            return delegate.get_policy (target, type);
        }
        catch(INV_POLICY e)
        {
            e.minor = 1;
            throw e;
        }
    }

    public void add_request_service_context(ServiceContext service_context,
                                            boolean replace)
    {

        if (caller_op != ClientInterceptorIterator.SEND_REQUEST)
        {
            throw new BAD_INV_ORDER("The operation \"add_request_service_context\" is " +
                                    "currently invalid!", 10,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        Integer _id = Integer.valueOf(service_context.context_id);

        if (! replace && request_ctx.containsKey(_id))
        {
            throw new BAD_INV_ORDER("The ServiceContext with id " + _id.toString()
                                    + " has already been set!", 11,
                                    CompletionStatus.COMPLETED_MAYBE);
        }

        request_ctx.put(_id, service_context);
    }


	/**
	 * Public accessor to return the connection being used by this ClientRequestInfoImpl.
	 * 
	 * @return the connection
	 */
	public ClientConnection getConnection() 
	{
		return connection;
	}
	
    // These functions should NOT be used internally and are only provided
    // to allow users to interrogate the streams.
    /**
     * Public accessor to access the internal reply stream. Using this API it
     * may be possible to corrupt the inputstream and therebye the call
     * chain. Use at your own risk.
     */
    public ReplyInputStream getReplyStream ()
    {
        return reply_is;
    }

    /**
     * Public accessor to access the internal request stream. Using this API it
     * may be possible to corrupt the outputstream and thereby the call
     * chain. Use at your own risk.
     */
    public RequestOutputStream getRequestStream ()
    {
        return request_os;
    }
} // ClientRequestInfoImpl
