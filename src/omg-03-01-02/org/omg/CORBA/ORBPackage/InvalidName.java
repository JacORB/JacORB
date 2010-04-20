/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.ORBPackage;

public final class InvalidName extends org.omg.CORBA.UserException {

    public InvalidName() {
        super(InvalidNameHelper.id());
    }

    public InvalidName(String reason_str) { // full constructor
        super(InvalidNameHelper.id()+" "+reason_str);
    }
}
