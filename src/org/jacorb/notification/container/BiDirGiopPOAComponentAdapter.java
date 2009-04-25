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

package org.jacorb.notification.container;

import java.util.ArrayList;
import java.util.List;

import org.jacorb.config.*;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.util.LogUtil;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.DecoratingComponentAdapter;

public class BiDirGiopPOAComponentAdapter extends DecoratingComponentAdapter
{
    private static final long serialVersionUID = 1L;

    private static final String BIDIR_GIOP_OPTION = "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init";

    public BiDirGiopPOAComponentAdapter(ComponentAdapter delegate)
    {
        super(delegate);
    }

    private static boolean isBiDirGiopEnabled(Configuration config)
    {
        return (config.getAttribute(BIDIR_GIOP_OPTION, null) != null);
    }

    private static Policy newBiDirGiopPolicy(ORB orb) throws PolicyError
    {
        final Any _any = orb.create_any();

        BidirectionalPolicyValueHelper.insert(_any, BOTH.value);

        final Policy _policy = orb.create_policy(BIDIRECTIONAL_POLICY_TYPE.value, _any);

        return _policy;
    }

    public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
            PicoIntrospectionException
    {
        final POA rootPOA = (POA) super.getComponentInstance(container);

        final Configuration config = (Configuration) container.getComponentInstanceOfType(Configuration.class);

        final Logger _logger = LogUtil.getLogger(config, getClass().getName());

        try
        {
            final ORB orb = (ORB) container.getComponentInstanceOfType(ORB.class);

            final List _policyList = new ArrayList();

            _policyList.add(rootPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION));

            addBiDirGiopPolicy(_policyList, orb, config);

            if (isBiDirGiopEnabled(config) && _logger.isInfoEnabled())
            {
                _logger.info(BIDIR_GIOP_OPTION 
                             + " is set:"
                             + " Will enable Bidirectional GIOP.");
            }
            
            org.omg.CORBA.Policy[] _policies = (org.omg.CORBA.Policy[]) _policyList
                    .toArray(new org.omg.CORBA.Policy[_policyList.size()]);

            POA poa = rootPOA.create_POA("NotifyServicePOA", rootPOA.the_POAManager(), _policies);

            for (int x = 0; x < _policies.length; ++x)
            {
                _policies[x].destroy();
            }

            return poa;
        } catch (UserException e)
        {
            throw new PicoInitializationException("Error enabling BiDirectional GIOP for POA", e);
        }
    }

    
    /**
     * add an optional Policy to enable Bidirectional GIOP to the supplied list.
     * the decision if BiDir GIOP should be enabled is based on the Configuration settings.
     * 
     * @param policies will be modified by this method if BiDir GIOP is enabled
     * @param orb
     * @param config
     * @throws PolicyError
     */
    public static void addBiDirGiopPolicy(List policies, ORB orb, Configuration config) throws PolicyError
    {
        if (isBiDirGiopEnabled(config))
        {
            Policy policy = newBiDirGiopPolicy(orb);
            policies.add(policy);
        }
    }
}
