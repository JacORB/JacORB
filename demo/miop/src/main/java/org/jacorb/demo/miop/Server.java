package demo.miop;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import org.jacorb.orb.util.CorbaLoc;
import org.omg.PortableGroup.GOA;
import org.omg.PortableGroup.GOAHelper;

/**
 * This is simple MIOP based server that will instantiate a Hello Servant and
 * associate it with a group. It uses a default MIOP corbaloc URL of
 * "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234"
 * which is written out to an IOR file named 'miop.ior'
 *
 * Parameters:
 *    -noGroupProfile  This not write out a corbaloc ior with a group iiop profile
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com"></a>
 * @version 1.0
 */
public class Server
{
   static org.omg.CORBA.ORB  orb;


   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception
   {
      String iorFile   = "miop.ior";
      String miopURL   = "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234";
      Properties props = new Properties ();
      props.setProperty
         ("jacorb.transport.factories", "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
      props.setProperty
         ("jacorb.transport.client.selector", "org.jacorb.orb.miop.MIOPProfileSelector");
      boolean writeGroupProfile = true;


      if (args.length > 0 && args[0].equals ("-noGroupProfile"))
      {
         writeGroupProfile = false;
      }
      else if (args.length > 0)
      {
         // Hidden option - used only by the demo ant scripts to automate the demo.
         iorFile = args[0];
      }


      orb = org.omg.CORBA.ORB.init(args, props);
//      org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, props);

      GreetingService helloGroup = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(miopURL));

      org.omg.PortableServer.POA poa = org.omg.PortableServer.POAHelper.narrow
         (orb.resolve_initial_references("RootPOA"));

      poa.the_POAManager().activate();
      GOA goa = GOAHelper.narrow(poa);

      GreetingImpl helloServant = new GreetingImpl();

      byte[] oid = poa.activate_object(helloServant);
      goa.associate_reference_with_id(helloGroup,oid);

      String gURL = miopURL;
      if (writeGroupProfile)
      {
         gURL = gURL + ";" + CorbaLoc.generateCorbaloc (orb, helloServant._this());
      }

      //writes the group URL in a specified file
      PrintWriter writer = new PrintWriter(new FileWriter(iorFile));
      writer.println(gURL);
      writer.close();

      System.err.println ("Corbaloc: " + gURL);

      if (args.length == 2)
      {
         // Hidden option - used only by the demo ant scripts to automate the demo.
         File killFile = new File(args[1]);
         while(!killFile.exists())
         {
            System.err.println ("Waiting for killfile");
            Thread.sleep (30000);
         }
         orb.shutdown(true);
      }
      else
      {
         orb.run();
      }
   }
}
