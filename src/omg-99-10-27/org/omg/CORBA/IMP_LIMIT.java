/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class IMP_LIMIT extends org.omg.CORBA.SystemException {

    public IMP_LIMIT() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public IMP_LIMIT(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public IMP_LIMIT(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public IMP_LIMIT(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
