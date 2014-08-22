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
package org.jacorb.transaction;

import org.slf4j.Logger;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.ControlHelper;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * This interceptor transfers the propagation context
 * from the corresponding service context to a slot
 * in the PICurrent.
 *
 * @author Nicolas Noffke
 * @author Vladimir Mencl
 */
public class ServerContextTransferInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    private Codec codec = null;
    private int slot_id = -1;
    private org.omg.CosTransactions.Current ts_current;
    private org.omg.CORBA.ORB orb;
    private Logger logger;

    public ServerContextTransferInterceptor(Codec codec, 
                                            int slot_id, 
                                            org.omg.CosTransactions.Current ts_current, 
                                            org.omg.CORBA.ORB orb) 
    {
        this.codec = codec;
        this.slot_id = slot_id;
        this.ts_current = ts_current;
        this.orb = orb;
        this.logger =
            ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb.tx_service.interceptor");
    }

    // implementation of org.omg.PortableInterceptor.InterceptorOperations interface
    public String name() 
    {
        return "ServerContextTransferInterceptor";
    }

    public void destroy()
    {
    }

    /**
     * Put the propagation context from the service context
     * into the PICurrent.
     */
    public void receive_request_service_contexts(ServerRequestInfo ri) 
        throws ForwardRequest
    {
        try
        {
            ServiceContext ctx = ri.get_request_service_context(TransactionService.value);
            ri.set_slot(slot_id, codec.decode(ctx.context_data));
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception", e);
        }
    }

    public void receive_request(ServerRequestInfo ri)
        throws ForwardRequest
    {
        try
        {
            org.omg.PortableInterceptor.Current pi_current =
                (org.omg.PortableInterceptor.Current) 
		orb.resolve_initial_references("PICurrent");

            PropagationContext context = PropagationContextHelper.extract
                (pi_current.get_slot(slot_id));

            Control control = ControlHelper.extract(context.implementation_specific_data);
            ts_current.resume(control);
        }
        catch(Exception e)
        {
            if (logger.isDebugEnabled())
                logger.debug("Exception", e);
        }
    }

    public void send_reply(ServerRequestInfo ri)
    {
        ts_current.suspend();
    }

    public void send_exception(ServerRequestInfo ri)
        throws ForwardRequest
    {
        ts_current.suspend();
    }

    public void send_other(ServerRequestInfo ri) 
        throws ForwardRequest
    {
        ts_current.suspend();
    }
} // ServerContextTransferInterceptor
