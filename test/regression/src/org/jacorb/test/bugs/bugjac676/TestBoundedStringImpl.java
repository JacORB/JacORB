package org.jacorb.test.bugs.bugjac676;

public class TestBoundedStringImpl extends org.jacorb.test.bugs.bugjac676.TestBoundedStringPOA 
{
    private StructOne obj;

    public StructOne get_object() 
    {
        return obj;
    }

    public void set_object(StructOne _obj) 
    {
        obj = _obj;
    }

    public StructOne get_bad_object() 
    {
        return new StructOne( new String( "012345678901234567890123456789012" ), 
                              new String( "some normal string" ));
    }
}
