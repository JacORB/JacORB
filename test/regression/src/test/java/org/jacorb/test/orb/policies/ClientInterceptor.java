package org.jacorb.test.orb.policies;


import org.jacorb.test.ComplexTimingServer;
import org.jacorb.test.ComplexTimingServerHelper;
import org.omg.PortableInterceptor.ForwardRequest;

public class ClientInterceptor
   extends org.omg.CORBA.LocalObject
   implements org.omg.PortableInterceptor.ClientRequestInterceptor
{
   static boolean forwardRequestThrown = false;
   static boolean forwardCallMade = false;

   public String name()
   {
      return "";
   }

   public void destroy()
   {
   }

   public void send_request( org.omg.PortableInterceptor.ClientRequestInfo ri )
      throws ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.SEND_REQ && !forwardRequestThrown)
       {
           forwardRequestThrown = true;

           /**
            * Wait a while to cause the timeout to expire
            */
           try
           {
               Thread.sleep (200);
           }
           catch (InterruptedException ie)
           {
           }

           throw new ForwardRequest (TestConfig.fwd);
       }
       else if (TestConfig.fwdReqPoint == TestConfig.CALL_AT_SEND_REQ && !forwardCallMade)
       {
          forwardCallMade = true;

          ComplexTimingServer server = ComplexTimingServerHelper.narrow (TestConfig.fwd);

          server.operation (432, 50);
       }

   }

   public void send_poll( org.omg.PortableInterceptor.ClientRequestInfo ri )
   {
   }

   public void receive_reply (org.omg.PortableInterceptor.ClientRequestInfo ri )
   {
   }

   public void receive_other( org.omg.PortableInterceptor.ClientRequestInfo ri )
      throws ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.REC_OTHER && !forwardRequestThrown )
       {
           forwardRequestThrown = true;

           /* Just throw the ForwardRequest as the sleep will have taken place on
            * the server side
            */
           throw new ForwardRequest (TestConfig.fwd);
       }
   }

   public void receive_exception( org.omg.PortableInterceptor.ClientRequestInfo ri )
      throws ForwardRequest
   {
       if (TestConfig.fwdReqPoint == TestConfig.REC_EX && !forwardRequestThrown)
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
       else if (TestConfig.fwdReqPoint == TestConfig.CALL_AT_REC_EX && !forwardCallMade)
       {
          forwardCallMade = true;

          ComplexTimingServer server = ComplexTimingServerHelper.narrow (TestConfig.fwd);

          server.operation (432, 100);
       }
   }
}
