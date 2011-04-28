package org.jacorb.test.bugs.bugjac801;

import junit.framework.Assert;

import org.jacorb.test.common.ORBTestCase;

public class BugJac801Test extends ORBTestCase
{
    public void testUnionToString()
    {
        org.jacorb.test.bugs.bugjac801.A testUnion = new org.jacorb.test.bugs.bugjac801.A();
        testUnion.toto (org.jacorb.test.bugs.bugjac801.B.case1, true);
        
        Assert.assertTrue ("A.toString() doesn't contain case for B.case3 discriminator value", 
                           testUnion.toString ().contains ("true"));

        testUnion.titi (org.jacorb.test.bugs.bugjac801.B.case3, (short) 20);
        
        Assert.assertTrue ("A.toString() doesn't contain case for B.case3 discriminator value", 
                           testUnion.toString ().contains ("20"));
    }
}
