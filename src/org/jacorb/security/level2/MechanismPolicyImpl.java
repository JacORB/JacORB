/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 * MechanismPolicyImpl.java
 *
 *
 * Created: Tue Jun 13 11:37:29 2000
 *
 * $Id$
 */

public class MechanismPolicyImpl 
    extends org.omg.CORBA.LocalObject 
    implements MechanismPolicy
{

    private String[] mechanisms = null;

    public MechanismPolicyImpl(String[] mechanisms) 
    {        
        this.mechanisms = mechanisms;
    }

    /**
     *
     * @return <description>
     */
    public String[] mechanisms()
    {
        return mechanisms;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        return new MechanismPolicyImpl((String[]) mechanisms.clone());
    }

    /**
     *
     */
    public void destroy() 
    {
        mechanisms = null;
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        return SecMechanismsPolicy.value;
    }    
} // MechanismPolicyImpl






