package org.jacorb.orb.policies;

import org.omg.PortableInterceptor.*;
import org.omg.BiDirPolicy.*;
import org.omg.CORBA.*;

public class BiDirPolicyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements PolicyFactory
{
 
    public BiDirPolicyFactoryImpl(ORB orb)
    {
    }

    public Policy create_policy( int type, Any value )
        throws PolicyError
    {
        if( type != BIDIRECTIONAL_POLICY_TYPE.value )
            throw new PolicyError();

        return new BiDirPolicyImpl( BidirectionalPolicyValueHelper.extract( value ));

    }
}






