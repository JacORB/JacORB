/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class ObjectNotActive extends org.omg.CORBA.UserException {

    public ObjectNotActive() {
        super(ObjectNotActiveHelper.id());
    }

    public ObjectNotActive(String reason) { // full constructor
        super(ObjectNotActiveHelper.id()+" "+reason);
    }
}
