/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class INITIALIZE extends org.omg.CORBA.SystemException {

    public INITIALIZE() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public INITIALIZE(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public INITIALIZE(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public INITIALIZE(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
