/*
 * @(#)HTTPResponse.java				0.3-2 18/06/1999
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

import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;


/**
 * This defines the http-response class returned by the requests. It's
 * basically a wrapper around the Response class which first lets all
 * the modules handle the response before finally giving the info to
 * the user.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	0.3
 */

public class HTTPResponse implements GlobalConstants, HTTPClientModuleConstants
{
    /** the list of modules */
    private HTTPClientModule[]  modules;

    /** the timeout for reads */
    private int          timeout;

    /** the request */
    private Request      request = null;

    /** the current response */
    private Response     response = null;

    /** the HttpOutputStream to synchronize on */
    private HttpOutputStream out_stream = null;

    /** our input stream from the stream demux */
    private InputStream  inp_stream;

    /** the status code returned. */
    private int          StatusCode;

    /** the reason line associated with the status code. */
    private String       ReasonLine;

    /** the HTTP version of the response. */
    private String       Version;

    /** the original URI used. */
    private URI          OriginalURI = null;

    /** the final URI of the document. */
    private URI          EffectiveURI = null;

    /** any headers which were received and do not fit in the above list. */
    private CIHashtable  Headers = null;

    /** any trailers which were received and do not fit in the above list. */
    private CIHashtable  Trailers = null;

    /** the ContentLength of the data. */
    private int          ContentLength = -1;

    /** the data (body) returned. */
    private byte[]       Data = null;

    /** signals if we have got and parsed the headers yet? */
    private boolean      initialized = false;

    /** signals if we have got the trailers yet? */
    private boolean      got_trailers = false;

    /** marks this response as aborted (stop() in HTTPConnection) */
    private boolean      aborted = false;

    /** the method used in the request */
    private String       method = null;


    // Constructors

    /**
     * Creates a new HTTPResponse.
     *
     * @param modules the list of modules handling this response
     * @param timeout the timeout to be used on stream read()'s
     */
    HTTPResponse(HTTPClientModule[] modules, int timeout, Request orig)
    {
	this.modules = modules;
	this.timeout = timeout;
	try
	{
	    this.OriginalURI = new URI(orig.getConnection().getProtocol(),
				       orig.getConnection().getHost(),
				       orig.getConnection().getPort(),
				       orig.getRequestURI());
	}
	catch (ParseException pe)
	    { }
	this.method = orig.getMethod();
    }


    /**
     * @param req the request
     * @param resp the response
     */
    void set(Request req, Response resp)
    {
	this.request   = req;
	this.response  = resp;
	resp.http_resp = this;
	resp.timeout   = timeout;
	this.aborted   = resp.final_resp;
    }


    /**
     * @param req the request
     * @param resp the response
     */
    void set(Request req, HttpOutputStream out_stream)
    {
	this.request    = req;
	this.out_stream = out_stream;
    }


    // Methods

    /**
     * Give the status code for this request. These are grouped as follows:
     * <UL>
     *   <LI> 1xx - Informational (new in HTTP/1.1)
     *   <LI> 2xx - Success
     *   <LI> 3xx - Redirection
     *   <LI> 4xx - Client Error
     *   <LI> 5xx - Server Error
     * </UL>
     *
     * @exception IOException if any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public final int getStatusCode()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return StatusCode;
    }

    /**
     * Give the reason line associated with the status code.
     *
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public final String getReasonLine()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return ReasonLine;
    }

    /**
     * Get the HTTP version used for the response.
     *
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public final String getVersion()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return Version;
    }

    /**
     * Get the name and type of server.
     *
     * @deprecated This method is a remnant of V0.1; use
     *             <code>getHeader("Server")</code> instead.
     * @see #getHeader(java.lang.String)
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public final String getServer()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return getHeader("Server");
    }


    /**
     * Get the original URI used in the request.
     *
     * @return the URI used in primary request
     */
    public final URI getOriginalURI()
    {
	return OriginalURI;
    }


    /**
     * Get the final URL of the document. This is set if the original
     * request was deferred via the "moved" (301, 302, or 303) return
     * status.
     *
     * @return the effective URL, or null if no redirection occured
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     * @deprecated use getEffectiveURI() instead
     * @see #getEffectiveURI
     */
    public final URL getEffectiveURL()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	if (EffectiveURI != null)
	    return EffectiveURI.toURL();
	return null;
    }


    /**
     * Get the final URI of the document. If the request was redirected
     * via the "moved" (301, 302, 303, or 307) return status this returns
     * the URI used in the last redirection; otherwise it returns the
     * original URI.
     *
     * @return the effective URI
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public final URI getEffectiveURI()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	if (EffectiveURI != null)
	    return EffectiveURI;
	return OriginalURI;
    }


    /**
     * Retrieves the value for a given header.
     *
     * @param  hdr the header name.
     * @return the value for the header, or null if non-existent.
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public String getHeader(String hdr)  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return (String) Headers.get(hdr.trim());
    }

    /**
     * Retrieves the value for a given header. The value is parsed as an
     * int.
     *
     * @param  hdr the header name.
     * @return the value for the header if the header exists
     * @exception NumberFormatException if the header's value is not a number
     *                                  or if the header does not exist.
     * @exception IOException if any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public int getHeaderAsInt(String hdr)
		throws IOException, ModuleException, NumberFormatException
    {
	return Integer.parseInt(getHeader(hdr));
    }

    /**
     * Retrieves the value for a given header. The value is parsed as a
     * date; if this fails it is parsed as a long representing the number
     * of seconds since 12:00 AM, Jan 1st, 1970. If this also fails an
     * exception is thrown.
     * <br>Note: When sending dates use Util.httpDate().
     *
     * @param  hdr the header name.
     * @return the value for the header, or null if non-existent.
     * @exception IllegalArgumentException if the header's value is neither a
     *            legal date nor a number.
     * @exception IOException if any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public Date getHeaderAsDate(String hdr)
		throws IOException, IllegalArgumentException, ModuleException
    {
	String raw_date = getHeader(hdr);
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
     * Returns an enumeration of all the headers available via getHeader().
     *
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public Enumeration listHeaders()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();
	return Headers.keys();
    }


    /**
     * Retrieves the value for a given trailer. This should not be invoked
     * until all response data has been read. If invoked before it will
     * call <code>getData()</code> to force the data to be read.
     *
     * @param  trailer the trailer name.
     * @return the value for the trailer, or null if non-existent.
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     * @see #getData()
     */
    public String getTrailer(String trailer) throws IOException, ModuleException
    {
	if (!got_trailers)  getTrailers();
	return (String) Trailers.get(trailer.trim());
    }

    /**
     * Retrieves the value for a given tailer. The value is parsed as an
     * int.
     *
     * @param  trailer the tailer name.
     * @return the value for the trailer if the trailer exists
     * @exception NumberFormatException if the trailer's value is not a number
     *                                  or if the trailer does not exist.
     * @exception IOException if any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public int getTrailerAsInt(String trailer)
		throws IOException, ModuleException, NumberFormatException
    {
	return Integer.parseInt(getTrailer(trailer));
    }

    /**
     * Retrieves the value for a given trailer. The value is parsed as a
     * date; if this fails it is parsed as a long representing the number
     * of seconds since 12:00 AM, Jan 1st, 1970. If this also fails an
     * IllegalArgumentException is thrown.
     * <br>Note: When sending dates use Util.httpDate().
     *
     * @param  trailer the trailer name.
     * @return the value for the trailer, or null if non-existent.
     * @exception IllegalArgumentException if the trailer's value is neither a
     *            legal date nor a number.
     * @exception IOException if any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public Date getTrailerAsDate(String trailer)
		throws IOException, IllegalArgumentException, ModuleException
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
     * Returns an enumeration of all the trailers available via getTrailer().
     *
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public Enumeration listTrailers()  throws IOException, ModuleException
    {
	if (!got_trailers)  getTrailers();
	return Trailers.keys();
    }


    /**
     * Reads all the response data into a byte array. Note that this method
     * won't return until <em>all</em> the data has been received (so for
     * instance don't invoke this method if the server is doing a server
     * push). If <code>getInputStream()</code> had been previously invoked
     * then this method only returns any unread data remaining on the stream
     * and then closes it.
     *
     * <P>Note to the unwarry: code like
     *    <code>System.out.println("The data: " + resp.getData())</code>
     * will probably not do what you want - use
     *    <code>System.out.println("The data: " + new String(resp.getData()))</code>
     * instead.
     *
     * @see #getInputStream()
     * @return an array containing the data (body) returned. If no data
     *         was returned then it's set to a zero-length array.
     * @exception IOException If any io exception occured while reading
     *			      the data
     * @exception ModuleException if any module encounters an exception.
     */
    public synchronized byte[] getData()  throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();

	if (Data == null)
	{
	    try
		{ readResponseData(inp_stream); }
	    catch (InterruptedIOException ie)		// don't intercept
	    {
		throw ie;
	    }
	    catch (IOException ioe)
	    {
		if (DebugResp)
		{
		    System.err.println("HResp: (\"" + method + " " +
				       OriginalURI.getPath() + "\")");
		    System.err.print("       ");
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
     * that if <code>getData()</code> had been previously invoked it will
     * actually return a ByteArrayInputStream created from that data.
     *
     * @see #getData()
     * @return the InputStream.
     * @exception IOException If any exception occurs on the socket.
     * @exception ModuleException if any module encounters an exception.
     */
    public synchronized InputStream getInputStream()
	    throws IOException, ModuleException
    {
	if (!initialized)  handleResponse();

	if (Data == null)
	    return inp_stream;
	else
	{
	    getData();		// ensure complete data is read
	    return new ByteArrayInputStream(Data);
	}
    }


    /**
     * produces a full list of headers and their values, one per line.
     *
     * @return a string containing the headers
     */
    public String toString()
    {
	if (!initialized)
	{
	    try
		{ handleResponse(); }
	    catch (Exception e)
	    {
		if (DebugResp  &&  !(e instanceof InterruptedIOException))
		{
		    System.err.println("HResp: (\"" + method + " " +
				       OriginalURI.getPath() + "\")");
		    System.err.print("       ");
		    e.printStackTrace();
		}
		return "Failed to read headers: " + e;
	    }
	}

	String nl = System.getProperty("line.separator", "\n");

	StringBuffer str = new StringBuffer(Version);
	str.append(' ');
	str.append(StatusCode);
	str.append(' ');
	str.append(ReasonLine);
	str.append(nl);

	if (EffectiveURI != null)
	{
	    str.append("Effective-URI: ");
	    str.append(EffectiveURI);
	    str.append(nl);
	}

	Enumeration hdr_list = Headers.keys();
	while (hdr_list.hasMoreElements())
	{
	    String hdr = (String) hdr_list.nextElement();
	    str.append(hdr);
	    str.append(": ");
	    str.append(Headers.get(hdr));
	    str.append(nl);
	}

	return str.toString();
    }


    // Helper Methods


    HTTPClientModule[] getModules()
    {
	return modules;
    }


    /**
     * Processes a Response. This is done by calling the response handler
     * in each module. When all is done, the various fields of this instance
     * are intialized from the last Response.
     *
     * @exception IOException if any handler throws an IOException.
     * @exception ModuleException if any module encounters an exception.
     * @return true if a new request was generated. This is used for internal
     *         subrequests only
     */
    synchronized boolean handleResponse()  throws IOException, ModuleException
    {
	if (initialized)  return false;


	/* first get the response if necessary */

	if (out_stream != null)
	{
	    response           = out_stream.getResponse();
	    response.http_resp = this;
	    out_stream         = null;
	}


	/* go through modules and handle them */

	doModules: while (true)
	{

	Phase1: for (int idx=0; idx<modules.length && !aborted; idx++)
	{
	    try
		{ modules[idx].responsePhase1Handler(response, request); }
	    catch (RetryException re)
	    {
		if (re.restart)
		    continue doModules;
		else
		    throw re;
	    }
	}

	Phase2: for (int idx=0; idx<modules.length && !aborted; idx++)
	{
            int sts = modules[idx].responsePhase2Handler(response, request);
            switch (sts)
            {
                case RSP_CONTINUE:	// continue processing
                    break;

                case RSP_RESTART:	// restart response processing
                    idx = -1;
		    continue doModules;

                case RSP_SHORTCIRC:	// stop processing and return
                    break doModules;

                case RSP_REQUEST:	// go to phase 1
                case RSP_NEWCON_REQ:	// process the request using a new con
		    response.getInputStream().close();
		    if (handle_trailers) invokeTrailerHandlers(true);
		    if (request.internal_subrequest)  return true;
		    request.getConnection().
				handleRequest(request, this, response, true);
		    if (initialized)  break doModules;

                    idx = -1;
		    continue doModules;

                case RSP_SEND:		// send the request immediately
                case RSP_NEWCON_SND:	// send the request using a new con
		    response.getInputStream().close();
		    if (handle_trailers) invokeTrailerHandlers(true);
		    if (request.internal_subrequest)  return true;
		    request.getConnection().
				handleRequest(request, this, response, false);
                    idx = -1;
		    continue doModules;

                default:                // not valid
                    throw new Error("HTTPClient Internal Error: invalid status"+
                                    " " + sts + " returned by module " +
                                    modules[idx].getClass().getName());
	    }
	}

	Phase3: for (int idx=0; idx<modules.length && !aborted; idx++)
	{
            modules[idx].responsePhase3Handler(response, request);
	}

	break doModules;
	}

	/* force a read on the response in case none of the modules did */
	response.getStatusCode();

	/* all done, so copy data */
	if (!request.internal_subrequest)
	    init(response);

	if (handle_trailers)
	    invokeTrailerHandlers(false);

	return false;
    }


    /**
     * Copies the relevant fields from Response and marks this as initialized.
     *
     * @param resp the Response class to copy from
     */
    void init(Response resp)
    {
	if (initialized)  return;

	this.StatusCode    = resp.StatusCode;
	this.ReasonLine    = resp.ReasonLine;
	this.Version       = resp.Version;
	this.EffectiveURI  = resp.EffectiveURI;
	this.ContentLength = resp.ContentLength;
	this.Headers       = resp.Headers;
	this.inp_stream    = resp.inp_stream;
	this.Data          = resp.Data;
	initialized        = true;
    }


    private boolean handle_trailers  = false;
    private boolean trailers_handled = false;

    /**
     * This is invoked by the RespInputStream when it is close()'d. It
     * just invokes the trailer handler in each module.
     *
     * @param force invoke the handlers even if not initialized yet?
     * @exception IOException     if thrown by any module
     * @exception ModuleException if thrown by any module
     */
    void invokeTrailerHandlers(boolean force)
	    throws IOException, ModuleException
    {
	if (trailers_handled)  return;

	if (!force  &&  !initialized)
	{
	    handle_trailers = true;
	    return;
	}

	for (int idx=0; idx<modules.length && !aborted; idx++)
	{
            modules[idx].trailerHandler(response, request);
	}

	trailers_handled = true;
    }


    /**
     * Mark this request as having been aborted. It's invoked by
     * HTTPConnection.stop().
     */
    void markAborted()
    {
	aborted = true;
    }


    /**
     * Gets any trailers from the response if we haven't already done so.
     */
    private synchronized void getTrailers()  throws IOException, ModuleException
    {
	if (got_trailers)  return;
	if (!initialized)  handleResponse();

	response.getTrailer("Any");
	Trailers = response.Trailers;
	got_trailers = true;

	invokeTrailerHandlers(false);
    }


    /**
     * Reads the response data received. Does not return until either
     * Content-Length bytes have been read or EOF is reached.
     *
     * @inp       the input stream from which to read the data
     * @exception IOException if any read on the input stream fails
     */
    private void readResponseData(InputStream inp)
	    throws IOException, ModuleException
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


    int getTimeout()
    {
	return timeout;
    }
}

