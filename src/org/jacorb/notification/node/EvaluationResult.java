/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.notification.node;

/**
 * EvaluationResult.java
 *
 *
 * Created: Sat Jul 06 02:08:43 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */

import org.omg.CORBA.TCKind;
import org.omg.CORBA.Any;

public class EvaluationResult implements TCLTokenTypes {

//     private String string_;
//     private Double number_;
//     private boolean bool_;

    private boolean isFloat_;

    private Object value_;
    private Any any_;

    public static final EvaluationResult BOOL_TRUE;
    public static final EvaluationResult BOOL_FALSE;

    static {
	BOOL_TRUE = new EvaluationResult();
	BOOL_FALSE = new EvaluationResult();

	BOOL_TRUE.setBool(true);
	BOOL_FALSE.setBool(false);
    }

    public boolean isFloat() {
	return isFloat_;
    }

    public void setString(String s) {
	value_ = s;
    }

    public void setFloat(float f) {
	setFloat(new Double(f));
    }

    public void setFloat(Double d) {
	isFloat_ = true;
	value_ = d;
    }

    public void setInt(int i) {
	setInt(new Double(i));
    }

    public void setInt(Double i) {
	isFloat_ = false;
	value_ = i;
    }

    // TODO test was am schnellsten geht
    // try catch, instanceOf, membervariable
    public int getInt() throws DynamicTypeException {
	int _n;
	try {
	    return ((Double)value_).intValue();
	} catch (ClassCastException c) {}
	
	try {
	    return ((Boolean)value_).booleanValue() ? 1 : 0;
	} catch (ClassCastException c2) {}

	try {
	    String _s = (String)value_;
	    if (_s.length() == 1) {
		return _s.charAt(0);
	    }
	} catch (ClassCastException c3) {}

	throw new DynamicTypeException();
    }

    public String getString() throws DynamicTypeException {
	try {
	    return (String)value_;
	} catch (ClassCastException c) {
	    throw new DynamicTypeException();
	}
    }

    public float getFloat() throws DynamicTypeException {
	try {
	    return ((Double)value_).floatValue();
	} catch (ClassCastException c) {}

	try {
	    return ((Boolean)value_).booleanValue() ? (float)1.0 : 0;
	} catch (ClassCastException c2) {}

	try {
	    String _s = (String)value_;
	    if (_s.length() == 1) {
		return _s.charAt(0);
	    }
	} catch (ClassCastException c3) {}

	throw new DynamicTypeException();
    }

    public boolean getBool() throws DynamicTypeException {
	try {
	    return ((Boolean)value_).booleanValue();
	} catch (ClassCastException c) {}
	throw new DynamicTypeException();
    }

    public void setBool(boolean b) {
	if (b) {
	    value_ = Boolean.TRUE;
	} else {
	    value_ = Boolean.FALSE;
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

	_buffer.append(value_);

	_buffer.append("/any=");
	_buffer.append(any_);
	_buffer.append("}");

	return _buffer.toString();
    }

    public boolean equals(Object o) {
	if (o instanceof EvaluationResult) {
	    return (((EvaluationResult)o).value_.equals(value_));
	}
	return super.equals(o);
    }

    public int compareTo(EvaluationResult other) throws DynamicTypeException {
	int _ret = Integer.MAX_VALUE;

	if (value_ instanceof String || other.value_ instanceof String) {
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

    void debug(String msg) {
	System.err.println("[EvaluationResult] " +msg);
    }

}// EvaluationResult
