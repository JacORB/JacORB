package org.jacorb.orb.domain.gui;

import org.jacorb.orb.domain.*;
import javax.swing.UIManager;
import java.awt.*;
import java.io.*;
import org.jacorb.util.Debug;

/**
 * The startup class of the domain browser.
 * A domain browser manages domains, their members and policies.
 * The domain to display as root may be provided by command line arguments.
 * If the first command line argument reads "-f" then the next command line
 * argument is assumed to be the file name containing the IOR of the domain. 
 * Otherwise the first command line argument is assumed to be 
 * the IOR of the root domain.
 * <p>
 * @author Herbert Kiefer
 * @version 1.0
 */

public class Browser 
{
    boolean packFrame = false;
  
    //Construct the application  
    public Browser( SharedData data, Domain root) 
    {
        BrowserFrame frame;

        if (root == null)
            frame = new BrowserFrame(data);
        else 
            frame = new BrowserFrame(data, root);

        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) 
        {
            frame.pack();
        }
        else 
        {
            frame.validate();
        }

        //Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) 
        {
            frameSize.height = screenSize.height;
        }

        if (frameSize.width > screenSize.width) 
        {
            frameSize.width = screenSize.width;
        }
        // every new frame (except the first) gets moved by the offset values
        int xOffset= frameSize.width  / 5;
        int yOffset= frameSize.height / 5;
        frame.setLocation(((screenSize.width - frameSize.width) / 2) 
                          + (data.getFrameCount() - 1) * xOffset, 
                          ((screenSize.height - frameSize.height) / 2)
                          + (data.getFrameCount() - 1) * yOffset);
        frame.setVisible(true);
    }

    //Main method
    public static void main(String[] args) 
    {
        try 
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      
            // all browser share the same orb via the SharedData class
            SharedData sharedData = new SharedDataImpl();
            if ( args.length == 0 )
                new Browser( sharedData, null); // default root domain: domain browser
            else  
            { 
                // get domain to diplay as root in domain browser from 
                // command line args
                Domain root = null;
                org.omg.CORBA.ORB orb = sharedData.getORB();
                String iorString = readIORStringFromArguments(args);
                try
                {
                    root= DomainHelper.narrow( orb.string_to_object(iorString) );
                }
                catch (Exception e)
                {
                    Debug.output(0, e);
                    root= null; // fallback to domain server		
                    // usage();
                }
                new Browser( sharedData, root );
            }	
            Debug.output(Debug.DOMAIN | Debug.INFORMATION, "Domain Browser up.");
        }
        catch(Exception e) 
        {
            Debug.output(Debug.DOMAIN | Debug.IMPORTANT, e);
        }
    } // main

    private static String readIORStringFromArguments(String[] args)
    {
        Debug.myAssert(1,  args.length > 0, "Browser.readIORStringFromArguments: "
                     + "no arguments.");
        // take domain reference from args
        String iorString= null;
        if( args[0].equals("-f"))
        { // read IOR of domain from file
            String line= null;
            try
            {
                // System.out.println ( "arg.length: " + arg.length );
                // System.out.println ( "arg[ 0 ]: " + arg[ 0 ] );
                // System.out.println ( "reading IOR from file: " + arg[ 1 ] );
                BufferedReader br = new BufferedReader (new FileReader(args[1]),2048 );
                line = br.readLine();
                //		System.out.print ( line );
                if ( line != null ) 
                { 
                    iorString = line;
                    while ( line != null ) 
                    { 
                        line = br.readLine();
                        if ( line != null ) iorString = iorString + line;
                        // System.out.print ( line );
                    }
                }
                // System.out.println ( "red IOR from file:" );
            } 
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
                // System.exit(1);
            }
        }
        else
        {
            iorString = args[0];
        }
        return iorString;
    } // readIORStringFromArguments

    private static void usage()
    {
        System.err.println
            ("Usage: jaco org.jacorb.orb.domain.gui.Browser [<IOR> | -f <filename>]");
        System.exit(-1);
    } // usage
} // Browser





