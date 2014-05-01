/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class Context {

    abstract public String context_name();
    abstract public org.omg.CORBA.Context parent();
    abstract public org.omg.CORBA.Context create_child(
                String child_context_name);
    abstract public void set_one_value(String prop_name,
                org.omg.CORBA.Any value);
    abstract public void set_values(org.omg.CORBA.NVList values);
    abstract public void delete_values(String prop_name);
    abstract public org.omg.CORBA.NVList get_values(String start_scope,
                int op_flags, String pattern);
}
