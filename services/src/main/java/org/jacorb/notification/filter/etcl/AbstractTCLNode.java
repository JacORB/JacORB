package org.jacorb.notification.filter.etcl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

import java.lang.reflect.Field;

import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.filter.EvaluationException;
import org.jacorb.notification.filter.EvaluationResult;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * Base Class for TCLTree Nodes.
 * 
 * @author Alphonse Bendt
 */

public abstract class AbstractTCLNode extends BaseAST implements TCLParserTokenTypes
{
    private int astNodeType_;

    //  private TCKind tcKind_;

    private String name_;

    ////////////////////////////////////////////////////////////
    // Constructor

    public AbstractTCLNode(Token tok)
    {
        super();

        setType(tok.getType());
    }

    protected AbstractTCLNode()
    {
        super();
    }

    //////////////////////////////////////////////////

    /**
     * Evaluate this Node.
     * 
     * @param context
     *            an <code>EvaluationContext</code> value contains all context information
     *            necessary for the evaluation
     * @return an <code>EvaluationResult</code> value
     * 
     * @exception EvaluationException
     *                occurs if e.g. an expression contains a reference to a non-existent struct
     *                member or if it is tried to add a string and a number
     */
    public EvaluationResult evaluate(EvaluationContext context) throws EvaluationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * accept a visitor for traversal Inorder
     * 
     * @param visitor
     */
    public abstract void acceptInOrder(AbstractTCLVisitor visitor) throws VisitorException;

    /**
     * accept a visitor for traversal in Preorder.
     * 
     * the root node is visited before the left and the right subtrees are visited.
     * 
     * @param visitor
     */
    public abstract void acceptPreOrder(AbstractTCLVisitor visitor) throws VisitorException;

    /**
     * accept a visitor for traversal in Postorder.
     * 
     * the right and left subtrees are visited before the root node is visited.
     * 
     * @param visitor
     */
    public abstract void acceptPostOrder(AbstractTCLVisitor visitor) throws VisitorException;

    ////////////////////////////////////////////////////////////

    public final String getName()
    {
        return name_;
    }

    void setName(String name)
    {
        name_ = name;
    }

    /**
     * Check wether this node has a Sibling.
     * 
     * @return true, if this node has a Sibling
     */
    public boolean hasNextSibling()
    {
        return (getNextSibling() != null);
    }

    public void printToStringBuffer(StringBuffer buffer)
    {
        if (getFirstChild() != null)
        {
            buffer.append(" (");
        }

        buffer.append(" ");
        buffer.append(toString());

        if (getFirstChild() != null)
        {
            buffer.append(((AbstractTCLNode) getFirstChild()).toStringList());
        }

        if (getFirstChild() != null)
        {
            buffer.append(" )");
        }
    }

    /**
     * create a visualization of this node and all its children.
     * 
     * @return a String representation of this Node and all its children
     */
    public String toStringTree()
    {
        StringBuffer _buffer = new StringBuffer();

        printToStringBuffer(_buffer);

        return _buffer.toString();
    }

    /**
     * Access the left child. This method returns null if this node has no left child
     * 
     * @return the left Child or null.
     */
    public AbstractTCLNode left()
    {
        return (AbstractTCLNode) getFirstChild();
    }

    /**
     * Access the right child. This method returns null if this node has no right child
     * 
     * @return the right Child or null.
     */
    public AbstractTCLNode right()
    {
        return (AbstractTCLNode) getFirstChild().getNextSibling();
    }

    ////////////////////////////////////////////////////////////

    public boolean isStatic()
    {
        return false;
    }

    public boolean isNumber()
    {
        return false;
    }

    public boolean isString()
    {
        return false;
    }

    public boolean isBoolean()
    {
        return false;
    }

    /**
     * Get the AST Token Type for this node.
     * 
     * @return the AST Token Type value
     * @see TCLParserTokenTypes
     */
    public int getType()
    {
        return astNodeType_;
    }

    /**
     * Set AST Token Type for this node.
     * 
     * @param type
     *            must be a valid TCLTokenType.
     * @see TCLParserTokenTypes
     */
    public void setType(int type)
    {
        astNodeType_ = type;
    }

    /**
     * converts an int tree token type to a name. Does this by reflecting on
     * nsdidl.IDLTreeTokenTypes, and is dependent on how ANTLR 2.00 outputs that class. this snippet
     * was taken from http://www.codetransform.com/
     */
    public static String getNameForType(int t)
    {
        try
        {
            Field[] _fields = TCLParserTokenTypes.class.getDeclaredFields();

            if (t - 6 < _fields.length)
            {
                return _fields[t - 6].getName();
            }
        } catch (Exception e)
        {
            // ignore
        }

        return "unknown type: " + t;
    }

    /**
     * satisfy abstract method from BaseAST. Not used.
     */
    public final void initialize(int t, String txt)
    {
        // no op
    }

    /**
     * satisfy abstract method from BaseAST. Not used.
     */
    public final void initialize(AST t)
    {
        // no op
    }

    /**
     * satisfy abstract method from BaseAST. Not used.
     */
    public final void initialize(Token tok)
    {
        // no op
    }
}