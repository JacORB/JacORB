package org.jacorb.test.bugs.bugjac670;

public class GreetingServiceServer
{
   public static void main( String[] args )
   {
      org.omg.CORBA.ORB orb =  org.omg.CORBA.ORB.init(args, null);
      try
      {

         org.omg.PortableServer.POA poa =
         org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

         GreetingServiceImpl servant = new GreetingServiceImpl();

         byte[] id = poa.activate_object( servant ) ;
         org.omg.CORBA.Object obj = poa.id_to_reference( id ) ;
         GreetingService greeting = GreetingServiceHelper.narrow(obj);

         poa.the_POAManager().activate();

         org.omg.CORBA.Object balancerobj = orb.resolve_initial_references("balancer");
         GSLoadBalancer balancer = GSLoadBalancerHelper.narrow(balancerobj);

         balancer.addGreetingService(greeting);
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      System.out.println("GreetingServiceServer Ready ...");

      orb.run();
   }
}