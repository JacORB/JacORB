/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class OBJ_ADAPTER extends org.omg.CORBA.SystemException {

    public OBJ_ADAPTER() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public OBJ_ADAPTER(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public OBJ_ADAPTER(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public OBJ_ADAPTER(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
