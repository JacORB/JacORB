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

import org.apache.avalon.framework.logger.Logger;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class GssUpContext implements ISASContext
{
    private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");
    private static String username = "";
    private static String password = "";
    private InitialContextToken initialContextToken = null;

    public static void setUsernamePassword(String username, String password) {
        GssUpContext.username = username;
        GssUpContext.password = password;
    }

    public String getMechOID() {
        return GSSUPMechOID.value.substring(4);
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#createContext(org.omg.PortableInterceptor.ClientRequestInfo)
     */
    public byte[] createClientContext(ClientRequestInfo ri, CompoundSecMechList csmList) {
        byte[] target = csmList.mechanism_list[0].as_context_mech.target_name;
        byte[] contextToken = GSSUPNameSpi.encode(username, password, target);
        initialContextToken = GSSUPNameSpi.decode(contextToken);
        return contextToken;
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#getCreatedPrincipal()
     */
    public String getClientPrincipal() {
        return username;
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#validateContext(org.omg.PortableInterceptor.ServerRequestInfo, byte[])
     */
    public boolean validateContext(ServerRequestInfo ri, byte[] contextToken) {
        initialContextToken = GSSUPNameSpi.decode(contextToken);
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
