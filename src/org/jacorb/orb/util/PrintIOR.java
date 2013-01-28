/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORBConstants;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.giop.CodeSet;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.miop.MIOPProfile;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_SECIOP_SEC_TRANS;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TAG_GROUP;
import org.omg.IOP.TAG_JAVA_CODEBASE;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TAG_NULL_TAG;
import org.omg.IOP.TAG_ORB_TYPE;
import org.omg.IOP.TAG_POLICIES;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableGroup.TagGroupTaggedComponent;
import org.omg.PortableGroup.TagGroupTaggedComponentHelper;
import org.omg.RTCORBA.PRIORITY_BANDED_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;
import org.omg.RTCORBA.PriorityModel;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;
import org.slf4j.Logger;

/**
 * @author Gerald Brose
 */
public class PrintIOR
{
    /**
     * entry point from the command line
     */

    public static void main(String args[]) throws Exception
    {
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
        final org.jacorb.orb.ORB jorb = (org.jacorb.orb.ORB)orb;
        final Logger logger =
            jorb.getConfiguration().getLogger("jacorb.print_ior");

        boolean urlForm = false;
        boolean corbalocForm = false;
        String iorString = null;
        String file = null;

        if( args.length < 2 || args.length > 3)
        {
            usage();
        }

        for (int i = 0; i < args.length; i++)
        {
            if ("-u".equals(args[i]))
            {
                urlForm = true;
            }
            else if ("-c".equals(args[i]))
            {
                corbalocForm = true;
            }
            else if ("-i".equals(args[i]))
            {
                iorString = args[i + 1];
                ++i;
            }
            else if ("-f".equals(args[i]))
            {
                file = args[i + 1];
                ++i;
            }
            else
            {
                usage();
            }
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

        try
        {
           if (file != null)
            {
                final LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(file)));
                try
                {
                    String line = null;
                    while( (line = in.readLine()) != null)
                    {
                       iorString = line;
                    }
                }
                finally
                {
                    in.close();
                }
            }


           if( iorString.startsWith( "IOR:" ))
           {
              final ParsedIOR pior = new ParsedIOR(jorb, iorString );
              if (urlForm)
              {
                 out.println(CorbaLoc.parseKey(pior.get_object_key()));
              }
              else if (corbalocForm)
              {
                  out.println (printCorbalocIOR (orb, iorString));
              }
              else
              {
                 printIOR(jorb, pior, out);
              }
           }
           else
           {
              out.println("Sorry, we only unparse IORs in the standard IOR URL scheme");
           }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        finally
        {
            out.flush();
        }

        orb.shutdown(true);
    }


    /**
     * Given a IOR string this will decode it and output a corbaloc::... string.
     *
     * Split off into a separate function from main so its easier to be called programmatically.
     */
    public static String printCorbalocIOR (org.omg.CORBA.ORB orb, String iorString)
    {
       if ( ! (orb instanceof org.jacorb.orb.ORB))
       {
          throw new RuntimeException ("ORB must be a JacORB ORB.");
       }
       final ParsedIOR pior = new ParsedIOR((org.jacorb.orb.ORB)orb, iorString );

       StringBuffer result = new StringBuffer();

       result.append ("corbaloc:iiop:");

       ProfileBase profile = (ProfileBase)pior.getEffectiveProfile ();

       if (profile instanceof IIOPProfile)
       {
          result.append ("1." + Byte.valueOf(profile.version().minor) + "@");
          result.append (((IIOPAddress)((IIOPProfile)profile).getAddress()).getOriginalHost());
          result.append (':');
          result.append (((IIOPAddress)((IIOPProfile)profile).getAddress()).getPort());
          result.append ('/');
          result.append (CorbaLoc.parseKey(pior.get_object_key()));
       }
       else
       {
          throw new RuntimeException ("Sorry, only print corbaloc strings for IIOP profiles.");
       }
       return result.toString ();
    }

    private static void usage()
    {
        System.err.println("Usage: java PrintIOR "
                + "[ -i ior_str ] [ -f filename] [-u | -c]"
                + "\n\tior_str\t IOR as String"
                + "\n\t-f\t reads one or more IOR's from the file <filename>"
                + "\n\t-u\t extract object key in URL-Form instead of HEX "
                + "\n\t-c\t output corbaloc form of IOR string"
                ); // NOPMD
        System.exit( 1 );
    }

    /**
     * top-level
     */

    public static void printIOR (ORB orb, ParsedIOR pior, PrintWriter out)
    {
        org.omg.IOP.IOR ior = pior.getIOR();

        out.println("------IOR components-----");
        out.println("TypeId\t:\t" + ior.type_id );

        List profiles = pior.getProfiles();

        out.println("TAG_INTERNET_IOP Profiles:");
        for( int i = 0; i < profiles.size(); i++ )
        {
            out.println("\tProfile Id:\t\t" + i);

            ProfileBase profile = (ProfileBase)profiles.get(i);
            out.println("\tIIOP Version:\t\t" +
                               (int)profile.version().major + "." +
                               (int)profile.version().minor);
            if (profile instanceof IIOPProfile)
            {
                out.println("\tHost:\t\t\t" +
                  ((IIOPAddress)((IIOPProfile)profile).getAddress()).getOriginalHost());
                int port = ((IIOPAddress)((IIOPProfile)profile).getAddress()).getPort();
                if( port < 0 )
                {
                    port += 65536;
                }

                out.println("\tPort:\t\t\t" + port );
            }
            else if (profile instanceof MIOPProfile)
            {
                out.println ("MIOPProfile:\t" + ((MIOPProfile)profile).toString ());
            }
            out.println("\tObject key (URL):\t" + CorbaLoc.parseKey( profile.get_object_key()));
            out.print  ("\tObject key (hex):\t0x" );
            dumpHex( profile.get_object_key(), out);
            out.println();

            if ( profile.version().minor >= ( char ) 1 )
            {
                if( profile.getComponents().size() > 0 )
                {
                    out.println("\t-- Found " +
                                       profile.getComponents().size() +
                                       " Tagged Components--" );
                }

                printTaggedComponents(orb, profile.getComponents().asArray(), out);
            }
            out.print("\n");
        }

        TaggedComponentList multiple_components = pior.getMultipleComponents();

        if( multiple_components.size() > 0 )
        {
            out.println("Components in MULTIPLE_COMPONENTS profile: " +
                               multiple_components.size() );

            printTaggedComponents(orb, multiple_components.asArray(), out);
        }

        // Print any unknown tags. This block is a simplified version of the private
        // ParsedIOR::decode function.
        for (int i=0; i < ior.profiles.length; i++)
        {
            int tag = ior.profiles[i].tag;
            boolean found = false;

            // See if JacORB managed to parse this tag before into the ParsedIOR
            for (int j=0; j < profiles.size(); j++)
            {
                final IIOPProfile profile = (IIOPProfile)profiles.get(j);

                if (profile.tag () == tag)
                {
                    found = true;
                }

                if (tag == TAG_MULTIPLE_COMPONENTS.value)
                {
                    found = true;
                }
            }
            // This is an unknown tag that wasn't dealt with before.
            if ( ! found)
            {
                out.println ("Unknown profile found with tag " + tag);
            }
        }
    }

    /**
     * Iterates over a tagged IOP components and prints those that are
     * recognized.
     */

    private static void printTaggedComponents( ORB orb, TaggedComponent[] taggedComponents, PrintWriter out )
    {
        for( int i = 0; i < taggedComponents.length; i++ )
        {
            switch( taggedComponents[i].tag )
            {
                case TAG_SSL_SEC_TRANS.value:
                {
                    out.println("\t#"+ i + ": TAG_SSL_SEC_TRANS");
                    printSSLTaggedComponent(taggedComponents[i], out);
                    break;
                }
                case TAG_CSI_SEC_MECH_LIST.value:
                {
                    out.println("\t#"+ i + ": TAG_CSI_SEC_MECH_LIST");
                    printCSIMechComponent(taggedComponents[i], out);
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
                    printAlternateAddress(orb, taggedComponents[i], out);
                    break;
                }
                case TAG_CODE_SETS.value:
                {
                    out.println("\t#"+ i + ": TAG_CODE_SETS");
                    printCodeSetComponent(taggedComponents[i], out);
                    break;
                }
                case TAG_JAVA_CODEBASE.value:
                {
                    out.println("\t#"+ i + ": TAG_JAVA_CODEBASE");
                    printJavaCodebaseComponent(taggedComponents[i], out);
                    break;
                }
                case TAG_ORB_TYPE.value:
                {
                    out.println("\t#"+ i + ": TAG_ORB_TYPE");
                    printOrbTypeComponent(taggedComponents[i], out);
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
                case TAG_GROUP.value:
                {
                    out.println ("\t#" + i + ": TAG_GROUP");
                    printTagGroupTaggedComponent (taggedComponents[i], out);
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

    private static void printCSIMechComponent(TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream(taggedComponent.component_data);

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

    private static void printNTExportedName(byte[] nameData, PrintWriter out)
    {
        // check for token identifier
        if (nameData.length < 2 || nameData[0] != 0x04 || nameData[1] != 0x01)
        {
            dumpHex(nameData, out);
            out.println();
            return;
        }

        // get mech length
        int mechLen = (nameData[2] << 8) + (nameData[3] & 0xFF);
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

    private static void printTlsSecTrans(byte[] tagData, PrintWriter out)
    {
        CDRInputStream in = new CDRInputStream(tagData );

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

    private static void printCodeSetComponent(TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream(taggedComponent.component_data);

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
                    out.print( CodeSet.csName( codeSet.ForCharData.conversion_code_sets[ji] ) );

                    if( ji < (codeSet.ForCharData.conversion_code_sets.length - 1) )
                    {
                        out.print( ", " );
                    }
                }
                out.print("\n");

                out.println("\t\tForWChar native code set Id: " +
                            CodeSet.csName(codeSet.ForWcharData.native_code_set ));
                out.print("\t\tWChar Conversion Code Sets: ");
                for( int ji = 0; ji < codeSet.ForWcharData.conversion_code_sets.length; ji++ )
                {
                    out.print( CodeSet.csName( codeSet.ForWcharData.conversion_code_sets[ji] ));

                    if( ji < (codeSet.ForWcharData.conversion_code_sets.length - 1) )
                    {
                        out.print( ", " );
                    }
                }
                out.print("\n");
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
            CDRInputStream in = new CDRInputStream(taggedComponent.component_data );
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


    private static void printJavaCodebaseComponent(TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream in = new CDRInputStream( taggedComponent.component_data );

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
        final CDRInputStream is = new CDRInputStream (taggedComponent.component_data );

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

    private static void printAlternateAddress(ORB orb, TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream(taggedComponent.component_data);

        try
        {
            is.openEncapsulatedArray();
            String hostname = is.read_string();
            short port = is.read_ushort();

            IIOPAddress result = new IIOPAddress (hostname, port);
            try
            {
               result.configure(((org.jacorb.orb.ORB)orb).getConfiguration ());
            }
            catch( ConfigurationException ce)
            {
               ((org.jacorb.orb.ORB)orb).getConfiguration ().getLogger ("PrintIOR").warn("ConfigurationException", ce );
            }


            out.println("\t\tAddress: " + result.toString ());
        }
        finally
        {
            is.close();
        }
    }

    private static void printTagGroupTaggedComponent(TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream(org.omg.CORBA.ORBSingleton.init(), taggedComponent.component_data);

        is.openEncapsulatedArray();
        TagGroupTaggedComponent tagGroup = TagGroupTaggedComponentHelper.read (is);
        is.close ();

        out.println ("\t\tVersion: " + tagGroup.group_version.major + ":" + tagGroup.group_version.minor);
        out.println ("\t\tDomain: " + tagGroup.group_domain_id);
        out.println ("\t\tObjectGroupID: " + tagGroup.object_group_id);
        out.println ("\t\tObject Version: " + tagGroup.object_group_ref_version);
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



    private static void printPolicyComponent (TaggedComponent taggedComponent, PrintWriter out)
    {
        final CDRInputStream is = new CDRInputStream (taggedComponent.component_data);

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
