/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class ObjectAlreadyActive extends org.omg.CORBA.UserException {

    public ObjectAlreadyActive() {
        super(ObjectAlreadyActiveHelper.id());
    }

    public ObjectAlreadyActive(String reason) { // full constructor
        super(ObjectAlreadyActiveHelper.id()+" "+reason);
    }
}
