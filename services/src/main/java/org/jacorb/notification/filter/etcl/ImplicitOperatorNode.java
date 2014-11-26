package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;
import org.omg.CORBA.Any;

import antlr.Token;

/**
 * @author Alphonse Bendt
 */

public class ImplicitOperatorNode extends AbstractTCLNode
{
    static final String TOKEN_DISCRIM = "_d";

    static final String TOKEN_LENGTH = "_length";

    static final String TOKEN_REPOS_ID = "_repos_id";

    static final String TOKEN_TYPE_ID = "_type_id";

    public final static ImplicitOperator OPERATOR_DISCRIM = new DiscrimOperator();

    public final static ImplicitOperator OPERATOR_LENGTH = new LengthOperator();

    public final static ImplicitOperator OPERATOR_REPO_ID = new RepoOperator();

    public final static ImplicitOperator OPERATOR_TYPE_ID = new TypeOperator();

    private final ImplicitOperator operator_;

    public ImplicitOperatorNode(Token token)
    {
        super(token);
        String _tokenText = token.getText();

        if (TOKEN_DISCRIM.equals(_tokenText))
        {
            operator_ = OPERATOR_DISCRIM;
            // operatorName_ = DISCRIM;
            setName("ImplicitOperator - _d");
        }
        else if (TOKEN_LENGTH.equals(_tokenText))
        {
            operator_ = OPERATOR_LENGTH;
            // operatorName_ = LENGTH;
            setName("ImplicitOperator - _length");
        }
        else if (TOKEN_REPOS_ID.equals(_tokenText))
        {
            operator_ = OPERATOR_REPO_ID;
            // operatorName_ = REPO_ID;
            setName("Implicit - _repos_id");
        }
        else if (TOKEN_TYPE_ID.equals(_tokenText))
        {
            operator_ = OPERATOR_TYPE_ID;
            // operatorName_ = TYPE_ID;
            setName("Implicit - _type_id");
        }
        else
        {
            throw new RuntimeException();
        }
    }

    public ImplicitOperator getOperator()
    {
        return operator_;
    }

    public EvaluationResult evaluate(EvaluationContext context) throws EvaluationException
    {
        return null;
    }

    public void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitImplicit(this);
    }

    public void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitImplicit(this);
    }

    public void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException
    {
        visitor.visitImplicit(this);
    }

    public String toString()
    {
        return operator_.toString();
    }
}

class RepoOperator implements ImplicitOperator
{
    public String toString()
    {
        return ImplicitOperatorNode.TOKEN_REPOS_ID;
    }

    public Any evaluateImplicit(ETCLEvaluator evaluator, Any value) throws EvaluationException
    {
        return evaluator.evaluateRepositoryId(value);
    }
}

class TypeOperator implements ImplicitOperator
{
    public String toString()
    {
        return ImplicitOperatorNode.TOKEN_TYPE_ID;
    }

    public Any evaluateImplicit(ETCLEvaluator evaluator, Any value) throws EvaluationException
    {
        return evaluator.evaluateTypeName(value);
    }
}

class DiscrimOperator implements ImplicitOperator
{
    public String toString()
    {
        return ImplicitOperatorNode.TOKEN_DISCRIM;
    }

    public Any evaluateImplicit(ETCLEvaluator evaluator, Any value) throws EvaluationException
    {
        return evaluator.evaluateDiscriminator(value);
    }
}

class LengthOperator implements ImplicitOperator
{
    public String toString()
    {
        return ImplicitOperatorNode.TOKEN_LENGTH;
    }

    public Any evaluateImplicit(ETCLEvaluator evaluator, Any value) throws EvaluationException
    {
        return evaluator.evaluateListLength(value);
    }
}
