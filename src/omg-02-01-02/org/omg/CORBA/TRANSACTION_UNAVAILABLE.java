/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class TRANSACTION_UNAVAILABLE extends org.omg.CORBA.SystemException {

    public TRANSACTION_UNAVAILABLE() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSACTION_UNAVAILABLE(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public TRANSACTION_UNAVAILABLE(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSACTION_UNAVAILABLE(String reason,
                    int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
