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

import org.omg.CORBA.*;
import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

import java.util.*;
import java.lang.reflect.*;


/**
 * Dummy AccessDecisionImpl.java
 *
 * Created: Tue Jun 13 10:54:41 2000
 *
 * $Id$
 */

public class AccessDecisionImpl
  extends org.omg.CORBA.LocalObject
  implements AccessDecision
{

    public AccessDecisionImpl()
    {
    }

    /**
     *
     * @param cred_list <description>
     * @param target <description>
     * @param operation_name <description>
     * @param target_interface_name <description>
     * @return <description>
     */
    public boolean access_allowed(Credentials[] cred_list, 
				  org.omg.CORBA.Object target, 
				  String operation_name, 
				  String target_interface_name)
    {    

        // do something to find out...
        
        return true;       
    }  

} // AccessDecisionImpl










