/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class Bounds extends org.omg.CORBA.UserException {

    public Bounds() {
        super(BoundsHelper.id());
    }

    public Bounds(String reason_str) { // full constructor
        super(BoundsHelper.id()+" "+reason_str);
    }
}
