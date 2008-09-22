package org.jacorb.test.notification.node;

import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import junit.framework.Test;

import org.jacorb.notification.MessageFactory;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.filter.ParseException;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.ETCLComponentName;
import org.jacorb.notification.filter.etcl.ETCLFilterConstraint;
import org.jacorb.notification.filter.etcl.StaticTypeChecker;
import org.jacorb.notification.filter.etcl.StaticTypeException;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.test.notification.Address;
import org.jacorb.test.notification.NamedValue;
import org.jacorb.test.notification.NamedValueSeqHelper;
import org.jacorb.test.notification.Person;
import org.jacorb.test.notification.PersonHelper;
import org.jacorb.test.notification.Profession;
import org.jacorb.test.notification.TestUnion;
import org.jacorb.test.notification.TestUnionHelper;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.test.notification.common.NotificationTestUtils;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.PropertySeqHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

/**
 * JUnit TestCase. Test Parsing and Evaluation of various ETCL Expressions.
 *
 * @author Alphonse Bendt
 */

public class TCLTest extends NotificationTestCase
{
    private NotificationTestUtils testUtils_;

    //////////////////////////////////////////////////
    // the testdata

    private Any testPerson_;

    private Any testUnion_;

    private Any testUnion1_;

    private Any testUnion2_;

    private Any testUnion3_;

    private Any testUnion4_;

    private Any testUnion5_;

    private final String[] visitorTestExpressions_ = new String[] { "$.value < 1", "$.value != 1",
            "$.value <= 1", "$.value > 1", "$.value >= 1", "$.value == 1", "$.value + 1",
            "$.value - 1", "$.value * 1", "$.value / 1", "$.value ~ $.x" };

    private void setUpTestUnion(Person person)
    {
        TestUnion _t1, _t2, _t3, _t4, _t5, _t;

        _t = new TestUnion();
        _t.default_person(person);

        _t1 = new TestUnion();
        _t1.long_(100);

        _t2 = new TestUnion();
        _t2.string_("String");

        _t3 = new TestUnion();
        _t3.named_value_(new NamedValue("this is the name", "this is the value"));

        _t4 = new TestUnion();
        _t4.person_(person);

        _t5 = new TestUnion();
        NamedValue[] lonv = new NamedValue[1];
        lonv[0] = new NamedValue("name", "value");
        _t5.named_value_array(lonv);

        testUnion_ = getORB().create_any();
        testUnion1_ = getORB().create_any();
        testUnion2_ = getORB().create_any();
        testUnion3_ = getORB().create_any();
        testUnion4_ = getORB().create_any();
        testUnion5_ = getORB().create_any();

        TestUnionHelper.insert(testUnion_, _t);
        TestUnionHelper.insert(testUnion1_, _t1);
        TestUnionHelper.insert(testUnion2_, _t2);
        TestUnionHelper.insert(testUnion3_, _t3);
        TestUnionHelper.insert(testUnion4_, _t4);
        TestUnionHelper.insert(testUnion5_, _t5);
    }

    private Person setUpPerson()
    {
        Person _person = new Person();
        Address _address = new Address();
        NamedValue _nv1 = new NamedValue();
        NamedValue _nv2 = new NamedValue();
        Profession _p = Profession.STUDENT;

        _person.first_name = "Firstname";
        _person.last_name = "Lastname";
        _person.age = 29;
        _person.phone_numbers = new String[2];
        _person.phone_numbers[0] = "32132132";
        _person.phone_numbers[1] = "";
        _person.nv = new NamedValue[2];
        _person.person_profession = _p;

        _address.street = "Takustr.";
        _address.number = 9;
        _address.city = "Berlin";

        _person.home_address = _address;

        _nv1.name = "priority";
        _nv1.value = "Very High";
        _person.nv[0] = _nv1;

        _nv2.name = "stuff";
        _nv2.value = "not important";
        _person.nv[1] = _nv2;

        _person.aliases = new String[] { "Alias0", "Alias1", "Alias2" };
        _person.numbers = new int[] { 10, 20, 30, 40, 50 };

        testPerson_ = getORB().create_any();
        PersonHelper.insert(testPerson_, _person);
        return _person;
    }

    public void setUpTest() throws Exception
    {
        testUtils_ = new NotificationTestUtils(getORB());

        Person _person = setUpPerson();

        setUpTestUnion(_person);
    }

    public TCLTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    ////////////////////
    // Helper functions

    private void runStaticTypeCheck(String expr) throws Exception
    {
        runStaticTypeCheck(expr, true);
    }

    private void runStaticTypeCheck(String expr, boolean shouldFail) throws Exception
    {
        try
        {
            AbstractTCLNode _root = TCLParser.parse(expr);

            StaticTypeChecker _checker = new StaticTypeChecker();

            _checker.check(_root);

            if (shouldFail)
            {
                fail("static type error should have occcured");
            }
        } catch (StaticTypeException ste)
        {
            // expected
        }
    }

    /**
     * parse both strings, evaluate the resulting tree and compare the results. the results are
     * expected to be equal.
     *
     * @param expect
     *            an ETCL expression
     * @param expression
     *            an ETCL expression
     * @throws Exception
     *             if parsing fails
     */
    private void runEvaluation(String expect, String expression) throws Exception
    {
        AbstractTCLNode fstNode = TCLParser.parse(expect);
        AbstractTCLNode sndNode = TCLParser.parse(expression);

        EvaluationContext _context = new EvaluationContext(getEvaluator());

        EvaluationResult pre_res = fstNode.evaluate(_context);
        EvaluationResult pst_res = sndNode.evaluate(_context);

        assertEquals("expected " + fstNode.toStringTree() + " == " + sndNode.toStringTree(),
                pre_res, pst_res);
    }

    //////////////////////////////////////////////////
    // and now some testing

    public void testPLUS() throws Exception
    {
        runEvaluation("2", "1 + 1");
        runEvaluation("0", "1 + -1");
        runEvaluation("0", "-1 + 1");
        runEvaluation("2", "1 + 1.0");
        runEvaluation("2.0", "1 + 1");
    }

    public void testMINUS() throws Exception
    {
        runEvaluation("0", "1 - 1");
        runEvaluation("2", "1 - -1");
        runEvaluation("-2", "-1 - 1");
        runEvaluation("0", "1 - 1.0");
        runEvaluation("0.0", "1 - 1");
    }

    public void testDIV() throws Exception
    {
        runEvaluation("5", "10/2");
        runEvaluation("1/3", "10/30");
        runEvaluation("0.25", "1.0/4");
    }

    public void testMULT() throws Exception
    {
        runEvaluation("100", "10*10");
        runEvaluation("1", "1 * 1");
        runEvaluation("1", "0.25 * 4");
        runEvaluation("-1", "1 * -1");
    }

    public void testSimpleNumbers() throws Exception
    {
        runEvaluation("5", "5");
        runEvaluation("-1", "-1");
        runEvaluation("1", "+1");
        runEvaluation("1", "1.0");
        runEvaluation("1", "+1.0");
        runEvaluation("-1", "-1.0");
        runEvaluation(".1", "+.1");
        runEvaluation("-0.1", "-.1");
    }

    public void testFloatNumbers() throws Exception
    {
        runEvaluation("1000", "10e+2");
        runEvaluation("100", "10e+1");
        runEvaluation("10", "10e+0");
        runEvaluation("1", "10e-1");
        runEvaluation(".1", "10e-2");
        runEvaluation(".01", "10e-3");
        runEvaluation("-.01", "-10e-3");
    }

    public void testSimpleOperations() throws Exception
    {
        runEvaluation("0", "1-1");
        runEvaluation("2", "1+1");
        runEvaluation("1", "1*1");
        runEvaluation("1", "1/1");
    }

    public void testParentheses() throws Exception
    {
        runEvaluation("7", "1+2*3");
        runEvaluation("7", "1+(2*3)");
        runEvaluation("1+2*3", "1+(2*3)");
        runEvaluation("9", "(1+2)*3");
        runEvaluation("1+(2+(3+(4+5)))", "(((1+2)+3)+4)+5");
        runEvaluation("1*(2*(3*(4*5)))", "(((1*2)*3)*4)*5");
    }

    public void testGT() throws Exception
    {
        runEvaluation("TRUE", "1>0");
        runEvaluation("TRUE", "0.2 > 0.1");
        runEvaluation("TRUE", "1.1 > 1");
        runEvaluation("FALSE", "0>1");

        runEvaluation("TRUE", "TRUE > FALSE");
        runEvaluation("FALSE", "FALSE > TRUE");

        runEvaluation("TRUE", "'bbb' > 'aaa'");
        runEvaluation("FALSE", "'bbb' > 'ccc'");
    }

    public void testLT() throws Exception
    {
        runEvaluation("FALSE", "1<0");
        runEvaluation("FALSE", "0.2 < 0.1");
        runEvaluation("FALSE", "1.1 < 1");
        runEvaluation("TRUE", "0<1");

        runEvaluation("FALSE", "TRUE < FALSE");
        runEvaluation("TRUE", "FALSE < TRUE");

        runEvaluation("FALSE", "'bbb' < 'aaa'");
        runEvaluation("TRUE", "'bbb' < 'ccc'");
    }

    public void testLTE() throws Exception
    {
        runEvaluation("TRUE", "0<=1");
        runEvaluation("TRUE", "0<=0");
        runEvaluation("FALSE", "1<=0");
        runEvaluation("TRUE", "'abc'<='abc'");
        runEvaluation("TRUE", "'abc'<='dbc'");
        runEvaluation("FALSE", "'bbc'<='abc'");
    }

    public void testGTE() throws Exception
    {
        runEvaluation("FALSE", "0>=1");
        runEvaluation("TRUE", "0>=0");
        runEvaluation("TRUE", "1>=0");
        runEvaluation("TRUE", "'abc'>='abc'");
        runEvaluation("FALSE", "'abc'>='dbc'");
        runEvaluation("TRUE", "'bbc'>='abc'");
    }

    public void testEQ() throws Exception
    {
        runEvaluation("TRUE", "TRUE == TRUE");
        runEvaluation("TRUE", "FALSE == FALSE");
        runEvaluation("FALSE", "FALSE == TRUE");
        runEvaluation("FALSE", "TRUE == FALSE");
    }

    public void testNEQ() throws Exception
    {
        runEvaluation("TRUE", "0!=1");
        runEvaluation("FALSE", "1!=1");

        runEvaluation("TRUE", "'bla'!='blubb'");
        runEvaluation("FALSE", "'bla'!='bla'");

        runEvaluation("TRUE", "TRUE!=FALSE");
        runEvaluation("TRUE", "FALSE!=TRUE");
        runEvaluation("FALSE", "TRUE!=TRUE");
        runEvaluation("FALSE", "FALSE!=FALSE");
    }

    public void testAND() throws Exception
    {
        runEvaluation("TRUE", "TRUE and TRUE");
        runEvaluation("FALSE", "TRUE and FALSE");
        runEvaluation("FALSE", "FALSE and TRUE");
        runEvaluation("FALSE", "FALSE and FALSE");
    }

    public void testOR() throws Exception
    {
        runEvaluation("TRUE", "TRUE or TRUE");
        runEvaluation("TRUE", "TRUE or FALSE");
        runEvaluation("TRUE", "FALSE or TRUE");
        runEvaluation("FALSE", "FALSE or FALSE");
    }

    public void testNOT() throws Exception
    {
        runEvaluation("TRUE", "not FALSE");
        runEvaluation("FALSE", "not TRUE");
    }

    public void testLazyEval() throws Exception
    {
        try
        {
            runEvaluation("TRUE", "1/0");
            fail("Division by zero should cause an exception");
        } catch (EvaluationException e)
        {
            // expected
        }

        runEvaluation("TRUE", "TRUE or (1/0)");
        runEvaluation("FALSE", "FALSE and (1/0)");
    }

    public void testTwiddle() throws Exception
    {
        runEvaluation("TRUE", "'substr' ~ 'substring'");
        runEvaluation("FALSE", "'not' ~ 'substring'");
    }

    public void testCast() throws Exception
    {
        runEvaluation("FALSE", "2/3 > 0");
        runEvaluation("TRUE", "(1.0 * 2/3) > 0");
    }

    public void testTypeConversion() throws Exception
    {
        runEvaluation("TRUE", "'H' + 1 > 32");
    }

    public void testStaticTypeCheck() throws Exception
    {
        runStaticTypeCheck("5 + 'al'");
        runStaticTypeCheck("5 + 'a'", false);
        runStaticTypeCheck("5 ~ 'a'");
        runStaticTypeCheck("TRUE + 'a'");
        runStaticTypeCheck("'a' and 'b'");
        runStaticTypeCheck("1 * (TRUE and TRUE)", false);
    }

    public void testEnum() throws Exception
    {
        runEvaluation(testPerson_, "$.person_profession == STUDENT");
    }

    public void testImplicit() throws Exception
    {
        runEvaluation(testPerson_, "$._type_id == 'Person'");

        runEvaluation(testPerson_,
                "$._repos_id == 'IDL:jacorb.org/org/jacorb/test/notification/Person:1.0'");

        runEvaluation(testPerson_, "$.nv._length == 2");

        try
        {
            runEvaluation(testPerson_, "$.first_name._length == 2");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        runEvaluation(testPerson_, "$.phone_numbers._length == 2");

        runEvaluation(testPerson_, "$.4._length == 2");

        runEvaluation(testUnion5_, "$.named_value_array._length == 1");

        runEvaluation(testUnion5_, "$.(5)._length == 1");
    }

    public void testDefault() throws Exception
    {
        runEvaluation(testUnion_, "default $._d and $.().first_name == 'Firstname'");
    }

    public void testExist() throws Exception
    {
        runEvaluation(testPerson_, "exist $._type_id");
        runEvaluation(testPerson_, "exist $._type_id and $._type_id =='Person'");

        runEvaluation(testPerson_, "exist $._repos_id");

        runEvaluation(testUnion1_, "not exist $.1");
        runEvaluation(testUnion1_, "not exist $.0");
        runEvaluation(testUnion1_, "exist $.(0)");
        runEvaluation(testUnion1_, "exist $.(1)");

        runEvaluation(testPerson_, "exist $.first_name");
        runEvaluation(testPerson_, "not exist $.third_name");

        runEvaluation(testUnion1_, "exist $._d");
        runEvaluation(testPerson_, "not exist $._d");

        runEvaluation(testPerson_, "exist $.nv._length");
        runEvaluation(testPerson_, "exist $.5._length");
        runEvaluation(testPerson_, "not exist $.home_address._length");

        runEvaluation(testUnion1_, "exist $.long_");
        runEvaluation(testUnion1_, "exist $.(1)");

        runEvaluation(testUnion2_, "exist $.string_");
        runEvaluation(testUnion2_, "exist $.(2)");

        runEvaluation(testUnion3_, "exist $.named_value_");
        runEvaluation(testUnion3_, "exist $.(3)");

        runEvaluation(testUnion4_, "exist $.person_");
        runEvaluation(testUnion4_, "exist $.(4)");
        runEvaluation(testUnion4_, "exist $.(4).first_name");
        runEvaluation(testUnion4_, "exist $.(4).phone_numbers[0]");

        runEvaluation(testUnion_, "exist $.()");
        runEvaluation(testUnion_, "exist $.default_person");
    }

    public void testUnion() throws Exception
    {
        runEvaluation(testUnion1_, "$.long_ > 54");
        runEvaluation(testUnion1_, "$._d == 1 and $.(0) == 100");
        runEvaluation(testUnion1_, "$._d == 1 and $.(1) == 100");

        try
        {
            runEvaluation(testUnion1_, "$.string_");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        runEvaluation(testUnion1_, "$.(1) > 54");

        runEvaluation(testUnion1_, "$.(0) > 54");

        runEvaluation(testUnion2_, "$.string_", "'String'");
        runEvaluation(testUnion2_, "$.(2)", "'String'");

        runEvaluation(testUnion3_, "$.named_value_.name", "'this is the name'");

        runEvaluation(testUnion3_, "$.(3).name", "'this is the name'");

        runEvaluation(testUnion4_, "$.person_.home_address.street == 'Takustr.'");

        runEvaluation(testUnion4_, "$.(4).home_address.street == 'Takustr.'");

        runEvaluation(testUnion4_, "$.(4).3.street == 'Takustr.'");
        runEvaluation(testUnion4_, "$.(4).3.0 == 'Takustr.'");
        runEvaluation(testUnion4_, "$.(4).home_address.0 == 'Takustr.'");

        runEvaluation(testUnion5_, "$.named_value_array(name)", "'value'");

        runEvaluation(testUnion5_, "$.(5)(name)", "'value'");

        runEvaluation(testUnion_, "$.().first_name", "'Firstname'");

        runEvaluation(testUnion_, "$.default_person.first_name", "'Firstname'");
    }

    public void testComponent() throws Exception
    {
        runEvaluation(testPerson_, "$.first_name", "'Firstname'");

        try
        {
            runEvaluation(testPerson_, "$.third_name", "'does not exist'");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        runEvaluation(testPerson_, "$.0", "'Firstname'");

        runEvaluation(testPerson_, "$.home_address.street", "'Takustr.'");

        runEvaluation(testPerson_, "$.3.0", "'Takustr.'");

        runEvaluation(testPerson_, "$.first_name == 'Firstname'");

        runEvaluation(testPerson_, "$.age > 30", "FALSE");

        runEvaluation(testPerson_, "$.age > 20 and $.first_name =='Firstname'");

        runEvaluation(testPerson_, "$.age < 30 or $.first_name == 'Adalbert'");

        runEvaluation(testPerson_, "$.phone_numbers[0]", "'32132132'");

        runEvaluation(testPerson_, "$.4[0]", "'32132132'");

        runEvaluation(testPerson_, "'321' ~ $.phone_numbers[0]");

        runEvaluation(testPerson_, "$.nv(priority)", "'Very High'");

        runEvaluation(testPerson_, "$.5(priority)", "'Very High'");

        runEvaluation(testPerson_, "$.nv[0].name == 'priority' and $.nv[0].value == 'Very High'");

        runEvaluation(testPerson_, "$.nv(stuff) > 'aaa'");
    }

    public void testDynamicTypeExceptions() throws Exception
    {
        // provoke some Dynamic Type Errors

        try
        {
            runEvaluation(testPerson_, "$.first_name + 1 == 10");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        try
        {
            runEvaluation(testPerson_, "$.age == '29'");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        try
        {
            runEvaluation(testPerson_, "$.age and true");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

    }

    public void testParse() throws Exception
    {
        try
        {
            TCLParser.parse("$..");
            fail();
        } catch (ParseException e)
        {
            // expected
        }

        try
        {
            TCLParser.parse("ab +-/ abc");
            fail();
        } catch (ParseException e)
        {
            // expected
        }
    }

    public void testShorthandNotation() throws Exception
    {
        Any _testData = testUtils_.getStructuredEventAny();

        runEvaluation(_testData, "$domain_name == 'TESTING'");
        runEvaluation(_testData, "$type_name == 'TESTING'");
        runEvaluation(_testData, "$event_name == 'ALARM'");

        StructuredEvent _structuredEvent = testUtils_.getStructuredEvent();

        runEvaluation(_structuredEvent, "$domain_name == 'TESTING'");
        runEvaluation(_structuredEvent, "$type_name == 'TESTING'");
        runEvaluation(_structuredEvent, "$event_name == 'ALARM'");
    }

    public void testShorthandVariableHeader() throws Exception
    {
        StructuredEvent _structuredEvent = testUtils_.getStructuredEvent();

        Any _any = getORB().create_any();

        _structuredEvent.header.variable_header = new Property[2];

        _any.insert_long(10);
        _structuredEvent.header.variable_header[0] = new Property("long", _any);

        _any = getORB().create_any();
        _any.insert_string("text");
        _structuredEvent.header.variable_header[1] = new Property("string", _any);

        runEvaluation(_structuredEvent, "$long == 10");
        runEvaluation(_structuredEvent, "$long == $.header.variable_header(long)");

        runEvaluation(_structuredEvent, "$string == 'text'");
        runEvaluation(_structuredEvent, "$string == $.header.variable_header(string)");
    }

    public void testShorthandFilterableData() throws Exception
    {
        StructuredEvent _structuredEvent = testUtils_.getStructuredEvent();

        Any _any = getORB().create_any();

        Property[] _props = new Property[2];
        _any.insert_long(10);
        _props[0] = new Property("long", _any);

        _any = getORB().create_any();
        _any.insert_string("text");
        _props[1] = new Property("string", _any);

        _structuredEvent.filterable_data = _props;

        runEvaluation(_structuredEvent, "$long == 10");
        runEvaluation(_structuredEvent, "$long == $.filterable_data(long)");

        runEvaluation(_structuredEvent, "$string == 'text'");
        runEvaluation(_structuredEvent, "$string == $.filterable_data(string)");
    }

    public void testShorthandDefault() throws Exception
    {
        runEvaluation(testPerson_, "$first_name == $.first_name");
    }

    public void testShorthandDefaultAny() throws Exception
    {
        NamedValue[] _nv = new NamedValue[2];
        _nv[0] = new NamedValue("name1", "value1");
        _nv[1] = new NamedValue("name2", "value2");

        Any _a = getORB().create_any();
        NamedValueSeqHelper.insert(_a, _nv);
        runEvaluation(_a, "$name1 == $(name1)");
        runEvaluation(_a, "$name2 == $(name2)");
    }

    public void testInsertComponentName() throws Exception
    {
        ETCLComponentName _comp = (ETCLComponentName) TCLParser.parse("$.first_name.last_name");

        _comp.acceptInOrder(new TCLCleanUp());
        assertEquals("$.first_name.last_name", _comp.getComponentName());

        AbstractTCLNode _root = TCLParser.parse("$.first_name.value + 5");

        _root.acceptInOrder(new TCLCleanUp());
        _comp = (ETCLComponentName) _root.getFirstChild();
        assertEquals("$.first_name.value", _comp.getComponentName());

        _comp = (ETCLComponentName) TCLParser.parse("$domain_name");
        _comp.acceptInOrder(new TCLCleanUp());
        assertEquals("$domain_name", _comp.getComponentName());

        _comp = (ETCLComponentName) TCLParser.parse("$domain_name._type_id");
        _comp.acceptInOrder(new TCLCleanUp());
        assertEquals("$domain_name._type_id", _comp.getComponentName());

        _comp = (ETCLComponentName) TCLParser.parse("$.(1)");
        _comp.acceptInOrder(new TCLCleanUp());
        assertEquals("$.(1)", _comp.getComponentName());

        _comp = (ETCLComponentName) TCLParser.parse("$.()");
        _comp.acceptInOrder(new TCLCleanUp());
        assertEquals("$.(default)", _comp.getComponentName());
    }

    public void testInOperator() throws Exception
    {
        runEvaluation(testPerson_, "'Alias0' in $.aliases");
        runEvaluation(testPerson_, "'Alias1' in $.aliases");
        runEvaluation(testPerson_, "'Alias2' in $.aliases");
        runEvaluation(testPerson_, "not ('AliasXYZ' in $.aliases)");
        runEvaluation(testPerson_, "10 in $.numbers");
        runEvaluation(testPerson_, "not (25 in $.numbers)");
    }

    public void testPassOverUnnamedLayers() throws Exception
    {
        Any _any = getORB().create_any();
        _any.insert_any(testPerson_);

        runEvaluation(_any, "$.first_name == 'Firstname'");

        Any _any2 = getORB().create_any();
        _any2.insert_any(_any);

        runEvaluation(_any, "$.first_name == 'Firstname'");

        runEvaluation(_any, "$ == 'FirstName'", "FALSE");

        Any _any3 = getORB().create_any();
        _any3.insert_long(10);

        runEvaluation(_any3, "$ == 10");

        try
        {
            runEvaluation(_any3, "$ == '10'");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }
    }

    public void testVariableCurtime() throws Exception
    {
        runEvaluation(testPerson_, "$curtime._repos_id == 'IDL:omg.org/TimeBase/UtcT:1.0'");

        Any _timeAny = getORB().create_any();
        UtcT _time = org.jacorb.util.Time.corbaTime();
        UtcTHelper.insert(_timeAny, _time);

        runEvaluation(_timeAny, "$curtime._repos_id == $._repos_id");
        runEvaluation(_timeAny, "$.time <= $curtime.time");
        runEvaluation(_timeAny, "$.time + 1 >= $.time");
        runEvaluation(_timeAny, "$.time - 1 <= $.time");
    }

    public void testBug671() throws Exception
    {
        Any _any = getORB().create_any();
        _any.insert_longlong(139192928000000000l);
        runEvaluation(_any, "$ >= 139192928000000000");
    }

    public void testAccessNonExistingStructMember() throws Exception
    {
        try
        {
            runEvaluation(testPerson_, "$.not_exist");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }
    }

    public void testAccessNonExistingRuntimeVariable() throws Exception
    {
        try
        {
            runEvaluation(testPerson_, "$not_exist");
            fail();
        } catch (EvaluationException e)
        {
            // expected
        }
    }

    public void testAccessNonExistingProperty() throws Exception
    {
        StructuredEvent event = testUtils_.getStructuredEvent();

        try
        {
            runEvaluation(event, "$.filterable_data(PROPERTY) == 1");

            fail();
        } catch (EvaluationException e)
        {
            // expected
        }

        try
        {
            runEvaluation(event, "$.filterable_data(PROPERTY) < 5");

            fail();
        } catch (EvaluationException e)
        {
            // expected
        }
    }

    public void testLTEforwardsVisitorBug() throws Exception
    {
        String _expr = "$.time <= 1";
        AbstractTCLNode _root = TCLParser.parse(_expr);
        _root.acceptPostOrder(new TCLCleanUp());

        ETCLComponentName _n = (ETCLComponentName) _root.left();

        assertEquals("$.time", _n.getComponentName());
    }

    public void testAcceptPostOrder() throws Exception
    {
        for (int x = 0; x < visitorTestExpressions_.length; ++x)
        {
            AbstractTCLNode _root = TCLParser.parse(visitorTestExpressions_[x]);
            _root.acceptPostOrder(new TCLCleanUp());
            ETCLComponentName _n = (ETCLComponentName) _root.left();

            assertEquals(visitorTestExpressions_[x] + " failed", "$.value", _n.getComponentName());
        }
    }

    public void testAcceptInOrder() throws Exception
    {
        for (int x = 0; x < visitorTestExpressions_.length; ++x)
        {
            AbstractTCLNode _root = TCLParser.parse(visitorTestExpressions_[x]);
            _root.acceptInOrder(new TCLCleanUp());
            ETCLComponentName _n = (ETCLComponentName) _root.left();

            assertEquals(visitorTestExpressions_[x] + " failed", "$.value", _n.getComponentName());
        }
    }

    public void testAcceptPreOrder() throws Exception
    {
        for (int x = 0; x < visitorTestExpressions_.length; ++x)
        {
            AbstractTCLNode _root = TCLParser.parse(visitorTestExpressions_[x]);
            _root.acceptPreOrder(new TCLCleanUp());
            ETCLComponentName _n = (ETCLComponentName) _root.left();

            assertEquals(visitorTestExpressions_[x] + " failed", "$.value", _n.getComponentName());
        }
    }

    public void testWhiteboardExpr() throws Exception
    {
        Any _any = getORB().create_any();

        StructuredEvent _event = testUtils_.getStructuredEvent();
        _event.header.variable_header = new Property[1];

        Any _anyInt = getORB().create_any();
        _anyInt.insert_long(10);

        _event.header.variable_header[0] = new Property("workgroup_id", _anyInt);

        StructuredEventHelper.insert(_any, _event);
        runEvaluation(_any, "$.header.variable_header(workgroup_id) != 20");
    }

    public void testNewLexer() throws Exception
    {
        assertNotNull(TCLParser.parse(".1"));

        assertNotNull(TCLParser.parse("$.1"));
    }

    public void testTypedEvent() throws Exception
    {
        Property[] _props = new Property[] { new Property("operation", toAny("operationName")),
                new Property("value1", toAny(100)), new Property("value2", toAny(200)) };

        Any _any = getORB().create_any();

        PropertySeqHelper.insert(_any, _props);

        runEvaluation(_any, "$operation == 'operationName'");
        runEvaluation(_any, "$value1 > 50 and $value2 > 50");
    }

    public void testStringMayContainSpecialChars() throws Exception
    {
        TCLParser.parse("'noti*server'");
        TCLParser.parse("'noti{}server'");
        TCLParser.parse("'noti\"server'");
        TCLParser.parse("'noti|server'");
        TCLParser.parse("'noti;server'");
        TCLParser.parse("'noti^server'");
        TCLParser.parse("'noti~server'");
        TCLParser.parse("'noti=server'");
        TCLParser.parse("'noti,server'");
        TCLParser.parse("'noti$server'");
        TCLParser.parse("'noti#server'");
        TCLParser.parse("'noti%server'");
    }

    public void testParserIgnoresWhitespace() throws Exception
    {
        TCLParser.parse("\t\n\r ");

        runEvaluation("TRUE", "\t\n\r TRUE\n\r \t ");
        runEvaluation("2", "\t1\r+\n\r1\t");
    }

    public void testBug748() throws Exception {
        EventType type = new EventType("Finance", "StockQuote");
        FixedEventHeader fixed = new FixedEventHeader(type, "10");
        StructuredEvent quoteEvent = new StructuredEvent();

        // Complete header date
        Property variable[] = new Property[0];
        quoteEvent.header = new EventHeader(fixed, variable);
        quoteEvent.filterable_data = new Property[2];

        Any idAny = getORB().create_any();
        idAny.insert_string("stockQuotes");
        quoteEvent.filterable_data[0] = new Property("stock_id", idAny);

        Any priceAny = getORB().create_any();
        priceAny.insert_double(200.0);
        quoteEvent.filterable_data[1] = new Property("stock_price", priceAny);

        quoteEvent.remainder_of_body = getORB().create_any();
        runEvaluation(quoteEvent, "$stock_price >= 200.00", "TRUE");
        runEvaluation(quoteEvent, "$stock_price <= 200.00", "TRUE");
        runEvaluation(quoteEvent, "$stock_price == 200.00", "TRUE");
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite("TCL Parsing and Evaluation Tests", TCLTest.class
        //, "testBug748"
                );
    }

    private void runEvaluation(Any any, String expr) throws Exception
    {
        runEvaluation(any, expr, "TRUE");
    }

    private void runEvaluation(Any any, String expr, String expect) throws Exception
    {
        MessageFactory _notificationEventFactory = new DefaultMessageFactory(getORB(), getConfiguration());

        Message _event = _notificationEventFactory.newMessage(any);
        try
        {
            runEvaluation(_event, expr, expect);
        } finally
        {
            _event.dispose();
        }
    }

    private void runEvaluation(StructuredEvent event, String expr) throws Exception
    {
        runEvaluation(event, expr, "TRUE");
    }

    private void runEvaluation(StructuredEvent event, String expr, String expect) throws Exception
    {
        // TODO factor out MessageFactory to NotificationTestCase (all tests)
        MessageFactory _notificationEventFactory = new DefaultMessageFactory(getORB(), getConfiguration());

        Message _event = _notificationEventFactory.newMessage(event);
        runEvaluation(_event, expr, expect);
    }

    private void runEvaluation(Message event, String expr, String expect) throws Exception
    {
        AbstractTCLNode _root = TCLParser.parse(expr);
        AbstractTCLNode _expect = TCLParser.parse(expect);

        FilterConstraint _evaluator = new ETCLFilterConstraint(_root);
        EvaluationResult _res;
        _root.acceptPostOrder(new TCLCleanUp());

        EvaluationContext _context = new EvaluationContext(getEvaluator());

        _res = _evaluator.evaluate(_context, event);

        assertEquals("expected " + _root.toStringTree() + " == " + _expect.toStringTree(), _expect
                .evaluate(null), _res);
    }
}

