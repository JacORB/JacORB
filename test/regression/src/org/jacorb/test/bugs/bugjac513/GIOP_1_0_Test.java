package org.jacorb.test.bugs.bugjac513;

import org.apache.regexp.RE;

public class GIOP_1_0_Test extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "0";
    }

    protected void verifyPrintIOROutput(String result)
    {
        RE re = new RE("IIOP Version:\\s+1\\.0");
        assertTrue(re.match(result));

        re = new RE("Found \\d Tagged Components");
        assertFalse(re.match(result));

        assertTrue(result.indexOf("Unknown profile") < 0);
    }
}
