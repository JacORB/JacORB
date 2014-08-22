/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class InvalidPolicy extends org.omg.CORBA.UserException {

    public short index;

    public InvalidPolicy() {
        super(InvalidPolicyHelper.id());
    }

    public InvalidPolicy(short index) {
        super(InvalidPolicyHelper.id());
        this.index = index;
    }

    public InvalidPolicy(String reason, short index) { // full constructor
        super(InvalidPolicyHelper.id()+" "+reason);
        this.index = index;
    }
}
