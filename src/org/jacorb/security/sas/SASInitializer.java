package org.jacorb.security.sas;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2002-2003 Gerald Brose
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

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.standardInterceptors.SASComponentInterceptor;
import org.jacorb.sasPolicy.ATLAS_POLICY_TYPE;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

/**
 * This initializes the SAS Target Security Service (TSS) Interceptor
 *
 * @author David Robison
 * @version $Id$
 */

public class SASInitializer
        extends org.omg.CORBA.LocalObject
        implements ORBInitializer
{
	/** the logger used by the naming service implementation */
	private static Logger logger = org.jacorb.util.Debug.getNamedLogger("jacorb.SAS");

    public static final int SecurityAttributeService = 15;

    public static int sasPrincipalNamePIC = (-1);

    /**
    * This method registers the interceptors.
    */
    public void post_init( ORBInitInfo info )
    {
		// install the TSS interceptor
		try
		{
			sasPrincipalNamePIC = info.allocate_slot_id();
			info.add_server_request_interceptor(new SASTargetInterceptor(info));
		}
		catch (DuplicateName duplicateName)
		{
			logger.error("TSS DuplicateName", duplicateName);
		}
		catch (UnknownEncoding unknownEncoding)
		{
			logger.error("TSS UnknownEncoding", unknownEncoding);
		}
		
		// install the CSS interceptor
		try
		{
			info.add_client_request_interceptor(new SASClientInterceptor(info));
		}
		catch (DuplicateName duplicateName)
		{
			logger.error("CSS DuplicateName", duplicateName);
		}
		catch (UnknownEncoding unknownEncoding)
		{
			logger.error("CSS UnknownEncoding", unknownEncoding);
		}
		
		// install IOR interceptor
		try
		{
			info.add_ior_interceptor(new SASComponentInterceptor(info));
		}
		catch (DuplicateName duplicateName)
		{
			logger.error("IOR DuplicateName", duplicateName);
		}
		
		// create policy factory
		info.register_policy_factory( SAS_POLICY_TYPE.value, new SASPolicyFactory() );
		info.register_policy_factory( ATLAS_POLICY_TYPE.value, new ATLASPolicyFactory() );
    }

    public void pre_init(ORBInitInfo info)
    {
    }
}    // SAS setup Initializer
