/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class MARSHAL extends org.omg.CORBA.SystemException {

    public MARSHAL() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public MARSHAL(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public MARSHAL(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public MARSHAL(String reason, int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
