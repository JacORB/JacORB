package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class Context  
{
	abstract public java.lang.String context_name();
	abstract public org.omg.CORBA.Context parent();
	abstract public org.omg.CORBA.Context create_child(java.lang.String child_context_name); 
	abstract public void set_one_value(java.lang.String prop_name, org.omg.CORBA.Any prop_value);
	abstract public void set_values(org.omg.CORBA.NVList prop_values);
	abstract public void delete_values(java.lang.String prop_name);
	abstract public org.omg.CORBA.NVList get_values(java.lang.String start_scope, int op_flags, java.lang.String pattern);
}


