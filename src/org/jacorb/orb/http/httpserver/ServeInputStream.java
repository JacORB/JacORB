package org.jacorb.orb.connection.http.httpserver;

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








