package org.jacorb.test.ir;

import java.io.File;
import java.io.IOException;

import org.jacorb.ir.IRServer;
import org.jacorb.test.common.TestUtils;

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

        File iorFile = new File(iorFileName);

        TestUtils.printServerIOR(iorFile);
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
