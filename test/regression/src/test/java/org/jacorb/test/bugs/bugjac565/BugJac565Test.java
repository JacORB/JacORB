package org.jacorb.test.bugs.bugjac565;

import static org.junit.Assert.fail;
import org.jacorb.test.bugs.bugjac565.ModulatorPackage.Mode;
import org.jacorb.test.bugs.bugjac565.ModulatorPackage.States;
import org.junit.Test;

public class BugJac565Test 
{
    @Test
    public void testDefaultMethod ()
    {
        Mode mode = new Mode( );
        try
        {
            // trying to set default value for union 
            // from explicitly defined cases
            mode.__default( States.one );
        }
        catch( org.omg.CORBA.BAD_PARAM e)
        {
            // expected exception just return
            return;
        }
        fail("JAC#565: Expected exception org.omg.CORBA.BAD_PARAM from __default(value) method was not thrown");
    }
}
