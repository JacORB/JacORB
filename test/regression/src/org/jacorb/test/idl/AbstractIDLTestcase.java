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
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.test.common.TestUtils;

public class AbstractIDLTestcase extends TestCase
{
    protected final String fileName;
    protected final String filePath;
    protected final String testName;
    
    public AbstractIDLTestcase(String name, File file)
    {
        // sets testname to a more informative value.
        // this would break JUnit as the testname
        // is used to look up the testmethod to run.
        // for this reason the runTest() method
        // is overriden locally to get back
        // the standard behaviour.
        super(name + ": " + file.getName());

        testName = name;
        fileName = file.getName();
        filePath = file.getAbsolutePath();
    }

    protected void runJacIDL(boolean shouldFail) 
    {
        String[] file = createJacIDLArgs();

        StringWriter writer = new StringWriter();
        try 
        {
            org.jacorb.idl.parser.compile(file, writer);
            
            if (shouldFail)
            {
                fail("parsing of " + fileName + " should fail.");
            }
        } catch (Exception e)
        {
            if (!shouldFail)
            {
                AssertionFailedError error = new AssertionFailedError("parsing of " + fileName + " failed: " + writer.toString());
                error.initCause(e);
                throw error;
            }            
        }
    }

    protected String[] createJacIDLArgs()
    {
        String file[] = new String[3];
        file[0] = "-d";
        file[1] = TestUtils.testHome() + "/src/generated";
        file[2] = filePath;
        return file;
    }
   
    protected void runTest() throws Throwable {
        assertNotNull(testName);
        Method runMethod= null;
        try {
            runMethod= getClass().getMethod(testName, null);
        } catch (NoSuchMethodException e) {
            fail("Method \""+testName+"\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            fail("Method \""+testName+"\" should be public");
        }

        try {
            runMethod.invoke(this, new Class[0]);
        }
        catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        }
        catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }
    
    protected static Test suite(String srcName, Class testClazz)
    {
        return suite(srcName, new String[0], testClazz);
    }
    
    protected static Test suite(String srcName, String[] ignored, Class testClazz)
    {
        final Set ignoredNames = new HashSet(Arrays.asList(ignored));
        TestSuite suite = new TestSuite();

        File srcDir = new File(srcName);
        File[] files = srcDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".idl") && !ignoredNames.contains(name);
            }
        });

        try
        {
            Constructor ctor = testClazz.getConstructor(new Class[] { File.class });

            for (int i = 0; i < files.length; ++i)
            {
                suite.addTest((Test) ctor.newInstance(new Object[] {files[i]}));
            }

            return suite;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
