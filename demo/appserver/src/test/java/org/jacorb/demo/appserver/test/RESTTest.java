package org.jacorb.demo.appserver.test;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.jacorb.demo.appserver.rest.GoodDayRest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RESTTest
{
    @ArquillianResource
    URL deploymentURL;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File file = new File("target/jacorb-appserver.war");
        assertTrue("War must exist", file.exists());

        return ShrinkWrap.createFromZipFile(WebArchive.class, file);
    }

    @Test
    public void testrest(@ArquillianResteasyResource GoodDayRest target) throws Exception
    {
        final String name = UUID.randomUUID().toString();

        String result = target.getHelloWorldXML();
        assertTrue ("XML should contain null", result.contains("null"));

        result = target.getHelloWorldXML(name);
        assertTrue ("UUID missing", result.contains(name));
    }
}
