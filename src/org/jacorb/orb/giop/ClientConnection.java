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

import java.util.*;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.util.Debug;

import org.omg.IOP.*;
import org.omg.CONV_FRAME.*;
/**
 * ClientConnection.java
 *
 *
 * Created: Sat Aug 18 18:37:56 2001
 *
 * @author Nicolas Noffke
 * @version $Id$ 
 */

public class ClientConnection 
    implements ReplyListener, ConnectionListener
{
    private GIOPConnection connection = null;
    private org.omg.CORBA.ORB orb = null;

    private Hashtable replies = null;

    /* how many clients use this connection? */
    private int client_count = 0;

    //to generate request ids
    private int id_count = 0;

    private ConnectionManager conn_mg = null;

    private boolean client_initiated = true;

    private String info = null;

    public ClientConnection( GIOPConnection connection,
                             org.omg.CORBA.ORB orb,
                             ConnectionManager conn_mg,
                             String info,
                             boolean client_initiated )
    {
        this.connection = connection;
        this.orb = orb;
        this.conn_mg = conn_mg;
        this.info = info;
        this.client_initiated = client_initiated;

        //For BiDirGIOP, the connection initiator may only generate
        //even valued request ids, and the other side odd valued
        //request ids. Therefore, we always step the counter by 2, so
        //we always get only odd or even ids depending on the counters
        //initial value.
        if( ! client_initiated )
        {
            id_count = 1;
        }
        
        connection.setReplyListener( this );
        connection.setConnectionListener( this );

        replies = new Hashtable();
    }

    public ServiceContext setCodeSet( ParsedIOR pior )
    {
        if( isTCSNegotiated() )
        {
            //if negotiated, do nothing
            return null;
        }

        CodeSetComponentInfo info = pior.getCodeSetComponentInfo();

        if( info == null )
        {
            Debug.output( 2, "No CodeSetComponentInfo present in IOR. Will use default CodeSets" );
            
            //If we can't find matching codesets, we still mark the
            //GIOPConnection as negotiated, so the following requests
            //will not always try to select a codeset again.
            connection.markTCSNegotiated();

            return null;
        }
        
        int tcs = CodeSet.selectTCS( info );
        int tcsw = CodeSet.selectTCSW( info );
        
        if( tcs == -1 || tcsw == -1 )
        {
            Debug.output( 2, "WARNING: CodeSet negotiation failed! No matching " +
                          (( tcs == -1 )? "normal" : "wide") +
                          " CodeSet found" );

            //If we can't find matching codesets, we still mark the
            //GIOPConnection as negotiated, so the following requests
            //will not always try to select a codeset again.
            connection.markTCSNegotiated();

            return null;
        }

        //this also marks tcs as negotiated.
        connection.setCodeSets( tcs, tcsw );

        Debug.output( 3, "Successfully negotiated Codesets. Using " +
                      CodeSet.csName( tcs ) + " as TCS and " +
                      CodeSet.csName( tcsw ) + " as TCSW" );

        // encapsulate context
	CDROutputStream os = new CDROutputStream( orb );
	os.beginEncapsulatedArray();
	CodeSetContextHelper.write( os, new CodeSetContext( tcs, tcsw ));

        return new ServiceContext( TAG_CODE_SETS.value,
                                   os.getBufferCopy() );
    }

    public boolean isTCSNegotiated()
    {
        return connection.isTCSNegotiated();
    }

    public int getTCS()
    {
        return connection.getTCS();
    }

    public int getTCSW()
    {
        return connection.getTCS();
    }
    
    public String getInfo()
    {
        return info;
    }

    public synchronized int getId()
    {
        int id = id_count;
        
        //if odd or even is determined by the starting value of
        //id_count
        id_count += 2;

        return id;
    }

    public void incClients()
    {
        client_count++;
    }
    
    public void decClients()
    {
        client_count--;
    }

    public boolean hasNoMoreClients()
    {
        return client_count == 0;
    }

    public boolean isClientInitiated()
    {
        return client_initiated;
    }

    /**
     * The request_id parameter is only used, if response_expected.
     */
    public ReplyPlaceholder sendRequest( MessageOutputStream os,
                                         boolean response_expected,
                                         int request_id )
    {        
        ReplyPlaceholder placeholder = null;

        if( response_expected )
        {
            Integer key = new Integer( request_id );
            
            placeholder = new ReplyPlaceholder();

            synchronized( replies )
            {
                replies.put( key, placeholder );
            }
        }

        try
        {
            connection.sendMessage( os );
        }
        catch( Exception e )
        {
            Debug.output(2,e);
            throw new org.omg.CORBA.COMM_FAILURE();
        }		
        
        return placeholder;
    }

    public void close()
    {
        connection.close();
    }

    /*
     * Operations from ReplyListener
     */

    public void replyReceived( byte[] reply,
                               GIOPConnection connection )
    {
        Integer key = new Integer( Messages.getRequestId( reply ));
        
        ReplyPlaceholder placeholder = null;
        
        synchronized( replies )
        {
            placeholder =
                (ReplyPlaceholder) replies.remove( key );
        }

        if( placeholder != null )
        {
            //this will unblock the waiting thread
            placeholder.replyReceived( new ReplyInputStream( orb, reply ));
        }
        else
        {
            Debug.output( 1, "WARNING: Received an unknown reply" );
        }
    }

        
    public void locateReplyReceived( byte[] reply,
                                     GIOPConnection connection )
    {
        Integer key = new Integer( Messages.getRequestId( reply ));

        ReplyPlaceholder placeholder = null;
        
        synchronized( replies )
        {
            placeholder =
                (ReplyPlaceholder) replies.remove( key );
        }

        if( placeholder != null )
        {
            //this will unblock the waiting thread
            placeholder.replyReceived( new LocateReplyInputStream( orb, 
                                                                   reply ));
        }
        else
        {
            Debug.output( 1, "WARNING: Received an unknown reply" );
        }        
    }

    public void closeConnectionReceived( byte[] close_conn,
                                         GIOPConnection connection )
    {
        
    }

    
    public void fragmentReceived( byte[] fragment,
                                  GIOPConnection connection )
    {
        
    }

    /*
     * Operations from ConnectionListener
     */
    public void connectionClosed()
    {
        streamClosed();

        if( ! client_initiated )
        {
            //if this is a server side BiDir connection, it will stay
            //pooled in the ConnectionManager even if no Delegate is
            //associated with it. Therefore, it has to be removed when
            //the underlying connection closed.
            
            conn_mg.removeConnection( this );
        }
    }
    
    public void connectionTimedOut()
    {
        synchronized( replies )
        {
            if( replies.size() > 0 )
            {
                Debug.output( 1, "ERROR: Read timed out. Lost " +
                              replies.size() + " outstanding replie(s)!");
            }

            for( Enumeration keys = replies.keys();
                 keys.hasMoreElements(); )
            {
                ReplyPlaceholder placeholder =
                    (ReplyPlaceholder) replies.remove( keys.nextElement() );
               
                placeholder.timeout();
            }
        }        
    }
    
    public void streamClosed()
    {
        synchronized( replies )
        {
            if( replies.size() > 0 )
            {
                Debug.output( 1, "ERROR: Abnormal connection termination. Lost " +
                              replies.size() + " outstanding replie(s)!");
            }

            for( Enumeration keys = replies.keys();
                 keys.hasMoreElements(); )
            {
                ReplyPlaceholder placeholder =
                    (ReplyPlaceholder) replies.remove( keys.nextElement() );
               
                placeholder.cancel();
            }
        }                
    }
}// ClientConnection



