/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class SystemException extends java.lang.RuntimeException {

    public int minor;
    public CompletionStatus completed;

    protected SystemException(String reason, int minor,
                    CompletionStatus completed) {
        super(reason);
        this.minor = minor;
        this.completed = completed;
    }
}
