/*
 * @(#)HttpURLConnection.java				0.3-2 18/06/1999
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

import java.net.URL;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * This class is a wrapper around HTTPConnection providing the interface
 * defined by java.net.URLConnection and java.net.HttpURLConnection.
 *
 * <P>This class can be used to replace the HttpClient in the JDK with this
 * HTTPClient by defining the property
 * <code>java.protocol.handler.pkgs=HTTPClient</code>.
 *
 * <P>One difference between Sun's HttpClient and this one is that this
 * one will provide you with a real output stream if possible. This leads
 * to two changes: you should set the request property "Content-Length",
 * if possible, before invoking getOutputStream(); and in many cases
 * getOutputStream() implies connect(). This should be transparent, though,
 * apart from the fact that you can't change any headers or other settings
 * anymore once you've gotten the output stream.
 * So, for large data do:
 * <PRE>
 *   HttpURLConnection con = (HttpURLConnection) url.openConnection();
 *
 *   con.setDoOutput(true);
 *   con.setRequestProperty("Content-Length", ...);
 *   OutputStream out = con.getOutputStream();
 *
 *   out.write(...);
 *   out.close();
 *
 *   if (con.getResponseCode() != 200)
 *       ...
 * </PRE>
 *
 * <P>The HTTPClient will send the request data using the chunked transfer
 * encoding when no Content-Length is specified and the server is HTTP/1.1
 * compatible. Because cgi-scripts can't usually handle this, you may
 * experience problems trying to POST data. For this reason, whenever
 * the Content-Type is application/x-www-form-urlencoded getOutputStream()
 * will buffer the data before sending it so as prevent chunking. If you
 * are sending requests with a different Content-Type and are experiencing
 * problems then you may want to try setting the system property
 * <var>HTTPClient.dontChunkRequests</var> to <var>true</var> (this needs
 * to be done either on the command line or somewhere in the code before
 * the first URLConnection.openConnection() is invoked).
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.3
 */

public class HttpURLConnection extends java.net.HttpURLConnection
			       implements GlobalConstants
{
    /** a list of HTTPConnections */
    private static Hashtable  connections = new Hashtable();

    /** the current connection */
    private HTTPConnection    con;

    /** the resource */
    private String            resource;

    /** the current method */
    private String            method;

    /** has the current method been set via setRequestMethod()? */
    private boolean           method_set;

    /** the default request headers */
    private static NVPair[]   default_headers = new NVPair[0];

    /** the request headers */
    private NVPair[]          headers;

    /** the response */
    private HTTPResponse      resp;

    /** is the redirection module activated for this instance? */
    private boolean           do_redir;

    /** the RedirectionModule class */
    private static Class      redir_mod;

    /** the output stream used for POST and PUT */
    private OutputStream      output_stream;


    static
    {
	// The default allowUserAction in java.net.URLConnection is
	// false.
	try
	{
	    if (Boolean.getBoolean("HTTPClient.HttpURLConnection.AllowUI"))
		setDefaultAllowUserInteraction(true);
	}
	catch (SecurityException se)
	    { }

	// get the RedirectionModule class
	try
	    { redir_mod = Class.forName("HTTPClient.RedirectionModule"); }
	catch (ClassNotFoundException cnfe)
	    { throw new NoClassDefFoundError(cnfe.getMessage()); }

	// Set the User-Agent if the http.agent property is set
	try
	{
	    String agent = System.getProperty("http.agent");
	    if (agent != null)
		setDefaultRequestProperty("User-Agent", agent);
	}
	catch (SecurityException se)
	    { }
    }


    // Constructors

    private static String non_proxy_hosts = "";
    private static String proxy_host = "";
    private static int    proxy_port = -1;

    /**
     * Construct a connection to the specified url. A cache of
     * HTTPConnections is used to maximize the reuse of these across
     * multiple HttpURLConnections.
     *
     * <BR>The default method is "GET".
     *
     * @param url the url of the request
     * @exception ProtocolNotSuppException if the protocol is not supported
     */
    public HttpURLConnection(URL url)
	    throws ProtocolNotSuppException, IOException
    {
	super(url);

	// first read proxy properties and set
        try
        {
            String hosts = System.getProperty("http.nonProxyHosts", "");
	    if (!hosts.equalsIgnoreCase(non_proxy_hosts))
	    {
		connections.clear();
		non_proxy_hosts = hosts;
		String[] list = Util.splitProperty(hosts);
		for (int idx=0; idx<list.length; idx++)
		    HTTPConnection.dontProxyFor(list[idx]);
	    }
        }
        catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
        catch (SecurityException se)
            { }

	try
	{
	    String host = System.getProperty("http.proxyHost", "");
	    int port = Integer.getInteger("http.proxyPort", -1).intValue();
	    if (!host.equalsIgnoreCase(proxy_host)  ||  port != proxy_port)
	    {
		connections.clear();
		proxy_host = host;
		proxy_port = port;
		HTTPConnection.setProxyServer(host, port);
	    }
	}
	catch (SecurityException se)
	    { }

	// now setup stuff
	con           = getConnection(url);
	method        = "GET";
	method_set    = false;
	resource      = url.getFile();
	headers       = default_headers;
	do_redir      = getFollowRedirects();
	output_stream = null;
    }


    /**
     * Returns an HTTPConnection. A cache of connections is kept and first
     * consulted; only when the cache lookup fails is a new one created
     * and added to the cache.
     *
     * @param url the url
     * @return an HTTPConnection
     * @exception ProtocolNotSuppException if the protocol is not supported
     */
    private HTTPConnection getConnection(URL url)
	    throws ProtocolNotSuppException
    {
	// try the cache, using the host name

	String php = url.getProtocol() + ":" + url.getHost() + ":" +
		     ((url.getPort() != -1) ? url.getPort() :
					URI.defaultPort(url.getProtocol()));
	php = php.toLowerCase();

	HTTPConnection con = (HTTPConnection) connections.get(php);
	if (con != null)  return con;


	// Not in cache, so create new one and cache it

	con = new HTTPConnection(url);
	connections.put(php, con);

	return con;
    }


    // Methods

    /**
     * Sets the request method (e.g. "PUT" or "HEAD"). Can only be set
     * before connect() is called.
     *
     * @param method the http method.
     * @exception ProtocolException if already connected.
     */
    public void setRequestMethod(String method)  throws ProtocolException
    {
	if (connected)
	    throw new ProtocolException("Already connected!");

	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Setting request method: " +
			       method);

	this.method = method.trim().toUpperCase();
	method_set  = true;
    }


    /**
     * Return the request method used.
     *
     * @return the http method.
     */
    public String getRequestMethod()
    {
	return method;
    }


    /**
     * Get the response code. Calls connect() if not connected.
     *
     * @return the http response code returned.
     */
    public int getResponseCode()  throws IOException
    {
	if (!connected)  connect();

	try
	    { return resp.getStatusCode(); }
	catch (ModuleException me)
	    { throw new IOException(me.toString()); }
    }


    /**
     * Get the response message describing the response code. Calls connect()
     * if not connected.
     *
     * @return the http response message returned with the response code.
     */
    public String getResponseMessage()  throws IOException
    {
	if (!connected)  connect();

	try
	    { return resp.getReasonLine(); }
	catch (ModuleException me)
	    { throw new IOException(me.toString()); }
    }


    /**
     * Get the value part of a header. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @return the value of the header, or null if no such header was returned.
     */
    public String getHeaderField(String name)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeader(name);
	}
	catch (Exception e)
	    { return null; }
    }


    /**
     * Get the value part of a header and converts it to an int. If the
     * header does not exist or if its value could not be converted to an
     * int then the default is returned. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @param  def  the default value to return in case of an error.
     * @return the value of the header, or null if no such header was returned.
     */
    public int getHeaderFieldInt(String name, int def)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeaderAsInt(name);
	}
	catch (Exception e)
	    { return def; }
    }


    /**
     * Get the value part of a header, interprets it as a date and converts
     * it to a long representing the number of milliseconds since 1970. If
     * the header does not exist or if its value could not be converted to a
     * date then the default is returned. Calls connect() if not connected.
     *
     * @param  name the of the header.
     * @param  def  the default value to return in case of an error.
     * @return the value of the header, or def in case of an error.
     */
    public long getHeaderFieldDate(String name, long def)
    {
	try
	{
	    if (!connected)  connect();
	    return resp.getHeaderAsDate(name).getTime();
	}
	catch (Exception e)
	    { return def; }
    }


    private String[] hdr_keys, hdr_values;

    /**
     * Gets header name of the n-th header. Calls connect() if not connected.
     * The name of the 0-th header is <var>null</var>, even though it the
     * 0-th header has a value.
     *
     * @param n which header to return.
     * @return the header name, or null if not that many headers.
     */
    public String getHeaderFieldKey(int n)
    {
	if (hdr_keys == null)
	    fill_hdr_arrays();

	if (n >= 0  &&  n < hdr_keys.length)
	    return hdr_keys[n];
	else
	    return null;
    }


    /**
     * Gets header value of the n-th header. Calls connect() if not connected.
     * The value of 0-th header is the Status-Line (e.g. "HTTP/1.1 200 Ok").
     *
     * @param n which header to return.
     * @return the header value, or null if not that many headers.
     */
    public String getHeaderField(int n)
    {
	if (hdr_values == null)
	    fill_hdr_arrays();

	if (n >= 0  &&  n < hdr_values.length)
	    return hdr_values[n];
	else
	    return null;
    }


    /**
     * Cache the list of headers.
     */
    private void fill_hdr_arrays()
    {
	try
	{
	    if (!connected)  connect();

	    // count number of headers
	    int num = 1;
	    Enumeration enum = resp.listHeaders();
	    while (enum.hasMoreElements())
	    {
		num++;
		enum.nextElement();
	    }

	    // allocate arrays
	    hdr_keys   = new String[num];
	    hdr_values = new String[num];

	    // fill arrays
	    enum = resp.listHeaders();
	    for (int idx=1; idx<num; idx++)
	    {
		hdr_keys[idx]   = (String) enum.nextElement();
		hdr_values[idx] = resp.getHeader(hdr_keys[idx]);
	    }

	    // the 0'th field is special
	    hdr_values[0] = resp.getVersion() + " " + resp.getStatusCode() +
			    " " + resp.getReasonLine();
	}
	catch (Exception e)
	    { hdr_keys = hdr_values = new String[0]; }
    }


    /**
     * Gets an input stream from which the data in the response may be read.
     * Calls connect() if not connected.
     *
     * @return an InputStream
     * @exception ProtocolException if input not enabled.
     * @see java.net.URLConnection#setDoInput(boolean)
     */
    public InputStream getInputStream()  throws IOException
    {
	if (!doInput)
	    throw new ProtocolException("Input not enabled! (use setDoInput(true))");

	if (!connected)  connect();

	InputStream stream;
	try
	    { stream = resp.getInputStream(); }
	catch (ModuleException e)
	    { throw new IOException(e.toString()); }

	return stream;
    }


    /**
     * Returns the error stream if the connection failed
     * but the server sent useful data nonetheless.
     *
     * <P>This method will not cause a connection to be initiated.
     *
     * @return an InputStream, or null if either the connection hasn't
     *         been established yet or no error occured
     * @see java.net.HttpURLConnection#getErrorStream()
     * @since V0.3-1
     */
    public InputStream getErrorStream()
    {
	try
	{
	    if (!doInput  ||  !connected  ||  resp.getStatusCode() < 300  ||
		resp.getHeaderAsInt("Content-length") <= 0)
		return null;

	    return resp.getInputStream();
	}
	catch (Exception e)
	    { return null; }
    }


    /**
     * Gets an output stream which can be used send an entity with the
     * request. Can be called multiple times, in which case always the
     * same stream is returned.
     *
     * <P>The default request method changes to "POST" when this method is
     * called. Cannot be called after connect().
     *
     * <P>If no Content-type has been set it defaults to
     * <var>application/x-www-form-urlencoded</var>. Furthermore, if the
     * Content-type is <var>application/x-www-form-urlencoded</var> then all
     * output will be collected in a buffer before sending it to the server;
     * otherwise an HttpOutputStream is used.
     *
     * @return an OutputStream
     * @exception ProtocolException if already connect()'ed, if output is not
     *                              enabled or if the request method does not
     *                              support output.
     * @see java.net.URLConnection#setDoOutput(boolean)
     * @see HTTPClient.HttpOutputStream
     */
    public synchronized OutputStream getOutputStream()  throws IOException
    {
	if (connected)
	    throw new ProtocolException("Already connected!");

	if (!doOutput)
	    throw new ProtocolException("Output not enabled! (use setDoOutput(true))");
	if (!method_set)
	    method = "POST";
	else if (method.equals("HEAD")  ||  method.equals("GET")  ||
		 method.equals("TRACE"))
	    throw new ProtocolException("Method "+method+" does not support output!");

	if (getRequestProperty("Content-type") == null)
	    setRequestProperty("Content-type", "application/x-www-form-urlencoded");

	if (output_stream == null)
	{
	    if (DebugURLC)
		System.err.println("URLC:  (" +url+ ") creating output stream");

	    String cl = getRequestProperty("Content-Length");
	    if (cl != null)
		output_stream = new HttpOutputStream(Integer.parseInt(cl));
	    else
	    {
		// Hack: because of restrictions when using true output streams
		// and because form-data is usually quite limited in size, we
		// first collect all data before sending it if this is
		// form-data.
		if (getRequestProperty("Content-type").equals(
			"application/x-www-form-urlencoded"))
		    output_stream = new ByteArrayOutputStream(300);
		else
		    output_stream = new HttpOutputStream();
	    }

	    if (output_stream instanceof HttpOutputStream)
		connect();
	}

	return output_stream;
    }


    /**
     * Gets the url for this connection. If we're connect()'d and the request
     * was redirected then the url returned is that of the final request.
     *
     * @return the final url, or null if any exception occured.
     */
    public URL getURL()
    {
	if (connected)
	{
	    try
	    {
		if (resp.getEffectiveURL() != null)
		    return resp.getEffectiveURL();
	    }
	    catch (Exception e)
		{ return null; }
	}

	return url;
    }


    /**
     * Sets the <var>If-Modified-Since</var> header.
     *
     * @param time the number of milliseconds since 1970.
     */
    public void setIfModifiedSince(long time)
    {
	super.setIfModifiedSince(time);
	setRequestProperty("If-Modified-Since", Util.httpDate(new Date(time)));
    }


    /**
     * Sets an arbitrary request header.
     *
     * @param name  the name of the header.
     * @param value the value for the header.
     */
    public void setRequestProperty(String name, String value)
    {
	if (DebugURLC)
	    System.err.println("URLC:  (" +url+ ") Setting request property: " +
			       name + " : " + value);

	int idx;
	for (idx=0; idx<headers.length; idx++)
	{
	    if (headers[idx].getName().equalsIgnoreCase(name))
		break;
	}

	if (idx == headers.length)
	    headers = Util.resizeArray(headers, idx+1);

	headers[idx] = new NVPair(name, value);
    }


    /**
     * Gets the value of a given request header.
     *
     * @param name  the name of the header.
     * @return the value part of the header, or null if no such header.
     */
    public String getRequestProperty(String name)
    {
	for (int idx=0; idx<headers.length; idx++)
	{
	    if (headers[idx].getName().equalsIgnoreCase(name))
		return headers[idx].getValue();
	}

	return null;
    }


    /**
     * Sets an arbitrary default request header. All headers set here are
     * automatically sent with each request.
     *
     * @param name  the name of the header.
     * @param value the value for the header.
     */
    public static void setDefaultRequestProperty(String name, String value)
    {
	if (DebugURLC)
	    System.err.println("URLC:  Setting default request property: " +
			       name + " : " + value);

	int idx;
	for (idx=0; idx<default_headers.length; idx++)
	{
	    if (default_headers[idx].getName().equalsIgnoreCase(name))
		break;
	}

	if (idx == default_headers.length)
	    default_headers = Util.resizeArray(default_headers, idx+1);

	default_headers[idx] = new NVPair(name, value);
    }


    /**
     * Gets the value for a given default request header.
     *
     * @param name  the name of the header.
     * @return the value part of the header, or null if no such header.
     */
    public static String getDefaultRequestProperty(String name)
    {
	for (int idx=0; idx<default_headers.length; idx++)
	{
	    if (default_headers[idx].getName().equalsIgnoreCase(name))
		return default_headers[idx].getValue();
	}

	return null;
    }


    /**
     * Enables or disables the automatic handling of redirection responses
     * for this instance only. Cannot be called after <code>connect()</code>.
     *
     * @param set enables automatic redirection handling if true.
     */
    public void setInstanceFollowRedirects(boolean set)
    {
	if (connected)
	    throw new IllegalStateException("Already connected!");

	do_redir = set;
    }


    /**
     * @return true if automatic redirection handling for this instance is
     *              enabled.
     */
    public boolean getInstanceFollowRedirects()
    {
	return do_redir;
    }


    /**
     * Connects to the server (if connection not still kept alive) and
     * issues the request.
     */
    public synchronized void connect()  throws IOException
    {
	if (connected)  return;

	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Connecting ...");

	// useCaches TBD!!!

	synchronized(con)
	{
	    con.setAllowUserInteraction(allowUserInteraction);
	    if (do_redir)
		con.addModule(redir_mod, 2);
	    else
		con.removeModule(redir_mod);

	    try
	    {
		if (output_stream instanceof ByteArrayOutputStream)
		    resp = con.ExtensionMethod(method, resource,
			((ByteArrayOutputStream) output_stream).toByteArray(),
					     headers);
		else
		    resp = con.ExtensionMethod(method, resource,
				    (HttpOutputStream) output_stream, headers);
	    }
	    catch (ModuleException e)
		{ throw new IOException(e.toString()); }
	}

	connected = true;
    }


    /**
     * Closes all the connections to this server.
     */
    public void disconnect()
    {
	if (DebugURLC)
	    System.err.println("URLC:  (" + url + ") Disconnecting ...");

	con.stop();
    }


    /**
     * Shows if request are being made through an http proxy or directly.
     *
     * @return true if an http proxy is being used.
     */
    public boolean usingProxy()
    {
	return (con.getProxyHost() != null);
    }


    /**
     * produces a string.
     * @return a string containing the HttpURLConnection
     */
    public String toString()
    {
	return getClass().getName() + "[" + url + "]";
    }
}

