/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class BAD_CONTEXT extends org.omg.CORBA.SystemException {

  public BAD_CONTEXT() {
    super(null, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public BAD_CONTEXT(int minor, CompletionStatus completed) {
    super(null, minor, completed);
  }

  public BAD_CONTEXT(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public BAD_CONTEXT(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
