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

package org.jacorb.notification.filter;

import org.omg.CORBA.Any;
import org.omg.CosNotification.Property;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface ETCLEvaluator
{
    ////////////////////////////////////////
    boolean hasDefaultDiscriminator(Any any) throws EvaluationException;

    Any evaluateExistIdentifier(Any value, String identifier) throws EvaluationException;

    /**
     * identify the unscoped IDL type name of a component. (e.g. mystruct._typeid == 'mystruct')
     * 
     * @param value
     *            the component
     * @return the IDL type name (string) wrapped in an any
     */
    Any evaluateTypeName(Any value) throws EvaluationException;

    /**
     * identify the RepositoryId of a component. (e.g. mystruct._repos_id ==
     * 'IDL:module/mystruct:1.0'
     * 
     * @param value
     *            the component
     * @return the IDL type name (string) wrapped in an any
     */
    Any evaluateRepositoryId(Any value) throws EvaluationException;

    /**
     * identify the number of elements of a component. if the parameter is a sequence or an array,
     * this method will return the number of elements in the list.
     * 
     * @param value
     *            the component
     * @return the number of elements in the list
     */
    Any evaluateListLength(Any value) throws EvaluationException;

    /**
     * extract the default member from Union wrapped inside the provided Any.
     */
    Any evaluateUnion(Any value) throws EvaluationException;

    Any evaluateUnion(Any value, int position) throws EvaluationException;

    Any evaluatePropertyList(Property[] list, String name);

    /**
     * extract a named value out of a sequence of name/value pairs.
     */
    Any evaluateNamedValueList(Any any, String name) throws EvaluationException;

    /**
     * extract the n-th position out of an Array wrapped inside an Any.
     */
    Any evaluateArrayIndex(Any any, int index) throws EvaluationException;

    Any evaluateIdentifier(Any any, int position) throws EvaluationException;

    Any evaluateDiscriminator(Any any) throws EvaluationException;

    EvaluationResult evaluateElementInSequence(EvaluationContext context, EvaluationResult element,
            Any sequence) throws EvaluationException;

    /**
     * expensive
     */
    Any evaluateIdentifier(Any any, String identifier) throws EvaluationException;
}