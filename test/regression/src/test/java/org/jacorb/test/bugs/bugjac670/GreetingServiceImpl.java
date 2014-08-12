package org.jacorb.test.bugs.bugjac670;

import org.jacorb.test.harness.TestUtils;

public class GreetingServiceImpl extends GreetingServicePOA
{
   public String greeting(String greetstr)
   {
      TestUtils.getLogger().debug("GreetingService greeting : " + greetstr);
      return "GreetingService greeting : " + greetstr;
   }
}
