package org.jacorb.test.bugs.bugjac801;

import org.jacorb.test.harness.ORBTestCase;
import org.junit.Assert;
import org.junit.Test;

public class BugJac801Test extends ORBTestCase
{
    @Test
    public void testUnionToString()
    {
        A testUnion = new A();
        testUnion.toto (B.case1, true);

        Assert.assertTrue ("A.toString() doesn't contain case for B.case3 discriminator value",
                           testUnion.toString ().contains ("true"));

        testUnion.titi (B.case3, (short) 20);

        Assert.assertTrue ("A.toString() doesn't contain case for B.case3 discriminator value",
                           testUnion.toString ().contains ("20"));
    }


    @Test
    public void testSunokeSwitch()
    {
        SimpleSwitch testUnion = new SimpleSwitch();
        testUnion.value ("FooBar");

        Assert.assertTrue
        (
            "A.toString() doesn't contain FooBar & incorrectly has discriminator value",
            testUnion.toString ().contains ("FooBar") && (! testUnion.toString().contains ("true"))
        );

        testUnion.Nothing (9999);

        Assert.assertTrue
        (
            "A.toString() doesn't contain 9999 & incorrectly has discriminator value ",
            testUnion.toString ().contains ("9999") && (! testUnion.toString().contains ("false"))
        );
    }

    @Test
    public void testLongSwitchUnionToString()
    {
        LongSwitch testUnion = new LongSwitch();
        testUnion.value ("FooBar");

        Assert.assertTrue
        (
            "A.toString() doesn't contain FooBar & incorrectly has discriminator value",
            testUnion.toString ().contains ("FooBar") && (! testUnion.toString().contains ("1"))
        );

        testUnion.Nothing (9999);

        Assert.assertTrue
        (
            "A.toString() doesn't contain 9999 & incorrectly has discriminator value ",
            testUnion.toString ().contains ("9999") && (! testUnion.toString().contains ("2"))
        );
    }

    @Test
    public void testOptElapsedTimeOnItemsUnionToString()
    {
        OptElapsedTimeOnItems testUnion = new OptElapsedTimeOnItems();

        OptSArrayElapsedTimeOnPointElapsedTimeOnItemsMAX_EET_POINT p = new OptSArrayElapsedTimeOnPointElapsedTimeOnItemsMAX_EET_POINT ();
        p.value ("OptSArrayElapsedTimeOnPointElapsedTimeOnItemsMAX_EET_POINT");
        OptSArrayElapsedTimeOnFIRElapsedTimeOnItemsMAX_EET_FIR f = new OptSArrayElapsedTimeOnFIRElapsedTimeOnItemsMAX_EET_FIR ();
        f.value ((short)10);

        ElapsedTimeOnItems etoi = new ElapsedTimeOnItems (p, f);
        testUnion.value (etoi);

        Assert.assertTrue
        (
            "A.toString() doesn't contain FooBar & incorrectly has discriminator value",
            testUnion.toString ().contains ("org.jacorb.test.bugs.bugjac801.OptSArrayElapsedTimeOnPointElapsedTimeOnItemsMAX_EET_POINT") && (! testUnion.toString().contains ("true"))
        );

        testUnion.Nothing (false);

        Assert.assertTrue
        (
            "A.toString() doesn't contain false",
            testUnion.toString ().contains ("false")
        );
    }
}
