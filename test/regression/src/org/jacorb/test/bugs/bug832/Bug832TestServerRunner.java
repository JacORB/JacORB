package org.jacorb.test.bugs.bug832;

import org.jacorb.test.bugs.bugjac182.JAC182Helper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantRetentionPolicyValue;

public class Bug832TestServerRunner
{
   // Hack n a half.
   // Integer increment to differentiate test runs.
   private static int testRun = 1;


    static MyLocator locator;

    public static void main (String[] args) throws Exception
    {
        //init ORB
        ORB serverOrb = ORB.init( args, null );

        //init POA
        POA rootPoa =
            POAHelper.narrow( serverOrb.resolve_initial_references( "RootPOA" ));
        rootPoa.the_POAManager().activate();

        Policy[] policies = null;

        if (testRun == 1)
        {
           policies = new Policy[]
           {
              rootPoa.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
              rootPoa.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER)
           };
        }
        else if (testRun == 2)
        {
           policies = new Policy[]
           {
              rootPoa.create_id_uniqueness_policy (IdUniquenessPolicyValue.MULTIPLE_ID),
              rootPoa.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
              rootPoa.create_request_processing_policy(RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
           };
        }

        POA poa = rootPoa.create_POA("childPOA1", rootPoa.the_POAManager(), policies);

        String className = args[0];
        Class servantClass = Class.forName (className);
        Servant servant = ( Servant ) servantClass.newInstance();

        if (testRun == 1)
        {
           locator = new MyLocator();
           poa.set_servant_manager (locator);
        }
        else if (testRun == 2)
        {
           poa.set_servant (servant);
        }

        org.omg.CORBA.Object obj = poa.create_reference (JAC182Helper.id ());

        if (testRun == 1)
        {
           locator.setup (servant);
        }

        poa.the_POAManager().activate();

        testRun++;
        System.out.println ("SERVER IOR: " + serverOrb.object_to_string(obj));
        System.out.flush();

        // wait for requests
        serverOrb.run();
    }


    public static class MyLocator extends org.omg.CORBA.portable.ObjectImpl implements org.omg.PortableServer.ServantLocator
    {
       private org.omg.PortableServer.Servant instance;


       /**
        * <code>setup</code> is used to setup this locator.
        *
        * @param instance an <code>org.omg.PortableServer.Servant</code> value
        * @param obj an <code>org.omg.CORBA.Object</code> value
        */
       void setup (org.omg.PortableServer.Servant instance)
       {
          this.instance = instance;
       }

       public org.omg.PortableServer.Servant preinvoke(
                byte[] oid,
                org.omg.PortableServer.POA adapter,
                java.lang.String operation,
                org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie)

       {
          return instance;
       }

       public void postinvoke(
                byte[] oid,
                org.omg.PortableServer.POA adapter,
                java.lang.String operation,
                java.lang.Object the_cookie,
                org.omg.PortableServer.Servant the_servant)

       {
       }

       public String[] _ids()
       {
          return null;
       }
    }

}
