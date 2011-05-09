package org.jacorb.test.bugs.bugjac663;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;


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

   public void testJac663 ()
   {
      ORB orb = null;
      POA rootPOA = null;

      try
      {
         orb = ORB.init(new String [0], null);

         org.omg.CORBA.Object obj
            = orb.resolve_initial_references("RootPOA");

         rootPOA = POAHelper.narrow(obj);

         rootPOA.the_POAManager().activate();

         TestThread thr = new TestThread (orb, rootPOA);

         thr.start();

         thr.join();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}

class TestThread extends Thread
{
   private ORB orb;
   private POA poa;

   public TestThread
   (
      org.omg.CORBA.ORB orb,
      org.omg.PortableServer.POA poa)
   {
      this.orb = orb;
      this.poa= poa;
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

      JAC663ServerImpl serverImpl = new JAC663ServerImpl (orb, poa);

      JAC663Server server = serverImpl._this(orb);

      server.send_message ("BOB");
   }
}
