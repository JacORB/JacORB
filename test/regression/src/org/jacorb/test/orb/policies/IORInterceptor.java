package org.jacorb.test.orb.policies;


public class IORInterceptor
   extends org.omg.CORBA.LocalObject
   implements org.omg.PortableInterceptor.IORInterceptor
{
   public String name()
   {
      return "IOR";
   }

   public void destroy()
   {
   }

   public void establish_components (org.omg.PortableInterceptor.IORInfo info)
   {
   }
}