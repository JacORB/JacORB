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

import java.lang.reflect.Constructor;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.engine.ConfigurablePushTaskExecutorFactory;
import org.jacorb.notification.engine.DefaultTaskFactory;
import org.jacorb.notification.engine.DefaultTaskProcessor;
import org.jacorb.notification.engine.PushTaskExecutorFactory;
import org.jacorb.notification.filter.impl.DefaultETCLEvaluator;
import org.jacorb.notification.impl.DefaultEvaluationContextFactory;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.impl.PoolingEvaluationContextFactory;
import org.jacorb.notification.interfaces.EvaluationContextFactory;
import org.jacorb.orb.ORB;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.BasicComponentParameter;
import org.picocontainer.defaults.CachingComponentAdapter;
import org.picocontainer.defaults.CachingComponentAdapterFactory;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapterFactory;
import org.picocontainer.defaults.DefaultLifecycleStrategy;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.monitors.DefaultComponentMonitor;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class PicoContainerFactory
{
    public static MutablePicoContainer createRootContainer(ORB orb)
    {
        return createRootContainer(null, orb);
    }

    public static MutablePicoContainer createRootContainer(MutablePicoContainer optionalParent, ORB orb)
    {
        final org.jacorb.config.Configuration _config = orb.getConfiguration();
        final Logger _logger = _config.getNamedLogger(PicoContainerFactory.class.getName());

        final MutablePicoContainer _container;

        if (optionalParent == null)
        {
            _container = createContainer(_logger);
        }
        else
        {
            _container = createContainer(optionalParent, _logger);
        }

        // register existing orb
        _container.registerComponentInstance(org.omg.CORBA.ORB.class, orb);

        registerORBServices(_container);

        registerCoreServices(_container);

        return _container;
    }

    private static void registerCoreServices(final MutablePicoContainer container)
    {
        container.registerComponent(new CachingComponentAdapter(
                new PushTaskExecutorFactoryComponentAdapter(
                        new ConstructorInjectionComponentAdapter(PushTaskExecutorFactory.class, ConfigurablePushTaskExecutorFactory.class))));

        // etcl evaluator
        container.registerComponentImplementation(DefaultETCLEvaluator.class);

        // message factory
        container.registerComponentImplementation(DefaultMessageFactory.class);

        // taskprocessor
        container.registerComponentImplementation(DefaultTaskFactory.class);
        container.registerComponentImplementation(DefaultTaskProcessor.class);

        registerEvaluationContextFactory(container);
    }

    private static void registerEvaluationContextFactory(final MutablePicoContainer container)
    {
        // PoolingEvaluationContextFactory depends on DefaultEvaluationContextFactory.
        // however both implement the same interface users depend on which causes an ambiguity.
        // therefore DefaultEvaluationContextFactory should be only visible to PoolingEvaluationContextFactory.
        final ConstructorInjectionComponentAdapter _serviceCA =
            new ConstructorInjectionComponentAdapter(DefaultEvaluationContextFactory.class, DefaultEvaluationContextFactory.class);

        final ConstructorInjectionComponentAdapter _poolingServiceCA =
            new ConstructorInjectionComponentAdapter(EvaluationContextFactory.class, PoolingEvaluationContextFactory.class, new Parameter[] {BasicComponentParameter.BASIC_DEFAULT, new BasicComponentParameter(DefaultEvaluationContextFactory.class)});

        final LocalParameterComponentAdapter _localParamCA =
            new LocalParameterComponentAdapter(_poolingServiceCA, new ComponentAdapter[] {_serviceCA});

        final ComponentAdapter _cachingCA =
            new CachingComponentAdapter(_localParamCA);

        container.registerComponent(_cachingCA);
    }

    private static void registerORBServices(final MutablePicoContainer container)
    {
        // register services that need to be looked up via orb using custom componentadapter

        // POA
        container.registerComponent(new CachingComponentAdapter(new BiDirGiopPOAComponentAdapter(new POAComponentAdapter())));

        // DynAnyFactory
        container.registerComponent(new CachingComponentAdapter(new DynAnyFactoryComponentAdapter()));

        // Configuration
        container.registerComponent(new CachingComponentAdapter(new ConfigurationComponentAdapter()));

        // FilterFactory
        container.registerComponent(new CachingComponentAdapter(new FilterFactoryComponentAdapter()));

        // IFR
        container.registerComponent(new CachingComponentAdapter(new RepositoryComponentAdapter()));

        // CurrentTimeUtil
        container.registerComponent(new CurrentTimeUtilComponentAdapter());
    }

    private static MutablePicoContainer createContainer(MutablePicoContainer parent, final Logger logger)
    {
        final MutablePicoContainer _container;
        _container = parent.makeChildContainer();

        logger.debug("Created Container with Parent");
        return _container;
    }

    private static MutablePicoContainer createContainer(final Logger logger)
    {
        final ConstructorInjectionComponentAdapterFactory _nonCachingCAFactory = new ConstructorInjectionComponentAdapterFactory(); //false, new ConsoleComponentMonitor(System.out));
        final ComponentAdapterFactory _cachingCAFactory = new CachingComponentAdapterFactory(_nonCachingCAFactory);
        final MutablePicoContainer _container = new DefaultPicoContainer(_cachingCAFactory);

        _container.registerComponentInstance(ComponentAdapterFactory.class,
                _nonCachingCAFactory);

        logger.debug("Created Top Level Container");
        return _container;
    }

    public static MutablePicoContainer createChildContainer(MutablePicoContainer parent)
    {
        return parent.makeChildContainer();
    }


    /**
     * helper method for easier debugging of unresolved dependencies.
     *
     * do NOT delete even if method is not referenced.
     */
    public static void dumpDependencies(PicoContainer container, Class clazzToBeCreated)
    {
        try
        {
            Constructor[] _ctors = clazzToBeCreated.getConstructors();

            StringBuffer _buffer = new StringBuffer();
            for (int i = 0; i < _ctors.length; i++)
            {
                Constructor _ctor = _ctors[i];

                _buffer.append(_ctor);
                _buffer.append("\n");
                Class[] _params = _ctor.getParameterTypes();

                for (int j = 0; j < _params.length; j++)
                {
                    Class _param = _params[j];

                    boolean resolvable = container.getComponentInstanceOfType(_param) != null;
                    _buffer.append(j);
                    _buffer.append(": ");
                    _buffer.append(_param);
                    _buffer.append(" -> ");
                    _buffer.append(resolvable);
                    _buffer.append("\n");
                }
            }

            System.err.println(_buffer.toString());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}