package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2004 Gerald Brose
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

import org.apache.avalon.framework.logger.Logger;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) Facotry GSSUP GSSManager
 *
 * @author David Robison
 * @version $Id$
 */

public final class GSSUPMechFactory
    implements MechanismFactory
{
    // private static Logger logger = Debug.getNamedLogger("jacorb.SAS.GSSUP");

    protected static Provider myProvider;

    private Oid myMechOid;
    private Oid[] nameTypes = 
       new Oid[] {GSSName.NT_EXPORT_NAME};

    public GSSUPMechFactory ()
    {
        try
        {
            myMechOid = new Oid("2.23.130.1.1.1");
        }
        catch (GSSException e)
        {
            // logger.error("GSSUPMechanism: " + e);
        }
    }

    public Oid getMechanismOid()
    {
        return myMechOid;
    }

    public Provider getProvider()
    {
        return myProvider;
    }

    public Oid[] getNameTypes()
    {
        return nameTypes;
    }

    public GSSCredentialSpi getCredentialElement(GSSNameSpi name, int initLifetime, int acceptLifetime, int usage) throws GSSException
    {
        return new GSSUPCredentialSpi(myProvider, myMechOid, name, initLifetime, acceptLifetime, usage);
    }

    public GSSNameSpi getNameElement(String name, Oid nameTypeOid) throws GSSException
    {
        return getNameElement(name.getBytes(), nameTypeOid);
    }

    public GSSNameSpi getNameElement(byte[] name ,Oid nameTypeOid) throws GSSException
    {
        return new GSSUPNameSpi(myProvider, myMechOid, name, nameTypeOid);
    }

    public GSSContextSpi getMechanismContext(GSSNameSpi nameSpi, GSSCredentialSpi credSpi, int lifetime) throws GSSException
    {
        return new GSSUPContextSpi(myProvider, myMechOid, nameSpi, credSpi, lifetime);
    }

    public GSSContextSpi getMechanismContext(GSSCredentialSpi credSpi) throws GSSException
    {
        return new GSSUPContextSpi(credSpi.getProvider(), credSpi.getMechanism(), credSpi.getName(), credSpi, credSpi.getInitLifetime());
    }

    public GSSContextSpi getMechanismContext(byte[] b1) throws GSSException
    {
        return null;
    }
}
