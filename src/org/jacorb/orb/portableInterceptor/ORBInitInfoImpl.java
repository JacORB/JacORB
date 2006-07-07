package org.jacorb.orb.portableInterceptor;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.Object;
import org.omg.IOP.CodecFactory;

import java.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.ORB;

/**
 * This class represents the type of info object
 * that will be passed to the ORBInitializers. <br>
 * See PI Spec p. 9-70ff
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ORBInitInfoImpl
    extends org.omg.CORBA.LocalObject
    implements ORBInitInfo
{
    private int slot_count = 0;
    private final ORB orb;
    private final Logger logger;

    private final Map named_server_interceptors;
    private final List anonymous_server_interceptors;

    private final Map named_client_interceptors;
    private final List anonymous_client_interceptors;

    private final Map named_ior_interceptors;
    private final List anonymous_ior_interceptors;

    private final Map policy_factories;

    private boolean valid = true;

    public ORBInitInfoImpl(ORB orb)
    {
        this.orb = orb;

        logger = orb.getConfiguration().getNamedLogger("jacorb.orb");

        named_server_interceptors = new HashMap();
        named_client_interceptors = new HashMap();

        anonymous_server_interceptors = new ArrayList();
        anonymous_client_interceptors = new ArrayList();

        named_ior_interceptors = new HashMap();
        anonymous_ior_interceptors = new ArrayList();

        policy_factories = new HashMap();
    }

    /**
     * This method is for interceptors that need access to the ORB.
     * Be careful with that since there is a reason, why there is no
     * other way to get access to the ORB.
     */
    public ORB getORB()
    {
        return orb;
    }

    public void setInvalid()
    {
        valid = false;
    }

    /**
     * Copies the elements of a Map into
     * a List.
     */

    private List merge(List target, Map source)
    {
        List result = new ArrayList(target);
        result.addAll(source.values());
        return result;
    }

    public List getClientInterceptors()
    {
        return merge(anonymous_client_interceptors,
                named_client_interceptors);
    }

    public List getServerInterceptors()
    {
        return merge(anonymous_server_interceptors,
                named_server_interceptors);
    }

    public List getIORInterceptors()
    {
        return merge(anonymous_ior_interceptors,
                named_ior_interceptors);
    }

    public Map getPolicyFactories()
    {
        return policy_factories;
    }

    public int getSlotCount()
    {
        return slot_count;
    }

    // implementation of org.omg.PortableInterceptor.ORBInitInfoOperations interface
    public void add_client_request_interceptor(ClientRequestInterceptor interceptor)
        throws DuplicateName
    {
        checkIsValid();

        checkInterceptorName(interceptor);

        if (interceptor.name().length() == 0)
        {
            anonymous_client_interceptors.add(interceptor);
        }
        else
        {
            if (named_client_interceptors.containsKey(interceptor.name()))
            {
                throw new DuplicateName(interceptor.name());
            }

            named_client_interceptors.put(interceptor.name(), interceptor);
        }
    }

    private void checkIsValid()
    {
        if (! valid)
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("This ORBInitIfo is not valid anymore!");
        }
    }

    private void checkInterceptorName(Interceptor interceptor) throws DuplicateName
    {
        if (interceptor.name() == null)
        {
            throw new DuplicateName("the name is null");
        }
    }

    public void add_ior_interceptor(IORInterceptor interceptor)
        throws DuplicateName
    {
        checkInterceptorName(interceptor);

        if (interceptor.name().length() == 0)
        {
            anonymous_ior_interceptors.add(interceptor);
        }
        else
        {
            if (named_ior_interceptors.containsKey(interceptor.name()))
            {
                throw new DuplicateName(interceptor.name());
            }

            named_ior_interceptors.put(interceptor.name(), interceptor);
        }
    }

    public void add_server_request_interceptor(ServerRequestInterceptor interceptor)
        throws DuplicateName
    {
        checkIsValid();

        checkInterceptorName(interceptor);

        if (interceptor.name().length() == 0)
        {
            anonymous_server_interceptors.add(interceptor);
        }
        else
        {
            if (named_server_interceptors.containsKey(interceptor.name()))
            {
                throw new DuplicateName(interceptor.name());
            }

            named_server_interceptors.put(interceptor.name(), interceptor);
        }
    }

    public int allocate_slot_id()
    {
        checkIsValid();

        return slot_count++;
    }

    public String[] arguments()
    {
        checkIsValid();

        return orb._args ;
    }

    public CodecFactory codec_factory()
    {
        checkIsValid();

        try
        {
            return (CodecFactory) orb.resolve_initial_references("CodecFactory");
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName e)
        {
            logger.fatalError("unexpected error", e);
            throw new INTERNAL(e.toString());
        }
    }

    public String orb_id()
    {
        checkIsValid();

        return ORB.orb_id;
    }

    public void register_initial_reference( String id, Object obj )
        throws InvalidName
    {
        checkIsValid();

        try
        {
            orb.register_initial_reference(id, obj);
        }
        catch(org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new InvalidName();
        }
    }

    public void register_policy_factory(int type, PolicyFactory policy_factory)
    {
        checkIsValid();

        if (policy_factory == null)
        {
            throw new org.omg.CORBA.BAD_PARAM("Actual parameter policy_factory is null!");
        }

        final Integer key = new Integer(type);
        if (policy_factories.containsKey(key))
        {
            throw new org.omg.CORBA.BAD_INV_ORDER("A PolicyFactory for type " + type +
                                                  " has already been registered!", 12,
                                                  org.omg.CORBA.CompletionStatus.
                                                  COMPLETED_MAYBE);
        }

        policy_factories.put(key, policy_factory);
    }


    public org.omg.CORBA.Object resolve_initial_references(String id)
        throws InvalidName
    {
        checkIsValid();

        try
        {
            return orb.resolve_initial_references(id);
        }
        catch(org.omg.CORBA.ORBPackage.InvalidName e)
        {
            throw new InvalidName();
        }
    }
}
