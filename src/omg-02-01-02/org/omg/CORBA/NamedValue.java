/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class NamedValue {

    abstract public String name();
    abstract public org.omg.CORBA.Any value();
    abstract public int flags();
}
