/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

/**
*  @deprecated org.omg.CORBA.DynamicImplementation
*/
abstract public class DynamicImplementation 
            extends org.omg.CORBA.portable.ObjectImpl {

    /**
    *  @deprecated Deprecated by the Portable Object Adapter (POA).
    */
    public void invoke(org.omg.CORBA.ServerRequest request) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


}
