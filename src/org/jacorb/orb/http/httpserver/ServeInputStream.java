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

public class ServeInputStream extends InputStream
    {

    private InputStream in;

    public ServeInputStream( InputStream in )
	{
	this.in = in;
	}

    public int readLine( byte[] b, int off, int len ) throws IOException
	{
	int off2 = off;
	while ( off2 - off < len )
	    {
	    int r = read();
	    if ( r == -1 )
		{
		if (off2 == off )
		    return -1;
		break;
		}
	    if ( r == 13 )
		continue;
	    if ( r == 10 )
		break;
	    b[off2] = (byte) r;
	    ++off2;
	    }
	return off2 - off;
	}

    public int read() throws IOException
	{
	int b=in.read();
	return b;
	}

    public int read( byte[] b, int off, int len ) throws IOException
	{
	return in.read( b, off, len );
	}

    public int available() throws IOException
	{
	return in.available();
	}

    public void close() throws IOException
	{
	in.close();
	}

    }








