/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class TRANSACTION_REQUIRED extends org.omg.CORBA.SystemException {

    public TRANSACTION_REQUIRED() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSACTION_REQUIRED(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public TRANSACTION_REQUIRED(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSACTION_REQUIRED(String reason,
                    int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
