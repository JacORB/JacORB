package org.jacorb.notification.node;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import org.omg.CORBA.TCKind;
import org.omg.CORBA.Any;
import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.framework.Poolable;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.jacorb.notification.util.ObjectPoolBase;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.EvaluationContext;
import org.omg.CORBA.Any;
import org.jacorb.notification.evaluate.EvaluationException;
import org.jacorb.notification.util.ObjectPoolBase;

/**
 * EvaluationResult.java
 *
 *
 * Created: Sat Jul 06 02:08:43 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class EvaluationResult implements TCLTokenTypes, Poolable {
    private boolean isFloat_;
    private Object value_;
    protected Object getValue() {return value_;}
    private Object setValue(Object value_) { this.value_=value_; return value_;}

    private Any any_;
    private ObjectPoolBase objectPool_ = null;

    public static final EvaluationResult BOOL_TRUE;
    public static final EvaluationResult BOOL_FALSE;

    static {
	EvaluationResult _r = new EvaluationResult();
	_r.setBool(true);
	BOOL_TRUE = wrapImmutable(_r);
	_r = new EvaluationResult();
	_r.setBool(false);
	BOOL_FALSE = wrapImmutable(_r);
    }

    public void reset() {
	setValue(null);
	isFloat_ = false;
	any_ = null;
    }

    public boolean isFloat() {
	return isFloat_;
    }

    public void setString(String s) {
	setValue(s);
    }

    public void setFloat(float f) {
	setFloat(new Double(f));
    }

    public void setFloat(Double d) {
	isFloat_ = true;
	setValue(d);
    }

    public void setInt(int i) {
	setInt(new Double(i));
    }

    public void setInt(Double i) {
	isFloat_ = false;
	setValue(i);
    }

    // TODO test was am schnellsten geht
    // try catch, instanceOf, membervariable
    public int getInt() throws DynamicTypeException {
	int _n;
	if (getValue() != null) {
	    try {
		return ((Double)getValue()).intValue();
	    } catch (ClassCastException c) {}
	    
	    try {
		return ((Boolean)getValue()).booleanValue() ? 1 : 0;
	    } catch (ClassCastException c2) {}
	    
	    try {
		String _s = (String)getValue();
		if (_s.length() == 1) {
		    return _s.charAt(0);
		}
	    } catch (ClassCastException c3) {}

	} else if (any_ != null) {
	    return any_.extract_long();
	}
	throw new DynamicTypeException();
    }

    public String getString() throws DynamicTypeException {
	try {
	    return (String)getValue();
	} catch (ClassCastException c) {
	    throw new DynamicTypeException();
	}
    }

    public float getFloat() throws DynamicTypeException {
	try {
	    return ((Double)getValue()).floatValue();
	} catch (ClassCastException c) {}

	try {
	    return ((Boolean)getValue()).booleanValue() ? (float)1.0 : 0;
	} catch (ClassCastException c2) {}

	try {
	    String _s = (String)getValue();
	    if (_s.length() == 1) {
		return _s.charAt(0);
	    }
	} catch (ClassCastException c3) {}

	throw new DynamicTypeException();
    }

    public boolean getBool() throws DynamicTypeException {
	try {
	    return ((Boolean)getValue()).booleanValue();
	} catch (ClassCastException c) {}
	throw new DynamicTypeException();
    }

    public void setBool(boolean b) {
	if (b) {
	    setValue(Boolean.TRUE);
	} else {
	    setValue(Boolean.FALSE);
	}
    }

    public Any getAny() {
	return any_;
    }

    public void addAny(Any any) {
	any_ = any;
    }

    public String toString() {
	StringBuffer _buffer = new StringBuffer("{");

	_buffer.append(getValue());

	_buffer.append("/any=");
	_buffer.append(any_);
	_buffer.append("}");

	return _buffer.toString();
    }

    public boolean equals(Object o) {
	if (o instanceof EvaluationResult) {
	    return (((EvaluationResult)o).getValue().equals(getValue()));
	}
	return super.equals(o);
    }

    public int compareTo(EvaluationContext context, EvaluationResult other) throws DynamicTypeException, 
										   EvaluationException {
	int _ret = Integer.MAX_VALUE;

	debug("compare " + this + ", " + other);

	if (getValue() == null && any_ != null && other.getValue() instanceof String) {
	    try {
		String _l = any_.type().member_name(0);
		String _r = other.getString();

		_ret = _l.compareTo(other.getString());
	    } catch (BadKind bk) {
	    } catch (Bounds bounds) {
	    }
	} else if (getValue() instanceof String || other.getValue() instanceof String) {
	    String _l = getString();
	    String _r = other.getString();

	    _ret = _l.compareTo(_r);
	    if (_ret < -1) {
		_ret = -1;
	    } else if (_ret > 1) {
		_ret = 1;
	    }
	} else if (isFloat() || other.isFloat()) {
	    float _l = getFloat();
	    float _r = other.getFloat();
	    
	    if (_l < _r) {
		_ret = -1;
	    } else if (_l == _r) {
		_ret = 0;
	    } else {
		_ret = 1;
	    }

	} else {
	    int _l = getInt();
	    int _r = other.getInt();

	    if (_l < _r) {
		_ret =  -1;
	    } else if (_l == _r) {
		_ret = 0;
	    } else {
		_ret = 1;
	    }
	}

	if (_ret == Integer.MAX_VALUE) {
	    throw new DynamicTypeException();
	}
	return _ret;
    }

    boolean DEBUG = false;

    void debug(String msg) {
	if (DEBUG) {
	    System.err.println("[EvaluationResult] " +msg);
	}
    }

    public void setObjectPool(ObjectPoolBase objectPool) {
	objectPool_ = objectPool;
    }

    public void release() {
	if (objectPool_ != null) {
	    objectPool_.returnObject(this);
	}
    }

    public static EvaluationResult wrapImmutable(EvaluationResult er) {
	return new ImmutableEvaluationResultWrapper(er);
    }
}// EvaluationResult

class ImmutableEvaluationResultWrapper extends EvaluationResult {

    static void unsupported() {
	throw new UnsupportedOperationException();
    }

    EvaluationResult er_;

    public int compareTo(EvaluationContext evaluationContext, 
			 EvaluationResult evaluationResult) throws DynamicTypeException, EvaluationException {
	return er_.compareTo(evaluationContext, evaluationResult);
    }

    public Object getValue() {
	return er_.getValue();
    }

    public int getInt() throws DynamicTypeException {
	return er_.getInt();
    }

    public float getFloat() throws DynamicTypeException {
	return er_.getFloat();
    }

    public boolean equals(Object object) {
	return er_.equals(object);
    }

    public String toString() {
	return er_.toString();
    }

    public String getString() throws DynamicTypeException {
	return er_.getString();
    }

    public boolean isFloat() {
	return er_.isFloat();
    }

    public boolean getBool() throws DynamicTypeException {
	return er_.getBool();
    }

    public Any getAny() {
	return er_.getAny();
    }

    public void setObjectPool(ObjectPoolBase objectPoolBase) {
	er_.setObjectPool(objectPoolBase);
    }

    ImmutableEvaluationResultWrapper(EvaluationResult er) {
	er_ = er;
    }
    
    public void reset() {
	unsupported();
    }

    public void release() {
	unsupported();
    }

    public void setObjectPool() {
	unsupported();
    }

    public void setString(String s) {
	unsupported();
    }

    public void setFloat(float f) {
	unsupported();
    }

    public void setFloat(Double d) {
	unsupported();
    }

    public void setInt(int i) {
	unsupported();
    }

    public void setInt(Double i) {
	unsupported();
    }

    public void setBool(boolean b) {
	unsupported();
    }

    public void addAny(Any a) {
	unsupported();
    }
}
