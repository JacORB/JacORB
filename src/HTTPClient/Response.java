/*
 * @(#)Response.java					0.3-2 18/06/1999
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
import java.io.SequenceInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.EOFException;
import java.net.URL;
import java.net.ProtocolException;
import java.util.Date;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;


/**
 * This class represents an intermediate response. It's used internally by the
 * modules. When all modules have handled the response then the HTTPResponse
 * fills in its fields with the data from this class.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public final class Response implements RoResponse, GlobalConstants
{
    /** our http connection */
    private HTTPConnection connection;

    /** our stream demux */
    private StreamDemultiplexor stream_handler;

    /** the HTTPResponse we're coupled with */
            HTTPResponse http_resp;

    /** the timeout for read operations */
            int          timeout = 0;

    /** our input stream (usually from the stream demux). Push input streams
     *  onto this if necessary. */
    public  InputStream  inp_stream;

    /** our response input stream from the stream demux */
    private RespInputStream  resp_inp_stream = null;

    /** the method used in the request */
    private String       method;

    /** the resource in the request (for debugging purposes) */
            String       resource;

    /** was a proxy used for the request? */
    private boolean      used_proxy;

    /** did the request contain an entity? */
    private boolean      sent_entity;

    /** the status code returned. */
            int          StatusCode = 0;

    /** the reason line associated with the status code. */
            String       ReasonLine;

    /** the HTTP version of the response. */
            String       Version;

    /** the final URI of the document. */
            URI          EffectiveURI = null;

    /** any headers which were received and do not fit in the above list. */
            CIHashtable  Headers = new CIHashtable();

    /** any trailers which were received and do not fit in the above list. */
            CIHashtable  Trailers = new CIHashtable();

    /** the message length of the response if either there is no data (in which
     *  case ContentLength=0) or if the message length is controlled by a
     *  Content-Length header. If neither of these, then it's -1  */
            int          ContentLength = -1;

    /** this indicates how the length of the entity body is determined */
            int          cd_type = CD_HDRS;

    /** the data (body) returned. */
            byte[]       Data = null;

    /** signals if we in the process of reading the headers */
            boolean      reading_headers = false;

    /** signals if we have got and parsed the headers yet */
            boolean      got_headers = false;

    /** signals if we have got and parsed the trailers yet */
            boolean      got_trailers = false;

    /** remembers any exception received while reading/parsing headers */
    private IOException  exception = null;

    /** should this response be handled further? */
            boolean      final_resp = false;


    // Constructors

    /**
     * Creates a new Response and registers it with the stream-demultiplexor.
     */
    Response(Request request, boolean used_proxy,
	     StreamDemultiplexor stream_handler)
	    throws IOException
    {
	this.connection     = request.getConnection();
	this.method         = request.getMethod();
	this.resource       = request.getRequestURI();
	this.used_proxy     = used_proxy;
	this.stream_handler = stream_handler;
	sent_entity         = (request.getData() != null) ? true : false;

	stream_handler.register(this, request);
	resp_inp_stream     = stream_handler.getStream(this);
	inp_stream          = resp_inp_stream;
    }


    /**
     * Creates a new Response that reads from the given stream. This is
     * used for the CONNECT subrequest which is used in establishing an
     * SSL tunnel through a proxy.
     *
     * @param request the subrequest
     * @param is      the input stream from which to read the headers and
     *                data.
     */
    Response(Request request, InputStream is) throws IOException
    {
	this.connection = request.getConnection();
	this.method     = request.getMethod();
	this.resource   = request.getRequestURI();
	used_proxy      = false;
	stream_handler  = null;
	sent_entity     = (request.getData() != null) ? true : false;
	inp_stream      = is;
    }


    /**
     * Create a new response with the given info. This is used when
     * creating a response in a requestHandler().
     *
     * <P>If <var>data</var> is not null then that is used; else if the
     * <var>is</var> is not null that is used; else the entity is empty.
     * If the input stream is used then <var>cont_len</var> specifies
     * the length of the data that can be read from it, or -1 if unknown.
     *
     * @param version  the response version (such as "HTTP/1.1")
     * @param status   the status code
     * @param reason   the reason line
     * @param headers  the response headers
     * @param data     the response entity
     * @param is       the response entity as an InputStream
     * @param cont_len the length of the data in the InputStream
     */
    public Response(String version, int status, String reason, NVPair[] headers,
		    byte[] data, InputStream is, int cont_len)
    {
	this.Version    = version;
	this.StatusCode = status;
	this.ReasonLine = reason;
	if (headers != null)
	    for (int idx=0; idx<headers.length; idx++)
		setHeader(headers[idx].getName(), headers[idx].getValue());
	if (data != null)
	    this.Data   = data;
	else if (is == null)
	    this.Data   = new byte[0];
	else
	{
	    this.inp_stream = is;
	    ContentLength   = cont_len;
	}

	got_headers  = true;
	got_trailers = true;
    }


    // Methods

    /**
     * give the status code for this request. These are grouped as follows:
     * <UL>
     *   <LI> 1xx - Informational (new in HTTP/1.1)
     *   <LI> 2xx - Success
     *   <LI> 3xx - Redirection
     *   <LI> 4xx - Client Error
     *   <LI> 5xx - Server Error
     * </UL>
     *
     * @exception IOException If any exception occurs on the socket.
     */
    public final int getStatusCode()  throws IOException
    {
	if (!got_headers)  getHeaders(true);
	return StatusCode;
    }

    /**
     * give the reason line associated with the status code.
     *
     * @exception IOException If any exception occurs on the socket.
     */
    public final String getReasonLine()  throws IOException
    {
	if (!got_headers)  getHeaders(true);
	return ReasonLine;
    }

    /**
     * get the HTTP version used for the response.
     *
     * @exception IOException If any exception occurs on the socket.
     */
    public final String getVersion()  throws IOException
    {
	if (!got_headers)  getHeaders(true);
	return Version;
    }

    /**
     * Wait for either a '100 Continue' or an error.
     *
     * @return the return status.
     */
    int getContinue()  throws IOException
    {
	getHeaders(false);
	return StatusCode;
    }

    /**
     * get the final URI of the document. This is set if the original
     * request was deferred via the "moved" (301, 302, or 303) return
     * status.
     *
     * @return the new URI, or null if not redirected
     * @exception IOException If any exception occurs on the socket.
     */
    public final URI getEffectiveURI()  throws IOException
    {
	if (!got_headers)  getHeaders(true);
	return EffectiveURI;
    }

    /**
     * set the final URI of the document. This is only for internal use.
     */
    public void setEffectiveURI(URI final_uri)
    {
	EffectiveURI = final_uri;
    }

    /**
     * get the final URL of the document. This is set if the original
     * request was deferred via the "moved" (301, 302, or 303) return
     * status.
     *
     * @exception IOException If any exception occurs on the socket.
     * @deprecated use getEffectiveURI() instead
     * @see #getEffectiveURI
     */
    public final URL getEffectiveURL()  throws IOException
    {
	return getEffectiveURI().toURL();
    }

    /**
     * set the final URL of the document. This is only for internal use.
     *
     * @deprecated use setEffectiveURI() instead
     * @see #setEffectiveURI
     */
    public void setEffectiveURL(URL final_url)
    {
	try
	    { setEffectiveURI(new URI(final_url)); }
	catch (ParseException pe)
	    { throw new Error(pe.toString()); }		// shouldn't happen
    }

    /**
     * retrieves the field for a given header.
     *
     * @param  hdr the header name.
     * @return the value for the header, or null if non-existent.
     * @exception IOException If any exception occurs on the socket.
     */
    public String getHeader(String hdr)  throws IOException
    {
	if (!got_headers)  getHeaders(true);
	return (String) Headers.get(hdr.trim());
    }

    /**
     * retrieves the field for a given header. The value is parsed as an
     * int.
     *
     * @param  hdr the header name.
     * @return the value for the header if the header exists
     * @exception NumberFormatException if the header's value is not a number
     *                                  or if the header does not exist.
     * @exception IOException if any exception occurs on the socket.
     */
    public int getHeaderAsInt(String hdr)
		throws IOException, NumberFormatException
    {
	return Integer.parseInt(getHeader(hdr));
    }

    /**
     * retrieves the field for a given header. The value is parsed as a
     * date; if this fails it is parsed as a long representing the number
     * of seconds since 12:00 AM, Jan 1st, 1970. If this also fails an
     * IllegalArgumentException is thrown.
     * 
     * <P>Note: When sending dates use Util.httpDate().
     *
     * @param  hdr the header name.
     * @return the value for the header, or null if non-existent.
     * @exception IOException If any exception occurs on the socket.
     * @exception IllegalArgumentException If the header cannot be parsed
     *            as a date or time.
     */
    public Date getHeaderAsDate(String hdr)
		throws IOException, IllegalArgumentException
    {
	String raw_date = getHeader(hdr);
	if (raw_date == null)  return null;

	// asctime() format is missing an explicit GMT specifier
	if (raw_date.toUpperCase().indexOf("GMT") == -1)
	    raw_date += " GMT";

	Date date;

	try
	    { date = new Date(raw_date); }
	catch (IllegalArgumentException iae)
	{
	    long time;
	    try
		{ time = Long.parseLong(raw_date); }
	    catch (NumberFormatException nfe)
		{ throw iae; }
	    if (time < 0)  time = 0;
	    date = new Date(time * 1000L);
	}

	return date;
    }


    /**
     * Set a header field in the list of headers. If the header already
     * exists it will be overwritten; otherwise the header will be added
     * to the list. This is used by some modules when they process the
     * header so that higher level stuff doesn't get confused when the
     * headers and data don't match.
     *
     * @param header The name of header field to set.
     * @param value  The value to set the field to.
     */
    public void setHeader(String header, String value)
    {
	Headers.put(header.trim(), value.trim());
    }


    /**
     * Removes a header field from the list of headers. This is used by
     * some modules when they process the header so that higher level stuff
     * doesn't get confused when the headers and data don't match.
     *
     * @param header The name of header field to remove.
     */
    public void deleteHeader(String header)
    {
	Headers.remove(header.trim());
    }


    /**
     * Retrieves the field for a given trailer. Note that this should not
     * be invoked until all the response data has been read. If invoked
     * before, it will force the data to be read via <code>getData()</code>.
     *
     * @param  trailer the trailer name.
     * @return the value for the trailer, or null if non-existent.
     * @exception IOException If any exception occurs on the socket.
     */
    public String getTrailer(String trailer)  throws IOException
    {
	if (!got_trailers)  getTrailers();
	return (String) Trailers.get(trailer.trim());
    }


    /**
     * Retrieves the field for a given tailer. The value is parsed as an
     * int.
     *
     * @param  trailer the tailer name.
     * @return the value for the trailer if the trailer exists
     * @exception NumberFormatException if the trailer's value is not a number
     *                                  or if the trailer does not exist.
     * @exception IOException if any exception occurs on the socket.
     */
    public int getTrailerAsInt(String trailer)
		throws IOException, NumberFormatException
    {
	return Integer.parseInt(getTrailer(trailer));
    }


    /**
     * Retrieves the field for a given trailer. The value is parsed as a
     * date; if this fails it is parsed as a long representing the number
     * of seconds since 12:00 AM, Jan 1st, 1970. If this also fails an
     * IllegalArgumentException is thrown.
     *
     * <P>Note: When sending dates use Util.httpDate().
     *
     * @param  trailer the trailer name.
     * @return the value for the trailer, or null if non-existent.
     * @exception IllegalArgumentException if the trailer's value is neither a
     *            legal date nor a number.
     * @exception IOException if any exception occurs on the socket.
     * @exception IllegalArgumentException If the header cannot be parsed
     *            as a date or time.
     */
    public Date getTrailerAsDate(String trailer)
		throws IOException, IllegalArgumentException
    {
	String raw_date = getTrailer(trailer);
	if (raw_date == null) return null;

	// asctime() format is missing an explicit GMT specifier
	if (raw_date.toUpperCase().indexOf("GMT") == -1)
	    raw_date += " GMT";

	Date   date;

	try
	    { date = new Date(raw_date); }
	catch (IllegalArgumentException iae)
	{
	    // some servers erroneously send a number, so let's try that
	    long time;
	    try
		{ time = Long.parseLong(raw_date); }
	    catch (NumberFormatException nfe)
		{ throw iae; }	// give up
	    if (time < 0)  time = 0;
	    date = new Date(time * 1000L);
	}

	return date;
    }


    /**
     * Set a trailer field in the list of trailers. If the trailer already
     * exists it will be overwritten; otherwise the trailer will be added
     * to the list. This is used by some modules when they process the
     * trailer so that higher level stuff doesn't get confused when the
     * trailer and data don't match.
     *
     * @param trailer The name of trailer field to set.
     * @param value   The value to set the field to.
     */
    public void setTrailer(String trailer, String value)
    {
	Trailers.put(trailer.trim(), value.trim());
    }


    /**
     * Removes a trailer field from the list of trailers. This is used by
     * some modules when they process the trailer so that higher level stuff
     * doesn't get confused when the trailers and data don't match.
     *
     * @param trailer The name of trailer field to remove.
     */
    public void deleteTrailer(String trailer)
    {
	Trailers.remove(trailer.trim());
    }


    /**
     * Reads all the response data into a byte array. Note that this method
     * won't return until <em>all</em> the data has been received (so for
     * instance don't invoke this method if the server is doing a server
     * push). If getInputStream() had been previously called then this method
     * only returns any unread data remaining on the stream and then closes
     * it.
     *
     * @see #getInputStream()
     * @return an array containing the data (body) returned. If no data
     *         was returned then it's set to a zero-length array.
     * @exception IOException If any io exception occured while reading
     *			      the data
     */
    public synchronized byte[] getData()  throws IOException
    {
	if (!got_headers)  getHeaders(true);

	if (Data == null)
	{
	    try
		{ readResponseData(inp_stream); }
	    catch (InterruptedIOException ie)		// don't intercept
		{ throw ie; }
	    catch (IOException ioe)
	    {
		if (DebugResp)
		{
		    System.err.print("Resp:  (" + inp_stream.hashCode() +
				     ") (" + Thread.currentThread() + ")");
		    ioe.printStackTrace();
		}
		try { inp_stream.close(); } catch (Exception e) { }
		throw ioe;
	    }

	    inp_stream.close();
	}

	return Data;
    }

    /**
     * Gets an input stream from which the returned data can be read. Note
     * that if getData() had been previously called it will actually return
     * a ByteArrayInputStream created from that data.
     *
     * @see #getData()
     * @return the InputStream.
     * @exception IOException If any exception occurs on the socket.
     */
    public synchronized InputStream getInputStream()  throws IOException
    {
	if (!got_headers)  getHeaders(true);

	if (Data == null)
	    return inp_stream;
	else
	    return new ByteArrayInputStream(Data);
    }

    /**
     * Some responses such as those from a HEAD or with certain status
     * codes don't have an entity. This is detected by the client and
     * can be queried here. Note that this won't try to do a read() on
     * the input stream (it will however cause the headers to be read
     * and parsed if not already done).
     *
     * @return true if the response has an entity, false otherwise
     * @since V0.3-1
     */
    public synchronized boolean hasEntity()  throws IOException
    {
	if (!got_headers)  getHeaders(true);

	return (cd_type != CD_0);
    }


    // Helper Methods

    /**
     * Gets and parses the headers. Sets up Data if no data will be received.
     *
     * @param skip_cont  if true skips over '100 Continue' status codes.
     * @exception IOException If any exception occurs while reading the headers.
     */
    private synchronized void getHeaders(boolean skip_cont)  throws IOException
    {
	if (got_headers)  return;
	if (exception != null)
	    throw (IOException) exception.fillInStackTrace();

	reading_headers = true;
	try
	{
	    do
	    {
		Headers.clear();	// clear any headers from 100 Continue
		String headers = readResponseHeaders(inp_stream);
		parseResponseHeaders(headers);
	    } while ((StatusCode == 100  &&  skip_cont)  ||	// Continue
		     (StatusCode > 101  &&  StatusCode < 200));	// Unknown
	}
	catch (IOException ioe)
	{
	    if (!(ioe instanceof InterruptedIOException))
		exception = ioe;
	    if (ioe instanceof ProtocolException)	// thrown internally
	    {
		cd_type = CD_CLOSE;
		if (stream_handler != null)
		    stream_handler.markForClose(this);
	    }
	    throw ioe;
	}
	finally
	    { reading_headers = false; }
	if (StatusCode == 100) return;


	// parse the Content-Length header

	int cont_len = -1;
	String cl_hdr = (String) Headers.get("Content-Length");
	if (cl_hdr != null)
	{
	    try
	    {
		cont_len = Integer.parseInt(cl_hdr);
		if (cont_len < 0)
		    throw new NumberFormatException();
	    }
	    catch (NumberFormatException nfe)
	    {
		throw new ProtocolException("Invalid Content-length header"+
					    " received: "+cl_hdr);
	    }
	}


	// parse the Transfer-Encoding header

	boolean te_chunked = false, te_is_identity = true, ct_mpbr = false;
	Vector  te_hdr = null;
	try
	    { te_hdr = Util.parseHeader((String) Headers.get("Transfer-Encoding")); }
	catch (ParseException pe)
	    { }
	if (te_hdr != null)
	{
	    te_chunked = ((HttpHeaderElement) te_hdr.lastElement()).getName().
			 equalsIgnoreCase("chunked");
	    for (int idx=0; idx<te_hdr.size(); idx++)
		if (((HttpHeaderElement) te_hdr.elementAt(idx)).getName().
		    equalsIgnoreCase("identity"))
		    te_hdr.removeElementAt(idx--);
		else
		    te_is_identity = false;
	}


	// parse Content-Type header

	try
	{
	    String hdr;
	    if ((hdr = (String) Headers.get("Content-Type")) != null)
	    {
		Vector phdr = Util.parseHeader(hdr);
		ct_mpbr = phdr.contains(new HttpHeaderElement("multipart/byteranges"))  ||
			  phdr.contains(new HttpHeaderElement("multipart/x-byteranges"));
	    }
	}
	catch (ParseException pe)
	    { }


	// now determine content-delimiter

	if (StatusCode < 200  ||  StatusCode == 204  ||  StatusCode == 205  ||
	    StatusCode == 304)
	{
	    cd_type = CD_0;
	}
	else if (te_chunked)
	{
	    cd_type = CD_CHUNKED;

	    te_hdr.removeElementAt(te_hdr.size()-1);
	    if (te_hdr.size() > 0)
		setHeader("Transfer-Encoding", Util.assembleHeader(te_hdr));
	    else
		deleteHeader("Transfer-Encoding");
	}
	else if (cont_len != -1  &&  te_is_identity)
	    cd_type = CD_CONTLEN;
	else if (ct_mpbr  &&  te_is_identity)
	    cd_type = CD_MP_BR;
	else if (!method.equals("HEAD"))
	{
	    cd_type = CD_CLOSE;
	    if (stream_handler != null)
		stream_handler.markForClose(this);

	    if (Version.equals("HTTP/0.9"))
	    {
		inp_stream =
			new SequenceInputStream(new ByteArrayInputStream(Data),
						inp_stream);
		Data = null;
	    }
	}

	if (cd_type == CD_CONTLEN)
	    ContentLength = cont_len;
	else
	    deleteHeader("Content-Length");	// Content-Length is not valid in this case

	/* We treat HEAD specially down here because the above code needs
	 * to know whether to remove the Content-length header or not.
	 */
	if (method.equals("HEAD"))
	    cd_type = CD_0;

	if (cd_type == CD_0)
	{
	    ContentLength = 0;
	    Data = new byte[0];
	    inp_stream.close();		// we will not receive any more data
	}

	if (DebugResp)
	{
	    System.err.println("Resp:  Response entity delimiter: " +
		(cd_type == CD_0       ? "No Entity"      :
		 cd_type == CD_CLOSE   ? "Close"          :
		 cd_type == CD_CONTLEN ? "Content-Length" :
		 cd_type == CD_CHUNKED ? "Chunked"        :
		 cd_type == CD_MP_BR   ? "Multipart"      :
		 "???" ) + " (" + inp_stream.hashCode() + ") (" +
		 Thread.currentThread() + ")");
	}


	// remove erroneous connection tokens

	if (connection.ServerProtocolVersion >= HTTP_1_1)
	    deleteHeader("Proxy-Connection");
	else					// HTTP/1.0
	{
	    if (connection.getProxyHost() != null)
		deleteHeader("Connection");
	    else
		deleteHeader("Proxy-Connection");

	    Vector pco;
	    try
		{ pco = Util.parseHeader((String) Headers.get("Connection")); }
	    catch (ParseException pe)
		{ pco = null; }

	    if (pco != null)
	    {
		for (int idx=0; idx<pco.size(); idx++)
		{
		    String name =
			    ((HttpHeaderElement) pco.elementAt(idx)).getName();
		    if (!name.equalsIgnoreCase("keep-alive"))
		    {
			pco.removeElementAt(idx);
			deleteHeader(name);
			idx--;
		    }
		}

		if (pco.size() > 0)
		    setHeader("Connection", Util.assembleHeader(pco));
		else
		    deleteHeader("Connection");
	    }

	    try
		{ pco = Util.parseHeader((String) Headers.get("Proxy-Connection")); }
	    catch (ParseException pe)
		{ pco = null; }

	    if (pco != null)
	    {
		for (int idx=0; idx<pco.size(); idx++)
		{
		    String name =
			    ((HttpHeaderElement) pco.elementAt(idx)).getName();
		    if (!name.equalsIgnoreCase("keep-alive"))
		    {
			pco.removeElementAt(idx);
			deleteHeader(name);
			idx--;
		    }
		}

		if (pco.size() > 0)
		    setHeader("Proxy-Connection", Util.assembleHeader(pco));
		else
		    deleteHeader("Proxy-Connection");
	    }
	}


	// this must be set before we invoke handleFirstRequest()
	got_headers = true;

	// special handling if this is the first response received
	if (isFirstResponse)
	{
	    if (!connection.handleFirstRequest(req, this))
	    {
		// got a buggy server - need to redo the request
		Response resp;
		try
		    { resp = connection.sendRequest(req, timeout); }
		catch (ModuleException me)
		    { throw new IOException(me.toString()); }
		resp.getVersion();

		this.StatusCode    = resp.StatusCode;
		this.ReasonLine    = resp.ReasonLine;
		this.Version       = resp.Version;
		this.EffectiveURI  = resp.EffectiveURI;
		this.ContentLength = resp.ContentLength;
		this.Headers       = resp.Headers;
		this.inp_stream    = resp.inp_stream;
		this.Data          = resp.Data;

		req = null;
	    }
	}
    }


    /* these are external to readResponseHeaders() because we need to be
     * able to restart after an InterruptedIOException
     */
    private byte[]       buf     = new byte[7];
    private int          buf_pos = 0;
    private StringBuffer hdrs    = new StringBuffer(400);
    private boolean      reading_lines = false;
    private boolean      bol     = true;
    private boolean      got_cr  = false;

    /**
     * Reads the response headers received, folding continued lines.
     *
     * <P>Some of the code is a bit convoluted because we have to be able
     * restart after an InterruptedIOException.
     *
     * @inp    the input stream from which to read the response
     * @return a (newline separated) list of headers
     * @exception IOException if any read on the input stream fails
     */
    private String readResponseHeaders(InputStream inp)  throws IOException
    {
	if (DebugResp)
	{
	    if (buf_pos == 0)
		System.err.println("Resp:  Reading Response headers " +
				    inp_stream.hashCode() + " (" +
				    Thread.currentThread() + ")");
	    else
		System.err.println("Resp:  Resuming reading Response headers " +
				    inp_stream.hashCode() + " (" +
				    Thread.currentThread() + ")");
	}


	// read 7 bytes to see type of response
	if (!reading_lines)
	{
	    try
	    {
		// Skip any leading white space to accomodate buggy responses
		if (buf_pos == 0)
		{
		    int c;
		    do
		    {
			if ((c = inp.read()) == -1)
			    throw new EOFException("Encountered premature EOF "
						   + "while reading Version");
		    } while (Character.isSpace( (char) (c & 0xFF) )) ;
		    buf[0] = (byte) (c & 0xFF);
		    buf_pos = 1;
		}

		// Now read first seven bytes (the version string)
		while (buf_pos < buf.length)
		{
		    int got = inp.read(buf, buf_pos, buf.length-buf_pos);
		    if (got == -1)
			throw new EOFException("Encountered premature EOF " +
						"while reading Version");
		    buf_pos += got;
		}
	    }
	    catch (EOFException eof)
	    {
		if (DebugResp)
		{
		    System.err.print("Resp:  (" + inp_stream.hashCode() + ") ("
				     + Thread.currentThread() + ")");
		    eof.printStackTrace();
		}
		throw eof;
	    }
	    for (int idx=0; idx<buf.length; idx++)
		hdrs.append((char) buf[idx]);

	    reading_lines = true;
	}

	if (hdrs.toString().startsWith("HTTP/")  ||		// It's x.x
	    hdrs.toString().startsWith("HTTP "))		// NCSA bug
	    readLines(inp);

	// reset variables for next round
	buf_pos = 0;
	reading_lines = false;
	bol     = true;
	got_cr  = false;

	String tmp = hdrs.toString();
	hdrs.setLength(0);
	return tmp;
    }


    boolean trailers_read = false;

    /**
     * This is called by the StreamDemultiplexor to read all the trailers
     * of a chunked encoded entity.
     *
     * @param inp the raw input stream to read from
     * @exception IOException if any IOException is thrown by the stream
     */
    void readTrailers(InputStream inp)  throws IOException
    {
	try
	{
	    readLines(inp);
	    trailers_read = true;
	}
	catch (IOException ioe)
	{
	    if (!(ioe instanceof InterruptedIOException))
		exception = ioe;
	    throw ioe;
	}
    }


    /**
     * This reads a set of lines up to and including the first empty line.
     * A line is terminated by either a <CR><LF> or <LF>. The lines are
     * stored in the <var>hdrs</var> buffers. Continued lines are merged
     * and stored as one line.
     *
     * <P>This method is restartable after an InterruptedIOException.
     *
     * @param inp the input stream to read from
     * @exception IOException if any IOException is thrown by the stream
     */
    private void readLines(InputStream inp)  throws IOException
    {
	/* This loop is a merge of readLine() from DataInputStream and
	 * the necessary header logic to merge continued lines and terminate
	 * after an empty line. The reason this is explicit is because of
	 * the need to handle InterruptedIOExceptions.
	 */
	loop: while (true)
	{
	    int b = inp.read();
	    switch (b)
	    {
		case -1:
		    throw new EOFException("Encountered premature EOF while reading headers:\n" + hdrs);
		case '\r':
		    got_cr = true;
		    break;
		case '\n':
		    if (bol)  break loop;	// all headers read
		    hdrs.append('\n');
		    bol    = true;
		    got_cr = false;
		    break;
		case ' ':
		case '\t':
		    if (bol)		// a continued line
		    {
			// replace previous \n with SP
			hdrs.setCharAt(hdrs.length()-1, ' ');
			bol = false;
			break;
		    }
		default:
		    if (got_cr)
		    {
			hdrs.append('\r');
			got_cr = false;
		    }
		    hdrs.append((char) (b & 0xFF));
		    bol = false;
		    break;
	    }
	}
    }


    /**
     * Parses the headers received into a new Response structure.
     *
     * @param  headers a (newline separated) list of headers
     * @exception ProtocolException if any part of the headers do not
     *            conform
     */
    private void parseResponseHeaders(String headers) throws ProtocolException
    {
	String          sts_line = null;
	StringTokenizer lines = new StringTokenizer(headers, "\r\n"),
			elem;


	if (DebugResp)
	    System.err.println("Resp:  Parsing Response headers from Request "+
				"\"" + method + " " + resource + "\":  (" +
				inp_stream.hashCode() + ") (" +
				Thread.currentThread() + ")\n\n"+headers);


	// Detect and handle HTTP/0.9 responses

	if (!headers.regionMatches(true, 0, "HTTP/", 0, 5)  &&
	    !headers.regionMatches(true, 0, "HTTP ", 0, 5))	// NCSA bug
	{
	    Version    = "HTTP/0.9";
	    StatusCode = 200;
	    ReasonLine = "OK";

	    Data       = new byte[headers.length()];
	    headers.getBytes(0, headers.length(), Data, 0);

	    return;
	}


	// get the status line

	try
	{
	    sts_line = lines.nextToken();
	    elem     = new StringTokenizer(sts_line, " \t");

	    Version    = elem.nextToken();
	    StatusCode = Integer.valueOf(elem.nextToken()).intValue();

	    if (Version.equalsIgnoreCase("HTTP"))	// NCSA bug
		Version = "HTTP/1.0";
	}
	catch (NoSuchElementException e)
	{
	    throw new ProtocolException("Invalid HTTP status line received: " +
					sts_line);
	}
	try
	    { ReasonLine = elem.nextToken("").trim(); }
	catch (NoSuchElementException e)
	    { ReasonLine = ""; }


	/* If the status code shows an error and we're sending (or have sent)
	 * an entity and it's length is delimited by a Content-length header,
	 * then we must close the the connection (if indeed it hasn't already
	 * been done) - RFC-2068, Section 8.2 .
	 */
	if (StatusCode >= 300  &&  sent_entity)
	{
	    if (stream_handler != null)
		stream_handler.markForClose(this);
	}


	// get the rest of the headers

	parseHeaderFields(lines, Headers);


	/* make sure the connection isn't closed prematurely if we have
	 * trailer fields
	 */
	if (Headers.get("Trailer") != null  &&  resp_inp_stream != null)
	    resp_inp_stream.dontTruncate();

	// Mark the end of the connection if it's not to be kept alive

	int vers;
	if (Version.equalsIgnoreCase("HTTP/0.9")  ||
	    Version.equalsIgnoreCase("HTTP/1.0"))
	    vers = 0;
	else
	    vers = 1;

	try
	{
	    String con = (String) Headers.get("Connection"),
		  pcon = (String) Headers.get("Proxy-Connection");

	    // parse connection header
	    if ((vers == 1  &&  con != null  &&  Util.hasToken(con, "close"))
		||
		(vers == 0  &&
		 !((!used_proxy && con != null &&
					Util.hasToken(con, "keep-alive"))  ||
		   (used_proxy && pcon != null &&
					Util.hasToken(pcon, "keep-alive")))
		)
	       )
		if (stream_handler != null)
		    stream_handler.markForClose(this);
	}
	catch (ParseException pe) { }
    }


    /**
     * If the trailers have not been read it calls <code>getData()</code>
     * to first force all data and trailers to be read. Then the trailers
     * parsed into the <var>Trailers</var> hashtable.
     *
     * @exception IOException if any exception occured during reading of the
     *                        response
     */
    private synchronized void getTrailers()  throws IOException
    {
	if (got_trailers)  return;
	if (exception != null)
	    throw (IOException) exception.fillInStackTrace();

	if (DebugResp)
	    System.err.println("Resp:  Reading Response trailers " +
				inp_stream.hashCode() + " (" +
				Thread.currentThread() + ")");

	try
	{
	    if (!trailers_read)
	    {
		if (resp_inp_stream != null)
		    resp_inp_stream.readAll(timeout);
	    }

	    if (trailers_read)
	    {
		if (DebugResp)
		    System.err.println("Resp:  Parsing Response trailers from "+
				       "Request \"" + method + " " + resource +
				       "\":  (" + inp_stream.hashCode() + ") ("+
				       Thread.currentThread() + ")\n\n"+hdrs);

		parseHeaderFields(new StringTokenizer(hdrs.toString(), "\r\n"),
				  Trailers);
	    }
	}
	finally
	{
	    got_trailers = true;
	}
    }


    /**
     * Parses the given lines as header fields of the form "<name>: <value>"
     * into the given list.
     *
     * @param lines the header or trailer lines, one header field per line
     * @param list  the Hashtable to store the parsed fields in
     * @exception ProtocolException if any part of the headers do not
     *                              conform
     */
    private void parseHeaderFields(StringTokenizer lines, CIHashtable list)
	    throws ProtocolException
    {
	while (lines.hasMoreTokens())
	{
	    String hdr = lines.nextToken();
	    int    sep = hdr.indexOf(':');

	    /* Once again we have to deal with broken servers and try
	     * to wing it here. If no ':' is found, try using the first
	     * space:
	     */
	    if (sep == -1)
		sep = hdr.indexOf(' ');
	    if (sep == -1)
	    {
		throw new ProtocolException("Invalid HTTP header received: " +
					    hdr);
	    }

	    String hdr_name = hdr.substring(0, sep).trim();

	    int len = hdr.length();
	    sep++;
	    while (sep < len  &&  Character.isSpace(hdr.charAt(sep)))  sep++;
	    String hdr_value = hdr.substring(sep);

	    String old_value  = (String) list.get(hdr_name);
	    if (old_value == null)
		list.put(hdr_name, hdr_value);
	    else
		list.put(hdr_name, old_value + ", " + hdr_value);
	}
    }


    /**
     * Reads the response data received. Does not return until either
     * Content-Length bytes have been read or EOF is reached.
     *
     * @inp       the input stream from which to read the data
     * @exception IOException if any read on the input stream fails
     */
    private void readResponseData(InputStream inp) throws IOException
    {
	if (ContentLength == 0)
	    return;

	if (Data == null)
	    Data = new byte[0];


	// read response data

	int off = Data.length;

	try
	{
	    // check Content-length header in case CE-Module removed it
	    if (getHeader("Content-Length") != null)
	    {
		int rcvd = 0;
		Data = new byte[ContentLength];

		do
		{
		    off  += rcvd;
		    rcvd  = inp.read(Data, off, ContentLength-off);
		} while (rcvd != -1  &&  off+rcvd < ContentLength);

		/* Don't do this!
		 * If we do, then getData() won't work after a getInputStream()
		 * because we'll never get all the expected data. Instead, let
		 * the underlying RespInputStream throw the EOF.
		if (rcvd == -1)	// premature EOF
		{
		    throw new EOFException("Encountered premature EOF while " +
					    "reading headers: received " + off +
					    " bytes instead of the expected " +
					    ContentLength + " bytes");
		}
		*/
	    }
	    else
	    {
		int inc  = 1000,
		    rcvd = 0;

		do
		{
		    off  += rcvd;
		    Data  = Util.resizeArray(Data, off+inc);
		} while ((rcvd = inp.read(Data, off, inc)) != -1);

		Data = Util.resizeArray(Data, off);
	    }
	}
	catch (IOException ioe)
	{
	    Data = Util.resizeArray(Data, off);
	    throw ioe;
	}
	finally
	{
	    try
		{ inp.close(); }
	    catch (IOException ioe)
		{ }
	}
    }


    Request        req = null;
    boolean isFirstResponse = false;
    /**
     * This marks this response as belonging to the first request made
     * over an HTTPConnection. The <var>con</var> and <var>req</var>
     * parameters are needed in case we have to do a resend of the request -
     * this is to handle buggy servers which barf upon receiving a request
     * marked as HTTP/1.1 .
     *
     * @param con The HTTPConnection used
     * @param req The Request sent
     */
    void markAsFirstResponse(Request req)
    {
	this.req = req;
	isFirstResponse = true;
    }
}

