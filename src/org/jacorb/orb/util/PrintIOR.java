/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import org.jacorb.orb.connection.CodeSet;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.*;

import org.omg.IOP.*;
import org.omg.GIOP.*;
import org.omg.IIOP.*;
import org.omg.SSLIOP.*;
import org.omg.CSIIOP.*;
import org.omg.CONV_FRAME.*;

import java.io.*;

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
        String line, iorString = null;
    
        if( args.length < 1 || args.length > 2)
        {
            System.err.println("Usage: java PrintIOR [ ior_str | -f filename ]");
            System.exit( 1 );
        }
    
        if( args[0].equals("-f"))
        {
            try
            {
                // System.out.println ( "arg.length: " + arg.length );
                // System.out.println ( "arg[ 0 ]: " + arg[ 0 ] );
                // System.out.println ( "reading IOR from file: " + arg[ 1 ] );
                BufferedReader br = new BufferedReader ( new FileReader( args[1] ), 2048 );
                line = br.readLine();
                // System.out.print ( line );
                if ( line != null )
                {
                    iorString = line;
                    while ( line != null )
                    {
                        line = br.readLine();
                        if ( line != null ) 
                            iorString = iorString + line;
                        // System.out.print ( line );
                    }
                }
            } 
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            iorString = args[0];
        }
    
        if( iorString.startsWith( "IOR:" ))
        {
            ParsedIOR pior = new ParsedIOR( iorString );
            printIOR(pior, orb);
        }
        else
            System.out.println("Sorry, we only unparse IORs in the standard IOR URL scheme");

    }


    /** 
     * top-level 
     */

    public static void printIOR( ParsedIOR pior, org.omg.CORBA.ORB orb)
    {
        org.omg.IOP.IOR ior = pior.getIOR();

        System.out.println("------IOR components-----");
        System.out.println("TypeId\t:\t" + ior.type_id );

        org.omg.IIOP.ProfileBody_1_1[] profiles =  pior.getProfileBodies();
    
        System.out.println("TAG_INTERNET_IOP Profiles:");
        for( int i = 0; i < profiles.length; i++ )
        {      
            System.out.print("\tProfile Id   :  ");

            org.omg.IIOP.ProfileBody_1_1 pb = profiles[i];
            System.out.println("\tIIOP Version :  " + 
                               (int)pb.iiop_version.major + "." + 
                               (int)pb.iiop_version.minor);

            System.out.println("\tHost\t:\t" + pb.host);
            int port = pb.port;
            if( port < 0 ) 
                port += 65536;

            System.out.println("\tPort\t:\t" + port );
            try
            {
                System.out.println("\tObject key (URL):      " + 
                                   CorbaLoc.parseKey( pior.get_object_key()));
            }
            catch( Exception e )
            {
                // ignore, object key not in url format
            }
            System.out.print("\tObject key (hex):    0x" );
            dumpHex( pior.get_object_key() );
            System.out.println();

            if ( pb.iiop_version.minor >= ( char ) 1 )
            {
                if( pb.components.length > 0 )
                    System.out.println("\t-- Found " + 
                                       pb.components.length + 
                                       " Tagged Components--" );

                printTaggedComponents( pb.components );
                String codebase = pior.getCodebaseComponent();
                if( codebase != null )
                {
                    System.out.println("\tJava Codebase Component:\n\t\t" + codebase);		
                }
                System.out.print("\n");

            }
            System.out.print("\n");
        }
        orb.shutdown(true);
    }

    /**
     * Iterates over a tagged IOP components and prints those that are
     * recognized.
     */

    private static void printTaggedComponents( TaggedComponent[] taggedComponents )
    {
        for( int i = 0; i < taggedComponents.length; i++ )
        {            
            switch( taggedComponents[i].tag )
            {
                case TAG_SSL_SEC_TRANS.value:
                    System.out.println("\t#"+ i + ": TAG_SSL_SEC_TRANS");
                    printSSLTaggedComponent( taggedComponents[i] );
                    break;
                case TAG_CSI_SEC_MECH_LIST.value: 
                    System.out.println("\t#"+ i + ": TAG_CSI_SEC_MECH_LIST");
                    printCSIMechComponent( taggedComponents[i] );
                    break;
                case TAG_SECIOP_SEC_TRANS.value: 
                    System.out.println("\t#"+ i + ": TAG_SECIOP_SEC_TRANS");
                    break;
                case TAG_ALTERNATIVE_IIOP_ADDRESS.value: 
                    System.out.println("\t#"+ i + ": TAG_ALTERNATIVE_IIOP_ADDRESS");
                    break;
                case TAG_CODE_SETS.value:
                    System.out.println("\t#"+ i + ": TAG_CODE_SETS");
                    printCodeSetComponent( taggedComponents[i] );
                    break;
                default:             
                    System.out.println("\tUnknown tag : " + 
                                       taggedComponents[i].tag);
            }
        }
    }

    private static void printCSIMechComponent( TaggedComponent taggedComponent )
    {
        CDRInputStream is =
            new CDRInputStream( (org.omg.CORBA.ORB)null, 
                                taggedComponent.component_data);
                
        is.openEncapsulatedArray();
        CompoundSecMechList csmList = CompoundSecMechListHelper.read( is ); 

        if( csmList!= null )
        {
            System.out.println("\t\tis stateful: " + csmList.stateful );
            for( int i = 0; i < csmList.mechanism_list.length; i++ )
            {
                System.out.println("\t\tCompoundSecMech #" + i);
                System.out.println("\t\t\ttarget_requires: " + 
                                   csmList.mechanism_list[i].target_requires );
                System.out.print("\t\t\ttransport mechanism tag: ");
                switch( csmList.mechanism_list[i].transport_mech.tag )
                {
                    case TAG_TLS_SEC_TRANS.value:
                        System.out.println("TAG_TLS_SEC_TRANS");
                        break;
                    default:
                        System.out.println("Unknown tag : " + 
                                           csmList.mechanism_list[i].transport_mech.tag );
                }
                if( csmList.mechanism_list[i].as_context_mech.target_supports == 0 )
                    System.out.println("\t\t\tNo AS_ContextSec Mechanism.: ");
                else
                    System.out.println("\t\t\tAS_ContextSec mech: " + 
                                       new String( csmList.mechanism_list[i].as_context_mech.client_authentication_mech ));

                        
            }
        }
    }

    private static void printCodeSetComponent( TaggedComponent taggedComponent )
    {
        CDRInputStream is =
            new CDRInputStream( (org.omg.CORBA.ORB)null, 
                                taggedComponent.component_data);
                
        is.openEncapsulatedArray();
                
        org.omg.CONV_FRAME.CodeSetComponentInfo codeSet = 
            CodeSetComponentInfoHelper.read( is );           
        
        if( codeSet != null )
        {
            System.out.println("\t\tForChar native code set Id: " +
                               CodeSet.csName(codeSet.ForCharData.native_code_set ));
            System.out.print("\t\tChar Conversion Code Sets: ");
            for( int ji = 0; ji < codeSet.ForCharData.conversion_code_sets.length; ji++ )
            {
                System.out.println( CodeSet.csName( 
                                                   codeSet.ForCharData.conversion_code_sets[ji] )+ ",");
                
            }

            System.out.println("\t\tForWChar native code set Id: " +
                               CodeSet.csName(codeSet.ForWcharData.native_code_set ));
            System.out.print("\t\tWChar Conversion Code Sets: ");
            for( int ji = 0; ji < codeSet.ForWcharData.conversion_code_sets.length; ji++ )
            {
                System.out.println( CodeSet.csName( 
                                                   codeSet.ForWcharData.conversion_code_sets[ji] ) + ",");
            } 
        }
    }

    private static void printSSLTaggedComponent( TaggedComponent taggedComponent )
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
                ssl_port += 65536;
            
            System.out.print   ( "\t\ttarget_supports\t:\t" );
            dump               ( ssl.target_supports );
            java.lang.System.out.println();
            System.out.print   ( "\t\ttarget_requires\t:\t" );
            dump               ( ssl.target_requires );
            java.lang.System.out.println();
            System.out.println ( "\t\tSSL Port\t:\t" + ssl_port );

        }
        
    }

    
    public static void dumpHex(byte bs[])
    {
        for (int i=0; i<bs.length; i++)    {
            int n1 = (bs[i] & 0xff) / 16;
            int n2 = (bs[i] & 0xff) % 16;
            char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
            char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
            System.out.print( c1 + (c2 + " "));
        }
    }

    static char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                               'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static void dump ( byte bs[] ) {
        for ( int i = 0; i < bs.length; i++ ) {
            dump ( bs[ i ] );
            System.out.print( " " );
        }
    }

    public static void dump ( int is[] ) {
        for ( int i = 0; i < is.length; i++ ) {
            dump ( is[ i ] );
            System.out.print( " " );
        }
    }

    public static void dump ( byte b ) {
        java.lang.System.out.print( ""
                                    + hexDigit[ ( b >>  4 ) & 0x0f ]
                                    + hexDigit[ ( b       ) & 0x0f ]
                                    );
    }

    public static void dump ( short i ) {
        java.lang.System.out.print( ""
                                    + hexDigit[ ( i >> 12 ) & 0x0f ]
                                    + hexDigit[ ( i >>  9 ) & 0x0f ]
                                    + hexDigit[ ( i >>  4 ) & 0x0f ]
                                    + hexDigit[ ( i       ) & 0x0f ]
                                    );
    }

    public static void dump ( int i ) {
        java.lang.System.out.print( ""
                                    + hexDigit[ ( i >> 28 ) & 0x0f ]
                                    + hexDigit[ ( i >> 24 ) & 0x0f ]
                                    + hexDigit[ ( i >> 20 ) & 0x0f ]
                                    + hexDigit[ ( i >> 16 ) & 0x0f ]
                                    + hexDigit[ ( i >> 12 ) & 0x0f ]
                                    + hexDigit[ ( i >>  8 ) & 0x0f ]
                                    + hexDigit[ ( i >>  4 ) & 0x0f ]
                                    + hexDigit[ ( i       ) & 0x0f ]
                                    );
    }

    public static void dump ( byte bs[], boolean withChar ) {
        char c;
        int len = bs.length;
        for ( int i = 0; i < len; i++ ) {
            if ( 0 == i % 16 ) java.lang.System.out.println();
            if ( bs[ i ] > ( byte ) 31 && bs[ i ] < ( byte ) 127 ) c = ( char ) bs[ i ];
            else c = ' ';
            java.lang.System.out.print( ":"
                                        + hexDigit[ ( bs [ i ] >> 4 ) & 0x0f ]
                                        + hexDigit[ bs [ i ] & 0x0f ]
                                        + " " + c
                                        );
        }
    }
}

