/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class INVALID_TRANSACTION extends org.omg.CORBA.SystemException {

    public INVALID_TRANSACTION() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INVALID_TRANSACTION(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public INVALID_TRANSACTION(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INVALID_TRANSACTION(String reason,
                int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
