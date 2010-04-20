/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class INV_POLICY extends org.omg.CORBA.SystemException {

    public INV_POLICY() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INV_POLICY(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public INV_POLICY(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INV_POLICY(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
