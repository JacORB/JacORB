package org.jacorb.test.util;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.orb.BasicServerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.slf4j.Logger;


/**
 * Unit Test for class LogKitLoggerFactory
 *
 * @author Alphonse Bendt
 */
public class JDKLoggerTest extends ORBTestCase
{
    private File logDirectory = null;

    @Before
    public void setUp()
    {
        purgeLogDirectory();
    }

    @After
    public void tearDown()
    {
        purgeLogDirectory();
    }

    /**
     * Verify servant_preinvoke logging (esp: non_existent).
     */
    @Test
    public void testServantPreInvoke () throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "2");

        ORB orb = this.getAnotherORB(props);
        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references( "RootPOA" ));
        POAManager poaManager = rootPOA.the_POAManager();

        poaManager.activate();

        BasicServerImpl servant = new BasicServerImpl();

        rootPOA.activate_object(servant);

        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(servant));

        assertEquals(42, server.bounce_long(42));

        rootPOA.deactivate_object(rootPOA.servant_to_id(servant));

        assertFileExists (getLogFilename("jacorb.log"));
        try
        {
            server.bounce_boolean(true);
        }
        catch (OBJECT_NOT_EXIST e)
        {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);

            assertTrue ( stringWriter.toString().contains("servant_preinvoke"));
        }

        boolean result = server._non_existent();
        assertTrue (result == true);

        assertFileNotContains (getLogFilename("jacorb.log"), ".*OBJECT_NOT_EXIST.*");
    }

    /**
     * Tests logging to a file, rather than the terminal.
     */
    @Test
    public void testLogFile() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is a test message");
    }

    /**
     * Tests a log target that ends in $implname.
     */
    @Test
    public void testLogFileImplName() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.implname", "myimpl");
        props.put ("jacorb.logfile", getLogFilename("jacorb-$implname"));
        props.put ("jacorb.log.default.verbosity", "1");

        ORB orb = this.getAnotherORB(props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb-myimpl.log"));
        assertFileContains (getLogFilename("jacorb-myimpl.log"),
                            ".*?this is a test message");
    }

    /**
     * Tests the separate log target for the singleton ORB.
     *
     * Disabling this test for now and JDK logger behaves strangely with multiple loggers/ORB
     * - the most recent ORB(full or singleton) takes over the logging.
     */
    @Test
    public void testLogFileSingleton() throws Exception
    {
        Properties oldProps = System.getProperties();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile",  getLogDirectory().toString());
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.orb.singleton.log.verbosity", "1");
        System.setProperties(props);

        ORB orb = new org.jacorb.orb.ORBSingleton();

        System.setProperties(oldProps);

        Logger logger = ((org.jacorb.orb.ORBSingleton)orb).getLogger();
        logger.error("this is a test message");

        // search for the log file -- the name has a timestamp in it
        File dir = getLogDirectory();
        String[] files = dir.list();
        String file = null;
        if (files.length < 1)
        {
            fail ("no log file");
        }

        for (int i = 0; i < files.length; ++i)
        {
            if (files[i].startsWith("orbsingleton") &&
                !files[i].endsWith(".lck") )
            {
                file = files[i];
                break;
            }
        }
        if (file == null)
        {
            fail ("no singleton log file found");
        }

        File logFile = new File (getLogDirectory(), file);
        BufferedReader in = new BufferedReader
        (
            new FileReader (logFile)
        );
        String line = in.readLine();
        in.close();
        assertNotNull("Line from " + logFile + " was null", line);
        if (!line.matches(".*this is a test message"))
        {
            fail ("log file does not have correct content");
        }
    }

    /**
     * Write to the same log file twice (from two different ORBs),
     * using append mode.
     */
    @Test
    public void testLogFileAppend() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "4");
        props.put ("jacorb.logfile.append", "on");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("testLogFileAppend this is the first test message");

        orb.shutdown(true);
        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();

        orb = this.getAnotherORB(props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("testLogFileAppend this is the second test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the first test message");
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the second test message");
    }

    /**
     * Write to the same log file twice (from two different ORBs),
     * without using append mode.
     */
    @Test
    public void testLogFileNotAppend() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.logfile.append", "off");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("testLogFileNotAppend this is the first test message");
        orb.shutdown(true);
        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();

        orb = ORB.init (new String[]{}, props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("testLogFileNotAppend this is the second test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileNotContains (getLogFilename("jacorb.log"),
                               ".*?this is the first test message");
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the second test message");
    }

    /**
     * Use a rotating file target, write several messages, and observe how
     * several logs are created.
     */
    @Test
    public void testLogFileRotation() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.logfile.maxLogSize", "100");
        props.put ("jacorb.logfile.rotateCount", "4");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        for (int i=0; i<82; i++)
            orbLogger.error("this is a test message");

        // JAC#384: jacorb.log file shouldn't be created
        //          when log rotating is used
        // assertFileExists (getLogFilename("jacorb.log"));

        assertFileExists (getLogFilename("jacorb.log.0"));
        assertFileExists (getLogFilename("jacorb.log.1"));
        assertFileExists (getLogFilename("jacorb.log.2"));
        assertFileExists (getLogFilename("jacorb.log.3"));
    }

    /**
     * Verify time is printed
     */
    @Test
    public void testLogFileTime() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.log.clockFormat", "TIME");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            "[0-9][0-9]:[0-9][0-9]:[0-9][0-9] SEVERE.*?this is a test message");
    }


    /**
     * Verify we can configure clock format with a lower case
     */
    @Test
    public void testLogFileTimeLowerCase() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.log.clockFormat", "time");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            "[0-9][0-9]:[0-9][0-9]:[0-9][0-9] SEVERE.*?this is a test message");
    }


    /**
     * Verify time and date is printed
     */
    @Test
    public void testLogFileDateTime() throws Exception
    {
        Properties props = new Properties();
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.log.clockFormat", "DATE_TIME");

        ORB orb = this.getAnotherORB(props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration().getLogger("org.jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9].*SEVERE.*?this is a test message");
    }


    private void assertFileExists (String filename)
    {
        File f = new File(filename);
        if (!f.exists())
        {
            fail ("file " + filename + " is missing");
        }
    }

    private void assertFileContains (String filename, String regex)
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader (new FileReader (filename));
            while (true)
            {
                String line = in.readLine();
                if (line == null) break;
                if (line.matches(regex)) return;
            }
            fail ("file " + filename + " does not contain " + regex);
        }
        catch (IOException ex)
        {
            fail ("IOException while searching file " + filename);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                // ignore
            }
        }
    }

    private void assertFileNotContains (String filename, String regex)
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader (new FileReader (filename));
            while (true)
            {
                String line = in.readLine();
                if (line == null) break;
                if (line.matches(regex))
                {
                    fail ("file " + filename + " should not contain " + regex);
                }
            }
            return;
        }
        catch (IOException ex)
        {
            fail ("IOException while searching file " + filename);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                // ignore
            }
        }
    }

    private String getLogFilename (String basename)
    {
        File result = new File (getLogDirectory(), basename);
        return result.toString();
    }

    private File getLogDirectory()
    {
        if (logDirectory == null)
        {
            logDirectory = new File (TestUtils.testHome(), "target/logtest");
            logDirectory.mkdirs();
        }

        return logDirectory;
    }

    private void purgeLogDirectory()
    {
        String[] files = getLogDirectory().list();
        for (int i=0; files != null && i<files.length; i++)
        {
            File f = new File (getLogDirectory(), files[i]);
            f.delete();
        }
    }
}
