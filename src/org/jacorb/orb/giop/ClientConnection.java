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

import org.jacorb.util.Debug;
import org.jacorb.orb.*;

import org.jacorb.util.*;
import org.jacorb.orb.factory.SocketFactory;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class ClientConnection
    extends AbstractConnection
{
    private int id_count = 0;
    protected String connection_info = null;
    protected InputStream in_stream;
    BufferedOutputStream out_stream;

    /** client-side socket timeout */
    protected int timeout = 0;

    protected ConnectionManager manager;

    /** write lock */
    public Object writeLock = new Object();

    // repID -> object
    protected Hashtable objects = new Hashtable();

    //contains RequestOutputStreams instead of byte[]
    protected Hashtable buffers = new Hashtable();

    protected Hashtable replies = new Hashtable();
    protected boolean littleEndian;

    /* how many clients use this connection? */
    protected int client_count = 0;

    protected Socket mysock = null;
    protected ReplyReceptor repReceptor;
    private byte [] header = new byte[ Messages.MSG_HEADER_SIZE ];
    protected SocketFactory socket_factory = null;
    
    /** 
     * dummy constructor
     */

    public ClientConnection()
    {}

    /**
     * class Constructor, establishes a connection over a given socket.
     * It is called from ConnectionManager.getConnection for clients and
     * from BasicAdapter for servers
     *
     * @param <code>Socket s</code>
     * @param <code>boolean _is_server</code> - if opened from the server side.<br>
     * @except <code>java.io.IOException</code>
     */
	       
    public ClientConnection ( ConnectionManager mgr, 
                              java.net.Socket s, 
                              SocketFactory socket_factory )
        throws IOException
    {
        this( mgr, 
              s, 
              new BufferedInputStream( s.getInputStream()),
              socket_factory );
    }


    public ClientConnection ( ConnectionManager mgr,
                              java.net.Socket s, 
                              InputStream in,
                              SocketFactory socket_factory )
        throws IOException
    {
        this.socket_factory = socket_factory;
        manager = mgr;
        this.orb = mgr.getORB();
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
        client_count = 1;
        
        Debug.output(1, "New " + ssl + "connection to " + host_and_port);
        repReceptor = new ReplyReceptor( this );

        /* get the client-side timeout property value */

        String prop = 
            Environment.getProperty("jacorb.connection.client_timeout");
        
        if( prop != null )
        {
            timeout = Integer.parseInt(prop);
        }
    }

    ORB getORB()
    {
        return orb;
    }
	
    public boolean isSSL()
    {
        return socket_factory.isSSL( mysock );
    }
	
    /** don't use, this is a temporary hack! */
	
    public Socket getSocket()
    {
        return mysock;
    }


    /**
     * This  code selects the appropriate codeset  for connection from
     * information  contained in  some IOR. Code  is called  by client
     * side connection.  Returns true  if common codeset was found and
     * so that it should be sent in context.  
     */

    protected boolean selectCodeSet( ParsedIOR pior )
    {
	for( int i = 0; i < pior.taggedComponents.length; i++ )
	{
	    if( pior.taggedComponents[i].tag != org.omg.IOP.TAG_CODE_SETS.value ) 
		continue;

	    Debug.output(4, "TAG_CODE_SETS found");			

	    // get server cs from IOR 
	    CDRInputStream is =
		new CDRInputStream( orb, pior.taggedComponents[i].component_data);

	    is.openEncapsulatedArray();

	    org.omg.CONV_FRAME.CodeSetComponentInfo inf = 
		org.omg.CONV_FRAME.CodeSetComponentInfoHelper.read( is );
		
	    // char data
            int tmpTCS = 
                selectCodeSet( inf.ForCharData, CodeSet.getTCSDefault() );

	    if( tmpTCS == 0 ) 
		tmpTCS = selectCodeSet( inf.ForCharData, 
                                     CodeSet.getConversionDefault() );
            if( tmpTCS == 0 ) 
                return false;
            else
                TCS = tmpTCS;
			
	    // wchar data
	    int tmpTCSW = 
                selectCodeSet( inf.ForWcharData, CodeSet.getTCSWDefault() );

	    if( tmpTCSW == 0) 
		tmpTCSW = selectCodeSet( inf.ForWcharData, 
                                         CodeSet.getConversionDefault() );
	    if( tmpTCSW == 0) 
		return false;
            else
                TCSW = tmpTCSW;

	    Debug.output(4,"selected TCS: " + CodeSet.csName(TCS) +
                         ", TCSW: " + CodeSet.csName(TCSW));
	    return true;
	}

	/* if we  are here then no tagged component was found
        */

	TCS = CodeSet.getTCSDefault();
	TCSW = CodeSet.getTCSWDefault();
		
	// mark as negotiated, why if it's not true ? because we don't
	// want to try negotiate on each IOR until IOR with codeset is found.
	// TODO(devik): Or should we ???

	markTcsNegotiated();
		
	Debug.output(4,"default TCS selected: "+CodeSet.csName(TCS)+"," +
                                 CodeSet.csName(TCSW));
	return false;
    }	

    protected int selectCodeSet( org.omg.CONV_FRAME.CodeSetComponent scs,
				 int ourNative )
    {
	// check if we support server's native sets
	if( scs.native_code_set == ourNative ) 
	    return ourNative;
		
	// is our native CS supported at server ?
	for( int i = 0; i < scs.conversion_code_sets.length; i++)
	{
	    if( scs.conversion_code_sets[i] == ourNative ) 
		return ourNative;
	}
		
	// can't find supported set ..
	return 0;
    }


    /**
     * Adds code set service context to another contexts if needed.
     */

    public org.omg.IOP.ServiceContext [] addCodeSetContext( org.omg.IOP.ServiceContext [] ctx,
							    ParsedIOR pior)
    {		
	// if already negotiated, don't send any further cs contexts
	// we should test it also directly before calling this method
	// as performance optimization

	if( isTCSNegotiated() || !Environment.charsetSendCtx()) 
	    return ctx;
		
	// not negotiated but also TCS is not selected, so select one
	// if it can't be selected (ior doesn't contain codesets) don't change ctx
	if( !isTCSNegotiated() ) 
            //	if( TCS == 0 ) 
	{
	    if(!selectCodeSet(pior)) 
		return ctx;
	}

	// encapsulate context

	CDROutputStream os = new CDROutputStream( orb );
	os.write_boolean(false);
	org.omg.CONV_FRAME.CodeSetContextHelper.write(os,
						      new org.omg.CONV_FRAME.CodeSetContext(TCS,TCSW));
		
	org.omg.IOP.ServiceContext [] ncx =
            new org.omg.IOP.ServiceContext[ctx.length+1];

	System.arraycopy(ctx,0,ncx,0,ctx.length);
	ncx[ctx.length] = 
            new org.omg.IOP.ServiceContext(org.omg.IOP.TAG_CODE_SETS.value,
                                           os.getBufferCopy());

	// Debug.output(4,"TCS ctx added: "+CodeSet.csName(TCS)+","+CodeSet.csName(TCSW));
	return ncx;
    }	


    /**
     *	Close  a  connection. Should  only  be  done  directly if  the
     *	connection   is broken or idCount has reached zero. Otherwise,
     *	use releaseConnection.  
     */

    public synchronized void closeConnection()
    {
	Debug.output(1,"Closing connection to " + connection_info);
	try
	{
	    if( repReceptor != null )
		repReceptor.stopReceptor();

	    Debug.output(4,"Closing connection to " + 
                         connection_info + " (reply receptor closed)");
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
	repReceptor = null;

	mysock = null;

	Debug.output(3,"Closing connection to " + 
                     connection_info + " (sockets closed)");

	manager.removeConnection( this );

	// connection_info = null;

	if( replies.size() > 0 )
	{
	    try 
	    {
		reconnect();
	    } 
	    catch ( org.omg.CORBA.COMM_FAILURE exe )
	    {
		Debug.output(1,"Could not reconnect to " + 
                             connection_info + 
                             " (loss of outstanding replies)");
	    }
	}
    }

    public boolean connected()
    {
        return mysock != null;
    }

    /** 
     * @returns a new request id 
     */

    public synchronized int getId()
    {
        return id_count++; /* */
    }

    protected synchronized void incUsers()
    {
        client_count++;
    }

    /** called by delegate when the delegate is duplicated */

    public void duplicate()
    {
        incUsers();
    }

    /** 
     * called to notify all waiting threads that the connection is
     * broken
     */

    private void abort( boolean timeout )
	throws EOFException, TimeOutException
    {
	Debug.output( 3, "Connection to " + 
                      connection_info + " aborting" + 
                      ( timeout ? " (because of a timeout)" : "..." ));

	Enumeration keys = replies.keys();
	int lost_replies = 0;

	while( keys.hasMoreElements() ) 
	{
	    Object key = keys.nextElement();

	    ReplyInputStream client = (ReplyInputStream)replies.get(key);
	    client.cancel( timeout );
	    lost_replies++;
	}

	replies.clear();
	objects.clear();

	if( lost_replies > 0 )
	    Debug.output( 2, "Lost " + lost_replies + 
                          " outstanding replies");

        if( timeout )
            throw new TimeOutException();
        else
            throw new EOFException();
    }

    /**
     * low-level input routine, not MT-safe, used by the ReplyReceptor object
     * for this connection
     */

    public byte[] readBuffer() 
	throws IOException 
    {
        boolean is_timeout = false;

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
		
		Debug.output( 3, "Connection timed out");
                is_timeout = true;
	    }
	    catch( Exception e )
	    {
		Debug.output(3,e);
	    }

	    if( input < 0 )		
	    {
		abort( is_timeout );
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
		abort( false );
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

		if( n < 0 )
		{
		    abort( false );
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
     * Receive a GIOP reply over this connection
     * and return an appropriate CDRInputStream
     */

    public void receiveReply() 
	throws IOException, CloseConnectionException
    {
	if( !connected() )
	    throw new EOFException();

	byte [] buf = readBuffer();
	int giop_version = buf[5];
	int msg_type = buf[7];

	Debug.output( 4, "received reply, GIOP version 1." + giop_version );

	switch ( msg_type )
	{
	case  org.omg.GIOP.MsgType_1_0._Reply: 
	    {
		Debug.output( 8, "receiveReply", buf );
		Integer key = new Integer(Messages.getRequestId( buf, msg_type ));
		org.omg.CORBA.Object o = (org.omg.CORBA.Object)objects.remove( key );

		/* retrieve the ReplyInputStream that is waiting for this reply */

		ReplyInputStream pending = (ReplyInputStream)replies.remove( key );
      
		if( pending != null ) 
		    pending.init( buf, o );
		else 
		    System.err.println("Serious Error! No pending request for reply " + 
                                       key );
		break;
	    }
	case org.omg.GIOP.MsgType_1_0._LocateReply:
	    { 
		Integer key = new Integer(Messages.getRequestId( buf, msg_type ));

		/* retrieve the ReplyInputStream that is waiting for this reply */

		LocateReplyInputStream pending = 
		    (LocateReplyInputStream)replies.remove( key );

		if( pending != null ) 
		{
		    pending.init( buf );
		} 
		else 
		    System.err.println("Serious Error! No pending request for reply " + 
                                       key );
		break;
	    }
	case org.omg.GIOP.MsgType_1_0._MessageError:
            {
		Debug.output(1,"Got <MessageError> from server, something went wrong!");
            }
	case org.omg.GIOP.MsgType_1_0._CloseConnection:
	    {
		Debug.output(3,"got close connection message");
		throw new CloseConnectionException("Received <CloseConnection> from Server");
	    }
	default:
	    throw new IOException("Received Message of unrecognizable type "+ msg_type);
	}
    }


    /**
     *
     */
    
    public synchronized void reconnect()
	throws org.omg.CORBA.COMM_FAILURE
    {	
	Debug.output(1,"Trying to reconnect to " + connection_info);

	int retries = Environment.noOfRetries();

	String host = 
            connection_info.substring(0,connection_info.indexOf(":"));
	int port = 
            new Integer( connection_info.substring( connection_info.indexOf(":")+1)).intValue();

	while( retries > 0 ) 
	{
	    try 
	    {
                //noffke: by now, the factory knows if to provide
                //ssl or not
                mysock = socket_factory.createSocket( host, port );

		mysock.setTcpNoDelay(true);

                if( timeout != 0 )
                {
                    /* re-set the socket timeout */
                    try
                    {
                        mysock.setSoTimeout( timeout );
                    } 
                    catch ( java.lang.NumberFormatException nfe )
                    {
                        // just ignore
                    }
                }

		in_stream = 
		    new BufferedInputStream(mysock.getInputStream());

		out_stream = 
		    new BufferedOutputStream(mysock.getOutputStream(), 
					     Environment.outBufSize());
		
		String ip = mysock.getInetAddress().getHostAddress();

		if( ip.indexOf('/') > 0)
		    ip = ip.substring( ip.indexOf('/') + 1 );

		String host_and_port = ip + ":"+ mysock.getPort();
		manager.addConnection( this );
		connection_info = host_and_port;

		Debug.output(1,"Reconnected to " + host_and_port);

		repReceptor = new ReplyReceptor( this );

		// notify waiting clients that they have to cancel
                // actually, there should not be any waiting clients at
                // this stage.
		for( Enumeration e = replies.elements(); e.hasMoreElements();)
		{
                    Debug.output(1,"WARNING: there were outstanding requests when reconnect succeeded! (Lost now)");
		    ((ReplyInputStream) e.nextElement()).cancel( false );
		}
		return;
	    } 
	    catch ( IOException c ) 
	    { 
		Debug.output(1,"Retrying to reconnect to " + 
                                         connection_info);
		try 
		{
		    Thread.sleep( Environment.retryInterval() );
		} 
		catch ( InterruptedException i ){}
		retries--;
	    }
	}
	if( retries == 0 )
	    throw new org.omg.CORBA.TRANSIENT("Retries exceeded, couldn't reconnect to " + 
						 connection_info );
    }

    /**
     *	Release a connection. If called by the last client using
     *	this connection (using Stub._release()), it is closed.
     */

    public synchronized void releaseConnection()
    {
        client_count--;
        if( client_count == 0 )
        {
            closeConnection();
        }
        else
        {
            Debug.output(2,"Releasing one connection to " + 
                         connection_info );		
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
     * writes the arguments of method calls to the output stream 
     */
    
    public org.omg.CORBA.portable.InputStream sendRequest( org.omg.CORBA.Object o,
							   RequestOutputStream os ) 
	throws org.omg.CORBA.COMM_FAILURE, org.omg.CORBA.IMP_LIMIT
    {

	if( !connected() )
	{
	    Debug.output(3, "not connected" );
	    reconnect();
	}

	synchronized( writeLock )
	{
	    ReplyInputStream rep = null;
	    
	    try
	    {
		if( os.response_expected() )
		{
		    rep = new ReplyInputStream(this.orb, os.requestId());
                    rep.setCodeSet( this.TCS, this.TCSW );
		    Integer key = new Integer( os.requestId() );
		    //		    buffers.put( key, os );
		    replies.put( key, rep );
		    objects.put( key, o );
		}

		if (org.jacorb.util.Environment.verbosityLevel() > 4)
		{
		    //This is a costly op, since it involves buffer copying!
		    Debug.output(5,"send request", os.getBufferCopy());
		}

		os.write_to(out_stream);
		
		/** release the stream so its buffer can go back into the pool */
		os.release();		

		//Debug.output(5,"sendreq",buf,size);
	    } 
	    catch ( TimeOutException t )
	    {
		Debug.output( 2, t );
		throw new org.omg.CORBA.IMP_LIMIT();
	    }		
	    catch ( Exception e )
	    {
		Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }		
	    return rep;
	}
    }

    public synchronized LocateReplyInputStream sendLocateRequest( 
                                                   LocateRequestOutputStream os ) 
	throws org.omg.CORBA.COMM_FAILURE
    {
	if( !connected() )
	{
	    reconnect();
	}

	synchronized( writeLock )
	{
	    LocateReplyInputStream rep = null;	    
	    try
	    {
		rep = new LocateReplyInputStream(this.orb, os.requestId());
		Integer key = new Integer( os.requestId() );
		//		buffers.put( key, os );
		replies.put( key, rep );
		
		os.write_to(out_stream);
		os.release();
	    } 
	    catch ( Exception e )
	    {
		Debug.output(2,e);
		throw new org.omg.CORBA.COMM_FAILURE();
	    }		
	    return rep;
	}
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

    public void setTimeOut(int timeout)
	throws SocketException
    {
	mysock.setSoTimeout(timeout);
    }

    public BufferedOutputStream get_out_stream()
    {
	return out_stream;
    }

    //      public Hashtable get_buffers(){
    //  	return buffers;
    //      }

    public Hashtable get_replies()
    {
	return replies;
    }

    public Hashtable get_objects()
    {
	return objects;
    }

    public void writeDirectly(byte[] buffer,int len) 
	throws IOException
    {
	get_out_stream().write(buffer,0, len);
	get_out_stream().flush();
    }


}

