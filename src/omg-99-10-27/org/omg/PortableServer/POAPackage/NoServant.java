/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer.POAPackage;

public final class NoServant extends org.omg.CORBA.UserException {

    public NoServant() {
        super(NoServantHelper.id());
    }

    public NoServant(String reason) { // full constructor
        super(NoServantHelper.id()+" "+reason);
    }
}
