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

import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
import org.omg.CORBA.*;
/**
 * DelegationDirectivePolicyImpl.java
 *
 *
 * Created: Tue Jun 13 17:02:48 2000
 *
 * $Id: DelegationDirectivePolicyImpl.java,v 1.8 2004/05/06 12:40:01 nicolas Exp $
 */

public class DelegationDirectivePolicyImpl 
    extends org.omg.CORBA.LocalObject 
    implements DelegationDirectivePolicy
{
  
    public DelegationDirectivePolicyImpl() 
    {
    
    }

    public DelegationDirective delegation_directive()
    {
        return null;
    }

    // implementation of org.omg.CORBA.PolicyOperations interface

    /**
     *
     * @return <description>
     */
    public Policy copy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return null;
    }

    /**
     *
     */
    public void destroy() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
    }

    /**
     *
     * @return <description>
     */
    public int policy_type() 
    {
        // TODO: implement this org.omg.CORBA.PolicyOperations method
        return -1;
    }
} // DelegationDirectivePolicyImpl






