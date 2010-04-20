/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class INV_FLAG extends org.omg.CORBA.SystemException {

    public INV_FLAG() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INV_FLAG(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public INV_FLAG(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INV_FLAG(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
