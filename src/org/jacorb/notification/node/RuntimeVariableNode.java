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

import antlr.Token;
import org.jacorb.notification.parser.TCLParser;
import antlr.TokenStreamException;
import antlr.RecognitionException;
import org.omg.CORBA.ORB;
import org.jacorb.notification.EvaluationContext;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

/**
 * RuntimeVariableNode.java
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class RuntimeVariableNode extends TCLNode
{
    static ORB sOrb_;

    static {
	sOrb_ = ORB.init(new String[] {}, null);
    }

    String value_;    

    TCLNode strategy_;

    public RuntimeVariableNode( Token token )
    {
	super(token);

	value_ = token.getText();

	strategy_ = newStrategy(value_);
    }

    public EvaluationResult evaluate(EvaluationContext context) throws InconsistentTypeCode,
								       TypeMismatch,
								       EvaluationException,
								       InvalidValue,
								       DynamicTypeException {

	return strategy_.evaluate(context);
    }

    private TCLNode newStrategy(String variable) {
	if (DomainNameShorthandNode.SHORT_NAME.equals(variable)) {
	    return new DomainNameShorthandNode();
	} else if (TypeNameShorthandNode.SHORT_NAME.equals(variable)) {
	    return new TypeNameShorthandNode();
	} else if (EventNameShorthandNode.SHORT_NAME.equals(variable)) {
	    return new EventNameShorthandNode();
	} else if (CurrentTimeNode.SHORT_NAME.equals(variable)) {
	    return new CurrentTimeNode(sOrb_);
	} else {
	    return new PropertyShorthandNode(variable);
	}
    }

    public void acceptPostOrder( TCLVisitor visitor ) throws VisitorException
    {
	if (getFirstChild() != null) {
	    ( ( TCLNode ) getFirstChild() ).acceptPostOrder( visitor );
	}
	visitor.visitRuntimeVariable( this );
    }

    public void acceptPreOrder( TCLVisitor visitor ) throws VisitorException
    {
	visitor.visitRuntimeVariable( this );

	if (getFirstChild() != null) {
	    ( ( TCLNode ) getFirstChild() ).acceptPreOrder( visitor );
	}
    }

    public void acceptInOrder( TCLVisitor visitor ) throws VisitorException
    {
	if (getFirstChild() != null) {
	    ( ( TCLNode ) getFirstChild() ).acceptInOrder( visitor );
	}

	visitor.visitRuntimeVariable(this);
    }
    
    public String toString() {
	return value_;
    }
    
    public String getIdentifier() {
	return value_;
    }

}
