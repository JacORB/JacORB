/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class INVALID_TRANSACTION extends org.omg.CORBA.SystemException {

    public INVALID_TRANSACTION() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public INVALID_TRANSACTION(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public INVALID_TRANSACTION(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public INVALID_TRANSACTION(String reason,
                int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
