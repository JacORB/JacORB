/***** Copyright (c) 2002 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class BAD_QOS extends org.omg.CORBA.SystemException {

    public BAD_QOS() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public BAD_QOS(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public BAD_QOS(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public BAD_QOS(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
