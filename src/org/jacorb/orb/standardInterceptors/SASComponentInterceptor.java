/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.orb.standardInterceptors;

import java.util.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.CSIIOP.*;
import org.omg.GSSUP.*;
import org.ietf.jgss.*;
import org.omg.ATLAS.*;

import org.jacorb.orb.*;
import org.jacorb.util.*;

/**
 * This interceptor creates an sas TaggedComponent
 *
 * @author David Robison
 * @version $Id$
 */

public class SASComponentInterceptor
    extends org.omg.CORBA.LocalObject
    implements IORInterceptor
{
    private ORB orb = null;
    private Codec codec = null;
    private TaggedComponent tc = null;

    public SASComponentInterceptor( ORB orb )
    {
        this.orb = orb;
        try
        {
            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
            CodecFactory codec_factory = (CodecFactory) orb.resolve_initial_references("CodecFactory");
            codec = codec_factory.create_codec(encoding);
        }
        catch (Exception e)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, e);
        }
    }

    // implementation of org.omg.PortableInterceptor.IORInterceptorOperations interface

    public String name()
    {
        return "SASComponentCreator";
    }

    public void destroy()
    {
    }

    /**
     * Builds an sas TaggedComponent.
     * Was formerly: ORB.makeSASComponent()
     */

    public void establish_components(IORInfo info)
    {
        try
        {
            if( tc == null )
            {
                // parse required association options
		String targetSupportsNames = Environment.getProperty( "jacorb.security.sas.tss.target_supports", "" );
                short targetSupports = (short)0;
                StringTokenizer nameTokens = new StringTokenizer(targetSupportsNames, ":;, ");
                while (nameTokens.hasMoreTokens())
                {
                    String token = nameTokens.nextToken();
                    if (token.equals("Integrity")) targetSupports |= Integrity.value;
                    else if (token.equals("Confidentiality"))        targetSupports |= Confidentiality.value;
                    else if (token.equals("EstablishTrustInTarget")) targetSupports |= EstablishTrustInTarget.value;
                    else if (token.equals("EstablishTrustInClient")) targetSupports |= EstablishTrustInClient.value;
                    else if (token.equals("IdentityAssertion"))      targetSupports |= IdentityAssertion.value;
                    else if (token.equals("DelegationByClient"))     targetSupports |= DelegationByClient.value;
                    else org.jacorb.util.Debug.output(1, "Unknown SAS Association Taken: " + token);
                }
		String targetRequiresNames = Environment.getProperty( "jacorb.security.sas.tss.target_requires", "" );
                short targetRequires = (short)0;
                nameTokens = new StringTokenizer(targetRequiresNames, ":;, ");
                while (nameTokens.hasMoreTokens())
                {
                    String token = nameTokens.nextToken();
                    if (token.equals("Integrity")) targetRequires |= Integrity.value;
                    else if (token.equals("Confidentiality"))        targetRequires |= Confidentiality.value;
                    else if (token.equals("EstablishTrustInTarget")) targetRequires |= EstablishTrustInTarget.value;
                    else if (token.equals("EstablishTrustInClient")) targetRequires |= EstablishTrustInClient.value;
                    else if (token.equals("IdentityAssertion"))      targetRequires |= IdentityAssertion.value;
                    else if (token.equals("DelegationByClient"))     targetRequires |= DelegationByClient.value;
                    else org.jacorb.util.Debug.output(1, "Unknown SAS Association Taken: " + token);
                }

                // for now, no transport mechanizms
                TaggedComponent transportMech = new TaggedComponent(TAG_NULL_TAG.value, new byte[0]);

                // the AS_ContextSec
                byte[] targetName = Environment.getProperty( "jacorb.security.sas.tss.target_name").getBytes();
                short asTargetSupports = targetSupports;
                short asTargetRequires = targetRequires;

                // the SAS_ContextSec
                //String atlasLocator = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.locator", "URL");
                String atlasURL = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.url");
                String atlasCache = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.cacheid");
                ServiceConfiguration[] serviceConfiguration = null;
                if (atlasURL == null)
                {
                    serviceConfiguration = new ServiceConfiguration[0];
                }
                else
                {
                    if (atlasCache == null) atlasCache = "";
                    ATLASLocator atlasLoc = new ATLASLocator();
                    //String atlasLocator = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.URL");
                    //if (atlasLocator != null)
                    //{
                        atlasLoc.the_url(atlasURL);
                    //}
                    //atlasLocator = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.Naming");
                    //if (atlasLocator != null)
                    //{
                    //    atlasLoc.naming_locator(atlasLocator);
                    //}
                    ATLASProfile profile = new ATLASProfile();
                    profile.the_cache_id = atlasCache.getBytes();
                    profile.the_locator = atlasLoc;
                    byte[] cdrProfile = new byte[0];
                    //try
                    //{
                        org.omg.CORBA.Any any = orb.create_any();
                        ATLASProfileHelper.insert( any, profile );
                        cdrProfile = codec.encode(any);
                    //}
                    //catch (UnknownEncoding unknownEncoding)
                    //{
                    //    Debug.output( Debug.SECURITY | Debug.IMPORTANT, unknownEncoding);
                    //}
                    serviceConfiguration = new ServiceConfiguration[1];
                    serviceConfiguration[0] = new ServiceConfiguration(SCS_ATLAS.value, cdrProfile);
                }
                SAS_ContextSec sasContextSec = new SAS_ContextSec((short)0, (short)0, serviceConfiguration, new byte[0][0], 0);

                // create the security mech list
                int mechCnt = 0;
                for (int i = 1; i <= 16; i++)
                {
                    String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".oid");
                    if (mechOID != null) mechCnt++;
                }
                CompoundSecMech[] compoundSecMech = new CompoundSecMech[mechCnt];
                mechCnt = 0;
                for (int i = 1; i <= 16; i++)
                {
                    String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".oid");
                    if (mechOID == null) continue;
                    Oid oid = new Oid(mechOID);
                    byte[] clientAuthenticationMech = oid.getDER();
                    AS_ContextSec asContextSec = new AS_ContextSec(asTargetSupports, asTargetRequires, clientAuthenticationMech, targetName);
                    compoundSecMech[mechCnt++] = new CompoundSecMech(targetRequires, transportMech, asContextSec, sasContextSec);
                }
                CompoundSecMechList compoundSecMechList = new CompoundSecMechList(false, compoundSecMech);

                // export to tagged component
                CDROutputStream sasDataStream = new CDROutputStream( orb );
                sasDataStream.beginEncapsulatedArray();
                CompoundSecMechListHelper.write( sasDataStream , compoundSecMechList );
                tc = new TaggedComponent( TAG_CSI_SEC_MECH_LIST.value, sasDataStream.getBufferCopy() );
            }

            info.add_ior_component_to_profile (tc, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            Debug.output( Debug.SECURITY | Debug.IMPORTANT, e);
        }
    }
} // SASComponentInterceptor
