package org.jacorb.test.bugs.bug1012;

import org.jacorb.test.harness.TestUtils;

public final class DoorImpl extends DoorPOA
{

    @Override public void knock_knock(String name)
    {
        TestUtils.getLogger().debug("knock, knock, knock, " + name);
        //System.out.println("Who is there?");
    }

    @Override public void itsMe(String name)
    {
        TestUtils.getLogger().debug("\"It is " + name + "\"");
    }

    @Override public boolean canIComeIn()
    {
        TestUtils.getLogger().debug("Ok! Come in please!");
        return true;
    }

}
