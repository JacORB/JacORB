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
package org.jacorb.orb;

import java.util.Enumeration;

import org.apache.avalon.framework.logger.*;

import org.jacorb.orb.giop.*;
import org.jacorb.orb.portableInterceptor.*;

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.IOP.ServiceContext;
import org.omg.GIOP.ReplyHeader_1_2;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.PortableInterceptor.*;

/**
 * An instance of this class handles all interactions between one particular
 * client request and any interceptors registered for it.
 *
 * @author Andre Spiegel
 * @version $Id$
 */
public class ClientInterceptorHandler
{
    private ClientRequestInfoImpl info = null;
    private Logger logger;

    /**
     * Constructs an interceptor handler for the given parameters.
     * If no interceptors are registered on the client side,
     * the resulting object will be a dummy object that does nothing when
     * invoked.
     */
    public ClientInterceptorHandler
                      ( org.jacorb.orb.ORB orb,
                        org.jacorb.orb.giop.RequestOutputStream ros,
                        org.omg.CORBA.Object self,
                        org.jacorb.orb.Delegate delegate,
                        org.jacorb.orb.ParsedIOR piorOriginal,
                        org.jacorb.orb.giop.ClientConnection connection )
    {
        if ( orb.hasClientRequestInterceptors() )
        {
            info = new ClientRequestInfoImpl ( orb, ros, self, delegate,
                                               piorOriginal, connection );
        }
        logger =
            orb.getConfiguration().getNamedLogger("jacorb.orb.client_interceptors");
    }

    public void handle_send_request() throws RemarshalException
    {
        if ( info != null )
        {
            invokeInterceptors ( info, ClientInterceptorIterator.SEND_REQUEST );

            // Add any new service contexts to the message
            Enumeration ctx = info.getRequestServiceContexts();

            while ( ctx.hasMoreElements() )
            {
                info.request_os.addServiceContext
                                       ( ( ServiceContext ) ctx.nextElement() );
            }
        }
    }

    public void handle_location_forward ( ReplyInputStream     reply,
                                          org.omg.CORBA.Object forward_reference )
        throws RemarshalException
    {
        if ( info != null )
        {
            info.setReplyStatus (LOCATION_FORWARD.value);
            info.setReplyServiceContexts( reply.rep_hdr.service_context );

            info.setForwardReference (forward_reference);

            //allow interceptors access to reply input stream
            info.reply_is = reply;

            invokeInterceptors( info,
                                ClientInterceptorIterator.RECEIVE_OTHER );
        }
    }

    public void handle_receive_reply ( ReplyInputStream reply )
        throws RemarshalException
    {
        if ( info != null )
        {
            ReplyHeader_1_2 header = reply.rep_hdr;

            if ( header.reply_status.value() == ReplyStatusType_1_2._NO_EXCEPTION )
            {
                info.setReplyStatus (SUCCESSFUL.value);

                info.setReplyServiceContexts( header.service_context );

                // the case that invoke was called from
                // dii.Request._invoke() will be handled inside
                // of dii.Request._invoke() itself, because the
                // result will first be available there
                if ( info.request_os.getRequest() == null )
                {
                    InterceptorManager manager = info.orb.getInterceptorManager();
                    info.setCurrent (manager.getCurrent());

                    //allow interceptors access to reply input stream
                    info.reply_is = reply;

                    invokeInterceptors( info,
                                        ClientInterceptorIterator.RECEIVE_REPLY );
                }
                else
                    info.request_os.getRequest().setInfo( info );
            }
        }
    }

    public void handle_receive_other ( short reply_status )
        throws RemarshalException
    {
        if ( info != null )
        {
            info.setReplyStatus (reply_status);
            invokeInterceptors ( info, ClientInterceptorIterator.RECEIVE_OTHER );
        }
    }

    public void handle_receive_exception ( org.omg.CORBA.SystemException ex )
        throws RemarshalException
    {
        handle_receive_exception ( ex, null );
    }

    public void handle_receive_exception ( org.omg.CORBA.SystemException ex,
                                           ReplyInputStream reply )
        throws RemarshalException
    {
        if ( info != null )
        {
            SystemExceptionHelper.insert ( info.received_exception, ex );
            try
            {
                info.received_exception_id =
                    SystemExceptionHelper.type ( ex ).id();
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
            {
                if (logger.isDebugEnabled())
                    logger.debug("BadKind: " + bk.getMessage());
            }
            info.setReplyStatus (SYSTEM_EXCEPTION.value);

            if ( reply != null )
            {
                info.setReplyServiceContexts ( reply.rep_hdr.service_context );
                info.reply_is = reply;
            }

            invokeInterceptors ( info,
                                 ClientInterceptorIterator.RECEIVE_EXCEPTION );
        }
    }

    public void handle_receive_exception ( ApplicationException ex,
                                           ReplyInputStream reply )
        throws RemarshalException
    {
        if ( info != null )
        {
            info.received_exception_id = ex.getId();
            try
            {
                ApplicationExceptionHelper.insert( info.received_exception,
                                                   ex );
            }
            catch ( Exception e )
            {
                if (logger.isDebugEnabled())
                    logger.debug(e.getMessage());
                SystemExceptionHelper.insert ( info.received_exception,
                                               new org.omg.CORBA.UNKNOWN
                                                  ( e.getMessage() ) );
            }
            info.setReplyStatus (USER_EXCEPTION.value);

            try
            {
                reply.reset();
            }
            catch ( Exception e )
            {
                // shouldn't happen anyway
                if (logger.isWarnEnabled())
                    logger.warn(e.getMessage());
            }

            info.setReplyServiceContexts ( reply.rep_hdr.service_context );
            info.reply_is = reply;

            invokeInterceptors ( info,
                                 ClientInterceptorIterator.RECEIVE_EXCEPTION );
        }
    }

    private void invokeInterceptors( ClientRequestInfoImpl info, short op )
      throws RemarshalException
    {
        ClientInterceptorIterator intercept_iter =
            info.orb.getInterceptorManager().getClientIterator();

        try
        {
            intercept_iter.iterate( info, op );
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest fwd )
        {
            // This allows SendRequest to access the forwarded object.
            //
            // Note that the current version of the specification does not
            // permit forward_reference to be accessed by SendRequest; this
            // modification is a PrismTech enhancement complying one of the
            // suggested portable solutions within
            // http://www.omg.org/issues/issue5266.txt.
            info.setForwardReference(fwd.forward);

            info.delegate.rebind(fwd.forward );

            throw new RemarshalException();
        }
        catch ( org.omg.CORBA.UserException ue )
        {
            if (logger.isWarnEnabled())
                logger.warn("UserException: " + ue.getMessage());
        }
    }


}
