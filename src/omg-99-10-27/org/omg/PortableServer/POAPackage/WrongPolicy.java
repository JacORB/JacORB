/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class WrongPolicy extends org.omg.CORBA.UserException {

    public WrongPolicy() {
        super(WrongPolicyHelper.id());
    }

    public WrongPolicy(String reason) { // full constructor
        super(WrongPolicyHelper.id()+" "+reason);
    }
}
