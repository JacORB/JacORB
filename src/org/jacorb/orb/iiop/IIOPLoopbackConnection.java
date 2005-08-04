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

package org.jacorb.orb.iiop;

import java.io.IOException;

import org.omg.CORBA.INITIALIZE;
import org.omg.ETF.Profile;

/**
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 */
public class IIOPLoopbackConnection
    extends org.jacorb.orb.etf.StreamConnectionBase
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
            throw to_COMM_FAILURE(ioe);
        }
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
}
