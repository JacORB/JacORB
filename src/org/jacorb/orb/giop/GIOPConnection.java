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

import org.omg.GIOP.*;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.CompletionStatus;

import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.util.*;


/**
 * GIOPConnection.java
 *
 *
 * Created: Sun Aug 12 21:30:48 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class GIOPConnection 
{
    private Transport transport = null;
    
    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;

    private boolean writer_active = false;
    private Object write_sync = new Object();

    /*
     * Connection OSF character formats.
     */
    private int TCS = CodeSet.getTCSDefault();
    private int TCSW = CodeSet.getTCSWDefault();	

    public GIOPConnection( Transport transport,
                           RequestListener request_listener,
                           ReplyListener reply_listener )
    {
        this.transport = transport;
        this.request_listener = request_listener;
        this.reply_listener = reply_listener;
    }
    
    public void setCodeSets( int TCS, int TCSW )
    {
        this.TCS = TCS;
        this.TCSW = TCSW;
    }

    public int getTCS()
    {
        return TCS;
    }

    public int getTCSW()
    {
        return TCSW;
    }
        
    /**
     * Get the value of request_listener.
     * @return value of request_listener.
     */
    private synchronized RequestListener getRequestListener() 
    {
        return request_listener;
    }
    
    /**
     * Set the value of request_listener.
     * @param v  Value to assign to request_listener.
     */
    public synchronized void setRequestListener( RequestListener  v ) 
    {
        this.request_listener = v;
    }
    
    /**
     * Get the value of reply_listener.
     * @return value of reply_listener.
     */
    private synchronized ReplyListener getReplyListener() 
    {
        return reply_listener;
    }
    
    /**
     * Set the value of reply_listener.
     * @param v  Value to assign to reply_listener.
     */
    public synchronized void setReplyListener( ReplyListener  v ) 
    {
        this.reply_listener = v;
    }
    
    public void receiveMessages()
        throws IOException
    {
        while( true )
        {
            byte[] message = null;
            
            try
            {
                message = transport.getMessage();
            }
            catch( IOException e )
            {
                request_listener.connectionClosed();
                reply_listener.connectionClosed();

                throw e;
            }
            
            if( message == null )
            {
                //do sth. else?
                continue;
            }
            
            Debug.output( 10, "Receive Request", message );

            //check major version
            if( Messages.getGIOPMajor( message ) != 1 )
            {
                Debug.output( 1, "ERROR: Invalid GIOP major version encountered: " +
                              Messages.getGIOPMajor( message ) );
                
                Debug.output( 3, "GIOPConnection.receiveMessages()", message );
                
                continue;
            }

            int msg_type = Messages.getMsgType( message );

            //don't even look at fragment or fragmented messages
            if( msg_type == MsgType_1_1._Fragment ||
                Messages.isFragmented( message ))
            {
                Debug.output( 1, "WARNING: Received a Fragment message" );
                
                int giop_minor = Messages.getGIOPMinor( message );
                
                ReplyOutputStream out = 
                    new ReplyOutputStream( Messages.getRequestId( message ),
                                           ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                           giop_minor );
                
                SystemExceptionHelper.write( out, 
                      new NO_IMPLEMENT( 0, CompletionStatus.COMPLETED_NO ));
        
                sendMessage( out );

                continue;
            }
            
            switch( msg_type )
            {
                case MsgType_1_1._Request:
                {                                
                    getRequestListener().requestReceived( message, this );

                    break;
                }                 
                case MsgType_1_1._Reply:
                {
                    getReplyListener().replyReceived( message, this );
                    
                    break;                    
                }
                case MsgType_1_1._CancelRequest:
                {
                    getRequestListener().cancelRequestReceived( message, this );
                    
                    break;
                }
                case MsgType_1_1._LocateRequest:
                {
                    getRequestListener().locateRequestReceived( message, this );

                    break;
                }
                case MsgType_1_1._LocateReply:
                {
                    getReplyListener().locateReplyReceived( message, this );
   
                    break;
                }
                case MsgType_1_1._CloseConnection:
                {
                    getReplyListener().closeConnectionReceived( message, this );

                    break;
                }
                case MsgType_1_1._MessageError:
                {
                    break;
                }
                case MsgType_1_1._Fragment:
                {
                    //currently not reached
                    break;
                }
                default:
                {
                    Debug.output(0, "ERROR: received message with unknown message type " + msg_type);
                    Debug.output( 3, "GIOPConnection.receiveMessages()", message );
                }
            }            
        }
    }        
                                        
    private void getWriteLock()
    {
        synchronized( write_sync )
        {
            while( writer_active )
            {
                try
                {
                    wait();
                }
                catch( InterruptedException e )
                {
                }
            }
        
            writer_active = true;
        }
    }

    private synchronized void releaseWriteLock()
    {
        synchronized( write_sync )
        {            
            writer_active = false;

            write_sync.notifyAll();
        }
    }

    public void addMessageFragment( byte[] message, int start, int size )
        throws IOException
    {
        transport.addOutgoingMessage( message, start, size );
    }
    
    public void sendMessage( MessageOutputStream out )
        throws IOException
    {
        try
        {
            getWriteLock();
            
            out.write_to( this );
            
            transport.sendMessages();
        }
        finally
        {
            releaseWriteLock();
        }
    }

    public boolean isSSL()
    {
        return transport.isSSL();
    }
    
    public void close()
    {
        try
        {
            transport.close();
        }
        catch( IOException e )
        {
            Debug.output( 1, e );
        }      
    }
}// GIOPConnection













