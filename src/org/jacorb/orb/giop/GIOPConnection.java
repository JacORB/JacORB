/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
import java.util.*;

import org.omg.GIOP.*;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.CompletionStatus;

import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.orb.BufferManager;
import org.jacorb.util.*;


/**
 * GIOPConnection.java
 *
 *
 * Created: Sun Aug 12 21:30:48 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public final class GIOPConnection 
    extends java.io.OutputStream
{
    private Transport transport = null;
    
    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;
    private ConnectionListener connection_listener = null;

    private boolean writer_active = false;
    private Object write_sync = new Object();

    /*
     * Connection OSF character formats.
     */
    private int TCS = CodeSet.getTCSDefault();
    private int TCSW = CodeSet.getTCSWDefault();	

    private boolean tcs_negotiated = false;

    //map request id (Integer) to ByteArrayInputStream
    private Hashtable fragments = null;
    private BufferManager buf_mg = null;

    public GIOPConnection( Transport transport,
                           RequestListener request_listener,
                           ReplyListener reply_listener )
    {
        this.transport = transport;
        this.request_listener = request_listener;
        this.reply_listener = reply_listener;

        fragments = new Hashtable();
        buf_mg = BufferManager.getInstance();
    }
    
    public final void setCodeSets( int TCS, int TCSW )
    {
        this.TCS = TCS;
        this.TCSW = TCSW;

        tcs_negotiated = true;
    }

    public  final int getTCS()
    {
        return TCS;
    }

    public  final int getTCSW()
    {
        return TCSW;
    }

    public  final void markTCSNegotiated()
    {
        tcs_negotiated = true;
    }
    
    public  final boolean isTCSNegotiated()
    {
        return tcs_negotiated;
    }
        
    /**
     * Get the value of request_listener.
     * @return value of request_listener.
     */
    private  final synchronized RequestListener getRequestListener() 
    {
        return request_listener;
    }
    
    /**
     * Set the value of request_listener.
     * @param v  Value to assign to request_listener.
     */
    public  final synchronized void setRequestListener( RequestListener  v ) 
    {
        this.request_listener = v;
    }
    
    /**
     * Get the value of reply_listener.
     * @return value of reply_listener.
     */
    private  final synchronized ReplyListener getReplyListener() 
    {
        return reply_listener;
    }
    
    /**
     * Set the value of reply_listener.
     * @param v  Value to assign to reply_listener.
     */
    public  final synchronized void setReplyListener( ReplyListener  v ) 
    {
        this.reply_listener = v;
    }

    public  final void setConnectionListener( ConnectionListener connection_listener )
    {
        this.connection_listener = connection_listener;
    }

    public  final Transport getTransport()
    {
        return transport;
    }
    
    public  final void receiveMessages()
        throws IOException
    {
        while( true )
        {
            byte[] message = null;
            
            try
            {
                message = transport.getMessage();
            }
            catch( CloseConnectionException cce )
            {
                if( connection_listener != null )
                {
                    connection_listener.connectionClosed();
                }

                throw cce;
            }
            catch( TimeOutException toe )
            {
                if( connection_listener != null )
                {
                    connection_listener.connectionTimedOut();
                }
            }
            catch( StreamClosedException sce )
            {
                if( connection_listener != null )
                {
                    connection_listener.streamClosed();
                }
            }

            if( message == null )
            {
                //do sth. else?
                continue;
            }
            
            //check major version
            if( Messages.getGIOPMajor( message ) != 1 )
            {
                Debug.output( 1, "ERROR: Invalid GIOP major version encountered: " +
                              Messages.getGIOPMajor( message ) );
                
                Debug.output( 3, "GIOPConnection.receiveMessages()", message );
                
                continue;
            }

            int msg_type = Messages.getMsgType( message );

            if( msg_type == MsgType_1_1._Fragment )
            {
                Integer request_id = 
                    new Integer( Messages.getGIOPMinor( message ));
                
                //sanity check
                if( ! fragments.containsKey( request_id ))
                {
                    Debug.output( 1, "ERROR: No previous Fragment to this one" );
                    //FIXME: handle!
                    
                    continue;
                }
                
                ByteArrayOutputStream b_out = (ByteArrayOutputStream)
                    fragments.get( request_id );
                
                //add the message contents to stream (discarding the
                //GIOP message header and the request id ulong of the
                //Fragment header)
                b_out.write( message, 
                             Messages.MSG_HEADER_SIZE + 4 , 
                             Messages.getMsgSize(message) - 4 );

                //more to follow, so don't hand over to processing
                if( Messages.moreFragmentsFollow( message ))
                {
                    buf_mg.returnBuffer( message );
                    continue;
                }
                else
                {
                    buf_mg.returnBuffer( message );

                    //silently replace the original message buffer and type
                    message = b_out.toByteArray();
                    msg_type = Messages.getMsgType( message );
                    
                    fragments.remove( request_id );
                }
            }
            else if( Messages.moreFragmentsFollow( message ) )
            {
                //GIOP 1.0 messages aren't allowed to be fragmented
                if( Messages.getGIOPMinor( message ) == 0 )
                {
                    Debug.output( 1, "WARNING: Received a GIOP 1.0 message with the \"more fragments follow\" bits set" );
                    
                    MessageOutputStream out =
                        new MessageOutputStream();
                    out.writeGIOPMsgHeader( MsgType_1_1._MessageError,
                                            0 );
                    out.insertMsgSize();
                    sendMessage( out );
                    buf_mg.returnBuffer( message );

                    continue;
                }

                //If GIOP 1.1, only Request and Reply messages may be fragmented
                if( Messages.getGIOPMinor( message ) == 1 ) 
                {
                    if( msg_type != MsgType_1_1._Request &&
                        msg_type != MsgType_1_1._Reply )
                    {
                        Debug.output( 1, "WARNING: Received a GIOP 1.1 message with the \"more fragments follow\" bits set" );
                        
                        MessageOutputStream out =
                            new MessageOutputStream();
                        out.writeGIOPMsgHeader( MsgType_1_1._MessageError,
                                                1 );
                        out.insertMsgSize();
                        sendMessage( out );
                        buf_mg.returnBuffer( message );
                
                        continue;
                    }
                    else //GIOP 1.1 Fragmented messages currently not supported
                    {
                        Debug.output( 1, "WARNING: Received a fragmented GIOP 1.1 message" );
                        
                        int giop_minor = Messages.getGIOPMinor( message );
                        
                        ReplyOutputStream out = 
                            new ReplyOutputStream( Messages.getRequestId( message ),
                                                   ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                                   giop_minor,
                                                   false );//no locate reply
                        
                        SystemExceptionHelper.write( out, 
                                                     new NO_IMPLEMENT( 0, CompletionStatus.COMPLETED_NO ));
                        
                        sendMessage( out );
                        buf_mg.returnBuffer( message );

                        continue;
                    }
                }

                //check, that only the correct message types are fragmented
                if( msg_type == MsgType_1_1._CancelRequest ||
                    msg_type == MsgType_1_1._CloseConnection ||
                    msg_type == MsgType_1_1._CancelRequest )
                {
                    Debug.output( 1, "WARNING: Received a GIOP message of type " + msg_type +
                                  " with the \"more fragments follow\" bits set, but this " +
                                  "message type isn't allowed to be fragmented" );
                        
                    MessageOutputStream out =
                        new MessageOutputStream();
                    out.writeGIOPMsgHeader( MsgType_1_1._MessageError,
                                            1 );
                    out.insertMsgSize();
                    sendMessage( out );                    
                    buf_mg.returnBuffer( message );

                    continue;
                }
                
                //if we're here, it's the first part of a fragmented message
                Integer request_id = 
                    new Integer( Messages.getGIOPMinor( message ));
                
                //sanity check
                if( fragments.containsKey( request_id ))
                {
                    //FIXME: handle!
                    
                    continue;
                }
                
                //create new stream and add to table
                ByteArrayOutputStream b_out = new ByteArrayOutputStream();
                fragments.put( request_id, b_out );
                
                //add the message contents to stream
                b_out.write( message, 
                             0, 
                             Messages.MSG_HEADER_SIZE + 
                             Messages.getMsgSize(message) );


                buf_mg.returnBuffer( message );
                
                //This message isn't yet complete
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
                                        
    private  final void getWriteLock()
    {
        synchronized( write_sync )
        {
            while( writer_active )
            {
                try
                {
                    write_sync.wait();
                }
                catch( InterruptedException e )
                {
                }
            }
        
            writer_active = true;
        }
    }

    private  final void releaseWriteLock()
    {
        synchronized( write_sync )
        {            
            writer_active = false;

            write_sync.notifyAll();
        }
    }

    /**
     * write (a fragment of) the message (passes it on to the wire)
     */

    public  final void write( byte[] fragment, int start, int size )
        throws IOException
    {
        transport.write( fragment, start, size );
    }

    /* pro forma implementations of io.OutputStream methods */

    public  final void write(int i) 
        throws java.io.IOException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public final void write(byte[] b) throws java.io.IOException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    } 


    public final void flush() throws java.io.IOException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    
    public final void sendMessage( MessageOutputStream out )
        throws IOException
    {
        try
        {
            getWriteLock();
            
            out.write_to( this );
            
            transport.flush();
        }
        finally
        {
            releaseWriteLock();
        }
    }

    public  final boolean isSSL()
    {
        return transport.isSSL();
    }
    
    public  final void close()
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













