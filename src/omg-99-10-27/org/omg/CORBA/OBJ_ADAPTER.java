/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class OBJ_ADAPTER extends org.omg.CORBA.SystemException {

    public OBJ_ADAPTER() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public OBJ_ADAPTER(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public OBJ_ADAPTER(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public OBJ_ADAPTER(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
