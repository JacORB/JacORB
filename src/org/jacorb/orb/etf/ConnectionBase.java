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
package org.jacorb.orb.etf;

import java.io.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

/**
 * This an abstract base implementation of the ETF::Connection interface.
 *
 * 
 *
 * @author Nicolas Noffke / Andre Spiegel 
 * @version $Id$
 */

public abstract class ConnectionBase
    extends org.omg.ETF._ConnectionLocalBase
    implements Configurable
{
    protected boolean connected = false;
    
    /**
    * Optionally initialised to be used in the dumping of messages.
    * See property <code>jacorb.debug.dump_outgoing_messages</code>.
    * Default is off.
    */
    protected ByteArrayOutputStream b_out = null;
    
    /**
    * Time out after a close connection has been received.
    * See property <code>jacorb.connection.timeout_after_closeconnection</code>.
    * Default 20000 milliseconds.
    */ 
    protected int finalTimeout = 20000;
    
    /**
    * The Profile of the target / server side of the connection.
    */
    protected ProfileBase profile = null;

    /** shared with sub classes */
    protected Logger logger;
    protected org.jacorb.config.Configuration configuration;
    protected String connection_info;
    
    protected ConnectionBase()
    {
    }
    
    /**
    * Initialise this instance as a copy of another. Intended for use within subclass
    * constructors.
    */
    protected ConnectionBase(ConnectionBase other)
    {
        this.b_out = other.b_out;
        this.connection_info = other.connection_info;
        this.finalTimeout = other.finalTimeout;
        this.profile = other.profile;
    }
    
    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)configuration;
        logger = this.configuration.getNamedLogger(
                 this.configuration.getLoggerName(this.getClass()));

        if( configuration.getAttribute("jacorb.debug.dump_outgoing_messages","off").equals("on"))
        {
            b_out = new ByteArrayOutputStream();
        }

        finalTimeout =
            configuration.getAttributeAsInteger("jacorb.connection.timeout_after_closeconnection",
                                                20000 );
    }
    
    protected abstract void setTimeout(int timeout);
    
    protected abstract int getTimeout();
    
    public org.omg.ETF.Profile get_server_profile()
    {
        return profile;
    }
    
    public synchronized boolean is_connected()
    {
        return connected;
    }

    /**
     * This is used to tell the transport that a CloseConnection has
     * been sent, and that it should set a timeout in case the client
     * doesn't close its side of the connection right away.
     *
     * This should only be called on the thread that listens on the
     * socket because timeouts are not applied until read() is called
     * the next time.
     */
    public void turnOnFinalTimeout()
    {                
        setTimeout( finalTimeout );
    }

    protected org.omg.CORBA.COMM_FAILURE to_COMM_FAILURE (IOException ex)
    {
        return new org.omg.CORBA.COMM_FAILURE("IOException: "
                                               + ex.toString());
    }

    /**
     * Wait for the given time_out period for incoming data on this
     * connection. It shall return false if this call times out and
     * no data is available. It may not throw a  TIMEOUT  exception.
     * If data can already be read or arrives before the end of the
     * time out, this function shall return true, immediately.
     */
    public boolean wait_next_data (long time_out)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    /**
     * A boolean flag describing whether this connection supports the
     * Bidirectional GIOP mechanism as described by GIOP-1.2 in CORBA 2.3.1
     * (OMG Document: formal/99-10-07). It shall return true if it does,
     * and false if it does not.
     */
    public boolean supports_callback()
    {
        return true;
    }

    /**
     * A flag directing the ORB to use either the Handle class to perform
     * data queries with a time_out, or the transport layer (through this
     * connection). The instance shall return true, if the Handle should
     * signal time outs for read operations. Then the ORB may not call
     * wait_next_data. Otherwise, a false shall be returned, and the
     * function wait_next_data shall be implemented by this class.
     */
    public boolean use_handle_time_out()
    {
        // We have neither mechanism in JacORB.
        // I wonder if we should.  AS.
        return false;
    }
}

