/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class UNKNOWN extends org.omg.CORBA.SystemException {

    public UNKNOWN() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public UNKNOWN(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public UNKNOWN(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public UNKNOWN(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
