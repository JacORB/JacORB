package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2002 Gerald Brose
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

import java.security.Provider;
import org.omg.GSSUP.*;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) Provider for the GSSUP Name
 *
 * @author David Robison
 * @version $Id$
 */

public class NT_GSSUPProvider extends Provider
{

    protected static org.omg.CORBA.ORB orb = null;
    protected static org.omg.IOP.Codec codec = null;

    /**
     * Returns the default GSSManager implementation.
     *
     * @return a GSSManager implementation
     */
    public NT_GSSUPProvider()
    {
            super("NT-GSSUP", 1.0, "JacORB NT GSSUP provider v1.0");
            NT_GSSUPMechFactory.myProvider = this;
            this.put("GssApiMechanism.2.23.130.1.1.1", "org.jacorb.security.sas.NT_GSSUPMechFactory");
    }
}
