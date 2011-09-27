package org.jacorb.test.orb.policies;

import org.omg.CORBA.INTERNAL;


/**
 * An ORB initializer class.
 */
public class ServerPIInitializer
   extends org.omg.CORBA.LocalObject
   implements org.omg.PortableInterceptor.ORBInitializer
{
   /**
    * Called before init of the actual ORB.
    *
    * @param info The ORB init info.
    */
   public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info)
   {
      try
      {
         info.add_server_request_interceptor (new ServerInterceptor());
         info.add_ior_interceptor( new IORInterceptor() );
      }
      catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
      {
         throw new INTERNAL ("unexpected exception received: " + ex);
      }
   }

   /**
    * Called after init of the actual ORB.
    *
    * @param info The ORB init info.
    */
   public void post_init (org.omg.PortableInterceptor.ORBInitInfo info)
   {
   }
}