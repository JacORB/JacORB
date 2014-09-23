package org.jacorb.test.listenendpoints.echo_corbaloc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;


public class CmdArgs {
    protected final static int DEFAULT_DELAY = 1000;
    protected final static int DEFAULT_NTIMES = -1;
    protected final String defaultMsg = new String(InetAddress.getLocalHost() +
                                        " is hailing server!");

    protected String appName = "AppName";
    protected String[] args = null;
    protected String echoMsg = null;
    protected String iorFile = null;
    protected String iorString = null;
    protected String corbalocString = null;
    protected int delayMilliSec = DEFAULT_DELAY;
    protected int ntimes = DEFAULT_NTIMES;
    protected int nthreads = 1;
    protected boolean loop = false;
    protected boolean verbose = false;
    protected boolean testMode = false;
    protected String testType = "A";

    public CmdArgs (String appName, String[] args) throws Exception
    {
        this.args = args;
        this.appName = appName;
        if (appName == null)
        {
            this.appName = "AppName";
        }
        processArgs();
    }

    public boolean processArgs() throws Exception
    {
        try
        {
            if (args == null || args.length < 1)
            {
                return false;
            }

            for(int i=0; i < args.length; i++) {
                if (args[i] == null) {
                    continue;
                }

                String cmd = args[i].trim();

                if("-help".equals(cmd) || "--help".equals(cmd)) {
                    help();
                    return false;
                }

                if ("-iorfile".equals(cmd) || "--iorfile".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        iorFile = args[++i].trim();
                        // System.out.println("Got " + cmd + ": " + iorFile);
                        File f = new File( iorFile);
                        if ("Client".equals(appName) &&
                                (! f.exists() || f.isDirectory()))
                        {
                            System.err.println("File " + iorFile +
                                               " does not exist or is a directory.");
                            return false;
                        }
                        if ("Client".equals(appName))
                        {
                            BufferedReader br =
                                    new BufferedReader( new FileReader( f ));
                            iorString = br.readLine();
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
                if ("-iorstr".equals(cmd) || "--iorstr".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        iorString = args[++i].trim();
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
                if("-corbaloc".equals(cmd) || "--corbaloc".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        corbalocString = args[++i].trim();
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
                if("-msg".equals(cmd) || "--msg".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        echoMsg = args[++i];
                        // System.out.println("Got " + cmd + ": " + echoMsg);
                        if (echoMsg.length() == 0)
                        {
                            echoMsg = defaultMsg;
                        }
                        continue;
                    }

                }
                if("-delay".equals(cmd) || "--delay".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        delayMilliSec = Integer.parseInt(args[++i]);
                        System.out.println("Got " + cmd + ": " + delayMilliSec + " mSec");

                        continue;
                    }

                }
                if("-ntimes".equals(cmd) || "--ntimes".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        ntimes = Integer.parseInt(args[++i]);
                        // System.out.println("Got " + cmd + ": " + ntimes);

                        continue;
                    }

                }
                if ("-nthreads".equals(cmd) || "--nthreads".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        nthreads = Integer.parseInt(args[++i]);
                        if (nthreads <= 0) {
                            nthreads = 1;
                        }
                        //System.out.println("Got " + cmd + ": " + nthreads);

                        continue;
                    }

                }
                if ("-loop".equals(cmd) || "--loop".equals(cmd)) {
                    loop = true;
                    // System.out.println("Got " + cmd + ": " + Boolean.toString(loop));
                    continue;
                }
                if ("-testmode".equals(cmd) || "--testmode".equals(cmd)) {
                    testMode = true;
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        testType = args[++i].trim();
                        // System.out.println("Got " + cmd + ": " + corbalocString);
                        if (!"P".equalsIgnoreCase(testType) && !"T".equalsIgnoreCase(testType)) {
                            System.err.println("Commandline argument " + cmd + " <value> maybe P (permanent) or T (transient)");
                        }
                        continue;
                    }
                    else
                    {
                        System.err.println("Commandline argument " + cmd + " <value> is missing the value");
                        // help();
                        return false;
                    }

                    // System.out.println("Got " + cmd + ": " + Boolean.toString(loop));
                }
                if ("-verbose".equals(cmd) || "--verbose".equals(cmd)) {
                    verbose = true;
                    // System.out.println("Got " + cmd + ": " + Boolean.toString(loop));
                    continue;
                }

            }

            if (delayMilliSec <= 0)
            {
                delayMilliSec = DEFAULT_DELAY;
            }

            return true;

        }
        catch(Exception e)
        {
            throw new Exception (e.getMessage());
        }


    }

    public void help() {
        System.out.println(appName + " [-help]");
        System.out.println(appName + " <-iorfile filename> [-msg quoted message] [-delay mSec] [-ntimes ntimes] [-nthreads nthreads] [-loop]");
        System.out.println(appName + " <-iorstr IOR string> [-msg quoted message] [-delay mSec] [-ntimes ntimes] [-nthreads nthreads] [-loop]");
        System.out.println(appName + " <-corbaloc corbaloc string> <-msg quoted message>  [-delay mSec] [-ntimes ntimes] [-nthreads nthreads] [-loop]");
    }

    public String[] getCmdArgs()
    {
        return args;
    }

    public String getIORString()
    {
        return iorString;
    }

    public String getIORFile()
    {
        return iorFile;
    }

    public String getCorbalocString()
    {
        return corbalocString;
    }

    public int getDelay()
    {
        return delayMilliSec;
    }

    public String getEchoMsg()
    {
        if (echoMsg != null)
        {
            return echoMsg;
        }
        else
        {
            return defaultMsg;
        }
    }

    public int getnThreads()
    {
        return nthreads;
    }

    public int getnTimes()
    {
        return ntimes;
    }

     public boolean getLoop()
    {
        return loop;
    }

    public boolean getTestMode()
    {
        return testMode;
    }

    public String getTestType()
    {
        return testType;
    }

    public void show()
    {
        System.out.println("CmdArgs.appName: <" + (appName==null? "is null" : appName) + ">");
        System.out.println("CmdArgs.echoMsg: <" + (echoMsg==null? "is null"  : echoMsg) + ">");
        System.out.println("CmdArgs.iorFile: <" + (iorFile == null? "is null" : iorFile) + ">");
        System.out.println("CmdArgs.iorString: <" + (iorString==null? "is null" : iorString) + ">");
        System.out.println("CmdArgs.corbalocString: <" + (corbalocString==null? "is null" : corbalocString) + ">");
        System.out.println("CmdArgs.delayMilliSec: <" + delayMilliSec + ">");
        System.out.println("CmdArgs.ntimes: <" + ntimes + ">");
        System.out.println("CmdArgs.nthreads: <" + nthreads + ">");
        System.out.println("CmdArgs.loop: <" + loop + ">");
    }

}
