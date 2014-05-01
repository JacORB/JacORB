/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class AdapterAlreadyExists extends org.omg.CORBA.UserException {

    public AdapterAlreadyExists() {
        super(AdapterAlreadyExistsHelper.id());
    }

    public AdapterAlreadyExists(String reason) { // full constructor
        super(AdapterAlreadyExistsHelper.id()+" "+reason);
    }
}
