/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public final class CODESET_INCOMPATIBLE extends org.omg.CORBA.SystemException {

  public CODESET_INCOMPATIBLE() {
    super("", 0, CompletionStatus.COMPLETED_NO);
  }

  public CODESET_INCOMPATIBLE(int minor, CompletionStatus completed) {
    super("", minor, completed);
  }

  public CODESET_INCOMPATIBLE(String reason) {
    super(reason, 0, CompletionStatus.COMPLETED_NO);
  }

  public CODESET_INCOMPATIBLE(String reason, int minor, CompletionStatus completed) {
    super(reason, minor, completed);
  }

}
