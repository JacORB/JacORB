package Map;

// Client.java

import Map.*;
import java.io.*;
import java.util.StringTokenizer;
import org.omg.CosNaming.*;


public class Client 
{

    static String[] stringToArray(String from, String separator) 
    {
        if (from == null) {
            return null;
        }
        if (separator == null) {
            separator = " ";
        }
        StringTokenizer toks = new StringTokenizer(from, separator);
        String[] result = new String[toks.countTokens()];
        int i = 0;
        while (toks.hasMoreTokens()) {
            result[i++] = toks.nextToken().trim();
        }
        return result;
    }

    static void printUsage() {
        System.out.println("USAGE INFORMATION");
        System.out.println("createbase <label>");
        System.out.println("createderived <label>");
        System.out.println("connect <label>");
        System.out.println("destroy <label>");
        System.out.println("get <label>");
        System.out.println("list <num copies of each>");
        System.out.println("quit");
    }

    public static void main(String[] args) 
    {
        try
        {
            // Initialize the ORB.
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

            NamingContextExt nc = 
                NamingContextExtHelper.narrow(
                         orb.resolve_initial_references( "NameService" ));

            org.omg.CORBA.Object o = 
                nc.resolve(nc.to_name("PointManager1.example"));

            PointManager pointManager = 
                PointManagerHelper.narrow( o );

            int x = 0;
            int y = -1000;

            BufferedReader reader = 
                new BufferedReader((new InputStreamReader(java.lang.System.in)));

            boolean done = false;

            java.lang.String lab = null;
            while (!done) 
            {
                System.out.print("Command-->");
                try 
                {
                    java.lang.String input = reader.readLine();

                    if (input.equals("")) {
                        Client.printUsage(); 
                        continue;
                    }

                    java.lang.String[] tokens = Client.stringToArray(input, null);

                    if (tokens == null) 
                    {
                        Client.printUsage(); 
                        continue;
                    }

                    lab = (tokens.length == 2) ? tokens[1] : null;

                    if (tokens[0].equals("createbase")) 
                    {
                        Point point = pointManager.create_point(x++, y++, lab, false);
                        if (point != null) 
                        {
                            point.print();
                        }
                        else 
                        {
                            System.out.println("Point is [nil]");
                        }
                    }
                    else if (tokens[0].equals("createderived")) 
                    {
                        Point point = pointManager.create_point(x++, y++, lab, true);
                        if (point != null) 
                        {
                            point.print();
                        }
                        else 
                        {
                            System.out.println("Point is [nil]");
                        }
                    }
                    else if (tokens[0].equals("connect")) 
                    {
                        System.out.print("Connect " + lab + " to how many points? ");
                        java.lang.String name = reader.readLine(); 
                        int num = Integer.parseInt(name);
                        String[] connectTo = new String[num];
                        for (int i = 0; i < num; i++) 
                        {
                            System.out.print("\tconnection #" + i + ": ");
                            connectTo[i] = reader.readLine();
                        } 
                        pointManager.connect_point(lab, connectTo);
                    }
                    else if (tokens[0].equals("destroy")) 
                    {
                        pointManager.destroy_point(lab);
                    }
                    else if (tokens[0].equals("get")) 
                    {
                        Point point = pointManager.get_point(lab);
                        if (point != null) 
                        {
                            point.print();
                        }
                        else 
                        {
                            System.out.println("Point is [nil]");
                        }
                    }
                    else if (tokens[0].equals("list")) 
                    {
                        Point[] points = 
                            pointManager.list_points( Integer.parseInt(lab) );

                        for (int i = 0; i < points.length; i++) 
                        {
                            if (points[i] != null) 
                            {
                                points[i].print();
                            }
                            else 
                            {
                                System.out.println("Point is [nil]");
                            }
                        }
                    }
                    else if (tokens[0].equals("quit")) 
                    {
                        System.out.println("quit");
                        done = true;
                    }
                    else 
                    {
                        Client.printUsage();
                    }
                }
                catch(InvalidPoint e) {
                    System.out.println("InvalidPoint exception caught");
                    Client.printUsage();
                }
                catch(java.io.IOException e) {
                    System.out.println(e);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }

    }

}
