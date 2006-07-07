package org.jacorb.test.common;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.*;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Stub;

/**
 * Utility class used to setup JUnit-TestSuite
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class TestUtils
{
    private static final String[] STRING_ARRAY_TEMPLATE = new String[0];

    private static String testHome = null;
    private static String systemRoot = null;
    private static final boolean verbose = "true".equalsIgnoreCase(System.getProperty("jacorb.test.verbose"));

    private static final boolean JDK_13 =
        (
            System.getProperty ("java.version").indexOf("1.3") != -1 ||
            "CVM".equals((String)System.getProperty ("java.vm.name"))
        );


    static
    {
        log("jacorb.test.verbose is set to true");
    }

    /**
     * this method returns a List of all public Methods which Names start with the Prefix "test" and
     * accept no Parameters e.g:
     *
     * <ul>
     * <li>testOperation
     * <li>testSomething
     * </ul>
     *
     */
    public static String[] getTestMethods(Class clazz)
    {
        return getTestMethods(clazz, "test");
    }

    public static String[] getTestMethods(Class clazz, String prefix)
    {
        Method[] methods = clazz.getMethods();

        List result = new ArrayList();

        for (int x = 0; x < methods.length; ++x)
        {
            if (methods[x].getName().startsWith(prefix))
            {
                if (methods[x].getParameterTypes().length == 0)
                {
                    result.add(methods[x].getName());
                }
            }
        }

        return (String[]) result.toArray(STRING_ARRAY_TEMPLATE);
    }

    /**
     * Returns the name of the home directory of this regression suite.
     */
    public static String testHome()
    {
        if (testHome == null)
        {
            //See if we set it as a property using TestLauncher
            testHome = System.getProperty ("jacorb.test.home");

            //Try to find it from the run directory
            if (testHome == null)
            {
                URL url = TestUtils.class.getResource("/.");

                String result = url.toString();
                if (result.matches("file:/.*?"))
                {
                    result = result.substring (5, result.length() - 9);
                }
                String relativePath="/classes/";
                if (!pathExists(result, relativePath))
                {
                    throw new RuntimeException ("cannot find test home");
                }
                testHome = osDependentPath(result);
            }
        }
        return testHome;
    }

    public static Test suite(Class testClazz, Class testSetupClazz, String suiteName, String testMethodPrefix)
        throws Exception
    {
        TestSuite suite = new TestSuite(suiteName);

        TestSetup setup = newSetup(suite, testSetupClazz);

        String[] testMethods = getTestMethods(testClazz, testMethodPrefix);

        addToSuite(suite, setup, testClazz, testMethods);

        return setup;
    }

    private static TestSetup newSetup(Test suite, Class testSetupClazz) throws Exception
    {
        Constructor ctor = testSetupClazz.getConstructor(new Class[] { Test.class });
        return (TestSetup) ctor.newInstance(new Object[] { suite });
    }

    public static void addToSuite(TestSuite suite, TestSetup setup, Class clazz)
    {
        addToSuite(suite, setup, clazz, getTestMethods(clazz));
    }

    public static void addToSuite(TestSuite suite, TestSetup setup, Class clazz, String methodPrefix)
    {
        addToSuite(suite, setup, clazz, getTestMethods(clazz, methodPrefix));
    }

    public static void addToSuite(TestSuite suite, TestSetup setup, Class clazz,
            String[] testMethods)
    {
        try
        {
            Constructor ctor = getConstructor(clazz, setup);

            for (int x = 0; x < testMethods.length; ++x)
            {
                suite.addTest((Test) ctor.newInstance(new Object[] { testMethods[x], setup }));
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * try to locate a constructor that accepts (String, TestSetup (or subclasses));
     */
    private static Constructor getConstructor(Class clazz, TestSetup setup)
    {
        Class setupClazz = setup.getClass();

        while(setupClazz != null)
        {
            try
            {
                return clazz.getConstructor(new Class[] { String.class, setupClazz});
            }
            catch(NoSuchMethodException e)
            {
                // ignored
            }

            setupClazz = setupClazz.getSuperclass();
        }

        throw new RuntimeException("Could not locate constructor for (" + String.class.getName() + ", " + setup.getClass().getName() + ") in class " + clazz.getName());
    }

    public static String osDependentPath(String path)
    {
        path=(new File(path)).toString();
        return path.trim();
    }

    private static boolean pathExists(String basePath, String suffix)
    {
        File filePath = new File(basePath + suffix);
        return filePath.exists();
    }

    public static String pathAppend(String path1, String path2)
    {
        String osDepPath1 = (new File(path1)).toString();
        String osDepPath2 = (new File(path2)).toString();
        String pathSeperator = System.getProperty("path.separator");
        return osDepPath1 + pathSeperator + osDepPath2;
    }
    /**
     * In addition to file and path seperators being differnt,
     * Windows requires an additional environment variable for
     * SystemRoot in DirectLauncer.
     */
    public static boolean isWindows()
    {
        return (System.getProperty("os.name").indexOf("Windows") != -1) ;
    }

    /**
     * Returns the SystemRoot, should be used on Windows only.  This
     * is necessary to pevent the following error:
     * "Unrecognized Windows Sockets error: 10106: create"
     */
    public static String systemRoot() throws RuntimeException, java.io.IOException
    {

        if (isWindows())
        {
            if (systemRoot == null)
            {
                //See if we set it as a property using TestLauncher.
                //This means we are likely in the DirectLauncher
                systemRoot = System.getProperty ("jacorb.SystemRoot");

                if (systemRoot == null)
                {
                    //We are likely in the TestLauncher, see if we can
                    //get it from the system environment.
                    Properties env = new Properties();
                    try
                    {
                        String setCmd = getSetCommand();
                        Process proc = Runtime.getRuntime().exec(setCmd);
                        InputStream ioStream = proc.getInputStream();
                        env.load(ioStream);
                        systemRoot = env.getProperty("SystemRoot");
                        ioStream.close();
                        proc.destroy();
                    }
                    catch(java.io.IOException e)
                    {
                        throw e;
                    }
                    if (systemRoot == null)
                    {
                        throw new RuntimeException("Could not find SystemRoot, make sure SystemRoot env var is set");
                    }
                    //The '\' character gets interpeted as an escape
                    if (systemRoot.charAt(1) == ':' && systemRoot.charAt(2) != '\\')
                    {
                        String prefix = systemRoot.substring(0, 2);
                        String suffix = systemRoot.substring(2, systemRoot.length());
                        systemRoot = prefix + "\\" + suffix;
                    }
                }
            }
        }
        else
        {
            throw new RuntimeException("TestUtils.systemRoot() was called on a non-Windows OS");
        }
        return systemRoot;
    }

    /*
     * Private method to get the set command.  This is necessary because
     * if SystemRoot has not been set, it needs to be set for DirectLauncer.
     */
    private static String getSetCommand()
    {
        String setCmd;
        String osName = System.getProperty("os.name");

        if (osName.indexOf("indows")  != -1)
        {
            if (osName.indexOf("indows 9") != -1)
            {
                setCmd = "command.com /c set";
            }
            else
            {
                setCmd = "cmd.exe /c set";
            }
        }
        else
        {
            setCmd = "/usr/bin/env";
            //should double check for all unix platforms
        }
        return setCmd;
    }

    public static void log(String string)
    {
        if (verbose)
        {
            System.err.println (string);
        }
    }

    /**
     * create properties that contain the correct (JDK specific)
     * settings to create a Sun ORB.
     */
    public static Properties newSunORBProperties()
    {
        final Properties props = new Properties();
        boolean isJDK5;

        try
        {
            // if JDK1.5 version of ORBImpl is available we'll
            // assume that this is JDK1.5
            Class.forName("com.sun.corba.se.impl.orb.ORBImpl");
            isJDK5 = true;
        }
        catch (ClassNotFoundException e)
        {
            // JDK1.4
            isJDK5 = false;
        }

        if (isJDK5)
        {
            props.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.impl.orb.ORBImpl");
            props.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.impl.orb.ORBSingleton");
        }
        else
        {
            props.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.Interceptors.PIORB");
            props.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");
        }
        return props;
    }

    public static List propsToArgList (Properties props)
    {
        List result = new ArrayList();

        if (props == null) return result;

        for (Iterator i = props.keySet().iterator(); i.hasNext();)
        {
            String key = (String)i.next();
            String value = props.getProperty(key);
            result.add ("-D" + key + "=" + value);
        }

        return result;
    }

    public static Stub toStub(Remote remote, org.omg.CORBA.Object reference, Class clazz) throws NoSuchObjectException
    {
         if (JDK_13)
         {
             return (Stub) PortableRemoteObject.narrow(reference, clazz);
         }

         return (Stub) PortableRemoteObject.toStub(remote);
    }

    public static boolean isJDK13()
    {
        return JDK_13;
    }

    public static boolean isPropertyTrue(String value)
    {
        return "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }
}