/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class ServantAlreadyActive extends org.omg.CORBA.UserException {

    public ServantAlreadyActive() {
        super(ServantAlreadyActiveHelper.id());
    }

    public ServantAlreadyActive(String reason) { // full constructor
        super(ServantAlreadyActiveHelper.id()+" "+reason);
    }
}
