package demo.ac;

import jacorb.orb.domain.*;
import java.util.*;

/**
 * BooleanConflictResolver.java
 *
 * Created: Thu Jul 13 10:31:23 2000
 *
 * @author Nicolas Noffke
 * @version
 */

public class BooleanConflictResolver 
    extends ACConflictResolver 
{    
    private static final int POLICY = 2;
    private static final int META_POLICY = 200;

    public static String resolve(Domain start)
    {
        Vector domains = DomainUtil.getAllParents(start);

        //collect all policies        
        Vector policies = new Vector();
        for(int i = 0; i < domains.size(); i++)
        {
            try
            {
                Domain d = DomainHelper.narrow
                    ((org.omg.CORBA.Object) domains.elementAt(i));

                policies.addElement(d.get_domain_policy(POLICY));
            }
            catch(org.omg.CORBA.INV_POLICY e)
            {
                //ignore, not policy present
            }
        }

        PropertyPolicy meta = 
            PropertyPolicyHelper.narrow(start.get_domain_policy(META_POLICY));

        String meta_val = meta.getValueOfProperty("Operation");

        System.out.println("Meta val: " + meta_val);
 
        String final_policy_val = null;
        for(int i = 0; i < policies.size(); i++)
        {
            PropertyPolicy current = PropertyPolicyHelper.narrow
                ((org.omg.CORBA.Object) policies.elementAt(i));

            if (meta_val.equals("AND"))
            {
                if ("AccessDenied".equals
                    (current.getValueOfProperty("AccessControl")))
                {
                    final_policy_val = "AccessDenied";
                    break;
                }
            }
            else //meta_val.equals("OR")
            {
                if ("AccessAllowed".equals
                    (current.getValueOfProperty("AccessControl")))
                {
                    final_policy_val = "AccessAllowed";
                    break;
                }
            }  
        }

        return final_policy_val;
    }
} // BooleanConflictResolver
