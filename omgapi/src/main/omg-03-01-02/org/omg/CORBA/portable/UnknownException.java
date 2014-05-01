/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to
       duplicate and use this code are hereby granted provided that this
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public class UnknownException extends org.omg.CORBA.SystemException {

    public Throwable originalEx;
    public UnknownException(Throwable ex) {
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx = ex;
    }
    /****  added by resolution to Issue 3570  ****/
    UnknownException(Throwable orig, int minor_code,
                     org.omg.CORBA.CompletionStatus status) {
        super("", minor_code, status);
        originalEx = orig;
    }
    UnknownException(Throwable orig, String message) {
        super(message, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx = orig;
    }
    UnknownException(Throwable orig, String message, int minor_code,
                     org.omg.CORBA.CompletionStatus status) {
        super(message, minor_code, status);
        originalEx = orig;
    }
    /****  end of additions by resolution to Issue 3570  ****/
}
