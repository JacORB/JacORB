package org.jacorb.test.orb.dynany;

import junit.framework.TestCase;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynSequence;

/**
 * DynAnySeqObjectTest.java
 *
 * DynAny tests for sequences of object types.
 */

public class DynAnySeqObjectTest extends DynAnyXXXTestCase
{
    private static final String nameService = "IOR:000000000000002B49444C3A6F6D672E6F72672F436F734E616D696E672F4E616"
            + "D696E67436F6E746578744578743A312E30000000000002000000000000007400010"
            + "2000000000E3231332E34382E39312E31353700836C0000001F5374616E646172644"
            + "E532F4E616D655365727665722D504F412F5F726F6F7400000000020000000000000"
            + "008000000004A414300000000010000001C000000000001000100000001050100010"
            + "00101090000000105010001000000010000002C00000000000000010000000100000"
            + "01C00000000000100010000000105010001000101090000000105010001";


    public void testInsertAnyObject() throws Exception
    {
        DynSequence dyn_seq = null;

        org.omg.CORBA.Object obj = orb.string_to_object(nameService);

        TypeCode tc = org.omg.CosNaming.NamingContextExtHelper.type();

        TypeCode seq_tc = orb.create_sequence_tc(2, tc);

        dyn_seq = (DynSequence) factory.create_dyn_any_from_type_code(seq_tc);

        Any[] object_sequence = new Any[2];
        object_sequence[0] = orb.create_any();
        object_sequence[1] = orb.create_any();
        object_sequence[0].insert_Object(obj, tc);
        object_sequence[1].insert_Object(obj, tc);

        dyn_seq.set_elements(object_sequence);

        dyn_seq.to_any();

        dyn_seq.destroy();
    }

    public void testInsertDynAnyObject() throws Exception
    {
        DynSequence dyn_seq = null;

        TypeCode tc = org.omg.CosNaming.NamingContextExtHelper.type();

        TypeCode seq_tc = orb.create_sequence_tc(2, tc);

        dyn_seq = (DynSequence) factory.create_dyn_any_from_type_code(seq_tc);

        DynAny[] object_sequence = new DynAny[2];

        object_sequence[0] = factory.create_dyn_any_from_type_code(tc);
        object_sequence[1] = factory.create_dyn_any_from_type_code(tc);

        dyn_seq.set_elements_as_dyn_any(object_sequence);

        dyn_seq.to_any();

        dyn_seq.destroy();
    }
}
