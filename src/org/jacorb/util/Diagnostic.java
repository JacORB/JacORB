package org.jacorb.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.iiop.IIOPAddress;

/**
 * JacORB Diagnostic class. This will print to System.out the following information:
 * <br/>
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
 *<br/>
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
        Locale l = Locale.getDefault();
        String defaultIOEncoding = (new OutputStreamWriter(new ByteArrayOutputStream ())).getEncoding();

        System.out.println("JacORB Version: " + org.jacorb.util.Version.versionInfo);
        System.out.println();

        System.out.println("Operating system name: " + props.get("os.name"));
        System.out.println("Operating system version: " + props.get("os.version"));
        System.out.println("Operating system architecture: " + props.get("os.arch"));
        System.out.println("Java Vendor: " + props.get("java.vm.vendor"));
        System.out.println("Java Version: " + props.get("java.version"));
        System.out.println();

        try
        {
            System.out.println("Default local host address " + InetAddress.getLocalHost());
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        System.out.println("Preferred non-loopback address " + IIOPAddress.getLocalHost());
        System.out.println();


        System.out.println("Default locale " + l + " (which is " + l.getDisplayName() + ')');
        System.out.println("System file encoding property: " + System.getProperty("file.encoding"));
        System.out.println("Cannonical encoding: " + defaultIOEncoding);
        System.out.println("Default WChar encoding: " + CodeSet.getTCSWDefault().getName());
        System.out.println();
    }
}
