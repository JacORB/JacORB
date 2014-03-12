package org.jacorb.test.bugs.bug964;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class GSLoadBalancerServer
{
   public static void main( String[] args )
   {
      org.omg.CORBA.ORB orb =  org.omg.CORBA.ORB.init(args, null);
      String ior = "";

      try
      {
         POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

         GSLoadBalancerImpl servant = new GSLoadBalancerImpl();

         byte[] id = poa.activate_object(servant);
         org.omg.CORBA.Object obj = poa.id_to_reference( id ) ;

         ior = orb.object_to_string(obj);

         ((org.jacorb.orb.ORB)orb).addObjectKey("GSLBService", ior);

         poa.the_POAManager().activate();
      }
      catch ( Exception e )
      {
      }

      System.out.println ("SERVER IOR: " + ior);

      orb.run();
   }
}
