/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

// package ImplementationRepository;
package org.jacorb.tao_imr.ImplementationRepository;

import org.omg.CORBA.*;


/**
 *
 * @author Quynh Nguyen
 *
 * This is the implementation of the ServerObject interface, which serves as a callback
 * when the JacORB is registered with a TAO ImR.  It allows the TAO ImR to ping
 * a JacORB server and shutdown the server when needed.
 */
public class ServerObjectImpl
    extends ServerObjectPOA
{
    private org.omg.CORBA.ORB orb_ = null;
    org.omg.PortableServer.POA poa_ = null;
    org.omg.PortableServer.POA root_poa_ = null;

    public ServerObjectImpl (org.omg.CORBA.ORB orb,
                      org.omg.PortableServer.POA poa,
                      org.omg.PortableServer.POA root_poa)
    {
        this.orb_ = orb;
        this.poa_ = poa;
        this.root_poa_ = root_poa; // This is rootPOA
    }

    /**
     * TAO ImR will use this call checkup on a JacORB server
     */
    public void ping()
    {

    }

    /**
     * TAO ImR will use this call to shutdown a JacORB server
     */
    public void shutdown()
    {

        try
        {
            // Note : We want our child POAs to be able to unregister themselves from
            // the ImR, so we must destroy them before shutting down the orb.
            if (poa_ instanceof org.jacorb.poa.POA)
            {
               ((org.jacorb.poa.POA) this.root_poa_).destroy(true, false);
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        try
        {
            orb_.shutdown (false);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }

    }

    /**
     *
     * @return the POA object that was ginven to this object
     */
    public org.omg.PortableServer.POA _default_POA()
    {
        return (org.omg.PortableServer.POA) root_poa_;
    }
}
