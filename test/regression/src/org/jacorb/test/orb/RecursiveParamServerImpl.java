package org.jacorb.test.orb;

import java.io.*;
import junit.framework.*;
import junit.extensions.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.jacorb.util.*;
import org.jacorb.test.*;
import org.jacorb.test.RecursiveParamServerPackage.*;
import org.jacorb.test.RecursiveParamServerPackage.ParmPackage.*;


public class RecursiveParamServerImpl extends RecursiveParamServerPOA
{
    public void passParm( Parm p )
    {
        System.out.println("Parm name" + p.name );
        System.out.println("Parm value");
        switch( p.value.discriminator().value() )
        {
            case ParmValueType._string_type :
            System.out.println( p.value.string_value());
            break;
            case ParmValueType._nested_type:
            System.out.println("nested:");
            Parm[][] nested = p.value.nested_value();
            for( int i = 0; i < nested.length; i++ )
                for( int j = 0; j < nested[i].length; j++ )
                    passParm( nested[i][j]);
        }
        System.out.println("Parm ok");
    }


    public void passAny( Any a )
    {
        try
        {
            blubT union = blubTHelper.extract( a );
            if( union.discriminator() )
            {
                blubT[] blubs = union.b();
            }
            System.out.println("Any ok");
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }
}
