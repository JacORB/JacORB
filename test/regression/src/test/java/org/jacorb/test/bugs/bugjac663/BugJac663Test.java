package org.jacorb.test.bugs.bugjac663;

import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.ORB;


public class BugJac663Test extends ORBTestCase
{
    @Test
    public void testJac663 () throws Exception
   {
      TestThread thr = new TestThread (orb);

      thr.start();

      thr.join();

      orb.shutdown (true);
   }
}

class TestThread extends Thread
{
   private ORB orb;

   public TestThread (org.omg.CORBA.ORB orb)
   {
      this.orb = orb;
   }

   public void run()
   {
      try
      {
         sleep(2000);
      }
      catch (InterruptedException ie)
      {
      }

      JAC663ServerImpl serverImpl = new JAC663ServerImpl (orb);

      JAC663Server server = serverImpl._this(orb);

      server.send_message ("BOB");
   }
}
