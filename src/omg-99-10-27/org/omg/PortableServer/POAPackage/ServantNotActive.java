/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class ServantNotActive extends org.omg.CORBA.UserException {

    public ServantNotActive() {
        super(ServantNotActiveHelper.id());
    }

    public ServantNotActive(String reason) { // full constructor
        super(ServantNotActiveHelper.id()+" "+reason);
    }
}
