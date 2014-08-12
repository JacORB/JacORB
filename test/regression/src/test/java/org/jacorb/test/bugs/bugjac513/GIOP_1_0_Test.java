package org.jacorb.test.bugs.bugjac513;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.regex.Pattern;
import org.jacorb.test.harness.TestUtils;

public class GIOP_1_0_Test extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "0";
    }

    protected void verifyPrintIOROutput(String result)
    {
        Pattern re = Pattern.compile("IIOP Version:\\s+1\\.0");
        assertTrue(TestUtils.patternMatcher(re, result) != 0);

        re = Pattern.compile("Found \\d Tagged Components");
        assertFalse(TestUtils.patternMatcher(re, result) != 0);

        assertTrue(result.indexOf("Unknown profile") < 0);
    }
}
