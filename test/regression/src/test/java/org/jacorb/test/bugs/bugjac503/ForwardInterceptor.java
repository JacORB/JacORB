/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.bugs.bugjac503;

import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.RTCORBA.ClientProtocolPolicy;
import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.RTORB;

/**
 * @author Alphonse Bendt
 */
public class ForwardInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    public static Protocol[] protocols;

    private RTORB orb;

    public ForwardInterceptor(RTORB orb)
    {
        this.orb = orb;
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest
    {
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void send_request(ClientRequestInfo ri) throws ForwardRequest
    {
        if (protocols != null)
        {
            org.omg.CORBA.Object target = ri.effective_target ();

            ClientProtocolPolicy cpp = orb.create_client_protocol_policy(protocols);

            Policy[] policies = new Policy[] {cpp};

            org.omg.CORBA.Object rebindTo = target._set_policy_override (policies, SetOverrideType.SET_OVERRIDE);

            protocols = null;

            throw new ForwardRequest ("Switch to other protocol", rebindTo);
        }
    }

    public void destroy()
    {
    }

    public String name()
    {
        return "Test-ForwardInterceptor";
    }
}
