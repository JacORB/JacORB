/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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
 */

package org.jacorb.orb.connection;


import java.io.*;
import java.net.*;
import java.util.*;

import org.jacorb.orb.*;
import org.jacorb.util.Debug;
import org.jacorb.util.*;

/**
 * This class manages connections.<br>
 * It writes to an OutputStream or receives from an InputStream,<br>
 * converting type representations from internal to external or<br>
 * vice versa<p>
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

public class ServerConnection
    extends AbstractConnection
{
    protected String connection_info = null;
    protected InputStream in_stream;
    BufferedOutputStream out_stream;

    /** write lock */
    public Object writeLock = new Object();

    //contains RequestOutputStreams instead of byte[]

    protected boolean littleEndian;
    protected boolean isSSL;
    protected Socket mysock = null;
    private byte [] header = new byte[ Messages.MSG_HEADER_SIZE ];
    
    /** 
     * dummy constructor
     */

    public ServerConnection(){}

    /**
     * class Constructor, establishes a connection over a given socket.
     * It is called from ConnectionManager.getConnection for clients and
     * from BasicAdapter for servers
     *
     * @param <code>Socket s</code>
     * @param <code>boolean _is_server</code> - if opened from the server side.<br>
     * @except <code>java.io.IOException</code>
     */	
	
    public ServerConnection( org.jacorb.orb.ORB orb,
                             boolean is_ssl, 
                             java.net.Socket s )
        throws IOException
    {
        this( orb,
              is_ssl, 
              s,
              new BufferedInputStream( s.getInputStream()));
    }


    public ServerConnection( org.jacorb.orb.ORB orb,
                             boolean is_ssl,
                             java.net.Socket s, 
                             InputStream in  )
        throws IOException
    {
        this.orb = orb;
        this.isSSL = is_ssl;

        mysock = s;
        in_stream = in;
        out_stream = 
            new BufferedOutputStream( mysock.getOutputStream(), 
                                     Environment.outBufSize() );

        String ip = mysock.getInetAddress().getHostAddress();
        if ( ip.indexOf('/') > 0 ) 
            ip = ip.substring( ip.indexOf('/') + 1 );

        String host_and_port = ip + ":"+ mysock.getPort();
        String ssl = isSSL() ? "SSL " : ""; //bnv
        connection_info = host_and_port;

        Debug.output(1,"Accepted " + ssl + "connection from " + host_and_port);
    }

    public boolean isSSL()
    {
        return isSSL;
    }
	
    /** don't use, this is a temporary hack! */
	
    public Socket getSocket()
    {
        return mysock;
    }


    public boolean connected()
    {
        return mysock != null;
    }

    /**
     * Called on server side connection to evaluate requext contexts and
     * set TCS if one found. Returns true if TCS was correctly set.
     * Currently it's called from BasicAdapter.
     */

    public boolean setServerCodeSet( org.omg.IOP.ServiceContext [] ctx )
    {
        if( !Environment.charsetScanCtx() ) 
	    return false;

	// search all contexts until TAG_CODE_SETS found
	for( int i = 0; i < ctx.length; i++ )
	{
	    if( ctx[i].context_id != org.omg.IOP.TAG_CODE_SETS.value ) 
		continue;
			
	    // TAG_CODE_SETS found, demarshall
	    CDRInputStream is = new CDRInputStream( orb, ctx[i].context_data);
	    is.setLittleEndian(is.read_boolean());
	    org.omg.CONV_FRAME.CodeSetContext csx =
		org.omg.CONV_FRAME.CodeSetContextHelper.read(is);

	    TCSW = csx.wchar_data;
	    TCS = csx.char_data;
	    markTcsNegotiated();

	    Debug.output(4, "TCS set: "+CodeSet.csName(TCS)+","+
                                     CodeSet.csName(TCSW));
	    return true;
	}
	return false; // no TAG_CODE_SETS here
    }
	

    /**
     *	Close a connection. Should only be done directly if the connection
     *  is broken or idCount has reached zero. Otherwise, use releaseConnection.
     */

    public synchronized void closeConnection()
    {
	Debug.output(1,"Closing connection to " + connection_info);
	try
	{
	    try 
	    {
		if( mysock != null )
		    mysock.close();
	    } 
	    catch(IOException e) 
	    {
		Debug.output(4, e);
	    }
	    
	    if( in_stream != null )
		in_stream.close();

	    Debug.output(4,"Closing connection to " + 
                                     connection_info + " (in streams closed)");

	    if( out_stream != null )
		out_stream.close();

	    Debug.output(4,"Closing connection to " + 
                                     connection_info + " (out streams closed)");

	} 
	catch ( IOException e)
	{
	    Debug.output(4,e);
	}

	in_stream = null;
	out_stream = null;
	mysock = null;

	Debug.output(3,"Closing connection to " + 
                                 connection_info + " (sockets closed)");

    }


    /** 
     * called to notify all waiting threads that the connection is
     * broken
     */

    private void abort()
	throws  EOFException
    {
	Debug.output(3,"Connection to " + 
                                 connection_info + " aborting...");
	throw new EOFException();
    }

    /**
     * low-level input routine, not MT-safe, used by the ReplyReceptor object
     * for this connection
     */

    public byte[] readBuffer() 
	throws IOException 
    {
	for( int i = 0; i < Messages.MSG_HEADER_SIZE; i++ )
	{
	    int input = -1;

	    try
	    {
		input = in_stream.read();
	    } 
	    catch( java.io.InterruptedIOException ioint )
	    {
		/* if a client-side time out has been set we have to
		   abort. NOTE: outstanding replies are lost! */
		
		Debug.output(3,"Connection timed out");
	    }
	    catch( Exception e )
	    {
		Debug.output(3,e);
	    }
	    if( input < 0 )		
	    {
		abort();
	    }
	    header[i]=(byte)input;
	}       

	/* check for SECIOP vs. GIOP headers */

	if( (char)header[0] == 'S' && (char)header[1] == 'E' && 
	    (char)header[2] =='C' && (char)header[3] == 'P' )
	{
	    throw new IOException("Cannot handle SECIOP messages yet...");
	}
	else if( (char)header[0] == 'G' && (char)header[1] == 'I' && 
		 (char)header[2] =='O' && (char)header[3] == 'P')
	{
	    /* determine message size, but respect byte order! */
	    int msg_size = 0;
	    
	    if( header[6] == 0 ) // big-endian
		msg_size =  ((0xff & header[8]) << 24) + 
		    ((0xff & header[9]) << 16) + 
		    ((0xff & header[10])<< 8) + 
		    ((0xff & header[11]));
	    else	// little-endian
		msg_size =  ((0xff & header[11]) << 24) + 
		    ((0xff & header[10]) << 16) + 
		    ((0xff & header[9])  << 8) + 
		    ((0xff & header[8]));
	    
	    if( msg_size < 0 )
	    {
		abort();
	    }
	    
	    int bufSize = msg_size + Messages.MSG_HEADER_SIZE;
	    byte[] inbuf = BufferManager.getInstance().getBuffer(bufSize);
	    
	    /* copy header */
	    
	    for( int ii = 0; ii < Messages.MSG_HEADER_SIZE; ii++ )
	    {
		inbuf[ii] = (byte)header[ii];
	    }

	    int read = Messages.MSG_HEADER_SIZE;

	    //	    while( read < inbuf.length )
	    while( read < bufSize )
	    {
		int n = -1;
		try
		{
		    n = in_stream.read(inbuf, read, bufSize-read);
		} 
		catch( IOException ie )
		{
		    Debug.output(4,ie);
		}

		if( n<0 )
		{
		    abort();
		}  
		read += n;
	    }
	    return inbuf;
	}
	else 
	{
	    Debug.output( 0, "Unknown message header type detected by " + 
				      Thread.currentThread().getName() + 
                                      ", discarding ", header );
	    return readBuffer();
	    //	    throw new IOException("Unknown message header type!");
	}
    }


    /**
     *
     */

    public String getInfo()
    {
	return connection_info;
    }


    /**
     * send a "close connection" message to the other side.
     */

    public synchronized void sendCloseConnection() 
	throws org.omg.CORBA.COMM_FAILURE 
    {

	try
	{
	    out_stream.write( Messages.closeConnectionMessage());
	    out_stream.flush();
	    closeConnection();
	} 
	catch ( Exception e )
	{
	    throw new org.omg.CORBA.COMM_FAILURE();
	}

    }

    /**
     *
     */

    public void sendLocateReply( int request_id, int status, org.omg.CORBA.Object arg ) 
	throws org.omg.CORBA.COMM_FAILURE 
    {
	synchronized( writeLock )
	{
	    try
	    {
		out_stream.write( Messages.locateReplyMessage( request_id, status, arg ));
		out_stream.flush();
	    } 
	    catch ( Exception e )
	    {
		Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }
	}
    }

    /**
     * called from dsi/ServerRequest
     */


    public void sendReply( ReplyOutputStream os ) 
	throws IOException 
    {
	synchronized( writeLock )
	{
	    try
	    {	     
		if ( org.jacorb.util.Environment.verbosityLevel() > 4)
		{
		    //This is a costly op, since it involves buffer copying!
		    Debug.output(5,"send reply", os.getBufferCopy());
		}

		os.write_to(out_stream);		
		os.release();
	    } 
	    catch ( Exception e )
	    {
		Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }
	}
    }


    public void setTimeOut(int timeout)
	throws SocketException
    {
	mysock.setSoTimeout(timeout);
    }

    public BufferedOutputStream get_out_stream()
    {
	return out_stream;
    }

    public void writeDirectly(byte[] buffer,int len) 
	throws IOException
    {
	get_out_stream().write(buffer,0, len);
	get_out_stream().flush();
    }



}



