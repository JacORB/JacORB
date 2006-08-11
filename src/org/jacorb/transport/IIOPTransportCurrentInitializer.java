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
 * sure the infrastructure the IIOP-specific Transport Current needs, is
 * properly initialized.
 * 
 * @author Iliyan Jeliazkov
 */
public class IIOPTransportCurrentInitializer extends TransportCurrentInitializer {

    protected String getName() 
    {
        return "JacOrbIIOPTransportCurrent";
    }

    protected DefaultCurrentImpl getCurrentImpl()
    {
        return new IIOPCurrentImpl ();
    }

} // IIOPTransportCurrentInitializer

