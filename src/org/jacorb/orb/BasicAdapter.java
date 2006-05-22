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

package org.jacorb.orb;

import java.lang.reflect.Constructor;
import java.net.*;
import java.util.*;

import org.jacorb.orb.giop.*;
import org.jacorb.orb.iiop.*;
import org.jacorb.orb.factory.SSLServerSocketFactory;
import org.jacorb.orb.factory.ServerSocketFactory;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.orb.factory.SocketFactoryManager;
import org.jacorb.util.ObjectUtil;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import org.omg.CORBA.INTERNAL;
import org.omg.ETF.*;
import org.omg.PortableServer.POA;

/**
 *
 * Class BasicAdapter, used by the POA.
 *
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public class BasicAdapter
    extends org.omg.ETF._HandleLocalBase
    implements Configurable
{
    public  SSLServerSocketFactory ssl_socket_factory = null;
    private ServerSocketFactory socket_factory = null;

    private org.jacorb.orb.ORB orb;
    private POA rootPOA;

    private List listeners = new ArrayList();

    private MessageReceptorPool receptor_pool = null;
    private ServerRequestListener request_listener = null;
    private ReplyListener reply_listener = null;

    private TransportManager transport_manager = null;
    private GIOPConnectionManager giop_connection_manager = null;

    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger = null;

    /**
     * called from ORB.java
     */

    BasicAdapter( org.jacorb.orb.ORB orb,
                  POA rootPOA,
                  TransportManager transport_manager,
                  GIOPConnectionManager giop_connection_manager )
    {
        this.orb = orb;
        this.rootPOA = rootPOA;
        this.transport_manager = transport_manager;
        this.giop_connection_manager = giop_connection_manager;
    }

    /**
     * configure the BasicAdapter
     */

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration =
            (org.jacorb.config.Configuration)myConfiguration;
        logger =
            configuration.getNamedLogger("jacorb.orb.basic");

        socket_factory =
            transport_manager.getSocketFactoryManager().getServerSocketFactory();

        if( configuration.getAttribute("jacorb.security.support_ssl","off").equals("on"))
        {
            if( ssl_socket_factory == null )
            {
                String s =
                    configuration.getAttribute( "jacorb.ssl.server_socket_factory","" );
                if(  s.length() == 0 )
                {
                    throw new org.omg.CORBA.INITIALIZE( "SSL support is on, but the property \"jacorb.ssl.server_socket_factory\" is not set!" );
                }

                try
                {
                    Class ssl = ObjectUtil.classForName( s );

                    Constructor constr =
                        ssl.getConstructor( new Class[]{org.jacorb.orb.ORB.class });

                    ssl_socket_factory =
                        (SSLServerSocketFactory)constr.newInstance( new Object[]{ orb });

                    ((Configurable)ssl_socket_factory).configure(configuration);
                }
                catch (Exception e)
                {
                    logger.warn("Exception",e);

                    throw new org.omg.CORBA.INITIALIZE( "SSL support is on, but the ssl server socket factory can't be instanciated (see trace)!" );
                }
            }

        }

        receptor_pool = new MessageReceptorPool("ServerMessageReceptor", myConfiguration);

        request_listener = new ServerRequestListener( orb, rootPOA );
        request_listener.configure( configuration );
        reply_listener = new NoBiDirServerReplyListener();

        // create all Listeners
        for (Iterator i = getListenerFactories().iterator();
             i.hasNext();)
        {
             Factories f = (Factories)i.next();
             Listener l = f.create_listener (null, (short)0, (short)0);
             l.set_handle(this);
             listeners.add (l);
        }

        // activate them
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            ((Listener)i.next()).listen();
        }
    }

    public SSLServerSocketFactory getSSLSocketFactory()
    {
        return ssl_socket_factory;
    }


    /**
     * Returns a List of Factories for all transport plugins that
     * should listen for incoming connections.
     */
    private List getListenerFactories()
        throws ConfigurationException
    {
        List result = new ArrayList();
        List tags =
            configuration.getAttributeList("jacorb.transport.server.listeners");

        if (tags.isEmpty())
        {
            result.addAll(transport_manager.getFactoriesList());
        }
        else
        {
            for (Iterator i = tags.iterator(); i.hasNext();)
            {
                String s = ((String)i.next());
                int tag = -1;
                try
                {
                    tag = Integer.parseInt(s);
                }
                catch (NumberFormatException ex)
                {
                    throw new RuntimeException
                        ("could not parse profile tag for listener: " + s
                         + " (should have been a number)");
                }
                Factories f = transport_manager.getFactories (tag);
                if (f == null)
                    throw new RuntimeException
                        ("could not find Factories for profile tag: " + tag);
                else
                    result.add(f);
            }
        }
        return result;
    }

    public RequestListener getRequestListener()
    {
        return request_listener;
    }

    /**
     * Returns a List of endpoint profiles for all transports that listen
     * for incoming connections.  Each individual profile is a copy and can
     * safely be modified by the caller (e.g. add an object key, patch the
     * address, stuff it into an IOR, etc.).
     */
    public List getEndpointProfiles()
    {
        List result = new ArrayList();
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            Listener l = (Listener)i.next();
            result.add (l.endpoint());
        }
        return result;
    }

    /**
     * If only a single IIOPListener (and no other Listener) is
     * active for this BasicAdapter, then this method returns it.
     * Otherwise it returns null.
     */
    private IIOPListener getIIOPListener()
    {
        if (listeners.size() == 1)
        {
            Listener l = (Listener)listeners.get(0);
            if (l instanceof IIOPListener)
                return (IIOPListener)l;
            else
                return null;
        }
        else
            return null;
    }

    /**
     * @deprecated This method cannot return a sensible result in the presence
     * of alternate transports, use {@link #getEndpointProfiles()} instead.
     */
    public int getPort()
    {
        IIOPListener l = getIIOPListener();
        if (l != null)
        {
            IIOPProfile profile = (IIOPProfile)l.endpoint();
            return ((IIOPAddress)profile.getAddress()).getPort();
        }
        else
        {
            throw new RuntimeException
                ("Cannot find server port for non-IIOP transport");
        }
    }

    /**
     * @deprecated This method cannot return a sensible result in the presence
     * of alternate transports, use {@link #getEndpointProfiles()} instead.
     */
    public int getSSLPort()
    {
        IIOPListener l = getIIOPListener();
        if (l != null)
        {
            IIOPProfile profile = (IIOPProfile)l.endpoint();
            return profile.getSSLPort();
        }
        else
        {
            throw new RuntimeException
                ("Non-IIOP transport does not have an SSL port");
        }
    }

    /**
     * @deprecated This method cannot return a sensible result in the presence
     * of alternate transports, use {@link #getEndpointProfiles()} instead.
     */
    public boolean hasSSLListener()
    {
        return getSSLPort() != -1;
    }

    /**
     * @deprecated This method cannot return a sensible result in the presence
     * of alternate transports, use {@link #getEndpointProfiles()} instead.
     */
    public String getAddress()
    {
        IIOPListener l = getIIOPListener();
        if (l != null)
        {
            IIOPProfile profile = (IIOPProfile)l.endpoint();
            return ((IIOPAddress)profile.getAddress()).getHostname();
        }
        else
        {
            throw new RuntimeException
                ("Cannot find server address for non-IIOP transport");
        }
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
                throw new INTERNAL("Request POA null!");
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
            logger.warn("unexpected exception", th);
            // TODO throw exception?
        }
    }

    /**
     * to be called from the POA
     */

    public void return_result(org.jacorb.orb.dsi.ServerRequest request)
    {
        request.reply();
    }

    public void stopListeners()
    {
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            ((Listener)i.next()).destroy();
        }
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
                conn.get_server_profile(),
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
