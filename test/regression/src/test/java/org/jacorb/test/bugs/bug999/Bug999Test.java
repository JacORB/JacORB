package org.jacorb.test.bugs.bug999;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

public final class Bug999Test extends ClientServerTestCase
{
    private static final String bigString;
    private static final Data[] bigData = new Data[2];

    private Hello server;

    static
    {
        String temp = "";
        for (int i = 0; i < 10000; i++)
        {
            temp += "1";
        }
        bigString = temp;

        int max = 10000;
        byte[] bytes = new byte[max];
        for (int i = 0; i < max; i++)
        {
            bytes[i] = 1;
        }

        bigData[0] = new Data();
        bigData[0].bytes = bytes;
        bigData[0].name = "first";
        bigData[1] = new Data();
        bigData[1].bytes = new byte[] { 2, 2 };
        bigData[1].name = "second";

    }

    @BeforeClass
    public static void beforeClassSetup() throws Exception
    {
        Properties props = new Properties();

        setup = new ClientServerSetup("org.jacorb.test.bugs.bug999.HelloImpl", props, props);
    }

    @Before
    public void beforeSetup() throws Exception
    {
        server = HelloHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testSayHello()
    {
        server.sayHello();
    }

    @Test
    public void testString()
    {
        server.inputString(bigString);
    }

    @Test
    public void testOctet()
    {
        server.inputData(bigData);
    }
}
