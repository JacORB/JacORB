/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class BAD_INV_ORDER extends org.omg.CORBA.SystemException {

  public BAD_INV_ORDER() {
    super("", 0, CompletionStatus.COMPLETED_NO);
  }

  public BAD_INV_ORDER(int minor, CompletionStatus completed) {
    super("", minor, completed);
  }

  public BAD_INV_ORDER(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_NO);
  }

  public BAD_INV_ORDER(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
