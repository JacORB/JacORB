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
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.jacorb.orb.CDRInputStream;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class KerberosContext implements ISASContext
{
	/** the logger used by the naming service implementation */
	private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

	//private GSSManager gssManager = GSSManager.getInstance(); 
	private GSSContext validatedContext = null;

	public byte[] createContext(ClientRequestInfo ri) {
		byte[] contextToken = new byte[0];
		try {
			TaggedComponent tc = ri.get_effective_component(TAG_CSI_SEC_MECH_LIST.value);
			CDRInputStream is = new CDRInputStream( (org.omg.CORBA.ORB)null, tc.component_data);
			is.openEncapsulatedArray();
			CompoundSecMechList csmList = CompoundSecMechListHelper.read( is );
			byte[] target = csmList.mechanism_list[0].as_context_mech.target_name;
			
			Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
			GSSManager gssManager = GSSManager.getInstance();
			GSSName myPeer = gssManager.createName(target, null, krb5Oid);
			GSSContext myContext = gssManager.createContext(myPeer, krb5Oid, null, GSSContext.DEFAULT_LIFETIME);
			contextToken = myContext.initSecContext(contextToken, 0, contextToken.length);
		} catch (Exception e) {
			logger.error("Error creating Kerberos context: "+e);
		}
		return contextToken;
    }
    
	public String getCreatedPrincipal() {
		String principal = "";
		try {
			GSSManager gssManager = GSSManager.getInstance();
			Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
			GSSCredential myCred = gssManager.createCredential(null, GSSCredential.DEFAULT_LIFETIME, krb5Oid, GSSCredential.INITIATE_ONLY);
			principal = myCred.getName().toString();
		} catch (Exception e) {
			logger.error("Error getting created principal: "+e);
		}
		return principal;
	}

	public boolean validateContext(ServerRequestInfo ri, byte[] contextToken) {
		byte[] token = null;
		
		try {
			GSSManager gssManager = GSSManager.getInstance();
			validatedContext = gssManager.createContext((GSSCredential)null);
			token = validatedContext.acceptSecContext(contextToken, 0, contextToken.length);
		} catch (GSSException e) {
			logger.error("Error accepting Kerberos context: "+e);
		}
		if (token == null) {
			logger.warn("Could not accept token");
			return false;
		} 

		return true;
	}

	public String getValidatedPrincipal() {
		if (validatedContext == null) return null;
		try {
			return validatedContext.getSrcName().toString();
		} catch (GSSException e) {
			logger.error("Error getting name: "+e);
		}
		return null;
	}
}
