/*
 * @(#)HttpOutputStream.java				0.3-2 18/06/1999
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


import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class provides an output stream for requests. The stream must first
 * be associated with a request before it may be used; this is done by
 * passing it to one of the request methods in HTTPConnection. Example:
 * <PRE>
 *    OutputStream out = new HttpOutputStream(12345);
 *    rsp = con.Post("/cgi-bin/my_cgi", out);
 *    out.write(...);
 *    out.close();
 *    if (rsp.getStatusCode() >= 300)
 *        ...
 * </PRE>
 *
 * <P>There are two constructors for this class, one taking a length parameter,
 * and one without any parameters. If the stream is created with a length
 * then the request will be sent with the corresponding Content-length header
 * and anything written to the stream will be written on the socket immediately.
 * This is the preferred way. If the stream is created without a length then
 * one of two things will happen: if, at the time of the request, the server
 * is known to understand HTTP/1.1 then each write() will send the data
 * immediately using the chunked encoding. If, however, either the server
 * version is unknown (because this is first request to that server) or the
 * server only understands HTTP/1.0 then all data will be written to a buffer
 * first, and only when the stream is closed will the request be sent.
 *
 * <P>Another reason that using the <var>HttpOutputStream(length)</var>
 * constructor is recommended over the <var>HttpOutputStream()</var> one is
 * that some HTTP/1.1 servers do not allow the chunked transfer encoding to
 * be used when POSTing to a cgi script. This is because the way the cgi API
 * is defined the cgi script expects a Content-length environment variable.
 * If the data is sent using the chunked transfer encoding however, then the
 * server would have to buffer all the data before invoking the cgi so that
 * this variable could be set correctly. Not all servers are willing to do
 * this.
 *
 * <P>If you cannot use the <var>HttpOutputStream(length)</var> constructor and
 * are having problems sending requests (usually a 411 response) then you can
 * try setting the system property <var>HTTPClient.dontChunkRequests</var> to
 * <var>true</var> (this needs to be done either on the command line or
 * somewhere in the code before the HTTPConnection is first accessed). This
 * will prevent the client from using the chunked encoding in this case and
 * will cause the HttpOutputStream to buffer all the data instead, sending it
 * only when close() is invoked.
 *
 * <P>The behaviour of a request sent with an output stream may differ from
 * that of a request sent with a data parameter. The reason for this is that
 * the various modules cannot resend a request which used an output stream.
 * Therefore such things as authorization and retrying of requests won't be
 * done by the HTTPClient for such requests.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.3
 */

public class HttpOutputStream extends OutputStream implements GlobalConstants
{
    /** null trailers */
    private static final NVPair[] empty = new NVPair[0];

    /** the length of the data to be sent */
    private int length;

    /** the length of the data received so far */
    private int rcvd = 0;

    /** the request this stream is associated with */
    private Request req = null;

    /** the response from sendRequest if we stalled the request */
    private Response resp = null;

    /** the socket output stream */
    private OutputStream os = null;

    /** the buffer to be used if needed */
    private ByteArrayOutputStream bos = null;

    /** the trailers to send if using chunked encoding. */
    private NVPair[] trailers = empty;

    /** the timeout to pass to SendRequest() */
    private int con_to = 0;

    /** just ignore all the data if told to do so */
    private boolean ignore = false;


    // Constructors

    /**
     * Creates an output stream of unspecified length. Note that it is
     * <strong>highly</strong> recommended that this constructor be avoided
     * where possible and <code>HttpOutputStream(int)</code> used instead.
     *
     * @see HttpOutputStream#HttpOutputStream(int)
     */
    public HttpOutputStream()
    {
	length = -1;
    }


    /**
     * This creates an output stream which will take <var>length</var> bytes
     * of data.
     *
     * @param length the number of bytes which will be sent over this stream
     */
    public HttpOutputStream(int length)
    {
	if (length < 0)
	   throw new IllegalArgumentException("Length must be greater equal 0");
	this.length = length;
    }


    // Methods

    /**
     * Associates this stream with a request and the actual output stream.
     * No other methods in this class may be invoked until this method has
     * been invoked by the HTTPConnection.
     *
     * @param req    the request this stream is to be associated with
     * @param os     the underlying output stream to write our data to, or null
     *               if we should write to a ByteArrayOutputStream instead.
     * @param con_to connection timeout to use in sendRequest()
     */
    void goAhead(Request req, OutputStream os, int con_to)
    {
	this.req    = req;
	this.os     = os;
	this.con_to = con_to;

	if (os == null)
	    bos = new ByteArrayOutputStream();

	if (DebugConn)
	{
	    System.err.println("OutS:  Stream ready for writing");
	    if (bos != null)
		System.err.println("OutS:  Buffering all data before sending " +
				   "request");
	}
    }


    /**
     * Setup this stream to dump the data to the great bit-bucket in the sky.
     * This is needed for when a module handles the request directly.
     *
     * @param req the request this stream is to be associated with
     */
    void ignoreData(Request req)
    {
	this.req = req;
	ignore = true;
    }


    /**
     * Return the response we got from sendRequest(). This waits until
     * the request has actually been sent.
     *
     * @return the response returned by sendRequest()
     */
    synchronized Response getResponse()
    {
	while (resp == null)
	    try { wait(); } catch (InterruptedException ie) { }

	return resp;
    }


    /**
     * Returns the number of bytes this stream is willing to accept, or -1
     * if it is unbounded.
     *
     * @return the number of bytes
     */
    public int getLength()
    {
	return length;
    }


    /**
     * Gets the trailers which were set with <code>setTrailers()</code>.
     *
     * @return an array of header fields
     * @see #setTrailers(NVPair[])
     */
    public NVPair[] getTrailers()
    {
	return trailers;
    }


    /**
     * Sets the trailers to be sent if the output is sent with the
     * chunked transfer encoding. These must be set before the output
     * stream is closed for them to be sent.
     *
     * <P>Any trailers set here <strong>should</strong> be mentioned
     * in a <var>Trailer</var> header in the request (see section 14.40
     * of draft-ietf-http-v11-spec-rev-06.txt).
     *
     * <P>This method (and its related <code>getTrailers()</code>)) are
     * in this class and not in <var>Request</var> because setting
     * trailers is something an application may want to do, not only
     * modules.
     *
     * @param trailers an array of header fields
     */
    public void setTrailers(NVPair[] trailers)
    {
	if (trailers != null)
	    this.trailers = trailers;
	else
	    this.trailers = empty;
    }


    /**
     * Writes a single byte on the stream. It is subject to the same rules
     * as <code>write(byte[], int, int)</code>.
     *
     * @param b the byte to write
     * @exception IOException if any exception is thrown by the socket
     * @see #write(byte[], int, int)
     */
    public void write(int b)  throws IOException, IllegalAccessError
    {
	byte[] tmp = { (byte) b };
	write(tmp, 0, 1);
    }


    /**
     * Writes an array of bytes on the stream. This method may not be used
     * until this stream has been passed to one of the methods in
     * HTTPConnection (i.e. until it has been associated with a request).
     *
     * @param buf an array containing the data to write
     * @param off the offset of the data whithin the buffer
     * @param len the number bytes (starting at <var>off</var>) to write
     * @exception IOException if any exception is thrown by the socket, or
     *            if writing <var>len</var> bytes would cause more bytes to
     *            be written than this stream is willing to accept.
     * @exception IllegalAccessError if this stream has not been associated
     *            with a request yet
     */
    public synchronized void write(byte[] buf, int off, int len)
	    throws IOException, IllegalAccessError
    {
	if (req == null)
	    throw new IllegalAccessError("Stream not associated with a request");

	if (ignore) return;

	if (length != -1  &&  rcvd+len > length)
	{
	    IOException ioe =
		new IOException("Tried to write too many bytes (" + (rcvd+len) +
				" > " + length + ")");
	    req.getConnection().closeDemux(ioe, false);
	    req.getConnection().outputFinished();
	    throw ioe;
	}

	try
	{
	    if (bos != null)
		bos.write(buf, off, len);
	    else if (length != -1)
		os.write(buf, off, len);
	    else
		os.write(Codecs.chunkedEncode(buf, off, len, null, false));
	}
	catch (IOException ioe)
	{
	    req.getConnection().closeDemux(ioe, true);
	    req.getConnection().outputFinished();
	    throw ioe;
	}

	rcvd += len;
    }


    /**
     * Closes the stream and causes the data to be sent if it has not already
     * been done so. This method <strong>must</strong> be invoked when all
     * data has been written.
     *
     * @exception IOException if any exception is thrown by the underlying
     *            socket, or if too few bytes were written.
     * @exception IllegalAccessError if this stream has not been associated
     *            with a request yet.
     */
    public synchronized void close()  throws IOException, IllegalAccessError
    {
	if (req == null)
	    throw new IllegalAccessError("Stream not associated with a request");

	if (ignore) return;

	if (bos != null)
	{
	    req.setData(bos.toByteArray());
	    req.setStream(null);

	    if (trailers.length > 0)
	    {
		NVPair[] hdrs = req.getHeaders();

		// remove any Trailer header field

		int len = hdrs.length;
		for (int idx=0; idx<len; idx++)
		{
		    if (hdrs[idx].getName().equalsIgnoreCase("Trailer"))
		    {
			System.arraycopy(hdrs, idx+1, hdrs, idx, len-idx-1);
			len--;
		    }
		}


		// add the trailers to the headers

		hdrs = Util.resizeArray(hdrs, len+trailers.length);
		System.arraycopy(trailers, 0, hdrs, len, trailers.length);

		req.setHeaders(hdrs);
	    }

	    if (DebugConn)  System.err.println("OutS:  Sending request");

	    try
		{ resp = req.getConnection().sendRequest(req, con_to); }
	    catch (ModuleException me)
		{ throw new IOException(me.toString()); }
	    notify();
	}
	else
	{
	    if (rcvd < length)
	    {
		IOException ioe =
		    new IOException("Premature close: only " + rcvd +
				    " bytes written instead of the " +
				    "expected " + length);
		req.getConnection().closeDemux(ioe, false);
		req.getConnection().outputFinished();
		throw ioe;
	    }

	    try
	    {
		if (length == -1)
		{
		    if (DebugConn  &&  trailers.length > 0)
		    {
			System.err.println("OutS:  Sending trailers:");
			for (int idx=0; idx<trailers.length; idx++)
			    System.err.println("       " +
				      trailers[idx].getName() + ": " +
				      trailers[idx].getValue());
		    }

		    os.write(Codecs.chunkedEncode(null, 0, 0, trailers, true));
		}

		os.flush();

		if (DebugConn)  System.err.println("OutS:  All data sent");
	    }
	    catch (IOException ioe)
	    {
		req.getConnection().closeDemux(ioe, true);
		throw ioe;
	    }
	    finally
	    {
		req.getConnection().outputFinished();
	    }
	}
    }


    /**
     * produces a string describing this stream.
     *
     * @return a string containing the name and the length
     */
    public String toString()
    {
	return getClass().getName() + "[length=" + length + "]";
    }
}

