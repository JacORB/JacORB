/*
 * @(#)ContentMD5Module.java				0.3-2 18/06/1999
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
 * This module handles the Content-MD5 response header. If this header was
 * sent with a response and the entity isn't encoded using an unknown
 * transport encoding then an MD5InputStream is wrapped around the response
 * input stream. The MD5InputStream keeps a running digest and checks this
 * against the expected digest from the Content-MD5 header the stream is
 * closed. An IOException is thrown at that point if the digests don't match.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class ContentMD5Module implements HTTPClientModule, GlobalConstants
{
    // Constructors

    ContentMD5Module()
    {
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
    {
	return RSP_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
		throws IOException, ModuleException
    {
	if (req.getMethod().equals("HEAD"))
	    return;

	String md5_digest = resp.getHeader("Content-MD5");
	String trailer    = resp.getHeader("Trailer");
	boolean md5_tok = false;
	try
	{
	    if (trailer != null)
		md5_tok = Util.hasToken(trailer, "Content-MD5");
	}
	catch (ParseException pe)
	    { throw new ModuleException(pe.toString()); }

	if ((md5_digest == null  &&  !md5_tok)  ||
	    resp.getHeader("Transfer-Encoding") != null)
	    return;

	if (DebugMods)
	{
	    if (md5_digest != null)
		System.err.println("CMD5M: Received digest: " + md5_digest +
				    " - pushing md5-check-stream");
	    else
		System.err.println("CMD5M: Expecting digest in trailer " +
				    " - pushing md5-check-stream");
	}

	resp.inp_stream = new MD5InputStream(resp.inp_stream,
					     new VerifyMD5(resp));
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)
    {
    }
}


class VerifyMD5 implements HashVerifier, GlobalConstants
{
    RoResponse resp;


    public VerifyMD5(RoResponse resp)
    {
	this.resp = resp;
    }


    public void verifyHash(byte[] hash, long len)  throws IOException
    {
	String hdr;
	try
	{
	    if ((hdr = resp.getHeader("Content-MD5")) == null)
		hdr = resp.getTrailer("Content-MD5");
	}
	catch (IOException ioe)
	    { return; }		// shouldn't happen

	if (hdr == null)  return;

	hdr = hdr.trim();
	byte[] ContMD5 = new byte[hdr.length()];
	hdr.getBytes(0, ContMD5.length, ContMD5, 0);
	ContMD5 = Codecs.base64Decode(ContMD5);

	for (int idx=0; idx<hash.length; idx++)
	{
	    if (hash[idx] != ContMD5[idx])
		throw new IOException("MD5-Digest mismatch: expected " +
				      hex(ContMD5) + " but calculated " +
				      hex(hash));
	}

	if (DebugMods)
	    System.err.println("CMD5M: hash successfully verified");
    }


    /**
     * Produce a string of the form "A5:22:F1:0B:53"
     */
    private static String hex(byte[] buf)
    {
	StringBuffer str = new StringBuffer(buf.length*3);
	for (int idx=0; idx<buf.length; idx++)
	{
	    str.append(Character.forDigit((buf[idx] >>> 4) & 15, 16));
	    str.append(Character.forDigit(buf[idx] & 15, 16));
	    str.append(':');
	}
	str.setLength(str.length()-1);

	return str.toString();
    }
}

