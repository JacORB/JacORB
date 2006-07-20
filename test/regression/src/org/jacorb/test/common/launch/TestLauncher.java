package org.jacorb.test.common.launch;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.io.*;
import java.util.*;
import java.text.*;

import org.jacorb.test.common.*;
import org.jacorb.util.ObjectUtil;

/**
 * This is the main class for launching regression tests.  It takes care
 * of launching the client side (on which the JUnit tests are actually executed)
 * against an appropriate JacORB version.  The client-side code itself
 * launches servers when appropriate.  This is the invocation syntax:
 *
 *   java [ -Djacorb.test.client.version=CLIENT_VERSION ]
 *        [ -Djacorb.test.server.version=SERVER_VERSION ]
 *        [ -Djacorb.test.coverage=on/off ]
 *        org.jacorb.test.common.launch.TestLauncher
 *        TESTSUITE
 *
 * Here, CLIENT_VERSION and SERVER_VERSION are ids of available JacORB
 * installations, as specified in the file
 * $JACORB_HOME/test/regression/test.properties.
 * The third optional property, jacorb.test.coverage, specifies whether
 * coverage information should be collected during this test run or not.
 * The final argument, after the name of the class itself, is the name
 * of the TESTSUITE to execute (e.g. org.jacorb.test.AllTest).
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class TestLauncher
{
    private static String[] args = null;
    private static PrintWriter outFile = null;
    private static boolean outputFileTestName;
    private static Date testDate = new java.util.Date();

    private static DateFormat idFormatter =
        new SimpleDateFormat ("yyyy-MM-dd.HH-mm-ss");

    private static DateFormat dateStringFormatter =
        new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss Z");
    private static String testId;
    private static boolean isSSL;
    private static boolean useIMR;

    private static class Listener extends Thread
    {
       private BufferedReader in = null;

        public Listener (InputStream in)
        {
            this.in = new BufferedReader (new InputStreamReader (in));
        }

        public void run()
        {
            try
            {
                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    System.out.println (line);
                    outFile.println (line);
                }
            }
            catch (Exception ex)
            {
                System.out.println ("exception reading from test client: "
                                    + ex.toString());
            }
        }
    }

    public static void printTestHeader (PrintWriter out)
    {
        out.println("-------------------------------------------------------------------------------");
        out.println();
        out.println("  JacORB Regression Test Report");
        out.println();
        out.println("  Suite:    " + getSuiteName() + " [" + (isSSL ? "" : "NO") + "SSL]");
        out.println("");
        out.println("  Date:     " + getTestDateString());
        out.println("  User:     " + getTestUser());
        out.println("  Platform: " + getTestPlatform());
        out.println();
        out.println("  Client Version:   " + getClientVersion());
        out.println("  Server Version:   " + getServerVersion());
        out.println("  Coverage:         " + (getCoverage() ? "yes" : "no"));
        out.println("  SSL:              " + (isSSL ? "yes" : "no"));
        out.println("  IMR:              " + (useIMR ? "yes" : "no"));
        out.println();
        out.println("-------------------------------------------------------------------------------");
    }

    public static void printTestHeader (PrintStream out)
    {
        PrintWriter x = new PrintWriter (out);
        printTestHeader(x);
        x.flush();
    }

    public static String getSuiteName()
    {
        return args[0];
    }

    public static Date getTestDate()
    {
        return testDate;
    }

    private static String newTestID()
    {
        return idFormatter.format (getTestDate());
    }

    public static String getTestDateString()
    {
        return dateStringFormatter.format (getTestDate());
    }

    public static boolean getCoverage()
    {
        String cs = System.getProperty("jacorb.test.coverage", "false");
        return TestUtils.isPropertyTrue(cs);
    }

    public static String getClientVersion()
    {
        return System.getProperty ("jacorb.test.client.version", "cvs");
    }

    public static String getServerVersion()
    {
        return System.getProperty ("jacorb.test.server.version", "cvs");
    }

    public static String getTestUser()
    {
        return System.getProperty ("user.name", "<unknown>");
    }

    public static String getTestPlatform()
    {
        return "java " + System.getProperty ("java.version")
             + " (" + System.getProperty ("java.vendor") + ") "
             + System.getProperty ("os.name") + " "
             + System.getProperty ("os.version") + " ("
             + System.getProperty ("os.arch") + ")";
    }

    public static String getOutFilename()
    {
        final String result;
        if ( ! outputFileTestName)
        {
            String dir = TestUtils.testHome() + "/output/" + testId;
            File dirF = new File (dir);
            // File class is platform independent, no need to convert
            if (!dirF.exists())
            {
                dirF.mkdir();
            }
            result = dir + "/report" + ".txt";
        }
        else
        {
            result = TestUtils.testHome() + "/output/TEST-" + args[0] + ".txt";
        }
        return result;
    }

    public static void main(String[] args) throws Exception
    {
        TestLauncher.args = args;

        isSSL = TestUtils.isPropertyTrue(System.getProperty("jacorb.test.ssl"));
        useIMR = TestUtils.isPropertyTrue(System.getProperty("jacorb.test.imr"));
        testId = System.getProperty("jacorb.test.id", newTestID());

        String filetestname = System.getProperty("jacorb.test.outputfile.testname", "false");
        outputFileTestName =
        (
            TestUtils.isPropertyTrue(filetestname)
        );

        outFile = new PrintWriter (new FileWriter (getOutFilename()));
        printTestHeader (outFile);
        printTestHeader (System.out);

        String mainClass = "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner";
        String testHome = TestUtils.osDependentPath(TestUtils.testHome());

        String classpath = testHome + "/classes/";
        classpath = TestUtils.pathAppend(classpath, testHome + "/lib/junit.jar");
        classpath = TestUtils.pathAppend(classpath, testHome + "/lib/easymock-1.1.jar");
        //Convert to platform dependent path
        classpath = TestUtils.osDependentPath(classpath);
        Properties props = new Properties();
        props.put("jacorb.test.id", testId);
        props.put("jacorb.test.coverage", getCoverage() ? "true" : "false");
        props.put("jacorb.test.client.version", getClientVersion());
        props.put("jacorb.test.server.version", getServerVersion());
        props.put("EXCLUDE_SERVICES", System.getProperty("EXCLUDE_SERVICES", "false"));
        props.put("jacorb.test.verbose", System.getProperty("jacorb.test.verbose"));
        props.put("jacorb.test.outputfile.testname",
                  System.getProperty("jacorb.test.outputfile.testname" , "false"));
        props.put("jacorb.test.ssl", System.getProperty("jacorb.test.ssl"));

        try
        {
            ObjectUtil.classForName("org.jacorb.test.orb.rmi.FixSunDelegateBug");
            props.put("javax.rmi.CORBA.UtilClass",
                      "org.jacorb.test.orb.rmi.FixSunDelegateBug");
            System.err.println("Using org.jacorb.test.orb.rmi.FixSunDelegateBug");
        }
        catch(NoClassDefFoundError e)
        {
        }
        catch(Exception e)
        {
        }

        props.put("jacorb.test.home", testHome);

        if (TestUtils.isWindows())
        {
            try
            {
                String systemRoot = TestUtils.systemRoot();
                props.put("jacorb.SystemRoot", systemRoot);
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
        }

        JacORBLauncher launcher = JacORBLauncher.getLauncher
        (
            getClientVersion(), getCoverage()
        );

        if (getCoverage())
        {
            String coveragePath = new String
            (
                launcher.getJacorbHome() +
                "/test/regression/output/" +
                (outputFileTestName == true ? "" : testId) +
                "/coverage-client.ec"
            );
            coveragePath = TestUtils.osDependentPath(coveragePath);
            props.put ("emma.coverage.out.file", coveragePath);
        }

        Process process = launcher.launch(classpath, props, mainClass, args);
        Listener outL = new Listener(process.getInputStream());
        outL.start();
        Listener errL = new Listener(process.getErrorStream());
        errL.start();

        process.waitFor();

        outFile.close();
    }
}
