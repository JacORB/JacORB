/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class UserException extends java.lang.Exception 
        implements org.omg.CORBA.portable.IDLEntity {

    public UserException () {
    super();
}

public UserException (java.lang.String value) {

    super(value);
}

}
