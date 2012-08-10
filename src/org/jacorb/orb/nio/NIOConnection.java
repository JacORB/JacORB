/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2011-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.nio;

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

import org.jacorb.orb.etf.StreamConnectionBase;
import org.omg.CORBA.COMM_FAILURE;
import org.jacorb.util.SelectorRequest;
import org.jacorb.util.SelectorRequestCallback;
import org.jacorb.util.SelectorManager;
import org.jacorb.config.*;
import org.omg.CORBA.TIMEOUT;

/**
 * @author Ciju John
 */
public abstract class NIOConnection
        extends StreamConnectionBase
{
    private int timeout;
    protected SocketChannel channel = null;
    protected SelectorManager selectorManager = null;
    private int maxConsecutiveTimeouts = 0;
    protected int failedWriteAttempts = 0;
    protected boolean isDebugEnabled = false;

    public void configure(Configuration config)
    throws ConfigurationException
    {

        super.configure (config);

        isDebugEnabled = logger.isDebugEnabled();

        selectorManager = orb.getSelectorManager ();

        maxConsecutiveTimeouts = configuration.getAttributeAsInteger("jacorb.nio.maxConsecutiveTimeouts", 0);

        try
        {
            channel = SocketChannel.open ();
        }
        catch (Exception ex)
        {
            logger.error ("Unable to initialize channel: " + ex.toString());
            // can't do much more
        }
    }

    // /* no SSL support yet */
    public boolean isSSL()
    {
        return false;
    }

    @Override // ConnectionBase
    public synchronized boolean is_connected()
    {
        if (isDebugEnabled)
        {
            logger.debug (this.toString() + ".is_connected()");
        }

        return connected && channel.isConnected();
    }

    protected synchronized void setConnected (boolean connected)
    {
        this.connected = connected;
    }

    @Override // ConnectionBase
    protected COMM_FAILURE handleCommFailure(IOException e)
    {
        return to_COMM_FAILURE(e);
    }

    @Override // ConnectionBase
    protected void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    @Override // ConnectionBase
    protected int getTimeout()
    {
        return timeout;
    }

    //boolean is_connected();

    // time_out is probably in milli seconds
    @Override
    public int read (org.omg.ETF.BufferHolder data,
                     int offset,
                     int min_length,
                     int max_length,
                     long time_out)
    {

        SocketChannel myChannel;
        synchronized (this)
        {
            myChannel = channel;
        }

        if (!myChannel.isConnected())
        {
            throw new org.omg.CORBA.COMM_FAILURE ("read() did not return any data");
        }

        long nanoDeadline = (time_out == 0 ? Long.MAX_VALUE : System.nanoTime() + time_out * 1000000);
        ReadCallback callback = new ReadCallback (data, offset, min_length, max_length);
        SelectorRequest request = new SelectorRequest (SelectorRequest.Type.READ, myChannel,
                callback, nanoDeadline);

        if (!selectorManager.add (request))
        {
            if (request.status == SelectorRequest.Status.EXPIRED)
            {
                throw new TIMEOUT("Message expired before write attempt.");
            }
            else
            {
                throw handleCommFailure(new IOException("Unable to add read request to SelectorManager"));
            }
        }
        request.waitOnCompletion (nanoDeadline);

        if (request.status == SelectorRequest.Status.EXPIRED || !request.isFinalized())
        {
            throw new TIMEOUT("Message expired before write attempt.");
        }
        else if (request.status == SelectorRequest.Status.FAILED)
        {
            throw new org.omg.CORBA.COMM_FAILURE ("Read request failed. Request status: FAILED");
        }
        else if (request.status == SelectorRequest.Status.SHUTDOWN)
        {
            throw new org.omg.CORBA.TRANSIENT ("Read request failed. Request status: SHUTDOWN");
        }

        return callback.readLength;
    }

    // time_out is probably in milli seconds
    @Override
    public void write (boolean is_first,
                       boolean is_last,
                       byte[] data,
                       int offset,
                       int length,
                       long time_out)
    {

        SocketChannel myChannel;
        synchronized (this)
        {
            myChannel = channel;
        }

        if (!myChannel.isConnected())
        {
            throw handleCommFailure(new IOException("Channel has been closed"));
        }

        long nanoDeadline = (time_out == 0 ? Long.MAX_VALUE : System.nanoTime() + time_out * 1000000);
        WriteCallback writeCallback = new WriteCallback (data, offset, length);
        SelectorRequest request = new SelectorRequest (SelectorRequest.Type.WRITE, myChannel,
                writeCallback, nanoDeadline);

        if (!selectorManager.add (request))
        {
            if (request.status == SelectorRequest.Status.EXPIRED)
            {
                throw new TIMEOUT("Message expired before write attempt.");
            }
            else
            {
                throw handleCommFailure(new IOException("Unable to add write request to SelectorManager"));
            }
        }
        request.waitOnCompletion (nanoDeadline);

        if (!writeCallback.writeFinished())
        {

            int failCount = 0;
            synchronized (this)
            {
                failCount = ++failedWriteAttempts;
            }

            if (failCount >= maxConsecutiveTimeouts)
            {

                boolean isConnected;
                isConnected = myChannel.isConnected();
                try
                {
                    myChannel.close();
                }
                catch (IOException ex)
                {
                    // disregard
                    logger.debug ("Exception while trying to close channel after write failure. "
                                  + ex.getMessage());
                }

                if (isDebugEnabled)
                {
                    logger.debug ("Write attempts exceeded maximum allowed attempts (" + maxConsecutiveTimeouts +
                                  "). " + (isConnected ? "Closing channel." : "Channel already closed."));
                }

                return;
            }

            throw new TIMEOUT("Message expired before write attempt.");

        }
        else
        {
            synchronized (this)
            {
                failedWriteAttempts = 0;
            }
        }

    }

    public void flush()
    {
        // no op
    }

    private class ReadCallback extends SelectorRequestCallback
    {

        private final ByteBuffer byteBuffer;
        private final org.omg.ETF.BufferHolder data;
        private final int offset;
        private final int min_length;
        public int readLength = 0;

        public ReadCallback (org.omg.ETF.BufferHolder data, int offset, int min_length, int max_length)
        {
            super ();

            byteBuffer = ByteBuffer.allocate (max_length);
            byteBuffer.clear ();

            this.data = data;
            this.offset = offset;
            this.min_length = min_length;
        }

        public boolean call (SelectorRequest request)
        {

            SocketChannel myChannel = request.channel;
            try
            {
                if (request.status == SelectorRequest.Status.READY)
                {
                    int numRead = myChannel.read (byteBuffer);
                    if (numRead < 0)
                    {
                        // Remote entity shut the socket down cleanly. Do the
                        // same from our end and cancel the channel.
                        myChannel.close();

                        if (isDebugEnabled)
                        {
                            logger.debug("Transport to " + connection_info +
                                         ": stream closed on read  < 0" );
                        }
                    }
                    else
                    {
                        if (byteBuffer.position() < min_length)
                        {
                            // need more data, reactivate channel by returning true
                            return true;
                        }
                        else
                        {
                            readLength = byteBuffer.position();
                            byteBuffer.rewind ();
                            byteBuffer.get (data.value, offset, readLength);
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                try
                {
                    myChannel.close();
                }
                catch (IOException ex2)
                {
                    logger.error ("Failed to close channel: " + ex2.toString());
                }

                if (isDebugEnabled)
                {
                    logger.debug("Got IOException in read(). Transport to " + connection_info +
                                 ": stream closed: " + ex.toString());
                }
            }

            return false;
        }

    }

    private class WriteCallback extends SelectorRequestCallback
    {

        final ByteBuffer byteBuffer;
        final int length;
        private int writeCount = 0;

        public synchronized boolean writeFinished ()
        {
            return writeCount == length;
        }

        public WriteCallback (byte[] data, int offset, int length)
        {
            super ();

            this.length = length;

            // allocate a bytebuffer with the input data size
            byteBuffer = ByteBuffer.allocate (length);
            byteBuffer.clear ();

            // copy bytes into ByteBuffer
            byteBuffer.put (data, offset, length);
            byteBuffer.flip ();
        }

        public boolean call (SelectorRequest request)
        {

            SocketChannel myChannel = request.channel;

            try
            {
                if (request.status == SelectorRequest.Status.READY)
                {

                    int bytesWritten = myChannel.write (byteBuffer);
                    synchronized (this)
                    {
                        writeCount += bytesWritten;
                    }

                    if (isDebugEnabled)
                    {
                        logger.debug ("wrote {} bytes to {}", bytesWritten, connection_info);
                    }
                    // if buffer isn't empty request to be reactivated
                    if (byteBuffer.hasRemaining())
                    {
                        return true;
                    }
                }
            }
            catch (IOException ex)
            {
                try
                {
                    myChannel.close();
                }
                catch (IOException ex2)
                {
                    logger.error ("Failed to close channel: " + ex2.toString());
                }
                if (isDebugEnabled)
                {
                    logger.debug("Got IOException in write(). Transport to " + connection_info +
                                 ": stream closed: " + ex.toString());
                }
            }

            return false;
        }
    }

}