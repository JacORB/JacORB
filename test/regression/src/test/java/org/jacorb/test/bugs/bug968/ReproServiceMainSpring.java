package org.jacorb.test.bugs.bug968;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ReproServiceMainSpring
{
    public static void main(String[] args) throws IOException
    {
        Properties props = System.getProperties();
        props.put( "org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB" );
        props.put( "org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton" );

        new ClassPathXmlApplicationContext(new String[] {"repro-service-context.xml"});

        File f = new File (ReproClientTest.IOR);

        while ( ! f.exists () )
        {
            try
            {
                Thread.sleep (1000);
            }
            catch (InterruptedException e)
            {
            }
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        String ior = br.readLine();
        br.close();
        System.out.println ("SERVER IOR: " + ior);

        while(true)
        {
        }
    }
}
