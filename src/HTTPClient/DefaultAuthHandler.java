/*
 * @(#)DefaultAuthHandler.java				0.3-2 18/06/1999
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
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.util.Vector;
import java.util.StringTokenizer;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

/**
 * A simple authorization handler that throws up a message box requesting
 * both a username and password. This is default authorization handler.
 * Currently only handles the authentication types "Basic", "Digest" and
 * "SOCKS5" (used for the SocksClient and not part of HTTP per se).
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.2
 */

class DefaultAuthHandler implements AuthorizationHandler, GlobalConstants
{
    private static final byte[] NUL = new byte[0];

    private static final int DI_A1  = 0;
    private static final int DI_A1S = 1;
    private static final int DI_QOP = 2;

    private static byte[] digest_secret = null;

    private BasicAuthBox inp = null;



    /**
     * For Digest authentication we need to set the uri, response and
     * opaque parameters. For "Basic" and "SOCKS5" nothing is done.
     */
    public AuthorizationInfo fixupAuthInfo(AuthorizationInfo info,
					   RoRequest req,
					   AuthorizationInfo challenge,
					   RoResponse resp)
		    throws AuthSchemeNotImplException
    {
	// nothing to do for Basic and SOCKS5 schemes

	if (info.getScheme().equalsIgnoreCase("Basic")  ||
	    info.getScheme().equalsIgnoreCase("SOCKS5"))
	    return info;
	else if (!info.getScheme().equalsIgnoreCase("Digest"))
	    throw new AuthSchemeNotImplException(info.getScheme());

	if (DebugAuth)
	    System.err.println("Auth:  fixing up Authorization for host " +
				info.getHost()+":"+info.getPort() +
				"; scheme: " + info.getScheme() +
				"; realm: " + info.getRealm());

	return digest_fixup(info, req, challenge, resp);
    }


    /**
     * returns the requested authorization, or null if none was given.
     *
     * @param challenge the parsed challenge from the server.
     * @param req the request which solicited this response
     * @param resp the full response received
     * @return a structure containing the necessary authorization info,
     *         or null
     * @exception AuthSchemeNotImplException if the authentication scheme
     *             in the challenge cannot be handled.
     */
    public AuthorizationInfo getAuthorization(AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp)
		    throws AuthSchemeNotImplException
    {
	AuthorizationInfo cred;


	if (DebugAuth)
	    System.err.println("Auth:  Requesting Authorization for host " +
				challenge.getHost()+":"+challenge.getPort() +
				"; scheme: " + challenge.getScheme() +
				"; realm: " + challenge.getRealm());


	// we only handle Basic, Digest and SOCKS5 authentication

	if (!challenge.getScheme().equalsIgnoreCase("Basic")  &&
	    !challenge.getScheme().equalsIgnoreCase("Digest")  &&
	    !challenge.getScheme().equalsIgnoreCase("SOCKS5"))
	    throw new AuthSchemeNotImplException(challenge.getScheme());


	// For digest authentication, check if stale is set

	if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    cred = digest_check_stale(challenge, req, resp);
	    if (cred != null)
		return cred;
	}


	// Ask the user for username/password

	if (!req.allowUI())
	    return null;

	if (inp == null)
	{
	    synchronized(getClass())
	    {
		if (inp == null)
		    inp = new BasicAuthBox();
	    }
	}

	NVPair answer;
	if (challenge.getScheme().equalsIgnoreCase("basic")  ||
	    challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    answer = inp.getInput("Enter username and password for realm `" +
				  challenge.getRealm() + "'",
				  "on host " + challenge.getHost() + ":" +
				  challenge.getPort(),
				  "Authentication Scheme: " +
				  challenge.getScheme());
	}
	else
	{
	    answer = inp.getInput("Enter username and password for SOCKS " +
				  "server on host ", challenge.getHost(),
				  "Authentication Method: username/password");
	}

	if (answer == null)
	    return null;


	// Now process the username/password

	if (challenge.getScheme().equalsIgnoreCase("basic"))
	{
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 Codecs.base64Encode(
						answer.getName() + ":" +
						answer.getValue()));
	}
	else if (challenge.getScheme().equalsIgnoreCase("Digest"))
	{
	    cred = digest_gen_auth_info(challenge.getHost(),
					challenge.getPort(),
				        challenge.getRealm(), answer.getName(),
					answer.getValue(),
					req.getConnection().getContext());
	    cred = digest_fixup(cred, req, challenge, null);
	}
	else	// SOCKS5
	{
	    NVPair[] upwd = { answer };
	    cred = new AuthorizationInfo(challenge.getHost(),
					 challenge.getPort(),
					 challenge.getScheme(),
					 challenge.getRealm(),
					 upwd, null);
	}


	// try to get rid of any unencoded passwords in memory

	answer = null;
	System.gc();


	// Done

	if (DebugAuth) System.err.println("Auth:  Got Authorization");

	return cred;
    }


    /**
     * We handle the "Authentication-Info" and "Proxy-Authentication-Info"
     * headers here.
     */
    public void handleAuthHeaders(Response resp, RoRequest req,
				  AuthorizationInfo prev,
				  AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getHeader("Authentication-Info");
	String prxy_info = resp.getHeader("Proxy-Authentication-Info");

	if (auth_info == null  &&  prev != null  &&
	    hasParam(prev.getParams(), "qop", "auth-int"))
	    auth_info = "";

	if (prxy_info == null  &&  prxy != null  &&
	    hasParam(prxy.getParams(), "qop", "auth-int"))
	    prxy_info = "";

	try
	{
	    handleAuthInfo(auth_info, "Authentication-Info", prev, resp, req,
			   true);
	    handleAuthInfo(prxy_info, "Proxy-Authentication-Info", prxy, resp,
			   req, true);
	}
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    /**
     * We handle the "Authentication-Info" and "Proxy-Authentication-Info"
     * trailers here.
     */
    public void handleAuthTrailers(Response resp, RoRequest req,
				   AuthorizationInfo prev,
				   AuthorizationInfo prxy)
	    throws IOException
    {
	String auth_info = resp.getTrailer("Authentication-Info");
	String prxy_info = resp.getTrailer("Proxy-Authentication-Info");

	try
	{
	    handleAuthInfo(auth_info, "Authentication-Info", prev, resp, req,
			   false);
	    handleAuthInfo(prxy_info, "Proxy-Authentication-Info", prxy, resp,
			   req, false);
	}
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
    }


    private static void handleAuthInfo(String auth_info, String hdr_name,
				       AuthorizationInfo prev, Response resp,
				       RoRequest req, boolean in_headers)
	    throws ParseException, IOException
    {
	if (auth_info == null)  return;

	Vector pai = Util.parseHeader(auth_info);
	HttpHeaderElement elem;

	if (handle_nextnonce(prev, req,
			     elem = Util.getElement(pai, "nextnonce")))
	    pai.removeElement(elem);
	if (handle_discard(prev, req, elem = Util.getElement(pai, "discard")))
	    pai.removeElement(elem);

	if (in_headers)
	{
	    HttpHeaderElement qop = null;

	    if (pai != null  &&
		(qop = Util.getElement(pai, "qop")) != null  &&
		qop.getValue() != null)
	    {
		handle_rspauth(prev, resp, req, pai, hdr_name);
	    }
	    else if (prev != null  &&
		     (Util.hasToken(resp.getHeader("Trailer"), hdr_name)  &&
		      hasParam(prev.getParams(), "qop", null)  ||
		      hasParam(prev.getParams(), "qop", "auth-int")))
	    {
		handle_rspauth(prev, resp, req, null, hdr_name);
	    }

	    else if ((pai != null  &&  qop == null  &&
		      pai.contains(new HttpHeaderElement("digest")))  ||
		     (Util.hasToken(resp.getHeader("Trailer"), hdr_name)  &&
		      prev != null  &&
		      !hasParam(prev.getParams(), "qop", null)))
	    {
		handle_digest(prev, resp, req, hdr_name);
	    }
	}

	if (pai.size() > 0)
	    resp.setHeader(hdr_name, Util.assembleHeader(pai));
	else
	    resp.deleteHeader(hdr_name);
    }


    private static final boolean hasParam(NVPair[] params, String name,
					  String val)
    {
	for (int idx=0; idx<params.length; idx++)
	    if (params[idx].getName().equalsIgnoreCase(name)  &&
		(val == null  ||  params[idx].getValue().equalsIgnoreCase(val)))
		return true;

	return false;
    }


    /*
     * Here are all the Digest specific methods
     */

    private static AuthorizationInfo digest_gen_auth_info(String host, int port,
							  String realm,
							  String user,
							  String pass,
							  Object context)
    {
	String A1 = user + ":" + realm + ":" + pass;
	String[] info = { new MD5(A1).asHex(), null, null };

	AuthorizationInfo prev = AuthorizationInfo.getAuthorization(host, port,
						    "Digest", realm, context);
	NVPair[] params;
	if (prev == null)
	{
	    params = new NVPair[4];
	    params[0] = new NVPair("username", user);
	    params[1] = new NVPair("uri", "");
	    params[2] = new NVPair("nonce", "");
	    params[3] = new NVPair("response", "");
	}
	else
	{
	    params = prev.getParams();
	    for (int idx=0; idx<params.length; idx++)
	    {
		if (params[idx].getName().equalsIgnoreCase("username"))
		{
		    params[idx] = new NVPair("username", user);
		    break;
		}
	    }
	}

	return new AuthorizationInfo(host, port, "Digest", realm, params, info);
    }


    /**
     * The fixup handler
     */
    private static AuthorizationInfo digest_fixup(AuthorizationInfo info,
						  RoRequest req,
						  AuthorizationInfo challenge,
						  RoResponse resp)
	    throws AuthSchemeNotImplException
    {
	// get various parameters from challenge

	int ch_domain=-1, ch_nonce=-1, ch_alg=-1, ch_opaque=-1, ch_stale=-1,
	    ch_dreq=-1, ch_qop=-1;
	NVPair[] ch_params = null;
	if (challenge != null)
	{
	    ch_params = challenge.getParams();

	    for (int idx=0; idx<ch_params.length; idx++)
	    {
		String name = ch_params[idx].getName().toLowerCase();
		if (name.equals("domain"))               ch_domain = idx;
		else if (name.equals("nonce"))           ch_nonce  = idx;
		else if (name.equals("opaque"))          ch_opaque = idx;
		else if (name.equals("algorithm"))       ch_alg    = idx;
		else if (name.equals("stale"))           ch_stale  = idx;
		else if (name.equals("digest-required")) ch_dreq   = idx;
		else if (name.equals("qop"))             ch_qop    = idx;
	    }
	}


	// get various parameters from info

	int uri=-1, user=-1, alg=-1, response=-1, nonce=-1, cnonce=-1, nc=-1,
	    opaque=-1, digest=-1, dreq=-1, qop=-1;
	NVPair[] params;
	String[] extra;

	synchronized(info)	// we need to juggle nonce, nc, etc
	{
	    params = info.getParams();

	    for (int idx=0; idx<params.length; idx++)
	    {
		String name = params[idx].getName().toLowerCase();
		if (name.equals("uri"))                  uri      = idx;
		else if (name.equals("username"))        user     = idx;
		else if (name.equals("algorithm"))       alg      = idx;
		else if (name.equals("nonce"))           nonce    = idx;
		else if (name.equals("cnonce"))          cnonce   = idx;
		else if (name.equals("nc"))              nc       = idx;
		else if (name.equals("response"))        response = idx;
		else if (name.equals("opaque"))          opaque   = idx;
		else if (name.equals("digest"))          digest   = idx;
		else if (name.equals("digest-required")) dreq     = idx;
		else if (name.equals("qop"))             qop      = idx;
	    }

	    extra = (String[]) info.getExtraInfo();


	    // currently only MD5 hash (and "MD5-sess") is supported

	    if (alg != -1  &&
		!params[alg].getValue().equalsIgnoreCase("MD5")  &&
		!params[alg].getValue().equalsIgnoreCase("MD5-sess"))
		throw new AuthSchemeNotImplException("Digest auth scheme: " +
				    "Algorithm " + params[alg].getValue() +
				    " not implemented");

	    if (ch_alg != -1  &&
		!ch_params[ch_alg].getValue().equalsIgnoreCase("MD5") &&
		!ch_params[ch_alg].getValue().equalsIgnoreCase("MD5-sess"))
		throw new AuthSchemeNotImplException("Digest auth scheme: " +
				    "Algorithm " + ch_params[ch_alg].getValue()+
				    " not implemented");


	    // fix up uri and nonce

	    params[uri] = new NVPair("uri", req.getRequestURI());
	    String old_nonce = params[nonce].getValue();
	    if (ch_nonce != -1  &&
		!old_nonce.equals(ch_params[ch_nonce].getValue()))
		params[nonce] = ch_params[ch_nonce];


	    // update or add optional attributes (opaque, algorithm, cnonce,
	    // nonce-count, and qop

	    if (ch_opaque != -1)
	    {
		if (opaque == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    opaque = params.length-1;
		}
		params[opaque] = ch_params[ch_opaque];
	    }

	    if (ch_alg != -1)
	    {
		if (alg == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    alg = params.length-1;
		}
		params[alg] = ch_params[ch_alg];
	    }

	    if (ch_qop != -1  ||
		(ch_alg != -1  &&
		 ch_params[ch_alg].getValue().equalsIgnoreCase("MD5-sess")))
	    {
		if (cnonce == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    cnonce = params.length-1;
		}

		if (digest_secret == null)
		    digest_secret = gen_random_bytes(20);

		long l_time = System.currentTimeMillis();
		byte[] time = new byte[8];
		time[0] = (byte) (l_time & 0xFF);
		time[1] = (byte) ((l_time >>  8) & 0xFF);
		time[2] = (byte) ((l_time >> 16) & 0xFF);
		time[3] = (byte) ((l_time >> 24) & 0xFF);
		time[4] = (byte) ((l_time >> 32) & 0xFF);
		time[5] = (byte) ((l_time >> 40) & 0xFF);
		time[6] = (byte) ((l_time >> 48) & 0xFF);
		time[7] = (byte) ((l_time >> 56) & 0xFF);

		MD5 hash = new MD5(digest_secret);
		hash.Update(time);
		params[cnonce] = new NVPair("cnonce", hash.asHex());
	    }


	    // select qop option

	    if (ch_qop != -1)
	    {
		if (qop == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    qop = params.length-1;
		}
		extra[DI_QOP] = ch_params[ch_qop].getValue();


		// select qop option

		String[] qops = splitList(extra[DI_QOP], ",");
		String p = null;
		for (int idx=0; idx<qops.length; idx++)
		{
		    if (qops[idx].equalsIgnoreCase("auth-int")  &&
			(req.getStream() == null  ||
			 req.getConnection().ServProtVersKnown  &&
			 req.getConnection().ServerProtocolVersion >= HTTP_1_1))
		    {
			p = "auth-int";
			break;
		    }
		    if (qops[idx].equalsIgnoreCase("auth"))
			p = "auth";
		}
		if (p == null)
		{
		    for (int idx=0; idx<qops.length; idx++)
			if (qops[idx].equalsIgnoreCase("auth-int"))
			    throw new AuthSchemeNotImplException(
				"Digest auth scheme: Can't comply with qop " +
				"option 'auth-int' because an HttpOutputStream " +
				"is being used and the server doesn't speak " +
				"HTTP/1.1");

		    throw new AuthSchemeNotImplException("Digest auth scheme: "+
				"None of the available qop options '" +
				ch_params[ch_qop].getValue() + "' implemented");
		}
		params[qop] = new NVPair("qop", p);
	    }


	    // increment nonce-count.

	    if (qop != -1)
	    {
		/* Note: we should actually be serializing all requests through
		 *       here so that the server sees the nonce-count in a
		 *       strictly increasing order. However, this would be a
		 *       *major* hassle to do, so we're just winging it. Most
		 *       of the time the requests will go over the wire in the
		 *       same order as they pass through here, but in MT apps
		 *       it's possible for one request to "overtake" another
		 *       between here and the synchronized block in
		 *       sendRequest().
		 */
		if (nc == -1)
		{
		    params = Util.resizeArray(params, params.length+1);
		    nc = params.length-1;
		    params[nc] = new NVPair("nc", "00000001");
		}
		else if (old_nonce.equals(params[nonce].getValue()))
		{
		    String c = Long.toHexString(
				Long.parseLong(params[nc].getValue(), 16) + 1);
		    params[nc] =
			new NVPair("nc", "00000000".substring(c.length()) + c);
		}
		else
		    params[nc] = new NVPair("nc", "00000001");
	    }


	    // calc new session key if necessary

	    if (challenge != null  &&
		(ch_stale == -1  ||
		 !ch_params[ch_stale].getValue().equalsIgnoreCase("true"))  &&
		alg != -1  &&
		params[alg].getValue().equalsIgnoreCase("MD5-sess"))
	    {
		extra[DI_A1S] = new MD5(extra[DI_A1] + ":" +
					params[nonce].getValue() + ":" +
					params[cnonce].getValue()).asHex();
	    }


	    // update parameters for next auth cycle

	    info.setParams(params);
	    info.setExtraInfo(extra);
	}


	// calc "response" attribute

	String hash = null;
	if (qop != -1 && params[qop].getValue().equalsIgnoreCase("auth-int") &&
	    req.getStream() == null)
	{
	    MD5 entity_hash = new MD5();
	    entity_hash.Update(req.getData() == null ? NUL : req.getData());
	    hash = entity_hash.asHex();
	}

	if (req.getStream() == null)
	    params[response] = new NVPair("response", 
		  calcResponseAttr(hash, extra, params, alg, uri, qop, nonce,
				   nc, cnonce, req.getMethod()));


	// calc digest if necessary

	AuthorizationInfo new_info;

	boolean ch_dreq_val = false;
	if (ch_dreq != -1  &&
	    (ch_params[ch_dreq].getValue() == null  ||
	     ch_params[ch_dreq].getValue().equalsIgnoreCase("true")))
	    ch_dreq_val = true;

	if ((ch_dreq_val  ||  digest != -1)  &&  req.getStream() == null)
	{
	    NVPair[] d_params;
	    if (digest == -1)
	    {
		d_params = Util.resizeArray(params, params.length+1);
		digest = params.length;
	    }
	    else
		d_params = params;
	    d_params[digest] = new NVPair("digest",
		   calc_digest(req, extra[DI_A1], params[nonce].getValue()));

	    if (dreq == -1)	// if server requires digest, then so do we...
	    {
		dreq = d_params.length;
		d_params = Util.resizeArray(d_params, d_params.length+1);
		d_params[dreq] = new NVPair("digest-required", "true");
	    }

	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     d_params, extra);
	}
	else if (ch_dreq_val)
	    new_info = null;
	else
	    new_info = new AuthorizationInfo(info.getHost(), info.getPort(),
					     info.getScheme(), info.getRealm(),
					     params, extra);


	// add info for other domains, if listed

	boolean from_server = (challenge != null)  &&
	    challenge.getHost().equalsIgnoreCase(req.getConnection().getHost());
	if (ch_domain != -1)
	{
	    URI base = null;
	    try
	    {
		base = new URI(req.getConnection().getProtocol(),
			       req.getConnection().getHost(),
			       req.getConnection().getPort(),
			       req.getRequestURI());
	    }
	    catch (ParseException pe)
		{ }

	    StringTokenizer tok =
			new StringTokenizer(ch_params[ch_domain].getValue());
	    while (tok.hasMoreTokens())
	    {
		URI Uri;
		try
		    { Uri = new URI(base, tok.nextToken()); }
		catch (ParseException pe)
		    { continue; }

		AuthorizationInfo tmp =
		    AuthorizationInfo.getAuthorization(Uri.getHost(),
						       Uri.getPort(),
						       info.getScheme(),
						       info.getRealm(),
					     req.getConnection().getContext());
		if (tmp == null)
		{
		    params[uri] = new NVPair("uri", Uri.getPath());
		    tmp = new AuthorizationInfo(Uri.getHost(), Uri.getPort(),
					        info.getScheme(),
						info.getRealm(), params,
						extra);
		    AuthorizationInfo.addAuthorization(tmp);
		}
		if (from_server)
		    tmp.addPath(Uri.getPath());
	    }
	}
	else if (from_server  &&  challenge != null)
	{
	    // Spec says that if no domain attribute is present then the
	    // whole server should be considered being in the same space
	    AuthorizationInfo tmp =
		AuthorizationInfo.getAuthorization(challenge.getHost(),
						   challenge.getPort(),
						   info.getScheme(),
						   info.getRealm(),
					     req.getConnection().getContext());
	    if (tmp != null)  tmp.addPath("/");
	}


	// now return the one to use

	return new_info;
    }


    /**
     * @return the fixed info is stale=true; null otherwise
     */
    private static AuthorizationInfo digest_check_stale(
					      AuthorizationInfo challenge,
					      RoRequest req, RoResponse resp)
	    throws AuthSchemeNotImplException
    {
	AuthorizationInfo cred = null;

	NVPair[] params = challenge.getParams();
	for (int idx=0; idx<params.length; idx++)
	{
	    String name = params[idx].getName();
	    if (name.equalsIgnoreCase("stale")  &&
		params[idx].getValue().equalsIgnoreCase("true"))
	    {
		cred = AuthorizationInfo.getAuthorization(challenge, req, resp,
							  false);
		if (cred != null)	// should always be the case
		    return digest_fixup(cred, req, challenge, resp);
		break;			// should never be reached
	    }
	}

	return cred;
    }


    /**
     * Handle nextnonce field.
     */
    private static boolean handle_nextnonce(AuthorizationInfo prev,
					    RoRequest req,
					    HttpHeaderElement nextnonce)
    {
	if (prev == null  ||  nextnonce == null  ||
	    nextnonce.getValue() == null)
	    return false;

	AuthorizationInfo ai;
	try
	    { ai = AuthorizationInfo.getAuthorization(prev, req, null, false); }
	catch (AuthSchemeNotImplException asnie)
	    { ai = prev; /* shouldn't happen */ }
	synchronized(ai)
	{
	    NVPair[] params = ai.getParams();
	    params = setValue(params, "nonce", nextnonce.getValue());
	    params = setValue(params, "nc", "00000000");
	    ai.setParams(params);
	}

	return true;
    }


    /**
     * Handle digest field of the Authentication-Info response header.
     */
    private static boolean handle_digest(AuthorizationInfo prev, Response resp,
					 RoRequest req, String hdr_name)
	    throws IOException
    {
	if (prev == null)
	    return false;

	NVPair[] params = prev.getParams();
	VerifyDigest
	    verifier = new VerifyDigest(((String[]) prev.getExtraInfo())[0],
					getValue(params, "nonce"),
					req.getMethod(),
					getValue(params, "uri"),
					hdr_name, resp);

	if (resp.hasEntity())
	{
	    if (DebugAuth)
		System.err.println("Auth:  pushing md5-check-stream to verify "+
				   "digest from " + hdr_name);
	    resp.inp_stream = new MD5InputStream(resp.inp_stream, verifier);
	}
	else
	{
	    if (DebugAuth)
		System.err.println("Auth:  verifying digest from " + hdr_name);
	    verifier.verifyHash(new MD5().Final(), 0);
	}

	return true;
    }


    /**
     * Handle rspauth field of the Authentication-Info response header.
     */
    private static boolean handle_rspauth(AuthorizationInfo prev, Response resp,
					  RoRequest req, Vector auth_info,
					  String hdr_name)
	    throws IOException
    {
	if (prev == null)
	    return false;


	// get the parameters we sent

	NVPair[] params = prev.getParams();
	int uri=-1, alg=-1, nonce=-1, cnonce=-1, nc=-1;
	for (int idx=0; idx<params.length; idx++)
	{
	    String name = params[idx].getName().toLowerCase();
	    if (name.equals("uri"))            uri    = idx;
	    else if (name.equals("algorithm")) alg    = idx;
	    else if (name.equals("nonce"))     nonce  = idx;
	    else if (name.equals("cnonce"))    cnonce = idx;
	    else if (name.equals("nc"))        nc     = idx;
	}


	// create hash verifier to verify rspauth

	VerifyRspAuth
	    verifier = new VerifyRspAuth(params[uri].getValue(),
			      ((String[]) prev.getExtraInfo())[0],
			      (alg == -1 ? null : params[alg].getValue()),
						  params[nonce].getValue(),
			      (cnonce == -1 ? "" : params[cnonce].getValue()),
			      (nc == -1 ? "" : params[nc].getValue()),
			      hdr_name, resp);


	// if Authentication-Info in header and qop=auth then verify immediately

	HttpHeaderElement qop = null;
	if (auth_info != null  &&
	    (qop = Util.getElement(auth_info, "qop")) != null  &&
	    qop.getValue() != null  &&
	    (qop.getValue().equalsIgnoreCase("auth")  ||
	     !resp.hasEntity()  &&  qop.getValue().equalsIgnoreCase("auth-int"))
	   )
	{
	    if (DebugAuth)
		System.err.println("Auth:  verifying rspauth from " + hdr_name);
	    verifier.verifyHash(new MD5().Final(), 0);
	}
	else
	{
	    // else push md5 stream and verify after body

	    if (DebugAuth)
		System.err.println("Auth:  pushing md5-check-stream to verify "+
				   "rspauth from " + hdr_name);
	    resp.inp_stream = new MD5InputStream(resp.inp_stream, verifier);
	}

	return true;
    }


    /**
     * Calc "response" attribute for a request.
     */
    private static String calcResponseAttr(String hash, String[] extra,
					   NVPair[] params, int alg,
					   int uri, int qop, int nonce,
					   int nc, int cnonce, String method)
    {
	String A1, A2, resp_val;

	if (alg != -1  &&
	    params[alg].getValue().equalsIgnoreCase("MD5-sess"))
	    A1 = extra[DI_A1S];
	else
	    A1 = extra[DI_A1];

	A2 = method + ":" + params[uri].getValue();
	if (qop != -1  &&
	    params[qop].getValue().equalsIgnoreCase("auth-int"))
	{
	    A2 += ":" + hash;
	}
	A2 = new MD5(A2).asHex();

	if (qop == -1)
	    resp_val = new MD5(A1 + ":" + params[nonce].getValue() + ":" +
			       A2).asHex();
	else
	    resp_val =
		new MD5(A1 + ":" + params[nonce].getValue() + ":" +
			params[nc].getValue() + ":" +
			params[cnonce].getValue() + ":" +
			params[qop].getValue() + ":" + A2).asHex();

	return resp_val;
    }


    /**
     * Calculates the digest of the request body. This was in RFC-2069
     * and draft-ietf-http-authentication-00.txt, but has subsequently
     * been removed. Here for backwards compatibility.
     */
    private static String calc_digest(RoRequest req, String A1_hash,
				      String nonce)
    {
	if (req.getStream() != null)
	    return "";

	int ct=-1, ce=-1, lm=-1, ex=-1, dt=-1;
	for (int idx=0; idx<req.getHeaders().length; idx++)
	{
	    String name = req.getHeaders()[idx].getName();
	    if (name.equalsIgnoreCase("Content-type"))
		ct = idx;
	    else if (name.equalsIgnoreCase("Content-Encoding"))
		ce = idx;
	    else if (name.equalsIgnoreCase("Last-Modified"))
		lm = idx;
	    else if (name.equalsIgnoreCase("Expires"))
		ex = idx;
	    else if (name.equalsIgnoreCase("Date"))
		dt = idx;
	}


	NVPair[] hdrs = req.getHeaders();
	byte[] entity_body = (req.getData() == null ? NUL : req.getData());
	MD5 entity_hash = new MD5();
	entity_hash.Update(entity_body);

	String entity_info = new MD5(req.getRequestURI() + ":" +
	     (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
	     entity_body.length + ":" +
	     (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
	     (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
	     (ex == -1 ? "" : hdrs[ex].getValue())).asHex();
	String entity_digest = A1_hash + ":" + nonce + ":" + req.getMethod() +
			":" + (dt == -1 ? "" : hdrs[dt].getValue()) +
			":" + entity_info + ":" + entity_hash.asHex();

	if (DebugAuth)
	{
	    System.err.println("Auth:  Entity-Info: '" + req.getRequestURI() + ":" +
		 (ct == -1 ? "" : hdrs[ct].getValue()) + ":" +
		 entity_body.length + ":" +
		 (ce == -1 ? "" : hdrs[ce].getValue()) + ":" +
		 (lm == -1 ? "" : hdrs[lm].getValue()) + ":" +
		 (ex == -1 ? "" : hdrs[ex].getValue()) +"'");
	    System.err.println("Auth:  Entity-Body: '" + entity_hash.asHex() + "'");
	    System.err.println("Auth:  Entity-Digest: '" + entity_digest + "'");
	}

	return new MD5(entity_digest).asHex();
    }


    /**
     * Handle discard token
     */
    private static boolean handle_discard(AuthorizationInfo prev, RoRequest req,
					  HttpHeaderElement discard)
    {
	if (discard != null  &&  prev != null)
	{
	    AuthorizationInfo.removeAuthorization(prev,
					    req.getConnection().getContext());
	    return true;
	}

	return false;
    }


    /**
     * Generate <var>num</var> bytes of random data.
     *
     * @param num  the number of bytes to generate
     * @return a byte array of random data
     */
    private static byte[] gen_random_bytes(int num)
    {
	// first try /dev/random
	try
	{
	    FileInputStream rnd = new FileInputStream("/dev/random");
	    DataInputStream din = new DataInputStream(rnd);
	    byte[] data = new byte[num];
	    din.readFully(data);
	    try { din.close(); } catch (IOException ioe) { }
	    return data;
	}
	catch (Throwable t)
	    { }

	/* This is probably a much better generator, but it can be awfully
	 * slow (~ 6 secs / byte on my old LX)
	 */
	//return new java.security.SecureRandom().getSeed(num);

	/* this is faster, but needs to be done better... */
	byte[] data = new byte[num];
	try
	{
	    long fm = Runtime.getRuntime().freeMemory();
	    data[0] = (byte) (fm & 0xFF);
	    data[1] = (byte) ((fm >>  8) & 0xFF);

	    int h = data.hashCode();
	    data[2] = (byte) (h & 0xFF);
	    data[3] = (byte) ((h >>  8) & 0xFF);
	    data[4] = (byte) ((h >> 16) & 0xFF);
	    data[5] = (byte) ((h >> 24) & 0xFF);

	    long time = System.currentTimeMillis();
	    data[6] = (byte) (time & 0xFF);
	    data[7] = (byte) ((time >>  8) & 0xFF);
	}
	catch (ArrayIndexOutOfBoundsException aioobe)
	    { }

	return data;
    }


    /**
     * Return the value of the first NVPair whose name matches the key
     * using a case-insensitive search.
     *
     * @param list an array of NVPair's
     * @param key  the key to search for
     * @return the value of the NVPair with that key, or null if not
     *         found.
     */
    private final static String getValue(NVPair[] list, String key)
    {
	int len = list.length;

	for (int idx=0; idx<len; idx++)
	    if (list[idx].getName().equalsIgnoreCase(key))
		return list[idx].getValue();

	return null;
    }

    /**
     * Return the index of the first NVPair whose name matches the key
     * using a case-insensitive search.
     *
     * @param list an array of NVPair's
     * @param key  the key to search for
     * @return the index of the NVPair with that key, or -1 if not
     *         found.
     */
    private final static int getIndex(NVPair[] list, String key)
    {
	int len = list.length;

	for (int idx=0; idx<len; idx++)
	    if (list[idx].getName().equalsIgnoreCase(key))
		return idx;

	return -1;
    }

    /**
     * Sets the value of the NVPair with the name that matches the key
     * (case-insensitive). If no name matches, a new entry is created.
     *
     * @param list an array of NVPair's
     * @param key  the name of the NVPair
     * @param val  the value of the new NVPair
     * @return the (possibly) new list
     */
    private final static NVPair[] setValue(NVPair[] list, String key, String val)
    {
	int idx = getIndex(list, key);
	if (idx == -1)
	{
	    idx = list.length;
	    list = Util.resizeArray(list, list.length+1);
	}

	list[idx] = new NVPair(key, val);
	return list;
    }


    /**
     * Split a list into an array of Strings, using sep as the
     * separator and removing whitespace around the separator.
     */
    private static String[] splitList(String str, String sep)
    {
	if (str == null)  return new String[0];

	StringTokenizer tok = new StringTokenizer(str, sep);
	String[] list = new String[tok.countTokens()];
	for (int idx=0; idx<list.length; idx++)
	    list[idx] = tok.nextToken().trim();

	return list;
    }


    /**
     * Produce a string of the form "A5:22:F1:0B:53"
     */
    static String hex(byte[] buf)
    {
	StringBuffer str = new StringBuffer(buf.length*3);
	for (int idx=0; idx<buf.length; idx++)
	{
	    str.append(Character.forDigit(buf[idx] >>> 4, 16));
	    str.append(Character.forDigit(buf[idx] & 16, 16));
	    str.append(':');
	}
	str.setLength(str.length()-1);

	return str.toString();
    }


    static final byte[] unHex(String hex)
    {
	byte[] digest = new byte[hex.length()/2];

	for (int idx=0; idx<digest.length; idx++)
	{
	    digest[idx] = (byte) (0xFF & Integer.parseInt(
				  hex.substring(2*idx, 2*(idx+1)), 16));
	}

	return digest;
    }
}


/**
 * This verifies the "rspauth" from draft-ietf-http-authentication-03
 */
class VerifyRspAuth implements HashVerifier, GlobalConstants
{
    private String     uri;
    private String     HA1;
    private String     alg;
    private String     nonce;
    private String     cnonce;
    private String     nc;
    private String     hdr;
    private RoResponse resp;


    public VerifyRspAuth(String uri, String HA1, String alg, String nonce,
			 String cnonce, String nc, String hdr, RoResponse resp)
    {
	this.uri    = uri;
	this.HA1    = HA1;
	this.alg    = alg;
	this.nonce  = nonce;
	this.cnonce = cnonce;
	this.nc     = nc;
	this.hdr    = hdr;
	this.resp   = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String auth_info = resp.getHeader(hdr);
	if (auth_info == null)
	    auth_info = resp.getTrailer(hdr);
	if (auth_info == null)
	    return;

	Vector pai;
	try
	    { pai = Util.parseHeader(auth_info); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }

	String qop;
	HttpHeaderElement elem = Util.getElement(pai, "qop");
	if (elem == null  ||  (qop = elem.getValue()) == null  ||
	    (!qop.equalsIgnoreCase("auth")  &&
	     !qop.equalsIgnoreCase("auth-int")))
	    return;

	elem = Util.getElement(pai, "rspauth");
	if (elem == null  ||  elem.getValue() == null) return;
	byte[] digest = DefaultAuthHandler.unHex(elem.getValue());

	elem = Util.getElement(pai, "cnonce");
	if (elem != null  &&  elem.getValue() != null  &&
	    !elem.getValue().equals(cnonce))
	    throw new IOException("Digest auth scheme: received wrong " +
				  "client-nonce '" + elem.getValue() +
				  "' - expected '" + cnonce + "'");

	elem = Util.getElement(pai, "nc");
	if (elem != null  &&  elem.getValue() != null  &&
	    !elem.getValue().equals(nc))
	    throw new IOException("Digest auth scheme: received wrong " +
				  "nonce-count '" + elem.getValue() +
				  "' - expected '" + nc + "'");

	String A1, A2;
	if (alg != null  &&  alg.equalsIgnoreCase("MD5-sess"))
	    A1 = new MD5(HA1 + ":" + nonce + ":" + cnonce).asHex();
	else
	    A1 = HA1;

	// draft-01 was: A2 = resp.getStatusCode() + ":" + uri;
	A2 = ":" + uri;
	if (qop.equalsIgnoreCase("auth-int"))
	    A2 += ":" + MD5.asHex(hash);
	A2 = new MD5(A2).asHex();

	hash = new MD5(A1 + ":" + nonce + ":" +  nc + ":" + cnonce + ":" +
		       qop + ":" + A2).Final();

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != digest[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      DefaultAuthHandler.hex(digest) +
				      " but calculated " +
				      DefaultAuthHandler.hex(hash));
	}

	if (DebugAuth)
	    System.err.println("Auth:  rspauth from " + hdr +
			       " successfully verified");
    }
}


/**
 * This verifies the "digest" from rfc-2069
 */
class VerifyDigest implements HashVerifier, GlobalConstants
{
    private String     HA1;
    private String     nonce;
    private String     method;
    private String     uri;
    private String     hdr;
    private RoResponse resp;


    public VerifyDigest(String HA1, String nonce, String method, String uri,
			String hdr, RoResponse resp)
    {
	this.HA1    = HA1;
	this.nonce  = nonce;
	this.method = method;
	this.uri    = uri;
	this.hdr    = hdr;
	this.resp   = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String auth_info = resp.getHeader(hdr);
	if (auth_info == null)
	    auth_info = resp.getTrailer(hdr);
	if (auth_info == null)
	    return;

	Vector pai;
	try
	    { pai = Util.parseHeader(auth_info); }
	catch (ParseException pe)
	    { throw new IOException(pe.toString()); }
	HttpHeaderElement elem = Util.getElement(pai, "digest");
	if (elem == null  ||  elem.getValue() == null)
	    return;

	byte[] digest = DefaultAuthHandler.unHex(elem.getValue());

	String entity_info = new MD5(
				uri + ":" +
				header_val("Content-Type", resp) + ":" +
				header_val("Content-Length", resp) + ":" +
				header_val("Content-Encoding", resp) + ":" +
				header_val("Last-Modified", resp) + ":" +
				header_val("Expires", resp)).asHex();
	hash = new MD5(HA1 + ":" + nonce + ":" + method + ":" +
		       header_val("Date", resp) +
		       ":" + entity_info + ":" + MD5.asHex(hash)).Final();

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != digest[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      DefaultAuthHandler.hex(digest) +
				      " but calculated " +
				      DefaultAuthHandler.hex(hash));
	}

	if (DebugAuth)
	    System.err.println("Auth:  digest from " + hdr +
			       " successfully verified");
    }


    private static final String header_val(String hdr_name, RoResponse resp)
	    throws IOException
    {
	String hdr = resp.getHeader(hdr_name);
	String tlr = resp.getTrailer(hdr_name);
	return (hdr != null ? hdr : (tlr != null ? tlr : ""));
    }
}


/**
 * This class implements a simple popup that request username and password
 * used for the "basic" and "digest" authentication schemes.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */
class BasicAuthBox extends Frame
{
    private final static String title = "Authorization Request";
    private Dimension           screen;
    private Label		line1, line2, line3;
    private TextField		user, pass;
    private int                 done;
    private final static int    OK = 1, CANCEL = 0;


    /**
     * Constructs the popup with two lines of text above the input fields
     */
    BasicAuthBox()
    {
	super(title);

        screen = getToolkit().getScreenSize();

	addNotify();
	addWindowListener(new Close());
	setLayout(new BorderLayout());

	Panel p = new Panel(new GridLayout(3,1));
	p.add(line1 = new Label());
	p.add(line2 = new Label());
	p.add(line3 = new Label());
	add("North", p);

	p = new Panel(new GridLayout(2,1));
	p.add(new Label("Username:"));
	p.add(new Label("Password:"));
	add("West", p);
	p = new Panel(new GridLayout(2,1));
	p.add(user = new TextField(30));
	p.add(pass = new TextField(30));
	pass.addActionListener(new Ok());
	pass.setEchoChar('*');
	add("East", p);

	GridBagLayout gb = new GridBagLayout();
	p = new Panel(gb);
	GridBagConstraints constr = new GridBagConstraints();
	Panel pp = new Panel();
	p.add(pp);
	constr.gridwidth = GridBagConstraints.REMAINDER;
	gb.setConstraints(pp, constr);
	constr.gridwidth = 1;
	constr.weightx = 1.0;
	Button b;
	p.add(b = new Button("  OK  "));
	b.addActionListener(new Ok());
	constr.weightx = 1.0;
	gb.setConstraints(b, constr);
	p.add(b = new Button("Clear"));
	b.addActionListener(new Clear());
	constr.weightx = 2.0;
	gb.setConstraints(b, constr);
	p.add(b = new Button("Cancel"));
	b.addActionListener(new Cancel());
	constr.weightx = 1.0;
	gb.setConstraints(b, constr);
	add("South", p);

	pack();
	setResizable(false);
    }


    /**
     * our event handlers
     */
    private class Ok implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    done = OK;
	    synchronized (BasicAuthBox.this) { BasicAuthBox.this.notifyAll(); }
	}
    }

    private class Clear implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    user.setText("");
	    pass.setText("");
	    user.requestFocus();
	}
    }

    private class Cancel implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    done = CANCEL;
	    synchronized (BasicAuthBox.this) { BasicAuthBox.this.notifyAll(); }
	}
    }


    private class Close extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    new Cancel().actionPerformed(null);
	}
    }


    /**
     * the method called by DefaultAuthHandler.
     *
     * @return the username/password pair
     */
    synchronized NVPair getInput(String l1, String l2, String l3)
    {
	line1.setText(l1);
	line2.setText(l2);
	line3.setText(l3);

	line1.invalidate();
	line2.invalidate();
	line3.invalidate();

	setResizable(true);
	pack();
	setResizable(false);
	setLocation((screen.width-getPreferredSize().width)/2,
		    (int) ((screen.height-getPreferredSize().height)/2*.7));
	user.requestFocus();
	setVisible(true);

	try { wait(); } catch (InterruptedException e) { }

	setVisible(false);

	NVPair result = new NVPair(user.getText(), pass.getText());
	user.setText("");
	pass.setText("");

	if (done == CANCEL)
	    return null;
	else
	    return result;
    }
}

