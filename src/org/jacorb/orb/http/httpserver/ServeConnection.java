/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.orb.http.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class ServeConnection
    {

    private Socket socket;

    private ServeInputStream in;
    private ServeOutputStream out;

    private Vector cookies = new Vector(); // !!!


    public static final int SC_CONTINUE = 100;
    public static final int SC_SWITCHING_PROTOCOLS = 101;
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int SC_NO_CONTENT = 204;
    public static final int SC_RESET_CONTENT = 205;
    public static final int SC_PARTIAL_CONTENT = 206;
    public static final int SC_MULTIPLE_CHOICES = 300;
    public static final int SC_MOVED_PERMANENTLY = 301;
    public static final int SC_MOVED_TEMPORARILY = 302;
    public static final int SC_SEE_OTHER = 303;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_USE_PROXY = 305;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_PAYMENT_REQUIRED = 402;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_NOT_ACCEPTABLE = 406;
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int SC_REQUEST_TIMEOUT = 408;
    public static final int SC_CONFLICT = 409;
    public static final int SC_GONE = 410;
    public static final int SC_LENGTH_REQUIRED = 411;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int SC_REQUEST_URI_TOO_LONG = 414;
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_GATEWAY_TIMEOUT = 504;
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;



    /// Constructor.
    public ServeConnection( Socket socket,java.io.InputStream _in)
	{
	// Save arguments.
	this.socket = socket;
	try
	    {
	    // Get the streams.
	    in =  new ServeInputStream(_in);
	    out = new ServeOutputStream( socket.getOutputStream(), this );
	    }
	catch ( IOException e )
	    {
	    problem( "Getting streams: " + e.getMessage(), SC_BAD_REQUEST );
	    }

	parseRequest();

	
	//closing must be done by BA
	//try
	//   {
	//  socket.close();
	//  }
	//catch ( IOException e ) { /* ignore */ }

	}


    // Methods from Runnable.

    private String reqMethod = null;
    private String reqUriPath = null;
    private String reqProtocol = null;
    private boolean oneOne;		// HTTP/1.1 or better
    private boolean reqMime;
    String reqQuery = null;
    private Vector reqHeaderNames = new Vector();
    private Vector reqHeaderValues = new Vector();


    public  void close(){
	
	try
	    {
	    socket.close();
	    }
	catch ( IOException e ) { /* ignore */ }
    }

    private void parseRequest()
	{
	byte[] lineBytes = new byte[4096];
	int len;
	String line;

	try
	    {
	    // Read the first line of the request.
	    len = in.readLine( lineBytes, 0, lineBytes.length );
	    if ( len == -1 || len == 0 )
		{
		problem( "Empty request", SC_BAD_REQUEST );
		return;
		}
	    line = new String( lineBytes, 0, len );
	    String[] tokens = splitStr( line );
	    switch ( tokens.length )
		{
		case 2:
		// Two tokens means the protocol is HTTP/0.9.
		reqProtocol = "HTTP/0.9";
		oneOne = false;
		reqMime = false;
		break;
		case 3:
		reqProtocol = tokens[2];
		oneOne = ! reqProtocol.toUpperCase().equals( "HTTP/1.0" );
		reqMime = true;
		// Read the rest of the lines.
		while ( true )
		    {
		    len = in.readLine( lineBytes, 0, lineBytes.length );
		    if ( len == -1 || len == 0 )
			break;
		    line = new String( lineBytes, 0, len );
		    int colonBlank = line.indexOf( ": " );
		    if ( colonBlank != -1 )
			{
			String name = line.substring( 0, colonBlank );
			String value = line.substring( colonBlank + 2 );
			reqHeaderNames.addElement( name.toLowerCase() );
			reqHeaderValues.addElement( value );
			}
		    }
		break;
		default:
		problem( "Malformed request line", SC_BAD_REQUEST );
		return;
		}
	    reqMethod = tokens[0];
	    reqUriPath = tokens[1];

	    // Check Host: header in HTTP/1.1 requests.
	    if ( oneOne )
		{
		String host = getHeader( "host" );
		if ( host == null )
		    {
		    problem(
			"Host header missing on HTTP/1.1 request",
			SC_BAD_REQUEST );
		    return;
		    }
		// !!!
		}

	    // Split off query string, if any.
	    int qmark = reqUriPath.indexOf( '?' );
	    if ( qmark != -1 )
		{
		reqQuery = reqUriPath.substring( qmark + 1 );
		reqUriPath = reqUriPath.substring( 0, qmark );
		}

	    // Decode %-sequences.
	    reqUriPath = decode( reqUriPath );

	     //you can handle the request now
	}catch ( Exception e )
	    {
	    problem( "Reading request: " + e.getMessage(), SC_BAD_REQUEST );
	    }
	}

    public void answerRequest(byte[] answer)
	{
	// Set default response fields.
	setStatus( SC_OK );
	setDateHeader( "Date", System.currentTimeMillis() );
	setHeader( "Server", "JacORB builtin HTTPServer");
	setHeader( "Connection", "close" );
	try
	    {
		out.write(answer,0,answer.length);	
		out.flush();
		out.close();
	    }
	catch ( Exception e )
	    {
	    problem(
		"unexpected problem running servlet: " + e.toString(),
		SC_INTERNAL_SERVER_ERROR );
	    }
	}

    private void problem( String logMessage, int resCode )
	{
	try
	    {
	    sendError( resCode );
	    }
	catch ( IOException e ) { /* ignore */ }
	}

    private String decode( String str )
	{
	StringBuffer result = new StringBuffer();
	int l = str.length();
	for ( int i = 0; i < l; ++i )
	    {
	    char c = str.charAt( i );
	    if ( c == '%' && i + 2 < l )
		{
		char c1 = str.charAt( i + 1 );
		char c2 = str.charAt( i + 2 );
		if ( isHexit( c1 ) && isHexit( c2 ) )
		    {
		    result.append( (char) ( hexit( c1 ) * 16 + hexit( c2 ) ) );
		    i += 2;
		    }
		else
		    result.append( c );
		}
	    else
		result.append( c );
	    }
	return result.toString();
	}

    private boolean isHexit( char c )
	{
	String legalChars = "0123456789abcdefABCDEF";
	return ( legalChars.indexOf( c ) != -1 );
	}

    private int hexit( char c )
	{
	if ( c >= '0' && c <= '9' )
	    return c - '0';
	if ( c >= 'a' && c <= 'f' )
	    return c - 'a' + 10;
	if ( c >= 'A' && c <= 'F' )
	    return c - 'A' + 10;
	return 0;	// shouldn't happen, we're guarded by isHexit()
	}


    // Methods from ServletRequest.

    /// Returns the size of the request entity data, or -1 if not known.
    // Same as the CGI variable CONTENT_LENGTH.
    public int getContentLength()
	{
	return getIntHeader( "content-length", -1 );
	}

    /// Returns the MIME type of the request entity data, or null if
    // not known.
    // Same as the CGI variable CONTENT_TYPE.
    public String getContentType()
	{
	return getHeader( "content-type" );
	}

    /// Returns the protocol and version of the request as a string of
    // the form <protocol>/<major version>.<minor version>.
    // Same as the CGI variable SERVER_PROTOCOL.
    public String getProtocol()
	{
	return reqProtocol;
	}

    ///  Returns the scheme of the URL used in this request, for example
    // "http", "https", or "ftp".  Different schemes have different rules
    // for constructing URLs, as noted in RFC 1738.  The URL used to create
    // a request may be reconstructed using this scheme, the server name
    // and port, and additional information such as URIs.
    public String getScheme()
	{
	return "http";
	}

    /// Returns the host name of the server as used in the <host> part of
    // the request URI.
    // Same as the CGI variable SERVER_NAME.
    public String getServerName()
	{
	try
	    {
	    return InetAddress.getLocalHost().getHostName();
	    }
	catch ( UnknownHostException e )
	    {
	    return null;
	    }
	}

    /// Returns the port number on which this request was received as used in
    // the <port> part of the request URI.
    // Same as the CGI variable SERVER_PORT.
    public int getServerPort()
	{
	return socket.getLocalPort();
	}

    /// Returns the IP address of the agent that sent the request.
    // Same as the CGI variable REMOTE_ADDR.
    public String getRemoteAddr()
	{
	return socket.getInetAddress().toString();
	}

    /// Returns the fully qualified host name of the agent that sent the
    // request.
    // Same as the CGI variable REMOTE_HOST.
    public String getRemoteHost()
	{
	return socket.getInetAddress().getHostName();
	}

    /// Applies alias rules to the specified virtual path and returns the
    // corresponding real path, or null if the translation can not be
    // performed for any reason.  For example, an HTTP servlet would
    // resolve the path using the virtual docroot, if virtual hosting is
    // enabled, and with the default docroot otherwise.  Calling this
    // method with the string "/" as an argument returns the document root.
    public String getRealPath( String path )
	{
	return "/"; //serve.getRealPath( path );
	}

    /// Returns an input stream for reading request data.
    // @exception IllegalStateException if getReader has already been called
    // @exception IOException on other I/O-related errors
    public ServeInputStream getInputStream() throws IOException
	{
	return in;
	}

    /// Returns a buffered reader for reading request data.
    // @exception UnsupportedEncodingException if the character set encoding isn't supported
    // @exception IllegalStateException if getInputStream has already been called
    // @exception IOException on other I/O-related errors
    public BufferedReader getReader()
	{
	// !!!
	return null;
	}

    Vector queryNames = null;
    Vector queryValues = null;

    /// Returns the parameter names for this request.
    public Enumeration getParameterNames()
	{
	if ( queryNames == null )
	    {
	    queryNames = new Vector();
	    queryValues = new Vector();
	    String qs = getQueryString();
	    if ( qs != null )
		{
		Enumeration en = new StringTokenizer( qs, "&" );
		while ( en.hasMoreElements() )
		    {
		    String nv = (String) en.nextElement();
		    int eq = nv.indexOf( '=' );
		    String name, value;
		    if ( eq == -1 )
			{
			name = nv;
			value = "";
			}
		    else
			{
			name = nv.substring( 0, eq );
			value = nv.substring( eq + 1 );
			}
		    queryNames.addElement( name );
		    queryValues.addElement( value );
		    }
		}
	    }
	return queryNames.elements();
	}

    /// Returns the value of the specified query string parameter, or null
    // if not found.
    // @param name the parameter name
    public String getParameter( String name )
	{
	Enumeration en = getParameterNames();
	int i = queryNames.indexOf( name );
	if ( i == -1 )
	    return null;
	else
	    return (String) queryValues.elementAt( i );
	}

    /// Returns the values of the specified parameter for the request as an
    // array of strings, or null if the named parameter does not exist.
    public String[] getParameterValues( String name )
	{
	Vector v = new Vector();
	Enumeration en = getParameterNames();
	for ( int i = 0; i < queryNames.size(); ++i )
	    {
	    String n = (String) queryNames.elementAt( i );
	    if ( name.equals( n ) )
		v.addElement( queryValues.elementAt( i ) );
	    }
	if ( v.size() == 0 )
	    return null;
	String[] vArray = new String[v.size()];
	v.copyInto( vArray );
	return vArray;
	}
    
    /// Returns the value of the named attribute of the request, or null if
    // the attribute does not exist.  This method allows access to request
    // information not already provided by the other methods in this interface.
    public Object getAttribute( String name )
	{
	// This server does not implement attributes.
	return null;
	}


    // Methods from HttpServletRequest.

    /// Gets the array of cookies found in this request.
    //public Cookie[] getCookies()
//	{
//	Cookie[] cookieArray = new Cookie[cookies.size()];
//	cookies.copyInto( cookieArray );
//	return cookieArray;
//	}

    /// Returns the method with which the request was made. This can be "GET",
    // "HEAD", "POST", or an extension method.
    // Same as the CGI variable REQUEST_METHOD.
    public String getMethod()
	{
	return reqMethod;
	}

    /// Returns the full request URI.
    public String getRequestURI()
	{
	String portPart = "";
	int port = getServerPort();
	if ( port != 80 )
	    portPart = ":" + port;
	String queryPart = "";
	String queryString = getQueryString();
	if ( queryString != null && queryString.length() > 0 )
	    queryPart = "?" + queryString;
	return "http://" + getServerName() + portPart + reqUriPath + queryPart;
	}

    /// Returns the part of the request URI that referred to the servlet being
    // invoked.
    // Analogous to the CGI variable SCRIPT_NAME.
    public String getServletPath()
	{
	// In this server, the entire path is regexp-matched against the
	// servlet pattern, so there's no good way to distinguish which
	// part refers to the servlet.
	return reqUriPath;
	}

    /// Returns optional extra path information following the servlet path, but
    // immediately preceding the query string.  Returns null if not specified.
    // Same as the CGI variable PATH_INFO.
    public String getPathInfo()
	{
	// In this server, the entire path is regexp-matched against the
	// servlet pattern, so there's no good way to distinguish which
	// part refers to the servlet.
	return null;
	}

    /// Returns extra path information translated to a real path.  Returns
    // null if no extra path information was specified.
    // Same as the CGI variable PATH_TRANSLATED.
    public String getPathTranslated()
	{
	// In this server, the entire path is regexp-matched against the
	// servlet pattern, so there's no good way to distinguish which
	// part refers to the servlet.
	return null;
	}

    /// Returns the query string part of the servlet URI, or null if not known.
    // Same as the CGI variable QUERY_STRING.
    public String getQueryString()
	{
	return reqQuery;
	}

    /// Returns the name of the user making this request, or null if not known.
    // Same as the CGI variable REMOTE_USER.
    public String getRemoteUser()
	{
	// This server does not support authentication, so even if a username
	// is supplied in the headers we don't want to look at it.
	return null;
	}

    /// Returns the authentication scheme of the request, or null if none.
    // Same as the CGI variable AUTH_TYPE.
    public String getAuthType()
	{
	// This server does not support authentication.
	return null;
	}

    /// Returns the value of a header field, or null if not known.
    // Same as the information passed in the CGI variabled HTTP_*.
    // @param name the header field name
    public String getHeader( String name )
	{
	int i = reqHeaderNames.indexOf( name.toLowerCase() );
	if ( i == -1 )
	    return null;
	return (String) reqHeaderValues.elementAt( i );
	}

    /// Returns the value of an integer header field.
    // @param name the header field name
    // @param def the integer value to return if header not found or invalid
    public int getIntHeader( String name, int def )
	{
	String val = getHeader( name );
	if ( val == null )
	    return def;
	try
	    {
	    return Integer.parseInt( val );
	    }
	catch ( Exception e )
	    {
	    return def;
	    }
	}

    /// Returns the value of a long header field.
    // @param name the header field name
    // @param def the long value to return if header not found or invalid
    public long getLongHeader( String name, long def )
	{
	String val = getHeader( name );
	if ( val == null )
	    return def;
	try
	    {
	    return Long.parseLong( val );
	    }
	catch ( Exception e )
	    {
	    return def;
	    }
	}

    /// Returns the value of a date header field.
    // @param name the header field name
    // @param def the date value to return if header not found or invalid
    public long getDateHeader( String name, long def )
	{
	String val = getHeader( name );
	if ( val == null )
	    return def;
	try
	    {
	    return DateFormat.getDateInstance().parse( val ).getTime();
	    }
	catch ( Exception e )
	    {
	    return def;
	    }
	}

    /// Returns an Enumeration of the header names.
    public Enumeration getHeaderNames()
	{
	return reqHeaderNames.elements();
	}

    // Session stuff.  Not implemented, but the API is here for compatibility.

    /// Gets the current valid session associated with this request, if
    // create is false or, if necessary, creates a new session for the
    // request, if create is true.
    // <P>
    // Note: to ensure the session is properly maintained, the servlet
    // developer must call this method (at least once) before any output
    // is written to the response.
    // <P>
    // Additionally, application-writers need to be aware that newly
    // created sessions (that is, sessions for which HttpSession.isNew
    // returns true) do not have any application-specific state.
//    public HttpSession getSession( boolean create )
//	{
//	return null;
//	}

    /// Gets the session id specified with this request. This may differ
    // from the actual session id.  For example, if the request specified
    // an id for an invalid session, then this will get a new session with
    // a new id.
    public String getRequestedSessionId()
	{
	return null;
	}

    /// Checks whether this request is associated with a session that is
    // valid in the current session context.  If it is not valid, the
    // requested session will never be returned from the getSession
    // method.
    public boolean isRequestedSessionIdValid()
	{
	return false;
	}

    /// Checks whether the session id specified by this request came in as
    // a cookie.  (The requested session may not be one returned by the
    // getSession method.)
    public boolean isRequestedSessionIdFromCookie()
	{
	return false;
	}

    /// Checks whether the session id specified by this request came in as
    // part of the URL.  (The requested session may not be the one returned
    // by the getSession method.)
    public boolean isRequestedSessionIdFromUrl()
	{
	return false;
	}


    // Methods from ServletResponse.

    /// Sets the content length for this response.
    // @param length the content length
    public void setContentLength( int length )
	{
	setIntHeader( "Content-length", length );
	}

    /// Sets the content type for this response.
    // @param type the content type
    public void setContentType( String type )
	{
	setHeader( "Content-type", type );
	}

    /// Returns an output stream for writing response data.
    public ServeOutputStream getOutputStream()
	{
	return out;
	}

    /// Returns a print writer for writing response data.  The MIME type of
    // the response will be modified, if necessary, to reflect the character
    // encoding used, through the charset=... property.  This means that the
    // content type must be set before calling this method.
    // @exception UnsupportedEncodingException if no such encoding can be provided
    // @exception IllegalStateException if getOutputStream has been called
    // @exception IOException on other I/O errors
    public PrintWriter getWriter() throws IOException
	{
	// !!!
	return null;
	}

    /// Returns the character set encoding used for this MIME body.  The
    // character encoding is either the one specified in the assigned
    // content type, or one which the client understands.  If no content
    // type has yet been assigned, it is implicitly set to text/plain.
    public String getCharacterEncoding()
	{
	// !!!
	return null;
	}


    // Methods from HttpServletResponse.

    /// Adds the specified cookie to the response.  It can be called
    // multiple times to set more than one cookie.
//    public void addCookie( Cookie cookie )
//	{
//	cookies.addElement( cookie );
//	}
    
    /// Checks whether the response message header has a field with the
    // specified name.
    public boolean containsHeader( String name )
	{
	return resHeaderNames.contains( name );
	}

    private int resCode = -1;
    private String resMessage = null;
    private Vector resHeaderNames = new Vector();
    private Vector resHeaderValues = new Vector();

    /// Sets the status code and message for this response.
    // @param resCode the status code
    // @param resMessage the status message
    public void setStatus( int resCode, String resMessage )
	{
	this.resCode = resCode;
	this.resMessage = resMessage;
	}

    /// Sets the status code and a default message for this response.
    // @param resCode the status code
    public void setStatus( int resCode )
	{
	switch ( resCode )
	    {
	    case SC_CONTINUE: setStatus( resCode, "Continue" ); break;
	    case SC_SWITCHING_PROTOCOLS:
	    setStatus( resCode, "Switching protocols" ); break;
	    case SC_OK: setStatus( resCode, "Ok" ); break;
	    case SC_CREATED: setStatus( resCode, "Created" ); break;
	    case SC_ACCEPTED: setStatus( resCode, "Accepted" ); break;
	    case SC_NON_AUTHORITATIVE_INFORMATION:
	    setStatus( resCode, "Non-authoritative" ); break;
	    case SC_NO_CONTENT: setStatus( resCode, "No content" ); break;
	    case SC_RESET_CONTENT: setStatus( resCode, "Reset content" ); break;
	    case SC_PARTIAL_CONTENT:
	    setStatus( resCode, "Partial content" ); break;
	    case SC_MULTIPLE_CHOICES:
	    setStatus( resCode, "Multiple choices" ); break;
	    case SC_MOVED_PERMANENTLY:
	    setStatus( resCode, "Moved permanentently" ); break;
	    case SC_MOVED_TEMPORARILY:
	    setStatus( resCode, "Moved temporarily" ); break;
	    case SC_SEE_OTHER: setStatus( resCode, "See other" ); break;
	    case SC_NOT_MODIFIED: setStatus( resCode, "Not modified" ); break;
	    case SC_USE_PROXY: setStatus( resCode, "Use proxy" ); break;
	    case SC_BAD_REQUEST: setStatus( resCode, "Bad request" ); break;
	    case SC_UNAUTHORIZED: setStatus( resCode, "Unauthorized" ); break;
	    case SC_PAYMENT_REQUIRED:
	    setStatus( resCode, "Payment required" ); break;
	    case SC_FORBIDDEN: setStatus( resCode, "Forbidden" ); break;
	    case SC_NOT_FOUND: setStatus( resCode, "Not found" ); break;
	    case SC_METHOD_NOT_ALLOWED:
	    setStatus( resCode, "Method not allowed" ); break;
	    case SC_NOT_ACCEPTABLE:
	    setStatus( resCode, "Not acceptable" ); break;
	    case SC_PROXY_AUTHENTICATION_REQUIRED:
	    setStatus( resCode, "Proxy auth required" ); break;
	    case SC_REQUEST_TIMEOUT:
	    setStatus( resCode, "Request timeout" ); break;
	    case SC_CONFLICT: setStatus( resCode, "Conflict" ); break;
	    case SC_GONE: setStatus( resCode, "Gone" ); break;
	    case SC_LENGTH_REQUIRED:
	    setStatus( resCode, "Length required" ); break;
	    case SC_PRECONDITION_FAILED:
	    setStatus( resCode, "Precondition failed" ); break;
	    case SC_REQUEST_ENTITY_TOO_LARGE:
	    setStatus( resCode, "Request entity too large" ); break;
	    case SC_REQUEST_URI_TOO_LONG:
	    setStatus( resCode, "Request URI too large" ); break;
	    case SC_UNSUPPORTED_MEDIA_TYPE:
	    setStatus( resCode, "Unsupported media type" ); break;
	    case SC_INTERNAL_SERVER_ERROR:
	    setStatus( resCode, "Internal server error" ); break;
	    case SC_NOT_IMPLEMENTED:
	    setStatus( resCode, "Not implemented" ); break;
	    case SC_BAD_GATEWAY: setStatus( resCode, "Bad gateway" ); break;
	    case SC_SERVICE_UNAVAILABLE:
	    setStatus( resCode, "Service unavailable" ); break;
	    case SC_GATEWAY_TIMEOUT:
	    setStatus( resCode, "Gateway timeout" ); break;
	    case SC_HTTP_VERSION_NOT_SUPPORTED:
	    setStatus( resCode, "HTTP version not supported" ); break;
	    default: setStatus( resCode, "" ); break;
	    }
	}

    /// Sets the value of a header field.
    // @param name the header field name
    // @param value the header field value
    public void setHeader( String name, String value )
	{
	resHeaderNames.addElement( name );
	resHeaderValues.addElement( value );
	}

    /// Sets the value of an integer header field.
    // @param name the header field name
    // @param value the header field integer value
    public void setIntHeader( String name, int value )
	{
	setHeader( name, Integer.toString( value ) );
	}

    /// Sets the value of a long header field.
    // @param name the header field name
    // @param value the header field long value
    public void setLongHeader( String name, long value )
	{
	setHeader( name, Long.toString( value ) );
	}

    /// Sets the value of a date header field.
    // @param name the header field name
    // @param value the header field date value
    public void setDateHeader( String name, long value )
	{
	setHeader( name, to1123String( new Date( value ) ) );
	}

    private static final String[] weekdays =
	{ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    /// Converts a Date into an RFC-1123 string.
    private static String to1123String( Date date )
	{
	// We have to go through some machinations here to get the
	// correct day of the week in GMT.  getDay() gives the day in
	// local time.  getDate() gives the day of the month in local
	// time.  toGMTString() gives a formatted string in GMT.  So, we
	// extract the day of the month from the GMT string, and if it
	// doesn't match the local one we change the local day of the
	// week accordingly.
	//
	// The Date class sucks.
	int localDay = date.getDay();
	int localDate = date.getDate();
	String gmtStr = date.toGMTString();
	int blank = gmtStr.indexOf( ' ' );
	int gmtDate = Integer.parseInt( gmtStr.substring( 0, blank ) );
	int gmtDay;
	if ( gmtDate > localDate || ( gmtDate < localDate && gmtDate == 1 ) )
	    gmtDay = ( localDay + 1 ) % 7;
	else if ( localDate > gmtDate || ( localDate < gmtDate && localDate == 1 ) )
	    gmtDay = ( localDay + 6 ) % 7;
	else
	    gmtDay = localDay;
	return weekdays[gmtDay] + ( gmtDate < 10 ? ", 0" : ", " ) + gmtStr;
	}

    private boolean headersWritten = false;

    /// Writes the status line and message headers for this response to the
    // output stream.
    // @exception IOException if an I/O error has occurred
    void writeHeaders() throws IOException
	{
	if ( headersWritten )
	    return;
	headersWritten = true;
	if ( reqMime )
	    {
	    out.println( reqProtocol + " " + resCode + " " + resMessage );
	    for ( int i = 0; i < resHeaderNames.size(); ++i )
		{
		String name = (String) resHeaderNames.elementAt( i );
		String value = (String) resHeaderValues.elementAt( i );
		if ( value != null )	// just in case
		    out.println( name + ": " + value );
		}
	    out.println( "" );
	    out.flush();
	    }
	}

    /// Writes an error response using the specified status code and message.
    // @param resCode the status code
    // @param resMessage the status message
    // @exception IOException if an I/O error has occurred
    public void sendError( int resCode, String resMessage ) throws IOException
	{
	setStatus( resCode, resMessage );
	realSendError();
	}

    /// Writes an error response using the specified status code and a default
    // message.
    // @param resCode the status code
    // @exception IOException if an I/O error has occurred
    public void sendError( int resCode ) throws IOException
	{
	setStatus( resCode );
	realSendError();
	}

    private void realSendError() throws IOException
	{
	setContentType( "text/html" );
	out.println( "<HTML><HEAD>" );
	out.println( "<TITLE>" + resCode + " " + resMessage + "</TITLE>" );
	out.println( "</HEAD><BODY BGCOLOR=\"#99cc99\">" );
	out.println( "<H2>" + resCode + " " + resMessage + "</H2>" );
	out.println( "<HR>" );
	out.println( "</BODY></HTML>" );
	out.flush();
	}

    /// Sends a redirect message to the client using the specified redirect
    // location URL.
    // @param location the redirect location URL
    // @exception IOException if an I/O error has occurred
    public void sendRedirect( String location ) throws IOException
	{
	setHeader( "Location", location );
	sendError( SC_MOVED_TEMPORARILY );
	}

    // URL session-encoding stuff.  Not implemented, but the API is here
    // for compatibility.

    /// Encodes the specified URL by including the session ID in it, or, if
    // encoding is not needed, returns the URL unchanged. The
    // implementation of this method should include the logic to determine
    // whether the session ID needs to be encoded in the URL. For example,
    // if the browser supports cookies, or session tracking is turned off,
    // URL encoding is unnecessary.
    // <P>
    // All URLs emitted by a Servlet should be run through this method.
    // Otherwise, URL rewriting cannot be used with browsers which do not
    // support cookies.
    public String encodeUrl( String url )
	{
	return url;
	}

    /// Encodes the specified URL for use in the sendRedirect method or, if
    // encoding is not needed, returns the URL unchanged. The
    // implementation of this method should include the logic to determine
    // whether the session ID needs to be encoded in the URL.  Because the
    // rules for making this determination differ from those used to
    // decide whether to encode a normal link, this method is seperate
    // from the encodeUrl method.
    // <P>
    // All URLs sent to the HttpServletResponse.sendRedirect method should be
    // run through this method.  Otherwise, URL rewriting cannot be used with
    // browsers which do not support cookies.
    public String encodeRedirectUrl( String url )
	{
	return url;
	}

   //from Acme.Utils:
    /// Turns a String into an array of Strings, by using StringTokenizer
    // to split it up at whitespace.
    public static String[] splitStr( String str )
        {
        StringTokenizer st = new StringTokenizer( str );
        int n = st.countTokens();
        String[] strs = new String[n];
        for ( int i = 0; i < n; ++i )
            strs[i] = st.nextToken();
        return strs;
        }
    }
	








