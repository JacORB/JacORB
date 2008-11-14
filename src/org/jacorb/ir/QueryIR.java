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
import org.jacorb.orb.ORB;

public class QueryIR
{
    public static void main( String[] args )
    {
        if( args.length != 1 )
        {
            System.err.println("Usage: qir <RepositoryID>");
            System.exit(1);
        }

        try
        {
            ORB orb = (ORB) org.omg.CORBA.ORB.init( args, null );
            org.omg.CORBA.Repository ir =
                org.omg.CORBA.RepositoryHelper.narrow( orb.resolve_initial_references( "InterfaceRepository"));

            Logger logger = orb.getConfiguration().getNamedLogger("jacorb.ir");

            if( ir == null )
            {
                System.out.println( "Could not find IR.");
                System.exit(1);
            }

            org.omg.CORBA.Contained c = ir.lookup_id( args[0] );

            if( c != null )
            {
                IdlWriter idlw = new IdlWriter(orb, System.out, logger);
                idlw.printContained( c, 2 );
            }
            else
                System.out.println( args[0] + " not found in IR.");
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }
    }
}










