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

import org.jacorb.util.Debug;

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
    implements ReplyListener
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

        replies = new Hashtable();
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

    public void connectionClosed()
    {
        synchronized( replies )
        {
            if( replies.size() > 0 )
            {
                Debug.output( 1, "ERROR: Abnormal connection termination. Lost " +
                              replies.size() + " outstanding replies!");
            }

            for( Enumeration keys = replies.keys();
                 keys.hasMoreElements(); )
            {
                ReplyPlaceholder placeholder =
                    (ReplyPlaceholder) replies.remove( keys.nextElement() );
                
                placeholder.cancel();
            }
        }        

        if( ! client_initiated )
        {
            //if this is a server side BiDir connection, it will stay
            //pooled in the COnnectionManager even if no Delegate is
            //associated with it. Therefore, it has to be removed when
            //the underlying connection closed.
            
            conn_mg.removeConnection( this );
        }
    }
}// ClientConnection



