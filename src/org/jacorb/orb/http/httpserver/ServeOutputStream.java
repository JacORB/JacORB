/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.orb.connection.http.httpserver;

import java.io.*;

public class ServeOutputStream extends OutputStream
    {

    private PrintStream out;
    private ServeConnection conn;

    public ServeOutputStream( OutputStream out, ServeConnection conn )
	{
	this.out = new PrintStream( out );
	this.conn = conn;
	}

    public void write( int b ) throws IOException
	{
	conn.writeHeaders();
	out.write( b );
	}

    public void write( byte[] b, int off, int len ) throws IOException
	{
	conn.writeHeaders();
	out.write( b, off, len );
	}

    public void flush() throws IOException
	{
	conn.writeHeaders();
	out.flush();
	}

    public void close() throws IOException
	{
	conn.writeHeaders();
	out.close();

	}

    public void print( String s ) throws IOException
	{
	conn.writeHeaders();
	out.print( s );
	}

    public void print( int i ) throws IOException
	{
	conn.writeHeaders();
	out.print( i );
	}

    public void print( long l ) throws IOException
	{
	conn.writeHeaders();
	out.print( l );
	}

    public void println( String s ) throws IOException
	{
	conn.writeHeaders();
	out.println( s );
	}

    public void println( int i ) throws IOException
	{
	conn.writeHeaders();
	out.println( i );
	}

    public void println( long l ) throws IOException
	{
	conn.writeHeaders();
	out.println( l );
	}

    public void println() throws IOException
	{
	conn.writeHeaders();
	out.println();
	}

    }






