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

public final class GSSUPNameSpi 
    implements GSSNameSpi
{
    private static Oid mechOid;

    private Provider provider;
    private Oid nameTypeOid;

    private InitialContextToken subject = null;

    static
    {
        try
        {
            mechOid = new Oid("2.23.130.1.1.1");
        }
        catch (GSSException e)
        {
        }
    }

    public GSSUPNameSpi(Provider provider, Oid mechOid, byte[] name ,Oid nameTypeOid)
    {
        this.provider = provider;
        this.nameTypeOid = nameTypeOid;
        //GSSUPNameSpi.mechOid = mechOid;

        // parse the name
        if (name.length > 0)
        {
            try
            {
                Any any = 
                    GSSUPProvider.codec.decode_value( name, 
                                                      InitialContextTokenHelper.type());
                subject = InitialContextTokenHelper.extract(any);
            }
            catch (Exception e)
            {
                // logger.error("Error creating GSSNameSpi: " + e);
                subject = new InitialContextToken(new byte[0], new byte[0], new byte[0]);
            }
        }
        else
        {
            subject = new InitialContextToken(new byte[0], new byte[0], new byte[0]);
        }
    }

    public static byte[] encode(String username, String password, byte[] target_name)
    {
        InitialContextToken subject = null;
        try
        {
            subject = new InitialContextToken( username.getBytes("UTF-8"),
                                               password.getBytes("UTF-8"),
                                               target_name);
        }
        catch(java.io.UnsupportedEncodingException e)
        {
            //should never happen
            // logger.error("Error creating InitialContextToken: " + e);
            return new byte[0];
        }
        byte[] out = null;
        Any any = GSSUPProvider.orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        try
        {
            out = GSSUPProvider.codec.encode_value( any );
        }
        catch (Exception e)
        {
            // logger.error("Error encoding for GSSNameSpi: " + e);
            return new byte[0];
        }

        byte[] mechOidArray = null;
        try
        {
            mechOidArray = mechOid.getDER();
        }
        catch(org.ietf.jgss.GSSException e)
        {
            // logger.error("Error retrieving mechOid DER: " + e);
            return new byte[0];
        }

        int length = out.length + mechOidArray.length;
        byte[] encodedLength = null;

        if((length >> 7) == 0)
        {
            //length fits into 7 bit
            encodedLength = new byte[]{(byte) 0x60,
                                       (byte) length};
        }
        else if((length >> 14) == 0)
        {
            //length fits into 14 bit
            encodedLength = new byte[]{(byte) 0x60,
                                       (byte) ((length >> 7) | 0x80),
                                       (byte)  (length & 0x7F)};
        }
        else if((length >> 21) == 0)
        {
            //length fits into 21 bit
            encodedLength = new byte[]{(byte) 0x60,
                                       (byte)  ((length >> 14)         | 0x80),
                                       (byte) (((length >>  7) & 0x7F) | 0x80),
                                       (byte)   (length        & 0x7F)};
        }
        else if((length >> 28) == 0)
        {
            //length fits into 28 bit
            encodedLength = new byte[]{(byte) 0x60,
                                       (byte)  ((length >> 21)         | 0x80),
                                       (byte) (((length >> 14) & 0x7F) | 0x80),
                                       (byte) (((length >>  7) & 0x7F) | 0x80),
                                       (byte)  (length         & 0x7F)};
        }
        else
        {
            //length fits into 32 bit
            encodedLength = new byte[]{(byte) 0x60,
                                       (byte)  ((length >> 28)         | 0x80),
                                       (byte) (((length >> 21) & 0x7F) | 0x80),
                                       (byte) (((length >> 14) & 0x7F) | 0x80),
                                       (byte) (((length >>  7) & 0x7F) | 0x80),
                                       (byte)   (length        & 0x7F)};
        }

        byte[] completeContext = new byte[length + encodedLength.length];
        System.arraycopy(encodedLength, 0,
                         completeContext, 0,
                         encodedLength.length);
        System.arraycopy(mechOidArray, 0,
                         completeContext, encodedLength.length,
                         mechOidArray.length);
        System.arraycopy(out, 0,
                         completeContext, encodedLength.length + mechOidArray.length,
                         out.length);

        return completeContext;
    }

    public static byte[] encode(String username, char[] password, String target_name)
    {
        return encode(username, new String(password), target_name.getBytes());
    }

    public static InitialContextToken decode(byte[] gssToken)
    {
        if(gssToken[0] != 0x60)
        {
            // logger.error("GSSToken doesn't start with expected value '0x60'");
            return null;
        }

        //skip total size, the GSSToken already has the correct length

        //find first octet where the MSB is zero
        int index = 1;
        while(index < gssToken.length &&
              (gssToken[index] & 0x80) == 1)
        {
            ++index;
        }

        if(index == gssToken.length)
        {
            //end not found
            // logger.error("GSSToken doesn't contain valid length");
            return null;
        }

        byte[] mechOidArray = null;
        try
        {
            mechOidArray = mechOid.getDER();
        }
        catch(org.ietf.jgss.GSSException e)
        {
            // logger.error("Error retrieving mechOid DER: " + e);
            return null;
        }

        //skip last octet of length
        ++index;

        if((index + mechOidArray.length) >= gssToken.length)
        {
            // logger.error("GSSToken doesn't contain OID");
            return null;
        }

        for(int i = 0; i < mechOidArray.length; ++i)
        {
            if(mechOidArray[i] != gssToken[index + i])
            {
                // logger.error("GSSToken doesn't contain GSSUPMechOID");
                return null;
            }
        }

        //skip oid
        index += mechOidArray.length;

        byte[] icToken = new byte[gssToken.length - index];
        System.arraycopy(gssToken, index, icToken, 0, icToken.length);

        try
        {
            Any any =
            GSSUPProvider.codec.decode_value(
                icToken,
                InitialContextTokenHelper.type());
            return InitialContextTokenHelper.extract(any);
        }
        catch (Exception e)
        {
            // logger.error("Error decoding for GSSNameSpi: " + e);
        }
        logger.error("Bailout - GSSUP");
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
            out = GSSUPProvider.codec.encode_value( any );
        }
        catch (Exception e)
        {
            // logger.error("Error encoding for GSSNameSpi: " + e);
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
            out = GSSUPProvider.codec.encode_value( any );
        }
        catch (Exception e)
        {
            // logger.error("Error encoding for GSSNameSpi: " + e);
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
