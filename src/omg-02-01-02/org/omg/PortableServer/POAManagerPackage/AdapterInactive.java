/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAManagerPackage;

public final class AdapterInactive extends org.omg.CORBA.UserException {

    public AdapterInactive() {
        super(AdapterInactiveHelper.id());
    }

    public AdapterInactive(String reason) { // full constructor
        super(AdapterInactiveHelper.id()+" "+reason);
    }
}
