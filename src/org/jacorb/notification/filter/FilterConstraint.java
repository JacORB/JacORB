package org.jacorb.notification.filter;

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

import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.StaticTypeChecker;
import org.jacorb.notification.filter.etcl.StaticTypeException;
import org.jacorb.notification.filter.etcl.TCLCleanUp;
import org.jacorb.notification.filter.etcl.TCLParser;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.util.Debug;

import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;

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
        if (logger_.isDebugEnabled()) {
            logger_.debug("Create new Constraint. Expression=" + constraintExp.constraint_expr);
        }

        try
        {
            constraint_ = constraintExp.constraint_expr;
            rootNode_ = TCLParser.parse( constraintExp.constraint_expr );

            if (rootNode_ != null) {
                TCLCleanUp _cleanUp = new TCLCleanUp();
                _cleanUp.fix( rootNode_ );

                StaticTypeChecker _checker = new StaticTypeChecker();
                _checker.check( rootNode_ );
            }

            return;
        }
        catch ( StaticTypeException e )
        {
            throw new InvalidConstraint( e.getMessage(), constraintExp );
        }
        catch ( ParseException e )
        {
            throw new InvalidConstraint( e.getMessage(), constraintExp );
        }
    }

    ////////////////////////////////////////

    public String getConstraint()
    {
        return constraint_;
    }

    public EvaluationResult evaluate( EvaluationContext evaluationContext,
                                      Message event )
        throws EvaluationException
    {
        if (rootNode_ == null) {
            return EvaluationResult.BOOL_TRUE;
        }

        if (logger_.isDebugEnabled() ) {
            logger_.debug("evaluate()" + rootNode_.toStringTree());
        }

        evaluationContext.setCurrentMessagea( event );

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
