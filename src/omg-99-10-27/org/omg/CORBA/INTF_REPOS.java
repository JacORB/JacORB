/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class INTF_REPOS extends org.omg.CORBA.SystemException {

    public INTF_REPOS() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public INTF_REPOS(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public INTF_REPOS(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public INTF_REPOS(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
