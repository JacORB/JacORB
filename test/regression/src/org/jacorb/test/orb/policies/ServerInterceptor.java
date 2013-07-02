package org.jacorb.test.orb.policies;

import org.omg.PortableInterceptor.ForwardRequest;

public class ServerInterceptor
   extends org.omg.CORBA.LocalObject
   implements org.omg.PortableInterceptor.ServerRequestInterceptor
{
   static boolean forwardRequestThrown = false;

   public java.lang.String name()
   {
      return "";
   }

   public void destroy()
   {
   }

   public void receive_request_service_contexts(
      org.omg.PortableInterceptor.ServerRequestInfo ri )
      throws org.omg.PortableInterceptor.ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.RRSC && !forwardRequestThrown)
       {
           forwardRequestThrown = true;

           /**
            * Wait a while to cause the timeout to expire
            */
           try
           {
               Thread.sleep (150);
           }
           catch (InterruptedException ie)
           {
           }

           throw new ForwardRequest (TestConfig.fwd);
       }
   }

   public void receive_request( org.omg.PortableInterceptor.ServerRequestInfo ri )
      throws org.omg.PortableInterceptor.ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.REC_REQ && !forwardRequestThrown)
       {
           forwardRequestThrown = true;

           /**
            * Wait a while to cause the timeout to expire
            */
           try
           {
               Thread.sleep (150);
           }
           catch (InterruptedException ie)
           {
           }

           throw new ForwardRequest (TestConfig.fwd);

       }
   }

   public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
   {
   }

   public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
      throws org.omg.PortableInterceptor.ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.SEND_EX && !forwardRequestThrown)
       {
           forwardRequestThrown = true;

           /**
            * Wait a while to cause the timeout to expire
            */
           try
           {
               Thread.sleep (120);
           }
           catch (InterruptedException ie)
           {
           }

           throw new ForwardRequest (TestConfig.fwd);
       }
   }

   public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
      throws org.omg.PortableInterceptor.ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.SEND_OTHER)
       {
       }
   }
}
