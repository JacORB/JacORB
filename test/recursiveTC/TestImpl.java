package test.recursiveTC;

import org.omg.CORBA.*;
import test.recursiveTC.TestPackage.*;
import test.recursiveTC.TestPackage.ParmPackage.*;

public class TestImpl 
    extends TestPOA
{

    public TestImpl() 
    {
    }

    public void passParm( Parm p )
    {
        System.out.println("Parm name" + p.name );
        System.out.println("Parm value");
        switch( p.value.discriminator().value() )
        {
        case test.recursiveTC.TestPackage.ParmValueType._string_type : 
            System.out.println( p.value.string_value());
            break;
        case test.recursiveTC.TestPackage.ParmValueType._nested_type:
            System.out.println("nested:");
            test.recursiveTC.TestPackage.Parm[][] nested = p.value.nested_value();
            for( int i = 0; i < nested.length; i++ )
                for( int j = 0; j < nested[i].length; j++ )
                    passParm( nested[i][j]);
            
            
        }
        System.out.println("Parm ok");
    }

    public void passAny( Any a )
    {
        blubT union = blubTHelper.extract( a );
        
        if( union.discriminator() )
        {
            blubT[] blubs = union.b();
            System.out.println("unions contains " + blubs.length);
        }

        System.out.println("Any ok");
    }


}
