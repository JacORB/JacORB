/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
package org.jacorb.transaction;


import org.slf4j.Logger;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * This interceptor adds a service context with
 * the transactions propagation context to the
 * outgoing message.
 *
 * @author Nicolas Noffke
 */

public class ClientContextTransferInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{

    private int slot_id = -1;
    private Codec codec = null;
    private Logger logger = null;

    public ClientContextTransferInterceptor (Logger logger, 
                                             int slot_id, 
                                             Codec codec) 
    {
        this.logger = logger;
        this.slot_id = slot_id;
        this.codec = codec;
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface

    public String name() 
    {
        return "ClientContextTransferInterceptor";
    }

    public void destroy()
    {
    }

    /**
     * Add the propagation context to the outgoing message
     */

    public void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        try
        {
            org.omg.CORBA.Any any = ri.get_slot(slot_id);
      
            if (! (any.type().kind().value() == org.omg.CORBA.TCKind._tk_null))
            {
                ServiceContext ctx = new ServiceContext(TransactionService.value,
                                                        codec.encode(any));
                if (logger.isDebugEnabled())
                    logger.debug("adding Transaction Service Context"
                                 + " to outgoing request");
                ri.add_request_service_context(ctx, false);
            }
        }
        catch (Exception e)
        {

        }
    }

    public void send_poll(ClientRequestInfo ri){
    }

    public void receive_reply(ClientRequestInfo ri){
    }

    public void receive_exception(ClientRequestInfo ri) 
        throws ForwardRequest{
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest{
    }
} // ClientContextTransferInterceptor






