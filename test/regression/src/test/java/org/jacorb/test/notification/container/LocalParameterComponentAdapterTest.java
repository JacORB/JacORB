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

package org.jacorb.test.notification.container;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.jacorb.notification.container.LocalParameterComponentAdapter;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

public class LocalParameterComponentAdapterTest
{
    private DefaultPicoContainer picoContainer_;
    
    @Test
    public void testCreateWithoutDeps() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DefaultService.class);
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, new ComponentAdapter[0]));
        
        assertNotNull(picoContainer_.getComponentInstanceOfType(Service.class));
    }
    
    @Test
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
    
    @Test
    public void testCreateWithDeps() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        
        assertNotNull(picoContainer_.getComponentInstanceOfType(Service.class));
    }

    @Test
    public void testLocalCAsAreNotAccessible() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        
        assertNull(picoContainer_.getComponentInstanceOfType(DefaultService.class));
        assertNull(picoContainer_.getComponentAdapterOfType(DefaultService.class));
    }
    
    @Test
    public void testInContainer() throws Exception
    {
        ConstructorInjectionComponentAdapter ca = new ConstructorInjectionComponentAdapter(Service.class, DecoratedService.class);
        ComponentAdapter[] localCAs = new ComponentAdapter[] {new ConstructorInjectionComponentAdapter(DefaultService.class, DefaultService.class)};
        
        picoContainer_.registerComponent(new LocalParameterComponentAdapter(ca, localCAs));
        picoContainer_.registerComponentImplementation(DependsOnService.class);
        
        assertNotNull(picoContainer_.getComponentInstance(DependsOnService.class));
    }
    
    @Before
    public void setUp() throws Exception
    {
        picoContainer_ = new DefaultPicoContainer();
    }
}
