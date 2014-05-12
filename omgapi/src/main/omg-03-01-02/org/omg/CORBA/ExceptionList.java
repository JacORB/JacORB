/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class ExceptionList {

    abstract public int count();

    abstract public void add(org.omg.CORBA.TypeCode exc);

    abstract public org.omg.CORBA.TypeCode item(int index) throws
                        org.omg.CORBA.Bounds;

    abstract public void remove(int index) throws org.omg.CORBA.Bounds;
}
