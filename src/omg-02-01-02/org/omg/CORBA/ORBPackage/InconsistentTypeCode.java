/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.ORBPackage;

public final class InconsistentTypeCode extends org.omg.CORBA.UserException {

    public InconsistentTypeCode() {
        super(InconsistentTypeCodeHelper.id());
    }

    public InconsistentTypeCode(String reason_str) { // full constructor
        super(InconsistentTypeCodeHelper.id()+" "+reason_str);
    }
}
