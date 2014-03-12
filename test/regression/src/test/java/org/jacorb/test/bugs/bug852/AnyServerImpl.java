package org.jacorb.test.bugs.bug852;


import org.omg.CORBA.Any;

public class AnyServerImpl
    extends AnyServerPOA
{
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();

    public AnyServerImpl(){}

    public java.lang.String generic(Any a)
    {
        return "generic";
    }

    public Any roundtripany(Any a)
    {
        Any any = org.omg.CORBA.ORB.init().create_any();

        any.insert_Object(a.extract_Object(), a.type());

        return any;
    }
}
