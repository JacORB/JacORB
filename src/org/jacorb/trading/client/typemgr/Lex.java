
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

package org.jacorb.trading.client.typemgr;

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
  private int m_line             = 1;
  private Hashtable m_literals   = new Hashtable();
  private boolean m_eof          = false;


  public static final int ERROR         = 0;
  public static final int END           = 1;  // EOF
  public static final int LBRACE        = 2;
  public static final int RBRACE        = 3;
  public static final int LANGLE        = 4;
  public static final int RANGLE        = 5;
  public static final int COLON         = 6;
  public static final int DOUBLECOLON   = 7;
  public static final int SEMICOLON     = 8;
  public static final int COMMA         = 9;
  public static final int SERVICE       = 10;
  public static final int INTERFACE     = 11;
  public static final int IDENT         = 12;
  public static final int MANDATORY     = 13;
  public static final int READONLY      = 14;
  public static final int PROPERTY      = 15;
  public static final int UNSIGNED      = 16;
  public static final int BOOLEAN       = 17;
  public static final int CHAR          = 18;
  public static final int SHORT         = 19;
  public static final int LONG          = 20;
  public static final int FLOAT         = 21;
  public static final int DOUBLE        = 22;
  public static final int STRING        = 23;
  public static final int SEQUENCE      = 24;
  public static final int OTHER         = 25;




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
    m_literals.put("service",   new Integer(SERVICE));
    m_literals.put("interface", new Integer(INTERFACE));
    m_literals.put("mandatory", new Integer(MANDATORY));
    m_literals.put("readonly",  new Integer(READONLY));
    m_literals.put("property",  new Integer(PROPERTY));
    m_literals.put("unsigned",  new Integer(UNSIGNED));
    m_literals.put("boolean",   new Integer(BOOLEAN));
    m_literals.put("char",      new Integer(CHAR));
    m_literals.put("short",     new Integer(SHORT));
    m_literals.put("long",      new Integer(LONG));
    m_literals.put("float",     new Integer(FLOAT));
    m_literals.put("double",    new Integer(DOUBLE));
    m_literals.put("string",    new Integer(STRING));
    m_literals.put("sequence",  new Integer(SEQUENCE));
    m_literals.put("other",     new Integer(OTHER));
  }


  /**
   * Advances to the next token
   */
  public void nextToken()
    throws LexException
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
        case '{':
          result = LBRACE;
          done = true;
          addLexeme(c);
          break;

        case '}':
          result = RBRACE;
          done = true;
          addLexeme(c);
          break;

        case '<':
          result = LANGLE;
          done = true;
          addLexeme(c);
          break;

        case '>':
          result = RANGLE;
          done = true;
          addLexeme(c);
          break;

/***
        case ':':
          result = COLON;
          done = true;
          addLexeme(c);
          break;
***/

        case ':': {
            addLexeme(c);
            c = nextChar();
            if (c != ':') {
              pushBack(c);
              result = COLON;
              done = true;
            }
            else {
              result = DOUBLECOLON;
              addLexeme(c);
              done = true;
            }
          }
          break;

        case ';':
          result = SEMICOLON;
          done = true;
          addLexeme(c);
          break;

        case ',':
          result = COMMA;
          done = true;
          addLexeme(c);
          break;

        case '/': {
            c = nextChar();
            if (c != '/')
              throw new LexException("expected comment", m_line);

              // consume the rest of the line
            while ((c = nextChar()) != '\n')
              ;

            continue;
          }

        case ' ':
        case '\t':
        case '\n':
        case '\r':
          continue;  // skip whitespace

      } // switch (c)


        // if we're still not done, then we must have a string, either
        // a literal or an identifier
      if (! done) {
          // if the character isn't compatible with the beginning of
          // a literal or identifier, it's an error
        if (c != '_' && ! Character.isLetter(c))
          throw new LexException("unexpected input", m_line);

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
   * Returns the current line of the analyzer
   */
  public int getLine()
  {
    return m_line;
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

      if (result == '\n')
        m_line++;
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

  /**************** comment out this line to enable main()

  public static void main(String[] args)
  {
    try {
      Lex lex = new Lex(new InputStreamReader(System.in));
      lex.nextToken();
      int token = lex.getToken();
      while (token != Lex.END && token != Lex.ERROR) {
        System.out.println("Token = '" + lex.getLexeme() + "' (" +
          tokenName(token) + ")");
        lex.nextToken();
        token = lex.getToken();
      }

      System.out.println("Token = " + tokenName(token));
    }
    catch (LexException e) {
      System.out.println("Error (" + e.getLine() + ") : " + e.getMessage());
    }
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
      case LBRACE:
        result = "LBRACE";
        break;
      case RBRACE:
        result = "RBRACE";
        break;
      case LANGLE:
        result = "LANGLE";
        break;
      case RANGLE:
        result = "RANGLE";
        break;
      case COLON:
        result = "COLON";
        break;
      case DOUBLECOLON:
        result = "DOUBLECOLON";
        break;
      case SEMICOLON:
        result = "SEMICOLON";
        break;
      case COMMA:
        result = "COMMA";
        break;
      case SERVICE:
        result = "SERVICE";
        break;
      case INTERFACE:
        result = "INTERFACE";
        break;
      case IDENT:
        result = "IDENT";
        break;
      case MANDATORY:
        result = "MANDATORY";
        break;
      case READONLY:
        result = "READONLY";
        break;
      case PROPERTY:
        result = "PROPERTY";
        break;
      case UNSIGNED:
        result = "UNSIGNED";
        break;
      case BOOLEAN:
        result = "BOOLEAN";
        break;
      case CHAR:
        result = "CHAR";
        break;
      case SHORT:
        result = "SHORT";
        break;
      case LONG:
        result = "LONG";
        break;
      case FLOAT:
        result = "FLOAT";
        break;
      case DOUBLE:
        result = "DOUBLE";
        break;
      case STRING:
        result = "STRING";
        break;
      case SEQUENCE:
        result = "SEQUENCE";
        break;
      case OTHER:
        result = "OTHER";
        break;
      default:
        result = "<unknown>";
    }

    return result;
  }

  /*********** comment out this line to enable main() */
}










