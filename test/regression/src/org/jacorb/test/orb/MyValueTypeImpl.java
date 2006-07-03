package org.jacorb.test.orb;

public class MyValueTypeImpl extends MyValueType
{
    public MyValueTypeImpl()
    {
        // This no-arg constructor is used at unmarshalling time.
        // Because of its presence, there is no need for a separate
        // value factory for this type.  This is a special JacORB feature,
        // see README and org.jacorb.orb.ORB.lookup_value_factory() for
        // details.
        super();
    }

    public MyValueTypeImpl(int member)
    {
        this();

        this.member = member;
    }

    public boolean equals(Object other)
    {
        return other instanceof MyValueType &&
            member == ((MyValueType) other).member;
    }
}
