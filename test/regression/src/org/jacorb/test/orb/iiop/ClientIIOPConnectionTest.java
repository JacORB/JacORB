package org.jacorb.test.orb.iiop;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2008 Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
import java.util.Properties;
import junit.framework.TestSuite;
import org.jacorb.config.ConfigurationException;
import org.jacorb.config.JacORBConfiguration;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.ClientIIOPConnection;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.SAS_ContextSec;
import org.omg.CSIIOP.ServiceConfiguration;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.CSIIOP.TransportAddress;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TAG_NULL_TAG;
import org.omg.IOP.TaggedComponent;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author <a href="mailto:russell.gold@oracle.com">Russell Gold</a>
 */
public class ClientIIOPConnectionTest extends ORBTestCase
{

    private TestConnection testConnection = new TestConnection();
    private IIOPProfile iiopProfile = new IIOPProfile( new IIOPAddress( "localhost", 4000 ),
                                                       null,
                                                       ((org.jacorb.orb.ORB)orb).getGIOPMinorVersion());

    private static final short SSL_FEATURE = (short) 16;
    private static final short NOT_REQUIRED = (short) 0;
    private static final short NOT_SUPPORTED = (short) 0;


    public static TestSuite suite()
    {
        return new TestSuite( ClientIIOPConnectionTest.class );
    }


    static class TestConnection extends ClientIIOPConnection
    {
        void setProfile( IIOPProfile profile ) throws ConfigurationException
        {
            this.profile = profile;
            profile.configure( JacORBConfiguration.getConfiguration( new Properties(), orb, false ));
            checkSSL();
        }

    }


    private void configureConnection( boolean enableSsl, int clientSupports, int clientRequires ) throws Exception
    {
        configureObjectUnderTest( new String[][]
                {
                        { "jacorb.security.support_ssl", enableSsl ? "on" : "off" },
                        { "jacorb.security.ssl.client.supported_options", Integer.toString( clientSupports ) },
                        { "jacorb.security.ssl.client.required_options", Integer.toString( clientRequires ) },
                }
        );
    }


    private void configureObjectUnderTest( String[][] settings ) throws ConfigurationException
    {
        Properties properties = new Properties();
        for (int i = 0; i < settings.length; i++)
        {
            properties.setProperty( settings[i][0], settings[i][1] );
        }
        testConnection.configure( JacORBConfiguration.getConfiguration( properties, (ORB) orb, false ));
    }


    /**
     * Verifies that if no profiles are available, the connection does not use ssl.
     */
    public void testNoProfiles() throws Exception
    {
        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_SSL_SEC_TRANS profile (now obsolete) is present,
     * the connection will use it as a specification of SSL.
     */
    public void testSslSecTransProfile() throws Exception
    {
        int port = 5000;

        configureConnection( true, SSL_FEATURE, SSL_FEATURE );
        addSslSecTransComponent( SSL_FEATURE, SSL_FEATURE, port );
        testConnection.setProfile( iiopProfile );

        assertTrue( "Should report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", port, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_SSL_SEC_TRANS profile is present,
     * but the client is not supporting ssl, the connection does not use it.
     */
    public void testSslSecTransWithSslNotSupported() throws Exception
    {
        configureConnection( false, NOT_SUPPORTED, NOT_REQUIRED );
        addSslSecTransComponent( SSL_FEATURE, SSL_FEATURE, (short) 80 );
        testConnection.setProfile( iiopProfile );

        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_SSL_SEC_TRANS profile does not enable ssl if neither client nor server requires it.
     */
    public void testSslSecTransProfileNotRequired() throws Exception
    {
        configureConnection( true, SSL_FEATURE, NOT_REQUIRED );
        addSslSecTransComponent( SSL_FEATURE, NOT_REQUIRED, 50 );
        testConnection.setProfile( iiopProfile );

        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_COMPOUND_SEC_MECH profile with a TLS transport mechanism is present,
     * the connection will use it as a specification of SSL.
     */
    public void testTlsTransProfile() throws Exception
    {
        int port = 5000;

        configureConnection( true, SSL_FEATURE, SSL_FEATURE );
        addCompoundSecMechComponentWithTls( SSL_FEATURE, SSL_FEATURE, port );
        testConnection.setProfile( iiopProfile );

        assertTrue( "Should report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", port, testConnection.getSsl_port() );
    }


    /**
     * Verifies that port numbers > 32767 are supported.
     */
    public void testTlsHighPortNum() throws Exception
    {
        int port = 40000;

        configureConnection( true, SSL_FEATURE, SSL_FEATURE );
        addCompoundSecMechComponentWithTls( SSL_FEATURE, SSL_FEATURE, port );
        testConnection.setProfile( iiopProfile );

        assertTrue( "Should report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", port, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_COMPOUND_SEC_MECH is present with no transport mechanism,
     * and the client requires SSL, an exception will be thrown.
     */
    public void testCompoundSecMechNoTls() throws Exception
    {
        try
        {
            configureConnection( true, SSL_FEATURE, SSL_FEATURE );
            addCompoundSecMechComponentWithoutTls( SSL_FEATURE, SSL_FEATURE );
            testConnection.setProfile( iiopProfile );
            fail( "Should have thrown a NO_PERMISSION exception" );
        } catch (NO_PERMISSION e)
        {
        }
        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_COMPOUND_SEC_MECH profile with a TLS transport mechanism is present,
     * but the client is not supporting ssl, the connection does not use it.
     */
    public void testTlsTransProfileWithSslNotSupported() throws Exception
    {
        configureConnection( false, SSL_FEATURE, SSL_FEATURE );
        addCompoundSecMechComponentWithTls( SSL_FEATURE, SSL_FEATURE, 60 );
        testConnection.setProfile( iiopProfile );

        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if a TAG_COMPOUND_SEC_MECH profile with a TLS transport mechanism is present,
     * ssl is not enabled if neither client nor server requires it.
     */
    public void testTlsTransProfileNotRequired() throws Exception
    {
        int port = 5000;

        configureConnection( true, SSL_FEATURE, NOT_REQUIRED );
        addCompoundSecMechComponentWithTls( SSL_FEATURE, NOT_REQUIRED, port );
        testConnection.setProfile( iiopProfile );

        assertFalse( "Should not report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", -1, testConnection.getSsl_port() );
    }


    /**
     * Verifies that if both a TAG_SSL_SEC_TRANS and a TAG_COMPOUND_SEC_MECH profile without a TLS transport mechanism are present,
     * the SSL_SEC_TRANS will be used.
     */
    public void testSslTransProfileFavoredOverNonTlsSecMech() throws Exception
    {
        int port = 5000;

        configureConnection( true, SSL_FEATURE, SSL_FEATURE );
        addSslSecTransComponent( SSL_FEATURE, SSL_FEATURE, port );
        addCompoundSecMechComponentWithoutTls( SSL_FEATURE, SSL_FEATURE );
        testConnection.setProfile( iiopProfile );

        assertTrue( "Should report ssl enabled", testConnection.isSSL() );
        assertEquals( "ssl port", port, testConnection.getSsl_port() );
    }


    private void addSslSecTransComponent( short target_supports, short target_requires, int port )
    {
        iiopProfile.addComponent( TAG_SSL_SEC_TRANS.value, new SSL( target_supports, target_requires, (short) (port & 0x0ffff)), SSLHelper.class);
    }


    private void addCompoundSecMechComponentWithTls( short target_supports, short target_requires, int port )
    {
        iiopProfile.addComponent( TAG_CSI_SEC_MECH_LIST.value, createCompoundSecMechListWithTls( target_supports, target_requires, port), CompoundSecMechListHelper.class);
    }


    private CompoundSecMechList createCompoundSecMechListWithTls( short targetSupports, short targetRequires, int port )
    {
        SAS_ContextSec sasContextSec =
            new SAS_ContextSec( NOT_REQUIRED, NOT_REQUIRED, new ServiceConfiguration[0], new byte[0][0], 0 );

        TaggedComponent transportMech = createTlsTransportMech( targetSupports, targetRequires, port );

        AS_ContextSec asContextSec =
            new AS_ContextSec( targetSupports, targetRequires, new byte[0], new byte[0] );
        CompoundSecMech[] compoundSecMech =
            new CompoundSecMech[] { new CompoundSecMech( targetRequires, transportMech, asContextSec, sasContextSec) };

        return new CompoundSecMechList(false, compoundSecMech);
    }


    private void addCompoundSecMechComponentWithoutTls( short target_supports, short target_requires )
    {
        iiopProfile.addComponent( TAG_CSI_SEC_MECH_LIST.value, createCompoundSecMechListWithoutTls( target_supports, target_requires ), CompoundSecMechListHelper.class);
    }


    private CompoundSecMechList createCompoundSecMechListWithoutTls( short targetSupports, short targetRequires )
    {
        SAS_ContextSec sasContextSec = new SAS_ContextSec( NOT_REQUIRED, NOT_REQUIRED, new ServiceConfiguration[0], new byte[0][0], 0 );
        TaggedComponent transportMech = new TaggedComponent( TAG_NULL_TAG.value, new byte[0] );
        AS_ContextSec asContextSec = new AS_ContextSec( targetSupports, targetRequires, new byte[0], new byte[0] );

        CompoundSecMech[] compoundSecMech =
            new CompoundSecMech[] { new CompoundSecMech( targetRequires, transportMech, asContextSec, sasContextSec) };

        return new CompoundSecMechList(false, compoundSecMech);
    }


    private static TaggedComponent createTlsTransportMech( short targetSupports, short targetRequires, int port )
    {
        CDROutputStream out = new CDROutputStream();
        try
        {
            out.beginEncapsulatedArray();
            TLS_SEC_TRANS tls = new TLS_SEC_TRANS( targetSupports, targetRequires, new TransportAddress[] { new TransportAddress( "local", (short) (port & 0x0ffff) )} );
            TLS_SEC_TRANSHelper.write( out, tls );
            return new TaggedComponent( TAG_TLS_SEC_TRANS.value, out.getBufferCopy() );
        }
        finally
        {
            out.close();
        }
    }

}
