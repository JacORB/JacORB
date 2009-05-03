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

import org.jacorb.config.*;
import org.slf4j.Logger;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;

public class GssUpContext 
    implements ISASContext
{
    private Logger logger = null;
    private static String username = "";
    private static String password = "";
    protected InitialContextToken initialContextToken = null;

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger = 
            ((org.jacorb.config.Configuration)configuration).getLogger("jacorb.security.sas.GSSUP");
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
        byte[] contextToken = GSSUPNameSpi.encode(orb, codec, username, password, new byte[0]);
        initialContextToken = GSSUPNameSpi.decode(orb, codec, contextToken);
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
        initialContextToken = GSSUPNameSpi.decode(orb, codec, contextToken);
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
}
