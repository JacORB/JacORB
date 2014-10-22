package org.jacorb.orb.standardInterceptors;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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


import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

/**
 * This interceptor creates a codeset TaggedComponent.
 *
 */
public class CodeSetInfoInterceptor extends LocalObject
    implements IORInterceptor, Configurable
{
    private final TaggedComponent tagc;

    public CodeSetInfoInterceptor(ORB orb) throws ConfigurationException
    {
        super();

        configure(orb.getConfiguration());

        // encapsulate it into TaggedComponent
        final CDROutputStream out = new CDROutputStream( orb );
        try
        {
            out.beginEncapsulatedArray();
            CodeSetComponentInfoHelper.write( out, orb.getLocalCodeSetComponentInfo() );

            tagc = new TaggedComponent(TAG_CODE_SETS.value, out.getBufferCopy());
        }
        finally
        {
            out.close();
        }
    }


    @Override
    public void configure(Configuration config)
        throws ConfigurationException
    {
    }

    @Override
    public String name()
    {
        return "CodeSetInfoComponentCreator";
    }

    @Override
    public void destroy()
    {
        // nothing to do
    }

    /**
     * Creates default IOR codeset  component.
     */
    @Override
    public void establish_components( IORInfo info )
    {
        info.add_ior_component_to_profile( tagc, TAG_INTERNET_IOP.value );
    }
}
