package org.jacorb.orb.etf;

import org.omg.IOP.TAG_UIPMC;

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


/**
 * The purpose of the ListenEndpoint class is to hold a set of two endpoint
 * address and sslAddress, and other informations that are
 * relevant to the endpoint.  Upon being initiated, the TransportManager will
 * create a list of default ListenEndpoint objects for the endpoints that are
 * specified by the network properties OAAddress, OASSLAddress, OASSLPort,
 * OAPort and OAIAddr, and it will create an overriding list of endpoints that
 * are specified by the command-line -ORBListendEndpints argument.  The
 * TransportManager will then store the ListenEndpoint objects which will
 * be used by BasicAdapter to update the listeners for the assigned ListenEndpoint
 * object.
 *
 * @author nguyenq
 *
 */
public class ListenEndpoint
{
    public static enum Protocol
    {
        IIOP, MIOP, UIOP, NIOP, SSLIOP, DIOP;

        /**
         * Maps an ETF Factory integer to a Protocol
         *
         * @param tag
         * @return
         */
        public static Protocol mapProfileTag (Integer tag)
        {
            if (tag == TAG_UIPMC.value)
            {
                return MIOP;
            }
            else
            {
                return IIOP;
            }
        }
    };

    private ProtocolAddressBase address;
    private ProtocolAddressBase sslAddress;
    private Protocol protocolId;

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
     * @param protocolId is of type Protocol which describes the protocol id
     * of the endpoint.
     */
    public void setProtocol(Protocol protocolId)
    {
        this.protocolId = protocolId;
    }

    /**
     * Return a <b>copy</b> of the non-SSL address of the endpoint
     * @return address of type ProtocolAddressBase which describes the non-SSL
     * network parameters of the endpoint.
     */
    public ProtocolAddressBase getAddress()
    {
        return (address == null ? null : address.copy());
    }

    /**
     * Return a <b>copy</b> of the SSL address of the endpoint
     * @return address of type ProtocolAddressBase which describes the non-SSL
     * network SSL parameters of the endpoint.
     */
    public ProtocolAddressBase getSSLAddress()
    {
        return (sslAddress == null ? null : sslAddress.copy());
    }

    /**
     * Return the protocol of the endpoint.
     * @return protocolId of type Protocol which describes the protocol of the
     * endpoint.
     */
    public Protocol getProtocol()
    {
        return protocolId;
    }
}
