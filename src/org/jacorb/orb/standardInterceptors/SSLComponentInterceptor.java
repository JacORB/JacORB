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
package org.jacorb.orb.standardInterceptors;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.slf4j.Logger;

/**
 * This interceptor creates an ssl TaggedComponent
 *
 * @author Nicolas Noffke
 */

public class SSLComponentInterceptor
    extends org.omg.CORBA.LocalObject
    implements IORInterceptor, Configurable
{
    private final ORB orb;
    private final Logger logger;
    private TaggedComponent tc = null;
    private int supported = 0;
    private int required = 0;

    public SSLComponentInterceptor( ORB orb )
        throws ConfigurationException
   {
        this.orb = orb;
        configure( orb.getConfiguration());
        logger = orb.getConfiguration().getLogger(getClass().getName());
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        supported = configuration.getAttributeAsInteger("jacorb.security.ssl.server.supported_options", 0x20, 16); // 16 is the base as we take the string value as hex!

        required = configuration.getAttributeAsInteger("jacorb.security.ssl.server.required_options", 0, 16);

    }

    // implementation of org.omg.PortableInterceptor.IORInterceptorOperations interface
    public String name()
    {
        return "SSLComponentCreator";
    }

    public void destroy()
    {
    }

    /**
     * Builds an ssl TaggedComponent.
     * Was formerly: ORB.makeSSLComponent()
     */

    /*
        typedef unsigned short   AssociationOptions;

        const AssociationOptions NoProtection = 1; 0x001
        const AssociationOptions Integrity = 2; 0x002
        const AssociationOptions Confidentiality = 4; 0x004
        const AssociationOptions DetectReplay = 8; 0x008
        const AssociationOptions DetectMisordering = 16;0x010
        const AssociationOptions EstablishTrustInTarget = 32; 0x020
        const AssociationOptions EstablishTrustInClient = 64; 0x040
        const AssociationOptions NoDelegation = 128; 0x080
        const AssociationOptions SimpleDelegation = 256; 0x100
        const AssociationOptions CompositeDelegation = 512; 0x200
     */

    public void establish_components(IORInfo info)
    {
        try
        {
            if( tc == null )
            {
                SSL ssl =
                    new SSL ( (short)supported,
                              (short)required,
                              (short)orb.getBasicAdapter().getSSLPort());

                //we don't support delegation 0x80 -> NoDelegation we don't
                //care if the other side delegates, so no required options are
                //set.
                ssl.target_supports |= 0x80;

                //this is SSLs default behaviour, included for completeness
                ssl.target_supports |= 0x20; //establish trust in target

                CDROutputStream sslDataStream =
                    new CDROutputStream( orb );

                sslDataStream.beginEncapsulatedArray();

                SSLHelper.write( sslDataStream , ssl );

                tc = new TaggedComponent( org.omg.SSLIOP.TAG_SSL_SEC_TRANS.value,
                                          sslDataStream.getBufferCopy() );

                sslDataStream.close ();
                sslDataStream = null;
            }

            info.add_ior_component_to_profile (tc, TAG_INTERNET_IOP.value);
        }
        catch (Exception e)
        {
            logger.error("unexpected exception", e);
            throw new INTERNAL(e.toString());
        }
    }
} // SSLComponentInterceptor
