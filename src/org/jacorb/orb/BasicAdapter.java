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

package org.jacorb.orb;

import java.lang.reflect.Constructor;
import java.net.*;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.orb.factory.SSLServerSocketFactory;
import org.jacorb.orb.factory.ServerSocketFactory;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.util.Debug;
import org.jacorb.util.Environment;
import org.omg.ETF.Connection;
import org.omg.PortableServer.POA;

/**
 *
 * Class BasicAdapter, used by the POA.
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */
public class BasicAdapter extends org.omg.ETF._HandleLocalBase
{
    public  static SSLServerSocketFactory ssl_socket_factory = null;
    private static ServerSocketFactory socket_factory = null;

    static
    {
        socket_factory = SocketFactoryManager.getServerSocketFactory ((ORB) null);
    }

    private org.jacorb.orb.ORB orb;
    private POA rootPOA;
    private org.omg.ETF.Listener listener;

    private MessageReceptorPool receptor_pool = null;
    private RequestListener request_listener = null;
    private ReplyListener reply_listener = null;
    private int timeout = 0;

    private TransportManager transport_manager = null;
    private GIOPConnectionManager giop_connection_manager = null;

    public BasicAdapter( org.jacorb.orb.ORB orb,
                         POA rootPOA,
                         TransportManager transport_manager,
                         GIOPConnectionManager giop_connection_manager )
        throws org.omg.CORBA.INITIALIZE
    {
        this.orb = orb;
        this.rootPOA = rootPOA;
        this.transport_manager = transport_manager;
        this.giop_connection_manager = giop_connection_manager;

        if( Environment.isPropertyOn( "jacorb.security.support_ssl" ))
        {
            if( ssl_socket_factory == null )
            {
                String s = Environment.getProperty( "jacorb.ssl.server_socket_factory" );
                if( s == null || s.length() == 0 )
                {
                    throw new org.omg.CORBA.INITIALIZE( "SSL support is on, but the property \"jacorb.ssl.server_socket_factory\" is not set!" );
                }

                try
                {
                    Class ssl = Class.forName( s );

                    Constructor constr = ssl.getConstructor( new Class[]{
                        org.jacorb.orb.ORB.class });

                    ssl_socket_factory = (SSLServerSocketFactory)
                        constr.newInstance( new Object[]{ orb });
                }
                catch (Exception e)
                {
                    Debug.output( Debug.IMPORTANT | Debug.ORB_CONNECT,
                                  e );

                    throw new org.omg.CORBA.INITIALIZE( "SSL support is on, but the ssl server socket factory can't be instanciated (see trace)!" );
                }
            }

        }

        receptor_pool = MessageReceptorPool.getInstance();
        request_listener = new ServerRequestListener( orb, rootPOA );
        reply_listener = new NoBiDirServerReplyListener();

        String prop =
            Environment.getProperty("jacorb.connection.server_timeout");

        if( prop != null )
        {
            timeout = Integer.parseInt(prop);
        }

        listener = new IIOPListener();
        listener.set_handle (this);
        listener.listen();
    }

    public RequestListener getRequestListener()
    {
        return request_listener;
    }

    /**
     * obsolete
     */
    public int getPort()
    {
        IIOPProfile profile = (IIOPProfile)listener.endpoint();
        return profile.getAddress().getPort();
    }

    public int getSSLPort()
    {
        IIOPProfile profile = (IIOPProfile)listener.endpoint();
        return profile.getSSLPort();
    }

    public boolean hasSSLListener()
    {
        return getSSLPort() != -1;
    }

    /**
     * @returns the IP address we are listening on
     */

    public String getAddress()
    {
        IIOPProfile profile = (IIOPProfile)listener.endpoint();
        return profile.getAddress().getHost();
    }

    /**
     * to be called from the POA, code duplicated for performance
     * reasons to avoid synchronization in the private version of this
     * method.
     */
    public synchronized void deliverRequest( org.jacorb.orb.dsi.ServerRequest request,
                                             org.omg.PortableServer.POA poa )
    {
        org.jacorb.poa.POA tmp_poa = (org.jacorb.poa.POA)poa;
        String scopes[] = request.remainingPOAName();

        try
        {
            for( int i=0; i < scopes.length-1; i++)
            {
                if( scopes[i].equals(""))
                {
                    request.setRemainingPOAName(null);
                    break;
                }
                try
                {
                    tmp_poa = tmp_poa._getChildPOA( scopes[i] );
                }
                catch ( org.jacorb.poa.except.ParentIsHolding p )
                {
                    /*
                     * if one of the POAs is in holding state, we
                     * simply deliver deliver the request to this
                     * POA. It will forward the request to its child
                     * POAs if necessary when changing back to active
                     * For the POA to be able to forward this request
                     * to its child POAa, we need to supply the
                     * remaining part of the child's POA name
                     */
                    String[] rest_of_name = new String[scopes.length - i];
                    for( int j = 0; j < i; j++ )
                        rest_of_name[j] = scopes[j+i];
                    request.setRemainingPOAName(rest_of_name);
                    break;
                }
            }

            if( tmp_poa == null )
            {
                throw new Error("request POA null!");
            }
            else
            {
                /* hand over to the POA */
                ((org.jacorb.poa.POA)tmp_poa)._invoke( request );
            }

        }
        catch( org.omg.PortableServer.POAPackage.WrongAdapter wa )
        {
            // unknown oid (not previously generated)
            request.setSystemException( new org.omg.CORBA.OBJECT_NOT_EXIST("unknown oid") );
            request.reply();
        }
        catch( org.omg.CORBA.SystemException one )
        {
            request.setSystemException( one );
            request.reply();
        }
        catch( Throwable th )
        {
            request.setSystemException( new org.omg.CORBA.UNKNOWN( th.toString()) );
            request.reply();
            th.printStackTrace(); // TODO
        }
    }

    /**
     * to be called from the POA
     */

    public synchronized void return_result(org.jacorb.orb.dsi.ServerRequest request)
    {
        request.reply();
    }

    public void stopListeners()
    {
        listener.destroy();
    }

    // Handle methods below this line

    /**
     * Announces a new connection instance to the ORB. 
     * The caller shall examine the boolean return value and
     * destroy the connection, if the call returns false.
     * A new connection initially belongs to the plug-in, 
     * and it shall signal the connection to the ORB when 
     * the first incoming request data was received, 
     * using this Handle upcall.
     * <p>
     * The Handle shall accept the connection (and cache
     * information about it if needed), as long as it is
     * allowed to do so by the ORB. In this case it shall
     * return true. If a new connection is currently not 
     * allowed, it shall ignore the passed instance and 
     * return false.
     */
    public boolean add_input (org.omg.ETF.Connection conn)
    {
        GIOPConnection giopConnection =
            giop_connection_manager.createServerGIOPConnection
            (
                          listener.endpoint(),
                          conn,
                          request_listener,
                          reply_listener
            );
        receptor_pool.connectionCreated( giopConnection );
        return true;
    }

    /**
     * In some cases, the client side can initiate the closing of a 
     * connection. The plugin shall signal this event to the server side 
     * ORB via its Handle by calling this function.
     */
    public void closed_by_peer (org.omg.ETF.Connection conn)
    {
        // We don't do this in JacORB; Connections are never
        // given back to the Listener after they have been
        // passed up initially.
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * The plugged-in transport (e.g. the Listener instance) shall call
     * this function when it owns a server-side Connection and data arrives 
     * on the local endpoint. This will start a new request dispatching 
     * cycle in the ORB. Subsequently, it shall ignore any other incoming
     * data from this Connection until the Listener's completed_data function 
     * is called by the ORB.
     */
    public void signal_data_available (Connection conn)
    {
        // We don't do this in JacORB; Connections are never
        // given back to the Listener after they have been
        // passed up initially.
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
