/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class FREE_MEM extends org.omg.CORBA.SystemException {

    public FREE_MEM() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public FREE_MEM(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public FREE_MEM(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public FREE_MEM(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
