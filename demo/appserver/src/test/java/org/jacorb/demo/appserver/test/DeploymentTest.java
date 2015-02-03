package org.jacorb.demo.appserver.test;

import org.jacorb.demo.appserver.GoodDay;
import org.jacorb.demo.appserver.GoodDayHelper;
import org.jacorb.demo.appserver.ejb.CorbaService;
import org.jacorb.test.harness.ORBTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DeploymentTest extends ORBTestCase
{
    private static final org.jboss.logging.Logger logger = Logger.getLogger(DeploymentTest.class.getName());

    private UUID uuid = UUID.randomUUID();

    @EJB
    CorbaService corbaService;

    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("jacorb.interop.null_string_encoding", "true");
    }

    @Deployment
    public static WebArchive createDeployment() {
        File file = new File("target/jacorb-appserver.war");
        assertTrue("War must exist", file.exists());

        File lib[] = Maven.resolver().loadPomFromFile("pom.xml").
                resolve("org.jacorb:jacorb-regression").withoutTransitivity().asFile();

        return ShrinkWrap.createFromZipFile(WebArchive.class, file).addAsLibraries(lib);
    }

    @Test
    public void shouldDeployApp()
    {
        assertNotNull("IOR string shouldn't be null ", corbaService.getIOR());
        logger.info("IOR is " + corbaService.getIOR());
    }

    @Test
    public void callCorba() throws Exception
    {
        GoodDay o = GoodDayHelper.narrow(getORB().string_to_object(corbaService.getIOR()));

        assertNull("Should be null", o.get_string());

        o.record_string(uuid.toString());

        logger.info("Retrieved string " + o.get_string());

        assertEquals(uuid.toString(), o.get_string());
    }
}
