package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2003 Gerald Brose
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
import org.ietf.jgss.Oid;
import org.omg.CORBA.Any;
import org.omg.GSSUP.InitialContextToken;
import org.omg.GSSUP.InitialContextTokenHelper;

import sun.security.jgss.spi.GSSNameSpi;

/**
 * This is the GSS-API Sercurity Provider Interface (SPI) for the GSSUP Name
 *
 * @author David Robison
 * @version $Id$
 */

public final class GSSUPNameSpi implements GSSNameSpi
{
	/** the logger used by the naming service implementation */
	private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

    private Provider provider;
    private Oid mechOid;
    private Oid nameTypeOid;

    private InitialContextToken subject = null;

    public GSSUPNameSpi (Provider provider, Oid mechOid, byte[] name ,Oid nameTypeOid)
    {
        this.provider = provider;
        this.nameTypeOid = nameTypeOid;
        this.mechOid = mechOid;

        // parse the name
        if (name.length > 0)
        {
            try
            {
                Any any = GSSUPProvider.codec.decode( name );
                subject = InitialContextTokenHelper.extract(any);
            }
            catch (Exception e)
            {
                logger.error("Error decoding for GSSNameSpi: " + e);
                subject = new InitialContextToken(new byte[0], new byte[0], new byte[0]);
            }
        }
        else
        {
            subject = new InitialContextToken(new byte[0], new byte[0], new byte[0]);
        }
    }

    public static byte[] encode(String username, String password, String target_name)
    {
        InitialContextToken subject = new InitialContextToken(username.getBytes(), password.getBytes(), target_name.getBytes());
        Any any = GSSUPProvider.orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        byte[] out = new byte[0];
        try
        {
            out = GSSUPProvider.codec.encode( any );
        }
        catch (Exception e)
        {
            logger.error("Error encoding for GSSNameSpi: " + e);
        }
        return out;
    }

    public static byte[] encode(String username, char[] password, String target_name)
    {
        InitialContextToken subject = new InitialContextToken(username.getBytes(), (new String(password)).getBytes(), target_name.getBytes());
        Any any = GSSUPProvider.orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        byte[] out = new byte[0];
        try
        {
            out = GSSUPProvider.codec.encode( any );
        }
        catch (Exception e)
        {
            logger.error("Error encoding for GSSNameSpi: " + e);
        }
        return out;
    }

    public static InitialContextToken decode(byte[] name)
    {
        try
        {
            Any any = GSSUPProvider.codec.decode( name );
            return InitialContextTokenHelper.extract(any);
        }
        catch (Exception e)
        {
            logger.error("Error decoding for GSSNameSpi: " + e);
        }
        return null;
    }

    public Provider getProvider()
    {
        return provider;
    }

    public boolean equals(GSSNameSpi name) throws GSSException
    {
        return subject.equals(((GSSUPNameSpi)name).subject);
    }

    public byte[] export() throws GSSException
    {
        //System.out.println("GSSUPNameSpi.export");
        Any any = GSSUPProvider.orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        byte[] out = new byte[0];
        try
        {
            out = GSSUPProvider.codec.encode( any );
        }
        catch (Exception e)
        {
            logger.error("Error encoding for GSSNameSpi: " + e);
        }
        return out;
    }

    public Oid getMechanism()
    {
        return mechOid;
    }

    public String toString()
    {
        Any any = GSSUPProvider.orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        byte[] out = new byte[0];
        try
        {
            out = GSSUPProvider.codec.encode( any );
        }
        catch (Exception e)
        {
            logger.error("Error encoding for GSSNameSpi: " + e);
        }
        return new String(out);
    }

    public Oid getStringNameType()
    {
        return nameTypeOid;
    }

    public boolean isAnonymousName()
    {
        System.out.println("GSSUPNameSpi.isAnonymousName");
        return false;
    }
}
