/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

package org.jacorb.orb.util;

import org.apache.avalon.framework.logger.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORBConstants;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.iiop.IIOPAddress;

import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.CSIIOP.TAG_SECIOP_SEC_TRANS;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_JAVA_CODEBASE;
import org.omg.IOP.TAG_ORB_TYPE;
import org.omg.IOP.TAG_POLICIES;
import org.omg.IOP.TaggedComponent;
import org.omg.RTCORBA.PRIORITY_BANDED_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;
import org.omg.RTCORBA.PriorityModel;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class PrintIOR
{
    /**
     * entry point from the command line
     */

    public static void main(String args[])
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
        Logger logger =
            ((org.jacorb.orb.ORB)orb).getConfiguration().getNamedLogger("jacorb.print_ior");
        String line, iorString = null;

        if( args.length != 2)
        {
            System.err.println("Usage: java PrintIOR [ -i ior_str | -f filename ]"); // NOPMD
            System.exit( 1 );
        }

        if( args[0].equals("-f"))
        {
            try
            {
                BufferedReader br = new BufferedReader ( new FileReader( args[1] ), 2048 );
                line = br.readLine();
                if ( line != null )
                {
                    iorString = line;
                    while ( line != null )
                    {
                        line = br.readLine();
                        if ( line != null )
                        {
                            iorString = iorString + line;
                        }
                    }
                }
            }
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
                System.exit(1);
            }
        }
        else if ( args[0].equals("-i"))
        {
            iorString = args[1];
        }
        else
        {
            System.err.println("Usage: java PrintIOR [ -i ior_str | -f filename ]"); // NOPMD
            System.exit( 1 );
        }

        if( logger.isDebugEnabled() )
        {
            logger.debug
            (
                "Under " +
                System.getProperty ("os.name") +
                " the encoding name is " +
                System.getProperty( "file.encoding" ) +
                " and the canonical encoding name is " +
                ( new java.io.OutputStreamWriter( new ByteArrayOutputStream () ) ).getEncoding()
            );
        }

        PrintWriter out = new PrintWriter(System.out, true);

        if( iorString.startsWith( "IOR:" ))
        {
            ParsedIOR pior = new ParsedIOR( iorString, orb, logger );
            printIOR(pior, orb, out);
        }
        else
        {
            out.println("Sorry, we only unparse IORs in the standard IOR URL scheme");
        }

        orb.shutdown(true);
    }

    /**
     * top-level
     */

    public static void printIOR( ParsedIOR pior, org.omg.CORBA.ORB orb, PrintWriter out)
    {
        org.omg.IOP.IOR ior = pior.getIOR();

        out.println("------IOR components-----");
        out.println("TypeId\t:\t" + ior.type_id );

        List profiles = pior.getProfiles();

        out.println("TAG_INTERNET_IOP Profiles:");
        for( int i = 0; i < profiles.size(); i++ )
        {
            out.println("\tProfile Id:\t\t" + i);

            IIOPProfile profile = (IIOPProfile)profiles.get(i);
            out.println("\tIIOP Version:\t\t" +
                               (int)profile.version().major + "." +
                               (int)profile.version().minor);

            out.println("\tHost:\t\t\t" +
                               ((IIOPAddress)profile.getAddress()).getOriginalHost());
            int port = ((IIOPAddress)profile.getAddress()).getPort();
            if( port < 0 )
            {
                port += 65536;
            }

            out.println("\tPort:\t\t\t" + port );
            try
            {
                out.println("\tObject key (URL):\t" +
                                   CorbaLoc.parseKey( pior.get_object_key()));
            }
            catch( Exception e )
            {
                // ignore, object key not in url format
            }
            out.print("\tObject key (hex):\t0x" );
            dumpHex( pior.get_object_key(), out);
            out.println();

            if ( profile.version().minor >= ( char ) 1 )
            {
                if( profile.getComponents().size() > 0 )
                {
                    out.println("\t-- Found " +
                                       profile.getComponents().size() +
                                       " Tagged Components--" );
                }

                printTaggedComponents( profile.getComponents().asArray(), out);
            }
            out.print("\n");
        }

        TaggedComponentList multiple_components = pior.getMultipleComponents();

        if( multiple_components.size() > 0 )
        {
            out.println("Components in MULTIPLE_COMPONENTS profile: " +
                               multiple_components.size() );

            printTaggedComponents( multiple_components.asArray(), out);
        }

    }

    /**
     * Iterates over a tagged IOP components and prints those that are
     * recognized.
     */

    private static void printTaggedComponents( TaggedComponent[] taggedComponents, PrintWriter out )
    {
        for( int i = 0; i < taggedComponents.length; i++ )
        {
            switch( taggedComponents[i].tag )
            {
                case TAG_SSL_SEC_TRANS.value:
                {
                    out.println("\t#"+ i + ": TAG_SSL_SEC_TRANS");
                    printSSLTaggedComponent( taggedComponents[i], out);
                    break;
                }
                case TAG_CSI_SEC_MECH_LIST.value:
                {
                    out.println("\t#"+ i + ": TAG_CSI_SEC_MECH_LIST");
                    printCSIMechComponent( taggedComponents[i], out);
                    break;
                }
                case TAG_SECIOP_SEC_TRANS.value:
                {
                    out.println("\t#"+ i + ": TAG_SECIOP_SEC_TRANS");
                    break;
                }
                case TAG_ALTERNATE_IIOP_ADDRESS.value:
                {
                    out.println("\t#"+ i + ": TAG_ALTERNATE_IIOP_ADDRESS");
                    printAlternateAddress(taggedComponents[i], out);
                    break;
                }
                case TAG_CODE_SETS.value:
                {
                    out.println("\t#"+ i + ": TAG_CODE_SETS");
                    printCodeSetComponent( taggedComponents[i], out);
                    break;
                }
                case TAG_JAVA_CODEBASE.value:
                {
                    out.println("\t#"+ i + ": TAG_JAVA_CODEBASE");
                    printJavaCodebaseComponent( taggedComponents[i], out);
                    break;
                }
                case TAG_ORB_TYPE.value:
                {
                    out.println("\t#"+ i + ": TAG_ORB_TYPE");
                    printOrbTypeComponent( taggedComponents[i], out);
                    break;
                }
                case TAG_POLICIES.value:
                {
                    out.println("\t#"+ i + ": TAG_POLICIES");
                    printPolicyComponent (taggedComponents[i], out);
                    break;
                }
                case TAG_NULL_TAG.value:
                {
                    out.println("\t#"+ i + ": TAG_NULL_TAG");
                    break;
                }
                default:
                {
                    out.println("\tUnknown tag : " +
                            taggedComponents[i].tag);
                }
            }
        }
    }

    private static void printCSIMechComponent( TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is =
            new CDRInputStream( (org.omg.CORBA.ORB)null,
                    taggedComponent.component_data);

        try
        {
            is.openEncapsulatedArray();
            CompoundSecMechList csmList = CompoundSecMechListHelper.read( is );

            if( csmList!= null )
            {
                out.println("\t\tis stateful: " + csmList.stateful );
                for( int i = 0; i < csmList.mechanism_list.length; i++ )
                {
                    out.println("\t\tCompoundSecMech #" + i);
                    out.println("\t\t\ttarget_requires: " +
                            csmList.mechanism_list[i].target_requires );
                    out.print("\t\t\ttransport mechanism tag: ");
                    switch( csmList.mechanism_list[i].transport_mech.tag )
                    {
                        case TAG_TLS_SEC_TRANS.value:
                        {
                            out.println("TAG_TLS_SEC_TRANS");
                            printTlsSecTrans(csmList.mechanism_list[i].transport_mech.component_data, out);
                            break;
                        }
                        case TAG_NULL_TAG.value:
                        {
                            out.println("TAG_NULL_TAG");
                            break;
                        }
                        default:
                        {
                            out.println("Unknown tag : " +
                                    csmList.mechanism_list[i].transport_mech.tag );
                        }
                    }
                    out.println("\t\t\tAS_ContextSec target_supports: " + csmList.mechanism_list[i].as_context_mech.target_supports );
                    out.println("\t\t\tAS_ContextSec target_requires: " + csmList.mechanism_list[i].as_context_mech.target_requires );
                    out.print("\t\t\tAS_ContextSec mech: " );
                    dumpHex(csmList.mechanism_list[i].as_context_mech.client_authentication_mech, out);
                    out.println();
                    out.print("\t\t\tAS_ContextSec target_name: " );
                    printNTExportedName(csmList.mechanism_list[i].as_context_mech.target_name, out);
                    out.println("\t\t\tSAS_ContextSec target_supports: " + csmList.mechanism_list[i].sas_context_mech.target_supports );
                    out.println("\t\t\tSAS_ContextSec target_requires: " + csmList.mechanism_list[i].sas_context_mech.target_requires );

                    for (int j = 0; j < csmList.mechanism_list[i].sas_context_mech.supported_naming_mechanisms.length; j++) {
                        out.print("\t\t\tSAS_ContextSec Naming mech: " );
                        dumpHex(csmList.mechanism_list[i].sas_context_mech.supported_naming_mechanisms[j], out);
                        out.println();
                    }
                    out.println("\t\t\tSAS_ContextSec Naming types: " + csmList.mechanism_list[i].sas_context_mech.supported_identity_types);
                    out.println();
                }
            }
        }
        finally
        {
            is.close();
        }
    }

    private static void printNTExportedName(byte[] nameData, PrintWriter out) {
        // check for token identifier
        if (nameData.length < 2 || nameData[0] != 0x04 || nameData[1] != 0x01)
        {
            dumpHex(nameData, out);
            out.println();
            return;
        }

        // get mech length
        int mechLen = (nameData[2] << 8) + nameData[3];
        if (mechLen > (nameData.length - 8))
        {
            dumpHex(nameData, out);
            out.println();
            return;
        }

        // get name length
        int nameLen = (nameData[mechLen + 4] << 24) +
                      (nameData[mechLen + 5] << 16) +
                      (nameData[mechLen + 6] << 8) +
                      (nameData[mechLen + 7]);
        if ((mechLen + nameLen) > (nameData.length - 8))
        {
            dumpHex(nameData, out);
            out.println();
            return;
        }
        byte[] name = new byte[nameLen];
        System.arraycopy(nameData, mechLen + 8, name, 0, nameLen);
        out.println(new String(name));
    }

    private static void printTlsSecTrans(byte[] tagData, PrintWriter out) {
        CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null, tagData );

        try
        {
            in.openEncapsulatedArray();
            TLS_SEC_TRANS tls = TLS_SEC_TRANSHelper.read( in );
            out.println("\t\t\tTLS SEC TRANS target requires: " + tls.target_requires);
            out.println("\t\t\tTLS SEC TRANS target supports: " + tls.target_supports);

            for (int i = 0; i < tls.addresses.length; i++)
            {
                int ssl_port = tls.addresses[i].port;
                if( ssl_port < 0 )
                {
                    ssl_port += 65536;
                }
                out.println("\t\t\tTLS SEC TRANS address: " + tls.addresses[i].host_name+":"+ssl_port);
            }
        }
        catch ( Exception ex )
        {
            out.print("\t\t\tTLS SEC TRANS: " );
            dumpHex(tagData, out);
            out.println();
        }
    }

    private static void printCodeSetComponent( TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is =
            new CDRInputStream( (org.omg.CORBA.ORB)null,
                    taggedComponent.component_data);

        try
        {
            is.openEncapsulatedArray();

            org.omg.CONV_FRAME.CodeSetComponentInfo codeSet =
                CodeSetComponentInfoHelper.read( is );

            if( codeSet != null )
            {
                out.println("\t\tForChar native code set Id: " +
                        CodeSet.csName(codeSet.ForCharData.native_code_set ));
                out.print("\t\tChar Conversion Code Sets: ");
                for( int ji = 0; ji < codeSet.ForCharData.conversion_code_sets.length; ji++ )
                {
                    out.println( CodeSet.csName( codeSet.ForCharData.conversion_code_sets[ji] ) );

                    if( ji < (codeSet.ForCharData.conversion_code_sets.length - 1) )
                    {
                        out.print( ", " );
                    }
                }
                if (codeSet.ForCharData.conversion_code_sets.length == 0 )
                {
                    out.print("\n");
                }

                out.println("\t\tForWChar native code set Id: " +
                        CodeSet.csName(codeSet.ForWcharData.native_code_set ));
                out.print("\t\tWChar Conversion Code Sets: ");
                for( int ji = 0; ji < codeSet.ForWcharData.conversion_code_sets.length; ji++ )
                {
                    out.println( CodeSet.csName( codeSet.ForWcharData.conversion_code_sets[ji] ));

                    if( ji < (codeSet.ForWcharData.conversion_code_sets.length - 1) )
                    {
                        out.print( ", " );
                    }
                }
                if (codeSet.ForCharData.conversion_code_sets.length == 0 )
                {
                    out.print("\n");
                }
            }
        }
        finally
        {
            is.close();
        }
    }

    private static void printSSLTaggedComponent( TaggedComponent taggedComponent, PrintWriter out)
    {
        org.omg.SSLIOP.SSL  ssl = null;
        if( taggedComponent.tag == 20 )
        {
            CDRInputStream in =
            new CDRInputStream( (org.omg.CORBA.ORB)null,
                                taggedComponent.component_data );
            try
            {
                in.openEncapsulatedArray();
                ssl =  org.omg.SSLIOP.SSLHelper.read( in );
            }
            catch ( Exception ex )
            {
                return;
            }
            int ssl_port = ssl.port;
            if( ssl_port < 0 )
            {
                ssl_port += 65536;
            }

            out.print( "\t\ttarget_supports\t:\t" );
            //dump               ( ssl.target_supports );
            decodeAssociationOption( ssl.target_supports, out);
            out.println();
            out.print( "\t\ttarget_requires\t:\t" );
            //dump               ( ssl.target_requires );
            decodeAssociationOption( ssl.target_requires, out);
            out.println();
            out.println( "\t\tSSL Port\t:\t" + ssl_port );
        }
    }
    private static void decodeAssociationOption( int option, PrintWriter out)
    {
        boolean first = true;

        if( (option & org.omg.Security.NoProtection.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "NoProtection" );

            first = false;
        }

        if( (option & org.omg.Security.Integrity.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "Integrity" );

            first = false;
        }

        if( (option & org.omg.Security.Confidentiality.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "Confidentiality" );

            first = false;
        }

        if( (option & org.omg.Security.DetectReplay.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "DetectReplay" );

            first = false;
        }

        if( (option & org.omg.Security.DetectMisordering.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "DetectMisordering" );

            first = false;
        }

        if( (option & org.omg.Security.EstablishTrustInTarget.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "EstablishTrustInTarget" );

            first = false;
        }

        if( (option & org.omg.Security.EstablishTrustInClient.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "EstablishTrustInClient" );

            first = false;
        }

        if( (option & org.omg.Security.NoDelegation.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "NoDelegation" );

            first = false;
        }

        if( (option & org.omg.Security.SimpleDelegation.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "SimpleDelegation" );

            first = false;
        }

        if( (option & org.omg.Security.CompositeDelegation.value) != 0 )
        {
            if( ! first )
            {
                out.print( ", " );
            }

            out.print( "CompositeDelegation" );

            first = false;
        }
    }


    private static void printJavaCodebaseComponent( TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null,
                            taggedComponent.component_data );

        try
        {
            in.openEncapsulatedArray();
            String codebase = in.read_string();

            out.println( "\t\tCodebase: " + codebase );
        }
        finally
        {
            in.close();
        }
    }

    private static void printOrbTypeComponent (TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream ((org.omg.CORBA.ORB)null, taggedComponent.component_data );

        try
        {
            is.openEncapsulatedArray ();
            int type = is.read_long ();

            out.print ( "\t\tType: " + type);
            if (type == ORBConstants.JACORB_ORB_ID)
            {
                out.println (" (JacORB)");
            }
            else
            {
                out.println (" (Foreign)");
            }
        }
        finally
        {
            is.close();
        }
    }

    private static void printAlternateAddress(TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream((org.omg.CORBA.ORB)null, taggedComponent.component_data);

        try
        {
            is.openEncapsulatedArray();
            out.println("\t\tAddress: " + IIOPAddress.read(is));
        }
        finally
        {
            is.close();
        }
    }


    public static void dumpHex(byte[] values)
    {
        final PrintWriter printWriter = new PrintWriter(System.out);
        try
        {
            dumpHex(values, printWriter);
        }
        finally
        {
            printWriter.close();
        }
    }

    private static void dumpHex(byte values[], PrintWriter out)
    {
        for (int i=0; i<values.length; i++)
        {
            int n1 = (values[i] & 0xff) / 16;
            int n2 = (values[i] & 0xff) % 16;
            char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
            char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
            out.print(c1);
            out.print(c2);
            out.print(' ');
        }
    }

    private static final char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                               'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String dump ( byte values[] ) {
        StringBuffer buffer = new StringBuffer();

        for ( int i = 0; i < values.length; i++ )
        {
            buffer.append(values[i]);
            buffer.append(' ');
        }

        return buffer.toString();
    }

    public static String dump ( int values[] ) {
        StringBuffer buffer = new StringBuffer();

        for ( int i = 0; i < values.length; i++ )
        {
            buffer.append(values[i]);

            buffer.append(' ');
        }

        return buffer.toString();
    }

    public static String dump ( byte value ) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(hexDigit[ (value >> 4) & 0x0f]);
        buffer.append(hexDigit[ (value ) & 0x0f]);

        return buffer.toString();
    }

    public static String dump ( short value ) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(hexDigit[ ( value >> 12 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >>  9 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >>  4 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value       ) & 0x0f ]);

        return buffer.toString();
    }

    public static String dump ( int value ) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(hexDigit[ ( value >> 28 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >> 24 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >> 20 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >> 16 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >> 12 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >>  8 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value >>  4 ) & 0x0f ]);
        buffer.append(hexDigit[ ( value       ) & 0x0f ]);

        return buffer.toString();
    }

    public static String dump ( byte values[], boolean withChar )
    {
        StringBuffer buffer = new StringBuffer();

        char c;
        int len = values.length;
        for ( int i = 0; i < len; i++ )
        {
            if ( 0 == i % 16 )
            {
                buffer.append('\n');
            }
            if ( values[ i ] > ( byte ) 31 && values[ i ] < ( byte ) 127 )
            {
                c = ( char ) values[ i ];
            }
            else {
                c = ' ';
            }
            buffer.append(':');
            buffer.append(hexDigit[ ( values [ i ] >> 4 ) & 0x0f ]);
            buffer.append(hexDigit[ values [ i ] & 0x0f ]);
            buffer.append(' ');
            buffer.append(c);
        }

        return buffer.toString();
    }

    private static void printPolicyComponent (TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream ((org.omg.CORBA.ORB)null, taggedComponent.component_data);

        try
        {
            int val;
            int count = 0;

            is.openEncapsulatedArray ();
            int len = is.read_long ();

            while (len-- != 0)
            {
                val = is.read_long ();
                out.print( "\t\t#" + count++ + ": ");
                is.openEncapsulation ();
                switch (val)
                {
                    case PRIORITY_BANDED_CONNECTION_POLICY_TYPE.value:
                    {
                        long i;
                        short low;
                        short high;

                        out.println ("RTCORBA::PRIORITY_BANDED_CONNECTION");
                        val = is.read_long ();
                        for (i = 0; i < val; i++)
                        {
                            low = is.read_short ();
                            high = is.read_short ();
                            out.println ("\t\t\tBand " + i + ": " + low + "-" + high);
                        }
                        break;
                    }
                    case PRIORITY_MODEL_POLICY_TYPE.value:
                    {
                        out.print("RTCORBA::PRIORITY_MODEL");
                        val = is.read_long ();
                        switch (val)
                        {
                            case PriorityModel._CLIENT_PROPAGATED:
                            {
                                out.print (" (CLIENT_PROPAGATED, ");
                                break;
                            }
                            case PriorityModel._SERVER_DECLARED:
                            {
                                out.print (" (SERVER_DECLARED, ");
                                break;
                            }
                            default:
                            {
                                out.print (" (Unknown, ");
                                break;
                            }
                        }
                        short prio = is.read_short ();
                        out.println (prio + ")");
                        break;
                    }
                    default:
                    {
                        out.println ("Unknown (" + val + ")");
                        break;
                    }
                }
                is.closeEncapsulation ();
            }

        }
        finally
        {
            is.close();
        }
    }
}
