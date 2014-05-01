/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class NVList {

    abstract public int count();
    abstract public org.omg.CORBA.NamedValue add(int flags);
    abstract public org.omg.CORBA.NamedValue add_item(String item_name,
                        int flags);
    abstract public org.omg.CORBA.NamedValue add_value(String name,
                        org.omg.CORBA.Any value, int flags);
    abstract public org.omg.CORBA.NamedValue item(int index) throws
                        org.omg.CORBA.Bounds;
    abstract public void remove(int index) throws org.omg.CORBA.Bounds;
}
