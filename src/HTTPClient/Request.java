/*
 * @(#)Request.java					0.3-2 18/06/1999
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


/**
 * This class represents an http request. It's used by classes which
 * implement the HTTPClientModule interface.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public final class Request implements RoRequest
{
    /** null headers */
    private static final NVPair[] empty = new NVPair[0];

    /** the current HTTPConnection */
    private HTTPConnection connection;

    /** the request method to be used (e.g. GET, POST, etc) */
    private String         method;

    /** the request-uri */
    private String         req_uri;

    /** the headers to be used */
    private NVPair[]       headers;

    /** the entity (if any) */
    private byte[]         data;

    /** or an output stream on which the entity will be written */
    private HttpOutputStream stream;

    /** are modules allowed to popup windows or otherwise prompt user? */
    private boolean        allow_ui;

    /** number of millisecs to wait for an error from the server before sending
	the entity (used when retrying requests) */
            long           delay_entity = 0;

    /** number of retries so far */
            int            num_retries = 0;

    /** disable pipelining of following request */
            boolean        dont_pipeline = false;

    /** was this request aborted by the user? */
            boolean        aborted = false;

    /** is this an internally generated subrequest? */
            boolean        internal_subrequest = false;


    // Constructors

    /**
     * Creates a new request structure.
     *
     * @param con      the current HTTPConnection
     * @param method   the request method
     * @param req_uri  the request-uri
     * @param headers  the request headers
     * @param data     the entity as a byte[]
     * @param stream   the entity as a stream
     * @param allow_ui allow user interaction
     */
    public Request(HTTPConnection con, String method, String req_uri,
		   NVPair[] headers, byte[] data, HttpOutputStream stream,
		   boolean allow_ui)
    {
	this.connection = con;
	this.method     = method;
	setRequestURI(req_uri);
	setHeaders(headers);
	this.data       = data;
	this.stream     = stream;
	this.allow_ui   = allow_ui;
    }


    // Methods

    /**
     * @return the HTTPConnection this request is associated with
     */
    public HTTPConnection getConnection()
    {
	return connection;
    }

    /**
     * @param con the HTTPConnection this request is associated with
     */
    public void setConnection(HTTPConnection  con)
    {
	this.connection = con;
    }


    /**
     * @return the request method
     */
    public String getMethod()
    {
	return method;
    }

    /**
     * @param method the request method (e.g. GET, POST, etc)
     */
    public void setMethod(String method)
    {
	this.method = method;
    }


    /**
     * @return the request-uri
     */
    public String getRequestURI()
    {
	return req_uri;
    }

    /**
     * @param req_uri the request-uri
     */
    public void setRequestURI(String req_uri)
    {
	if (req_uri != null  &&  req_uri.trim().length() > 0)
	{
	    req_uri = req_uri.trim();
	    if (req_uri.charAt(0) != '/'  &&  !req_uri.equals("*"))
		req_uri = "/" + req_uri;
	    this.req_uri = req_uri;
	}
	else
	    this.req_uri = "/";
    }


    /**
     * @return the headers making up this request
     */
    public NVPair[] getHeaders()
    {
	return headers;
    }

    /**
     * @param headers the headers for this request
     */
    public void setHeaders(NVPair[] headers)
    {
	if (headers != null)
	    this.headers = headers;
	else
	    this.headers = empty;
    }


    /**
     * @return the body of this request
     */
    public byte[] getData()
    {
	return data;
    }

    /**
     * @param data the entity for this request
     */
    public void setData(byte[] data)
    {
	this.data = data;
    }


    /**
     * @return the output stream on which the body is written
     */
    public HttpOutputStream getStream()
    {
	return stream;
    }

    /**
     * @param stream an output stream on which the entity is written
     */
    public void setStream(HttpOutputStream stream)
    {
	this.stream = stream;
    }


    /**
     * @return true if the modules or handlers for this request may popup
     *         windows or otherwise interact with the user
     */
    public boolean allowUI()
    {
	return allow_ui;
    }

    /**
     * @param allow_ui are modules and handlers allowed to popup windows or
     *                otherwise interact with the user?
     */
    public void setAllowUI(boolean allow_ui)
    {
	this.allow_ui = allow_ui;
    }


    /**
     * @return a string containing the method and request-uri
     */
    public String toString()
    {
	return getClass().getName() + ": " + method + " " + req_uri;
    }
}

