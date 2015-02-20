package org.jacorb.test.dii;

import java.util.Properties;

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DiiSpecialOperationsTest extends ClientServerTestCase
{
    private Object server;
    private ORB orb;

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("ignoreXBootClasspath", "true");

        setup = new ClientServerSetup(DynamicServer.class.getName(), props, props);

    }

    @Before
    public void setUp() throws Exception
    {
        server = setup.getServerObject();
        orb = setup.getClientOrb();
    }


    @After
    public void tearDown() throws Exception
    {
        server = null;
        orb = null;
    }

    @Test
    public void testRepositoryId()
    {
        org.omg.CORBA.Request request = server._request("_repository_id");

        request.set_return_type(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        request.invoke();
        assertNull(request.env().exception());
        assertEquals("IDL:org/jacorb/test/dii/DIIServer:1.0", request.return_value().extract_string());

    }}
