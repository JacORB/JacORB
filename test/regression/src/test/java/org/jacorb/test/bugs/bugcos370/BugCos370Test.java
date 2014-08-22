package org.jacorb.test.bugs.bugcos370;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.orb.AnyServer;
import org.jacorb.test.orb.AnyServerHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

/**
 * @author Alphonse Bendt
 */
public class BugCos370Test extends ClientServerTestCase
{
    private AnyServer server;

    @Before
    public void setUp() throws Exception
    {
        server = AnyServerHelper.narrow(setup.getServerObject());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        Properties props = new Properties();
        props.put("jacorb.compactTypecodes", "off");
        setup = new ClientServerSetup(BugCos370ServerImpl.class.getName(), props, null);
    }

    @Test
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
