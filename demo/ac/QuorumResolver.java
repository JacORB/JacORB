package demo.ac;

import jacorb.orb.domain.*;
import java.util.*;
/**
 * QuorumResolver.java
 *
 *
 * Created: Thu Jul 13 10:31:23 2000
 *
 * @author Nicolas Noffke
 * @version
 */

public class QuorumResolver 
    extends ACConflictResolver 
{
    
    private static final int POLICY = 2;
    private static final int META_POLICY = 200;

    public static String resolve(Domain start)
    {

System.out.println("Starting at domain " + start.name());

        Vector domains = DomainUtil.getAllParents(start);

        //collect all policies        
        Vector policies = new Vector();
        for(int i = 0; i < domains.size(); i++)
        {
            Domain d = null;
            try
            {
                d = DomainHelper.narrow
                    ((org.omg.CORBA.Object) domains.elementAt(i));

                policies.addElement(d.get_domain_policy(POLICY));
            }catch(Exception e)
            {
                System.out.println(e);
                System.out.println("At domain " + d.name());
 
                //ignore, no policy present
            }
        }

        PropertyPolicy meta = PropertyPolicyHelper.narrow
            (start.get_domain_policy(META_POLICY));

        String meta_val = meta.getValueOfProperty("Operation");

        System.out.println("Meta val: " + meta_val);
 
        int pro = 0;
        int contra = 0;

        for(int i = 0; i < policies.size(); i++)
        {
            PropertyPolicy current = PropertyPolicyHelper.narrow
                ((org.omg.CORBA.Object) policies.elementAt(i));

            if ("AccessAllowed".equals
                (current.getValueOfProperty("AccessControl")))
                pro++;
            else
                contra++;
        }

        if (meta_val.equals("Majority"))
        {
            if (pro > contra)
                return "AccessAllowed";
            else
                return "AccessDenied";
        }
        else //Minority
        {
            if (pro < contra)
                return "AccessAllowed";
            else
                return "AccessDenied";
        }
    }
} // QuorumResolver
