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

package org.jacorb.orb.connection.http;

import java.net.*;
import java.util.*;
import java.io.*;

import org.jacorb.util.*;
import org.jacorb.orb.*;
import org.jacorb.orb.connection.*;

//Note: HTTPServerConnections are good for one incoming Request only!

public final class ServerConnection 
    extends org.jacorb.orb.connection.ServerConnection
{
    private boolean connected = true;
    private int readcount = 0;	
    private boolean readyParsed = false;
    Object oldrequest;
    Object requestwait = new Object();
    org.jacorb.orb.connection.http.httpserver.ServeConnection realCon;
    Socket mysock;
    
    private ORB orb = null;
    private int client_count = 0;
   
    public ServerConnection( org.jacorb.orb.ORB orb, 
                             boolean is_ssl,
                             Socket s, 
                             java.io.InputStream _in_stream) 
        throws IOException
    {
        this.orb = orb;
	mysock = s;
        isSSL = is_ssl;

	in_stream = new BufferedInputStream(_in_stream);
	realCon = 
            new org.jacorb.orb.connection.http.httpserver.ServeConnection(s,_in_stream);	
	String ip = mysock.getInetAddress().getHostAddress();
	if( ip.indexOf('/') > 0)
	    ip = ip.substring( ip.indexOf('/') + 1 );
	String host_and_port = ip + ":"+ mysock.getPort();
	String ssl = isSSL() ? "SSL " : ""; //bnv
	connection_info = host_and_port;
	client_count = 1;
	org.jacorb.util.Debug.output(1,"Accepted HTTP connection from " + host_and_port);

    }
  
    protected void abort()
	throws  EOFException
    {
        org.jacorb.util.Debug.output(3,"HTTPServerConnection to " + connection_info + " aborting...");
        throw new EOFException();
	
    }

    /** 
     * socket closing etc is done after each single HTTP Connection
     */

    public synchronized void closeConnection()
    {
        connected=false;
        realCon.close();
    }

    public boolean connected()
    {
        return connected;
    }

    public byte[] readBuffer() 
        throws java.io.IOException 
    {
        readcount++;
        in_stream = realCon.getInputStream();
        return super.readBuffer();
    }

    public  synchronized void reconnect()
	throws org.omg.CORBA.COMM_FAILURE
    {	
	connected=true;
    }

    /**
     * send a "close connection" message to the other side.
     */

    public synchronized void sendCloseConnection() 
        throws org.omg.CORBA.COMM_FAILURE
    {
        String iaddr=mysock.getInetAddress().toString()+mysock.getPort();
        //BasicAdapter ba = orb.getBasicAdapter();		

        try
        {
            realCon.answerRequest(Messages.closeConnectionMessage());
            realCon.close();	
        } 
        catch ( Exception e )
        {
            throw new org.omg.CORBA.COMM_FAILURE();
        }
    }   

    /**
     *
     */

    public void sendLocateReply( int request_id, 
                                 int status, 
                                 org.omg.CORBA.Object arg,
                                 int giop_minor ) 
        throws org.omg.CORBA.COMM_FAILURE
    {   
	/**
	 * called from dsi/ServerRequest
	 */

        synchronized( writeLock )
	{
            String iaddr=mysock.getInetAddress().toString()+mysock.getPort();
            // BasicAdapter ba = orb.getBasicAdapter();
	    try
	    {

		realCon.answerRequest(Messages.locateReplyMessage( request_id,
                                                                   status,
                                                                   arg,
                                                                   giop_minor ));
		realCon.close();
			
	    } 
	    catch ( Exception e )
	    {
		org.jacorb.util.Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }
	}
    }

    public void sendReply( ReplyOutputStream os )
        throws IOException
    {
	synchronized( writeLock )
	{
            String iaddr=mysock.getInetAddress().toString()+mysock.getPort();
            // BasicAdapter ba=orb.getBasicAdapter();
	    try
	    {
		byte buf [];
                //	if( Environment.serverInterceptMessages())
                //	{
                //	    buf = orb.server_messageIntercept_post(os.getBufferCopy());
                //	    realCon.answerRequest(buf);
                //	}
                //	else
                //	{
                // org.jacorb.util.Debug.output(3,"bypassing any message-level interceptors");
                if (os!=null)
                {
                    realCon.answerRequest(os.getBufferCopy());
                    os.release();
                }
                else
                { //dummy reply
                    byte[] dummy={0};
                    realCon.answerRequest(dummy);
                }
                //org.jacorb.util.Debug.output(1,"sendReply",os.getBuffer(true));

                //	}
		// org.jacorb.util.Debug.output(3,"bypassing any message-level interceptors");
		realCon.close();					    		
	    } 
	    catch ( Exception e )
	    {
		org.jacorb.util.Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }
	}
    }


    public void setTimeOut(int timeout)
	throws SocketException
    {
	//do nothing here
    }
}






