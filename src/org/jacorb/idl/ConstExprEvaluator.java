package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 */


import java.util.HashMap;
import java.lang.Double;

/**
 * Based on the MathEvaluator class by The-Son LAI,
 * <a href="mailto:Lts@writeme.com">Lts@writeme.com</a>
 *
 * <i>Expression evaluator for IDL constant expression</i>
 *
 * Supports the following functions:
 * +, -, *, /, ^, %, ^, | , <<, >> <br>
 *
 * When the getValue() is called, a Double object is returned.
 * If it returns null, an error occured.<p>

 * @version $Id$
 * @author  Gerald Brose,
 *          The-Son LAI <a href="mailto:Lts@writeme.com">Lts@writeme.com</a>
 */

public class ConstExprEvaluator
{
    protected static   Operator[]   operators = null;
    private   Node    node                    = null;
    private   String  expression              = null;
    private   HashMap variables               = new HashMap();

    /**
     * creates an empty ConstExprEvaluator.
     * You need to use setExpression(String s) to assign an expression string to it.
     */

    public ConstExprEvaluator()
    {
        init();
    }

    /**
     * creates a ConstExprEvaluator and assign the expression string.
     */

    public ConstExprEvaluator(String s)
    {
        init();
        setExpression(s);
    }

    private void init()
    {
        if ( operators == null )
            initializeOperators();
    }

    /**
     * sets the expression
     */

    public void setExpression(String s)
    {
        expression = s;
    }

    /**
     * resets the evaluator
     */

    public void reset()
    {
        node   = null;
        expression   = null;
        variables   = new HashMap();
    }


    /**
     * evaluates and returns the value of the expression
     */

    public Double getValue()
    {
        if (expression == null)
        {
            return null;
        }

        try
        {
            node = new Node(expression);
            return evaluate(node);
        }
        catch (Exception e)
        {
            lexer.emit_error("unexpected exception: " + e.getMessage());
            // TODO throw exception?
            return null;
        }
    }

    private static Double evaluate(Node n)
    {
        if ( n.hasOperator() && n.hasChild() )
        {
            if ( n.getOperator().getType() == 1 )
                n.setValue ( evaluateExpression( n.getOperator(), evaluate( n.getLeft() ), null ) );
            else if ( n.getOperator().getType() == 2 )
                n.setValue( evaluateExpression( n.getOperator(), evaluate( n.getLeft() ), evaluate( n.getRight() ) ) );
        }
        return n.getValue();
    }


    private static Double evaluateExpression( Operator o, Double f1, Double f2)
    {
        String op   = o.getOperator();
        Double res   = null;

        if     ( "+".equals(op) )
            res = new Double( f1.doubleValue() + f2.doubleValue() );
        else if  ( "-".equals(op) )
            res = new Double( f1.doubleValue() - f2.doubleValue() );
        else if  ( "*".equals(op) )
            res = new Double( f1.doubleValue() * f2.doubleValue() );
        else if  ( "/".equals(op) )
            res = new Double( f1.doubleValue() / f2.doubleValue() );
        else if  ( "%".equals(op) )
            res = new Double( f1.doubleValue() % f2.doubleValue() );
        else if  ( "|".equals(op) )
            res =   new Double(Double.longBitsToDouble( Double.doubleToLongBits( f1.doubleValue()) | Double.doubleToLongBits( f2.doubleValue() ) ));
        else if  ( "&".equals(op) )
            res =  new Double( Double.longBitsToDouble( Double.doubleToLongBits( f1.doubleValue() ) & Double.doubleToLongBits( f2.doubleValue() ) ));
        else if  ( "^".equals(op) )
            res =  new Double( Double.longBitsToDouble( Double.doubleToLongBits( f1.doubleValue() ) ^ Double.doubleToLongBits( f2.doubleValue() ) ));
        else if  ( "<<".equals(op) )
            res =  new Double( Double.longBitsToDouble( Double.doubleToLongBits( f1.doubleValue() ) << Double.doubleToLongBits( f2.doubleValue() ) ));
        else if  ( ">>".equals(op) )
            res =  new Double( Double.longBitsToDouble( Double.doubleToLongBits( f1.doubleValue() ) >> Double.doubleToLongBits( f2.doubleValue() ) ));

        return res;
    }

    private void initializeOperators()
    {
        operators     = new Operator[10];

        // bit-wise operators
        operators[0]  = new Operator("|"  , 2, 0);

        operators[1]  = new Operator("^"  , 2, 2);

        operators[2]  = new Operator("&"  , 2, 4);

        operators[3]  = new Operator(">>"  , 2, 6);
        operators[4]  = new Operator("<<"  , 2, 6);
        // arithmetic operators
        operators[5]  = new Operator("+"  , 2, 8);
        operators[6]  = new Operator("-"  , 2, 8);

        operators[7]  = new Operator("*"  , 2, 10);
        operators[8]  = new Operator("/"  , 2, 10);
        operators[9]  = new Operator("%"  , 2, 10);
    }

    /**
     * gets the variable's value that was assigned previously
     */

    public Double getVariable(String s)
    {
        return( Double )variables.get(s);
    }

    private Double getDouble(String s)
    {
        if ( s == null )
            return null;

        Double res = null;
        try
        {
            res = new Double(Double.parseDouble(s));
        }
        catch(Exception e)
        {
            return getVariable(s);
        }

        return res;
    }

    protected Operator[] getOperators()
    {
        return operators;
    }

    protected class Operator
    {
        private String op;
        private int type;
        private int priority;

        public Operator(String o, int t, int p)
        {
            op = o;
            type = t;
            priority = p;
        }

        public String getOperator()
        {
            return op;
        }

        public void setOperator(String o)
        {
            op = o;
        }

        public int getType()
        {
            return type;
        }

        public int getPriority()
        {
            return priority;
        }
    }

    protected class Node
    {
        public String   nString  = null;
        public Operator nOperator   = null;
        public Node   nLeft  = null;
        public Node   nRight  = null;
        public Node   nParent  = null;
        public int  nLevel  = 0;
        public Double    nValue  = null;

        public Node(String s) throws Exception
        {
            init(null, s, 0);
        }

        public Node(Node parent, String s, int level) throws Exception
        {
            init(parent, s, level);
        }

        private void init(Node parent, String s, int level) throws Exception
        {
            s = removeIllegalCharacters(s);
            s = removeBrackets(s);
            s = addZero(s);
            if ( checkBrackets(s) != 0 )
                throw new Exception("Wrong number of brackets in [" + s + "]");

            nParent          = parent;
            nString     = s;
            nValue    = getDouble(s);
            nLevel     = level;
            int sLength    = s.length();
            int inBrackets  = 0;
            int startOperator   = 0;

            for (int i=0; i<sLength; i++)
            {
                if ( s.charAt(i) == '(' )
                    inBrackets++;
                else if ( s.charAt(i) == ')' )
                    inBrackets--;
                else
                {
                    // the expression must be at "root" level
                    if ( inBrackets == 0 )
                    {
                        Operator o = getOperator(nString,i);
                        if ( o != null )
                        {
                            // if first operator or lower priority operator
                            if ( nOperator == null || nOperator.getPriority() >= o.getPriority() )
                            {
                                nOperator   = o;
                                startOperator   = i;
                            }
                        }
                    }
                }
            }

            if ( nOperator != null )
            {
                // one operand, should always be at the beginning
                if ( startOperator==0 && nOperator.getType() == 1 )
                {
                    // the brackets must be ok
                    if ( checkBrackets( s.substring( nOperator.getOperator().length() ) ) == 0 )
                    {
                        nLeft  =
                        new Node( this, s.substring( nOperator.getOperator().length() ) , nLevel + 1);
                        nRight = null;
                        return;
                    }

                    throw new Exception("Error during parsing... missing brackets in [" + s + "]");
                }
                // two operands
                else if ( startOperator > 0 && nOperator.getType() == 2 )
                {
                    nLeft   =
                    new Node( this, s.substring(0,  startOperator), nLevel + 1 );
                    nRight   =
                    new Node( this, s.substring(startOperator + nOperator.getOperator().length()), nLevel + 1);
                }
            }
        }

        private Operator getOperator(String s, int start)
        {
            Operator[] operators = getOperators();
            String temp = s.substring(start);
            temp = getNextWord(temp);
            for (int i=0; i<operators.length; i++)
            {
                if ( temp.startsWith(operators[i].getOperator()) )
                    return operators[i];
            }
            return null;
        }

        private String getNextWord(String s)
        {
            int sLength = s.length();
            for (int i=1; i<sLength; i++)
            {
                char c = s.charAt(i);
                if ( (c > 'z' || c < 'a') && (c > '9' || c < '0') )
                    return s.substring(0, i);
            }
            return s;
        }

        /**
         * checks if there is any missing brackets
         * @return true if s is valid
         */
        protected int checkBrackets(String s)
        {
            int sLength    = s.length();
            int inBracket   = 0;

            for (int i=0; i<sLength; i++)
            {
                if  ( s.charAt(i) == '(' && inBracket >= 0 )
                    inBracket++;
                else if ( s.charAt(i) == ')' )
                    inBracket--;
            }

            return inBracket;
        }

        /**
         * returns a string that doesnt start with a + or a -
         */
        protected String addZero(String s)
        {
            if ( s.startsWith("+") || s.startsWith("-") )
            {
                int sLength    = s.length();
                for (int i=0; i<sLength; i++)
                {
                    if ( getOperator(s, i) != null )
                        return "0" + s;
                }
            }

            return s;
        }


        protected boolean hasChild()
        {
            return ( nLeft != null || nRight != null );
        }

        protected boolean hasOperator()
        {
            return ( nOperator != null );
        }

        protected boolean hasLeft()
        {
            return ( nLeft != null );
        }

        protected Node getLeft()
        {
            return nLeft;
        }

        protected boolean hasRight()
        {
            return ( nRight != null );
        }

        protected Node getRight()
        {
            return nRight;
        }

        protected Operator getOperator()
        {
            return nOperator;
        }

        protected int getLevel()
        {
            return nLevel;
        }

        protected Double getValue()
        {
            return nValue;
        }

        protected void setValue(Double f)
        {
            nValue = f;
        }

        protected String getString()
        {
            return nString;
        }

        /**
         * Removes spaces, tabs and brackets at the begining
         */

        public String removeBrackets(String s)
        {
            String res = s;
            if ( s.length() > 2 && res.startsWith("(") && res.endsWith(")") && checkBrackets(s.substring(1,s.length()-1)) == 0 )
            {
                res = res.substring(1, res.length()-1 );
            }
            if ( res != s )
                return removeBrackets(res);
            else
                return res;
        }

        /**
         * Removes illegal characters
         */

        public String removeIllegalCharacters(String s)
        {
            char[] illegalCharacters = { ' ' };
            String res = s;

            for ( int j=0; j<illegalCharacters.length; j++)
            {
                int i = res.lastIndexOf(illegalCharacters[j], res.length());
                while ( i != -1 )
                {
                    String temp = res;
                    res = temp.substring(0,i);
                    res += temp.substring(i + 1);
                    i = res.lastIndexOf(illegalCharacters[j], s.length());
                }
            }
            return res;
        }

    }
}
