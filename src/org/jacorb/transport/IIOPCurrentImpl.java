package org.jacorb.transport;

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


import java.net.Socket;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPConnection;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.transport.iiop.Current;


/**
 * An instance of this class plugs-in the ORB initialization mechanism to make
 * sure the infrastructure the IIOP-specific Transport Current needs, is
 * properly initialized.
 * 
 * @author Iliyan Jeliazkov
 */

public class IIOPCurrentImpl extends DefaultCurrentImpl implements Current {

    /**
     * ctor
     * 
     * @param c -
     *            Configuration
     */
    public IIOPCurrentImpl() {
    }


    public int remote_port() throws NoContext {

        return ((IIOPAddress) ((IIOPProfile) getLatestTransportCurentEvent ().profile ()).getAddress ()).getPort ();
    }


    public String remote_host() throws NoContext {

        return ((IIOPAddress) ((IIOPProfile) getLatestTransportCurentEvent ().profile ()).getAddress ()).getHostname ();
    }


    public int local_port() throws NoContext {

        Socket so = ((IIOPConnection) getLatestTransportCurentEvent ().transport ()).getSocket ();
        if (so == null) return 0;
        return so.getLocalPort();
    }


    public String local_host() throws NoContext {

        Socket so = ((IIOPConnection) getLatestTransportCurentEvent ().transport ()).getSocket ();
        if (so == null) return null;
        return so.getLocalAddress ().getCanonicalHostName ();
    }


    public String toString() {

        try {
            return getClass ().getName () + "[ " + local_host () + ':'
                            + local_port () + '-' + remote_host () + ':'
                            + remote_port () + ']';
        }
        catch (NoContext e) {
            return getClass ().getName () + "[ NoContext ]";
        }
    }


}
