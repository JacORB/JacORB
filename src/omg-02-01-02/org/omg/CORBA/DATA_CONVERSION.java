/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public class DATA_CONVERSION extends org.omg.CORBA.SystemException {

  public DATA_CONVERSION() {
    super(null, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public DATA_CONVERSION(int minor, CompletionStatus completed) {
    super(null, minor, completed);
  }

  public DATA_CONVERSION(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_MAYBE);
  }

  public DATA_CONVERSION(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
