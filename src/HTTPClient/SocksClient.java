/*
 * @(#)SocksClient.java					0.3-2 18/06/1999
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


import java.io.*;
import java.net.*;

/**
 * This class implements a SOCKS Client. Supports both versions 4 and 5.
 * GSSAPI however is not yet implemented.
 * <P>Usage is as follows: somewhere in the initialization code (and before
 * the first socket creation call) create a SocksClient instance. Then replace
 * each socket creation call
 *
 *     <code>sock = new Socket(host, port);</code>
 *
 * with
 *
 *     <code>sock = socks_client.getSocket(host, port);</code>
 *
 * (where <var>socks_client</var> is the above created SocksClient instance).
 * That's all.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class SocksClient implements GlobalConstants
{
    /** the host the socks server sits on */
    private String socks_host;

    /** the port the socks server listens on */
    private int    socks_port;

    /** the version of socks that the server handles */
    private int    socks_version;

    /** socks commands */
    private final static byte CONNECT = 1,
			      BIND    = 2,
			      UDP_ASS = 3;

    /** socks version 5 authentication methods */
    private final static byte NO_AUTH = 0,
			      GSSAPI  = 1,
			      USERPWD = 2,
			      NO_ACC  = (byte) 0xFF;

    /** socks version 5 address types */
    private final static byte IP_V4   = 1,
			      DMNAME  = 3,
			      IP_V6   = 4;


    // Constructors

    /**
     * Creates a new SOCKS Client using the specified host and port for
     * the server. Will try to establish the SOCKS version used when
     * establishing the first connection.
     *
     * @param host  the host the SOCKS server is sitting on.
     * @param port  the port the SOCKS server is listening on.
     */
    SocksClient(String host, int port)
    {
	this.socks_host    = host;
	this.socks_port    = port;
	this.socks_version = -1;	// as yet unknown
    }

    /**
     * Creates a new SOCKS Client using the specified host and port for
     * the server.
     *
     * @param host     the host the SOCKS server is sitting on.
     * @param port     the port the SOCKS server is listening on.
     * @param version  the version the SOCKS server is using.
     * @exception SocksException if the version is invalid (Currently allowed
     *                           are: 4 and 5).
     */
    SocksClient(String host, int port, int version)  throws SocksException
    {
	this.socks_host    = host;
	this.socks_port    = port;

	if (version != 4  &&  version != 5)
	    throw new SocksException("SOCKS Version not supported: "+version);
	this.socks_version = version;
    }


    // Methods

    /**
     * Initiates a connection to the socks server, does the startup
     * protocol and returns a socket ready for talking.
     *
     * @param host  the host you wish to connect to
     * @param port  the port you wish to connect to
     * @return a Socket with a connection via socks to the desired host/port
     * @exception IOException if any socket operation fails
     */
    Socket getSocket(String host, int port)  throws IOException
    {
	Socket sock = null;

	try
	{
	    if (DebugSocks)
		System.err.println("Socks: contacting server on " +
				    socks_host + ":" + socks_port);


	    // create socket and streams

	    sock = connect(socks_host, socks_port);
	    InputStream  inp = sock.getInputStream();
	    OutputStream out = sock.getOutputStream();


	    // setup connection depending on socks version

	    switch (socks_version)
	    {
		case 4:
		    v4ProtExchg(inp, out, host, port);
		    break;
		case 5:
		    v5ProtExchg(inp, out, host, port);
		    break;
		case -1:
		    // Ok, let's try and figure it out
		    try
		    {
			v4ProtExchg(inp, out, host, port);
			socks_version = 4;
		    }
		    catch (SocksException se)
		    {
			if (DebugSocks)
			    System.err.println("Socks: V4 request failed: " +
						se.getMessage());

			sock.close();
			sock = connect(socks_host, socks_port);
			inp = sock.getInputStream();
			out = sock.getOutputStream();

			v5ProtExchg(inp, out, host, port);
			socks_version = 5;
		    }
		    break;
		default:
		    throw new Error("SocksClient internal error: unknown " +
				    "version "+socks_version);
	    }

	    if (DebugSocks)
		System.err.println("Socks: connection established.");

	    return sock;
	}
	catch (IOException ioe)
	{
	    if (sock != null)
	    {
		try { sock.close(); }
		catch (IOException ee) {}
	    }

	    throw ioe;
	}
    }


    /**
     * Connect to the host/port, trying all addresses assciated with that
     * host.
     *
     * @return the Socket
     * @exception IOException if the connection could not be established
     */
    private static final Socket connect(String host, int port)
	    throws IOException
    {
	InetAddress[] addr_list = InetAddress.getAllByName(host);
	for (int idx=0; idx<addr_list.length; idx++)
	{
	    try
		{ return new Socket(addr_list[idx], port); }
	    catch (SocketException se)
	    {
		if (idx < addr_list.length-1)
		    continue;	// try next IP address
		else
		    throw se;	// none of them worked
	    }
	}

	return null;	// never reached - just here to shut up the compiler
    }


    private boolean v4A  = false;	// SOCKS version 4A
    private byte[]  user = null;

    /**
     * Does the protocol exchange for a version 4 SOCKS connection.
     */
    private void v4ProtExchg(InputStream inp, OutputStream out, String host,
			     int port)
	throws SocksException, IOException
    {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);

	if (DebugSocks)
	    System.err.println("Socks: Beginning V4 Protocol Exchange for host "
				+ host + ":" + port);

	// get ip addr and user name

	byte[] addr = { 0, 0, 0, 42 };
	if (!v4A)
	{
	    try
		{ addr = InetAddress.getByName(host).getAddress(); }
	    // if we can't translate, let's try the server
	    catch (UnknownHostException uhe)
		{ v4A = true; }
	    catch (SecurityException se)
		{ v4A = true; }
	    if (DebugSocks)
		if (v4A)
		    System.err.println("Socks: Switching to version 4A");
	}

	if (user == null)	// I see no reason not to cache this
	{
	    String user_str;
	    try
		{ user_str = System.getProperty("user.name", ""); }
	    catch (SecurityException se)
		{ user_str = "";	/* try it anyway */ }
	    user = new byte[user_str.length()+1];
	    user_str.getBytes(0, user_str.length(), user, 0);
	    user[user_str.length()] = 0;	// 0-terminated string
	}


	// send version 4 request

	if (DebugSocks)
	    System.err.println("Socks: Sending connect request for user " +
				new String(user, 0, 0, user.length-1));

	buffer.reset();
	buffer.write(4);				// version
	buffer.write(CONNECT);				// command
	buffer.write((port >> 8) & 0xff);		// port
	buffer.write(port & 0xff);
	buffer.write(addr, 0, addr.length);		// address
	buffer.write(user, 0, user.length);		// user
	if (v4A)
	{
	    byte[] host_buf = new byte[host.length()];
	    host.getBytes(0, host.length(), host_buf, 0);
	    buffer.write(host_buf, 0, host_buf.length);	// host name
	    buffer.write(0);				// terminating 0
	}
	buffer.writeTo(out);


	// read response

	int version = inp.read();
	if (version == -1)
	    throw new SocksException("Connection refused by server");
	else if (version == 4)	// not all socks4 servers are correct...
	    if (DebugSocks)
		System.err.println("Socks: Warning: received version 4 " +
				    "instead of 0");
	else if (version != 0)
	    throw new SocksException("Received invalid version: " + version +
				     "; expected: 0");

	int sts = inp.read();

	if (DebugSocks)
	    System.err.println("Socks: Received response; version: " + version +
				"; status: " + sts);

	switch (sts)
	{
	    case 90:	// request granted
		break;
	    case 91:	// request rejected
		throw new SocksException("Connection request rejected");
	    case 92:	// request rejected: can't connect to identd
		throw new SocksException("Connection request rejected: " +
					 "can't connect to identd");
	    case 93:	// request rejected: identd reports diff uid
		throw new SocksException("Connection request rejected: " +
					 "identd reports different user-id " +
					 "from "+
					 new String(user, 0, 0, user.length-1));
	    default:	// unknown status
		throw new SocksException("Connection request rejected: " +
					 "unknown error " + sts);
	}

	byte[] skip = new byte[2+4];		// skip port + address
	int rcvd = 0,
	    tot  = 0;
	while (tot < skip.length  &&
		(rcvd = inp.read(skip, 0, skip.length-tot)) != -1)
	    tot += rcvd;
    }


    /**
     * Does the protocol exchange for a version 5 SOCKS connection.
     * (rfc-1928)
     */
    private void v5ProtExchg(InputStream inp, OutputStream out, String host,
			     int port)
	throws SocksException, IOException
    {
	int                   version;
	ByteArrayOutputStream buffer = new ByteArrayOutputStream(100);

	if (DebugSocks)
	    System.err.println("Socks: Beginning V5 Protocol Exchange for host "
				+ host + ":" + port);

	// send version 5 verification methods

	if (DebugSocks)
	    System.err.println("Socks: Sending authentication request; methods"
				+ " No-Authentication, Username/Password");

	buffer.reset();
	buffer.write(5);		// version
	buffer.write(2);		// number of verification methods
	buffer.write(NO_AUTH);		// method: no authentication
	buffer.write(USERPWD);		// method: username/password
	//buffer.write(GSSAPI);		// method: gssapi
	buffer.writeTo(out);


	// receive servers repsonse

	version = inp.read();
	if (version == -1)
	    throw new SocksException("Connection refused by server");
	else if (version != 5)
	    throw new SocksException("Received invalid version: " + version +
				     "; expected: 5");

	int method = inp.read();

	if (DebugSocks)
	    System.err.println("Socks: Received response; version: " + version +
				"; method: " + method);


	// enter sub-negotiation for authentication

	switch(method)
	{
	    case NO_AUTH:
		break;
	    case GSSAPI:
		negotiate_gssapi(inp, out);
		break;
	    case USERPWD:
		negotiate_userpwd(inp, out);
		break;
	    case NO_ACC:
		throw new SocksException("Server unwilling to accept any " +
					 "standard authentication methods");
	    default:
		throw new SocksException("Cannot handle authentication method "
					 + method);
	}


	// send version 5 request

	if (DebugSocks)
	    System.err.println("Socks: Sending connect request");

	buffer.reset();
	buffer.write(5);				// version
	buffer.write(CONNECT);				// command
	buffer.write(0);				// reserved - must be 0
	buffer.write(DMNAME);				// address type
	buffer.write(host.length() & 0xff);		// address length
	byte[] hname = new byte[host.length()];
	host.getBytes(0, host.length(), hname, 0);
	buffer.write(hname, 0, hname.length);		// address
	buffer.write((port >> 8) & 0xff);		// port
	buffer.write(port & 0xff);
	buffer.writeTo(out);


	// read response

	version = inp.read();
	if (version != 5)
	    throw new SocksException("Received invalid version: " + version +
				     "; expected: 5");

	int sts = inp.read();

	if (DebugSocks)
	    System.err.println("Socks: Received response; version: " + version +
				"; status: " + sts);

	switch (sts)
	{
	    case 0:	// succeeded
		break;
	    case 1:
		throw new SocksException("General SOCKS server failure");
	    case 2:
		throw new SocksException("Connection not allowed");
	    case 3:
		throw new SocksException("Network unreachable");
	    case 4:
		throw new SocksException("Host unreachable");
	    case 5:
		throw new SocksException("Connection refused");
	    case 6:
		throw new SocksException("TTL expired");
	    case 7:
		throw new SocksException("Command not supported");
	    case 8:
		throw new SocksException("Address type not supported");
	    default:
		throw new SocksException("Unknown reply received from server: "
					 + sts);
	}

	inp.read();			// Reserved
	int atype = inp.read(),		// address type
	    alen;			// address length
	switch(atype)
	{
	    case IP_V6:
		alen = 16;
		break;
	    case IP_V4:
		alen = 4;
		break;
	    case DMNAME:
		alen = inp.read();
		break;
	    default:
		throw new SocksException("Invalid address type received from" +
					 " server: "+atype);
	}

	byte[] skip = new byte[alen+2];		// skip address + port
	int rcvd = 0,
	    tot  = 0;
	while (tot < skip.length  &&
		(rcvd = inp.read(skip, 0, skip.length-tot)) != -1)
	    tot += rcvd;
    }


    /**
     * Negotiates authentication using the gssapi protocol
     * (draft-ietf-aft-gssapi-02).
     *
     * NOTE: this is not implemented currently. Will have to wait till
     *       Java provides the necessary access to the system routines.
     */
    private void negotiate_gssapi(InputStream inp, OutputStream out)
	throws SocksException, IOException
    {
	throw new
	    SocksException("GSSAPI authentication protocol not implemented");
    }


    /**
     * Negotiates authentication using the username/password protocol
     * (rfc-1929). The username and password should previously have been
     * stored using the scheme "SOCKS5" and realm "USER/PASS"; e.g.
     * AuthorizationInfo.addAuthorization(socks_host, socks_port, "SOCKS5",
     *					  "USER/PASS", null,
     *					  { new NVPair(username, password) });
     *
     */
    private void negotiate_userpwd(InputStream inp, OutputStream out)
	throws SocksException, IOException
    {
	byte[] buffer;


	if (DebugSocks)
	    System.err.println("Socks: Entering authorization subnegotiation" +
				"; method: Username/Password");

	// get username/password

	AuthorizationInfo auth_info;
	try
	{
	    auth_info =
		AuthorizationInfo.getAuthorization(socks_host, socks_port,
						   "SOCKS5", "USER/PASS", true);
	}
	catch (AuthSchemeNotImplException atnie)
	    { auth_info = null; }

	if (auth_info == null)
	    throw new SocksException("No Authorization info for SOCKS found " +
				     "(server requested username/password).");

	NVPair[] unpw = auth_info.getParams();
	if (unpw == null  ||  unpw.length == 0)
	    throw new SocksException("No Username/Password found in " +
				     "authorization info for SOCKS.");

	String user_str = unpw[0].getName();
	String pass_str = unpw[0].getValue();


	// send them to server

	if (DebugSocks)
	    System.err.println("Socks: Sending authorization request for user "+
				user_str);

	buffer = new byte[1+1+user_str.length()+1+pass_str.length()];
	buffer[0] = 1;				// version 1 (subnegotiation)
	buffer[1] = (byte) user_str.length();		// Username length
	user_str.getBytes(0, buffer[1], buffer, 2);	// Username
	buffer[2+buffer[1]] = (byte) pass_str.length();	// Password length
	pass_str.getBytes(0, buffer[2+buffer[1]], buffer, 2+buffer[1]+1);// Password
	out.write(buffer);


	// get reply

	int version = inp.read();
	if (version != 1)
	    throw new SocksException("Wrong version received in username/" +
				     "password subnegotiation response: " +
				     version + "; expected: 1");

	int sts = inp.read();
	if (sts != 0)
	    throw new SocksException("Username/Password authentication " +
				     "failed; status: "+sts);

	if (DebugSocks)
	    System.err.println("Socks: Received response; version: " + version +
				"; status: " + sts);
    }


    /**
     * produces a string.
     * @return a string containing the host and port of the socks server
     */
    public String toString()
    {
	return getClass().getName() + "[" + socks_host + ":" + socks_port + "]";
    }
}

