/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.CurrentPackage;

public final class NoContext extends org.omg.CORBA.UserException {

    public NoContext() {
        super(NoContextHelper.id());
    }

    public NoContext(String reason) { // full constructor
        super(NoContextHelper.id()+" "+reason);
    }
}
