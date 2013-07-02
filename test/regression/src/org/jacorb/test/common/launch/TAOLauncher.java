package org.jacorb.test.common.launch;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jacorb.test.common.TestUtils;

/**
 * A special JacORBLauncher that launches a TAO-based program that corresponds
 * to a given JacORB program.  If the unqualified name of the JacORB main class
 * is "SomeProgram" or "SomeProgramImpl", then there must be a corresponding
 * TAO implementation of it under TEST_HOME/tao/SomeProgram/server.  The
 * launcher will then start the "server" binary instead of the JacORB-based
 * implementation.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class TAOLauncher extends AbstractLauncher
{
    private static final Pattern classNamePattern = Pattern.compile ("\\.([^\\.]+?)(?:Impl)?$");
    private String program;

    public void init()
    {
        final Matcher m = classNamePattern.matcher(args[0]);

        if (!m.find())
        {
            throw new RuntimeException ("cannot parse classname: " + args[0]);
        }

        String className = m.group(1);
        program = TestUtils.testHome() + "/tao/" + className + "/server";
    }

    public String getCommand()
    {
        return program;
    }

    public Process launch()
    {
        try
        {

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
