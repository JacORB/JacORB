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
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.jacorb.notification.evaluate.EvaluationException;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.EvaluationContext;

/**
 * ImplictOperatorNode.java
 *
 *
 * Created: Sat Sep 28 23:58:11 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class ImplicitOperatorNode extends TCLNode {
    
    static final String DISCRIM = "_d";
    static final String LENGTH = "_length";
    static final String REPO_ID = "_repos_id";
    static final String TYPE_ID = "_type_id";

    public final static ImplicitOperator OPERATOR_DISCRIM = 
	new DiscrimOperator();
    
    public final static ImplicitOperator OPERATOR_LENGTH = 
	new LengthOperator();

    public final static ImplicitOperator OPERATOR_REPO_ID = 
	new RepoOperator();

    public final static ImplicitOperator OPERATOR_TYPE_ID = 
	new TypeOperator();

    static final EvaluationException EVALUATION_EXCEPTION = 
	new EvaluationException();

    ImplicitOperator operator_;
    String operatorName_;
    
    public ImplicitOperatorNode(Token token) {
	super(token);
	String _tokenText = token.getText();

	if (DISCRIM.equals(_tokenText)) {
	    operator_ = OPERATOR_DISCRIM;
	    operatorName_ = DISCRIM;
	    setName("ImplicitOperator - _d");
	} else if (LENGTH.equals(_tokenText)) {
	    operator_ = OPERATOR_LENGTH;
	    operatorName_ = LENGTH;
	    setName("ImplicitOperator - _length");
	} else if (REPO_ID.equals(_tokenText)) {
	    operator_ = OPERATOR_REPO_ID;
	    operatorName_ = REPO_ID;
	    setName("Implicit - _repos_id");
	} else if (TYPE_ID.equals(_tokenText)) {
	    operator_ = OPERATOR_TYPE_ID;
	    operatorName_ = TYPE_ID;
	    setName("Implicit - _type_id");
	} else {
	    throw new RuntimeException();
	}
    }

    public ImplicitOperator getOperator() {
	return operator_;
    }

    public EvaluationResult evaluate(EvaluationContext context)
	throws InconsistentTypeCode,
	       TypeMismatch,
	       EvaluationException,
	       InvalidValue,
	       DynamicTypeException {

	return null;
    }

    public void acceptInOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitImplicit(this);
    }
    public void acceptPreOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitImplicit(this);
    }
    public void acceptPostOrder(TCLVisitor visitor) throws VisitorException {
	visitor.visitImplicit(this);
    }

    public String toString() {
	return operator_.toString();
    }

}// ImplictOperatorNode

class RepoOperator implements ImplicitOperator {
    public String toString() {
	return "_repos_id";
    }
}

class TypeOperator implements ImplicitOperator {
    public String toString() {
	return "_type_id";
    }
}

class DiscrimOperator implements ImplicitOperator {
    public String toString() {
	return "_d";
    }
}

class LengthOperator implements ImplicitOperator {
    public String toString() {
	return "_length";
    }
}
