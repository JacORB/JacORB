/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class FREE_MEM extends org.omg.CORBA.SystemException {

    public FREE_MEM() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public FREE_MEM(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public FREE_MEM(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public FREE_MEM(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
