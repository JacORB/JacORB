/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class NO_MEMORY extends org.omg.CORBA.SystemException {

    public NO_MEMORY() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public NO_MEMORY(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public NO_MEMORY(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public NO_MEMORY(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
