package org.jacorb.test.bugs.bugjac720;

import org.jacorb.test.common.ORBTestCase;

public class BugJac720Test extends ORBTestCase
{
    public void testUnionDefaultCase()
    {
        DefaultMultipleValuesUnion testUnion = new DefaultMultipleValuesUnion();
        try
        {
            testUnion.defaultCase(Color.Blue, 2);
            testUnion.defaultCase(Color.Green, 2);
        }
        catch (org.omg.CORBA.BAD_OPERATION e)
        {
            fail ("Incorrect values check for default case - operations are legal");
        }
        
        try
        {
            testUnion.defaultCase(Color.Red, 1);
            fail ("Incorrect values check for default case - Color.Red already used in excplicitly case. org.omg.CORBA.BAD_OPERATION is expected");
        }
        catch (org.omg.CORBA.BAD_OPERATION e)
        {
            // expected exception - do nothing
        }

    }
}
