/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.giop;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

import java.io.*;
import java.util.*;

import org.omg.GIOP.*;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.CompletionStatus;
import org.omg.ETF.*;

import org.jacorb.orb.SystemExceptionHelper;
import org.jacorb.orb.BufferManager;
import org.jacorb.orb.iiop.*;
import org.jacorb.util.*;


/**
 * GIOPConnection.java
 *
 * Created: Sun Aug 12 21:30:48 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public abstract class GIOPConnection
    extends java.io.OutputStream
{
    /**
     * Profile describing the remote endpoint of this connection.
     */
    protected org.omg.ETF.Profile    profile   = null;
    protected org.omg.ETF.Connection transport = null;

    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;
    protected ConnectionListener connection_listener = null;

    protected Object connect_sync = new Object();

    private boolean writer_active = false;
    private Object write_sync = new Object();

    private org.jacorb.config.Configuration configuration;
    protected Logger logger;

    /*
     * Connection OSF character formats.
     */
    private int TCS = CodeSet.getTCSDefault();
    private int TCSW = CodeSet.getTCSWDefault();

    private boolean tcs_negotiated = false;

    //map request id (Integer) to ByteArrayInputStream
    private final Map fragments = new HashMap();
    private BufferManager buf_mg = null;

    private boolean dump_incoming = false;
    private long timeout = 0;

    private BufferHolder msg_header
        = new BufferHolder (new byte[Messages.MSG_HEADER_SIZE]);

    private BufferHolder inbuf = new BufferHolder();


    //// support for SAS Stateful contexts
    //private Hashtable sasContexts = null;

    // provide cubbyholes for other layers to store connection persistent data
    private static int cubby_count = 0;
    private Object[] cubbyholes = null;

    // the no. of outstanding messages (requests/replies)
    // pending_messages refers only to expected replies, to be sent
    // in response to two-way requests. pending_write refers to messages
    // that are outbound but have not yet been sent. These could be one-way
    // or two-way requests, or they could be replies being sent out of a
    // server. There will typicially be only one pending write.
    private int pending_messages = 0;
    private int pending_write = 0;

    protected boolean discard_messages = false;

    //used to lock the section where we got a message, but it isn't
    //yet decided, if this might need a reply, i.e. set the transport
    //busy
    protected Object pendingUndecidedSync = new Object();

    //stop listening for messages
    protected boolean do_close = false;

    protected StatisticsProvider statistics_provider = null;

    public GIOPConnection( org.omg.ETF.Profile profile,
                           org.omg.ETF.Connection transport,
                           RequestListener request_listener,
                           ReplyListener reply_listener,
                           StatisticsProvider statistics_provider )
    {
        this.profile = profile;
        this.transport = transport;
        this.request_listener = request_listener;
        this.reply_listener = reply_listener;
        this.statistics_provider = statistics_provider;

        buf_mg = BufferManager.getInstance();
        //sasContexts = new Hashtable();

        cubbyholes = new Object[cubby_count];
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        logger = this.configuration.getNamedLogger("jacorb.giop.conn");
        dump_incoming =
            configuration.getAttribute("jacorb.debug.dump_incoming_messages","off").equals("on");
        timeout =
            configuration.getAttributeAsInteger("jacorb.connection.client.connect_timeout", 0);
    }


    public final void setCodeSets( int TCS, int TCSW )
    {
        this.TCS = TCS;
        this.TCSW = TCSW;
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
    protected final synchronized RequestListener getRequestListener()
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

    public final org.omg.ETF.Connection getTransport()
    {
        synchronized (connect_sync)
        {
            return transport;
        }
    }

    private boolean waitUntilConnected()
    {
         synchronized (connect_sync)
         {
            while (!transport.is_connected() &&
                   !do_close)
            {
                if (logger.isDebugEnabled())
                    logger.debug (this.toString() + ": will wait until connected");
                try
                {
                    connect_sync.wait();
                }
                catch( InterruptedException ie )
                {
                }
            }
            return !do_close;
         }
    }

    /**
     * Called by this.getMessage() to signal that the attempt to
     * read a message resulted in a timeout.  This method is implemented
     * differently on the client and server side.
     */
    protected abstract void readTimedOut();

    /**
     * Called by this.getMessage() to signal that the underlying transport
     * was closed while attempting to read a message.  This method is
     * implemented differently on the client and server side.
     */
    protected abstract void streamClosed();

    /**
     * Read a GIOP message from the stream. This will first try to
     * read in the fixed-length GIOP message header to determine the
     * message size, and the read the rest. It also checks the leading
     * four magic bytes of the message header. This method <b>is not
     * thread safe<b> and only expected to be called by a single
     * thread.
     *
     * @return a GIOP message or null.
     * @exception IOException passed through from the underlying IO layer.
     */
    private byte[] getMessage()
        throws IOException
    {
        //Wait until the actual socket connection is established. This
        //is necessary for the client side, so opening up a new
        //connection can be delayed until the first message is to be
        //sent.
        if( ! waitUntilConnected() )
        {
            return null;
        }

        try
        {
            transport.read (msg_header, 0,
                            Messages.MSG_HEADER_SIZE,
                            Messages.MSG_HEADER_SIZE,
                            0);
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            return null;
        }
        catch (org.omg.CORBA.COMM_FAILURE ex)
        {
            if (logger.isDebugEnabled())
                logger.debug(this.toString() + ": getMessage() -- COMM_FAILURE");
            this.streamClosed();
            return null;
        }
        catch (org.omg.CORBA.TIMEOUT ex)
        {
            if (logger.isDebugEnabled())
                logger.debug(this.toString() + ": getMessage() -- TIMEOUT");
            this.readTimedOut();
            return null;
        }

        byte[] header = msg_header.value;

        //(minimally) decode GIOP message header. Main checks should
        //be done one layer above.

        if( (char) header[0] == 'G' && (char) header[1] == 'I' &&
            (char) header[2] == 'O' && (char) header[3] == 'P')
        {
            //determine message size
            int msg_size = Messages.getMsgSize( header );

            if( msg_size < 0 )
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Negative GIOP message size: " + msg_size );
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("TCP_IP_GIOPTransport.getMessage() with header: \n" +
                                 header + "\nsize : " + Messages.MSG_HEADER_SIZE );
                }

                return null;
            }

            //get a large enough buffer from the pool
            inbuf.value = buf_mg.getBuffer( msg_size +
                                            Messages.MSG_HEADER_SIZE );

            //copy header
            System.arraycopy( header, 0, inbuf.value, 0, Messages.MSG_HEADER_SIZE );

            try
            {
                transport.read (inbuf, Messages.MSG_HEADER_SIZE,
                                msg_size, msg_size, 0);
            }
            catch (org.omg.CORBA.COMM_FAILURE ex)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Failed to read GIOP message" );
                }
                return null;
            }

            if( dump_incoming )
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("BufferDump:\n" +
                                ObjectUtil.bufToString( inbuf.value,
                                                        0,
                                                        msg_size + Messages.MSG_HEADER_SIZE ));
                }
            }

            if( statistics_provider != null )
            {
                statistics_provider.messageReceived( msg_size +
                                                     Messages.MSG_HEADER_SIZE );
            }

            //this is the "good" exit point.
            return inbuf.value;
        }
        else
        {
            if (logger.isErrorEnabled())
            {
                logger.error( "Failed to read GIOP message, incorrect magic number" );
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("GIOPConnection.getMessage()" + msg_header.value );
            }

            return null;
        }
    }

    public final void receiveMessages()
        throws IOException
    {
        while( true )
        {
            byte[] message = getMessage();

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
                    if (logger.isErrorEnabled())
                    {
                        logger.error( "Invalid GIOP major version encountered: " +
                                      Messages.getGIOPMajor( message ) );
                    }

                    buf_mg.returnBuffer( message );
                    continue;
                }

                int msg_type = Messages.getMsgType( message );

                if( msg_type == MsgType_1_1._Fragment )
                {
                    //GIOP 1.0 messages aren't allowed to be fragmented
                    if( Messages.getGIOPMinor( message ) == 0 )
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( "Received a GIOP 1.0 message of type Fragment" );
                        }

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
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( "Received a GIOP 1.1 Fragment message" );
                        }

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
                        if (logger.isErrorEnabled())
                        {
                            logger.error( "No previous Fragment to this one" );
                        }

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
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( "Received a GIOP 1.0 message with the \"more fragments follow\" bits set" );
                        }

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
                            if (logger.isWarnEnabled())
                            {
                                logger.warn( "Received a GIOP 1.1 message of type " +
                                             msg_type + " with the \"more fragments follow\" bits set" );
                            }

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
                            if (logger.isWarnEnabled())
                            {
                                logger.warn( "Received a fragmented GIOP 1.1 message" );
                            }

                            int giop_minor = Messages.getGIOPMinor( message );

                            ReplyOutputStream out =
                                new ReplyOutputStream( Messages.getRequestId( message ),
                                                       ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                                       giop_minor,
                                                       false,
                                                       logger);//no locate reply

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
                        if (logger.isWarnEnabled())
                        {
                            logger.warn( "Received a GIOP message of type " + msg_type +
                                         " with the \"more fragments follow\" bits set, but this " +
                                         "message type isn't allowed to be fragmented" );
                        }

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
                        if (logger.isErrorEnabled())
                        {
                            logger.error( "Received a message of type " +
                                          msg_type +
                                          " with the more fragments follow bit set, but there is already an fragmented, incomplete message with the same request id " +
                                          request_id + "!" );
                        }

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
                        if (logger.isErrorEnabled())
                        {
                            logger.error("received message with unknown message type " + msg_type);
                        }
                        buf_mg.returnBuffer( message );
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

    private final synchronized void incPendingWrite()
    {
        ++pending_write;
    }

    private final synchronized void decPendingWrite()
    {
        --pending_write;
    }

    public final synchronized void incPendingMessages()
    {
        ++pending_messages;
    }

    public final synchronized void decPendingMessages()
    {
        --pending_messages;
    }

    public final synchronized boolean hasPendingMessages()
    {
        return (pending_messages != 0) || (pending_write != 0);
    }

    /**
     * write (a fragment of) the message (passes it on to the wire)
     */

    public final void write( byte[] fragment, int start, int size )
    {
        transport.write( false, false, fragment, start, size, 0 );

        if (getStatisticsProvider() != null)
        {
            getStatisticsProvider().messageChunkSent (size);
        }
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
            incPendingWrite ();
            getWriteLock();
            if (!transport.is_connected())
            {
                tcs_negotiated = false;

                if (logger.isDebugEnabled())
                    logger.debug(this.toString()
                                 + ": sendMessage() -- opening transport");

                synchronized (connect_sync)
                {
                    try
                    {
                        transport.connect (profile, timeout);
                        connect_sync.notifyAll();
                    }
                    catch (RuntimeException ex)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug(this.toString()
                                         + ": sendMessage() -- failed to open transport");
                        throw ex;
                    }
                }

            }

            out.write_to( this );

            transport.flush();

            if (getStatisticsProvider() != null)
            {
                getStatisticsProvider().flushed();
            }
        }
        finally
        {
            decPendingWrite();
            releaseWriteLock();
        }
    }

    public final boolean isSSL()
    {
        if (transport instanceof IIOPConnection)
        {
            return ((IIOPConnection)transport).isSSL();
        }
        return false;
    }

    public void close()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(this.toString() + ": close()" );
        }

        synchronized (connect_sync)
         {
            if( connection_listener != null )
            {
                connection_listener.connectionClosed();
            }

            transport.close();
            do_close = true;
            connect_sync.notifyAll();
         }
    }


    /**
     * Get the statistics provider for transport usage statistics.
     */
    public final StatisticsProvider getStatisticsProvider()
    {
        return statistics_provider;
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
        if (id < 0 || id >= cubby_count)
        {
            if (logger.isErrorEnabled())
                logger.error( "Get bad cubby id "+id+" (max="+cubby_count+")");
            return null;
        }
        return cubbyholes[id];
    }

    public void set_cubby(int id, Object obj)
    {
        if (id < 0 || id >= cubby_count)
        {
           if (logger.isErrorEnabled())
               logger.error( "Set bad cubby id "+id+" (max="+cubby_count+")");
           return;
        }
        cubbyholes[id] = obj;
    }

}// GIOPConnection
