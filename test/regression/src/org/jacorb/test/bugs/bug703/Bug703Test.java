package org.jacorb.test.bugs.bug703;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.omg.CORBA.TCKind;

public class Bug703Test extends TestCase
{
    public void testOctetTypeCode()
    {
        Assert.assertEquals(TCKind._tk_alias, IDHelper.type().kind().value());
    }
}
