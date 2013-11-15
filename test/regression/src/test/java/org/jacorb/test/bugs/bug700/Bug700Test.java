package org.jacorb.test.bugs.bug700;

import static org.junit.Assert.fail;
import org.junit.Test;

public class Bug700Test
{
    @Test
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
