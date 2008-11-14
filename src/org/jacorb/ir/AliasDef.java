package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
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

import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;

public class AliasDef
    extends TypedefDef
    implements org.omg.CORBA.AliasDefOperations
{
    private org.omg.CORBA.IDLType original_type_def;
    private Logger logger;
    private POA poa;

    public AliasDef( org.omg.CORBA.TypeCode type,
                     org.omg.CORBA.Container defined_in,
                     org.omg.CORBA.Repository containing_repository,
                     Logger logger,
                     POA poa)
    {
        this.logger = logger;
        this.poa = poa;
        def_kind = org.omg.CORBA.DefinitionKind.dk_Alias;
        this.type = type;
        this.containing_repository = containing_repository;
        this.defined_in = defined_in;
        try
        {
            name( type.name() );
            id( type.id() );
            version = ("1.0");
            absolute_name = org.omg.CORBA.ContainedHelper.narrow( defined_in ).absolute_name() +
                "::" + name();
        }
        catch( Exception e )
        {
            this.logger.error("Caught Exception", e); // should not happen
        }
        
        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("New AliasDef name: " + name());
        }
    }

    public org.omg.CORBA.IDLType original_type_def()
    {
        if (original_type_def == null)
        {
           throw new INTF_REPOS ("Alias " + name () + " has null original_type_def" );
        }
        return original_type_def;
    }

    public void original_type_def(org.omg.CORBA.IDLType arg)
    {
        original_type_def = arg;
    }

    public void define()
    {
        try
        {
            original_type_def( IDLType.create( type().content_type(),
                                               containing_repository,
                                               true,
                                               this.logger,
                                               this.poa)
                               );
        }
        catch( Exception e )
        {
            this.logger.error("Caught Exception", e); // should not happen
        }
    }

    public  org.omg.CORBA.ContainedPackage.Description describe()
    {
        org.omg.CORBA.Any a = orb.create_any();
        String containerId;
        if( defined_in._is_a("IDL:omg.org/CORBA/Contained:1.0"))
            containerId = org.omg.CORBA.ContainedHelper.narrow(defined_in).id();
        else
            containerId = "IDL::1.0"; // top level, IR

        org.omg.CORBA.TypeDescription td =
            new org.omg.CORBA.TypeDescription( name,
                                               id(),
                                               containerId,
                                               version(),
                                               type() );

        org.omg.CORBA.TypeDescriptionHelper.insert( a , td );

        org.omg.CORBA.ContainedPackage.Description result =
            new org.omg.CORBA.ContainedPackage.Description(
                  org.omg.CORBA.DefinitionKind.dk_Alias, a );
        return result;
    }

    public void destroy()
    {}

}
