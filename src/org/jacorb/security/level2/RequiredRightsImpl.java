/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
/**
 * RequiredRightsImpl.java
 *
 *
 * Created: Tue Jun 13 10:47:07 2000
 *
 * $Id$
 */

public class RequiredRightsImpl 
    extends org.omg.CORBA.LocalObject
    implements RequiredRights
{
  
    public RequiredRightsImpl() 
    {    
    }

    /**
     *
     * @param obj <description>
     * @param operation_name <description>
     * @param interface_name <description>
     * @param rights <description>
     * @param rights_combinator <description>
     */
    public void get_required_rights(org.omg.CORBA.Object obj, 
                                    String operation_name, 
                                    String interface_name, 
                                    RightsListHolder rights, 
                                    RightsCombinatorHolder rights_combinator) 
    {
        // TODO: implement this RequiredRightsOperations method
    }

    /**
     *
     * @param operation_name <description>
     * @param interface_name <description>
     * @param rights <description>
     * @param rights_combinator <description>
     */
    public void set_required_rights(String operation_name, 
                                    String interface_name, 
                                    Right[] rights, 
                                    RightsCombinator rights_combinator) 
    {
        // TODO: implement this RequiredRightsOperations method
    }

} // RequiredRightsImpl






