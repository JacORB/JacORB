/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.omg.PortableInterceptor.*;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import org.jacorb.orb.*;
import org.jacorb.orb.giop.CodeSet;

/**
 * This interceptor creates a codeset TaggedComponent.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class CodeSetInfoInterceptor
    extends org.omg.CORBA.LocalObject
    implements IORInterceptor, Configurable
{
    private org.omg.IOP.TaggedComponent tagc = null;
    private org.apache.avalon.framework.logger.Logger logger = null;

    public CodeSetInfoInterceptor(ORB orb)
    {
        try {
            configure (orb.getConfiguration());
        } catch (ConfigurationException ex){
            // will have to do with defaults
        }

        // create the info
        org.omg.CONV_FRAME.CodeSetComponentInfo cs_info =
            new org.omg.CONV_FRAME.CodeSetComponentInfo();

        // fill the info
        cs_info.ForCharData =
            new org.omg.CONV_FRAME.
                CodeSetComponent( CodeSet.getTCSDefault(),
                                  new int[] { CodeSet.getConversionDefault() } );

        cs_info.ForWcharData =
            new org.omg.CONV_FRAME.
                CodeSetComponent( CodeSet.getTCSWDefault(),
                                  new int[] { CodeSet.UTF8 } );

        // encapsulate it into TaggedComponent
        CDROutputStream os = new CDROutputStream( orb );
        os.beginEncapsulatedArray();
        org.omg.CONV_FRAME.CodeSetComponentInfoHelper.write( os, cs_info );

        tagc =
            new org.omg.IOP.TaggedComponent( org.omg.IOP.TAG_CODE_SETS.value,
                                             os.getBufferCopy());

        os.close ();
        os = null;
        cs_info = null;
    }


    public void configure(Configuration config)
        throws ConfigurationException
    {
        org.jacorb.config.Configuration cfg =
            (org.jacorb.config.Configuration)config;

        logger = cfg.getNamedLogger("org.jacorb.interceptors.ior_init");

        String ncsc =
            config.getAttribute("jacorb.native_char_codeset", "ISO-8859-1");

        String ncsw =
            config.getAttribute("jacorb.native_wchar_codeset", "UTF-16");

        if (CodeSet.setNCSC (ncsc) == -1 &&
            logger.isErrorEnabled())
            logger.error("Cannot set default NCSC to " + ncsc);

        if (CodeSet.setNCSW (ncsw) == -1 &&
            logger.isErrorEnabled())
            logger.error("Cannot set default NCSW to " + ncsw);

    }

    public String name()
    {
        return "CodeSetInfoComponentCreator";
    }

    public void destroy()
    {
    }

    /**
     * Creates default IOR codeset  component.
     */

    public void establish_components( IORInfo info, int [] tags )
    {
        if (tags == null)
            {
                info.add_ior_component_to_profile( tagc,
                                                   org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value );
                info.add_ior_component_to_profile( tagc,
                                                   org.omg.IOP.TAG_INTERNET_IOP.value );
            }
        else
            for (int i = 0; i < tags.length; i++)
                info.add_ior_component_to_profile(tagc, tags[i]);
    }

    public void establish_components( IORInfo info )
    {
        info.add_ior_component_to_profile( tagc,
                                           org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value );
        info.add_ior_component_to_profile( tagc,
                                           org.omg.IOP.TAG_INTERNET_IOP.value );
    }
}
