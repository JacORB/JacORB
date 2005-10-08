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

package org.jacorb.test.notification.container;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jacorb.notification.container.LocalParameterComponentAdapter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

public class LocalParameterComponentAdapterTest extends TestCase
{
    private DefaultPicoContainer picoContainer_;
    
    public void testCreateWithoutDeps() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DefaultService.class);
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, new ComponentAdapter[0]));
        
        assertNotNull(picoContainer_.getComponentInstanceOfType(Service.class));
    }
    
    public void testCreateFailsWhenDepsNotResolvable() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, new ComponentAdapter[0]));
        
        try
        {
            picoContainer_.getComponentInstanceOfType(Service.class);
            fail();
        } catch (Exception e)
        {
            // expected
        }
    }
    
    public void testCreateWithDeps() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        
        assertNotNull(picoContainer_.getComponentInstanceOfType(Service.class));
    }

    public void testLocalCAsAreNotAccessible() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        
        assertNull(picoContainer_.getComponentInstanceOfType(DefaultService.class));
        assertNull(picoContainer_.getComponentAdapterOfType(DefaultService.class));
    }
    
    public void testInContainer() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        picoContainer_.registerComponentImplementation(DependsOnService.class);
        
        assertNotNull(picoContainer_.getComponentInstance(DependsOnService.class));
    }
    
    public void setUp() throws Exception
    {
        picoContainer_ = new DefaultPicoContainer();
    }

    public static Test suite()
    {
        return new TestSuite(LocalParameterComponentAdapterTest.class);
    }
}
