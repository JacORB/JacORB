/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class TRANSIENT extends org.omg.CORBA.SystemException {

    public TRANSIENT() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSIENT(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public TRANSIENT(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public TRANSIENT(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
