package org.jacorb.test.orb;

import org.jacorb.test.RecursiveParamServerPOA;
import org.jacorb.test.RecursiveParamServerPackage.Parm;
import org.jacorb.test.RecursiveParamServerPackage.ParmValueType;
import org.jacorb.test.RecursiveParamServerPackage.blubT;
import org.jacorb.test.RecursiveParamServerPackage.blubTHelper;
import org.omg.CORBA.Any;


public class RecursiveParamServerImpl extends RecursiveParamServerPOA
{
    public void passParm( Parm p )
    {
        switch( p.value.discriminator().value() )
        {
            case ParmValueType._string_type :
                break;
            case ParmValueType._nested_type:
                Parm[][] nested = p.value.nested_value();
            for( int i = 0; i < nested.length; i++ )
            {
                for( int j = 0; j < nested[i].length; j++ )
                {
                    passParm( nested[i][j]);
                }
            }
        }
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
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }
}
