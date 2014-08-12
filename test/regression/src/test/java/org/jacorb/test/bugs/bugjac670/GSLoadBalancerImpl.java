package org.jacorb.test.bugs.bugjac670;

import org.jacorb.test.harness.TestUtils;

public class GSLoadBalancerImpl extends GSLoadBalancerPOA
{
   static GreetingService service = null;

   public void addGreetingService(GreetingService greetObj)
   {
      TestUtils.getLogger().debug("A new GreetingServer is registered.");
      service = greetObj;
   }

   public java.lang.String greeting(java.lang.String greetstr)
   {
      TestUtils.getLogger().debug("LoadBalancer greeting : " + greetstr);
      return "LoadBalancer greeting : " + greetstr;
   }
}
