/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

public abstract class GIOPConnection
    extends java.io.OutputStream
    implements TransportListener
{
    protected Transport transport = null;

    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;
    protected ConnectionListener connection_listener = null;

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

    //// support for SAS Stateful contexts
    //private Hashtable sasContexts = null;

    // provide cubbyholes for other layers to store connection persistent data
    private static int cubby_count = 0;
    private Object[] cubbyholes = null;

    // the no. of outstanding messages (requests/replies)
    private int pending_messages = 0;

    private boolean discard_messages = false;

    //used to lock the section where we got a message, but it isn't
    //yet decided, if this might need a reply, i.e. set the transport
    //busy
    protected Object pendingUndecidedSync = new Object();

    //stop listening for messages
    private boolean do_close = false;

    public GIOPConnection( Transport transport,
                           RequestListener request_listener,
                           ReplyListener reply_listener )
    {
        this.transport = transport;
        this.request_listener = request_listener;
        this.reply_listener = reply_listener;

        transport.setTransportListener( this );

        fragments = new Hashtable();
        buf_mg = BufferManager.getInstance();
        //sasContexts = new Hashtable();

        cubbyholes = new Object[cubby_count];
    }

    public final void setCodeSets( int TCS, int TCSW )
    {
        this.TCS = TCS;
        this.TCSW = TCSW;

        tcs_negotiated = true;
    }

    public final int getTCS()
    {
        return TCS;
    }

    public final int getTCSW()
    {
        return TCSW;
    }

    public final void markTCSNegotiated()
    {
        tcs_negotiated = true;
    }

    public final boolean isTCSNegotiated()
    {
        return tcs_negotiated;
    }

    /**
     * Get the value of request_listener.
     * @return value of request_listener.
     */
    private final synchronized RequestListener getRequestListener()
    {
        return request_listener;
    }

    /**
     * Set the value of request_listener.
     * @param v  Value to assign to request_listener.
     */
    public final synchronized void setRequestListener( RequestListener  v )
    {
        this.request_listener = v;
    }

    /**
     * Get the value of reply_listener.
     * @return value of reply_listener.
     */
    private final synchronized ReplyListener getReplyListener()
    {
        return reply_listener;
    }

    /**
     * Set the value of reply_listener.
     * @param v  Value to assign to reply_listener.
     */
    public final synchronized void setReplyListener( ReplyListener  v )
    {
        this.reply_listener = v;
    }

    public final void setConnectionListener( ConnectionListener connection_listener )
    {
        this.connection_listener = connection_listener;
    }

    public final Transport getTransport()
    {
        return transport;
    }

    public final void receiveMessages()
        throws IOException
    {
        while( true )
        {
            byte[] message = transport.getMessage();


            if( message == null )
            {
                if( do_close )
                {
                    return;
                }
                else
                {
                    continue;
                }
            }
            
            synchronized( pendingUndecidedSync )
            {
                if( discard_messages )
                {
                    buf_mg.returnBuffer( message );
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
                    //GIOP 1.0 messages aren't allowed to be fragmented
                    if( Messages.getGIOPMinor( message ) == 0 )
                    {
                        Debug.output( 1, "WARNING: Received a GIOP 1.0 message of type Fragment" );

                        MessageOutputStream out =
                            new MessageOutputStream();
                        out.writeGIOPMsgHeader( MsgType_1_1._MessageError,
                                                0 );
                        out.insertMsgSize();
                        sendMessage( out );
                        buf_mg.returnBuffer( message );

                        continue;
                    }

                    //GIOP 1.1 Fragmented messages currently not supported
                    if( Messages.getGIOPMinor( message ) == 1 )
                    {
                        Debug.output( 1, "WARNING: Received a GIOP 1.1 Fragment message" );

                        //Can't return a message in this case, because
                        //GIOP 1.1 fragments don't have request
                        //ids. Therefore, just discard.
                        buf_mg.returnBuffer( message );

                        continue;
                    }

                    //for now, only GIOP 1.2 from here on

                    Integer request_id =
                        new Integer( Messages.getRequestId( message ));

                    //sanity check
                    if( ! fragments.containsKey( request_id ))
                    {
                        Debug.output( 1, "ERROR: No previous Fragment to this one" );

                        //Drop this one and continue
                        buf_mg.returnBuffer( message );

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

                    if( Messages.moreFragmentsFollow( message ))
                    {
                        //more to follow, so don't hand over to processing
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
                            Debug.output( 1, "WARNING: Received a GIOP 1.1 message of type " + msg_type + " with the \"more fragments follow\" bits set" );

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
                        new Integer( Messages.getRequestId( message ));

                    //sanity check
                    if( fragments.containsKey( request_id ))
                    {
                        Debug.output( 1, "ERROR, Received a message of type " +
                                      msg_type + " with the more fragments follow bit set, but there is already an fragmented, incomplete message with the same request id " +
                                      request_id + "!" );

                        //Drop this one and continue
                        buf_mg.returnBuffer( message );

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
            }//synchronized( pendingUndecidedSync )
        }
    }

    protected final void getWriteLock()
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

    protected final void releaseWriteLock()
    {
        synchronized( write_sync )
        {
            writer_active = false;

            write_sync.notifyAll();
        }
    }

    public final void incPendingMessages()
    {
        ++pending_messages;
    }

    public final void decPendingMessages()
    {
        --pending_messages;
    }

    public final boolean hasPendingMessages()
    {
        return pending_messages != 0;
    }

    /**
     * write (a fragment of) the message (passes it on to the wire)
     */

    public final void write( byte[] fragment, int start, int size )
        throws IOException
    {
        transport.write( fragment, start, size );
    }

    /* pro forma implementations of io.OutputStream methods */

    public final void write(int i)
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

    public final void sendRequest( MessageOutputStream out,
                                   boolean expect_reply )
        throws IOException
    {
        if( expect_reply )
        {
            incPendingMessages();
        }

        sendMessage( out );
    }

    public final void sendReply( MessageOutputStream out )
        throws IOException
    {
        decPendingMessages();

        sendMessage( out );
    }

    private final void sendMessage( MessageOutputStream out )
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

    public final boolean isSSL()
    {
        return transport.isSSL();
    }

    public void close()
    {
        closeCompletely();
    }

    public void closeCompletely()
    {
        if( connection_listener != null )
        {
            connection_listener.connectionClosed();
        }

        try
        {
            transport.closeCompletely();
        }
        catch( IOException e )
        {
            //Debug.output( 1, e );
        }

        do_close = true;

        Debug.output( 2, "GIOPConnection closed completely" );
    }

    /**
     * Get the statistics provider for transport usage statistics.
     */
    public final StatisticsProvider getStatisticsProvider()
    {
        return transport.getStatisticsProvider();
    }

    /**
     * Atomically try to set this connection into discarding mode, if
     * it doesn't have any pending messages.
     *
     * @return true, if the connection has been idle and discarding
     * has been set 
     */
    public boolean tryDiscard()
    {
        synchronized( pendingUndecidedSync )
        {
            if( pending_messages == 0 )
            {
                discard_messages = true;
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public void streamClosed()
    {
        if( connection_listener != null )
        {
            connection_listener.streamClosed();
        }
    }


    /*
      class CachedContext
      {
      public byte[] client_authentication_token;
      public EstablishContext msg;
      CachedContext(byte[] client_authentication_token, EstablishContext msg)
      {
      this.client_authentication_token = client_authentication_token;
      this.msg = msg;
      }
      }

      public void cacheSASContext(long client_context_id, byte[] client_authentication_token, EstablishContext msg)
      {
      synchronized ( sasContexts )
      {
      sasContexts.put(new Long(client_context_id), new CachedContext(client_authentication_token, msg));
      }
      }

      public void purgeSASContext(long client_context_id)
      {
      synchronized ( sasContexts )
      {
      sasContexts.remove(new Long(client_context_id));
      }
      }

      public byte[] getSASContext(long client_context_id)
      {
      Long key = new Long(client_context_id);
      synchronized (sasContexts)
      {
      if (!sasContexts.containsKey(key)) return null;
      return ((CachedContext)sasContexts.get(key)).client_authentication_token;
      }
      }

      public EstablishContext getSASContextMsg(long client_context_id)
      {
      Long key = new Long(client_context_id);
      synchronized (sasContexts)
      {
      if (!sasContexts.containsKey(key)) return null;
      return ((CachedContext)sasContexts.get(key)).msg;
      }
      }
    */

    // provide cubbyholes for data

    public static int allocate_cubby_id()
    {
        return cubby_count++;
    }

    public Object get_cubby(int id)
    {
        if (id < 0 || id >= cubby_count) {
            Debug.output(1, "Get bad cubby id "+id+" (max="+cubby_count+")");
            return null;
        }
        return cubbyholes[id];
    }

    public void set_cubby(int id, Object obj)
    {
        if (id < 0 || id >= cubby_count) {
            Debug.output(1, "Set bad cubby id "+id+" (max="+cubby_count+")");
            return;
        }
        cubbyholes[id] = obj;
    }

}// GIOPConnection













