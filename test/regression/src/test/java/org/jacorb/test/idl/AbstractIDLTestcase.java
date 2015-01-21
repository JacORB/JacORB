/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import junit.framework.AssertionFailedError;
import org.jacorb.idl.parser;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public abstract class AbstractIDLTestcase extends ORBTestCase
{
    protected final File idlFile;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * dir where the idl compiler places generated
     * java source files.
     */
    protected final File dirGeneration;

    /**
     * dir where .class are compiled to.
     */
    protected final File dirCompilation;

    /**
     * Logger content of running the IDL compiler.
     */
    protected static ByteArrayOutputStream loggerContent;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);

        initLogging();
    }

    protected static void initLogging ()
    {
        parser.logger = Logger.getLogger("org.jacorb.idl");
        parser.logger.setLevel(Level.SEVERE);

        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord arg0) {
                StringBuilder b = new StringBuilder();
                b.append(arg0.getLevel());
                b.append(" ");
                b.append(arg0.getSourceClassName());
                b.append(" ");
                b.append(arg0.getSourceMethodName());
                b.append(" ");
                b.append(arg0.getMessage());
                b.append(System.getProperty("line.separator"));

                Throwable t = arg0.getThrown();
                return t == null ? b.toString() : b.toString () + getStackTrace (t);
            }

            private String getStackTrace (Throwable t)
            {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter (sw));
                return sw.toString();
            }
        };

        loggerContent = new ByteArrayOutputStream();
        PrintStream prStr = new PrintStream(loggerContent);

        parser.handler = new StreamHandler(prStr, formatter);
        parser.handler.setFormatter (formatter);
        parser.handler.setLevel (Level.SEVERE);

        parser.logger.addHandler(parser.handler);
    }

    public AbstractIDLTestcase(File file) throws IOException
    {
        assertTrue(file + " should exist", file.exists());
        assertTrue(file.isFile());

        idlFile = file;

        folder.create();
        dirGeneration = folder.newFolder();
        dirCompilation = folder.newFolder();
    }

    /**
     * run JacORBs IDL compiler on the current idlFile
     */
    protected void runJacIDL(boolean failureExpected) throws Exception
    {
        final String details;

        details = runJacIDLInProcess(failureExpected);

        TestUtils.getLogger().debug("[" + idlFile.getName() + " output]:\n" + details);
    }

    private String runJacIDLInProcess(boolean failureExpected) throws AssertionFailedError
    {
        String[] file = createJacIDLArgs();
        String details = "";
        try
        {
            boolean success;

            try
            {
                success = org.jacorb.idl.parser.compile(file);
            }
            finally
            {
                parser.handler.flush ();
                details = loggerContent.toString ();
                loggerContent.reset();
            }
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
        return TestUtils.getJavaFilesRecursively(dirGeneration).toArray(new File[0]);
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

    /**
     * search for a method with the signature
     * public void verify_&lt;FILENAME&gt;(ClassLoader cl) {...}
     * (dots in filename will be converted to _) and invoke
     * it with the specified classloader.
     */
    protected void invokeVerifyMethod(ClassLoader cl) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            // test if a verify_ method is available and invoke it
            String file = idlFile.getName().replaceAll("\\.", "_");

            TestUtils.getLogger().debug("look for verify_" +  file);

            Method method = getClass().getMethod("verify_" + file, new Class[] {ClassLoader.class});
            method.invoke(this, new Object[] {cl});
        }
        catch (NoSuchMethodException e)
        {
            // ignored
        }
    }
}
