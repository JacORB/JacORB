package org.jacorb.test.bugs.bug1014;

import org.omg.CORBA.NO_PERMISSION;

public final class Servant extends DeniedServicePOA {
  private int count = 0;

  @Override
  public synchronized void resetWhenReach(int limit) {
    if (++count < limit) {
      throw new NO_PERMISSION();
    }
    count = 0;
  }
}
