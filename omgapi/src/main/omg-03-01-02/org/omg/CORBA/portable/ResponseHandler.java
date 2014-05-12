/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public interface ResponseHandler {

    public org.omg.CORBA.portable.OutputStream createReply();

    public org.omg.CORBA.portable.OutputStream createExceptionReply();

}
