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
