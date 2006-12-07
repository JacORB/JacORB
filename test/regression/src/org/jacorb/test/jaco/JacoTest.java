package org.jacorb.test.jaco;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.jacorb.test.common.TestUtils;

import junit.framework.TestCase;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class JacoTest extends TestCase
{
    public void testStartJaco() throws Exception
    {
        File jaco = new File(TestUtils.jacorbHome(), "bin/jaco");
        String command = jaco + " " + JacoTestServer.class.getName();

        Process process = Runtime.getRuntime().exec(command, new String[] {"CLASSPATH=" + TestUtils.testHome() + "/classes"});

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
            TestUtils.log(line);
            out.append(line);
            out.append('\n');
        }

        assertTrue("couldn't start process. buffer: " + out, seen);

        in.close();
        process.destroy();
    }
}
