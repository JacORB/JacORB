package org.jacorb.test.bugs.bug401;

import org.omg.CORBA.Any;

public class AnyServant extends AnyServerPOA
{
    public A getA()
    {
        A a = new A(){};
        a.aa = 0xAA;
        return a;
    }

    public B getB()
    {
        B b = new B(){};
        b.aa = 0xAA;
        b.bb = 0xBB;
        return b;
    }

    public Any getAnyA()
    {
        A a = getA();
        Any aa = _orb().create_any();
        AHelper.insert(aa, a);
        return aa;
    }

    public Any getAnyB()
    {
        B b = getB();
        Any bb = _orb().create_any();
        BHelper.insert(bb, b);
        return bb;
    }

    public Any[] getAnyAB()
    {
        Any[] any = new Any[2];

        any[0] = _orb().create_any();
        AHelper.insert(any[0], getA());

        any[1] = _orb().create_any();
        BHelper.insert(any[1], getB());

        return any;
    }

}