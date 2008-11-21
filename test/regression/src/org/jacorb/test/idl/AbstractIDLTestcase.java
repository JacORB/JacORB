/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

package org.jacorb.test.idl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.test.common.StreamListener;
import org.jacorb.test.common.TestUtils;

public class AbstractIDLTestcase extends TestCase
{
    protected final File idlFile;

    /**
     * dir where the idl compiler places generated
     * java source files.
     */
    protected final File dirGeneration;

    /**
     * dir where .class are compiled to.
     */
    protected final File dirCompilation;

    private final String testName;

    public AbstractIDLTestcase(String name, File file)
    {
        // sets testname to a more informative value.
        // this would break JUnit as the testname
        // is used to look up the testmethod to run.
        // for this reason the runTest() method
        // is overriden locally to get back
        // the standard behaviour.
        super(name + ": " + file.getName());

        assertTrue(file + " should exist", file.exists());
        assertTrue(file.isFile());

        testName = name;
        idlFile = file;

        dirGeneration = new File(TestUtils.testHome() + "/src-testidl/" + idlFile.getName());
        TestUtils.deleteRecursively(dirGeneration);
        File dirClasses = new File(TestUtils.testHome() + "/classes-testidl");

        dirClasses.mkdir();
        assertTrue(dirClasses.canWrite());
        assertTrue(dirClasses.isDirectory());

        dirCompilation = new File(dirClasses, idlFile.getName());
        TestUtils.deleteRecursively(dirCompilation);
    }

    /**
     * run JacORBs IDL compiler on the current idlFile
     */
    protected void runJacIDL(boolean failureExpected, boolean spawnProcess) throws Exception
    {
        final String details;

        if (spawnProcess)
        {
            details = runJacIDLExtraProcess(failureExpected);
        }
        else
        {
            details = runJacIDLInProcess(failureExpected);
        }

        TestUtils.log("[" + idlFile.getName() + " output]:\n" + details);
    }

    private String runJacIDLInProcess(boolean failureExpected) throws AssertionFailedError
    {
        String[] file = createJacIDLArgs();

        dirGeneration.mkdir();

        StringWriter writer = new StringWriter();
        final String details = writer.toString();
        try
        {
            boolean success = org.jacorb.idl.parser.compile(file, writer);

            assertTrue("parser didn't succeed", success);

            if (failureExpected)
            {
                fail("parsing of " + idlFile.getName() + " should fail.");
            }
        }
        catch (Exception e)
        {
            handleJacIDLFailed(failureExpected, details, e);
        }

        return details;
    }

    private String runJacIDLExtraProcess(boolean failureExpected) throws Exception
    {
        List args = new ArrayList();
        args.add(TestUtils.testHome() + "/../../bin/idl");
        args.addAll(Arrays.asList(createJacIDLArgs()));

        TestUtils.log("Running: " + args);

        Process process = Runtime.getRuntime().exec((String[])args.toArray(new String[args.size()]));

        StreamListener outListener = new StreamListener (process.getInputStream(), "OUT");
        StreamListener errListener = new StreamListener (process.getErrorStream(), "ERR");
        String details = "STDOUT\n" + outListener.getBuffer() + "\nSTDERR:\n" + errListener.getBuffer() + "\n";

        try
        {
            outListener.start();
            errListener.start();
            process.waitFor();

            boolean success = process.exitValue() == 0;

            if (failureExpected)
            {
                assertFalse(success);
            }
            else
            {
                assertTrue(success);
            }
        }
        catch (Exception e)
        {
            handleJacIDLFailed(failureExpected, details, e);
        }

        return details;
    }

    private void handleJacIDLFailed(boolean failureExpected, final String details, Exception e) throws AssertionFailedError
    {
        if (!failureExpected)
        {
            AssertionFailedError error = new AssertionFailedError("parsing of " + idlFile.getName()
                    + " failed: " + details);
            error.initCause(e);
            throw error;
        }
    }

    /**
     * compile all .java files that were
     * generated by a previous IDL run.
     * this method depends on a properly configured
     * environment that has javac available.
     * @return a ClassLoader that can be used to access the compiled classes
     */
    protected ClassLoader compileGeneratedSources(boolean failureExpected) throws IOException
    {
        File[] files = getJavaFiles();
        return TestUtils.compileJavaFiles(this.dirCompilation, files, failureExpected);
    }

    /**
     * get a list of all .java files that were
     * generated by a previous IDL run.
     */
    protected File[] getJavaFiles()
    {
        return (File[]) TestUtils.getJavaFilesRecursively(dirGeneration).toArray(new File[0]);
    }

    /**
     * build the argument list that will be used to invoke the
     * IDL compiler.
     */
    protected String[] createJacIDLArgs()
    {
        String file[] = new String[] { "-forceOverwrite", "-d", dirGeneration.getAbsolutePath(),
                idlFile.getAbsolutePath() };
        return file;
    }

    protected void runTest() throws Throwable
    {
        assertNotNull(testName);
        Method runMethod = null;
        try
        {
            runMethod = getClass().getMethod(testName, new Class[0]);
        }
        catch (NoSuchMethodException e)
        {
            fail("Method \"" + testName + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers()))
        {
            fail("Method \"" + testName + "\" should be public");
        }

        try
        {
            runMethod.invoke(this, new Object[0]);
        }
        catch (InvocationTargetException e)
        {
            e.fillInStackTrace();
            throw e.getTargetException();
        }
        catch (IllegalAccessException e)
        {
            e.fillInStackTrace();
            throw e;
        }
    }

    /**
     * create a test suite for all idl files located in
     * the supplied directory.
     *
     * @param srcDir directory that contains the .idl files
     * @param testClazz must supply a constructor
     * that accepts a file argument.
     */
    protected static Test suite(String srcDir, Class testClazz)
    {
        return suite(srcDir, testClazz, ".idl");
    }

    /**
     * create a test suite for all idl files which names are
     * ending with the specified suffix and which are located in
     * the supplied directory.
     *
     * @param srcDir directory that contains the .idl files.
     * @param testClazz must supply a constructor that accepts a file argument.
     * @param suffix should match all IDL files that should be tested.
     */
    protected static Test suite(final String srcDir, final Class testClazz, final String suffix)
    {
        TestSuite suite = new TestSuite();

        File file = new File(srcDir);
        File[] files = file.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(suffix);
            }
        });

        try
        {
            Constructor ctor = testClazz.getConstructor(new Class[] { File.class });

            for (int i = 0; i < files.length; ++i)
            {
                suite.addTest((Test) ctor.newInstance(new Object[] { files[i] }));
            }

            return suite;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * search for a method with the signature
     * public void verify_<FILENAME>(ClassLoader cl) {...}
     * (dots in filename will be converted to _) and invoke
     * it with the specified classloader.
     */
    protected void invokeVerifyMethod(ClassLoader cl) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            // test if a verify_ method is available and invoke it
            String file = idlFile.getName().replaceAll("\\.", "_");

             TestUtils.log("look for verify_" +  file);

            Method method = getClass().getMethod("verify_" + file, new Class[] {ClassLoader.class});
            method.invoke(this, new Object[] {cl});
        }
        catch (NoSuchMethodException e)
        {
            // ignored
        }
    }
}
