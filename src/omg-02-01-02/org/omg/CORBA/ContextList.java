/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class ContextList {

    abstract public int count();
    abstract public void add(String ctx);
    abstract public String item( int index) throws org.omg.CORBA.Bounds;
    abstract public void remove( int index) throws org.omg.CORBA.Bounds;
}
