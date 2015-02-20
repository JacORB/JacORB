package org.jacorb.demo.appserver;

import java.util.Properties;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.omg.CORBA.ORB;

/**
 * Simple client to manually call the posted IOR.
 */
public class Client
{
    public static void main(String args[]) throws Exception
    {
        String updateString, ior;

        if (args.length >= 1)
        {
            updateString = args[0];
        }
        else
        {
            updateString = UUID.randomUUID().toString();
        }

        // Grab the IOR from the servlet.
        Document doc = Jsoup.connect("http://localhost:8080/jacorb-appserver/PrintIOR").get();
        ior = doc.select("h1").first().text();

        System.out.println("Retrieved ior " + ior);

        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        orbProps.setProperty("jacorb.interop.null_string_encoding", "true");
        ORB orb = ORB.init(args, orbProps);

        org.omg.CORBA.Object obj = orb.string_to_object(ior);
        GoodDay goodDay = GoodDayHelper.narrow(obj);

        // Invoke remote server
        System.out.println("Retrieved initial string " + goodDay.get_string());
        goodDay.record_string(updateString);
        System.out.println("Retrieved string " + goodDay.get_string());
    }
}
