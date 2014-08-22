package org.jacorb.test.bugs.bugjac415;

import static org.junit.Assert.fail;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;

public class BugJac415Test extends ORBTestCase
{
    @Test
    public void testExceptionInPOAUtil() throws Exception
    {
        try
        {
            rootPOA.reference_to_servant(null);
            fail();
        }
        catch(BAD_PARAM e)
        {

        }
    }

}
