/***** Copyright (c) 2002 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class REBIND extends org.omg.CORBA.SystemException {

    public REBIND() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public REBIND(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public REBIND(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public REBIND(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
