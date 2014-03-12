package org.jacorb.test.bugs.bugjac590;


public class BooleanUnionIntServerImpl extends BooleanUnionIntPOA
{
    public void f(org.jacorb.test.bugs.bugjac590.BooleanUnion x, org.jacorb.test.bugs.bugjac590.BooleanUnionHolder y)
    {
       y.value = x;
    }

    public void g(org.jacorb.test.bugs.bugjac590.EnumUnion x, org.jacorb.test.bugs.bugjac590.EnumUnionHolder y)
    {
       y.value = x;
    }

    public void e(org.jacorb.test.bugs.bugjac590.EnumUnion x)
    {
    }
}
