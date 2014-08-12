package org.jacorb.test.bugs.bug968;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.jacorb.test.harness.ServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import cxf.repro.ReproData;
import cxf.repro.ReproDatas;
import cxf.repro.ReproService;


public class ReproClientTest
{
    /**
     * Fixed, unique IOR name (also defined in xml).
     */
    static final String IOR = "/tmp/cxf-2B5EC0BB-B331-4F7D-AF69-BAE409982399.ior";

    private static ServerSetup serverSetUp;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Assume.assumeFalse(TestUtils.isSSLEnabled);
        // Hardcoded ior file in code *and* XML to /tmp/.... so exclude Windows systems.
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));

        serverSetUp = new ServerSetup ("org.jacorb.test.bugs.bug968.ReproServiceMainSpring",
                "ReproServiceMainSpring", null);

        Properties props = System.getProperties();
        props.put( "org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB" );
        props.put( "org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton" );

        serverSetUp.setUp();
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception
    {
        if (serverSetUp != null)
        {
            serverSetUp.tearDown();
        }
        new File (IOR).delete();
    }

    @Test
    public void testCXFAny() throws Exception
    {
        QName SERVICE_NAME = new QName("http://cxf.apache.org/bindings/corba/idl/repro",  "repro.ServiceCORBAService");

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        /*
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
         */
        factory.setServiceClass( ReproService.class );
        factory.setWsdlLocation( "classpath:repro.wsdl" );
        factory.setAddress( "file://" + IOR );
        factory.setServiceName( SERVICE_NAME );
        ReproService port = (ReproService) factory.create();

        ReproData in = new ReproData();
        in.setABool( true );
        in.setALong( 42 );
        in.setAString( "-" );

        // this call succeeds
        int out = port.works( in );
        assertTrue ("Value should be 42", out == 42);

        ReproDatas outs = port.failsEmpty( in );
        assertFalse ("Value should not be empty", outs.getItem().isEmpty());

        for( ReproData d : outs.getItem()) {
        	TestUtils.getLogger().debug( "===> [" + d.isABool() + ":" + d.getALong() + ":" + d.getAString() + "]");
        }

        // this call fails with a null pointer exception during unmarshal
        // (the service gets called correctly, but the client chokes on result data)
        ReproData d = port.failsCrash( in );
        assertTrue (d.isABool() == true);
       	TestUtils.getLogger().debug( "===> [" + d.isABool() + ":" + d.getALong() + ":" + d.getAString() + "]");
    }
}
