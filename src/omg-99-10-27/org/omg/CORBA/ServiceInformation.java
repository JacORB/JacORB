package org.omg.CORBA;


public final class ServiceInformation implements 
      org.omg.CORBA.portable.IDLEntity {

    public int[] service_options;
    public int[] service_details;

    public ServiceInformation() { 
    }

    public ServiceInformation (int[] service_options, int[] service_details) {
        this.service_options = service_options;
        this.service_details = service_details;
    }

}
