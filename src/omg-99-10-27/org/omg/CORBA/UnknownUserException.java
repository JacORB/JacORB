/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

final public class UnknownUserException extends org.omg.CORBA.UserException {
    public org.omg.CORBA.Any except;

    public UnknownUserException() {
        super();
    }

    public UnknownUserException(org.omg.CORBA.Any a) {
        super();
        except = a;
    }
}

