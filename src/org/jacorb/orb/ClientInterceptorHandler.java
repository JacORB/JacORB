/*
 *        JacORB  - a free Java ORB
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

package org.jacorb.orb;

import org.jacorb.orb.giop.ReplyInputStream;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * @author Alphonse Bendt
 */
public interface ClientInterceptorHandler
{
    void handle_send_request() throws RemarshalException, ForwardRequest;

    void handle_location_forward(ReplyInputStream reply,
            org.omg.CORBA.Object forward_reference) throws RemarshalException, ForwardRequest;

    void handle_receive_reply(ReplyInputStream reply) throws RemarshalException, ForwardRequest;

    void handle_receive_other(short reply_status) throws RemarshalException, ForwardRequest;

    void handle_receive_exception(org.omg.CORBA.SystemException exception)
        throws RemarshalException, ForwardRequest;

    void handle_receive_exception(org.omg.CORBA.SystemException exception,
            ReplyInputStream reply) throws RemarshalException, ForwardRequest;

    void handle_receive_exception(ApplicationException exception,
            ReplyInputStream reply) throws RemarshalException, ForwardRequest;
}
