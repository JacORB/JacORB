/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class OBJECT_NOT_EXIST extends org.omg.CORBA.SystemException {

    public OBJECT_NOT_EXIST() {
        super("", 0, CompletionStatus.COMPLETED_NO);
    }

    public OBJECT_NOT_EXIST(int minor, CompletionStatus completed) {
        super("", minor, completed);
    }

    public OBJECT_NOT_EXIST(String reason) {
        super(reason, 0, CompletionStatus.COMPLETED_NO);
    }

    public OBJECT_NOT_EXIST(String reason,
                  int minor, CompletionStatus completed) {
        super(reason, minor, completed);
    }

}
