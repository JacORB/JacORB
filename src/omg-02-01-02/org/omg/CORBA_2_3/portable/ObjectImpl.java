/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA_2_3.portable;

public abstract class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl {

    public String _get_codebase() {
        org.omg.CORBA.portable.Delegate delegate = _get_delegate();
        if (delegate instanceof org.omg.CORBA_2_3.portable.Delegate)
            return ((org.omg.CORBA_2_3.portable.Delegate)
                        delegate).get_codebase(this);
        return null;
    }
}
