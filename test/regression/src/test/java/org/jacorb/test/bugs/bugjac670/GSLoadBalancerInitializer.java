package org.jacorb.test.bugs.bugjac670;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

/**
 * <code>SInitializer</code> is basic initializer to register the interceptor.
 *
 * @author <a href="mailto:cj@prismtechnologies.com">Carol Jordon</a>
 * @version 1.0
 */
public class GSLoadBalancerInitializer
   extends org.omg.CORBA.LocalObject
   implements ORBInitializer
{
   /**
    * This method registers the interceptors.
    * @param info an <code>ORBInitInfo</code> value
    */
   public void post_init( ORBInitInfo info )
   {
      try
      {
         info.add_server_request_interceptor (new GSLoadBalancerInterceptor());
      }
      catch (DuplicateName e)
      {
         e.printStackTrace();
      }
   }

   /**
    * <code>pre_init</code> does nothing..
    *
    * @param info an <code>ORBInitInfo</code> value
    */
   public void pre_init(ORBInitInfo info)
   {
   }
}