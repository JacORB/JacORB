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
import java.io.OutputStream;

/**
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 */
class IIOPLoopbackOutputStream extends OutputStream
{
    private IIOPLoopbackInputStream lis ;
    
    IIOPLoopbackOutputStream()
    {
    }
    
    IIOPLoopbackOutputStream(final IIOPLoopbackInputStream lis)
        throws IOException
    {
        lis.connect(this) ;
    }
    
    public synchronized void write(final byte[] b, final int off, final int len)
        throws IOException
    {
        checkConnect() ;
        lis.writeIntoBuffer(b, off, len) ;
    }
    
    public synchronized void write(final int b)
        throws IOException
    {
        checkConnect() ;
        lis.writeIntoBuffer(b) ;
    }
    
    public synchronized void close()
        throws IOException
    {
        checkConnect() ;
        lis.writerClose() ;
    }
    
    synchronized void connect(final IIOPLoopbackInputStream lis)
        throws IOException
    {
        this.lis = lis ;
    }
    
    private void checkConnect()
        throws IOException
    {
        if (lis == null)
        {
            throw new IOException("IIOPLoopbackOutputStream not connected") ;
        }
    }
}
