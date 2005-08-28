package org.jacorb.test.common.launch;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import org.jacorb.test.common.*;

/**
 * A special JacORBLauncher that launches a TAO-based program that corresponds
 * to a given JacORB program.  If the unqualified name of the JacORB main class
 * is "SomeProgram" or "SomeProgramImpl", then there must be a corresponding
 * TAO implementation of it under TEST_HOME/tao/SomeProgram/server.  The
 * launcher will then start the "server" binary instead of the JacORB-based
 * implementation.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class TAOLauncher extends JacORBLauncher
{

    public TAOLauncher (String home, boolean coverage)
    {
        super (home, coverage);
    }

    private static Pattern classNamePattern 
      = Pattern.compile ("\\.([^\\.]+?)(?:Impl)?$");
    
    public Process launch(String classpath,
                          Properties props,
                          String mainClass,
                          String[] args)
    {
        try
        {
            Matcher m = classNamePattern.matcher (args[0]);
            if (!m.find())
                throw new RuntimeException ("cannot parse classname: " + args[0]);
            String className = m.group(1);
            String program = TestUtils.testHome() + "/tao/" + className + "/server";
            return Runtime.getRuntime().exec
            (
                program,
                new String[] { "LD_LIBRARY_PATH=/home/spiegel/projects/jacorb/ACE_wrappers/lib" }
            );
        }
        catch (IOException ex)
        {
            throw new RuntimeException (ex);
        }
    }

}
