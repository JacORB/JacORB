/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class BAD_TYPECODE extends org.omg.CORBA.SystemException {

  public BAD_TYPECODE() {
    super(null, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public BAD_TYPECODE(int minor, CompletionStatus completed) {
    super(null, minor, completed);
  }

  public BAD_TYPECODE(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public BAD_TYPECODE(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
