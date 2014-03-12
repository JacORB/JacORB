package org.jacorb.test.jaco;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import org.jacorb.test.common.TestUtils;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */

public class JacoTest
{
    @Test
    public void testStartJaco() throws Exception
    {
        File jaco = new File(TestUtils.jacorbHome(), "bin/jaco");
        String command = jaco + " " + JacoTestServer.class.getName();

        Process process = Runtime.getRuntime().exec(command,
                new String[] {
                    "CLASSPATH=" + TestUtils.testHome() + "/target/test-classes",
                    "JRE_HOME=" + System.getProperty("java.home")
        });

        try
        {
            InputStream in = process.getInputStream();
            BufferedInputStream bin = new BufferedInputStream(in);
            InputStreamReader reader = new InputStreamReader(bin);
            LineNumberReader lnr = new LineNumberReader(reader);

            String line = null;

            long maxWait = System.currentTimeMillis() + 10000;

            boolean seen = false;

            StringBuffer out = new StringBuffer();

            while( (!seen) &&  (line = lnr.readLine()) != null && System.currentTimeMillis() < maxWait)
            {
                if ("ORB: org.jacorb.orb.ORB".equals(line))
                {
                    seen = true;
                }
                TestUtils.getLogger().debug(line);
                out.append(line);
                out.append('\n');
            }

            if (!seen)
            {
                printErr(process);

            }

            assertTrue("couldn't start process. buffer: " + out, seen);

            in.close();
        }
        finally
        {
            process.destroy();
        }
    }

    private void printErr(Process process) throws Exception
    {
        InputStream in = process.getErrorStream();
        BufferedInputStream bin = new BufferedInputStream(in);
        InputStreamReader reader = new InputStreamReader(bin);
        LineNumberReader lnr = new LineNumberReader(reader);

        String line = null;
        StringBuffer buffer = new StringBuffer();
        while( (line = lnr.readLine()) != null )
        {
            buffer.append(line);
            buffer.append('\n');
        }

        System.err.println(buffer);

        fail(buffer.toString());
    }
}
