package org.jacorb.test.orb.orbreinvoke;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.jacorb.test.listenendpoints.echo_corbaloc.CmdArgs;


/**
 *
 * @author nguyenq
 */
public class MyCmdArgs extends CmdArgs {
    private boolean useNameService = false;
    private String nsCorbaName = null;
    private String nsIorString = null;

    public MyCmdArgs (String appName, String[] args) throws Exception
    {
        super(appName, args);
        args = super.getCmdArgs();
    }

    public boolean processArgs() throws Exception
    {
        if (!super.processArgs())
        {
            return false;
        }

        try
        {
            for(int i=0; i < args.length; i++) {
                if (args[i] == null) {
                    continue;
                }

                String cmd = args[i].trim();

                if ("-nsiorfile".equals(cmd) || "--nsiorfile".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        String nsIorFile = args[++i].trim();
                        // System.out.println("Got " + cmd + ": " + iorFile);
                        File f = new File( nsIorFile);
                        if ("Client".equals(appName) &&
                                (! f.exists() || f.isDirectory()))
                        {
                            System.err.println("File " + nsIorFile +
                                               " does not exist or is a directory.");
                            return false;
                        }
                        if ("Client".equals(appName))
                        {
                            BufferedReader br =
                                    new BufferedReader( new FileReader( f ));
                            nsIorString = br.readLine();
                            br.close();
                        }
                        continue;
                    }
                    else
                    {
                        System.err.println("Commandline argument " + cmd + " <value> is missing the value");
                        help();
                        return false;
                    }
                }
                if ("-nsiorstr".equals(cmd) || "--nsiorstr".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        nsIorString = args[++i].trim();
                        // System.out.println("Got " + cmd + ": " + iorString);
                        continue;
                    }
                    else
                    {
                        System.err.println("Commandline argument " + cmd + " <value> is missing the value");
                        help();
                        return false;
                    }
                }
                if("-nscorbaname".equals(cmd) || "--nscorbaname".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        nsCorbaName = args[++i].trim();
                        // System.out.println("Got " + cmd + ": " + corbalocString);
                        continue;
                    }
                    else
                    {
                        System.err.println("Commandline argument " + cmd + " <value> is missing the value");
                        help();
                        return false;
                    }
                }
            }

            if (nsCorbaName != null || nsIorString != null)
            {
                useNameService = true;
            }

            return true;

        }
        catch(Exception e)
        {
            throw new Exception (e.getMessage());
        }
    }

    public String getNsCorbaName()
    {
        return nsCorbaName;
    }

    public String getNsIorString()
    {
        return nsIorString;
    }

    public boolean useNameService()
    {
        return useNameService;
    }
}
