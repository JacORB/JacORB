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
package org.jacorb.notification.node;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

import org.omg.CORBA.TCKind;
import org.jacorb.notification.evaluate.EvaluationContext;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.jacorb.notification.evaluate.EvaluationException;
import org.apache.log4j.Logger;

public abstract class TCLNode extends BaseAST implements TCLTokenTypes {

    static boolean DEBUG = true;

    private int astNodeType_;
    private TCKind tcKind_;
    String name_;

    protected Logger logger_ = Logger.getLogger("TCLTree");

    ////////////////////////////////////////////////////////////
    // Constructor

    public TCLNode(Token tok) {
	super();
	setType(tok.getType());
    }

    protected TCLNode() {
	super();
    }

    ////////////////////////////////////////////////////////////
    // these should be abstract

    /**
     * Evaluate this Node.
     *
     * @param context an <code>EvaluationContext</code> value contains
     * all context information necessary for the evaluation
     * @return an <code>EvaluationResult</code> value
     * @exception DynamicTypeException if an dynamic type error occurs during the evaluation 
     *                                 e.g. the attempt to add a string and a number
     * @exception InconsistentTypeCode if an error occurs
     * @exception InvalidValue if an error occurs
     * @exception TypeMismatch if an error occurs
     * @exception EvaluationException these errors mostly occur if e.g. an expression contains a reference 
     *                                to a not-existent struct member.
     */
    public EvaluationResult evaluate(EvaluationContext context)
	throws DynamicTypeException,
	       InconsistentTypeCode,
	       InvalidValue,
	       TypeMismatch,
	       EvaluationException {
	return null;
    }

    /** 
     * accept a visitor for traversal Inorder
     * 
     * @param visitor 
     */
    abstract public void acceptInOrder(TCLVisitor visitor) throws VisitorException;

    /** 
     * accept a visitor for traversal in Preorder. the root node is
     * visited before the left and the right subtrees are visited.
     * 
     * @param visitor 
     */
    abstract public void acceptPreOrder(TCLVisitor visitor) throws VisitorException;

    /** 
     * accept a visitor for traversal in Postorder. the right and left
     * subtrees are visited before the root node is visited.
     * 
     * @param visitor 
     */
    abstract public void acceptPostOrder(TCLVisitor visitor) throws VisitorException;

    ////////////////////////////////////////////////////////////

    public String getName() {
	return name_;
    }

    void setName(String name) {
	name_ = name;
    }

    /** 
     * Check wether this node has a Sibling.
     *
     * @return true, if this node has a Sibling
     */
    public boolean hasNextSibling() {
	return (getNextSibling() != null);
    }

    /** 
     * get the AST Token Type of this nodes sibling
     * 
     * @return a AST Token Type
     */
    public int getNextType() {
	TCLNode _next = (TCLNode)getNextSibling();

	return _next.getType();
    }

    protected void setKind(TCKind kind) {
	tcKind_ = kind;
    }
    
    /**
     * Return the Runtimetype of this node.
     * If the Runtime type cannot be guessed statically this Method returns null.
     *
     * @return a <code>TCKind</code> value or null if the Runtimetype cannot be determined
     * statically.
     */
    public TCKind getKind() {
	return tcKind_;
    }

    /** 
     * create a visualization of this node and all its children.
     * 
     * @return a String representation
     */
    public String toStringTree() {
	StringBuffer _buffer = new StringBuffer();

	if (getFirstChild()!=null) {
	    _buffer.append(" (");
	}
	_buffer.append(" " + toString());
	if (getFirstChild()!=null) {
	    _buffer.append(((TCLNode)getFirstChild()).toStringList());
	}
	if (getFirstChild() != null) {
	    _buffer.append(" )");
	}
	return _buffer.toString();
    }

    protected void debug(String msg) {
	logger_.debug("[" + getName() + "] " + msg);
// 	if (DEBUG) {
// 	    System.err.println("[" + getName() + "] " + msg);
// 	}
    }

    /** 
     * Access the left child. This method returns null if this node
     * has no left child
     * 
     * @return the left Child or null.
     */
    public TCLNode left() {
	return (TCLNode)getFirstChild();
    }

    /** 
     * Access the right child. This method returns null if this node
     * has no right child
     * 
     * @return the right Child or null.
     */
    public TCLNode right() {
	return (TCLNode)getFirstChild().getNextSibling();
    }

    ////////////////////////////////////////////////////////////

    public boolean isStatic() {
	return false;
    }	    

    public boolean isNumber() {return false;}

    public boolean isString() {return false;}

    public boolean isBoolean() {return false;}

    /** 
     * Get the AST Token Type for this node.
     * 
     * @return the AST Token Type value
     * @see TCLTokenTypes
     */ 
    public int getType() {
	return astNodeType_;
    }

    /** 
     * Set AST Token Type for this node.
     * 
     * @param type must be a valid TCLTokenType.
     * @see TCLTokenTypes
     */
    public void setType(int type) {
	astNodeType_ = type;
    }

    /**
     * satisfy abstract methode from BaseAST. Not used.
     */
    public void initialize(int t, String txt) {
    }
    /**
     * satisfy abstract methode from BaseAST. Not used.
     */
    public void initialize(AST t) {
    }
    /**
     * satisfy abstract methode from BaseAST. Not used.
     */
    public void initialize(Token tok) {
    }
}
