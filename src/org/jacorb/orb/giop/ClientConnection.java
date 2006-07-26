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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.util.ObjectUtil;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */
public class ClientConnection
    implements ReplyListener, ConnectionListener
{
    private final GIOPConnection connection;
    private final org.omg.CORBA.ORB orb;

    private final Map replies;

    /**
     * <code>sasContexts</code> is used to support for SAS Stateful contexts.
     */
    private final Map sasContexts;
    private static long last_client_context_id = 0;

    /**
     * <code>client_count</code> denotes how many clients use this connection.
     */
    private int client_count = 0;

    /**
     * <code>id_count</code> is used to generate request ids.
     */
    private int id_count = 0;

    private final ClientConnectionManager conn_mg;

    private final boolean client_initiated;

    private final String info;

    /**
     * <code>gracefulStreamClose</code> indicates if the stream has been closed
     * gracefully, i.e. by a CloseConnection message. This will trigger a
     * remarshaling of all pending messages.
     */
    private boolean gracefulStreamClose;

    /**
     * <code>registeredProfile</code> indicates the profile that was used for
     * registering with the ClientConnectionManager. In case of BiDirIIOP it is
     * NOT equal to the transports profile.
     */
    private final org.omg.ETF.Profile registeredProfile ;

    /**
     * <code>logger</code> is the logger for this object.
     */
    private final Logger logger;

    /**
     * <code>ignoreComponentInfo</code> defaults to off. If jacorb.codeset
     * is turned on then it will NOT ignore component profiles. If
     * jacorb.codeset is turned off then this allows it to ignore codeset profiles
     * in an IOR.
     */
    private final boolean ignoreComponentInfo;



    public ClientConnection( GIOPConnection connection,
                             org.omg.CORBA.ORB orb,
                             ClientConnectionManager conn_mg,
                             org.omg.ETF.Profile registeredProfile,
                             boolean client_initiated )
    {
        this.connection = connection;
        this.orb = orb;
        this.conn_mg = conn_mg;
        this.registeredProfile = registeredProfile;
        this.info = registeredProfile.toString();
        this.client_initiated = client_initiated;

        final Configuration configuration = ((org.jacorb.orb.ORB)orb).getConfiguration();
        logger =
            configuration.getNamedLogger("jacorb.giop.conn");

        ignoreComponentInfo = ! (configuration.getAttributeAsBoolean("jacorb.codeset", true));

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

        replies = new HashMap();
        sasContexts = new HashMap();
    }

    public final GIOPConnection getGIOPConnection()
    {
        return connection;
    }

    /**
     * Get the profile that was used for registering with the
     * ClientConnectionManager. In case of BiDirIIOP it is NOT equal
     * to the transports profile.
     */
    public org.omg.ETF.Profile getRegisteredProfile()
    {
        return registeredProfile;
    }

    public void setCodeSet( ParsedIOR pior )
    {
        if( isTCSNegotiated() )
        {
            //if already negotiated, do nothing
            return;
        }

        //if the other side only talks GIOP 1.0, don't send a codeset
        //context and don't try again
        if( pior.getEffectiveProfile().version().minor == 0 )
        {
            connection.markTCSNegotiated();
            return;
        }

        int tcs = -1;
        int tcsw = -1;

        CodeSetComponentInfo info = pior.getCodeSetComponentInfo();
        if( info == null || ignoreComponentInfo)
        {
            logger.debug("No CodeSetComponentInfo in IOR. Will use default CodeSets" );

            //If we can't find matching codesets, we still mark the
            //GIOPConnection as negotiated, so the following requests
            //will not always try to select a codeset again.

            /* ******
               until the ETF spec is ammended to include components within
               the base Profile type, then this is going to be problem. So
               rather than not setting the codeset component, we should
               pick reasonable default values and send those.
            */

            connection.markTCSNegotiated();
            return;
        }
        else
        {
            tcs = CodeSet.selectTCS( info );
            tcsw = CodeSet.selectTCSW( info );
        }

        if( tcs == -1 || tcsw == -1 )
        {
            if (logger.isDebugEnabled())
            {
                CodeSetComponent original = info.ForCharData;

                logger.debug("Attempted to negotiate with target codeset " + CodeSet.csName(original.native_code_set) + " to match with " +  CodeSet.csName(CodeSet.getTCSDefault()));
                logger.debug("Target has " + (original.conversion_code_sets == null ? 0 : original.conversion_code_sets.length) + " conversion codesets and native has " + CodeSet.csName(CodeSet.getConversionDefault()));
                logger.debug("Was negotiating with IOR " + pior.getIORString());
            }
            //if no matching codesets can be found, an exception is
            //thrown
            throw new org.omg.CORBA.CODESET_INCOMPATIBLE(
                "WARNING: CodeSet negotiation failed! No matching " +
                (( tcs == -1 )? "normal" : "wide") +
                " CodeSet found");
        }

        connection.setCodeSets( tcs, tcsw );

        if (logger.isDebugEnabled())
        {
            logger.debug( "Successfully negotiated Codesets. Using " +
                          CodeSet.csName( tcs ) + " as TCS and " +
                          CodeSet.csName( tcsw ) + " as TCSW" );
        }

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
        return connection.getTCSW();
    }


    public synchronized int getId()
    {
        int result = id_count;

        //if odd or even is determined by the starting value of
        //id_count
        id_count += 2;

        return result;
    }

    /**
     * Increments the number of clients.
     */
    public synchronized void incClients()
    {
        client_count++;
    }

    /**
     * This method decrements the number of clients.
     *
     * @return a <code>boolean</code> value, true if client_count is zero.
     */
    public synchronized boolean decClients()
    {
        boolean result = false;

        client_count--;

        if (client_count == 0 )
        {
            result = true;
        }
        return result;
    }

    /**
     * Returns the number of clients currently using this connection.
     */
    public int numClients()
    {
        return client_count;
    }

    public boolean isClientInitiated()
    {
        return client_initiated;
    }

    /**
     * The request_id parameter is only used, if response_expected.
     */
    public void sendRequest( MessageOutputStream outputStream,
                             ReplyPlaceholder placeholder,
                             int request_id,
                             boolean response_expected )
    {
        Integer key = ObjectUtil.newInteger( request_id );

        synchronized( replies )
        {
            replies.put( key, placeholder );
        }

        try
        {
            sendRequest( outputStream, response_expected );
        }
        catch( org.omg.CORBA.SystemException e )
        {
           //remove reply receiver from list
           //because there will be no response to this request
           synchronized( replies )
           {
              replies.remove( key );
           }
           throw e;
        }
    }

    public void sendRequest( MessageOutputStream outputStream,
                             boolean response_expected )
    {
        try
        {
            connection.sendRequest( outputStream, response_expected );
        }
        catch (java.io.IOException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("IOException", e);
            }

            throw new org.omg.CORBA.COMM_FAILURE
                (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }
    }

    /**
     * called from Delegate/ConnectionManagement etc.
     */

    public void close()
    {
        connection.close();
    }

    /**
     * Operations from ReplyListener
     */

    public void replyReceived( byte[] reply,
                               GIOPConnection connection )
    {
        connection.decPendingMessages();

        Integer key = ObjectUtil.newInteger( Messages.getRequestId( reply ));

        ReplyPlaceholder placeholder = null;

        synchronized( replies )
        {
            placeholder =
                (ReplyPlaceholder) replies.remove( key );
        }

        if( placeholder != null )
        {
            ReplyInputStream ris = new ReplyInputStream (orb, reply);
            ris.setCodeSet (this.getTCS(),this.getTCSW());
            //this will unblock the waiting thread
            placeholder.replyReceived(ris);
        }
        else
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Received reply for unknown request id: " +
                    key);
            }
        }
    }


    public void locateReplyReceived( byte[] reply,
                                     GIOPConnection connection )
    {
        connection.decPendingMessages();

        Integer key = ObjectUtil.newInteger( Messages.getRequestId( reply ));

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
            if (logger.isWarnEnabled())
            {
                logger.warn("Received reply for unknown request id: " +
                    key);
            }
        }
    }

    /**
     * Received a CloseConnection message. Remarshal all pending
     * messages. The close mechanism will be invoked separately by the
     * actual closing of the Transport and will trigger the
     * remarshaling.
     */
    public void closeConnectionReceived( byte[] close_conn,
                                         GIOPConnection connection )
    {
        if (logger.isInfoEnabled())
        {
            logger.info("Received CloseConnection on " + connection.toString());
        }

        if( client_initiated )
        {
            gracefulStreamClose = true;
            ((ClientGIOPConnection) connection).closeAllowReopen();

            //since this is run on the message receptor thread itself, it
            //will not try to read again after returning, because it just
            //closed the transport itself. Therefore, no exception goes
            //back up into the GIOPConnection, where streamClosed() will
            //be called. Ergo, we need to call streamClosed() ourselves.
            streamClosed();
        }
    }


    /**
     * Operations from ConnectionListener
     * used for upcalls from GIOPConnection
     */

    public void connectionClosed()
    {
        if( ! client_initiated )
        {
            //if this is a server side BiDir connection, it will stay
            //pooled in the ClientConnectionManager even if no Delegate is
            //associated with it. Therefore, it has to be removed when
            //the underlying connection closed.

            conn_mg.removeConnection( this );
        }

        streamClosed();
    }

    /**
     * the transport has been
     * removed underneath the GIOP layer
     */

    public void streamClosed()
    {
        synchronized( replies )
        {
            if( replies.size() > 0 )
            {
                if( gracefulStreamClose )
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Stream closed. Will remarshal " +
                                     replies.size() + " messages" );
                    }
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Abnormal connection termination. Lost " +
                                     replies.size() + " outstanding replie(s)!");
                    }
                }

                Iterator entries = replies.values().iterator();
                ReplyPlaceholder placeholder;

                while( entries.hasNext() )
                {
                    placeholder = (ReplyPlaceholder)entries.next();

                    if( gracefulStreamClose )
                    {
                        placeholder.retry();
                    }
                    else
                    {
                        placeholder.cancel();
                    }
                    entries.remove();
                }
            }
        }

        gracefulStreamClose = false;
    }

    public org.omg.ETF.Profile get_server_profile()
    {
        return connection.getTransport().get_server_profile();
    }

    public long cacheSASContext(byte[] client_authentication_token)
    {
        long client_context_id = 0;
        String key = new String(client_authentication_token);
        synchronized ( sasContexts )
        {
            if (!sasContexts.containsKey(key))
            {
                // new context
                client_context_id = ++last_client_context_id;
                sasContexts.put(key, new Long(client_context_id));
                client_context_id = -client_context_id;
            }
            else
            {
                // reuse cached context
                client_context_id = ((Long)sasContexts.get(key)).longValue();
            }
        }
        return client_context_id;
    }

    public long purgeSASContext(long client_context_id)
    {
        synchronized ( sasContexts )
        {
            Iterator entries = sasContexts.keySet().iterator();
            while( entries.hasNext() )
            {
                Object key = entries.next();
                if (((Long)sasContexts.get(key)).longValue() != client_context_id)
                {
                    continue;
                }
                entries.remove();
                break;
            }
        }
        return client_context_id;
    }
}// ClientConnection
