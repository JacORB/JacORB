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
import org.jacorb.util.Debug;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class NullContext implements ISASContext
{
    private static Logger logger = Debug.getNamedLogger("jacorb.SAS");

    public String getMechOID() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#createContext(org.omg.PortableInterceptor.ClientRequestInfo)
     */
    public byte[] createClientContext(ClientRequestInfo ri, CompoundSecMechList csmList) {
        return new byte[0];
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#getCreatedPrincipal()
     */
    public String getClientPrincipal() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#validateContext(org.omg.PortableInterceptor.ServerRequestInfo, byte[])
     */
    public boolean validateContext(ServerRequestInfo ri, byte[] contextToken) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jacorb.security.sas.ISASContext#getValidatedPrincipal()
     */
    public String getValidatedPrincipal() {
        return "";
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
