package org.jacorb.test.ir;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.jacorb.ir.IRServer;
import org.jacorb.test.common.ClientServerSetup;

public class IRServerRunner
{
    public static void main(String[] args) throws Exception
    {
        final String iorFileName = getIORFileName();
        final String classpath = getClasspath();

        Thread thread = new Thread()
        {
            public void run() {
                try
                {
                    IRServer.main(new String[] {classpath, iorFileName});
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            };
        };

        thread.start();

        Thread.sleep(1000);

        File iorFile = new File(iorFileName);

        long timeout = System.currentTimeMillis() + ClientServerSetup.getTestTimeout();

        while(System.currentTimeMillis() < timeout && !(iorFile.canRead()))
        {
            Thread.sleep(1000);
        }

        LineNumberReader in = new LineNumberReader(new FileReader(iorFile));

        String ior = in.readLine();

        if (ior == null)
        {
            throw new IllegalArgumentException("could not read IOR within " + ClientServerSetup.getTestTimeout() + " ms");
        }

        System.out.println("SERVER IOR: " + ior);
    }

    private static String getClasspath()
    {
        String classpath = System.getProperty("jacorb.test.ir.classpath");
        if (classpath == null)
        {
            classpath = "/tmp";
        }
        return classpath;
    }

    private static String getIORFileName() throws IOException
    {
        String iorFileName = System.getProperty("jacorb.test.ir.iorfile");

        if (iorFileName == null)
        {
            File file = File.createTempFile("IR_IOR", ".ior");
            file.deleteOnExit();
            iorFileName = file.toString();
        }

        return iorFileName;
    }
}
