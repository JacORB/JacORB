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
    extends org.jacorb.orb.LocalityConstrainedObject
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






