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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.ietf.jgss.Oid;

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
import org.omg.PortableInterceptor.ORBInitInfo;

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
    private org.jacorb.config.Configuration config = null;

    /** the logger used by this implementation */
    private Logger logger = null;

    private ORB orb = null;
    private Codec codec = null;
    private TaggedComponent tc = null;
    private ISASContext sasContext = null;

    public SASComponentInterceptor(ORBInitInfo info)
    {
        orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB();
        config = orb.getConfiguration();
        logger = config.getNamedLogger("jacorb.SAS.IOR");

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

        String contextClass = null;

        try
        {
            config.getAttribute("jacorb.security.sas.contextClass");
        }
        catch( ConfigurationException ce )
        {
            // ignore;
        }
        if (contextClass != null) 
        {
            try 
            {
                Class c = org.jacorb.util.ObjectUtil.classForName(contextClass);
                sasContext = (ISASContext)c.newInstance();
            }
            catch (Exception e) 
            {
                logger.error("Could not instantiate SAS Context class " + 
                             contextClass + ": " + e);
            }
        }
        if (sasContext == null) 
        {
            logger.error("Could not load SAS context class: "+
                          contextClass);
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
        // see if SAS policy is set
        if (sasContext == null) 
            return;

        SASPolicyValues sasValues = null;
        try 
        {
            SASPolicy policy = 
                (SASPolicy)((IORInfoImpl)info).get_effective_policy(SAS_POLICY_TYPE.value);
            if (policy != null) 
                sasValues = policy.value();
        } 
        catch (BAD_PARAM e) 
        {
            logger.debug("No SAS Policy");
        } 
        catch (Exception e) 
        {
            logger.warn("Error fetching SAS policy: "+e);
        }

        if (sasValues == null) 
            return;

        if (sasValues.targetRequires == 0 && sasValues.targetSupports == 0) 
            return;

        ATLASPolicyValues atlasValues = null;
        try 
        {
            ATLASPolicy policy = 
                (ATLASPolicy)info.get_effective_policy(ATLAS_POLICY_TYPE.value);
            if (policy != null) 
                atlasValues = policy.value();
        } 
        catch (BAD_PARAM e) 
        {
            logger.debug("No ATLAS Policy");
        } 
        catch (Exception e) 
        {
            logger.warn("Error fetching ATLAS policy: "+e);
        }

        // generate SAS tag
        try
        {
            if( tc == null )
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
                        atlasValues.atlasCache = "";
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
                CDROutputStream sasDataStream = new CDROutputStream( orb );
                sasDataStream.beginEncapsulatedArray();
                CompoundSecMechListHelper.write( sasDataStream , compoundSecMechList );
                tc = new TaggedComponent( TAG_CSI_SEC_MECH_LIST.value, 
                                          sasDataStream.getBufferCopy() );

                sasDataStream.close ();
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
