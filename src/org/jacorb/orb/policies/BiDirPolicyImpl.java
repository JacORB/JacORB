package org.jacorb.orb.policies;

import org.omg.PortableInterceptor.*;

public class BiDirPolicyImpl
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.omg.BiDirPolicy.BidirectionalPolicy
{
    short value;

    public BiDirPolicyImpl(short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public int policy_type()
    {
        return org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value;
    }

    public org.omg.CORBA.Policy copy()
    {
        return new BiDirPolicyImpl( value );
    }

    public void destroy()
    {
    }
    

}
