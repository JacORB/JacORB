/*
 * @(#)RespInputStream.java				0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschalär
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;

import java.io.InputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This is the InputStream that gets returned to the user. The extensions
 * consist of the capability to have the data pushed into a buffer if the
 * stream demux needs to.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.2
 */
final class RespInputStream extends InputStream implements GlobalConstants
{
    /** the stream demultiplexor */
    private StreamDemultiplexor demux = null;

    /** our response handler */
    private ResponseHandler     resph;

    /** signals that the user has closed the stream and will therefore
	not read any further data */
	    boolean             closed = false;

    /** signals that the connection may not be closed prematurely */
    private boolean             dont_truncate = false;

    /** this buffer is used to buffer data that the demux has to get rid of */
    private byte[]              buffer = null;

    /** signals that we were interrupted and that the buffer is not complete */
    private boolean             interrupted = false;

    /** the offset at which the unread data starts in the buffer */
    private int                 offset = 0;

    /** the end of the data in the buffer */
    private int                 end = 0;

    /** the total number of bytes of entity data read from the demux so far */
            int                 count = 0;


    // Constructors

    RespInputStream(StreamDemultiplexor demux, ResponseHandler resph)
    {
	this.demux = demux;
	this.resph = resph;
    }


    // public Methods

    private byte[] ch = new byte[1];
    /**
     * Reads a single byte.
     *
     * @return the byte read, or -1 if EOF.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int read() throws IOException
    {
	int rcvd = read(ch, 0, 1);
	if (rcvd == 1)
	    return ch[0] & 0xff;
	else
	    return -1;
    }


    /**
     * Reads <var>len</var> bytes into <var>b</var>, starting at offset
     * <var>off</var>.
     *
     * @return the number of bytes actually read, or -1 if EOF.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
	if (closed)
	    return -1;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    if (left == 0)  return -1;

	    len = (len > left ? left : len);
	    System.arraycopy(buffer, offset, b, off, len);
	    offset += len;

	    return len;
	}
	else
	{
	    if (DebugDemux)
	    {
		if (resph.resp.cd_type != CD_HDRS)
		    System.err.println("RspIS: Reading stream " +
				       this.hashCode() +
				       " (" + Thread.currentThread() + ")");
	    }

	    int rcvd;
	    if (resph.resp.cd_type == CD_HDRS)
		rcvd = demux.read(b, off, len, resph, resph.resp.timeout);
	    else
		rcvd = demux.read(b, off, len, resph, 0);
	    if (rcvd != -1  &&  resph.resp.got_headers)
		count += rcvd;

	    return rcvd;
	}
    }


    /**
     * skips <var>num</var> bytes.
     *
     * @return the number of bytes actually skipped.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized long skip(long num) throws IOException
    {
	if (closed)
	    return 0;

	int left = end - offset;
	if (buffer != null  &&  !(left == 0  &&  interrupted))
	{
	    num = (num > left ? left : num);
	    offset  += num;
	    return num;
	}
	else
	{
	    long skpd = demux.skip(num, resph);
	    if (resph.resp.got_headers)
		count += skpd;
	    return skpd;
	}
    }


    /**
     * gets the number of bytes available for reading without blocking.
     *
     * @return the number of bytes available.
     * @exception IOException if any exception occured on the connection.
     */
    public synchronized int available() throws IOException
    {
	if (closed)
	    return 0;

	if (buffer != null  &&  !(end-offset == 0  &&  interrupted))
	    return end-offset;
	else
	    return demux.available(resph);
    }


    /**
     * closes the stream.
     *
     * @exception if any exception occured on the connection before or
     *            during close.
     */
    public synchronized void close()  throws IOException
    {
	if (!closed)
	{
	    closed = true;

	    if (dont_truncate  &&  (buffer == null  ||  interrupted))
		readAll(resph.resp.timeout);

	    if (DebugDemux)
		System.err.println("RspIS: User closed stream " + hashCode() +
				   " (" + Thread.currentThread() + ")");

	    demux.closeSocketIfAllStreamsClosed();

	    if (dont_truncate)
	    {
		try
		    { resph.resp.http_resp.invokeTrailerHandlers(false); }
		catch (ModuleException me)
		    { throw new IOException(me.toString()); }
	    }
	}
    }


    /**
     * A safety net to clean up.
     */
    protected void finalize()  throws Throwable
    {
	try
	    { close(); }
	finally
	    { super.finalize(); }
    }


    // local Methods

    /**
     * Reads all remainings data into buffer. This is used to force a read
     * of upstream responses.
     *
     * <P>This is probably the most tricky and buggy method around. It's the
     * only one that really violates the strict top-down method invocation
     * from the Response through the ResponseStream to the StreamDemultiplexor.
     * This means we need to be awfully careful about what is synchronized
     * and what parameters are passed to whom.
     *
     * @param timeout the timeout to use for reading from the demux
     * @exception IOException If any exception occurs while reading stream.
     */
    void readAll(int timeout)  throws IOException
    {
	if (DebugDemux)
	    System.err.println("RspIS: Read-all on stream " + this.hashCode() +
			       " (" + Thread.currentThread() + ")");

	synchronized(resph.resp)
	{
	    if (!resph.resp.got_headers)	// force headers to be read
	    {
		int sav_to = resph.resp.timeout;
		resph.resp.timeout = timeout;
		resph.resp.getStatusCode();
		resph.resp.timeout = sav_to;
	    }
	}

	synchronized(this)
	{
	    if (buffer != null  &&  !interrupted)  return;

	    int rcvd = 0;
	    try
	    {
		if (closed)			// throw away
		{
		    buffer = new byte[10000];
		    do
		    {
			count += rcvd;
			rcvd   = demux.read(buffer, 0, buffer.length, resph,
					    timeout);
		    } while (rcvd != -1);
		    buffer = null;
		}
		else
		{
		    if (buffer == null)
		    {
			buffer = new byte[10000];
			offset = 0;
			end    = 0;
		    }

		    do
		    {
			rcvd = demux.read(buffer, end, buffer.length-end, resph,
					  timeout);
			if (rcvd < 0)  break;

			count  += rcvd;
			end    += rcvd;
			buffer  = Util.resizeArray(buffer, end+10000);
		    } while (true);
		}
	    }
	    catch (InterruptedIOException iioe)
	    {
		interrupted = true;
		throw iioe;
	    }
	    catch (IOException ioe)
	    {
		buffer = null;	// force a read on demux for exception
	    }

	    interrupted = false;
	}
    }


    /**
     * Sometime the full response body must be read, i.e. the connection may
     * not be closed prematurely (by us). Currently this is needed when the
     * chunked encoding with trailers is used in a response.
     */
    synchronized void dontTruncate()
    {
	dont_truncate = true;
    }
}

