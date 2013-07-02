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

package org.jacorb.test.common;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

/**
 * @author Alphonse Bendt
 */
public class IDLTestSetup extends TestSetup
{
    private final Object idlFile;
    private final Object idlArgs;
    private File dirGeneration;

    public IDLTestSetup(Test test, Object idlFile, Object idlArgs)
    {
        super(test);

        this.idlFile = idlFile;
        this.idlArgs = idlArgs;
    }

    public final void setUp() throws Exception
    {
        dirGeneration = TestUtils.createTempDir("IDL_CLASSES");

        TestUtils.log("[IDLTestSetup] using temporary directory " + dirGeneration + " for IDL generation");

        String[] args = createJacIDLArgs(dirGeneration, getIDLFile(idlFile), getIDLArgs(idlArgs));

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

    public static String[] createJacIDLArgs(File dir, File idlFile, List additionalIDLArgs)
    {
        List args = new ArrayList();
        args.addAll(Arrays.asList(new String[] {"-ir", "-forceOverwrite", "-d", dir.getAbsolutePath()}));

        args.addAll(additionalIDLArgs);

        if (idlFile.isDirectory())
        {
            final List idlFiles = TestUtils.getFilesRecursively(idlFile, ".idl");
            for (Iterator iter = idlFiles.iterator(); iter.hasNext();)
            {
                String fileName = ((File) iter.next()).toString();
                args.add(fileName);
            }
        }
        else
        {
            args.add(idlFile.getAbsolutePath());
        }

        return (String[]) args.toArray(new String[args.size()]);
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
        List args = new ArrayList();
        args.add(TestUtils.testHome() + "/../../bin/idl");
        args.addAll(Arrays.asList(arg));

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
        return (File[]) TestUtils.getJavaFilesRecursively(dir).toArray(new File[0]);
    }

    private static File getIDLFile(Object idlFile)
    {
        final File _idlFile;
        if (idlFile == null)
        {
            throw new IllegalArgumentException("file might not be null");
        }
        else if (idlFile instanceof File)
        {
            _idlFile = (File) idlFile;
        }
        else if (idlFile instanceof String)
        {
            final String fileName = (String) idlFile;
            File file = new File(fileName);
            if (file.isAbsolute())
            {
                _idlFile = file;
            }
            else
            {
                _idlFile = new File(TestUtils.testHome() + "/idl/" + fileName);
            }
        }
        else
        {
            throw new IllegalArgumentException("don't know how to convert " + idlFile);
        }
        TestUtils.log("using IDL " + (_idlFile.isDirectory() ? "dir" : "file") + " " + _idlFile);

        return _idlFile;
    }

    private static List getIDLArgs(Object idlArgs)
    {
        final List _idlArgs;
        if (idlArgs == null)
        {
            return Collections.EMPTY_LIST;
        }
        else if (idlArgs instanceof List)
        {
            _idlArgs = (List) idlArgs;
        }
        else if (idlArgs instanceof String[])
        {
            _idlArgs = Arrays.asList((String[])idlArgs);
        }
        else if (idlArgs instanceof String)
        {
            _idlArgs = Collections.singletonList(idlArgs);
        }
        else
        {
            throw new IllegalArgumentException("don't know how to convert " + idlArgs);
        }
        return _idlArgs;
    }
}
