/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class BAD_OPERATION extends org.omg.CORBA.SystemException {

  public BAD_OPERATION() {
    super("", 0, CompletionStatus.COMPLETED_NO);
  }

  public BAD_OPERATION(int minor, CompletionStatus completed) {
    super("", minor, completed);
  }

  public BAD_OPERATION(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_NO);
  }

  public BAD_OPERATION(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
