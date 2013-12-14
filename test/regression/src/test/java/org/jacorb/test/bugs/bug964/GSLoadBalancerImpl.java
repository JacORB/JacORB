package org.jacorb.test.bugs.bug964;

import org.jacorb.test.bugs.bugjac670.GSLoadBalancerPOA;
import org.jacorb.test.bugs.bugjac670.GreetingService;


public class GSLoadBalancerImpl extends GSLoadBalancerPOA
{
    static String ID = "GSLoadBalancerImpl";

    public void addGreetingService(GreetingService greetObj)
    {
    }

    public java.lang.String greeting(java.lang.String greetstr)
    {
        return ID + " greeting : " + greetstr;
    }
}
