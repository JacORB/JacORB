/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */
package org.jacorb.orb.etf;

import java.util.*;

import org.omg.ETF.*;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public abstract class ListenerBase 
    extends _ListenerLocalBase
    implements Configurable
{
    /**
    * The ORB.
    */
    protected ORB orb = null;
    
    /**
    * The profile of this listener's endpoint.
    */
    protected Profile endpoint = null;
    
    /**
    * The primary acceptor of this listener.
    */
    protected Acceptor acceptor = null;
    
    /**
    * The configuration.
    */
    protected org.jacorb.config.Configuration configuration = null;
    
    /**
    * The logger.
    */
    protected Logger logger = null;
    
    /**
     * Reference to the ORB, for delivering
     * incoming connections via upcalls.
     */
    protected org.omg.ETF.Handle up = null;

    /**
     * Queue of incoming connections, which will be
     * delivered via calls to the accept() method.
     * Connections will only be put into this list
     * if no Handle has been set.
     */
    protected List incoming_connections = new ArrayList();

    protected boolean terminated = false;

    public ListenerBase()
    {
    }
    
    /**
    * @deprecated
    */
    public ListenerBase(ORB orb)
    {
        this.orb = orb;
    }
    
    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        
        if (orb == null)
        {
            // c.f. with the constructor taking an ORB param.
            this.orb = this.configuration.getORB();
        }
        
        logger = this.configuration.getNamedLogger(this.configuration.getLoggerName(this.getClass()));
    }
    
    /**
     * This call establishes the link between the ORB (i.e. the Handle
     * instance) and a server endpoint of the plugged-in transport.
     * All calls upwards into the ORB shall use the given instance.
     */
    public void set_handle(Handle up)
    {
        this.up = up;
    }
    
    /**
     * It is possible that connection requests arrive <i>after</i> the
     * initial creation of the Listener instance but <i>before</i> the
     * conclusion of the configuration of the specific endpoint in this
     * plugin. In order to provide a clear end of this configuration state,
     * we added the listen() method. It is called by the ORB when it ready
     * for incoming connection and thus signals the Listener instance to
     * start processing the incoming connection requests. Therefore,
     * a Listener instance shall not deliver incoming connections to the
     * ORB before this method was called.
     */
    public void listen()
    {
        if (acceptor != null)
            acceptor.start();
    }

    /**
    * Method the Acceptor implementation should call to pass 
    * an opened connection to the ORB.
    */
    protected void deliverConnection(Connection connection)
    {
        if (up != null)
        {
            up.add_input(connection);
        }
        else
        {
            synchronized (incoming_connections)
            {
                incoming_connections.add(connection);
                incoming_connections.notifyAll();
            }
        }
    }

    /**
     * This call is an alternative to using set_handle() to initiate the
     * callback-style of accepting new connections. This call blocks until
     * a client connects to the server. Then a new Connection instance is
     * returned. The transport plug-in must ensure that a thread blocked
     * in accept() returns when destroy() is called with a null object
     * reference. The transport plug-in must raise the CORBA::BAD_INV_ORDER
     * with minor code {TBD} if the ORB calls this operation and set_handle()
     * has ever been called previously on the same listener instance.
     */
    public Connection accept()
    {
        if (up != null)
            throw new org.omg.CORBA.BAD_INV_ORDER
                ("Must not call accept() when a Handle has been set");
        else
        {
            synchronized (incoming_connections)
            {
                while (!terminated &&
                       incoming_connections.isEmpty())
                {
                    try
                    {
                        incoming_connections.wait();
                    }
                    catch (InterruptedException ex)
                    {
                        // ignore
                    }
                }
                if (!terminated)
                    return (Connection)incoming_connections.remove (0);
                else
                    return null;
            }
        }
    }
    
    /**
     * The connection instance is returned to the Listener. It now shall
     * signal any incoming data to the Handle.
     */
    public void completed_data (Connection conn)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    
    /**
     * The Listener is instructed to close its endpoint. It shall no
     * longer accept any connection requests and shall close all
     * connections opened by it.
     */
    public void destroy()
    {
        if (acceptor != null)
            acceptor.terminate();

        this.terminated = true;
        if (up == null)
            incoming_connections.notifyAll();
    }

    
    /**
     * Returns a copy of the profile describing the endpoint
     * of this instance.
     */
    public Profile endpoint()
    {
        return endpoint.copy();
    }
    
    protected abstract class Acceptor 
        extends Thread
    {
        protected abstract void init();
        
        public abstract void run();
        
        public abstract void terminate();
    }
}
