/*
 * @(#)Handler.java					0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient.http package 
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

package HTTPClient.http;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.IOException;
import HTTPClient.ProtocolNotSuppException;


/**
 * This class implements a URLStreamHandler for http URLs. With this you
 * can use the HTTPClient package as a replacement for the JDKs client.
 * To do so define the property java.protocol.handler.pkgs=HTTPClient .
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

public class Handler extends URLStreamHandler
{
    public Handler()  throws ProtocolNotSuppException
    {
	new HTTPClient.HTTPConnection("http", "", -1);
    }

    public URLConnection openConnection(URL url)
	    throws IOException, ProtocolNotSuppException
    {
        return new HTTPClient.HttpURLConnection(url);
    }
}

