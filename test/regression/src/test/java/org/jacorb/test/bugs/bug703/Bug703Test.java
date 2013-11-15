package org.jacorb.test.bugs.bug703;

import org.junit.Assert;
import org.junit.Test;
import org.omg.CORBA.TCKind;

public class Bug703Test
{
    @Test
    public void testOctetTypeCode()
    {
        Assert.assertEquals(TCKind._tk_alias, IDHelper.type().kind().value());
    }
}
