/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;


public final class ServiceInformationHolder implements 
        org.omg.CORBA.portable.Streamable {

    public org.omg.CORBA.ServiceInformation value;

    public ServiceInformationHolder() {
    }

    public ServiceInformationHolder(org.omg.CORBA.ServiceInformation _value) {
        value = _value;
    }

    public void _read(org.omg.CORBA.portable.InputStream input) {
        value = org.omg.CORBA.ServiceInformationHelper.read(input);
    }

    public void _write(org.omg.CORBA.portable.OutputStream output) {
        org.omg.CORBA.ServiceInformationHelper.write(output, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CORBA.ServiceInformationHelper.type();
    }
}
