package org.jacorb.test.bugs.bugjac513;

import org.jacorb.test.common.PatternWrapper;

public class GIOP_1_0_Test extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "0";
    }

    protected void verifyPrintIOROutput(String result)
    {
        PatternWrapper re = PatternWrapper.init("IIOP Version:\\s+1\\.0");
        assertTrue(re.match(result) != 0);

        re = PatternWrapper.init ("Found \\d Tagged Components");
        assertFalse(re.match(result) != 0);

        assertTrue(result.indexOf("Unknown profile") < 0);
    }
}
