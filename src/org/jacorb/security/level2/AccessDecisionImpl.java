package org.jacorb.security.level2;

import org.omg.CORBA.*;
import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

import org.jacorb.orb.domain.*;
import org.jacorb.util.Debug;

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






