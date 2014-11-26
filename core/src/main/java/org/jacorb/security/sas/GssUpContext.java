package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2014 Gerald Brose / The JacORB Team.
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

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.GSSUP.InitialContextTokenHelper;
import org.omg.IOP.Codec;
import org.slf4j.Logger;

public class GssUpContext
    implements ISASContext
{
    private Logger logger = null;
    private static String username = "";
    private static String password = "";
    protected InitialContextToken initialContextToken = null;
    private static Oid mechOid;

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

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger =
            configuration.getLogger("org.jacorb.security.sas.GSSUP.log.verbosity");
    }

    public static void setUsernamePassword(String username, String password) {
        GssUpContext.username = username;
        GssUpContext.password = password;
    }

    public String getMechOID()
    {
        return GSSUPMechOID.value.substring(4);
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#createContext(org.omg.PortableInterceptor.ClientRequestInfo)
     */
    public byte[] createClientContext(ORB orb, Codec codec, CompoundSecMechList csmList)
    {
        byte[] contextToken;
        if ((csmList == null) || (csmList.mechanism_list == null) || (csmList.mechanism_list.length == 0))
        {
           contextToken = GssUpContext.encode(orb, codec, username, password, new byte[0]);
        }
        else
        {
           // XXX: not sure how do we select mechanism so let's try to take target_name from the first one
           contextToken = GssUpContext.encode(orb, codec, username, password,
                                              csmList.mechanism_list[0].as_context_mech.target_name);
        }



        initialContextToken = GssUpContext.decode(orb, codec, contextToken);
        return contextToken;
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#getCreatedPrincipal()
     */
    public String getClientPrincipal()
    {
        return username;
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#validateContext(org.omg.PortableInterceptor.ServerRequestInfo, byte[])
     */
    public boolean validateContext(ORB orb, Codec codec, byte[] contextToken)
    {
        initialContextToken = GssUpContext.decode(orb, codec, contextToken);
        return (initialContextToken != null);
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#getValidatedPrincipal()
     */
    public String getValidatedPrincipal() {
        if (initialContextToken == null) return null;
        return new String(initialContextToken.username);
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#initClient()
     */
    public void initClient() {
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#initTarget()
     */
    public void initTarget() {
    }

    public static byte[] encode(ORB orb, Codec codec, String username, String password, byte[] target_name)
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
        Any any = orb.create_any();
        InitialContextTokenHelper.insert( any, subject );
        try
        {
            out = codec.encode_value( any );
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

    public static byte[] encode(ORB orb, Codec codec, String username, char[] password, String target_name)
    {
        return encode(orb, codec, username, new String(password), target_name.getBytes());
    }

    public static InitialContextToken decode(ORB orb, Codec codec, byte[] gssToken)
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
            codec.decode_value(
                icToken,
                InitialContextTokenHelper.type());
            return InitialContextTokenHelper.extract(any);
        }
        catch (Exception e)
        {
            // logger.error("Error decoding for GSSNameSpi: " + e);
        }
        //logger.error("Bailout - GSSUP");
        return null;
    }
}
