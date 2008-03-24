package org.jacorb.test.orb.giop;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2008 Gerald Brose
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
 *
 */
import junit.framework.TestSuite;
import org.easymock.MockControl;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.giop.ClientConnection;
import org.jacorb.orb.giop.ClientGIOPConnection;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.omg.ETF.Profile;
import org.omg.GIOP.Version;
import org.omg.IOP.IOR;
import org.omg.IOP.TaggedProfile;

import java.util.ArrayList;

/**
 * @author <a href="mailto:russell.gold@oracle.com">Russell Gold</a>
 */
public class ClientConnectionTest extends ORBTestCase
{
    private static final int ISO8859_1_ID = 0x00010001;
    private static final int UTF8_ID      = 0x05010001;
    private static final int UTF16_ID     = 0x00010109;

    public static TestSuite suite()
    {
        return new TestSuite( ClientConnectionTest.class );
    }


    /**
     * Verifies that the connection defaults to the appropriate codesets if speaking giop 1.0, which does not
     * support negotiation.
     */
    public void testDefaultCodeSets() throws Exception
    {
        ParsedIOR ior = new ParsedIOR( (ORB) orb, new IOR( "", new TaggedProfile[0] ) );
        ClientConnection connection = createClientConnection( 1, 0, ior );

        assertEquals( "Default codeSet", ISO8859_1_ID, connection.getTCS().getId() );
        assertEquals( "Default wide codeset", UTF16_ID, connection.getTCSW().getId() );
    }


    /**
     * Verifies that the connection defaults to the appropriate codesets if speaking giop 1.1, when the server
     * fails to offer any code sets.
     */
    public void testNoCodeSetSpecified() throws Exception
    {
        ParsedIOR ior = new ParsedIOR( (ORB) orb, new IOR( "", new TaggedProfile[0] ) );
        ClientConnection connection = createClientConnection( 1, 1, ior );

        assertEquals( "Default codeSet", ISO8859_1_ID, connection.getTCS().getId() );
        assertEquals( "Default wide codeset", UTF16_ID, connection.getTCSW().getId() );
    }


    /**
     * Verifies that the connection can select the supported codesets in GIOP 1.1.
     */
    public void testSelectingStandardCodeSetsFor1_1() throws Exception
    {
        ParsedIOR ior = createParsedIOR( ISO8859_1_ID, UTF16_ID );
        ClientConnection connection = createClientConnection( 1, 1, ior );

        assertEquals( "Selected codeSet", ISO8859_1_ID, connection.getTCS().getId() );
        assertEquals( "Selected wide codeset", UTF16_ID, connection.getTCSW().getId() );
    }


    /**
     * Verifies that the connection can select UTF8 for wchar in GIOP 1.2.
     */
    public void testSelectingUTF8For1_2() throws Exception
    {
        ParsedIOR ior = createParsedIOR( ISO8859_1_ID, UTF8_ID );
        ClientConnection connection = createClientConnection( 1, 2, ior );

        assertEquals( "Selected codeSet",      ISO8859_1_ID, connection.getTCS().getId() );
        assertEquals( "Selected wide codeset", UTF8_ID,      connection.getTCSW().getId() );
    }


    /**
     * Verifies that the connection can select matching conversion codesets.
     */
    public void testSelectingConversionCodeSets() throws Exception
    {
        ParsedIOR ior = createParsedIOR( 0x11111, new int[]{ISO8859_1_ID}, 0x12345, new int[]{UTF16_ID} );
        ClientConnection connection = createClientConnection( 1, 2, ior );

        assertEquals( "Selected codeSet", ISO8859_1_ID, connection.getTCS().getId() );
        assertEquals( "Selected wide codeset", UTF16_ID, connection.getTCSW().getId() );
    }


    /**
     * Verify that the connection will be rejected if it requires an unknown codeset.
     */
    public void testRejectingUnknownCodeSet() throws Exception
    {
        ParsedIOR ior = createParsedIOR( ISO8859_1_ID, 0x111111 );
        try
        {
            createClientConnection( 1, 2, ior );
            fail( "Should have rejected unknown codeset 0x111111" );
        } catch (CODESET_INCOMPATIBLE e)
        {
        }
    }

    // xxx reject connection if 1.1 and requested UTF8 for wchar


    private ClientConnection createClientConnection( int majorVersion, int minorVersion, ParsedIOR ior )
    {
        Profile profile = createMockProfile( majorVersion, minorVersion );
        ClientGIOPConnection giopConnection = new ClientGIOPConnection( null, null, null, null, null );
        ClientConnection connection = new ClientConnection( giopConnection, orb, null, profile, false );

        ior.setProfileSelector( createMockProfileSelector( profile ) );
        connection.setCodeSet( ior );
        return connection;
    }


    private ProfileSelector createMockProfileSelector( Profile profile )
    {
        MockControl selectorControl = MockControl.createControl( ProfileSelector.class );
        ProfileSelector selector = (ProfileSelector) selectorControl.getMock();
        selector.selectProfile( new ArrayList(), ((ORB) orb).getClientConnectionManager() );
        selectorControl.setReturnValue( profile );
        selectorControl.replay();
        return selector;
    }


    private ParsedIOR createParsedIOR( int charCodeSet, int wcharCodeSet )
    {
        return createParsedIOR( charCodeSet, new int[0], wcharCodeSet, new int[0] );
    }


    private ParsedIOR createParsedIOR( int charCodeSet, int[] conversionCharSets, int wcharCodeSet, int[] conversionWCharSets )
    {
        final CodeSetComponent forCharData = new CodeSetComponent( charCodeSet, conversionCharSets );
        final CodeSetComponent forWCharData = new CodeSetComponent( wcharCodeSet, conversionWCharSets );
        return new ParsedIOR( (ORB) orb, new IOR( "", new TaggedProfile[0] ) )
        {
            public CodeSetComponentInfo getCodeSetComponentInfo()
            {
                return new CodeSetComponentInfo( forCharData, forWCharData );
            }
        };
    }


    private Profile createMockProfile( int majorVersion, int minorVersion )
    {
        MockControl profileControl = MockControl.createControl( Profile.class );
        Profile profile = (Profile) profileControl.getMock();
        profile.version();
        profileControl.setReturnValue( new Version( (byte) majorVersion, (byte) minorVersion ) );
        profileControl.replay();
        return profile;
    }

}
