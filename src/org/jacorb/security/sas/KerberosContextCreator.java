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

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.util.Debug;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo; 

public class KerberosContextCreator implements ISASContextCreator
{

	public byte[] create(ClientRequestInfo ri) {
		byte[] contextToken = new byte[0];
		try {
			// get target
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
			Debug.output("Error creating Kerberos context: "+e);
		}
		return contextToken;
    }
}
