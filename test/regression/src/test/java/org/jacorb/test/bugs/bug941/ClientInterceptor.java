package org.jacorb.test.bugs.bug941;

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

import static org.junit.Assert.assertTrue;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.giop.RequestOutputStream;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * @author Nick Cross
 */

public class ClientInterceptor extends org.omg.CORBA.LocalObject implements ClientRequestInterceptor
{
    public ClientInterceptor()
    {
    }

    public String name()
    {
        return "ClientInterceptor941";
    }

    public void destroy()
    {
    }

    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        final RequestOutputStream output = ((ClientRequestInfoImpl) ri).getRequestStream ();

        if (output != null && output.getBodyBegin() > 0)
        {
            int debut = output.getBodyBegin();
            final int fin = output.size();
            int resteALire = fin - debut;

            if (resteALire > 0)
            {
                final byte[] buf = output.getBufferCopy();
                final int pos = output.get_pos();

                // skip header
                output.skip(debut - pos);
                // rewrite remaining
                output.write_octet_array(buf, debut, resteALire);
            }
        }

        // This will fail to compile if public accessor API has been accidentally
        // removed (!)
        Delegate d = ((ClientRequestInfoImpl) ri).getDelegate();
        assertTrue (d != null);
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
        throws ForwardRequest
    {
    }

    public void receive_other(ClientRequestInfo ri)
        throws ForwardRequest
    {
    }
}
