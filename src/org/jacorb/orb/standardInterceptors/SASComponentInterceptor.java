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
import org.omg.CSIIOP.*;
import org.omg.GSSUP.*;
import org.ietf.jgss.*;

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
    private TaggedComponent tc = null;

    public SASComponentInterceptor( ORB orb )
    {
        this.orb = orb;
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
		String targetRequiresNames = Environment.getProperty( "jacorb.security.sas.server.target_requires", "" );
                short targetRequires = (short)0;
                StringTokenizer nameTokens = new StringTokenizer(targetRequiresNames, ":;, ");
                while (nameTokens.hasMoreTokens())
                {
                    String token = nameTokens.nextToken();
                    if (token.equals("Integrity")) targetRequires |= Integrity.value;
                    else if (token.equals("Confidentiality")) targetRequires |= Confidentiality.value;
                    else if (token.equals("EstablishTrustInTarget")) targetRequires |= EstablishTrustInTarget.value;
                    else if (token.equals("EstablishTrustInClient")) targetRequires |= EstablishTrustInClient.value;
                    else if (token.equals("IdentityAssertion")) targetRequires |= IdentityAssertion.value;
                    else if (token.equals("DelegationByClient")) targetRequires |= DelegationByClient.value;
                    else org.jacorb.util.Debug.output(1, "Unknown SAS Association Taken: " + token);
                }

                // for now, no transport mechanizms
                TaggedComponent transportMech = new TaggedComponent(TAG_NULL_TAG.value, new byte[0]);

                // the AS_ContextSec
                byte[] targetName = Environment.getProperty( "jacorb.security.sas.server.target_name").getBytes();
                short asTargetSupports = targetRequires;
                short asTargetRequires = targetRequires;

                // the SAS_ContextSec
                ServiceConfiguration[] serviceConfiguration = { new ServiceConfiguration(0, new byte[0]) };
                SAS_ContextSec sasContextSec = new SAS_ContextSec((short)0, (short)0, serviceConfiguration, new byte[0][0], 0);

                // create the security mech list
                Oid[] mechs = org.jacorb.security.sas.TSSInitializer.gssManager.getMechs();
                CompoundSecMech[] compoundSecMech = new CompoundSecMech[mechs.length-1];
                for (int i = 1; i < mechs.length; i++)
                {
                  byte[] clientAuthenticationMech = mechs[i].getDER();
                  AS_ContextSec asContextSec = new AS_ContextSec(asTargetSupports, asTargetRequires, clientAuthenticationMech, targetName);
                  compoundSecMech[i-1] = new CompoundSecMech(targetRequires, transportMech, asContextSec, sasContextSec);
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
