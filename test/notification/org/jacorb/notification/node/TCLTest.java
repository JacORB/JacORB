package org.jacorb.notification.node;

import antlr.RecognitionException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.NotificationEventFactory;
import org.jacorb.notification.TestUtils;
import org.jacorb.notification.evaluate.ConstraintEvaluator;
import org.jacorb.notification.evaluate.DynamicEvaluator;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.evaluate.ResultExtractor;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.NamedValue;
import org.jacorb.notification.test.Person;
import org.jacorb.notification.test.PersonHelper;
import org.jacorb.notification.test.Profession;
import org.jacorb.notification.test.TestUnion;
import org.jacorb.notification.test.TestUnionHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.StructuredEventHelper;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * TCLTest.java
 *
 * Created: Thu Jul 18 16:08:15 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TCLTest extends TestCase {

    ApplicationContext applicationContext_;
    ORB orb_;

    ResultExtractor resultExtractor_;
    DynamicEvaluator dynamicEvaluator_;
    DynAnyFactory dynAnyFactory_;
    NotificationEventFactory notificationEventFactory_;

    //////////////////////////////////////////////////
    // the testdata

    Any testPerson_;
    Any testUnion_;
    Any testUnion1_;
    Any testUnion2_;
    Any testUnion3_;
    Any testUnion4_;
    Any testUnion5_;

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	POA _poa = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	applicationContext_ = new ApplicationContext(orb_, _poa);

	dynAnyFactory_ = DynAnyFactoryHelper.narrow(orb_.resolve_initial_references("DynAnyFactory"));
	resultExtractor_ = new ResultExtractor(dynAnyFactory_);
	dynamicEvaluator_ = new DynamicEvaluator(orb_, dynAnyFactory_);

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

	_person.aliases = new String[] {"Alias0", "Alias1", "Alias2"};
	_person.numbers = new int[] {10, 20, 30, 40, 50};

	testPerson_ = orb_.create_any();
	PersonHelper.insert(testPerson_, _person);

	TestUnion _t1, _t2, _t3, _t4, _t5, _t;

	_t = new TestUnion();
	_t.default_person(_person);

	_t1 = new TestUnion();
	_t1.long_(100);

	_t2 = new TestUnion();
	_t2.string_("String");

	_t3 = new TestUnion();
	_t3.named_value_(new NamedValue("this is the name", "this is the value"));

	_t4 = new TestUnion();
	_t4.person_(_person);

	_t5 = new TestUnion();
	NamedValue[] lonv = new NamedValue[1];
	lonv[0] = new NamedValue("name", "value");
	_t5.named_value_array(lonv);

	testUnion_ = orb_.create_any();
	testUnion1_ = orb_.create_any();
	testUnion2_ = orb_.create_any();
	testUnion3_ = orb_.create_any();
	testUnion4_ = orb_.create_any();
	testUnion5_ = orb_.create_any();

	TestUnionHelper.insert(testUnion_, _t);
	TestUnionHelper.insert(testUnion1_, _t1);
	TestUnionHelper.insert(testUnion2_, _t2);
	TestUnionHelper.insert(testUnion3_, _t3);
	TestUnionHelper.insert(testUnion4_, _t4);
	TestUnionHelper.insert(testUnion5_, _t5);
    }

    public TCLTest(String name) {
	super(name);
    }

    void runEvaluation(Any any, String expr) throws Exception {
	TestUtils.runEvaluation(this, applicationContext_, any, expr);
    }

    void runEvaluation(Any any, String expr, String expect) throws Exception {
	TestUtils.runEvaluation(this, applicationContext_, any, expr, expect);
    }

    void runStaticTypeCheck(String expr) throws Exception {
	runStaticTypeCheck(expr, true);
    }

    void runStaticTypeCheck(String expr, boolean fails) throws Exception {
	try {
	    TCLNode _root = ConstraintEvaluator.parse(expr);

	    StaticTypeChecker _checker = new StaticTypeChecker();

	    _checker.check(_root);

	    if (fails) {
		fail("static type error should have occcured");
	    }
	} catch (StaticTypeException ste) {}
    }

    void runEvaluation(String fst, String snd) throws Exception {
	TCLNode fstNode = ConstraintEvaluator.parse(fst);
	TCLNode sndNode = ConstraintEvaluator.parse(snd);

	EvaluationContext _context = new EvaluationContext();
	    
	EvaluationResult pre_res = fstNode.evaluate(_context);
	EvaluationResult pst_res = sndNode.evaluate(_context);
	
	assertEquals("expected " + fstNode.toStringTree() + " == " + sndNode.toStringTree(),
		     pre_res, 
		     pst_res);
    }

    //////////////////////////////////////////////////
    // and now some tests

    public void testPlus() throws Exception {
	runEvaluation("2", "1 + 1");
	runEvaluation("0", "1 + -1");
	runEvaluation("0", "-1 + 1");
	runEvaluation("2", "1 + 1.0");
	runEvaluation("2.0", "1 + 1");
    }

    public void testMinus() throws Exception {
	runEvaluation("0", "1 - 1");
	runEvaluation("2", "1 - -1");
	runEvaluation("-2", "-1 - 1");
	runEvaluation("0", "1 - 1.0");
	runEvaluation("0.0", "1 - 1");
    }

    public void testDiv() throws Exception{
	runEvaluation("5", "10/2");
	runEvaluation("1/3", "10/30");
	runEvaluation("0.25", "1.0/4");
    }

    public void testMult() throws Exception{
	runEvaluation("100", "10*10");
	runEvaluation("1", "1 * 1");
	runEvaluation("1", "0.25 * 4");
	runEvaluation("-1", "1 * -1");
    }

    public void testSimpleNumbers() throws Exception {
	runEvaluation("5" , "5");
	runEvaluation("-1" , "-1");
	runEvaluation("1" , "+1");
	runEvaluation("1" , "1.0");
	runEvaluation("1" , "+1.0");
	runEvaluation("-1" , "-1.0");
	runEvaluation(".1" , "+.1");
	runEvaluation("-0.1" , "-.1");
    }

    public void testFloat() throws Exception {
	runEvaluation("1000", "10e+2");
	runEvaluation("100", "10e+1");
	runEvaluation("10", "10e+0");
	runEvaluation("1", "10e-1");
	runEvaluation(".1", "10e-2");
	runEvaluation(".01", "10e-3");
	runEvaluation("-.01", "-10e-3");
    }

    public void testSimpleOperations() throws Exception {
	runEvaluation("0" , "1-1");
	runEvaluation("2" , "1+1");
	runEvaluation("1" , "1*1");
	runEvaluation("1" , "1/1");
    }

    public void testParentheses() throws Exception {
	runEvaluation("7", "1+2*3");
	runEvaluation("7", "1+(2*3)");
	runEvaluation("1+2*3" , "1+(2*3)");
	runEvaluation("9", "(1+2)*3");
	runEvaluation("1+(2+(3+(4+5)))", "(((1+2)+3)+4)+5");
	runEvaluation("1*(2*(3*(4*5)))", "(((1*2)*3)*4)*5");
    }

    public void testGt() throws Exception{
	runEvaluation("TRUE", "1>0");
	runEvaluation("FALSE", "0>1");

	runEvaluation("TRUE", "TRUE > FALSE");
	runEvaluation("FALSE", "FALSE > TRUE");

	runEvaluation("TRUE", "'bbb' > 'aaa'");
	runEvaluation("FALSE", "'bbb' > 'ccc'");
    }

    public void testLt() throws Exception{
	runEvaluation("FALSE", "1<0");
	runEvaluation("TRUE", "0<1");

	runEvaluation("FALSE", "TRUE < FALSE");
	runEvaluation("TRUE", "FALSE < TRUE");

	runEvaluation("FALSE", "'bbb' < 'aaa'");
	runEvaluation("TRUE", "'bbb' < 'ccc'");
    }

    public void testLte() throws Exception{
	runEvaluation("TRUE", "0<=1");
	runEvaluation("TRUE", "0<=0");
	runEvaluation("FALSE", "1<=0");
	runEvaluation("TRUE", "'abc'<='abc'");
	runEvaluation("TRUE", "'abc'<='dbc'");
	runEvaluation("FALSE", "'bbc'<='abc'");
    }

    public void testGte() throws Exception{
 	runEvaluation("FALSE", "0>=1");
 	runEvaluation("TRUE", "0>=0");
 	runEvaluation("TRUE", "1>=0");
 	runEvaluation("TRUE", "'abc'>='abc'");
	runEvaluation("FALSE", "'abc'>='dbc'");
	runEvaluation("TRUE", "'bbc'>='abc'");
    }

    public void testEq() throws Exception{
	runEvaluation("TRUE", "TRUE == TRUE");
	runEvaluation("TRUE", "FALSE == FALSE");
	runEvaluation("FALSE", "FALSE == TRUE");
	runEvaluation("FALSE", "TRUE == FALSE");
    }

    public void testNeq() throws Exception {
	runEvaluation("TRUE", "0!=1");
	runEvaluation("FALSE", "1!=1");

	runEvaluation("TRUE", "'bla'!='blubb'");
	runEvaluation("FALSE", "'bla'!='bla'");

	runEvaluation("TRUE", "TRUE!=FALSE");
	runEvaluation("TRUE", "FALSE!=TRUE");
	runEvaluation("FALSE", "TRUE!=TRUE");
	runEvaluation("FALSE", "FALSE!=FALSE");
    }

    public void testAnd() throws Exception {
	runEvaluation("TRUE", "TRUE and TRUE");
	runEvaluation("FALSE", "TRUE and FALSE");
	runEvaluation("FALSE", "FALSE and TRUE");
	runEvaluation("FALSE", "FALSE and FALSE");
    }

    public void testOr() throws Exception {
	runEvaluation("TRUE", "TRUE or TRUE");
	runEvaluation("TRUE", "TRUE or FALSE");
	runEvaluation("TRUE", "FALSE or TRUE");
	runEvaluation("FALSE", "FALSE or FALSE");
    }

    public void testNot() throws Exception {
	runEvaluation("TRUE", "not FALSE");
	runEvaluation("FALSE", "not TRUE");
    }

    public void testLazyEval() throws Exception {
	runEvaluation("TRUE", "TRUE or (1/0)");
	runEvaluation("FALSE", "FALSE and (1/0)");

    }

    public void testTwiddle() throws Exception {
	runEvaluation("TRUE", "'substr' ~ 'substring'");
	runEvaluation("FALSE", "'not' ~ 'substring'");
    }

    public void testCast() throws Exception {
	runEvaluation("FALSE", "2/3 > 0");
	runEvaluation("TRUE", "(1.0 * 2/3) > 0");
    }

    public void testTypeConversion() throws Exception {
	runEvaluation("TRUE", "'H' + 1 > 32");
    }

    public void testStaticTypeCheck() throws Exception {
	runStaticTypeCheck("5 + 'al'");
	runStaticTypeCheck("5 + 'a'", false);
	runStaticTypeCheck("5 ~ 'a'");
	runStaticTypeCheck("TRUE + 'a'");
	runStaticTypeCheck("'a' and 'b'");
	runStaticTypeCheck("1 * (TRUE and TRUE)", false);
    }

    public void testEnum() throws Exception {
	runEvaluation(testPerson_, "$.person_profession == STUDENT");
    }

    public void testImplicit() throws Exception {
	runEvaluation(testPerson_, "$._type_id == 'Person'");
	runEvaluation(testPerson_, "$._repos_id == 'IDL:org.jacorb.notification/test/Person:1.0'");

	runEvaluation(testPerson_, "$.nv._length == 2");

	try {
	    runEvaluation(testPerson_, "$.first_name._length == 2");
	    fail();
	} catch (Throwable e) {}

	runEvaluation(testPerson_, "$.phone_numbers._length == 2");
	runEvaluation(testPerson_, "$.4._length == 2");

	runEvaluation(testUnion5_, "$.named_value_array._length == 1");
	runEvaluation(testUnion5_, "$.(5)._length == 1");
    }

    public void testDefault() throws Exception {
	runEvaluation(testUnion_, "default $._d and $.().first_name == 'Firstname'");
    }

    public void testExist() throws Exception {
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

    public void testUnion() throws Exception {

	runEvaluation(testUnion1_, "$.long_ > 54");
	runEvaluation(testUnion1_, "$._d == 1 and $.(0) == 100");
	runEvaluation(testUnion1_, "$._d == 1 and $.(1) == 100");

 	try {
 	    runEvaluation(testUnion1_, "$.string_");
 	    fail();
 	} catch (EvaluationException e) {}

	runEvaluation(testUnion1_, "$.(1) > 54");
	runEvaluation(testUnion1_, "$.(0) > 54");

	runEvaluation(testUnion2_, "$.string_" , "'String'");
	runEvaluation(testUnion2_, "$.(2)" , "'String'");

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

    public void testComponent() throws Exception {
	runEvaluation(testPerson_, "$.first_name", "'Firstname'");

	try {
	    runEvaluation(testPerson_, "$.third_name", "'Something'");
	    fail();
	} catch (EvaluationException e) {}		

	runEvaluation(testPerson_, "$.0", "'Firstname'");

	//////////

	runEvaluation(testPerson_, "$.home_address.street", "'Takustr.'");

	runEvaluation(testPerson_, "$.3.0", "'Takustr.'");

	//////////

	runEvaluation(testPerson_, "$.first_name == 'Firstname'");

	runEvaluation(testPerson_, "$.age > 30", "FALSE");

 	runEvaluation(testPerson_, "$.age > 20 and $.first_name =='Firstname'");

 	runEvaluation(testPerson_, "$.age < 30 or $.first_name == 'Adalbert'");

	//////////

  	runEvaluation(testPerson_, "$.phone_numbers[0]", "'32132132'");

	runEvaluation(testPerson_, "$.4[0]", "'32132132'");

	//////////

 	runEvaluation(testPerson_, "'321' ~ $.phone_numbers[0]");

	//////////

 	runEvaluation(testPerson_, "$.nv(priority)", "'Very High'");

	runEvaluation(testPerson_, "$.5(priority)", "'Very High'");

	runEvaluation(testPerson_, "$.nv[0].name == 'priority' and $.nv[0].value == 'Very High'");

	//////////

  	runEvaluation(testPerson_, "$.nv(stuff) > 'aaa'");
    }

    public void testDynamicTypeExceptions() throws Exception {
	// provoke some Dynamic Type Errors
 	try {
	    runEvaluation(testPerson_, "$.first_name + 1 == 10");
 	    fail();
 	} catch (org.jacorb.notification.node.DynamicTypeException e) {}

	try {
	    runEvaluation(testPerson_, "$.age == '29'");
	    fail();
	} catch (DynamicTypeException e) {}

	try {
	    runEvaluation(testPerson_, "$.age and true");
	    fail();
	} catch (DynamicTypeException e) {}
    }

    public void testParse() throws Exception {
	try {
	    ConstraintEvaluator.parse("$..");
	    fail();
	} catch(RecognitionException e) {}

	try {
	    ConstraintEvaluator.parse("ab +-/ abc");
	    fail();
	} catch(RecognitionException e) {}
    }

    public void testShorthandNotiation() throws Exception {
	Any _testData = TestUtils.getStructuredEventAny(orb_);

	runEvaluation(_testData, "$domain_name == 'TESTING'");
	runEvaluation(_testData, "$type_name == 'TESTING'");
	runEvaluation(_testData, "$event_name == 'ALARM'");
    }

    public void testInsertComponentName() throws Exception {
	ComponentOperator _comp = 
	    (ComponentOperator)ConstraintEvaluator.parse("$.first_name.last_name");

	_comp.acceptInOrder(new TCLCleanUp());
	assertEquals("$.first_name.last_name", _comp.getComponentName());

	TCLNode _root = 
	    (TCLNode)ConstraintEvaluator.parse("$.first_name.value + 5");

	_root.acceptInOrder(new TCLCleanUp());
	
	_comp = (ComponentOperator)_root.getFirstChild();
	assertEquals("$.first_name.value", _comp.getComponentName());

	_comp = (ComponentOperator)ConstraintEvaluator.parse("$domain_name");
	_comp.acceptInOrder(new TCLCleanUp());
	assertEquals("$domain_name", _comp.getComponentName());
    }

    public void testInOperator() throws Exception {
	runEvaluation(testPerson_, "'Alias0' in $.aliases");
	runEvaluation(testPerson_, "'Alias1' in $.aliases");
	runEvaluation(testPerson_, "'Alias2' in $.aliases");
	runEvaluation(testPerson_, "not ('AliasXYZ' in $.aliases)");
	runEvaluation(testPerson_, "10 in $.numbers");
	runEvaluation(testPerson_, "not (25 in $.numbers)");
    }

    public void testPassOverUnnamedLayers() throws Exception {
	Any _any = orb_.create_any();
	_any.insert_any(testPerson_);
	runEvaluation(_any, "$.first_name == 'Firstname'");
    }

    public void testWhiteboardExpr() throws Exception {
	Any _any = orb_.create_any();
	StructuredEvent _event = TestUtils.getStructuredEvent(orb_);
	_event.header.variable_header = new Property[1];
	Any _anyInt = orb_.create_any();
	_anyInt.insert_long(10);
	
	_event.header.variable_header[0] = new Property("workgroup_id", _anyInt);

	StructuredEventHelper.insert(_any, _event);
	runEvaluation(_any, "$.header.variable_header(workgroup_id) != 20");
    }

    public void testFixTCLGrammarBug() throws Exception {
	TCLNode _root = ConstraintEvaluator.parse(".1");
	//	System.out.println(_root.toStringTree());
	
	_root = ConstraintEvaluator.parse("$.1");
	//	System.out.println(_root.toStringTree());
    }

    public static Test suite() {
	TestSuite suite;

	suite = new TestSuite();
	suite.addTest(new TCLTest("testSimpleNumbers"));
	suite = new TestSuite(TCLTest.class);
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// TCLTest

