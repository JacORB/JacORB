package demo.miop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import org.omg.CORBA.INV_OBJREF;

/**
 * This is a simple MIOP based client that will multicast out a method
 * call to a group. It uses a default MIOP corbaloc URL of
 * "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234"
 * which is read from an IOR file named 'miop.ior'
 *
 * Parameters:
 * -fragment   This will cause the client to send an extra large message to induce fragmentation.
 * -ior [IOR/Corbaloc] optional parameter to read the IOR or corbaloc from, rather than from miop.ior
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com"></a>
 * @version 1.0
 */
public class Client
{
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception
   {
      String iorFile   = "miop.ior";
      Properties props = new Properties ();
      props.setProperty
         ("jacorb.transport.factories", "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
      props.setProperty
         ("jacorb.transport.client.selector", "org.jacorb.orb.miop.MIOPProfileSelector");
      String defaultParam = "Oneway call";
      String groupURL = null;

      for (int j=0 ; j<args.length; j++)
      {
         if(args[j].equals ("-fragment"))
         {
            StringBuilder sb = new StringBuilder (defaultParam);
            sb.append ("___");
            for (int i=0; i<1000; i++)
            {
               sb.append ("abcd");
            }
            defaultParam = sb.toString ();
         }
         else if (args[j].equals ("-ior"))
         {
            if (args.length < ++j)
            {
               System.err.println ("Insufficient parameters - missing ior URL");
               System.exit (-1);
            }
            groupURL = args[j];
         }
         else if (j == 0)
         {
            // Hidden option - override default IOR file of miop.ior with args[0]
            // for demo xml automated running.
            iorFile = args[0];
         }
      }


      if (groupURL == null)
      {
         BufferedReader reader = new BufferedReader (new FileReader(iorFile));
         groupURL = reader.readLine();
         reader.close();
      }

      org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,props);

      // Use an unchecked narrow so it doesn't do an is_a call remotely.
      GreetingService helloGroup = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(groupURL));

      System.out.println("### Sending a string of length " + defaultParam.length ());
      helloGroup.greeting_oneway(defaultParam);

      helloGroup.shutdown ();

      // A normal narrow should do a remote call. This will need the group IIOP profile which
      // may not have been transmitted so we do this part last.
      try
      {
         helloGroup = GreetingServiceHelper.narrow(orb.string_to_object(groupURL));
      }
      catch (INV_OBJREF e)
      {
         System.err.println ("Unable to narrow due to no Group IIOP Profile");
      }
   }
}
