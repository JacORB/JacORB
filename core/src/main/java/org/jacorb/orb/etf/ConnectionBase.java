/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.IIOPAddress;
import org.slf4j.Logger;


/**
 * This an abstract base implementation of the ETF::Connection interface.
 *
 * @author Nicolas Noffke
 * @author Andre Spiegel
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
    protected Configuration configuration;
    protected String connection_info;
    protected ORB orb;

    protected ConnectionBase()
    {
        super();
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

    public void configure(Configuration config)
        throws ConfigurationException
    {
        configuration = config;
        orb = configuration.getORB();

        logger = configuration.getLogger(getClass().getName());

        if(configuration.getAttributeAsBoolean("jacorb.debug.dump_outgoing_messages",false))
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

    /**
     * this is invoked whenever a communication error occurs.
     * subclasses must provide a appropiate implementation.
     * the simplest possible implementation would just pass in
     * the specified exception to to_COMM_FAILURE and return the
     * result.
     */
    protected abstract org.omg.CORBA.COMM_FAILURE handleCommFailure(IOException exception);

    /**
     * convert the specified exception into a CORBA COMM_FAILURE
     */
    protected org.omg.CORBA.COMM_FAILURE to_COMM_FAILURE(IOException exception)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("to_COMM_FAILURE: Caught exception", exception);
        }

        return new org.omg.CORBA.COMM_FAILURE("IOException: "
                                                + exception.toString());
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

    protected final String getLocalhost()
    {
        return IIOPAddress.getLocalHostAddress(logger);
    }
}
