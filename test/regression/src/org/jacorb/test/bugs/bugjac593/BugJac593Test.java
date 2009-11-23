package org.jacorb.test.bugs.bugjac593;


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


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;


/**
 * <code>BugJac593Test</code>
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac593Test extends ORBTestCase
{
    /**
     * <code>testInitORBSingleton</code> verifies that creating an ORBSingleton
     * works correctly.
     *
     * @exception Exception if an error occurs
     */
    public void testInitORBSingleton() throws Exception
    {
        String classpath = System.getProperty("java.class.path");
        String bootclasspath = System.getProperty("sun.boot.class.path");

        bootclasspath = replaceAll (bootclasspath, "lib/jacorb.jar", "lib/dummy.jar");
        bootclasspath = replaceAll (bootclasspath, "JacORB/classes", "JacORB/dummy-directory");

        Runtime rt = Runtime.getRuntime();

        String []cmd = new String []
        {
            "java",
            //"-Xbootclasspath:" + bootclasspath,
            "-classpath",
            classpath,
            //"-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton",
            "org.jacorb.test.bugs.bugjac593.BugJac593Test"
        };

        //System.out.println ("About to exec " + Arrays.toString (cmd));

        Process proc = rt.exec (cmd, new String[0]);
        int exitValue = -1;

        try
        {
            InputStream inputStream = proc.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = null;
            StringBuffer buffer = new StringBuffer();
            while((line = reader.readLine()) != null)
            {
                buffer.append(line);
                buffer.append('\n');
            }

            System.out.println ("ServerOut: " + buffer.toString());
        }
        finally
        {
            proc.waitFor();
            exitValue = proc.exitValue();
            proc.destroy();
        }
        if (exitValue != 0)
        {
            fail ("Exit value was not zero for BugJac593");
        }
    }


    public static void main (String args[]) throws Exception
    {
        // This block is only for manual running.
        if ( args.length > 0 )
        {
            new BugJac593Test().testInitORBSingleton();
            System.exit(1);
        }

        try
        {
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
            ORB.init(new String[0], props);
            System.err.println ("Created an ORB");
        }
        catch (ClassCastException e)
        {
            System.err.println ("Caught " + e);
            System.exit (-1);
        }
        System.exit (0);
    }


    /*
     * Private replaceAll as replaceAll is not available in CVM version
     */
    private static String replaceAll
    (
        String source,
        String toReplace,
        String replacement
    )
    {
        int idx = source.lastIndexOf( toReplace );

        if ( idx != -1 )
        {
            StringBuffer ret = new StringBuffer( source );
            ret.replace( idx, idx+toReplace.length(), replacement );

            while ( (idx=source.lastIndexOf(toReplace, idx-1)) != -1 )
            {
                ret.replace( idx, idx+toReplace.length(), replacement );
            }
            source = ret.toString();
        }

        return source;
    }
}
