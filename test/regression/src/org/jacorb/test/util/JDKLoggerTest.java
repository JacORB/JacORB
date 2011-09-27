package org.jacorb.test.util;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import junit.framework.TestSuite;
import org.jacorb.test.common.JacORBTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;


/**
 *  Unit Test for class LogKitLoggerFactory
 * @jacorb-since 2.2
 * @author Alphonse Bendt
 * @version $Id$
 */

public class JDKLoggerTest
    extends JacORBTestCase
{
    private String logDirectory = null;

    public JDKLoggerTest(String name)
    {
        super(name);
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(JDKLoggerTest.class);
        return suite;
    }

    /**
     * Tests logging to a file, rather than the terminal.
     */
    public void testLogFile() throws Exception
    {
        purgeLogDirectory();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");

        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getLogger("jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is a test message");

         purgeLogDirectory();
    }

    /**
     * Tests a log target that ends in $implname.
     */
    public void testLogFileImplName() throws Exception
    {
        purgeLogDirectory();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.implname", "myimpl");
        props.put ("jacorb.logfile", getLogFilename("jacorb-$implname"));
        props.put ("jacorb.log.default.verbosity", "1");

        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getLogger("jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb-myimpl.log"));
        assertFileContains (getLogFilename("jacorb-myimpl.log"),
                            ".*?this is a test message");

         purgeLogDirectory();
    }

    /**
     * Tests the separate log target for the singleton ORB.
     *
     * Disabling this test for now and JDK logger behaves strangely with multiple loggers/ORB
     * - the most recent ORB(full or singleton) takes over the logging.
     */
    public void XXXtestLogFileSingleton() throws Exception
    {
        purgeLogDirectory();

        Properties oldProps = System.getProperties();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogDirectory() + File.separatorChar);
        props.put ("jacorb.logfile.singleton", "jacorb-singleton");
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.orb.singleton.log.verbosity", "1");
        System.setProperties(props);

        ORB orb = new org.jacorb.orb.ORBSingleton();

        System.setProperties(oldProps);

        Logger logger = ((org.jacorb.orb.ORBSingleton)orb).getLogger();
        logger.error("this is a test message");

        // search for the log file -- the name has a timestamp in it
        File dir = new File (getLogDirectory());
        String[] files = dir.list();
        String file = null;
        if (files.length < 1)
        {
            fail ("no log file");
        }

        for (int i = 0; i < files.length; ++i) {
            if (files[i].startsWith("jacorb-singleton"))
            {
                file = files[i];
                break;
            }
        }
        if (file == null) {
            fail ("no singleton log file found");
        }

        File logFile = new File (getLogDirectory(), file);
        BufferedReader in = new BufferedReader
        (
            new FileReader (logFile)
        );
        String line = in.readLine();
        in.close();
        assertNotNull(line);
        if (!line.matches(".*?jacorb\\.orb\\.singleton.*?this is a test message"))
        {
            fail ("log file does not have correct content");
        }
        purgeLogDirectory();
    }

    /**
     * Write to the same log file twice (from two different ORBs),
     * using append mode.
     */
    public void testLogFileAppend() throws Exception
    {
        purgeLogDirectory();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "4");
        props.put ("jacorb.logfile.append", "on");

        ORB orb = ORB.init (new String[]{}, props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getLogger("jacorb");
        orbLogger.error("testLogFileAppend this is the first test message");

        orb.shutdown(true);
        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();

        orb = ORB.init (new String[]{}, props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                             .getLogger("jacorb");
        orbLogger.error("testLogFileAppend this is the second test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the first test message");
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the second test message");

        purgeLogDirectory();
    }

    /**
     * Write to the same log file twice (from two different ORBs),
     * without using append mode.
     */
    public void testLogFileNotAppend() throws Exception
    {
        purgeLogDirectory();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.logfile.append", "off");

        ORB orb = ORB.init (new String[]{}, props);

        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getLogger("jacorb");
        orbLogger.error("testLogFileNotAppend this is the first test message");
        orb.shutdown(true);
        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();

        orb = ORB.init (new String[]{}, props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                             .getLogger("jacorb");
        orbLogger.error("testLogFileNotAppend this is the second test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileNotContains (getLogFilename("jacorb.log"),
                               ".*?this is the first test message");
        assertFileContains (getLogFilename("jacorb.log"),
                            ".*?this is the second test message");

        purgeLogDirectory();
    }

    /**
     * Use a rotating file target, write several messages, and observe how
     * several logs are created.
     */
    public void testLogFileRotation() throws Exception
    {
        purgeLogDirectory();

        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogFilename("jacorb.log"));
        props.put ("jacorb.log.default.verbosity", "1");
        props.put ("jacorb.logfile.maxLogSize", "100");
        props.put ("jacorb.logfile.rotateCount", "4");

        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getLogger("jacorb");
        for (int i=0; i<82; i++)
            orbLogger.error("this is a test message");

        // JAC#384: jacorb.log file shouldn't be created
        //          when log rotating is used
        // assertFileExists (getLogFilename("jacorb.log"));

        assertFileExists (getLogFilename("jacorb.log.0"));
        assertFileExists (getLogFilename("jacorb.log.1"));
        assertFileExists (getLogFilename("jacorb.log.2"));
        assertFileExists (getLogFilename("jacorb.log.3"));

        purgeLogDirectory();
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

    private String getLogDirectory()
    {
        if (logDirectory == null)
        {
            File result = new File (TestUtils.testHome(), "logtest");
            result.mkdirs();
            logDirectory = result.toString();
        }
        return logDirectory;
    }

    private void purgeLogDirectory()
    {
        File dir = new File (getLogDirectory());
        String[] files = dir.list();
        for (int i=0; i<files.length; i++)
        {
            File f = new File (dir, files[i]);
            f.delete();
        }
    }
}
