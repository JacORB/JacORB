
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.constraint;

import java.io.*;


public class Expression
{
  private PropertySchema m_schema;
  private ExprNode m_root;


  private Expression()
  {
  }


  public Expression(PropertySchema schema)
  {
    m_schema = schema;
  }


  /**
   * Uses the given Lex object to validate an expression, throwing
   * ParseException if an error occurred; the type of expression is
   * returned upon successful parsing
   */
  public ValueType parse(Lex lex)
    throws ParseException
  {
    m_root = null;

    m_root = parseBoolOr(lex);

    if (m_root == null)
      throw new ParseException("invalid input");
    else if (lex.getToken() != Lex.END)
      throw new ParseException("unexpected input");

    return m_root.getType();
  }


  /**
   * Evaluates the expression using the given property source to supply
   * values for properties used in the expression; the value of the
   * expression is returned, or null if evaluation failed
   */
  public Value evaluate(PropertySource source)
  {
    Value result = null;

    try {
      result = m_root.evaluate(source);
    }
    catch (MissingPropertyException e) {
    }
    catch (ArithmeticException e) {
    }

    return result;
  }


  protected ExprNode parseBoolean(Lex lex)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.TRUE_LIT || token == Lex.FALSE_LIT) {
      result = new LiteralNode(LiteralNode.BOOLEAN, lex.getLexeme());
      lex.nextToken();
    }

    return result;
  }


  protected ExprNode parseString(Lex lex)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.STRING_LIT) {
      result = new LiteralNode(LiteralNode.STRING, lex.getLexeme());
      lex.nextToken();
    }

    return result;
  }


  protected ExprNode parseNumber(Lex lex)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.NUMBER_LIT) {
      result = new LiteralNode(LiteralNode.NUMBER, lex.getLexeme());
      lex.nextToken();
    }

    return result;
  }


  protected ExprNode parseFactor(Lex lex)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.LPAREN) {
      lex.nextToken();
      result = parseBoolOr(lex);
      if (lex.getToken() != Lex.RPAREN)
        throw new ParseException("missing closing parenthesis");
      else
        lex.nextToken();
    }
    else if (token == Lex.EXIST) {
      lex.nextToken();
      ExprNode ident = parseIdent(lex, false);
      if (ident == null)
        throw new ParseException("operand to 'exist' must be a property");
      else
        result = new ExistNode(ident);
    }
    else if (token == Lex.IDENT)
      result = parseIdent(lex, true);
    else if (token == Lex.NUMBER_LIT)
      result = parseNumber(lex);
    else if (token == Lex.MINUS) {
      lex.nextToken();
      ExprNode arg = null;

      if (lex.getToken() == Lex.NUMBER_LIT)
        arg = parseNumber(lex);
      else if (lex.getToken() == Lex.IDENT)
        arg = parseIdent(lex, true);

      if (arg == null)
        throw new ParseException("operand to unary '-' must be a number");
      else
        result = new NegNode(arg);
    }
    else if (token == Lex.STRING_LIT)
      result = parseString(lex);
    else if (token == Lex.TRUE_LIT || token == Lex.FALSE_LIT)
      result = parseBoolean(lex);
    else
      throw new ParseException("unexpected factor '" + lex.getLexeme() + "'");

    return result;
  }


  protected ExprNode parseFactorNot(Lex lex)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.NOT) {
      lex.nextToken();
      ExprNode child = parseFactor(lex);
      ValueType childType = child.getType();

      if (! ValueType.isCompatible(childType.getId(), ValueType.BOOLEAN) ||
            childType.isSequence())
        throw new ParseException(
          "operand to 'not' must be a boolean expression");
      else
        result = new NotNode(child);
    }
    else
      result = parseFactor(lex);

    return result;
  }


  protected ExprNode parseTerm(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseFactorNot(lex);

    int token = lex.getToken();

    while (token == Lex.MULT || token == Lex.DIV) {
        // save the operator for use in error messages
      String op = lex.getLexeme();

      lex.nextToken();
      ExprNode right = parseFactorNot(lex);

      if (! result.getType().isNumber() || ! right.getType().isNumber() ||
          result.getType().isSequence() || right.getType().isSequence())
        throw new ParseException("operands to '" + op + "' must be numeric");

      if (! ValueType.isCompatible(result.getType().getId(),
            right.getType().getId()))
        throw new ParseException("operands to '" + op + "' are not compatible");

      if (token == Lex.MULT)
        result = new MultiplyNode(result, right);
      else  // token == Lex.DIV
        result = new DivideNode(result, right);

      token = lex.getToken();
    }

    return result;
  }


  protected ExprNode parseExpr(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseTerm(lex);

    int token = lex.getToken();

    while (token == Lex.PLUS || token == Lex.MINUS) {
        // save the operator for use in error messages
      String op = lex.getLexeme();

      lex.nextToken();
      ExprNode right = parseTerm(lex);

      if (! result.getType().isNumber() || ! right.getType().isNumber() ||
          result.getType().isSequence() || right.getType().isSequence())
        throw new ParseException("operands to '" + op + "' must be numeric");

      if (! ValueType.isCompatible(result.getType().getId(),
            right.getType().getId()))
        throw new ParseException("operands to '" + op + "' are not compatible");

      if (token == Lex.PLUS)
        result = new AddNode(result, right);
      else  // token == Lex.MINUS
        result = new SubtractNode(result, right);

      token = lex.getToken();
    }

    return result;
  }


  protected ExprNode parseIdent(Lex lex, boolean checkType)
    throws ParseException
  {
    ExprNode result = null;

    int token = lex.getToken();
    if (token == Lex.IDENT) {
      String property = lex.getLexeme();
      ValueType type = m_schema.getPropertyType(property);
      if (type == null) {
        if (checkType)
          throw new ParseException("unknown property '" + property + "'");
        else
          type = new ValueType(ValueType.OTHER);
      }

      result = new PropertyNode(property, type);
      lex.nextToken();
    }

    return result;
  }


  protected ExprNode parseExprTwiddle(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseExpr(lex);

    int token = lex.getToken();

    if (token == Lex.TILDE) {
      lex.nextToken();
      ExprNode right = parseExpr(lex);

      if (! ValueType.isCompatible(result.getType().getId(), ValueType.STRING) ||
          ! ValueType.isCompatible(right.getType().getId(), ValueType.STRING))
        throw new ParseException("operands to '~' must be strings");

        // check for presence of sequence
      if (result.getType().isSequence() || right.getType().isSequence())
        throw new ParseException("sequence not allowed as operand to '~'");

      result = new SubstrNode(result, right);
    }

    return result;
  }


  protected ExprNode parseExprIn(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseExprTwiddle(lex);

    int token = lex.getToken();

    if (token == Lex.IN) {
      lex.nextToken();

        // the right operand to in must be a sequence
      ExprNode right = parseIdent(lex, true);

      if (right == null)
        throw new ParseException("right operand to 'in' must be a property");

        // make sure right is a sequence
      if (! right.getType().isSequence())
        throw new ParseException(
          "right operand to 'in' must be a sequence property");

      if (! ValueType.isCompatible(result.getType().getId(),
            right.getType().getId()))
        throw new ParseException("operands to 'in' are not compatible");

      result = new InNode(result, right);
    }

    return result;
  }


  protected ExprNode parseBoolCompare(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseExprIn(lex);

    int token = lex.getToken();

    if (token == Lex.EQUAL) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
            right.getType().getId()) || result.getType().isSequence() ||
            right.getType().isSequence())
        throw new ParseException("operands to '==' are not compatible");

      result = new EqNode(result, right);
    }
    else if (token == Lex.NOT_EQUAL) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
          right.getType().getId()) || result.getType().isSequence() ||
          right.getType().isSequence())
        throw new ParseException("operands to '!=' are not compatible");

      result = new NeqNode(result, right);
    }
    else if (token == Lex.LESS) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
          right.getType().getId()) || result.getType().isSequence() ||
          right.getType().isSequence())
        throw new ParseException("operands to '<' are not compatible");

      result = new LtNode(result, right);
    }
    else if (token == Lex.LESS_EQUAL) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
          right.getType().getId()) || result.getType().isSequence() ||
          right.getType().isSequence())
        throw new ParseException("operands to '<=' are not compatible");

      result = new LeNode(result, right);
    }
    else if (token == Lex.GREATER) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
          right.getType().getId()) || result.getType().isSequence() ||
          right.getType().isSequence())
        throw new ParseException("operands to '>' are not compatible");

      result = new GtNode(result, right);
    }
    else if (token == Lex.GREATER_EQUAL) {
      lex.nextToken();
      ExprNode right = parseExprIn(lex);

      if (! ValueType.isCompatible(result.getType().getId(),
          right.getType().getId()) || result.getType().isSequence() ||
          right.getType().isSequence())
        throw new ParseException("operands to '>=' are not compatible");

      result = new GeNode(result, right);
    }

    return result;
  }


  protected ExprNode parseBoolAnd(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseBoolCompare(lex);

    while (lex.getToken() == Lex.AND) {
      lex.nextToken();
      ExprNode right = parseBoolCompare(lex);

      if (! ValueType.isCompatible(result.getType().getId(), ValueType.BOOLEAN) ||
          ! ValueType.isCompatible(right.getType().getId(), ValueType.BOOLEAN) ||
          result.getType().isSequence() || right.getType().isSequence())
        throw new ParseException(
          "operands to 'and' must be boolean expressions");

      result = new AndNode(result, right);
    }

    return result;
  }


  protected ExprNode parseBoolOr(Lex lex)
    throws ParseException
  {
    ExprNode result;

    result = parseBoolAnd(lex);

    while (lex.getToken() == Lex.OR) {
      lex.nextToken();
      ExprNode right = parseBoolAnd(lex);

      if (! ValueType.isCompatible(result.getType().getId(), ValueType.BOOLEAN) ||
          ! ValueType.isCompatible(right.getType().getId(), ValueType.BOOLEAN) ||
          result.getType().isSequence() || right.getType().isSequence())
        throw new ParseException(
          "operands to 'or' must be boolean expressions");

      result = new OrNode(result, right);
    }

    return result;
  }


  /************** comment out this line to enable main()

  public static void main(String[] args)
  {
    if (args.length < 1) {
      System.err.println("Usage: Expression expr");
      System.exit(1);
    }

    Expression expr = new Expression(null);

    try {
      StringReader reader = new StringReader(args[0]);
      Lex lex = new Lex(reader);
      ValueType type = expr.parse(lex);
      System.out.println("type of expression = " + type);
      Value value = expr.evaluate(null);
      System.out.println("result of expression = " + value);
    }
    catch (ParseException e) {
      System.err.println("Parse error: " + e.getMessage());
    }
  }

  /************** comment out this line to enable main() */
}










