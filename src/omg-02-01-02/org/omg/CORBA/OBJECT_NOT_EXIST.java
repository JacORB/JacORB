/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class OBJECT_NOT_EXIST extends org.omg.CORBA.SystemException {

    public OBJECT_NOT_EXIST() {
        super(null, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public OBJECT_NOT_EXIST(int minor, CompletionStatus completed) {
        super(null, minor, completed);
    }

    public OBJECT_NOT_EXIST(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
    }

    public OBJECT_NOT_EXIST(String reason,
                  int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
