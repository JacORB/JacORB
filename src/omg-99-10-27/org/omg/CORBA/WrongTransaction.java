/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class WrongTransaction extends org.omg.CORBA.UserException {

    public WrongTransaction() {
        super(WrongTransactionHelper.id());
    }

    public WrongTransaction(String reason) { // full constructor
        super(WrongTransactionHelper.id()+" "+reason);
    }
}
