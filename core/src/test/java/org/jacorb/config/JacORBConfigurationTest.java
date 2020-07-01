package org.jacorb.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.omg.CORBA.ORB;

import java.util.Properties;

import static org.junit.Assert.*;

public class JacORBConfigurationTest
{
    @Rule
	public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Test
    public void setAttributes() throws ConfigurationException
    {
        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        ORB orb = ORB.init( new String[] {}, orbProps);
        Properties props = new Properties();
        props.put ("jacorb.config.log.verbosity", "4");
        props.put("jacorb.connection.request.write_timeout", 60000);
        props.put ("custom.props", "applet-special.properties");

        JacORBConfiguration.getConfiguration(props, orb, true);
        orb.destroy();

        assertTrue( systemErrRule.getLog().contains( "Property jacorb.connection.request.write_timeout does not map to String object" ) );
    }
}
