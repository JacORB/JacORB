/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class WrongAdapter extends org.omg.CORBA.UserException {

    public WrongAdapter() {
        super(WrongAdapterHelper.id());
    }

    public WrongAdapter(String reason) { // full constructor
        super(WrongAdapterHelper.id()+" "+reason);
    }
}
