package org.jacorb.test.bugs.bug700;

import junit.framework.TestCase;

public class Bug700Test extends TestCase
{
    public void testNestedTypePackagePlace()
    {
        try
        {
            Class.forName("org.jacorb.test.bugs.bug700.MyUnionPackage.NestedStruct");
        }
        catch (ClassNotFoundException e)
        {
            fail("Class org.jacorb.test.bugs.bug700.MyUnionPackage.NestedStruct not found");
        }
    }
}
