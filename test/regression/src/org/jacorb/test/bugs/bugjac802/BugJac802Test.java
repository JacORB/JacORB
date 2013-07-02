package org.jacorb.test.bugs.bugjac802;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.JacORBTestSuite;

public class BugJac802Test extends ClientServerTestCase
{
    private BasicServer server;
    
    private String serverOptions;
    private String clientOptions;

    public BugJac802Test (String name, ClientServerSetup setup, String serverOptions, String clientOptions)
    {
        super (name, setup);
        this.serverOptions = serverOptions;
        this.clientOptions = clientOptions;
    }
    
    public static Test suite()
    {
        TestSuite suite = new JacORBTestSuite("SSL client/server tests",
                                              BugJac802Test.class);
        ClientServerSetup setup =
            new ClientServerSetup( suite,
                                   "org.jacorb.test.orb.BasicServerImpl");
        if (setup.isSSLEnabled())
        {
            suite.addTest( new BugJac802Test( "test_ping", setup, "20", "20" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "40", "40" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "20", "60" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "40", "60" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "60", "60" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "60", "20" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "60", "40" ));
            suite.addTest( new BugJac802Test( "test_ping", setup, "1", "1" ));
        }
        else
        {
            System.err.println("Test ignored as SSL is not enabled (" + BugJac802Test.class.getName() + ")");
        }
        
        return setup;
    }
    
    public void setUp() throws Exception
    {
        Properties clientProperties = new Properties ();
        clientProperties.put ("jacorb.security.support_ssl", "on");
        clientProperties.put ("jacorb.security.ssl.client.supported_options", clientOptions);
        clientProperties.put ("jacorb.security.ssl.client.required_options", clientOptions);
        clientProperties.put ("jacorb.ssl.socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLSocketFactory");
        clientProperties.put ("jacorb.ssl.server_socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLServerSocketFactory");
        clientProperties.put ("jacorb.security.keystore_password", "jsse_client_ks_pass");
        clientProperties.put ("jacorb.security.keystore", "org/jacorb/test/bugs/bugjac802/jsse_client_ks");
        clientProperties.put ("jacorb.security.jsse.trustees_from_ks", "on");
        
        setup.patchClientPropertires (clientProperties);

        Properties serverProperties = new Properties ();
        serverProperties.put ("jacorb.security.support_ssl", "on");
        serverProperties.put ("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit", 
                              "org.jacorb.security.ssl.sun_jsse.SecurityServiceInitializer");
        serverProperties.put ("jacorb.security.ssl.server.supported_options", serverOptions);
        serverProperties.put ("jacorb.security.ssl.server.required_options", serverOptions);
        serverProperties.put ("jacorb.ssl.socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLSocketFactory");
        serverProperties.put ("jacorb.ssl.server_socket_factory", "org.jacorb.security.ssl.sun_jsse.SSLServerSocketFactory");
        serverProperties.put ("jacorb.security.keystore_password", "jsse_server_ks_pass");
        serverProperties.put ("jacorb.security.keystore", "org/jacorb/test/bugs/bugjac802/jsse_server_ks");
        serverProperties.put ("jacorb.security.jsse.trustees_from_ks", "on");
        setup.patchServerPropertires (serverProperties);
        
        // TODO: here is the hack that allow changing and applying new server and client properties
        // form test to test. Need to be reviewed to find proper way to set the parameters.
        setup.tearDown ();
        setup.setUp ();
        
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public void test_ping()
    {
        server.ping();
    }

    public void test_ping_should_fail()
    {
        try
        {
            server.ping();
            fail ("Should be failed due to incompatible types of authentication: server=0x"
                  + serverOptions + " client=0x" + clientOptions);
        }
        catch (Exception ex)
        {
            // expected
        }
    }
}
    
 
