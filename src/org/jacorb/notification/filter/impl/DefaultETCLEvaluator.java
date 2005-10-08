package org.jacorb.notification.filter.impl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;
import org.omg.CosNotification.Property;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.DynamicAny.DynSequence;
import org.omg.DynamicAny.DynSequenceHelper;
import org.omg.DynamicAny.DynStruct;
import org.omg.DynamicAny.DynStructHelper;
import org.omg.DynamicAny.DynUnion;
import org.omg.DynamicAny.DynUnionHelper;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

/**
 * Provide the Basic operations needed to evaluate filter expressions on Anys.
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */

public class DefaultETCLEvaluator implements ETCLEvaluator
{
    private static final String NAME = "name";

    private static final String VALUE = "value";

    ////////////////////////////////////////

    private final Logger logger_;

    private final DynAnyFactory dynAnyFactory_;

    private static final ORB orb_ = ORB.init();

    private static final Any TRUE_ANY = orb_.create_any();

    private static final Any FALSE_ANY = orb_.create_any();

    static
    {
        TRUE_ANY.insert_boolean(true);

        FALSE_ANY.insert_boolean(false);
    }

    ////////////////////////////////////////

    public DefaultETCLEvaluator(Configuration config, DynAnyFactory dynAnyFactory)
    {
        logger_ = ((org.jacorb.config.Configuration) config).getNamedLogger(getClass().getName());

        dynAnyFactory_ = dynAnyFactory;
    }

    ////////////////////////////////////////

    public boolean hasDefaultDiscriminator(Any any) throws EvaluationException
    {
        try
        {
            return (any.type().default_index() != -1);
        } catch (BadKind e)
        {
            throw newEvaluationException(e);
        }
    }

    public Any evaluateExistIdentifier(Any value, String identifier) throws EvaluationException
    {
        try
        {
            evaluateIdentifier(value, identifier);

            return TRUE_ANY;
        } catch (EvaluationException e)
        {
            return FALSE_ANY;
        }
    }

    public Any evaluateTypeName(Any value) throws EvaluationException
    {
        try
        {
            TypeCode _tc = value.type();
            Any _ret = orb_.create_any();
            _ret.insert_string(_tc.name());

            return _ret;
        } catch (BadKind e)
        {
            throw newEvaluationException(e);
        }
    }

    public Any evaluateRepositoryId(Any value) throws EvaluationException
    {
        try
        {
            TypeCode _tc = value.type();
            Any _ret = orb_.create_any();
            _ret.insert_string(_tc.id());

            return _ret;
        } catch (BadKind e)
        {
            throw newEvaluationException(e);
        }
    }

    public Any evaluateListLength(Any value) throws EvaluationException
    {
        final int _length;

        switch (value.type().kind().value()) {
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

    private String getDefaultUnionMemberName(TypeCode unionTypeCode) throws EvaluationException
    {
        try
        {
            int _defaultIndex = unionTypeCode.default_index();

            if (_defaultIndex != -1)
            {
                return unionTypeCode.member_name(_defaultIndex);
            }
        } catch (BadKind e)
        {
            throw newEvaluationException(e);
        } catch (Bounds e)
        {
            throw newEvaluationException(e);
        }

        throw new EvaluationException();
    }

    private String getUnionMemberNameFromDiscriminator(TypeCode unionTypeCode, int discriminator)
            throws EvaluationException
    {
        try
        {
            Any _any = orb_.create_any();

            switch (unionTypeCode.discriminator_type().kind().value()) {

            case TCKind._tk_long:
                _any.insert_long(discriminator);
                break;

            case TCKind._tk_ulong:
                _any.insert_ulong(discriminator);
                break;

            case TCKind._tk_short:
                _any.insert_short((short) discriminator);
                break;

            case TCKind._tk_double:
                _any.insert_double(discriminator);
                break;

            case TCKind._tk_ushort:
                _any.insert_ushort((short) discriminator);
                break;
            }

            int _memberCount = unionTypeCode.member_count();

            try
            {
                for (int _x = 0; _x < _memberCount; _x++)
                {
                    if (_any.equal(unionTypeCode.member_label(_x)))
                    {
                        return unionTypeCode.member_name(_x);
                    }
                }
            } catch (Bounds b)
            {
                // this should never happen as _x should be always < _memberCount.
                throw new RuntimeException();
            }

        } catch (BadKind e)
        {
            throw newEvaluationException(e);
        }

        throw new EvaluationException();
    }

    public Any evaluateUnion(Any value) throws EvaluationException
    {
        String _defaultMemberName = getDefaultUnionMemberName(value.type());

        return evaluateIdentifier(value, _defaultMemberName);
    }

    public Any evaluateUnion(Any value, int position) throws EvaluationException
    {
        final DynUnion _dynUnion = toDynUnion(value);

        _dynUnion.seek(0);

        if (logger_.isDebugEnabled())
        {
            logger_.debug("extract idx: " + position + " from Union " + _dynUnion.type());
        }

        String _discrimName = getUnionMemberNameFromDiscriminator(value.type(), position);

        return evaluateIdentifier(_dynUnion, _discrimName);
    }

    public Any evaluatePropertyList(Property[] list, String name)
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("evaluatePropertyList " + list);
            logger_.debug("list length: " + list.length);
        }

        for (int x = 0; x < list.length; ++x)
        {
            if (logger_.isDebugEnabled())
            {
                logger_.debug(x + ": " + list[x].name + " => " + list[x].value);
            }

            if (name.equals(list[x].name))
            {
                return list[x].value;
            }
        }

        return null;
    }

    public Any evaluateNamedValueList(Any any, String name) throws EvaluationException
    {
        try
        {
            if (logger_.isDebugEnabled())
            {
                logger_.debug("evaluateNamedValueList(" + any + ", " + name + ")");
            }

            final DynAny _dynAny = toDynAny(any);
            final int _count = _dynAny.component_count();

            DynAny _cursor;
            Any _ret = null;

            _dynAny.rewind();

            if (logger_.isDebugEnabled())
            {
                logger_.debug("Entries: " + _count);
            }

            for (int _x = 0; _x < _count; _x++)
            {
                _dynAny.seek(_x);
                _cursor = _dynAny.current_component();
                _ret = evaluateNamedValue(_cursor, name);

                if (_ret != null)
                {
                    break;
                }
            }

            return _ret;
        } catch (TypeMismatch e)
        {
            throw newEvaluationException(e);
        }
    }

    private Any evaluateNamedValue(DynAny any, String name) throws EvaluationException
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("evaluate assoc " + name + " on a Any of type: " + any.type());
        }

        final Any _result;

        final String _anyName = evaluateIdentifier(any, NAME).extract_string();

        if (logger_.isDebugEnabled())
        {
            logger_.debug("test if " + name + " == " + _anyName);
        }

        if (name.equals(_anyName))
        {
            logger_.debug("YES");
            _result = evaluateIdentifier(any, VALUE);
        }
        else
        {
            _result = null;
        }

        return _result;
    }

    public Any evaluateArrayIndex(Any any, int index) throws EvaluationException
    {
        try
        {
            if (logger_.isDebugEnabled())
            {
                logger_.debug("evaluate array idx " + index + " on a Any of type: " + any.type());
            }

            DynAny _dynAny = toDynAny(any);
            DynAny _cursor;

            _dynAny.rewind();
            _dynAny.seek(index);
            _cursor = _dynAny.current_component();

            if (logger_.isDebugEnabled())
            {
                logger_.debug("evaluation result is of type: " + _cursor.type());
            }

            return _cursor.to_any();
        } catch (TypeMismatch e)
        {
            throw newEvaluationException(e);
        }
    }

    private Any evaluateIdentifier(DynAny any, int position) throws EvaluationException
    {
        try
        {
            final DynAny _result;

            switch (any.type().kind().value()) {

            case TCKind._tk_struct:
                any.seek(position);
                _result = any.current_component();
                break;

            default:
                throw new EvaluationException("attempt to access member on non-struct");
            }

            return _result.to_any();
        } catch (TypeMismatch e)
        {
            throw newEvaluationException(e);
        }
    }

    public Any evaluateIdentifier(Any any, int position) throws EvaluationException
    {
        if (logger_.isDebugEnabled())
        {
            logger_.debug("evaluate idx " + position + " on Any");
        }

        DynAny _dynAny = toDynAny(any);

        return evaluateIdentifier(_dynAny, position);
    }

    public Any evaluateDiscriminator(Any any) throws EvaluationException
    {
        switch (any.type().kind().value()) {
        case TCKind._tk_union:
            DynUnion _dynUnion = toDynUnion(any);
            return _dynUnion.get_discriminator().to_any();

        default:
            throw new EvaluationException("any does not contain member _d");
        }
    }

    public EvaluationResult evaluateElementInSequence(EvaluationContext context,
            EvaluationResult element, Any sequence) throws EvaluationException
    {
        try
        {
            final DynSequence _dynSequence = DynSequenceHelper.narrow(toDynAny(sequence));            

            _dynSequence.rewind();

            do
            {
                final DynAny _currentComponent = _dynSequence.current_component();

                final EvaluationResult _currentElement = EvaluationResult.fromAny(_currentComponent.to_any());

                if (element.compareTo(_currentElement) == 0)
                {
                    return EvaluationResult.BOOL_TRUE;
                }
            } while (_dynSequence.next());
                
            return EvaluationResult.BOOL_FALSE;
        } catch (TypeMismatch e)
        {
            throw newEvaluationException(e);
        }
    }

    /**
     * expensive
     */
    public Any evaluateIdentifier(Any any, String identifier) throws EvaluationException
    {
        // expensive call
        DynAny _dynAny = toDynAny(any);

        // expensive call
        return evaluateIdentifier(_dynAny, identifier);
    }

    private Any evaluateIdentifier(DynAny any, String identifier) throws EvaluationException
    {
        try
        {
            final String _strippedIdentifier = stripBackslash(identifier);

            if (logger_.isDebugEnabled())
            {
                logger_.debug("evaluate " + _strippedIdentifier + " on Any");
            }

            DynAny _cursor = any;

            SWITCH_LABEL:
            switch (any.type().kind().value()) {

            case TCKind._tk_struct:

                logger_.debug("Any is a struct");

                final DynStruct _dynStruct = DynStructHelper.narrow(any);
                String _currentName;

                _dynStruct.rewind();

                do
                {
                    _currentName = _dynStruct.current_member_name();

                    if (logger_.isDebugEnabled())
                    {
                        logger_.debug(" => " + _currentName);
                    }

                    if (_currentName.equals(_strippedIdentifier))
                    {
                        // expensive operation
                        _cursor = _dynStruct.current_component();
                        break SWITCH_LABEL;
                    }
                } while (_dynStruct.next());
                
                throw new EvaluationException("struct has no member " + _strippedIdentifier);

            case TCKind._tk_union:

                if (logger_.isDebugEnabled())
                {
                    logger_.debug("Any is a Union");
                }

                DynUnion _dynUnion = toDynUnion(any);

                if (_dynUnion.member_name().equals(_strippedIdentifier))
                {
                    _cursor = _dynUnion.member();
                }
                else
                {
                    if (logger_.isDebugEnabled())
                    {
                        logger_.debug(_dynUnion.member_name() + " != " + _strippedIdentifier);
                    }

                    throw new EvaluationException("member " + _strippedIdentifier
                            + " is not active on struct");
                }

                break;

            case TCKind._tk_any:
                logger_.debug("encapsulated any");

                return evaluateIdentifier(any.get_any(), _strippedIdentifier);

            default:
                logger_.debug("unknown " + any.type());

                return null;
            }

            if (logger_.isDebugEnabled())
            {
                logger_.debug("Result: " + _cursor);
            }

            if (_cursor != null && logger_.isDebugEnabled())
            {
                logger_.debug("evaluation result is of type: " + _cursor.type());
            }

            if (_cursor == null)
            {
                logger_.debug("Member not found");

                throw new EvaluationException("member not found");
            }

            return _cursor.to_any();
        } catch (InvalidValue e)
        {
            throw newEvaluationException(e);
        } catch (TypeMismatch e)
        {
            throw newEvaluationException(e);
        }
    }

    ////////////////////////////////////////

    private DynAny toDynAny(Any any) throws EvaluationException
    {
        try
        {
            return dynAnyFactory_.create_dyn_any(any);
        } catch (InconsistentTypeCode e)
        {
            throw newEvaluationException(e);
        }
    }

    private DynUnion toDynUnion(Any any) throws EvaluationException
    {
        return DynUnionHelper.narrow(toDynAny(any));
    }

    private DynUnion toDynUnion(DynAny dynAny)
    {
        return DynUnionHelper.narrow(dynAny);
    }

    private DynSequence toDynSequence(Any any) throws EvaluationException
    {
        return DynSequenceHelper.narrow(toDynAny(any));
    }

    private static String stripBackslash(String identifier)
    {
        StringBuffer _buffer = new StringBuffer();
        int _length = identifier.length();

        for (int _x = 0; _x < _length; _x++)
        {
            if (identifier.charAt(_x) != '\\')
            {
                _buffer.append(identifier.charAt(_x));
            }
        }

        return _buffer.toString();
    }

    private static EvaluationException newEvaluationException(Exception e)
    {
        return new EvaluationException(e.getMessage());
    }
}