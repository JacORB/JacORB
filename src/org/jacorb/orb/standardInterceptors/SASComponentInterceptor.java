package org.jacorb.orb.standardInterceptors;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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


import org.ietf.jgss.Oid;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.portableInterceptor.IORInfoImpl;
import org.jacorb.sasPolicy.ATLASPolicy;
import org.jacorb.sasPolicy.ATLASPolicyValues;
import org.jacorb.sasPolicy.ATLAS_POLICY_TYPE;
import org.jacorb.sasPolicy.SASPolicy;
import org.jacorb.sasPolicy.SASPolicyValues;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.jacorb.security.sas.ISASContext;
import org.omg.ATLAS.ATLASLocator;
import org.omg.ATLAS.ATLASProfile;
import org.omg.ATLAS.ATLASProfileHelper;
import org.omg.ATLAS.SCS_ATLAS;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.SAS_ContextSec;
import org.omg.CSIIOP.ServiceConfiguration;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_NULL_TAG;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.slf4j.Logger;

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
    /** the configuration object  */
    private final org.jacorb.config.Configuration config;

    /** the logger used by this implementation */
    private final Logger logger;

    private final ORB orb;
    private Codec codec;
    private TaggedComponent taggedComponent;
    private final ISASContext sasContext;

    public SASComponentInterceptor(ORBInitInfo info)
    {
        super();

        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB();
        config = orb.getConfiguration();
        logger = config.getLogger("jacorb.SAS.IOR");

        try
        {
            Encoding encoding =
                new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);

            CodecFactory codec_factory =
                (CodecFactory)orb.resolve_initial_references("CodecFactory");

            codec = codec_factory.create_codec(encoding);
        }
        catch (Exception e)
        {
            logger.error("Error initing SASComponentInterceptor: ",e);
        }

        sasContext = newSasContext();

        if (sasContext == null && logger.isErrorEnabled())
        {
            logger.error("Could not load SAS context class: "+ config.getAttribute("jacorb.security.sas.contextClass", ""));
        }
    }

    private ISASContext newSasContext()
    {
        try
        {
            return (ISASContext)config.getAttributeAsObject("jacorb.security.sas.contextClass");
        }
        catch (ConfigurationException e)
        {
            return null;
        }
    }

    // implementation of org.omg.PortableInterceptor.IORInterceptorOperations interface

    public String name()
    {
        return "SASComponentCreator";
    }

    public void destroy()
    {
        // nothing to do
    }

    /**
     * Builds an sas TaggedComponent.
     * Was formerly: ORB.makeSASComponent()
     */

    public void establish_components(IORInfo info)
    {
        // see if SAS policy is set
        if (sasContext == null)
        {
            return;
        }

        SASPolicyValues sasValues = null;
        try
        {
            SASPolicy policy =
                (SASPolicy)((IORInfoImpl)info).get_effective_policy(SAS_POLICY_TYPE.value);
            if (policy != null)
            {
                sasValues = policy.value();
            }
        }
        catch (BAD_PARAM e)
        {
            logger.debug("No SAS Policy");
        }
        catch (Exception e)
        {
            logger.warn("Error fetching SAS policy", e);
        }

        if (sasValues == null)
        {
            return;
        }

        if (sasValues.targetRequires == 0 && sasValues.targetSupports == 0)
        {
            return;
        }

        ATLASPolicyValues atlasValues = null;
        try
        {
            ATLASPolicy policy =
                (ATLASPolicy)info.get_effective_policy(ATLAS_POLICY_TYPE.value);
            if (policy != null)
            {
                atlasValues = policy.value();
            }
        }
        catch (BAD_PARAM e)
        {
            logger.debug("No ATLAS Policy");
        }
        catch (Exception e)
        {
            logger.warn("Error fetching ATLAS policy", e);
        }

        // generate SAS tag
        try
        {
            if( taggedComponent == null )
            {
                // for now, no transport mechanizms
                TaggedComponent transportMech =
                    new TaggedComponent(TAG_NULL_TAG.value, new byte[0]);

                // the AS_ContextSec
                byte[] targetName = sasContext.getClientPrincipal().getBytes();
                ServiceConfiguration[] serviceConfiguration = null;
                if (atlasValues == null)
                {
                    serviceConfiguration = new ServiceConfiguration[0];
                }
                else
                {
                    if (atlasValues.atlasCache == null)
                    {
                        atlasValues.atlasCache = "";
                    }
                    ATLASLocator atlasLoc = new ATLASLocator();
                    atlasLoc.the_url(atlasValues.atlasURL);
                    ATLASProfile profile = new ATLASProfile();
                    profile.the_cache_id = atlasValues.atlasCache.getBytes();
                    profile.the_locator = atlasLoc;
                    byte[] cdrProfile = new byte[0];
                    org.omg.CORBA.Any any = orb.create_any();
                    ATLASProfileHelper.insert( any, profile );
                    cdrProfile = codec.encode(any);
                    serviceConfiguration = new ServiceConfiguration[1];
                    serviceConfiguration[0] =
                        new ServiceConfiguration(SCS_ATLAS.value, cdrProfile);
                }
                SAS_ContextSec sasContextSec =
                    new SAS_ContextSec((short)0,
                                       (short)0,
                                       serviceConfiguration,
                                       new byte[0][0],
                                       0);

                // create the security mech list
                boolean useStateful =
                    config.getAttributeAsBoolean("jacorb.security.sas.stateful", true);

                CompoundSecMech[] compoundSecMech = new CompoundSecMech[1];
                Oid oid = new Oid(sasContext.getMechOID());
                byte[] clientAuthenticationMech = oid.getDER();

                AS_ContextSec asContextSec =
                    new AS_ContextSec(sasValues.targetSupports,
                                      sasValues.targetRequires,
                                      clientAuthenticationMech,
                                      targetName);
                compoundSecMech[0] =
                    new CompoundSecMech(sasValues.targetRequires,
                                        transportMech,
                                        asContextSec,
                                        sasContextSec);

                CompoundSecMechList compoundSecMechList =
                    new CompoundSecMechList(useStateful, compoundSecMech);

                // export to tagged component
                final CDROutputStream sasDataStream = new CDROutputStream( orb );

                try
                {
                    sasDataStream.beginEncapsulatedArray();
                    CompoundSecMechListHelper.write( sasDataStream , compoundSecMechList );
                    taggedComponent = new TaggedComponent( TAG_CSI_SEC_MECH_LIST.value,
                            sasDataStream.getBufferCopy() );
                }
                finally
                {
                    sasDataStream.close();
                }
            }

            info.add_ior_component_to_profile (taggedComponent, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            logger.error("establish_components error: ", e);
        }
    }
}
