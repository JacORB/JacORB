package org.jacorb.notification.filter.etcl;

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

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.jacorb.notification.filter.FilterConstraint;
import org.jacorb.notification.filter.ParseException;
import org.jacorb.notification.interfaces.Message;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;

/**
 * Representation of a ETCL Filter Constraint.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ETCLFilterConstraint implements FilterConstraint
{
    /**
     * AST for the Constraint
     */
    private final AbstractTCLNode rootNode_;

    ////////////////////////////////////////

    public ETCLFilterConstraint( AbstractTCLNode root )
    {
        rootNode_ = root;
    }


    public ETCLFilterConstraint( ConstraintExp constraintExp )
        throws InvalidConstraint
    {
        try
        {
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
            throw new InvalidConstraint( e.toString(), constraintExp );
        }
        catch ( ParseException e )
        {
            throw new InvalidConstraint( e.toString(), constraintExp );
        }
    }

    ////////////////////////////////////////

    public EvaluationResult evaluate( EvaluationContext evaluationContext,
                                      Message event )
        throws EvaluationException
    {
        if (rootNode_ == null) {
            return EvaluationResult.BOOL_TRUE;
        }

        evaluationContext.setCurrentMessage( event );

        EvaluationResult _res = rootNode_.evaluate( evaluationContext );

        return _res;
    }


    public String toString()
    {
        StringBuffer _buffer = new StringBuffer("<FilterConstraint: ");

        rootNode_.printToStringBuffer(_buffer);
        _buffer.append(" >");

        return _buffer.toString();
    }
}
