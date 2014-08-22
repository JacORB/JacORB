package org.jacorb.test.bugs.bugjac730;

import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

public class BugJac730Test extends ORBTestCase
{
    @Test
    public void testLocalInterfaceForwardDeclaration () throws Exception
    {
        TypeCode tc = SeqIHelper.type();
        assertTrue (tc.content_type().content_type().kind().value() == TCKind.tk_local_interface.value());
    }
    
    @Test
    public void testLocalInterface () throws Exception
    {
        TypeCode tc = SeqJHelper.type();
        assertTrue (tc.content_type().content_type().kind().value() == TCKind.tk_local_interface.value());
    }
}
