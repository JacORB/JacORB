/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public interface InvokeHandler {

    public org.omg.CORBA.portable.OutputStream _invoke(String method,
                org.omg.CORBA.portable.InputStream is,
                ResponseHandler handler);
}
