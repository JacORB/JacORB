/*
 * @(#)DefaultModule.java				0.3-2 18/06/1999
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


/**
 * This is the default module which gets called after all other modules
 * have done their stuff.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class DefaultModule implements HTTPClientModule, GlobalConstants
{
    /** number of times the request will be retried */
    private int req_timeout_retries;


    // Constructors

    /**
     * Three retries upon receipt of a 408.
     */
    DefaultModule()
    {
	req_timeout_retries = 3;
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
	    throws IOException
    {
	/* handle various response status codes until satisfied */

	int sts  = resp.getStatusCode();
	switch(sts)
	{
	    case 408: // Request Timeout

		if (req_timeout_retries-- == 0  ||  req.getStream() != null)
		{
		    if (DebugMods)
			System.err.println("DefM:  Status " + sts + " " +
				    resp.getReasonLine() + " not handled - " +
				    "maximum number of retries exceeded");

		    return RSP_CONTINUE;
		}
		else
		{
		    if (DebugMods)
			System.err.println("DefM:  Handling " + sts + " " +
					    resp.getReasonLine() + " - " +
					    "resending request");

		    return RSP_REQUEST;
		}

	    case 411: // Length Required
		if (req.getStream() != null  &&
		    req.getStream().getLength() == -1)
		    return RSP_CONTINUE;

		try { resp.getInputStream().close(); }
		catch (IOException ioe) { }
		if (req.getData() != null)
		    throw new ProtocolException("Received status code 411 even"+
					    " though Content-Length was sent");

		if (DebugMods)
		    System.err.println("DefM:  Handling " + sts + " " +
					resp.getReasonLine() + " - resending " +
					"request with 'Content-length: 0'");

		req.setData(new byte[0]);	// will send Content-Length: 0
		return RSP_REQUEST;

	    case 505: // HTTP Version not supported
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
    public void trailerHandler(Response resp, RoRequest req)
    {
    }
}

