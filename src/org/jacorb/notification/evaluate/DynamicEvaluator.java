package org.jacorb.notification.evaluate;

import org.jacorb.notification.node.EvaluationResult;

import java.util.Map;
import java.util.Hashtable;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;

import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynAnyFactoryHelper;
import org.omg.DynamicAny.DynStruct;
import org.omg.DynamicAny.DynStructHelper;
import org.jacorb.notification.node.TCLNode;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynUnion;
import org.omg.DynamicAny.DynUnionHelper;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.omg.DynamicAny.DynSequenceHelper;
import org.omg.DynamicAny.DynSequence;

/**
 * IdentifierEvaluator.java
 *
 *
 * Created: Sat Sep 07 21:05:32 2002
 *
 * @author <a href="mailto:a.bendt@berlin.de">Alphonse Bendt</a>
 * @version
 */

public class DynamicEvaluator {

    static boolean DEBUG = false;

    DynAnyFactory dynAnyFactory_;
    ORB orb_;

    static String DYN_ANY_FACTORY = "DynAnyFactory";
    static String NAME = "name";
    static String VALUE = "value";

    Any trueAny_;
    Any falseAny_;

    public DynamicEvaluator(ORB orb, DynAnyFactory dynAnyFactory) {
	dynAnyFactory_ = dynAnyFactory;
	orb_ = orb;
	trueAny_  = orb_.create_any();
	falseAny_ = orb_.create_any();

	trueAny_.insert_boolean(true);
	falseAny_.insert_boolean(false);
    }

    public boolean hasDefaultDiscriminator(Any any) throws BadKind {
	return (any.type().default_index() != -1);
    }

    public Any evaluateExistIdentifier(Any value, String identifier) 
	throws InconsistentTypeCode,
	       InvalidValue, 
	       TypeMismatch  {
	
	try {
	    evaluateIdentifier(value, identifier);
	    return trueAny_;
	} catch (EvaluationException e) {
	    return falseAny_;
	}

    }

    /** 
     * identify the unscoped IDL type name of a component.
     * (e.g. mystruct._typeid == 'mystruct')
     * @param value the component 
     * 
     * @return the IDL type name (string) wrapped in an any
     */
    public Any evaluateTypeName(Any value) throws BadKind {
	TypeCode _tc = value.type();
	Any _ret = orb_.create_any();
	_ret.insert_string(_tc.name());

	return _ret;
    }

    /** 
     * identify the RepositoryId of a component.
     * (e.g. mystruct._repos_id == 'IDL:module/mystruct:1.0'
     * 
     * @param value the component
     * 
     * @return the IDL type name (string) wrapped in an any
     */
    public Any evaluateRepositoryId(Any value) throws BadKind {
	TypeCode _tc = value.type();
	Any _ret = orb_.create_any();
	_ret.insert_string(_tc.id());

	return _ret;
    }

    /** 
     * identify the number of elements of a component.
     * if the parameter is a sequence or an array, this method will
     * return the number of elements in the list.
     * @param value the component
     * 
     * @return the number of elements in the list
     */
    public Any evaluateListLength(Any value) throws InconsistentTypeCode, EvaluationException {
	int _length;

	switch(value.type().kind().value()) {
	case TCKind._tk_array:
	    DynAny _dynAny = toDynAny(value);
	    _length = _dynAny.component_count();
	    break;
	case TCKind._tk_sequence:
	    DynSequence _dynSequence = toDynSequence(value);
	    _length = _dynSequence.get_length();
	    break;
	default:
	    throw new EvaluationException("Neither array nor sequence");
	}

	Any _any = orb_.create_any();
	_any.insert_long(_length);

	return _any;
    }

    String getDefaultUnionMemberName(TypeCode unionTypeCode) throws EvaluationException {
	try {
	    int _defaultIndex = unionTypeCode.default_index();
	    if (_defaultIndex != -1) {
		return unionTypeCode.member_name(_defaultIndex);
	    }
	} catch (BadKind bk) {
	} catch (Bounds b) {}
	throw new EvaluationException();
    }

    String getUnionMemberNameFromDiscriminator(TypeCode unionTypeCode, int discriminator) 
	throws BadKind, 
	       EvaluationException {
	
	Any _any = orb_.create_any();

	switch(unionTypeCode.discriminator_type().kind().value()) {
	case TCKind._tk_long:
	    _any.insert_long(discriminator);
	    break;
	case TCKind._tk_ulong:
	    _any.insert_ulong(discriminator);
	    break;
	case TCKind._tk_short:
	    _any.insert_short((short)discriminator);
	    break;
	case TCKind._tk_double:
	    _any.insert_double(discriminator);
	    break;
	case TCKind._tk_ushort:
	    _any.insert_ushort((short)discriminator);
	    break;
	}

	int _memberCount = unionTypeCode.member_count();
	String _discrimName = null;
	try {
	    for (int x=0; x < _memberCount; x++) {
		if (_any.equal(unionTypeCode.member_label(x))) {
		    return unionTypeCode.member_name(x);
		}
	    }
	} catch (Bounds b) {}
	throw new EvaluationException();
    }

    /** 
     * 
     * 
     * @param value 
     * 
     * @return 
     */
    public Any evaluateUnion(Any value) 
	throws InconsistentTypeCode, 
	       TypeMismatch,
	       EvaluationException, 
	       InvalidValue {

	debug("evaluateUnion(Any)");

	String _defaultMemberName = getDefaultUnionMemberName(value.type());
	return evaluateIdentifier(value, _defaultMemberName);
    }


    public Any evaluateUnion(Any value, int position)
	throws InconsistentTypeCode,
	       TypeMismatch,
	       EvaluationException,
	       InvalidValue {

	debug("evaluateUnion(Any, " + position + ")");

	try {
	    Any _ret = null;
	    DynUnion _dynUnion= toDynUnion(value);

	    _dynUnion.seek(0);
	    debug("extract idx: " + position + " from Union " + _dynUnion.type());
	    
	    String _discrimName = getUnionMemberNameFromDiscriminator(value.type(), position);

	    _ret = evaluateIdentifier(_dynUnion, _discrimName);

	    return _ret;
	} catch (BadKind b) {
	    throw new EvaluationException();
	}
    }

    public Any evaluateNamedValueList(Any any, String name)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	debug("evaluateNamedValueList()");

	Any _ret = null;
	DynAny _dynAny = toDynAny(any);
	int _count = _dynAny.component_count();
	_dynAny.rewind();
	DynAny _cursor;

	debug("Entries: " +_count);

	for (int x=0; x<_count; x++) {
	    _dynAny.seek(x);
	    _cursor = _dynAny.current_component();
	    _ret = evaluateNamedValue(_cursor, name);
	    if (_ret!=null) {
		break;
	    }
	}
	return _ret;
    }

    protected Any evaluateNamedValue(DynAny any, String name)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	debug("evaluate assoc " + name + " on a Any of type: " + any.type());

	Any _ret = null;

	String _anyName = evaluateIdentifier(any, NAME).extract_string();;

	debug("Any contains name " + _anyName);

	if (name.equals(_anyName)) {

	    _ret = evaluateIdentifier(any, VALUE);

	}
	return _ret;
    }

    public Any evaluateArrayIndex(Any any, int index)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch {

	debug("evaluate array idx " + index + " on a Any of type: " + any.type());

	Any _ret = null;

	DynAny _dynAny = toDynAny(any);
	DynAny _cursor;
	Object _res;

	_dynAny.rewind();
	_dynAny.seek(index);
	_cursor = _dynAny.current_component();
	debug("evaluation result is of type: " + _cursor.type());

	return _cursor.to_any();
    }

    Any evaluateIdentifier(DynAny any, int position)
	throws InconsistentTypeCode,
	       TypeMismatch, 
	       EvaluationException {

	Any _ret = null;
	DynAny _cursor;

	switch(any.type().kind().value()) {
	case TCKind._tk_struct:
	    any.seek(position);
	    _cursor = any.current_component();
	    break;
	default:
	    throw new EvaluationException("attempt to access member on non-struct");
	}
	return _cursor.to_any();
    }

    public Any evaluateIdentifier(Any any, int position)
	throws InconsistentTypeCode,
	       TypeMismatch,
	       EvaluationException {

	Any _ret = null;

	debug("evaluate idx " + position + " on Any");

	DynAny _dynAny = toDynAny(any);

	return evaluateIdentifier(_dynAny, position);
    }

    public Any evaluateDiscriminator(Any any) throws InconsistentTypeCode, EvaluationException {
	switch(any.type().kind().value()) {
	case TCKind._tk_union:
	    DynUnion _dynUnion = toDynUnion(any);
	    return _dynUnion.get_discriminator().to_any();
	default:
	    throw new EvaluationException("any does not contain member _d");
	}
    }

    public Any evaluateIdentifier(Any any, String identifier)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	DynAny _dynAny = toDynAny(any);
	return evaluateIdentifier(_dynAny, identifier);
    }

    Any evaluateIdentifier(DynAny any, String identifier)
	throws InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {

	Any _ret = null;

	identifier = stripBackslash(identifier);

	debug("evaluate " + identifier + " on Any");

	DynAny _cursor = any;
	Object _res;

	switch (any.type().kind().value()) {
	case TCKind._tk_struct:
	    debug("Any is a struct");

	    DynStruct _dynStruct = DynStructHelper.narrow(any);
	    String _currentName;

	    _dynStruct.rewind();
	    while (true) {
		_currentName = _dynStruct.current_member_name();

		debug(" => " + _currentName);

		if (_currentName.equals(identifier)) {
		    _cursor = _dynStruct.current_component();
		    break;
		}
		boolean _hasNext = _dynStruct.next();
		if (!_hasNext) {
		    throw new EvaluationException("struct has no member " + identifier);
		}
	    }
	    break;
	case TCKind._tk_union:
	    debug("Any is a Union");

	    DynUnion _dynUnion = toDynUnion(any);
	    if (_dynUnion.member_name().equals(identifier)) {
		_cursor = _dynUnion.member();
	    } else {
		debug(_dynUnion.member_name() + " != " + identifier);
		throw new EvaluationException("member " + identifier + " is not active on struct");
	    }
	    break;
	default:
	    debug("? " + any.type());
	    throw new RuntimeException();
	}

	debug("Result: " + _cursor);
	if (_cursor != null) {
	    debug("evaluation result is of type: " + _cursor.type());
	}
	
	if (_cursor == null) {
	    throw new EvaluationException("member not found");
	}

	_ret = _cursor.to_any();

	return _ret;
    }

    DynAny toDynAny(Any any) throws InconsistentTypeCode {
	return dynAnyFactory_.create_dyn_any(any);
    }

    DynUnion toDynUnion(Any any) throws InconsistentTypeCode {
	return DynUnionHelper.narrow(toDynAny(any));
    }

    DynUnion toDynUnion(DynAny dynAny) throws InconsistentTypeCode {
	return DynUnionHelper.narrow(dynAny);
    }

    DynStruct toDynStruct(DynAny dynAny) throws InconsistentTypeCode {
	return DynStructHelper.narrow(dynAny);
    }

    DynStruct toDynStruct(Any any) throws InconsistentTypeCode {
	return DynStructHelper.narrow(toDynAny(any));
    }

    DynSequence toDynSequence(Any any) throws InconsistentTypeCode {
	return DynSequenceHelper.narrow(toDynAny(any));
    }

    static String stripBackslash(String identifier) {
	StringBuffer _buffer = new StringBuffer();
	int _length = identifier.length();
	for (int x = 0; x < _length; x++) {
	    if (identifier.charAt(x) != '\\') {
		_buffer.append(identifier.charAt(x));
	    }
	}
	return _buffer.toString();
    }

    void debug(String msg) {
	if (DEBUG) {
	    System.err.println("[DynamicEvaluator] " + msg);
	}
    }

}// DynamicEvaluator
