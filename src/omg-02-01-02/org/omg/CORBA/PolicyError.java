/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class PolicyError extends org.omg.CORBA.UserException {

    public short reason;

    public PolicyError() {
        super(PolicyErrorHelper.id());
    }

    public PolicyError(short reason) {
        super(PolicyErrorHelper.id());
        this.reason = reason;
    }

    public PolicyError(String reason_str, short reason) { // full constructor
        super(PolicyErrorHelper.id()+" "+reason_str);
        this.reason = reason;
    }
}
