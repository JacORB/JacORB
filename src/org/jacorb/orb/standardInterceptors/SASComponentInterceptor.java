/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.avalon.framework.logger.Logger;
import org.ietf.jgss.Oid;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.security.sas.ISASContext;
import org.jacorb.util.Environment;
import org.omg.ATLAS.ATLASLocator;
import org.omg.ATLAS.ATLASProfile;
import org.omg.ATLAS.ATLASProfileHelper;
import org.omg.ATLAS.SCS_ATLAS;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DelegationByClient;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.IdentityAssertion;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.SAS_ContextSec;
import org.omg.CSIIOP.ServiceConfiguration;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

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
	/** the logger used by the naming service implementation */
	private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

    private ORB orb = null;
    private Codec codec = null;
    private TaggedComponent tc = null;
    private ISASContext sasContext = null;

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
            logger.error("Error initing SASComponentInterceptor: ",e);
        }
		String contextClass = org.jacorb.util.Environment.getProperty("jacorb.security.sas.contextClass");
		if (contextClass != null) {
			try {
				Class c = org.jacorb.util.Environment.classForName(contextClass);
			  	sasContext = (ISASContext)c.newInstance();
			} catch (Exception e) {
			  logger.error("Could not instantiate SAS Context class " + contextClass + ": " + e);
			}
		}
		if (sasContext == null) {
			logger.error("Could not load SAS context class: "+contextClass);
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
                    else org.jacorb.util.Debug.output("Unknown SAS Association Taken: " + token);
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
                    else org.jacorb.util.Debug.output("Unknown SAS Association Taken: " + token);
                }

                // for now, no transport mechanizms
                TaggedComponent transportMech = new TaggedComponent(TAG_NULL_TAG.value, new byte[0]);

                // the AS_ContextSec
                byte[] targetName = new byte[0];
                if (sasContext != null) {
                	targetName = sasContext.getCreatedPrincipal().getBytes();
                } else {
                	targetName = Environment.getProperty( "jacorb.security.sas.tss.target_name").getBytes();
                }
                
                short asTargetSupports = targetSupports;
                short asTargetRequires = targetRequires;

                // the SAS_ContextSec
				String atlasURL = org.jacorb.util.Environment.getProperty("jacorb.security.sas.atlas.url");
				if (atlasURL != null) atlasURL = URLDecoder.decode(atlasURL);
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
                    atlasLoc.the_url(atlasURL);
                    ATLASProfile profile = new ATLASProfile();
                    profile.the_cache_id = atlasCache.getBytes();
                    profile.the_locator = atlasLoc;
                    byte[] cdrProfile = new byte[0];
                    org.omg.CORBA.Any any = orb.create_any();
                    ATLASProfileHelper.insert( any, profile );
                    cdrProfile = codec.encode(any);
                    serviceConfiguration = new ServiceConfiguration[1];
                    serviceConfiguration[0] = new ServiceConfiguration(SCS_ATLAS.value, cdrProfile);
                }
                SAS_ContextSec sasContextSec = new SAS_ContextSec((short)0, (short)0, serviceConfiguration, new byte[0][0], 0);

                // create the security mech list
                boolean useStateful = Boolean.valueOf(org.jacorb.util.Environment.getProperty("jacorb.security.sas.stateful", "true")).booleanValue();
                CompoundSecMech[] compoundSecMech = new CompoundSecMech[1];
                String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism.oid");
                byte[] clientAuthenticationMech;
                if (mechOID == null) {
                    clientAuthenticationMech = new byte[0];
                } else {
                  Oid oid = new Oid(mechOID);
                  clientAuthenticationMech = oid.getDER();
                }
                AS_ContextSec asContextSec = new AS_ContextSec(asTargetSupports, asTargetRequires, clientAuthenticationMech, targetName);
                compoundSecMech[0] = new CompoundSecMech(targetRequires, transportMech, asContextSec, sasContextSec);
                CompoundSecMechList compoundSecMechList = new CompoundSecMechList(useStateful, compoundSecMech);

                // export to tagged component
                CDROutputStream sasDataStream = new CDROutputStream( orb );
                sasDataStream.beginEncapsulatedArray();
                CompoundSecMechListHelper.write( sasDataStream , compoundSecMechList );
                tc = new TaggedComponent( TAG_CSI_SEC_MECH_LIST.value, sasDataStream.getBufferCopy() );

                sasDataStream.release ();
                sasDataStream = null;
            }

            info.add_ior_component_to_profile (tc, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            logger.error("establish_components error: ", e);
        }
    }
} // SASComponentInterceptor
