package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class Request {
   
	abstract public org.omg.CORBA.Object target();
	abstract public java.lang.String operation();
	abstract public org.omg.CORBA.NVList arguments();
	abstract public org.omg.CORBA.NamedValue result();
	abstract public org.omg.CORBA.Environment env();
	abstract public org.omg.CORBA.ExceptionList exceptions();
	abstract public org.omg.CORBA.ContextList contexts();

	abstract public void ctx(org.omg.CORBA.Context ctx);
	abstract public org.omg.CORBA.Context ctx();

	abstract public org.omg.CORBA.Any add_in_arg();
	abstract public org.omg.CORBA.Any add_named_in_arg( java.lang.String name );
	abstract public org.omg.CORBA.Any add_inout_arg();
	abstract public org.omg.CORBA.Any add_named_inout_arg( java.lang.String name );
	abstract public org.omg.CORBA.Any add_out_arg();
	abstract public org.omg.CORBA.Any add_named_out_arg( java.lang.String name );
	abstract public void set_return_type( org.omg.CORBA.TypeCode tc );
	abstract public org.omg.CORBA.Any return_value();

	abstract public void invoke();
	abstract public void send_oneway();
	abstract public void send_deferred();
	abstract public void get_response() throws org.omg.CORBA.INVALID_TRANSACTION;
	abstract public boolean poll_response(); 
} 


