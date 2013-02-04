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
 * */

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
    private int optionSSLlPort = 0;

    // for debugging purposes
    private String endpointArgs = null;
    private String sslEndpointArgs = null;
    private String[] optionsArgs = null;


    public void configure(Configuration myConfiguration)
    throws ConfigurationException
    {
        configuration = myConfiguration;
        logger = configuration.getLogger(configuration.getLoggerName(this.getClass()));
    }

    public void setAddress(ProtocolAddressBase address)
    {
        this.address = address;
    }

    public void setSSLAddress(ProtocolAddressBase sslAddress)
    {
        this.sslAddress = sslAddress;
    }

    public void setProtocol(String protocolId)
    {
        this.protocolId = protocolId;
    }

    public void setSSLPort(int optionSSLPort)
    {
        this.optionSSLlPort = optionSSLlPort;
    }

    public ProtocolAddressBase getAddress()
    {
        return address;
    }

    public ProtocolAddressBase getSSLAddress()
    {
        return sslAddress;
    }

    public String getProtocol()
    {
        return protocolId;
    }

    public int getSSLPort()
    {
        return optionSSLlPort;
    }
}