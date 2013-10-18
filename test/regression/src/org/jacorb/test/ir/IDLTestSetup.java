/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.ir;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.jacorb.test.common.StreamListener;
import org.jacorb.test.common.TestUtils;

/**
 * @author Alphonse Bendt
 */
class IDLTestSetup
{
    private File dirGeneration;

    public IDLTestSetup(String idlFile, String[] idlArgs) throws Exception
    {
        dirGeneration = TestUtils.createTempDir("IDL_CLASSES");

        TestUtils.log("[IDLTestSetup] using temporary directory " + dirGeneration + " for IDL generation");

        String[] args = createJacIDLArgs(dirGeneration, getIDLFile(idlFile), idlArgs);

        runJacIDLInProcess(idlFile.toString(), args, false);

        TestUtils.compileJavaFiles(dirGeneration, getJavaFiles(dirGeneration), false);
    }

    public void tearDown() throws Exception
    {
        TestUtils.deleteRecursively(dirGeneration);
    }

    public File getDirectory()
    {
        return dirGeneration;
    }

    public static String[] createJacIDLArgs(File dir, File idlFile, String []additionalIDLArgs)
    {
        List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList(new String[] {"-ir", "-forceOverwrite", "-d", dir.getAbsolutePath()}));
        if (additionalIDLArgs != null)
        {
            args.addAll(Arrays.asList(additionalIDLArgs));
        }
        if (idlFile.isDirectory())
        {
            final List<File> idlFiles = TestUtils.getFilesRecursively(idlFile, ".idl");
            for (Iterator<File> iter = idlFiles.iterator(); iter.hasNext();)
            {
                String fileName = iter.next().toString();
                args.add(fileName);
            }
        }
        else
        {
            args.add(idlFile.getAbsolutePath());
        }

        return args.toArray(new String[args.size()]);
    }

    public static String runJacIDLInProcess(String file, String[] args, boolean failureExpected) throws AssertionFailedError
    {
        StringWriter writer = new StringWriter();
        final String details = writer.toString();
        try
        {
            TestUtils.log("[IDLTestSetup] run JacIDL on file " + file + " with args " + Arrays.asList(args));

            org.jacorb.idl.parser.compile(args, writer);

            if (failureExpected)
            {
                fail("parsing of " + file + " should fail.");
            }
        }
        catch (Exception e)
        {
            handleJacIDLFailed(file, failureExpected, details, e);
        }

        return details;
    }

    public static String runJacIDLExtraProcess(String file, String[] arg, boolean failureExpected) throws Exception
    {
        List<String> args = new ArrayList<String>();
        args.add(TestUtils.testHome() + "/../../bin/idl");
        args.addAll(Arrays.asList(arg));

        TestUtils.log("Running: " + args);

        Process process = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));

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
            handleJacIDLFailed(file, failureExpected, details, e);
        }

        return details;
    }

    private static void handleJacIDLFailed(String file, boolean failureExpected, final String details, Exception e) throws AssertionFailedError
    {
        if (!failureExpected)
        {
            AssertionFailedError error = new AssertionFailedError("parsing of " + file
                    + " failed: " + details);
            error.initCause(e);
            throw error;
        }
    }

    private static File[] getJavaFiles(File dir)
    {
        return TestUtils.getJavaFilesRecursively(dir).toArray(new File[0]);
    }

    private static File getIDLFile(String fileName)
    {
        File result = new File(fileName);

        if ( ! result.isAbsolute())
        {
            result = new File(TestUtils.testHome() + "/idl/" + fileName);
        }

        TestUtils.log("using IDL " + (result.isDirectory() ? "dir" : "file") + " " + result);

        return result;
    }
}
