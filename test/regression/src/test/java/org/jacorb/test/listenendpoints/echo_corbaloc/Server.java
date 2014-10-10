
package org.jacorb.test.listenendpoints.echo_corbaloc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jacorb.orb.util.PrintIOR;
import org.jacorb.test.harness.FixedPortORBTestCase;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server extends FixedPortORBTestCase
{
   public static void main(String[] args)
   {
      try
      {
         CmdArgs cmdArgs = new CmdArgs("Server", args);
         Properties props = new Properties();
         props.setProperty("jacorb.implname", "EchoServer");
         props.setProperty("OAPort", Integer.toString(getNextAvailablePort()));
         String helloID = "EchoID";

         List<Endpoint> endpointList = null;
         try
         {
            endpointList = ListenEndpoints.getEndpointList(args);
         }
         catch (Exception e)
         {
             System.err.println("Got an exception in ListenEndpoints.getEndpointList(): " +  e.getMessage());
             System.exit(1);
         }

         //init ORB
         ORB orb = ORB.init(args, props);

         //init POA
         POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

         //init new POA
         Policy[] policies = new Policy[2];
         policies[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
         policies[1] = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

         POA helloPOAPersistent = rootPOA.create_POA
            ("EchoPOAP", rootPOA.the_POAManager(), policies);


         // Setup a second POA with a transient policy therebye producing a different corbaloc.
         policies = new Policy[3];
         policies[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
         policies[1] = rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.SYSTEM_ID);
         policies[2] = rootPOA.create_implicit_activation_policy (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

         POA helloPOATransient = rootPOA.create_POA
            ("EchoPOAT", rootPOA.the_POAManager(), policies);

         helloPOAPersistent.the_POAManager().activate();
         helloPOATransient.the_POAManager().activate();

         // create servant object
         EchoMessageImpl echoServant = new EchoMessageImpl("EchoPOA-Persistent");
         helloPOAPersistent.activate_object_with_id(helloID.getBytes(), echoServant);

         // Manually create a persistent based corbaloc.
         for (Iterator<Endpoint> x = endpointList.iterator(); x.hasNext();)
         {

             Endpoint ep = x.next();
             List<String> corbalocStrList = null;

             if (ep.getHostInetAddress() != null)
             {
                 corbalocStrList = buildCorbalocString(ep.getHostInetAddress(),
                                            ep.getProtocol(), ep.getPort(),
                                            props.getProperty("jacorb.implname"),
                                            helloPOAPersistent.the_name(),
                                            helloID);
             }
             else
             {
                 corbalocStrList = getCorbalocEndpoints(
                                            ep.getProtocol(), ep.getPort(),
                                            props.getProperty("jacorb.implname"),
                                            helloPOAPersistent.the_name(),
                                            helloID);
             }

              int n = 0;
              for (Iterator<String> xx = corbalocStrList.iterator(); xx.hasNext();)
              {
                   ++n;
                    String corbalocStr = xx.next();
                    if (!cmdArgs.getTestMode())
                    {
                        System.out.println("Server-Persistent corbaloc " + n + ": " + corbalocStr);
                    }
                    else
                    {
                        System.out.println("SERVER IOR: " + corbalocStr);
                    }

                    org.omg.CORBA.Object objP = orb.string_to_object(corbalocStr);
                    if (!cmdArgs.getTestMode())
                    {
                        System.out.println("Server-Persistent ior: " + orb.object_to_string (objP));
                    }
                    else if (cmdArgs.getTestType().equalsIgnoreCase("P") ||
                                cmdArgs.getTestType().equalsIgnoreCase("A"))
                    {
                        System.out.println("SERVER IOR: " + orb.object_to_string (objP));
                    }
                    System.out.flush();

                    if (cmdArgs.getIORFile() != null)
                    {
                        PrintWriter ps = new PrintWriter(new FileOutputStream(
                                new File( cmdArgs.getIORFile() + Integer.toString(n) ) + ".persistent"));
                        ps.println( orb.object_to_string( objP ) );
                        ps.close();
                    }

                    // Add an object key mapping to second server
                    // System.out.println("Adding object mapping for server 1 ior: " + orb.object_to_string (objP));
                    ((org.jacorb.orb.ORB)orb).addObjectKey ("VeryShortKey", orb.object_to_string (objP));
              }
         }

         // Setup second server
         org.omg.CORBA.Object objT = helloPOATransient.servant_to_reference(new EchoMessageImpl("EchoPOA-Transient"));

         // Use the PrintIOR utility function to extract a transient corbaloc string.
         String corbalocStr = PrintIOR.printCorbalocIOR (orb, orb.object_to_string(objT));
         if (!cmdArgs.getTestMode())
         {
             System.out.println("Server-Transient corbaloc: " + corbalocStr);
         }
         else if (cmdArgs.getTestType().equalsIgnoreCase("T") ||
                  cmdArgs.getTestType().equalsIgnoreCase("A"))
         {
             System.out.println("SERVER IOR: " + corbalocStr);
         }


        if (cmdArgs.getIORFile() != null)
        {
                PrintWriter ps = new PrintWriter(new FileOutputStream(new File( cmdArgs.getIORFile() ) + ".transient"));
                ps.println( corbalocStr );
                ps.close();
         }
        System.out.flush();

         // wait for requests
         orb.run();

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private static List<InetAddress> getInetAddressList() throws SocketException
   {
        List<InetAddress> inetList = new ArrayList<InetAddress>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(nets))
        {
            for ( InetAddress inetAddress : Collections.list(netint.getInetAddresses()) )
            {
                inetList.add(inetAddress);
            }

        }
        return inetList;
    }

    private static List<String> getCorbalocEndpoints(String protocol, int listen_port,
            String implName, String poaName, String objId) throws SocketException
    {
        try
        {
            List<InetAddress> inets = getInetAddressList();
            List<String> listen_eps = new ArrayList<String>();
            for (Iterator<InetAddress> x = inets.iterator(); x.hasNext();)
            {
                InetAddress inetAddr = x.next();
                String ipaddr = inetAddr.toString().substring(1);
                String conHostName = inetAddr.getCanonicalHostName();
                String hostName = inetAddr.getHostName();
                if (!inetAddr.isLoopbackAddress() && !inetAddr.isLinkLocalAddress())
                {
                    if (inetAddr instanceof Inet4Address)
                    {
                        String s = new String (
                                "corbaloc:" + protocol + ":" + ipaddr + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId);
                        listen_eps.add(s);
                        if (!hostName.equals(ipaddr))
                        {
                            listen_eps.add(new String ("corbaloc:" + protocol + ":" + hostName + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            listen_eps.add(new String ("corbaloc:" + protocol + ":"  + hostName + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }

                    }
                    else if (inetAddr instanceof Inet6Address)
                    {
                        String ipv6 = ipaddr;
                        int zoneid_delim = ipv6.indexOf('%');
                        if (zoneid_delim > 0)
                        {
                            ipv6 = ipv6.substring(0, zoneid_delim);
                        }
                        if (!ipv6.startsWith("fe80"))
                        {
                            listen_eps.add(new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }

                        if (!hostName.equals(ipaddr))
                        {
                            zoneid_delim = hostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = hostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add(new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            zoneid_delim = conHostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = conHostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add(new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                    }
                }
            }

            return listen_eps;

        }
        catch (SocketException e)
        {
            throw new SocketException (e.getMessage());
        }
    }

    private static List<String> buildCorbalocString(InetAddress inetAddr,
                         String protocol, int listen_port,
                         String implName, String poaName, String objId)
            throws SocketException
    {
                List<String> listen_eps = new ArrayList<String>();
                String ipaddr = inetAddr.toString().substring(1);
                String conHostName = inetAddr.getCanonicalHostName();
                String hostName = inetAddr.getHostName();
                // if (!inetAddr.isLoopbackAddress() && !inetAddr.isLinkLocalAddress())
                if (!inetAddr.isLoopbackAddress())
                {
                    if (inetAddr instanceof Inet4Address)
                    {
                        String s = new String (
                                "corbaloc:" + protocol + ":" + ipaddr + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId);
                        listen_eps.add (s);
                        if (!hostName.equals(ipaddr))
                        {
                            listen_eps.add (new String ("corbaloc:" + protocol + ":" + hostName + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            listen_eps.add (new String ("corbaloc:" + protocol + ":"  + hostName + ":" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }

                    }
                    else if (inetAddr instanceof Inet6Address)
                    {
                        String ipv6 = ipaddr;
                        int zoneid_delim = ipv6.indexOf('%');
                        if (zoneid_delim > 0)
                        {
                            ipv6 = ipv6.substring(0, zoneid_delim);
                        }
                        if (!ipv6.startsWith("fe80"))
                        {
                            listen_eps.add (new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }

                        if (!hostName.equals(ipaddr))
                        {
                            zoneid_delim = hostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = hostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add (new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                        if (!conHostName.equals(ipaddr) && !conHostName.equals(hostName))
                        {
                            zoneid_delim = conHostName.indexOf('%');
                            if (zoneid_delim > 0)
                            {
                                ipv6 = conHostName.substring(0, zoneid_delim);
                            }
                            listen_eps.add (new String ("corbaloc:" + protocol + ":"  + "[" + ipv6 + "]:" + listen_port
                                + "/" + implName + "/" + poaName + "/" + objId));
                        }
                    }
                }

                return listen_eps;
        }
}
