package org.jacorb.test.bugs.bug387;

import org.omg.CORBA.Any;

public class TestInterfaceImpl extends TestInterfacePOA {
    
    public Any test_return_value() 
    {
        TestStruct testStruct = new TestStruct("STRINGTEST", null, 1);
        Any any = _orb().create_any();
        TestStructHelper.insert(any, testStruct);
        return any;
    }
    
    public Any test_return_null()
    {
        TestStruct testStruct = new TestStruct(null, null, 1);
        Any any = _orb().create_any();
        TestStructHelper.insert(any, testStruct);
        return any;
    }
    
    public boolean test_pass_value(Any a, String expected)
    {        
        TestStruct testStruct = TestStructHelper.extract(a);
        return expected.equals (testStruct.name);
    }
     
    public boolean test_pass_null(Any a)
    {
        TestStruct testStruct = TestStructHelper.extract(a);
        return testStruct.name == null;
    }

    public boolean test_pass_shared(Any a)
    {
        TestStruct t = TestStructHelper.extract(a);
        return t.name == t.other;
    }
        
}
