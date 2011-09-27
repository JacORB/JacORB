package org.jacorb.test.bugs.bugjac670;


public class GSLoadBalancerImpl extends GSLoadBalancerPOA
{
   static GreetingService service = null;

   public void addGreetingService(GreetingService greetObj)
   {
      System.out.println("A new GreetingServer is registered.");
      service = greetObj;
   }

   public java.lang.String greeting(java.lang.String greetstr)
   {
      System.out.println("LoadBalancer greeting : " + greetstr);
      return "LoadBalancer greeting : " + greetstr;
   }
}