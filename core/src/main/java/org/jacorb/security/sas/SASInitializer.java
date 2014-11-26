package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.standardInterceptors.SASComponentInterceptor;
import org.jacorb.sasPolicy.ATLAS_POLICY_TYPE;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.slf4j.Logger;

/**
 * This initializes the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 */

public class SASInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    private Logger logger = null;
    public static final int SecurityAttributeService = 15;
    public static int sasPrincipalNamePIC = (-1);

    /**
     * This method registers the interceptors.
     */
    public void post_init( ORBInitInfo info )
    {
        org.jacorb.orb.ORB orb =
            ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl)info).getORB ();
        logger =
            orb.getConfiguration().getLogger("org.jacorb.security.sas.log.verbosity");

        // install the TSS interceptor
        try
        {
            sasPrincipalNamePIC = info.allocate_slot_id();
            info.add_server_request_interceptor(new SASTargetInterceptor(info));
        }
        catch (ConfigurationException ce)
        {
            if (logger.isErrorEnabled())
                logger.error("ConfigurationException", ce);
        }
        catch (DuplicateName duplicateName)
        {
            if (logger.isErrorEnabled())
                logger.error("TSS DuplicateName", duplicateName);
        }
        catch (UnknownEncoding unknownEncoding)
        {
            if (logger.isErrorEnabled())
                logger.error("TSS UnknownEncoding", unknownEncoding);
        }

        // install the CSS interceptor
        try
        {
            info.add_client_request_interceptor(new SASClientInterceptor(info));
        }
        catch (ConfigurationException ce)
        {
            logger.error("ConfigurationException", ce);
            throw new org.omg.CORBA.INITIALIZE(ce.getMessage());
        }
        catch (DuplicateName duplicateName)
        {
            logger.error("CSS DuplicateName", duplicateName);
            throw new org.omg.CORBA.INITIALIZE(duplicateName.getMessage());
        }
        catch (UnknownEncoding unknownEncoding)
        {
            logger.error("CSS UnknownEncoding", unknownEncoding);
            throw new org.omg.CORBA.INITIALIZE(unknownEncoding.getMessage());
        }

        // install IOR interceptor
        try
        {
            info.add_ior_interceptor(new SASComponentInterceptor(info));
        }
        catch (DuplicateName duplicateName)
        {
            logger.error("IOR DuplicateName", duplicateName);
            throw new org.omg.CORBA.INITIALIZE(duplicateName.getMessage());
        }

        // create policy factory
        info.register_policy_factory( SAS_POLICY_TYPE.value, new SASPolicyFactory() );
        info.register_policy_factory( ATLAS_POLICY_TYPE.value, new ATLASPolicyFactory() );
    }

    public void pre_init(ORBInitInfo info)
    {
    }
}    // SAS setup Initializer
