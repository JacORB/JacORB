package org.jacorb.test.bugs.bugjac663;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class BugJac663Test extends TestCase
{
    public static Test suite ()
    {
        TestSuite suite = new TestSuite (BugJac663Test.class);

        return suite;
    }

    protected void setUp () throws Exception
    {
    }

    protected void tearDown () throws Exception
    {
    }

   public void testJac663 () throws Exception
   {
      ORB orb = ORB.init(new String [0], null);

      org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");

      POA rootPOA = POAHelper.narrow(obj);

      rootPOA.the_POAManager().activate();

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
