/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class NO_RESOURCES extends org.omg.CORBA.SystemException {

    public NO_RESOURCES() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public NO_RESOURCES(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public NO_RESOURCES(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public NO_RESOURCES(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
