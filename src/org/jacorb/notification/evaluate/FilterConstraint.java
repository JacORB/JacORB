package org.jacorb.notification.evaluate;

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

import org.jacorb.notification.EvaluationContext;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.node.AbstractTCLNode;
import org.jacorb.notification.node.DynamicTypeException;
import org.jacorb.notification.node.EvaluationResult;
import org.jacorb.notification.node.StaticTypeChecker;
import org.jacorb.notification.node.StaticTypeException;
import org.jacorb.notification.node.TCLCleanUp;
import org.jacorb.notification.parser.TCLParser;
import org.jacorb.util.Debug;

import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.apache.avalon.framework.logger.Logger;

/**
 * Representation of a Constraint.
 * A {@link org.jacorb.notification.FilterImpl FilterImpl} encapsulates
 * several Constraints. Each Constraint is represented by an instance
 * of this Class.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterConstraint
{
    private Logger logger_ = Debug.getNamedLogger( getClass().getName() );

    /**
     * String representation of the Constraint.
     */
    private String constraint_;

    /**
     * AST for the Constraint
     */
    private AbstractTCLNode rootNode_;

    ////////////////////////////////////////

    public FilterConstraint( AbstractTCLNode root )
    {
        rootNode_ = root;
    }

    public FilterConstraint( ConstraintExp constraintExp )
        throws InvalidConstraint
    {
        try
        {
            constraint_ = constraintExp.constraint_expr;
            rootNode_ = TCLParser.parse( constraintExp.constraint_expr );
            TCLCleanUp _cleanUp = new TCLCleanUp();
            _cleanUp.fix( rootNode_ );
            StaticTypeChecker _checker = new StaticTypeChecker();
            _checker.check( rootNode_ );

            return;
        }
        catch ( StaticTypeException ste )
        {
            throw new InvalidConstraint( ste.getMessage(), constraintExp );
        }
        catch ( TokenStreamException tse )
        {}
        catch ( RecognitionException re )
        {}

        throw new InvalidConstraint( constraintExp );
    }

    ////////////////////////////////////////

    public String getConstraint()
    {
        return constraint_;
    }

    public EvaluationResult evaluate( EvaluationContext evaluationContext,
                                      Message event )
        throws EvaluationException,
               DynamicTypeException
    {
        if (logger_.isDebugEnabled() ) {
            logger_.debug("evaluate()" + rootNode_.toStringTree());
        }

        evaluationContext.setEvent( event );

        EvaluationResult _res = rootNode_.evaluate( evaluationContext );

        return _res;
    }

    public String toString()
    {
        StringBuffer _b = new StringBuffer("<FilterConstraint: ");

        rootNode_.printToStringBuffer(_b);
        _b.append(" >");

        return _b.toString();
    }
}
