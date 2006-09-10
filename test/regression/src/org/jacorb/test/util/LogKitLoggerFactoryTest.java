package org.jacorb.test.util;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.*;
import java.io.*;

import junit.framework.TestSuite;

import org.omg.CORBA.*;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.config.JacORBConfiguration;

import org.jacorb.test.common.*;


/**
 *  Unit Test for class LogKitLoggerFactory
 * @jacorb-since 2.2
 * @author Alphonse Bendt
 * @version $Id$
 */

public class LogKitLoggerFactoryTest 
    extends JacORBTestCase
{
    private String logDirectory = null;
    
    public LogKitLoggerFactoryTest(String name)
    {
        super(name);
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite(LogKitLoggerFactoryTest.class);
        return suite;
    }

    /**
     * Tests whether logger priorities are assigned correctly.
     */
    public void testGetPriorityForNamedLogger() 
        throws Exception
    {
        Properties props = new Properties();

        props.setProperty("jacorb.log.verbosity", "2");
        props.setProperty("jacorb.component.log.verbosity", "3");
        props.setProperty("jacorb.component.subcomponent.log.verbosity", "4");

        props.setProperty ("jacorb.overrated.log.verbosity", "5");
        props.setProperty ("jacorb.underrated.log.verbosity", "-1");
        
        props.setProperty("jacorb.trailingspace.test1", "INFO ");
        props.setProperty("jacorb.trailingspace.test2", "INFO");

        Configuration config = JacORBConfiguration.getConfiguration(props, null, false);
        int defaultPriority = config.getAttributeAsInteger("jacorb.log.default.verbosity",0);
        
        assertEquals(defaultPriority, priorityFor("foologger", config));

        assertEquals(2, priorityFor("jacorb", config));
        assertEquals(2, priorityFor("jacorb.other_component", config));
        assertEquals(2, priorityFor("jacorb.other_component.sub", config));

        assertEquals(3, priorityFor("jacorb.component", config));
        assertEquals(3, priorityFor("jacorb.component.subcomponent2", config));

        assertEquals(4, priorityFor("jacorb.component.subcomponent", config));
        assertEquals(4, priorityFor("jacorb.component.subcomponent.sub", config));

        assertEquals(4, priorityFor("jacorb.overrated", config));
        assertEquals(0, priorityFor("jacorb.underrated", config));
        
        assertEquals(priorityFor("jacorb.trailingspace.test1", config),
                     priorityFor("jacorb.trailingspace.test2", config));
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
        props.put ("jacorb.log.verbosity", "1");
        
        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getNamedLogger("jacorb");
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
        props.put ("jacorb.log.verbosity", "1");
        
        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getNamedLogger("jacorb");
        orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb-myimpl.log"));
        assertFileContains (getLogFilename("jacorb-myimpl.log"),
                            ".*?this is a test message");
        
        purgeLogDirectory();
    }

    /**
     * Tests the separate log target for the singleton ORB.
     */
    public void testLogFileSingleton() throws Exception
    {
        purgeLogDirectory();
        
        Properties oldProps = System.getProperties();
        
        Properties props = new Properties();
        props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.put ("jacorb.logfile", getLogDirectory() + File.separatorChar);
        props.put ("jacorb.logfile.singleton", "jacorb-singleton");
        props.put ("jacorb.log.verbosity", "1");
        props.put ("jacorb.orb.singleton.log.verbosity", "1");
        System.setProperties(props);
                
        ORB orb = new org.jacorb.orb.ORBSingleton();
        
        System.setProperties(oldProps);
        
        Logger logger = ((org.jacorb.orb.ORBSingleton)orb).getLogger();
        logger.error("this is a test message");

        // search for the log file -- the name has a timestamp in it
        File dir = new File (getLogDirectory());
        String[] files = dir.list();
        if (files.length != 1)
        {
            fail ("no log file");
        }
        if (!files[0].startsWith("jacorb-singleton"))
        {
            fail ("no singleton log file found");
        }
        File logFile = new File (getLogDirectory(), files[0]);
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
        props.put ("jacorb.log.verbosity", "1");
        props.put ("jacorb.logfile.append", "on");
        
        ORB orb = ORB.init (new String[]{}, props);
        assertTrue 
        (
            ((org.jacorb.orb.ORB)orb).getConfiguration()
                                     .getAttributeAsBoolean
                                     (
                                         "jacorb.logfile.append"
                                     )
        );
        
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getNamedLogger("jacorb");
        orbLogger.error("this is the first test message");

        orb = ORB.init (new String[]{}, props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                             .getNamedLogger("jacorb");
        orbLogger.error("this is the second test message");
        
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
        props.put ("jacorb.log.verbosity", "1");
        props.put ("jacorb.logfile.append", "off");
        
        ORB orb = ORB.init (new String[]{}, props);
        assertFalse 
        (
            ((org.jacorb.orb.ORB)orb).getConfiguration()
                                     .getAttributeAsBoolean
                                     (
                                         "jacorb.logfile.append"
                                     )
        );
        
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getNamedLogger("jacorb");
        orbLogger.error("this is the first test message");

        orb = ORB.init (new String[]{}, props);
        orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                             .getNamedLogger("jacorb");
        orbLogger.error("this is the second test message");
        
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
        props.put ("jacorb.log.verbosity", "1");
        props.put ("jacorb.logfile.maxLogSize", "1");
        
        ORB orb = ORB.init (new String[]{}, props);
        Logger orbLogger = ((org.jacorb.orb.ORB)orb).getConfiguration()
                                                    .getNamedLogger("jacorb");
        for (int i=0; i<52; i++)
            orbLogger.error("this is a test message");

        assertFileExists (getLogFilename("jacorb.log"));
        assertFileExists (getLogFilename("jacorb.log.000001"));
        assertFileExists (getLogFilename("jacorb.log.000002"));
        assertFileExists (getLogFilename("jacorb.log.000003"));
        assertFileExists (getLogFilename("jacorb.log.000004"));
        
        purgeLogDirectory();
    }

    // internal methods below this line
    
    private int priorityFor (String loggerName, Configuration config)
    {
        Logger logger = config.getNamedLogger(loggerName);
        if (logger.isDebugEnabled())
            return 4;
        else if (logger.isInfoEnabled())
            return 3;
        else if (logger.isWarnEnabled())
            return 2;
        else if (logger.isErrorEnabled())
            return 1;
        return 0;
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
