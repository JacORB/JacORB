package org.jacorb.test.bugs.bugjac415;

import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;

public class BugJac415Test extends ORBTestCase
{
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
