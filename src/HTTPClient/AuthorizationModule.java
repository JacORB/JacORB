/*
 * @(#)AuthorizationModule.java				0.3-2 18/06/1999
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
import java.net.ProtocolException;
import java.util.Hashtable;


/**
 * This module handles authentication requests. Authentication info is
 * preemptively sent if any suitable candidate info is available. If a
 * request returns with an appropriate status (401 or 407) then the
 * necessary info is sought from the AuthenticationInfo class.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class AuthorizationModule implements HTTPClientModule, GlobalConstants
{
    /** This holds the current Proxy-Authorization-Info for each
        HTTPConnection */
    private static Hashtable proxy_cntxt_list = new Hashtable();

    /** counters for challenge and auth-info lists */
    private int	auth_lst_idx,
		prxy_lst_idx,
		auth_scm_idx,
		prxy_scm_idx;

    /** the last auth info sent, if any */
    private AuthorizationInfo auth_sent;
    private AuthorizationInfo prxy_sent;

    /** is the info in auth_sent a preemtive guess or the result of a 4xx */
    private boolean auth_from_4xx;
    private boolean prxy_from_4xx;

    /** guard against bugs on both our side and the server side */
    private int num_tries;


    // Constructors

    /**
     * Initialize counters for challenge and auth-info lists.
     */
    AuthorizationModule()
    {
	auth_lst_idx = 0;
	prxy_lst_idx = 0;
	auth_scm_idx = 0;
	prxy_scm_idx = 0;

	auth_sent = null;
	prxy_sent = null;

	auth_from_4xx = false;
	prxy_from_4xx = false;

	num_tries  = 0;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	HTTPConnection con = req.getConnection();
	AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();
	AuthorizationInfo guess;


	// Preemptively send proxy authorization info

	Proxy: if (con.getProxyHost() != null  &&  !prxy_from_4xx)
	{
	    Hashtable proxy_auth_list = Util.getList(proxy_cntxt_list,
					     req.getConnection().getContext());
	    guess = (AuthorizationInfo) proxy_auth_list.get(
				    con.getProxyHost()+":"+con.getProxyPort());
	    if (guess == null)  break Proxy;

	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException atnie)
		    { break Proxy; }
		if (guess == null) break Proxy;
	    }

	    int idx;
	    NVPair[] hdrs = req.getHeaders();
	    for (idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Proxy-Authorization"))
		    break;	// already set
	    }
	    if (idx == hdrs.length) // add proxy-auth header
	    {
		hdrs = Util.resizeArray(hdrs, idx+1);
		req.setHeaders(hdrs);
	    }

	    hdrs[idx] = new NVPair("Proxy-Authorization", guess.toString());

	    prxy_sent     = guess;
	    prxy_from_4xx = false;

	    if (DebugMods)
		System.err.println("AuthM: Preemptively sending " +
				   "Proxy-Authorization '" + guess + "'");
	}


	// Preemptively send authorization info

	Auth: if (!auth_from_4xx)
	{
	    guess = AuthorizationInfo.findBest(req);
	    if (guess == null)  break Auth;

	    if (auth_handler != null)
	    {
		try
		    { guess = auth_handler.fixupAuthInfo(guess, req, null, null); }
		catch (AuthSchemeNotImplException atnie)
		    { break Auth; }
		if (guess == null) break Auth;
	    }

	    int idx;
	    NVPair[] hdrs = req.getHeaders();
	    for (idx=0; idx<hdrs.length; idx++)
	    {
		if (hdrs[idx].getName().equalsIgnoreCase("Authorization"))
		    break;		// already set
	    }
	    if (idx == hdrs.length)	// add auth header
	    {
		hdrs = Util.resizeArray(hdrs, idx+1);
		req.setHeaders(hdrs);
	    }

	    hdrs[idx] = new NVPair("Authorization", guess.toString());

	    auth_sent     = guess;
	    auth_from_4xx = false;

	    if (DebugMods)
		System.err.println("AuthM: Preemptively sending Authorization '"
				   + guess + "'");
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
		throws IOException
    {
	/* If auth info successful update path list. Note: if we
	 * preemptively sent auth info we don't actually know if
	 * it was necessary. Therefore we don't update the path
	 * list in this case; this prevents it from being
	 * contaminated. If the info was necessary, then the next
	 * time we access this resource we will again guess the
	 * same info and send it.
	 */
	if (resp.getStatusCode() != 401  &&  resp.getStatusCode() != 407)
	{
	    if (auth_sent != null  &&  auth_from_4xx)
	    {
		try
		{
		    AuthorizationInfo.getAuthorization(auth_sent, req, resp,
				    false).addPath(req.getRequestURI());
		}
		catch (AuthSchemeNotImplException asnie)
		    { /* shouldn't happen */ }
	    }

	    // reset guard if not an auth challenge
	    num_tries = 0;
	}

	auth_from_4xx = false;
	prxy_from_4xx = false;

	if (resp.getHeader("WWW-Authenticate") == null)
	{
	    auth_lst_idx = 0;
	    auth_scm_idx = 0;
	}

	if (resp.getHeader("Proxy-Authenticate") == null)
	{
	    prxy_lst_idx = 0;
	    prxy_scm_idx = 0;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
		throws IOException, AuthSchemeNotImplException
    {
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthHeaders(resp, req, auth_sent, prxy_sent);


	// handle 401 and 407 response codes

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 401: // Unauthorized
	    case 407: // Proxy Authentication Required

		// guard against infinite retries due to bugs

		num_tries++;
		if (num_tries > 10)
		    throw new ProtocolException("Bug in authorization handling: server refused the given info 10 times");


		if (DebugMods) System.err.println("AuthM: Handling status: " +
					    sts + " " + resp.getReasonLine());


		// handle WWW-Authenticate

		int[] idx_arr = { auth_lst_idx,	// hack to pass by ref
				  auth_scm_idx};
		auth_sent = setAuthHeaders(resp.getHeader("WWW-Authenticate"),
					   req, resp, "Authorization", idx_arr,
					   auth_sent);
		if (auth_sent != null)
		    auth_from_4xx = true;
		auth_lst_idx = idx_arr[0];
		auth_scm_idx = idx_arr[1];


		// handle Proxy-Authenticate

		idx_arr[0] = prxy_lst_idx;	// hack to pass by ref
		idx_arr[1] = prxy_scm_idx;
		prxy_sent = setAuthHeaders(resp.getHeader("Proxy-Authenticate"),
					   req, resp, "Proxy-Authorization",
					   idx_arr, prxy_sent);
		if (prxy_sent != null)
		    prxy_from_4xx = true;
		prxy_lst_idx = idx_arr[0];
		prxy_scm_idx = idx_arr[1];

		if (prxy_sent != null)
		{
		    HTTPConnection con = req.getConnection();
		    Util.getList(proxy_cntxt_list, con.getContext())
			.put(con.getProxyHost()+":"+con.getProxyPort(),
			     prxy_sent);
		}


		if (req.getStream() == null  &&
		    (auth_sent != null  ||  prxy_sent != null))
		{
		    try { resp.getInputStream().close(); }
		    catch (IOException ioe) { }

		    if (DebugMods)
		    {
			if (auth_sent != null)
			    System.err.println("AuthM: Resending request " +
					       "with Authorization '" +
					       auth_sent + "'");
			else
			    System.err.println("AuthM: Resending request " +
					       "with Proxy-Authorization '" +
					       prxy_sent + "'");
		    }

		    return RSP_REQUEST;
		}


		// check for headers

		if (auth_sent == null  &&  prxy_sent == null  &&
		    resp.getHeader("WWW-Authenticate") == null  &&
		    resp.getHeader("Proxy-Authenticate") == null)
		{
		    if (resp.getStatusCode() == 401)
			throw new ProtocolException("Missing WWW-Authenticate header");
		    else
			throw new ProtocolException("Missing Proxy-Authenticate header");
		}

		if (DebugMods)
		{
		    if (req.getStream() == null)
			System.err.println("AuthM: status " + sts + " not " +
					   "handled - request has an output " +
					   "stream");
		    else
			System.err.println("AuthM: No Auth Info found - " +
					   "status " + sts + " not handled");
		}

		return RSP_CONTINUE;

	    default:

		return RSP_CONTINUE;
	}
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)  throws IOException
    {
	// Let the AuthHandler handle any Authentication headers.

	AuthorizationHandler h = AuthorizationInfo.getAuthHandler();
	if (h != null)
	    h.handleAuthTrailers(resp, req, auth_sent, prxy_sent);
    }


    /**
     * Handles authentication requests and sets the authorization headers.
     * It tries to retrieve the neccessary parameters from AuthorizationInfo,
     * and failing that calls the AuthHandler. Handles multiple authentication
     * headers.
     *
     * @param  auth_str the authentication header field returned by the server.
     * @param  req      the Request used
     * @param  resp     the full Response received
     * @param  header   the header name to use in the new headers array.
     * @param  idx_arr  an array of indicies holding the state of where we
     *                  are when handling multiple authorization headers.
     * @param  prev     the previous auth info sent, or null if none
     * @return the new credentials, or null if none found
     * @exception ProtocolException if <var>auth_str</var> is null.
     * @exception AuthSchemeNotImplException if thrown by the AuthHandler.
     */
    private AuthorizationInfo setAuthHeaders(String auth_str, Request req,
					     RoResponse resp, String header,
					     int[] idx_arr,
					     AuthorizationInfo prev)
	throws ProtocolException, AuthSchemeNotImplException
    {
	if (auth_str == null)  return null;

	// get the list of challenges the server sent
	AuthorizationInfo[] challenge =
			AuthorizationInfo.parseAuthString(auth_str, req, resp);

	if (DebugMods)
	{
	    System.err.println("AuthM: parsed " + challenge.length +
			       " challenges:");
	    for (int idx=0; idx<challenge.length; idx++)
		System.err.println("AuthM: Challenge " + challenge[idx]);
	}


	/* some servers expect a 401 to invalidate sent credentials.
	 * However, only do this for Basic scheme (because e.g. digest
	 * "stale" handling will fail otherwise)
	 */
	if (prev != null  &&  prev.getScheme().equalsIgnoreCase("Basic"))
	{
	    for (int idx=0; idx<challenge.length; idx++)
		if (prev.getRealm().equals(challenge[idx].getRealm())  &&
		    prev.getScheme().equalsIgnoreCase(challenge[idx].getScheme()))
		    AuthorizationInfo.removeAuthorization(prev,
					      req.getConnection().getContext());
	}

	AuthorizationInfo    credentials  = null;
	AuthorizationHandler auth_handler = AuthorizationInfo.getAuthHandler();

	// try next auth challenge in list
	while (credentials == null  &&  idx_arr[0] != -1)
	{
	    credentials =
		AuthorizationInfo.getAuthorization(challenge[idx_arr[0]], req,
						   resp, false);
	    if (auth_handler != null  &&  credentials != null)
		credentials = auth_handler.fixupAuthInfo(credentials, req,
						challenge[idx_arr[0]], resp);
	    if (++idx_arr[0] == challenge.length)
		idx_arr[0] = -1;
	}


	// if we don't have any credentials then prompt the user
	if (credentials == null)
	{
	    for (int idx=0; idx<challenge.length; idx++)
	    {
		try
		{
		    credentials = AuthorizationInfo.queryAuthHandler(
					    challenge[idx_arr[1]], req, resp);
		}
		catch (AuthSchemeNotImplException atnie)
		{
		    if (idx == challenge.length-1)
			throw atnie;
		    continue;
		}
		break;
	    }
	    if (++idx_arr[1] == challenge.length)
		idx_arr[1] = 0;
	}

	// if we still don't have any credentials then give up
	if (credentials == null)
	    return null;

	// find auth info
	int auth_idx;
	NVPair[] hdrs = req.getHeaders();
	for (auth_idx=0; auth_idx<hdrs.length; auth_idx++)
	{
	    if (hdrs[auth_idx].getName().equalsIgnoreCase(header))
		break;
	}

	// add credentials to headers
	if (auth_idx == hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, auth_idx+1);
	    req.setHeaders(hdrs);
	}
	hdrs[auth_idx] = new NVPair(header, credentials.toString());

	return credentials;
    }
}

