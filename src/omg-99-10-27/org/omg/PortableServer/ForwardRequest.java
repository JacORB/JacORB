/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.PortableServer;

public final class ForwardRequest extends org.omg.CORBA.UserException {

    public org.omg.CORBA.Object forward_reference;

    public ForwardRequest() {
        super(ForwardRequestHelper.id());
    }

    public ForwardRequest(org.omg.CORBA.Object forward_reference) {
        super(ForwardRequestHelper.id());
        this.forward_reference = forward_reference;
    }

    public ForwardRequest(String reason,
                        org.omg.CORBA.Object forward_reference) { // full constructor
        super(ForwardRequestHelper.id()+" "+reason);
        this.forward_reference = forward_reference;
    }
}
