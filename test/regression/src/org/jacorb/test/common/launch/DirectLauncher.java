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
        
        String javaHome = System.getProperty("java.home");
        String javaCommand = javaHome + "/bin/java";
        cmdList.add (javaCommand);
        cmdList.add ("-classpath");
        cmdList.add (classpath + ":" + getJacORBLibraryPath());
        cmdList.add ("-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB");
        cmdList.add ("-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton");
        cmdList.addAll (propsToArgList(props));
        cmdList.add ("-Djacorb.home=" + jacorbHome);
        cmdList.add (mainClass);
        cmdList.addAll (Arrays.asList(args));
        
        String[] envp = new String[]
        {
            "JACORB_HOME=" + jacorbHome
        };
        
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
            return rt.exec (cmd, envp);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public String getJacORBLibraryPath()
    {
        return getJacORBPath() + ":"
             + jacorbHome + "/lib/logkit-1.2.jar:"
             + jacorbHome + "/lib/avalon-framework-4.1.5.jar:"
             + jacorbHome + "/lib/backport-util-concurrent.jar:"
             + jacorbHome + "/lib/antlr-2.7.2.jar:"
             + jacorbHome + "/lib/picocontainer-1.2.jar";
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
                return result.toString() + ":"
                     + jacorbHome + "/classes:"
                     + jacorbHome + "/test/regression/lib/emma.jar";
        }
        result = new File (jacorbHome, "classes/org");
        if (result.exists())
            return new File (jacorbHome, "classes").toString();
        else
            return new File (jacorbHome, "lib/jacorb.jar").toString();
    }
}
