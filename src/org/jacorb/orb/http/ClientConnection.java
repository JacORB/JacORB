/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose, Sebastian Mueller.
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

package org.jacorb.orb.http;

import java.net.*;
import java.util.*;
import java.io.*;

import org.jacorb.util.*;
import org.jacorb.orb.*;
import org.jacorb.orb.giop.*;

/**
 *   This class tunnels a GIOP request in HTTP.
 * @author Sebastian Mueller
 * @version $Id$
 */

public final class ClientConnection 
    extends org.jacorb.orb.giop.ClientConnection
{
    static int counter = 0;
    int mycounter = counter++; 
    boolean connected = true;
    Object notifier;

    //HTTP
    HTTPClient.HTTPResponse rsp;
    String host=null;
    int port;
    
    private ORB orb = null;

    public ClientConnection( String _host, 
                             int _port,
                             org.jacorb.orb.factory.SocketFactory factory,
                             ORB orb )
    {
        this.orb = orb;
        host=_host;
        port=_port;
        connection_info=host+":"+port;
        
        socket_factory = factory;
	   
    }   
    protected void abort()
	throws  java.io.EOFException
    {
	
	org.jacorb.util.Debug.output(3,"HTTPClient Connection "+mycounter+" to " + 
                                 connection_info + " aborting...->Ex");
	
	Enumeration keys = replies.keys();
	int lost_replies = 0;

	while(keys.hasMoreElements()) 
	{
	    ReplyInputStream client =
		(ReplyInputStream)replies.get(keys.nextElement());
	    client.cancel( false );
	    lost_replies++;
	}

	replies.clear();
	buffers.clear();

	if( lost_replies > 0 )
	    org.jacorb.util.Debug.output(2,"Lost " + lost_replies + " outstanding replies");
	
	throw new java.io.EOFException();
    }

    public synchronized void closeConnection()
    {
        connected=false;
    }

    public boolean connected()
    {
        //		return connected;		
        return true;
    }
    public Hashtable get_buffers(){
        return buffers;
    }
   
    public Hashtable get_replies(){
        return replies;
    }


    /* readBuffer is only called by receiveReply
     */
    public synchronized byte[] readBuffer() 
	throws IOException 
    {
	
        while(repReceptor==null) { 
            try {
	    	Thread.sleep(100); //make sure repReceptor is there
	    }catch(InterruptedException iex){}
        }
		
		
	synchronized(notifier){
            while(rsp==null){
                try{
                    notifier.wait();
                }catch(InterruptedException iex){}
            }
	}
	try{		
            in_stream=new BufferedInputStream(rsp.getInputStream());
	}catch(Exception e){
            org.jacorb.util.Debug.output(1,"This is a Client->Server HTTP Connection. ReadBuffer calls can only be done after some send call+receiveReply()");
	}
	//in_stream.mark(2048);
	//int i;
	//while((i=in_stream.read())>0){
	//	System.out.print(i+" ");
	//}
	//in_stream.reset();
	return super.readBuffer();
    }

    //TODO!
    public synchronized void reconnect()
	throws org.omg.CORBA.COMM_FAILURE
    {	
	org.jacorb.util.Debug.output(1,"Trying to reconnect to " + connection_info);
	//send waiting buffers
	
    }

    public LocateReplyInputStream sendLocateRequest( LocateRequestOutputStream os ) 
        throws org.omg.CORBA.COMM_FAILURE
    {       	
	synchronized( writeLock )
	{
            LocateReplyInputStream rep = null;	    
            try
	    {
                rsp = null;
		notifier=new Object();
		repReceptor=new ReplyReceptor(this,true);
		byte buf [] =  os.getBufferCopy();
		rep = new LocateReplyInputStream(this.orb, os.requestId());
		Integer key = new Integer( os.requestId() );
		buffers.put( key, os ); //changed in 1.1 from byte[]->CDR
		replies.put( key, rep );
		
		synchronized(notifier){
                    HTTPClient.HTTPConnection con = new HTTPClient.HTTPConnection("http://"+host+":"+port);
                    rsp = con.Post("",buf);
                    java.lang.Thread.yield();
                    notifier.notifyAll(); //notify replyRepector about ready http-reply object
		}
			
			
            } 
	    catch ( Exception e )
	    {
		org.jacorb.util.Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE
                    (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }		
	    return rep;
	}
	
    }

    /** writes the arguments of method calls to the output stream */
	
    public org.omg.CORBA.portable.InputStream sendRequest( org.omg.CORBA.Object o,
							   RequestOutputStream os ) 
        throws org.omg.CORBA.COMM_FAILURE{
        synchronized( writeLock )
	{
            ReplyInputStream rep = null;

	    try
	    {		
                rsp = null;
	    	notifier=new Object();
                repReceptor=new ReplyReceptor(this,true);
		byte buf [];
		int size = -1;

		//if( Environment.clientInterceptMessages())
		//{
		//    org.jacorb.util.Debug.output(4, " passing buffer to interceptors " );
		//    buf = orb.client_messageIntercept_pre( o, os.getBufferCopy());
		//    size = buf.length;
		//}
		//else
		//{
                buf = os.getBufferCopy();
                size = buf.length;
		//}

		if( os.response_expected() )
		{
		    rep = new ReplyInputStream(this.orb, os.requestId());
		    Integer key = new Integer( os.requestId() );
		    buffers.put( key, os );
		    replies.put( key, rep );
		}
		
		synchronized(notifier){
                    HTTPClient.HTTPConnection con = new HTTPClient.HTTPConnection(new URL("http://"+host+":"+port));
                    rsp = con.Post("",buf);
                    java.lang.Thread.yield();
                    notifier.notifyAll(); //notify replyRepector about ready http-reply object
		}
				

	        
	    }
	    catch ( Exception e )
	    {		    
		org.jacorb.util.Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE
                    (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }		
	    
	
            return rep;  
        }
    }


    public  void setTimeOut(int timeout)
	throws SocketException
    {
	//do nothing here
    }

    /* 
     * response handling has to be done by caller
     */
    public void  writeDirectly(byte[] data,int len) throws IOException{
	synchronized( writeLock )
	{
            org.jacorb.util.Debug.output(2,"Sending request with writeDirectly");
	    try
	    {		
	 
		//create buffer
	   
		notifier= new Object();	
		repReceptor=new ReplyReceptor(this,true);
		byte buf []=new byte[len];
		System.arraycopy(data,0,buf,0,len);
                int size = len;
		
		
		synchronized(notifier){
                    HTTPClient.HTTPConnection con = new HTTPClient.HTTPConnection(new URL("http://"+host+":"+port));
                    rsp = con.Post("",buf);
                    java.lang.Thread.yield();
                    notifier.notifyAll(); //notify replyRepector about ready http-reply object
		}
				

		//org.jacorb.util.Debug.output(5,"sendreq",buf,size);
	    
	        
	    }
	    catch ( Exception e )
	    {		    
		org.jacorb.util.Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE
                    (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }			    		  
        }
    }
}






