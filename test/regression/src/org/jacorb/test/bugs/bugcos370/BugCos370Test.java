package org.jacorb.test.bugs.bugcos370;

import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.orb.AnyServer;
import org.jacorb.test.orb.AnyServerHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

/**
 * @author Alphonse Bendt
 */
public class BugCos370Test extends ClientServerTestCase
{
    private AnyServer server;

    public BugCos370Test(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        Properties props = new Properties();
        props.put("jacorb.compactTypecodes", "off");
        ClientServerSetup setup = new ClientServerSetup(suite, BugCos370ServerImpl.class.getName(), props, null);

        suite.addTest(new BugCos370Test("testTypeCode", setup));

        return setup;
    }

    public void testTypeCode()
    {
        assertTrue(NamingAttributes_THelper.type().equivalent(NVSList_THelper.type()));
        assertTrue(NVSList_THelper.type().equivalent(NamingAttributes_THelper.type()));

        assertFalse(NamingAttributes_THelper.type().equal(NVSList_THelper.type()));
        assertFalse(NVSList_THelper.type().equal(NamingAttributes_THelper.type()));

        Any any = setup.getClientOrb().create_any();
        NameAndStringValue_T nameAndStringValue = new NameAndStringValue_T();
        NamingAttributes_THelper.insert(any, new NameAndStringValue_T[] {nameAndStringValue});

        TypeCode type = any.type();

        assertTrue(type.equivalent(NamingAttributes_THelper.type()));
        assertTrue(type.equal(NamingAttributes_THelper.type()));

        server.bounce_any(any);
    }
}
