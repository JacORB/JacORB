package multiprof;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CORBA.*;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class NSTest
{
    public NamingContextExt ctx;
    public Process primary;
    public Process backup;
    public ProcessBuilder primaryCmd;
    public ProcessBuilder backupCmd;
    public String aceRoot;
    public String taoRoot;
    public String nsPersist;
    public String grpPersist;
    public String nsIorFile;
    public String nsIor;
    public String primaryIorFile;
    public String backupIorFile;

    public void retry_resolve (String name, boolean retry)
    {
        String iteration = retry ? "First" : "Second";
        try
        {
            org.omg.CORBA.Object obj = ctx.resolve_str (name);
            System.out.println ("Resolve " + name + " worked on " + iteration + " try.");
        }
        catch (org.omg.CosNaming.NamingContextPackage.NotFound nfe)
        {
            System.out.println (iteration + " resolve threw a not found exception");
        }
        catch (org.omg.CosNaming.NamingContextPackage.CannotProceed cpe)
        {
            System.out.println (iteration + " resolve threw a cannot proceed exception");
        }
        catch (org.omg.CosNaming.NamingContextPackage.InvalidName ine)
        {
            System.out.println (iteration + " resolve threw an invalid name exception");
        }
        catch (org.omg.CORBA.SystemException ex)
        {
            System.out.println (iteration + " resolve threw " + ex);
            if (retry)
            {
                retry_resolve (name, false);
            }
        }
    }

    public void purgeFiles (File root)
    {
        if (root.isDirectory ())
        {
            File f[] = root.listFiles ();
            for (int i = 0; i < f.length; i++)
            {
                String n = f[i].getName ();
                if (n.equals (".") || n.equals (".."))
                {
                    continue;
                }
                if (f[i].isDirectory ())
                {
                    purgeFiles (f[i]);
                }
                f[i].delete ();
            }
        }
        root.delete ();
    }

    public void initServer ()
    {
        taoRoot = System.getenv ("TAO_ROOT");
        aceRoot = System.getenv ("ACE_ROOT");
        nsPersist = "nserv";
        grpPersist = "gserv";
        nsIorFile = "ns.ior";
        nsIor = "file://ns.ior";
        primaryIorFile = nsPersist + File.separator + "ns_replica_primary.ior";
        backupIorFile = nsPersist + File.separator + "ns_replica_backup.ior";

        File dir = new File (nsPersist);
        if (dir.exists ())
        {
            purgeFiles (dir);
        }
        dir.mkdir ();
        dir = new File (grpPersist);
        if (dir.exists ())
        {
            purgeFiles (dir);
        }
        dir.mkdir ();

        System.out.println ("Starting primary TAO NameService");
        System.out.println ("waiting for " + primaryIorFile );
        primaryCmd =
            new ProcessBuilder (taoRoot + "/orbsvcs/FT_Naming_Service/tao_ft_naming",
                                "--primary",
                                "-ORBListenEndPoints", "iiop://127.0.0.1:37107",
                                "-ORBDebugLevel", "10",
                                "-ORBLogFile", "ns_primary.log",
                                "-r", nsPersist,
                                "-v", grpPersist);
        startServer (1);
        System.out.println ("Starting backup TAO NameService");
        backupCmd =
            new ProcessBuilder (taoRoot + "/orbsvcs/FT_Naming_Service/tao_ft_naming",
                                "--backup",
                                "-ORBListenEndPoints", "iiop://127.0.0.1:37110",
                                "-ORBDebugLevel", "10",
                                "-ORBLogFile", "ns_backup.log",
                                "-c", nsIorFile,
                                "-r", nsPersist,
                                "-v", grpPersist);
        startServer (2);
        File f = new File (nsIorFile);
        try
        {
            for (int retries = 0; !f.exists () && retries < 5; retries ++)
            {
                Thread.currentThread ().sleep (1000);
            }
        }
        catch (Exception ex)
        {
            System.out.println ("Caught starting backup " + ex);
        }

        try
        {
            System.out.println ("invoking tao_nsadd --ns " + nsIor + " --name test1 --ior " + nsIor);
            ProcessBuilder util = new ProcessBuilder ("tao_nsadd",
                                                      "--ns", nsIor,
                                                      "--name", "test1",
                                                      "--ior", nsIor);
            Process p = util.start ();
            p.waitFor ();

            List<String> args = util.command ();
            args.set (4, "test2");
            System.out.println ("invoking tao_nsadd --ns " + nsIor + " --name test2 --ior " + nsIor);

            p = util.start ();
            p.waitFor ();
        }
        catch (Exception ex)
        {
            System.out.println ("Caught populating ns " + ex);
        }
    }

    public void cleanup ()
    {
        kill (0);
        purgeFiles (new File (nsPersist));
        purgeFiles (new File (grpPersist));
        File f = new File (nsIorFile);
        f.delete ();
    }

    public void startServer (int which)
    {
        try
        {
            File f;
            if (which == 1)
            {
                primary = primaryCmd.start ();
                f = new File (primaryIorFile);
            }
            else
            {
                backup = backupCmd.start ();
                f = new File (backupIorFile);
            }
            for (int retries = 0; !f.exists () && retries < 10; retries ++)
            {
                Thread.currentThread ().sleep (1000);
            }
            if (!f.exists ())
            {
                System.out.println ("Failed to start ns #" + which);
            }
            else
            {
                System.out.println ("Started ns #" + which);
            }
        }
        catch (Exception ex)
        {
            System.out.println ("Caught starting ns #" + which + " " + ex);
        }
    }

    public void kill (int which)
    {
        if (which != 2)
        {
            primary.destroy ();
            File f = new File (primaryIorFile);
            f.delete ();
            System.out.println ("Killed ns #1");
        }
        if (which != 1)
        {
            backup.destroy ();
            File f = new File (backupIorFile);
            f.delete ();
            System.out.println ("Killed ns #2");
        }
    }

    public static void main( String args[] )
    {
        NSTest nstest = new NSTest ();
        nstest.initServer ();

        try
        {
            Properties props = new Properties();
            props.setProperty ("ORBInitRef.NameService",nstest.nsIor);
            props.setProperty ("jacorb.log.default.verbosity","2");

            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props );

            System.out.println ("invoking narrow");
            nstest.ctx = NamingContextExtHelper.narrow
                (orb.resolve_initial_references ("NameService"));

            System.out.println ("resolve 1");
            nstest.retry_resolve ("test1", true);
            nstest.kill (1);
            Thread.currentThread ().sleep (5000);
            System.out.println ("resolve 2");
            nstest.retry_resolve ("test2", true);
            nstest.startServer (1);
            nstest.kill (2);
            Thread.currentThread ().sleep (5000);
            System.out.println ("resolve 3");
            nstest.retry_resolve ("test1", true);
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
        nstest.cleanup ();
  }
}

