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

import java.util.Hashtable;

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

    private String info = null;

    public ClientConnection( GIOPConnection connection,
                             org.omg.CORBA.ORB orb,
                             String info )
    {
        this.connection = connection;
        this.orb = orb;
        this.info = info;

        connection.setReplyListener( this );

        replies = new Hashtable();
    }

    public String getInfo()
    {
        return info;
    }

    public synchronized int getId()
    {
        return id_count++; /* */
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
            
            replies.put( key, placeholder );
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

    public void closeConnection()
    {
        connection.closeConnection();
    }

    /*
     * Operations from ReplyListener
     */

    public void replyReceived( byte[] reply,
                               GIOPConnection connection )
    {
        Integer key = new Integer( Messages.getRequestId( reply ));
        
        ReplyPlaceholder placeholder = 
            (ReplyPlaceholder) replies.remove( key );

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
        
        ReplyPlaceholder placeholder = 
            (ReplyPlaceholder) replies.remove( key );

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

}// ClientConnection



