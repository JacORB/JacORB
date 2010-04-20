/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class AdapterNonExistent extends org.omg.CORBA.UserException {

    public AdapterNonExistent() {
        super(AdapterNonExistentHelper.id());
    }

    public AdapterNonExistent(String reason) { // full constructor
        super(AdapterNonExistentHelper.id()+" "+reason);
    }
}
