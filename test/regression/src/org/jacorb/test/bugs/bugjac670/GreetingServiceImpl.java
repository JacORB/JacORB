package org.jacorb.test.bugs.bugjac670;

public class GreetingServiceImpl extends GreetingServicePOA
{
   public String greeting(String greetstr)
   {
      System.out.println("GreetingService greeting : " + greetstr);
      return "GreetingService greeting : " + greetstr;
   }
}