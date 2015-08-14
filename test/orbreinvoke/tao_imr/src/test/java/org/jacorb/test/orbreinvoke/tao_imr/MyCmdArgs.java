package org.jacorb.test.orbreinvoke.tao_imr;

import org.jacorb.test.listenendpoints.echo_corbaloc.*;

/**
 *
 * @author nguyenq
 */
public class MyCmdArgs extends CmdArgs {
    private String poaBaseName = null;

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

                if ("-poabasename".equals(cmd) || "--poabasename".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        String poaName = args[++i].trim();
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

            return true;

        }
        catch(Exception e)
        {
            throw new Exception (e.getMessage());
        }
    }

    public String getPoaBaseName()
    {
        return poaBaseName;
    }
}
