package org.jacorb.test.orb.policies;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.NullLogger;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.policies.PolicyManager;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
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

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PolicyManagerTest extends TestCase
{
    private PolicyManager objectUnderTest;
    private Policy policy1Mock;
    private Policy policy2Mock;

    protected void setUp() throws Exception
    {
        final MockControl configControl = MockControl.createControl(Configuration.class);
        final Configuration configMock = (Configuration) configControl.getMock();

        configControl.expectAndReturn(configMock.getNamedLogger("jacorb.orb.policies"), new NullLogger());

        configControl.replay();

        objectUnderTest = new PolicyManager(configMock);

        configControl.verify();

        MockControl policy1Control = MockControl.createControl(Policy.class);
        policy1Mock = (Policy) policy1Control.getMock();
        policy1Mock.policy_type();
        policy1Control.setReturnValue(1, MockControl.ZERO_OR_MORE);

        MockControl policy2Control = MockControl.createControl(Policy.class);
        policy2Mock = (Policy) policy2Control.getMock();
        policy2Mock.policy_type();
        policy2Control.setReturnValue(2, MockControl.ZERO_OR_MORE);

        policy1Control.replay();
        policy2Control.replay();
    }

    public void testAddOverride() throws Exception
    {
        objectUnderTest.set_policy_overrides(new Policy[] {policy1Mock}, SetOverrideType.SET_OVERRIDE);

        objectUnderTest.set_policy_overrides(new Policy[] {policy2Mock}, SetOverrideType.ADD_OVERRIDE);

        Policy[] result = objectUnderTest.get_policy_overrides(new int[] {1});
        assertEquals("first policy should still be there!", 1, result.length);
        assertEquals("first policy should still be there!", 1, result[0].policy_type());
    }

    public void testSetOverride() throws Exception
    {
        objectUnderTest.set_policy_overrides(new Policy[] {policy1Mock}, SetOverrideType.SET_OVERRIDE);

        objectUnderTest.set_policy_overrides(new Policy[] {policy2Mock}, SetOverrideType.SET_OVERRIDE);

        Policy[] result = objectUnderTest.get_policy_overrides(new int[] {1});
        assertEquals("first policy shouldn't be there", 0, result.length);
    }
}
