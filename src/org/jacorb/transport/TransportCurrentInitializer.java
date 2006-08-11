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



/**
 * An instance of this class plugs-in the ORB initialization mechanism to make
 * sure the infrastructure the Transport Current feature is using, is properly
 * initialized. The initialization does:
 * 
 * <ul>
 * <li>Registers an initial reference under the "JacOrbTransportCurrent" name;</li>
 * <li>Registers a TransportListener with the ORB's Transport Manager to be
 * able to receive notifications of Transport selection;</li>
 * </ul>
 * 
 * 
 * @author Iliyan Jeliazkov
 */
public class TransportCurrentInitializer extends AbstractTransportCurrentInitializer {

    protected String getName() 
    {
        return "JacOrbTransportCurrent";
    }

    protected DefaultCurrentImpl getCurrentImpl()
    {
        return new DefaultCurrentImpl ();
    }


}// TransportCurrentInitializer

