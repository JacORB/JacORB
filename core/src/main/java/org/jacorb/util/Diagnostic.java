package org.jacorb.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;
import org.jacorb.orb.ORBSingleton;
import org.jacorb.orb.iiop.IIOPAddress;
import org.omg.CORBA.ORB;


/**
 * JacORB Diagnostic class. This will print to System.out the following information:
 * <br>
 * <ul>
 * <li>JacORB Version</li>
 * <li>Operating System</li>
 * <li>OS Version</li>
 * <li>OS Architecture</li>
 * <li>Java Vendor</li>
 * <li>Java Version</li>
 * <li>Default network interface</li>
 * <li>Preferred network interface</li>
 * <li>Locale</li>
 * <li>System File Encoding</li>
 * <li>Cannonical Encoding</li>
 * <li>Default WChar Encoding</li>
 * </ul>
 * <br>
 * Remember the precedence levels of LC_ALL, LANG, LC_CTYPE etc. Preferred
 * way to override for *all* categories is to set LC_ALL. If you just set LANG
 * then if any other LC_* categories are set then these will take precedence.
 *
 */
public class Diagnostic
{
    public static void main (String args[])
    {
        Properties props = System.getProperties ();

        Properties p = new Properties();
        p.setProperty("org.omg.CORBA.ORBClass","org.jacorb.orb.ORB");
        p.setProperty("org.omg.CORBA.ORBSingletonClass","org.jacorb.orb.ORBSingleton");
        ORB orb = ORB.init(args, p);

        Locale l = Locale.getDefault();
        String defaultIOEncoding = (new OutputStreamWriter(new ByteArrayOutputStream ())).getEncoding();

        System.out.println("JacORB Version: " + org.jacorb.util.Version.versionInfo);
        System.out.println();

        System.out.println("Operating system name: " + props.get("os.name"));
        System.out.println("Operating system version: " + props.get("os.version"));
        System.out.println("Operating system architecture: " + props.get("os.arch"));
        System.out.println("Java Vendor: " + props.get("java.vm.vendor"));
        System.out.println("Java Version: " + props.get("java.version"));
        System.out.println("Runtime max memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "M");
        System.out.println();


        System.out.println("Found the following network addresses: ");

        for (InetAddress addr : IIOPAddress.getNetworkInetAddresses())
        {
            System.out.println("    " + addr.toString() + " / " + addr.getHostName());
        }
        System.out.println("Preferred non-loopback address " + IIOPAddress.getLocalHost());
        try
        {
            System.out.println("Preferred Java InetAddress address " + InetAddress.getLocalHost());
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        System.out.println();


        System.out.println("Default locale " + l + " (which is " + l.getDisplayName() + ')');
        System.out.println("System file encoding property: " + System.getProperty("file.encoding"));
        System.out.println("Cannonical encoding: " + defaultIOEncoding);
        System.out.println("Default WChar encoding: " + ((org.jacorb.orb.ORB)orb).getTCSWDefault().getName());
        System.out.println();

        System.out.println ("Created ORB " + orb.getClass().getName());

        // Trivial check to ensure standard startup still leads to a JacORB singleton.
        try
        {
            if ( ! ( ORB.init() instanceof ORBSingleton))
            {
                System.out.println ("Default Singleton ORB is not a JacORB singleton. This is not recommended as it *could* lead to classpath/classloader/stub conflicts.");
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
