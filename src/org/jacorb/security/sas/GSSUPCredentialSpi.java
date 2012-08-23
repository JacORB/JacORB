package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2012 Gerald Brose / The JacORB Team.
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

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) for the GSSUP Credential
 *
 * @author David Robison
 */

public final class GSSUPCredentialSpi implements GSSCredentialSpi
{

    private Provider myProvider = null;
    private Oid myMechOid = null;
    private GSSNameSpi name = null;
    private int initLifetime;
    private int acceptLifetime;
    private int usage;

    public GSSUPCredentialSpi (Provider myProvider, Oid myMechOid, GSSNameSpi name, int initLifetime, int acceptLifetime, int usage)
    {
        this.myProvider = myProvider;
        this.myMechOid = myMechOid;
        this.name = name;
        this.initLifetime = initLifetime;
        this.acceptLifetime = acceptLifetime;
        this.usage = usage;
    }

    public Provider getProvider()
    {
        return myProvider;
    }

    public void dispose() throws GSSException
    {
        Provider myProvider = null;
        Oid myMechOid = null;
        GSSNameSpi name = null;
    }

    public GSSNameSpi getName() throws GSSException
    {
        return name;
    }

    public int getInitLifetime() throws GSSException
    {
        return initLifetime;
    }

    public int getAcceptLifetime() throws GSSException
    {
        return acceptLifetime;
    }

    public boolean isInitiatorCredential() throws GSSException
    {
        return (usage == GSSCredential.INITIATE_ONLY);
    }

    public boolean isAcceptorCredential() throws GSSException
    {
        return (usage == GSSCredential.ACCEPT_ONLY);
    }

    public Oid getMechanism()
    {
        return myMechOid;
    }
}
