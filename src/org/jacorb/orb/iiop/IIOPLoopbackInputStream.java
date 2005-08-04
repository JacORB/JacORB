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
import java.io.InputStream;

/**
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 */
class IIOPLoopbackInputStream extends InputStream
{
    private static final int BUF_SIZE = 2048 ;
    
    private final byte[] buf = new byte[BUF_SIZE] ;
    
    private boolean connected ;
    private boolean closed ;
    private int writerIndex ;
    private int readerIndex ;
    
    IIOPLoopbackInputStream()
    {
    }
    
    IIOPLoopbackInputStream(final IIOPLoopbackOutputStream los)
        throws IOException
    {
        connect(los) ;
    }
    
    synchronized void connect(final IIOPLoopbackOutputStream los)
        throws IOException
    {
        if (connected)
        {
            throw new IOException("Alread connected") ;
        }
        connected = true ;
        los.connect(this) ;
    }
    
    public synchronized int read()
        throws IOException
    {
        checkConnect() ;
        return internalRead() ;
    }

    public synchronized int read(final byte[] b, final int off, final int len)
        throws IOException
    {
        checkConnect() ;
        checkBuffer(b, off, len) ;
        
        int returnCount = 0 ;
        
        while(returnCount < len)
        {
            final int val = internalRead() ;
            if (val == -1)
            {
                break ;
            }
            b[off + returnCount] = (byte)val ;
            returnCount++ ;
        }
        
        return (returnCount > 0 ? returnCount : -1) ;
    }

    public synchronized int available()
        throws IOException
    {
        checkConnect() ;
        
        if (writerIndex > readerIndex)
        {
            return (writerIndex - readerIndex) ;
        }
        else if (readerIndex > writerIndex)
        {
            return BUF_SIZE + writerIndex - readerIndex ;
        }
        else
        {
            checkClosed() ;
            return 0 ;
        }
    }

    public synchronized void close()
        throws IOException
    {
        checkConnect() ;
        closed = true ;
        notifyAll() ;
    }

    synchronized void writerClose()
        throws IOException
    {
        checkConnect() ;
        closed = true ;
        notifyAll() ;
    }
    
    synchronized void writeIntoBuffer(final int b)
        throws IOException
    {
        checkConnect() ;
        internalWrite((byte)b) ;
    }
    
    synchronized void writeIntoBuffer(final byte[] b, final int off, final int len)
        throws IOException
    {
        checkConnect() ;
        checkBuffer(b, off, len) ;
        
        for(int count= 0 ; count < len ; count++)
        {
            internalWrite(b[off + count]) ;
        }
    }
    
    private int internalRead()
    {
        while(bufferEmpty())
        {
            if (closed)
            {
                return -1 ;
            }
            try
            {
                wait() ;
            }
            catch (final InterruptedException ie) {} // Ignore
        }
        
        final boolean shouldNotify = bufferFull() ;
        
        final int val = buf[readerIndex] & 0xff ;
        readerIndex = nextIndex(readerIndex) ;
        
        if (shouldNotify)
        {
            notifyAll() ;
        }
        
        return val ;
    }
    
    private void internalWrite(final byte b)
        throws IOException
    {
        while(bufferFull())
        {
            checkClosed() ;
            try
            {
                wait() ;
            }
            catch (final InterruptedException ie) {} // Ignore
        }
        
        checkClosed() ;
        
        final boolean shouldNotify = bufferEmpty() ;
        
        buf[writerIndex] = b ;
        writerIndex = nextIndex(writerIndex) ;
        
        if (shouldNotify)
        {
            notifyAll() ;
        }
    }
    
    private void checkConnect()
        throws IOException
    {
        if (!connected)
        {
            throw new IOException("IIOPLoopbackInputStream not connected") ;
        }
    }
    
    private void checkClosed()
        throws IOException
    {
        if (closed)
        {
            throw new IOException("IIOPLoopbackInputStream closed") ;
        }
    }
    
    private void checkBuffer(final byte[] b, final int off, final int len)
    {
        if (b == null)
        {
            throw new NullPointerException("Null buffer") ;
        }
        
        final int bufLen = b.length ;
        if ((off < 0) || (off >= bufLen) || (len < 0) || (len > bufLen) || (off + len > bufLen))
        {
            throw new IndexOutOfBoundsException("Invalid offset/length") ;
        }
    }
    
    private boolean bufferEmpty()
    {
        return (readerIndex == writerIndex) ;
    }
    
    private boolean bufferFull()
    {
        return (readerIndex == nextIndex(writerIndex)) ;
    }
    
    private int nextIndex(final int index)
    {
        final int nextIndex = index+1 ;
        return (nextIndex == BUF_SIZE ? 0 : nextIndex) ;
    }
}
