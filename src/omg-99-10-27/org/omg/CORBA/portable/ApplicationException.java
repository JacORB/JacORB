/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public class ApplicationException extends Exception {

    private String _id;
    private org.omg.CORBA.portable.InputStream _is;

    public ApplicationException(String id,
                org.omg.CORBA.portable.InputStream is) {
        _id = id;
        _is = is;
    }

    public String getId() {
        return _id;
    }

    public InputStream getInputStream() {
        return _is;
    }

}
