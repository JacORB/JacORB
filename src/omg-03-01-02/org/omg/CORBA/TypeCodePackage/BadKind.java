/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.TypeCodePackage;

public final class BadKind extends org.omg.CORBA.UserException {

    public BadKind() {
        super(BadKindHelper.id());
    }

    public BadKind(String reason) { // full constructor
        super(BadKindHelper.id()+" "+reason);
    }
}
