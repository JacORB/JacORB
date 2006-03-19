package org.jacorb.test.common.launch;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.util.*;
import java.io.*;
import org.jacorb.test.common.TestUtils;

/**
 * Launches a JacORB process by direct invocation of a JVM
 * with appropriate arguments.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class DirectLauncher extends JacORBLauncher
{
    public DirectLauncher(String jacorbHome, boolean coverage)
    {
        super(jacorbHome, coverage);
    }
        
    public Process launch(String classpath,
                          Properties props,
                          String mainClass,
                          String[] args)
    {
        Runtime rt = Runtime.getRuntime();

        List cmdList = new ArrayList();

        String fullClasspath = TestUtils.pathAppend(classpath, getJacORBLibraryPath());      
        
        String javaHome = System.getProperty("java.home");
        String javaCommand = javaHome + "/bin/java";
        cmdList.add (javaCommand);
        cmdList.add ("-classpath");
        cmdList.add (fullClasspath);
        cmdList.add ("-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB");
        cmdList.add ("-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton");
        cmdList.addAll (propsToArgList(props));
        cmdList.add ("-Djacorb.home=" + jacorbHome);
        cmdList.add (mainClass);
        cmdList.addAll (Arrays.asList(args));
        
        String[] envp;
    
        if (TestUtils.isWindows())      
        {
            envp = new String[3];
            envp[0] = "JACORB_HOME=" + jacorbHome;
            try 
            {
                envp[1] = "SystemRoot=" + TestUtils.systemRoot();
            }
            catch (RuntimeException e) 
            {
                System.out.println("WARNING: caught RuntimeException when reading SystemRoot: " + e.getMessage());
            }
            catch (IOException e) 
            {
                System.out.println("WARNING: caught IOException when reading SystemRoot: " + e.getMessage());
            }
            envp[2] = "JACORB_TEST_HOME=" + TestUtils.testHome();
        }
        else
        {
            envp = new String[1];
            envp[0] = "JACORB_HOME=" + jacorbHome;

        }
        try
        {
            String[] cmd = toStringArray(cmdList);
            //System.out.print ("exec: ");
            //for (int i=0; i<cmd.length; i++)
            //{
            //    System.out.print (cmd[i]);
            //    if (i<cmd.length-1) System.out.print (" ");
            //}
            //System.out.println();
            Process proc = rt.exec (cmd, envp);
            return proc;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public String getJacORBLibraryPath()
    {
        String libPath = getJacORBPath();
        libPath = TestUtils.pathAppend(libPath, jacorbHome + "/lib/logkit-1.2.jar");
        libPath = TestUtils.pathAppend(libPath, jacorbHome + "/lib/avalon-framework-4.1.5.jar");
        libPath = TestUtils.pathAppend(libPath, jacorbHome + "/lib/backport-util-concurrent.jar");
        libPath = TestUtils.pathAppend(libPath, jacorbHome + "/lib/antlr-2.7.2.jar");
        libPath = TestUtils.pathAppend(libPath, jacorbHome + "/lib/picocontainer-1.2.jar");
        return libPath;
    }
    
    public String getJacORBPath()
    {
        File result = null;
        if (coverage)
        {
            result = new File (jacorbHome, "classes-instrumented");
            if (!result.exists())
                System.out.println ("WARNING: JacORB installation " 
                        + jacorbHome
                        + " is not instrumented; coverage "
                        + " will not be available");
            else
            {
                String jacorbPath = result.toString();
                jacorbPath = TestUtils.pathAppend(jacorbPath, jacorbHome + "/classes/");
                jacorbPath = TestUtils.pathAppend(jacorbPath, jacorbHome + "/test/regression/lib/emma.jar");
                return jacorbPath;
            }
        }
        result = new File (jacorbHome, "classes/org");
        if (result.exists())
            return new File (jacorbHome, "classes").toString();
        else
            return new File (jacorbHome, "lib/jacorb.jar").toString();
    }
}
