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

import sun.security.jgss.spi.*;
import org.ietf.jgss.*;
import java.security.*;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) for the GSSUP Name
 *
 * @author David Robison
 * @version $Id$
 */

public final class GSSUPNameSpi implements GSSNameSpi
{

    private Provider myProvider;
    private Oid myMechOid;
    private byte[] myName;
    private Oid myNameTypeOid;

    public GSSUPNameSpi (Provider myProvider, Oid myMechOid, byte[] name ,Oid nameTypeOid)
    {
        this.myProvider = myProvider;
        this.myMechOid = myMechOid;
        this.myName = name;
        this.myNameTypeOid = nameTypeOid;
    }

    public Provider getProvider()
    {
        return myProvider;
    }

    public boolean equals(GSSNameSpi name) throws GSSException
    {
        //System.out.println("GSSUPNameSpi.equals");
        return myName.equals(name);
    }

    public byte[] export() throws GSSException
    {
        //System.out.println("GSSUPNameSpi.export");
        return null;
    }

    public Oid getMechanism()
    {
        return myMechOid;
    }

    public String toString()
    {
        return new String(myName);
    }

    public Oid getStringNameType()
    {
        return myNameTypeOid;
    }

    public boolean isAnonymousName()
    {
        System.out.println("GSSUPNameSpi.isAnonymousName");
        return false;
    }
}

