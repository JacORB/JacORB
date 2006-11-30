/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.ant;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.jacorb.test.ant.samples.JUnitError;
import org.jacorb.test.ant.samples.JUnitFail;
import org.jacorb.test.ant.samples.JUnitOK;
import org.jacorb.test.ant.samples.JUnitTimeout;
import org.jacorb.test.common.TestUtils;

/**
 * NOTE: this class isn't run within the JacORB
 * regression test framework as it actually tests
 * the JacORB regression test framework.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class JacUnitTest extends BuildFileTest
{
    private File tempDir;
    private File outDir;

    public JacUnitTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        tempDir = TestUtils.createTempDir("JacUnitTest");

        outDir = new File(tempDir, "outdir");
        outDir.mkdir();

        URL url = getResource("jacorb-build-test.xml");
        configureProject(url.getFile(), Project.MSG_DEBUG);
    }

    protected void tearDown() throws Exception
    {
        if (TestUtils.verbose)
        {
            TestUtils.log(getOutput());
            TestUtils.log(getError());
        }
    }

    public void testTaskDef() throws Exception
    {
        configureProjectDefaults();

        executeTarget("jacunit-taskdef");
    }

    public void _testTimeout() throws Exception
    {
        project.setProperty("jacunit.shouldfail", "true");

        configureProjectDefaults();

        project.setProperty("jacunit.suite", JUnitTimeout.class.getName());
        project.setProperty("jacunit.testtimeout", "2000");

        final boolean[] done = new boolean[1];
        final Exception[] exc = new Exception[1];

        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    executeTarget("jacunit-run");
                    done[0] = true;
                }
                catch(BuildException e)
                {
                    exc[0] = e;
                }
            };
        };
        thread.start();

        thread.join();

        if (!done[0] && exc[0] == null)
        {
            thread.interrupt();
        }

        assertNotNull(exc[0]);
    }

    public void testRunJacUnit() throws Exception
    {
        configureProjectDefaults();

        project.setProperty("jacunit.suite", JUnitOK.class.getName());
        project.setProperty("jacunit.errorproperty", "jacunit.buildfailed");

        expectPropertyUnset("jacunit-run", "jacunit.buildfailed");

        assertTrue(getFullLog().indexOf(JUnitOK.class.getName()) >= 0);
    }

    public void testErrorPropertyWithErrors() throws Exception
    {
        configureProjectDefaults();

        project.setProperty("jacunit.suite", JUnitError.class.getName());
        project.setProperty("jacunit.errorproperty", "jacunit.buildfailed");

        expectPropertySet("jacunit-run", "jacunit.buildfailed");
    }

    public void testErrorPropertyWithFailures() throws Exception
    {
        configureProjectDefaults();

        project.setProperty("jacunit.suite", JUnitFail.class.getName());
        project.setProperty("jacunit.errorproperty", "jacunit.buildfailed");

        expectPropertySet("jacunit-run", "jacunit.buildfailed");
    }

    public void testRunBatchTest() throws Exception
    {
        configureProjectDefaults();

        project.setProperty("jacunit.testtimeout", "2000");
        project.setProperty("jacunit.errorproperty", "jacunit.buildfailed");
        project.setProperty("jacunit.srcdir", new File(getResource("/test.properties").getFile(), "../../src").toString());
        expectPropertySet("jacunit-batch", "jacunit.buildfailed");

        HashSet files = new HashSet(Arrays.asList(outDir.list()));

        assertFalse(files.isEmpty());

        File shouldBeThere = new File(outDir, "TEST-org.jacorb.test.ant.samples.JUnitTimeout.txt");
        assertTrue(shouldBeThere.exists());

        LineNumberReader reader = new LineNumberReader(new FileReader(shouldBeThere));
        StringBuffer content = new StringBuffer();
        String data = null;

        while( (data = reader.readLine()) != null)
        {
            content.append(data);
        }

        assertTrue(content.toString().length() > 0);
    }

    private void configureProjectDefaults() throws Exception
    {
        final File testDir = new File(tempDir, "testdir");
        testDir.mkdir();

        project.setProperty("jacunit.shouldfail", "false");
        project.setProperty("jacunit.testtimeout", "0");
        project.setProperty("jacunit.outdir", outDir.toString());
        project.setProperty("jacunit.testdir", testDir.toString());
        project.setProperty("jacunit.classpath", System.getProperty("java.class.path"));
    }
}
