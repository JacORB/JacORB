package org.jacorb.orb.etf;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.jacorb.util.ObjectUtil;
import org.slf4j.Logger;

/**
 *
 * @author nguyenq
 *
 * The purpose of the ListenEndpoint class is to hold a set of two endpoint
 * address and sslAddress, and other informations that are
 * relevant to the endpoint.  Upon being initiated, the TransportManager will
 * create a list of default ListenEndpoint objects for the endpoints that are
 * specified by the network properties OAAddress, OASSLAddress, OASSLPort,
 * OAPort and OAIAddr, and it will create an overriding list of endpoints that
 * are specified by the command-line -ORBListendEndpints argument.  The
 * TransportManager will then assign ListenEndpoint objects in the lists to
 * the listener factory, IIOPFactories, one object per factory type, which will
 * in turn create the listeners, IIOPListener, for the assigned ListenEndpoint
 * object.
 */
public class ListenEndpoint
    implements Configurable
{
    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;

    /** configuration properties */
    private Logger logger = null;

    private ProtocolAddressBase address = null;
    private ProtocolAddressBase sslAddress = null;
    private String protocolId = null;
    private int optionSSLPort = 0;

    // for debugging purposes
    // private String endpointArgs = null;
    // private String sslEndpointArgs = null;
    // private String[] optionsArgs = null;


    /**
     * Perform the initial configuration for the class
     * @param myConfiguration
     * @throws ConfigurationException
     */
    public void configure(Configuration myConfiguration)
    throws ConfigurationException
    {
        configuration = myConfiguration;
        logger = configuration.getLogger(configuration.getLoggerName(this.getClass()));
    }

    /**
     * Set the non-SSL address for the endpoint
     * @param address is of type ProtocolAddressBase which describes the
     * network parameters for the endpoint.
     */
    public void setAddress(ProtocolAddressBase address)
    {
        this.address = address;
    }

    /**
     * Set the SSL address for the endpoint
     * @param sslAddress is of type ProtocolAddressBase which describes the
     * network SSL parameters for the endpoint.
     */
    public void setSSLAddress(ProtocolAddressBase sslAddress)
    {
        this.sslAddress = sslAddress;
    }

    /**
     * Set the protocol for the endpoint
     * @param protocolId is of type String which describes the protocol id
     * of the endpoint.
     */
    public void setProtocol(String protocolId)
    {
        this.protocolId = protocolId;
    }

    /**
     * Set the SSL port for the endpoint
     * @param optionSSLPort is of type int which describes the SSL service
     * port of the endpoint.
     */
    public void setSSLPort(int optionSSLPort)
    {
        this.optionSSLPort = optionSSLPort;
    }

    /**
     * Return the non-SSL address of the endpoint
     * @return address of type ProtocolAddressBase which describes the non-SSL
     * network parameters of the endpoint.
     */
    public ProtocolAddressBase getAddress()
    {
        return address;
    }

    /**
     * Return the SSL address of the endpoint
     * @return address of type ProtocolAddressBase which describes the non-SSL
     * network SSL parameters of the endpoint.
     */
    public ProtocolAddressBase getSSLAddress()
    {
        return sslAddress;
    }

    /**
     * Return the protocol string of the endpoint.
     * @return protocolId of type String which describes the protocol of the
     * endpoint.
     */
    public String getProtocol()
    {
        return protocolId;
    }

    /**
     * Return the SSL port of the endpoint
     * @return optionSSLPort of type int which describes the SSL port of the
     * endpoint.
     */
    public int getSSLPort()
    {
        return optionSSLPort;
    }
}