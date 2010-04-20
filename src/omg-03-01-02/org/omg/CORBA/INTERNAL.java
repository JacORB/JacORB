/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class INTERNAL extends org.omg.CORBA.SystemException {

    public INTERNAL() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INTERNAL(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public INTERNAL(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public INTERNAL(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
