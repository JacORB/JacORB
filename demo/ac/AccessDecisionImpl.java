package demo.ac;

import org.omg.CORBA.*;
import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

import jacorb.orb.domain.*;
import jacorb.util.Debug;

import java.util.*;
import java.lang.reflect.*;

import jacorb.security.level2.*;

/**
 * AccessDecisionImpl.java
 * For SERVER access control
 *
 * Created: Tue Jun 13 10:54:41 2000
 *
 * $Id$
 */

public class AccessDecisionImpl
  extends jacorb.orb.LocalityConstrainedObject
  implements AccessDecision
{
    private static final int RESOLVER_POLICY = 200;
    private static final String DEFAULT_RESOLVER = "demo.ac.DummyResolver";

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

        System.out.println("In AccessDecision");
 
        DomainManager[] dm_list = target._get_domain_managers();
        System.out.println("Got DMs");

        //find starting domain
        //(first not orb domain)
        Domain dm = null;        
        for (int i = 0; i < dm_list.length; i++)
        {
            Domain d = DomainHelper.narrow(dm_list[i]);

            System.out.println("Found DM >>" + d.name() + "<<");
 
            if (!d._is_a(ORBDomainHelper.id()))
            {
                dm = d;
                break;
            }
        }

        if ( dm == null )
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT,
                         "No non-orb-domain present" );
            return false;
        }
        
        String the_resolver = DEFAULT_RESOLVER;
        try
        {
            PropertyPolicy resolver_p = PropertyPolicyHelper.narrow
                (dm.get_domain_policy(RESOLVER_POLICY));

            the_resolver = resolver_p.getValueOfProperty("ResolverClass");
        }catch (org.omg.CORBA.INV_POLICY ip)
        {
            Debug.output(Debug.SECURITY | Debug.DEBUG1, ip);
        }

        System.out.println(">>>>>>>>>>>AcD will use " + the_resolver); 

        Class res = null;
        try
        {
            res = Class.forName(the_resolver);
        }catch (ClassNotFoundException cnf)
        {
            Debug.output(Debug.SECURITY | Debug.DEBUG1, cnf);

            try
            {
              res = Class.forName(DEFAULT_RESOLVER);  
            }catch (ClassNotFoundException cnfe)
            {            
                //not supposed to happend
                Debug.output(Debug.SECURITY | Debug.IMPORTANT, cnfe);
            }
        }

System.out.println(">>>>>>>>>>>AcD will fetch methods");         

        Method[] methods = res.getDeclaredMethods();
        Class[] classes = methods[0].getParameterTypes();

        for(int i = 0; i < classes.length; i++)
            System.out.println("Found class >>" + classes[i].getName() + "<<");
        

        String final_policy_val = "AccessDenied";
        try
        {
            Method resolve = res.getDeclaredMethod("resolve", 
                                           new Class[]{Domain.class});

            final_policy_val = (String) resolve.invoke(null, new java.lang.Object[]{dm});
        }catch (Exception e)
        {
            Debug.output(Debug.SECURITY | Debug.IMPORTANT, e);
        }
            
        if ("AccessAllowed".equals(final_policy_val))
            return true;
        else
            return false;       
    }  
} // AccessDecisionImpl
