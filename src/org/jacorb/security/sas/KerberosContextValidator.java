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
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.jacorb.util.Debug;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class KerberosContextValidator implements ISASContextValidator
{
	private GSSManager manager = GSSManager.getInstance(); 
	private GSSContext context = null;

    public boolean validate(ServerRequestInfo ri, byte[] contextToken) {
		byte[] token = null;
		
		try {
			context = manager.createContext((GSSCredential)null);
			token = context.acceptSecContext(contextToken, 0, contextToken.length);
		} catch (GSSException e) {
			Debug.output("Error accepting Kerberos context: "+e);
		}
		if (token == null) {
			Debug.output("Could not accept token");
			return false;
		} 

        return true;
    }

    public String getPrincipalName() {
        if (context == null) return null;
		try {
        	return context.getSrcName().toString();
		} catch (GSSException e) {
			Debug.output("Error getting name: "+e);
		}
		return null;
    }
}
