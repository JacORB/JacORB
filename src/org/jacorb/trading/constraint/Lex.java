
// Copyright (C) 1998-2001
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
import java.util.*;


/**
 * Lex is the lexical analyzer used to produce tokens from an
 * input source
 */
public class Lex
{
  private StringBuffer m_input;
  private int m_token            = ERROR;
  private StringBuffer m_buffer  = new StringBuffer();
  private String m_lexeme        = null;
  private int m_pos              = 0;
  private Hashtable m_literals   = new Hashtable();
  private boolean m_eof          = false;


  public static final int ERROR         = 0;
  public static final int END           = 1;  // EOF
  public static final int LPAREN        = 2;
  public static final int RPAREN        = 3;
  public static final int EXIST         = 4;
  public static final int MINUS         = 5;
  public static final int NOT           = 6;
  public static final int MULT          = 7;
  public static final int DIV           = 8;
  public static final int PLUS          = 9;
  public static final int TILDE         = 10;
  public static final int IN            = 11;
  public static final int EQUAL         = 12;
  public static final int NOT_EQUAL     = 13;
  public static final int LESS          = 14;
  public static final int LESS_EQUAL    = 15;
  public static final int GREATER       = 16;
  public static final int GREATER_EQUAL = 17;
  public static final int AND           = 18;
  public static final int OR            = 19;
  public static final int IDENT         = 20;
  public static final int TRUE_LIT      = 21;  // literal
  public static final int FALSE_LIT     = 22;  // literal
  public static final int STRING_LIT    = 23;  // literal
  public static final int NUMBER_LIT    = 24;  // literal
  public static final int PREF_MIN      = 25;
  public static final int PREF_MAX      = 26;
  public static final int PREF_WITH     = 27;
  public static final int PREF_RANDOM   = 28;
  public static final int PREF_FIRST    = 29;


  private Lex()
  {
  }


  /**
   * Constructs a new lexical analyzer
   */
  public Lex(Reader reader)
  {
    BufferedReader br = new BufferedReader(reader);
    m_input = new StringBuffer();

      // read all of the characters into our string buffer
    boolean eof = false;
    char[] chars = new char[512];
    while (! eof) {
      try {
        int len = br.read(chars);
        if (len < 0)
          eof = true;
        else
          m_input.append(chars, 0, len);
      }
      catch (IOException e) {
      }
    }

      // load literals - maps the token to its numeric value
    m_literals.put("TRUE", new Integer(TRUE_LIT));
    m_literals.put("FALSE", new Integer(FALSE_LIT));
    m_literals.put("and", new Integer(AND));
    m_literals.put("not", new Integer(NOT));
    m_literals.put("or", new Integer(OR));
    m_literals.put("exist", new Integer(EXIST));
    m_literals.put("in", new Integer(IN));
    m_literals.put("min", new Integer(PREF_MIN));
    m_literals.put("max", new Integer(PREF_MAX));
    m_literals.put("with", new Integer(PREF_WITH));
    m_literals.put("random", new Integer(PREF_RANDOM));
    m_literals.put("first", new Integer(PREF_FIRST));

      // advance to first token
    nextToken();
  }


  /**
   * Advances to the next token
   */
  public void nextToken()
  {
    int result = ERROR;

    boolean done = false;

    clearLexeme();

    while (! done) {

      char c = nextChar();

      // check for end-of-file
      if (eof()) {
        result = END;
        break;
      }

      switch (c) {
        case '(':
          result = LPAREN;
          done = true;
          addLexeme(c);
          break;

        case ')':
          result = RPAREN;
          done = true;
          addLexeme(c);
          break;

        case '-':
          result = MINUS;
          done = true;
          addLexeme(c);
          break;

        case '*':
          result = MULT;
          done = true;
          addLexeme(c);
          break;

        case '/':
          result = DIV;
          done = true;
          addLexeme(c);
          break;

        case '+':
          result = PLUS;
          done = true;
          addLexeme(c);
          break;

        case '~':
          result = TILDE;
          done = true;
          addLexeme(c);
          break;

        case '=': {
            // make sure the next character is '=' (to form token '==')
          char peek = nextChar();
          if (peek == '=') {
            addLexeme(c);
            addLexeme(peek);
            result = EQUAL;
          }
          else {
            pushBack(peek);
            result = ERROR;
          }
          done = true;
          break;
        }

        case '!': {
            // make sure the next character is '=' (to form token '!=')
          char peek = nextChar();
          if (peek == '=') {
            addLexeme(c);
            addLexeme(peek);
            result = NOT_EQUAL;
          }
          else
            result = ERROR;
          done = true;
          break;
        }

        case '<': {
          char peek = nextChar();
          addLexeme(c);
            // check for trailing '=' (to form token '<=')
          if (peek == '=') {
            addLexeme(peek);
            result = LESS_EQUAL;
          }
          else {
              // trailing '=' not found - push back the character
            pushBack(peek);
            result = LESS;
          }
          done = true;
          break;
        }

        case '>': {
          char peek = nextChar();
          addLexeme(c);
            // check for trailing '=' (to form token '>=')
          if (peek == '=') {
            addLexeme(peek);
            result = GREATER_EQUAL;
          }
          else {
              // trailing '=' not found - push back the character
            pushBack(peek);
            result = GREATER;
          }
          done = true;
          break;
        }

        case '\'': {
            // quoted string

          result = STRING_LIT;

            // search until we find the matching quote
          while ((c = nextChar()) != '\'') {
            if (c == '\n') {
              result = ERROR;
              break;
            }

              // allow characters to be escaped (e.g. the quote)
            if (c == '\\')
              c = nextChar(); // skip the escape char

              // if we encounter EOF before the matching quote, it's an error
            if (eof()) {
              result = ERROR;
              break;
            }

            addLexeme(c);
          } // while

          done = true;
          break;
        }

        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9': {
            // deal with a number

          boolean seenExp = false;
          boolean seenPeriod = false;
          boolean formatError = false;
          char lastChar = 0;

          while (! done) {

            switch (c) {
              case '.':
                  // it's an error if we've already seen a '.'
                if (seenPeriod) {
                  formatError = true;
                  done = true;
                }
                else {
                  addLexeme(c);
                  seenPeriod = true;
                }
                break;

              case 'E':
              case 'e':
                  // it's an error if we've already seen a 'E' or if
                  // the previous character was not a digit
                if (seenExp || ! Character.isDigit(lastChar)) {
                  formatError = true;
                  done = true;
                }
                else {
                  seenExp = true;
                  addLexeme(c);
                  c = nextChar();
                    // check for +/- on exponent
                  if (c == '+' || c == '-')
                    addLexeme(c);
                  else
                    pushBack(c);
                }
                break;

              case '0':
              case '1':
              case '2':
              case '3':
              case '4':
              case '5':
              case '6':
              case '7':
              case '8':
              case '9':
                addLexeme(c);
                break;

              default:
                  // anything else terminates the number
                pushBack(c);
                done = true;
                break;
            } // switch (c)

            if (! done) {
              lastChar = c; // remember the last character
              c = nextChar();
              if (eof())
                done = true;
            }
          } // while (! done)

            // if there were no errors, then decide what kind of
            // number we've found
          if (! formatError) {
              // if the last character seen is not a digit, it's an error
            if (! Character.isDigit(lastChar))
              result = ERROR;
            else
              result = NUMBER_LIT;
          }
          break;
        }

        case ' ':
        case '\t':
        case '\n':
          continue;  // skip whitespace

      } // switch (c)

        // if we're still not done, then we must have a string, either
        // a literal or an identifier
      if (! done) {
          // if the character isn't compatible with the beginning of
          // a literal or identifier, it's an error
        if (c != '_' && ! Character.isLetter(c)) {
          result = ERROR;
          done = true;
        }
        else {

            // deal with string literal or identifier

          while (isIdent(c) && ! eof()) {
            addLexeme(c);
            c = nextChar();
          }

          if (! eof())
            pushBack(c);

            // see if the lexeme is a literal
          String lexeme = getLexeme();
          Integer val = (Integer)m_literals.get(lexeme);

            // if we didn't find a literal, then it must be an identifier
          if (val == null)
            result = IDENT;
          else
            result = val.intValue();

          done = true;
        }
      }

    } // while (! done)

    m_token = result;
  }


  /**
   * Returns the current token
   */
  public int getToken()
  {
    return m_token;
  }


  /**
   * Returns the current lexeme
   */
  public String getLexeme()
  {
    if (m_lexeme == null)
      m_lexeme = m_buffer.toString();

    return m_lexeme;
  }


  /**
   * Returns the current position of the analyzer
   */
  public int getPosition()
  {
    return m_pos;
  }


  protected boolean eof()
  {
    return m_eof;
  }


  protected char nextChar()
  {
    char result = 0;

    if (m_pos < m_input.length()) {
      result = m_input.charAt(m_pos);
      m_pos++;
    }
    else
      m_eof = true;

    return result;
  }


  protected void pushBack(char c)
  {
    if (c != 0)
      m_pos--;
  }


  protected boolean isIdent(char c)
  {
    return (Character.isLetter(c) || Character.isDigit(c) || (c == '_'));
  }


  protected void clearLexeme()
  {
    m_lexeme = null;
    m_buffer.setLength(0);
  }


  protected void addLexeme(char c)
  {
    m_buffer.append(c);
  }

  //**************** comment out this line to enable main()

  public static void main(String[] args)
  {
    if (args.length < 1) {
      System.err.println("Usage: Lex expr");
      System.exit(1);
    }

    Lex lex = new Lex(new StringReader(args[0]));
    int token = lex.getToken();
    while (token != Lex.END && token != Lex.ERROR) {
      System.out.println("Token = '" + lex.getLexeme() + "' (" +
        tokenName(token) + ")");
      lex.nextToken();
      token = lex.getToken();
    }
    System.out.println("Token = " + tokenName(token));
  }


  protected static String tokenName(int token)
  {
    String result;

    switch (token) {
      case ERROR:
        result = "ERROR";
        break;
      case END:
        result = "END";
        break;
      case LPAREN:
        result = "LPAREN";
        break;
      case RPAREN:
        result = "RPAREN";
        break;
      case EXIST:
        result = "EXIST";
        break;
      case MINUS:
        result = "MINUS";
        break;
      case NOT:
        result = "NOT";
        break;
      case MULT:
        result = "MULT";
        break;
      case DIV:
        result = "DIV";
        break;
      case PLUS:
        result = "PLUS";
        break;
      case TILDE:
        result = "TILDE";
        break;
      case IN:
        result = "IN";
        break;
      case EQUAL:
        result = "EQUAL";
        break;
      case NOT_EQUAL:
        result = "NOT_EQUAL";
        break;
      case LESS:
        result = "LESS";
        break;
      case LESS_EQUAL:
        result = "LESS_EQUAL";
        break;
      case GREATER:
        result = "GREATER";
        break;
      case GREATER_EQUAL:
        result = "GREATER_EQUAL";
        break;
      case AND:
        result = "AND";
        break;
      case OR:
        result = "OR";
        break;
      case IDENT:
        result = "IDENT";
        break;
      case TRUE_LIT:
        result = "TRUE_LIT";
        break;
      case FALSE_LIT:
        result = "FALSE_LIT";
        break;
      case STRING_LIT:
        result = "STRING_LIT";
        break;
      case NUMBER_LIT:
        result = "NUMBER_LIT";
        break;
      case PREF_MIN:
        result = "PREF_MIN";
        break;
      case PREF_MAX:
        result = "PREF_MAX";
        break;
      case PREF_WITH:
        result = "PREF_WITH";
        break;
      case PREF_RANDOM:
        result = "PREF_RANDOM";
        break;
      case PREF_FIRST:
        result = "PREF_FIRST";
        break;
      default:
        result = "<unknown>";
    }

    return result;
  }

  //*********** comment out this line to enable main() */
}










