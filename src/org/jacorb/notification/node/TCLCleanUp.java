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

import antlr.collections.AST;

/**
 * TCLCleanUp.java
 *
 *
 * Created: Wed Sep 18 02:07:17 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class TCLCleanUp extends TCLVisitor {

    static boolean DEBUG = false;

    public void fix(TCLNode node) {
	try {
	    node.acceptPreOrder(this);
	} catch (VisitorException ve) {
	}
    }

    void fixCompPos(TCLNode node) {
	AST _fixit = null;

	if ((node.getFirstChild() != null) && (node.getFirstChild().getType() == TCLTokenTypes.COMP_POS)) {
	    debug("left child needs repair");
	    _fixit = node.getFirstChild();
	    
	    DotOperator _dot = new DotOperator();
	    _dot.setType(TCLTokenTypes.DOT);
	    _dot.setNextSibling(_fixit);
	    
	    node.setFirstChild(_dot);
	} else if ((node.getNextSibling() != null) && (node.getNextSibling().getType() == TCLTokenTypes.COMP_POS)) {
	    debug("right child needs repair");
	    _fixit = node.getNextSibling();

	    DotOperator _dot = new DotOperator();
	    _dot.setType(TCLTokenTypes.DOT);
	    _dot.setNextSibling(_fixit);

	    node.setNextSibling(_dot);
	}
    }

    public void visitComponent(ComponentOperator component) throws VisitorException {
	debug("visit component");
	fixCompPos(component);
    }

    /**
     * Describe <code>visitComponentPosition</code> method here.
     *
     * @param componentPositionOperator a <code>ComponentPositionOperator</code> value
     * @exception VisitorException if an error occurs
     */
    public void visitComponentPosition(ComponentPositionOperator componentPositionOperator) throws VisitorException {
	debug("visit compPos");
	fixCompPos(componentPositionOperator);
    }


    /**
     * Describe <code>visitDot</code> method here.
     *
     * @param dotOperator a <code>DotOperator</code> value
     * @exception VisitorException if an error occurs
     */
    public void visitDot(DotOperator dotOperator) throws VisitorException {
	debug("visit dot");
    }

    void fixUnionPosition(UnionPositionOperator node) {
	debug("repair");
	
	AST _nextSibling = node.getNextSibling();

	if (_nextSibling == null) {
	    node.setDefault();
	} else {
	    switch (_nextSibling.getType()) {
	    case TCLTokenTypes.NUMBER:
		Double _position = ((NumberValue)_nextSibling).getNumber();
		node.setPosition(_position);
		node.setNextSibling(_nextSibling.getNextSibling());
	    case TCLTokenTypes.PLUS:
	    case TCLTokenTypes.MINUS:
	    case TCLTokenTypes.STRING:
		break;
	    default:
		node.setDefault();
		break;
	    }
	}
    }

    public void visitUnionPosition(UnionPositionOperator op) throws VisitorException {
	fixUnionPosition(op);
	fixCompPos(op);
    }

    void debug(String msg) {
	if (DEBUG) {
	    System.err.println("[TCLCleanUp] " + msg);
	}
    }

}// TCLCleanUp
