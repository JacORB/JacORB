/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.iiop;

import java.io.IOException;
import org.jacorb.orb.etf.StreamConnectionBase;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INITIALIZE;
import org.omg.ETF.Profile;

/**
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 */
public class IIOPLoopbackConnection
    extends StreamConnectionBase
{
    IIOPLoopbackConnection(final IIOPLoopbackInputStream lis,
                           final IIOPLoopbackOutputStream los)
    {
        try
        {
            in_stream = new IIOPLoopbackInputStream(los) ;
            out_stream = new IIOPLoopbackOutputStream(lis) ;
            connected = true ;
        }
        catch (final IOException ioe)
        {
            throw new INITIALIZE("Could not create loopback pipe connection");
        }
    }

    public void close()
    {
        try
        {
            if(in_stream != null)
            {
                in_stream.close();
            }

            if(out_stream != null)
            {
                out_stream.close();
            }
        }
        catch (final IOException ioe)
        {
            throw handleCommFailure(ioe);
        }
    }

    protected COMM_FAILURE handleCommFailure(IOException e)
    {
        return to_COMM_FAILURE(e);
    }

    protected void setTimeout(final int timeout)
    {
        // Can't handle timeout
    }

    protected int getTimeout()
    {
        // Can't handle timeout
        return 0;
    }

    public void connect(final Profile server_profile, final long time_out)
    {
        // Can't handle reconnect
    }

    public boolean isSSL()
    {
        return false;
    }

    /**
     * Returns a string describing this connection information. Only used by TransportListener.Event.toString. Could
     * be possibly be removed if JAC528 was done.
     * @return
     */
    public String getConnectionInfo()
    {
       return connection_info;
    }
}
