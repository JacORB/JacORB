/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class PERSIST_STORE extends org.omg.CORBA.SystemException {

    public PERSIST_STORE() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public PERSIST_STORE(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public PERSIST_STORE(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public PERSIST_STORE(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
