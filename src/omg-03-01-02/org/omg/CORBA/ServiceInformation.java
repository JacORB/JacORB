package org.omg.CORBA;


public final class ServiceInformation implements org.omg.CORBA.portable.IDLEntity
{
   public int[] service_options;
   public org.omg.CORBA.ServiceDetail[] service_details;

   public ServiceInformation() {}

   public ServiceInformation
      (int[] service_options, org.omg.CORBA.ServiceDetail[] service_details)
   {
      this.service_options = service_options;
      this.service_details = service_details;
   }

}
