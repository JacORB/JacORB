/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.security.level2;

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;
import org.omg.CORBA.*;
/**
 * InvocationCredentialsPolicyImpl
 *
 *
 * Created: Tue Jun 13 16:55:36 2000
 *
 * $Id$
 */

public class InvocationCredentialsPolicyImpl
    extends org.omg.CORBA.LocalObject  
    implements InvocationCredentialsPolicy
{
    private Credentials[] creds = null;

    public InvocationCredentialsPolicyImpl(Credentials[] creds) 
    {
        this.creds = creds;
    }

    public Credentials[] creds()
    {
        return creds;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        Credentials[] new_creds = new Credentials[creds.length];
        for(int i = 0; i < new_creds.length; i++)
            new_creds[i] = creds[i].copy();
        
        return new InvocationCredentialsPolicyImpl(new_creds);
    }

    /**
     *
     */
    public void destroy() 
    {
        creds = null;
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        return SecInvocationCredentialsPolicy.value;
    }
} // InvocationCredentialsPolicyImpl






