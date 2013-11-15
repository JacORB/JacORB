package org.jacorb.test.bugs.bugjac670;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

public class GSLoadBalancerInterceptor
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{

   public void receive_request_service_contexts(ServerRequestInfo ri)
      throws ForwardRequest
   {
      try
      {
         if(GSLoadBalancerImpl.service != null)
         {
            if(GSLoadBalancerImpl.service._non_existent())
            {
               GSLoadBalancerImpl.service = null;
            }
            else
            {
               throw new ForwardRequest(GSLoadBalancerImpl.service);
            }
         }
      }
      catch(ForwardRequest fr)
      {
         throw fr;
      }
      catch(Exception e)
      {
         GSLoadBalancerImpl.service = null;
      }
   }

   public void receive_request(ServerRequestInfo ri)
      throws ForwardRequest
   {
      // nothing
   }

   public void send_reply(ServerRequestInfo ri)
   {
      // nothing
   }

   public void send_exception(ServerRequestInfo ri)
      throws ForwardRequest
   {
      // nothing
   }

   public void send_other(ServerRequestInfo ri)
      throws ForwardRequest
   {
      // nothing
   }

   public String name()
   {
      return "GSLoadBalancerInterceptor";
   }

   public void destroy()
   {
      // nothing
   }
}