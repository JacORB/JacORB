/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

    public interface OperationDefOperations extends
                    org.omg.CORBA.ContainedOperations {

    public org.omg.CORBA.TypeCode result();

    public org.omg.CORBA.IDLType result_def();
    public void result_def(org.omg.CORBA.IDLType result_def);

    public org.omg.CORBA.ParameterDescription[] params();
    public void params(org.omg.CORBA.ParameterDescription[] params);

    public org.omg.CORBA.OperationMode mode();
    public void mode(org.omg.CORBA.OperationMode mode);

    public java.lang.String[] contexts();
    public void contexts(java.lang.String[] contexts);

    public org.omg.CORBA.ExceptionDef[] exceptions();
    public void exceptions(org.omg.CORBA.ExceptionDef[] exceptions);

}
