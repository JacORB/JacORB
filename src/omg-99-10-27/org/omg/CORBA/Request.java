/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

abstract public class Request {

    abstract public org.omg.CORBA.Object target();
    abstract public String operation();
    abstract public org.omg.CORBA.NVList arguments();
    abstract public org.omg.CORBA.NamedValue result();
    abstract public org.omg.CORBA.Environment env();
    abstract public org.omg.CORBA.ExceptionList exceptions();
    abstract public org.omg.CORBA.ContextList contexts();
    
    abstract public void ctx( org.omg.CORBA.Context ctx);
    abstract public org.omg.CORBA.Context ctx();

    abstract public org.omg.CORBA.Any add_in_arg();
    abstract public org.omg.CORBA.Any add_named_in_arg(String name);
    abstract public org.omg.CORBA.Any add_inout_arg();
    abstract public org.omg.CORBA.Any add_named_inout_arg(String name);
    abstract public org.omg.CORBA.Any add_out_arg();
    abstract public org.omg.CORBA.Any add_named_out_arg(String name);
    abstract public void set_return_type( org.omg.CORBA.TypeCode tc);
    abstract public org.omg.CORBA.Any return_value();

    abstract public void invoke();
    abstract public void send_oneway();
    abstract public void send_deferred();
    abstract public void get_response() throws
                org.omg.CORBA.WrongTransaction;
    abstract public boolean poll_response();
}
