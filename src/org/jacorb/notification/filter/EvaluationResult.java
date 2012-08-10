package org.jacorb.notification.filter;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
import java.lang.reflect.Field;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationResult
{
    public static final EvaluationResult BOOL_TRUE;

    public static final EvaluationResult BOOL_FALSE;

    // //////////////////////////////////////

    static
    {
        EvaluationResult _r = new EvaluationResult();
        _r.setBool(true);
        BOOL_TRUE = wrapImmutable(_r);

        _r = new EvaluationResult();
        _r.setBool(false);
        BOOL_FALSE = wrapImmutable(_r);
    }

    // //////////////////////////////////////

    private int typeCode_;

    private Object value_;

    private Any any_;

    // //////////////////////////////////////

    protected Object getValue()
    {
        return value_;
    }

    private Object setValue(Object value)
    {
        Object _old = value_;

        value_ = value;

        return _old;
    }

    public boolean isLongLong()
    {
        return typeCode_ == TCKind._tk_longlong;
    }

    public boolean isDouble()
    {
        return typeCode_ == TCKind._tk_double;
    }

    public boolean isFloat()
    {
        return typeCode_ == TCKind._tk_float;
    }

    public boolean isLong()
    {
        return typeCode_ == TCKind._tk_long;
    }

    public boolean isString()
    {
        return typeCode_ == TCKind._tk_string;
    }

    public void setString(String s)
    {
        setValue(s);
        typeCode_ = TCKind._tk_string;
    }

    public void setFloat(float f)
    {
        setFloat(new Double(f));
    }

    public void setFloat(double d)
    {
        setFloat(new Double(d));
    }

    public void setFloat(Double d)
    {
        setValue(d);
        typeCode_ = TCKind._tk_float;
    }

    public void setLongLong(long l)
    {
        setLongLong(new Double(l));
    }

    public void setLongLong(Double d)
    {
        setValue(d);
        typeCode_ = TCKind._tk_longlong;
    }

    public void setLong(int l)
    {
        setLong(new Double(l));
    }

    public void setLong(Double d)
    {
        setValue(d);
        typeCode_ = TCKind._tk_long;
    }

    public void setDouble(Double d)
    {
        setValue(d);
        typeCode_ = TCKind._tk_double;
    }

    public void setDouble(double d)
    {
        setDouble(new Double(d));
    }

    public String getString() throws DynamicTypeException
    {
        try
        {
            return (String) getValue();
        } catch (ClassCastException c)
        {
            throw newDynamicTypeException("String");
        }
    }

    private DynamicTypeException newDynamicTypeException(String type)
    {
        return new DynamicTypeException("could not convert value: " + getValue() + " to " + type);
    }

    private static DynamicTypeException newDynamicTypeException(String operand,
            EvaluationResult left, EvaluationResult right)
    {
        return new DynamicTypeException("failed to " + operand + ": incompatible operands " + left
                + " and " + right);
    }

    public long getLongLong() throws DynamicTypeException
    {
        if (getValue() == null)
        {
            return any_.extract_longlong();
        }

        try
        {
            return ((Double) getValue()).longValue();
        } catch (ClassCastException e)
        {
            // ignored. will retry
        }

        try
        {
            return ((Boolean) getValue()).booleanValue() ? 1l : 0;
        } catch (ClassCastException e)
        {
            // ignored. will retry
        }

        try
        {
            String _s = (String) getValue();

            if (_s.length() == 1)
            {
                return _s.charAt(0);
            }
        } catch (ClassCastException e)
        {
            // ignored. will throw error
        }

        throw newDynamicTypeException("LongLong");
    }

    public int getLong() throws DynamicTypeException
    {
        if (getValue() != null)
        {
            try
            {
                return ((Double) getValue()).intValue();
            } catch (ClassCastException e)
            {
                // ignored. will retry
            }

            try
            {
                return ((Boolean) getValue()).booleanValue() ? 1 : 0;
            } catch (ClassCastException e)
            {
                // ignored. will retry
            }

            try
            {
                String _s = (String) getValue();

                if (_s.length() == 1)
                {
                    return _s.charAt(0);
                }
            } catch (ClassCastException e)
            {
                // ignored. will throw error
            }

        }
        else
        {
            return any_.extract_long();
        }

        throw newDynamicTypeException("Long");
    }

    public double getDouble() throws DynamicTypeException
    {
        try
        {
            return ((Double) getValue()).doubleValue();
        } catch (ClassCastException e)
        {
            // ignored. will retry.
        }

        try
        {
            return ((Boolean) getValue()).booleanValue() ? 1d : 0;
        } catch (ClassCastException e)
        {
            // ignored. will retry.
        }

        try
        {
            String _s = (String) getValue();

            if (_s.length() == 1)
            {
                return _s.charAt(0);
            }
        } catch (ClassCastException e)
        {
            // ignored. will throw error
        }

        throw newDynamicTypeException("Double");
    }

    public float getFloat() throws DynamicTypeException
    {
        try
        {
            return ((Double) getValue()).floatValue();
        }
        catch (ClassCastException e)
        {
            // ignored. will retry.
        }

        try
        {
            return ((Boolean) getValue()).booleanValue() ? 1f : 0;
        }
        catch (ClassCastException e)
        {
            // ignored. will retry.
        }

        try
        {
            String _str = (String) getValue();

            if (_str.length() == 1)
            {
                return _str.charAt(0);
            }
        }
        catch (ClassCastException e)
        {
            // ignored. will throw error
        }

        throw newDynamicTypeException("Float");
    }

    public boolean getBool() throws DynamicTypeException
    {
        try
        {
            return ((Boolean) getValue()).booleanValue();
        }
        catch (ClassCastException c)
        {
            // ignored. will throw error
        }

        throw newDynamicTypeException("Boolean");
    }

    public void setBool(boolean value)
    {
        setValue(Boolean.valueOf(value));

        typeCode_ = TCKind._tk_boolean;
    }

    public Any getAny()
    {
        return any_;
    }

    public void addAny(Any any)
    {
        any_ = any;
    }

    public void setAny(Any any)
    {
        addAny(any);
        typeCode_ = any.type().kind().value();
    }

    private static String typeCodeToName(int x)
    {
        try
        {
            Field[] _fields = TCKind.class.getDeclaredFields();

            return _fields[x].getName();
        }
        catch (Exception e)
        {
            return "unknown: " + x;
        }
    }

    public String toString()
    {
        StringBuffer _buffer = new StringBuffer("{");

        _buffer.append(getValue());
        _buffer.append(";TC=");
        _buffer.append(typeCodeToName(typeCode_));
        _buffer.append(";any=");
        _buffer.append(any_);
        _buffer.append("}");

        return _buffer.toString();
    }

    public boolean equals(Object o)
    {
        if (o instanceof EvaluationResult)
        {
            return (((EvaluationResult) o).getValue().equals(getValue()));
        }

        return super.equals(o);
    }

    public int hashCode()
    {
        return getValue().hashCode();
    }

    public int compareTo(EvaluationResult other) throws DynamicTypeException, EvaluationException
    {
        final int _result;

        if (getValue() == null && any_ != null && other.getValue() instanceof String)
        {
            try
            {
                String _left = any_.type().member_name(0);

                _result = _left.compareTo(other.getString());
            } catch (BadKind e)
            {
                throw new EvaluationException(e);
            } catch (Bounds e)
            {
                throw new EvaluationException(e);
            }
        }
        else if (isString() || other.isString())
        {
            _result = getString().compareTo(other.getString());
        }
        else if (isFloat() || other.isFloat())
        {
            float _l = getFloat();
            float _r = other.getFloat();

            _result = Float.compare(_l, _r);
        }
        else if (isLongLong() || other.isLongLong())
        {
            long _l = getLongLong();
            long _r = other.getLongLong();

            _result = (_l<_r ? -1 : (_l==_r ? 0 : 1));
        }
        else
        {
            int _l = this.getLong();
            int _r = other.getLong();

            _result = _l - _r;
        }

        return _result;
    }

    public static EvaluationResult wrapImmutable(EvaluationResult e)
    {
        return new ImmutableEvaluationResultWrapper(e);
    }

    public static EvaluationResult plus(EvaluationResult left, EvaluationResult right)
            throws DynamicTypeException
    {
        final EvaluationResult _res = new EvaluationResult();

        if (left.isDouble() || right.isDouble())
        {
            _res.setDouble(left.getDouble() + right.getDouble());
        }
        else if (left.isFloat() || right.isFloat())
        {
            _res.setFloat(left.getDouble() + right.getDouble());
        }
        else if (left.isLongLong() || right.isLongLong())
        {
            _res.setLongLong(left.getLongLong() + right.getLongLong());
        }
        else if (left.isLong() || right.isLong())
        {
            _res.setLong(left.getLong() + right.getLong());
        }
        else
        {
            throw newDynamicTypeException("add", left, right);
        }

        return _res;
    }

    public static EvaluationResult minus(EvaluationResult left, EvaluationResult right)
            throws DynamicTypeException
    {
        final EvaluationResult _res = new EvaluationResult();

        if (left.isDouble() || right.isDouble())
        {
            _res.setDouble(left.getDouble() - right.getDouble());
        }
        else if (left.isFloat() || right.isFloat())
        {
            _res.setFloat(left.getDouble() - right.getDouble());
        }
        else if (left.isLongLong() || right.isLongLong())
        {
            _res.setLongLong(left.getLongLong() - right.getLongLong());
        }
        else if (left.isLong() || right.isLong())
        {
            _res.setLong(left.getLong() - right.getLong());
        }
        else
        {
            throw newDynamicTypeException("subtract", left, right);
        }

        return _res;
    }

    static public EvaluationResult unaryMinus(EvaluationResult r) throws DynamicTypeException
    {
        final EvaluationResult _ret = new EvaluationResult();

        if (r.isFloat())
        {
            _ret.setFloat(-r.getFloat());
        }
        else
        { // (r.isFloat()) {
            _ret.setDouble(-r.getDouble());
        }

        return _ret;
    }

    static public EvaluationResult div(EvaluationResult left, EvaluationResult right)
            throws DynamicTypeException
    {
        final EvaluationResult _res = new EvaluationResult();

        if (left.isDouble() || right.isDouble())
        {
            _res.setDouble(left.getDouble() / right.getDouble());
        }
        else if (left.isFloat() || right.isFloat())
        {
            _res.setFloat(left.getDouble() / right.getDouble());
        }
        else if (left.isLongLong() || right.isLongLong())
        {
            _res.setLongLong(left.getLongLong() / right.getLongLong());
        }
        else if (left.isLong() || right.isLong())
        {
            _res.setLong(left.getLong() / right.getLong());
        }
        else
        {
            throw newDynamicTypeException("divide", left, right);
        }

        return _res;
    }

    static public EvaluationResult mult(EvaluationResult left, EvaluationResult right)
            throws DynamicTypeException
    {
        final EvaluationResult _res = new EvaluationResult();

        if (left.isDouble() || right.isDouble())
        {
            _res.setDouble(left.getDouble() * right.getDouble());
        }
        else if (left.isFloat() || right.isFloat())
        {
            _res.setFloat(left.getDouble() * right.getDouble());
        }
        else if (left.isLongLong() || right.isLongLong())
        {
            _res.setLongLong(left.getLongLong() * right.getLongLong());
        }
        else if (left.isLong() || right.isLong())
        {
            _res.setLong(left.getLong() * right.getLong());
        }
        else
        {
            throw newDynamicTypeException("multiply", left, right);
        }

        return _res;
    }

    public static EvaluationResult fromAny(Any any)
    {
        if (any == null)
        {
            return null;
        }

        final EvaluationResult result;

        switch (any.type().kind().value()) {
        case TCKind._tk_any:
            result = fromAny(any.extract_any());

            break;
        default:
            result = new EvaluationResult();

            extractIntoEvaluationResult(result, any);
        }

        return result;
    }

    private static void extractIntoEvaluationResult(EvaluationResult result, Any any)
    {
        switch (any.type().kind().value()) {
        case TCKind._tk_boolean:
            result.setBool(any.extract_boolean());
            break;
        case TCKind._tk_string:
            result.setString(any.extract_string());
            break;
        case TCKind._tk_long:
            result.setLong(any.extract_long());
            break;
        case TCKind._tk_short:
            result.setLong(any.extract_short());
            break;
        case TCKind._tk_ulonglong:
            result.setLongLong(any.extract_ulonglong());
            break;
        case TCKind._tk_float:
            result.setFloat(any.extract_float());
            break;
        case TCKind._tk_double:
            result.setDouble(any.extract_double());
            break;
        default:
            result.setAny(any);
            break;
        }
    }
}

class ImmutableEvaluationResultWrapper extends EvaluationResult
{
    private final EvaluationResult delegate_;

    // //////////////////////////////////////

    ImmutableEvaluationResultWrapper(EvaluationResult delegate)
    {
        delegate_ = delegate;
    }

    // //////////////////////////////////////

    public Object getValue()
    {
        return delegate_.getValue();
    }

    public float getFloat() throws DynamicTypeException
    {
        return delegate_.getFloat();
    }

    public boolean equals(Object object)
    {
        return delegate_.equals(object);
    }

    public int hashCode()
    {
        return delegate_.hashCode();
    }

    public String toString()
    {
        return delegate_.toString();
    }

    public String getString() throws DynamicTypeException
    {
        return delegate_.getString();
    }

    public boolean isString()
    {
        return delegate_.isString();
    }

    public boolean isLong()
    {
        return delegate_.isLong();
    }

    public boolean isFloat()
    {
        return delegate_.isFloat();
    }

    public boolean isDouble()
    {
        return delegate_.isDouble();
    }

    public boolean getBool() throws DynamicTypeException
    {
        return delegate_.getBool();
    }

    public Any getAny()
    {
        return delegate_.getAny();
    }

    public void setString(String s)
    {
        unsupported();
    }

    public void setFloat(float f)
    {
        unsupported();
    }

    public void setFloat(Double d)
    {
        unsupported();
    }

    public void setInt(int i)
    {
        unsupported();
    }

    public void setInt(Double i)
    {
        unsupported();
    }

    public void setBool(boolean b)
    {
        unsupported();
    }

    public void addAny(Any a)
    {
        unsupported();
    }

    private static void unsupported()
    {
        throw new UnsupportedOperationException();
    }
}

class DynamicTypeException extends EvaluationException
{
    private static final long serialVersionUID = 1L;

    public DynamicTypeException()
    {
        super();
    }

    public DynamicTypeException(String msg)
    {
        super(msg);
    }
}