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

package org.jacorb.trading.client.typemgr;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;

public class Parser
{
  private Lex m_lex;
  private String m_currentIdent;
  private String m_currentScopedName;
  private String m_name;
  private String m_interface;
  private PropStruct m_currentProperty;
  private PropStruct[] m_properties;
  private String[] m_superTypes;



  public Parser()
  {
  }


  public void parse(Reader reader)
    throws ParserException
  {
    m_currentProperty = new PropStruct();

    try {
      m_lex = new Lex(reader);
      m_lex.nextToken();

      if (m_lex.getToken() != Lex.END)
        parseService();
      else
        throw new ParserException("no input", 0);
    }
    catch (LexException e) {
      throw new
        ParserException("Lexical error: " + e.getMessage(), e.getLine());
    }
  }


  public String getName()
  {
    return m_name;
  }


  public String getInterface()
  {
    return m_interface;
  }


  public PropStruct[] getProperties()
  {
    return m_properties;
  }


  public String[] getSuperTypes()
  {
    return m_superTypes;
  }


  protected void parseService()
    throws ParserException, LexException
  {
    int t = m_lex.getToken();
    if (t == Lex.SERVICE) {
      m_lex.nextToken();

      parseScopedName();
      m_name = m_currentScopedName;

      parseBaseTypes();

      if (m_lex.getToken() != Lex.LBRACE)
        throw new ParserException("expected '{' symbol", m_lex.getLine());
      m_lex.nextToken();

      if (m_lex.getToken() != Lex.INTERFACE)
        throw new ParserException(
          "expected 'interface' declaration", m_lex.getLine());
      m_lex.nextToken();

      parseScopedName();
      m_interface = m_currentScopedName;

      if (m_lex.getToken() != Lex.SEMICOLON)
        throw new ParserException(
          "expected ';' after 'interface' declaration", m_lex.getLine());
      m_lex.nextToken();

      parsePropertyList();

      if (m_lex.getToken() != Lex.RBRACE)
        throw new ParserException("expected '}' symbol", m_lex.getLine());
      m_lex.nextToken();

      if (m_lex.getToken() != Lex.SEMICOLON)
        throw new ParserException(
          "expected ';' after 'service' declaration", m_lex.getLine());
      m_lex.nextToken();
    }
    else
      throw new ParserException(
        "expected 'service' declaration", m_lex.getLine());
  }


  protected void parseBaseTypes()
    throws ParserException, LexException
  {
    Vector superTypes = new Vector();

    int t = m_lex.getToken();
    if (t == Lex.COLON) {
      m_lex.nextToken();

      parseScopedName();
      superTypes.addElement(m_currentScopedName);

        // process the base service types

      while (m_lex.getToken() == Lex.COMMA) {
        m_lex.nextToken();
        parseScopedName();
        superTypes.addElement(m_currentScopedName);
      }
    }

      // save our list of supertypes
    m_superTypes = new String[superTypes.size()];
    superTypes.copyInto(m_superTypes);
  }


  protected void parseScopedName()
    throws ParserException, LexException
  {
    int t = m_lex.getToken();

    if (t != Lex.IDENT && t != Lex.DOUBLECOLON)
      throw new ParserException(
        "expected scoped identifier", m_lex.getLine());

    boolean seenIdent = false;
    StringBuffer name = new StringBuffer();
    int lastToken = t;

    while (t == Lex.IDENT || t == Lex.DOUBLECOLON) {
      name.append(m_lex.getLexeme());
      if (t == Lex.IDENT)
        seenIdent = true;
      lastToken = t;
      m_lex.nextToken();
      t = m_lex.getToken();
        // don't want two DOUBLECOLONs in a row
      if (t == Lex.DOUBLECOLON && lastToken == Lex.DOUBLECOLON)
        throw new ParserException(
          "malformed scoped identifier", m_lex.getLine());
    }

      // check for a malformed identifier; see if we've gotten
      // and IDENT, and make sure the name didn't end with "::"
    if (! seenIdent || lastToken == Lex.DOUBLECOLON)
      throw new ParserException(
        "malformed scoped identifier", m_lex.getLine());

    m_currentScopedName = name.toString();
  }


  protected void parseIdent()
    throws ParserException, LexException
  {
    int t = m_lex.getToken();
    if (t == Lex.IDENT) {
      m_currentIdent = m_lex.getLexeme();
      m_lex.nextToken();
    }
  }


  protected void parsePropertyList()
    throws ParserException, LexException
  {
    Vector props = new Vector();

    int t = m_lex.getToken();
    while (t != Lex.RBRACE) {
      parseProperty();

        // save current property
      props.addElement(new PropStruct(m_currentProperty.name,
        m_currentProperty.value_type, m_currentProperty.mode));

      t = m_lex.getToken();
    }

      // save our list of supertypes
    m_properties = new PropStruct[props.size()];
    Enumeration e = props.elements();
    int count = 0;
    while (e.hasMoreElements())
      m_properties[count++] = (PropStruct)e.nextElement();
  }


  protected void parseProperty()
    throws ParserException, LexException
  {
    m_currentProperty.mode = PropertyMode.PROP_NORMAL;

    parseQualifierList();

    int t = m_lex.getToken();
    if (t == Lex.PROPERTY) {
      m_lex.nextToken();

      parseIDLType();

      if (m_lex.getToken() != Lex.IDENT)
        throw new ParserException(
          "expected property identifier after IDL type", m_lex.getLine());

      parseIdent();

      m_currentProperty.name = m_currentIdent;

      if (m_lex.getToken() != Lex.SEMICOLON)
        throw new ParserException(
          "expected ';' after property declaration", m_lex.getLine());
      m_lex.nextToken();
    }
    else
      throw new ParserException(
        "invalid property declaration", m_lex.getLine());
  }


  protected void parseQualifierList()
    throws ParserException, LexException
  {
    int t = m_lex.getToken();
    while (t == Lex.MANDATORY || t == Lex.READONLY) {
      PropertyMode mode = m_currentProperty.mode;

      if (t == Lex.MANDATORY) {
        if (mode == PropertyMode.PROP_NORMAL)
          mode = PropertyMode.PROP_MANDATORY;
        else if (mode == PropertyMode.PROP_READONLY)
          mode = PropertyMode.PROP_MANDATORY_READONLY;
        else
          throw new ParserException(
            "duplicate 'mandatory' qualifier", m_lex.getLine());
      }
      else if (t == Lex.READONLY) {
        if (mode == PropertyMode.PROP_NORMAL)
          mode = PropertyMode.PROP_READONLY;
        else if (mode == PropertyMode.PROP_MANDATORY)
          mode = PropertyMode.PROP_MANDATORY_READONLY;
        else
          throw new ParserException(
            "duplicate 'readonly' qualifier", m_lex.getLine());
      }

      m_currentProperty.mode = mode;

      m_lex.nextToken();
      t = m_lex.getToken();
    }
  }


  protected void parseIDLType()
    throws ParserException, LexException
  {
    int t = m_lex.getToken();

    if (t == Lex.SEQUENCE) {
      m_lex.nextToken();

      if (m_lex.getToken() != Lex.LANGLE)
        throw new ParserException(
          "expected '<' after 'sequence'", m_lex.getLine());
      m_lex.nextToken();

      parseType(true);

      if (m_lex.getToken() != Lex.RANGLE)
        throw new ParserException(
          "expected '>' after sequence type", m_lex.getLine());
      m_lex.nextToken();
    }
    else if (t == Lex.OTHER) {
      m_currentProperty.value_type = ORB.init().get_primitive_tc(TCKind.tk_null);
      m_lex.nextToken();
    }
    else
      parseType(false);
  }


  protected void parseType(boolean seq)
    throws ParserException, LexException
  {
    ORB orb = ORB.init();

    int t = m_lex.getToken();
    switch (t) {
      case Lex.UNSIGNED:
        m_lex.nextToken();
        t = m_lex.getToken();
        if (t == Lex.SHORT) {
          TypeCode shortTC = orb.get_primitive_tc(TCKind.tk_short);
          if (seq)
            m_currentProperty.value_type = orb.create_sequence_tc(0, shortTC);
          else
            m_currentProperty.value_type = shortTC;
          m_lex.nextToken();
        }
        else if (t == Lex.LONG) {
          TypeCode ulongTC = orb.get_primitive_tc(TCKind.tk_ulong);
          if (seq)
            m_currentProperty.value_type = orb.create_sequence_tc(0, ulongTC);
          else
            m_currentProperty.value_type = ulongTC;
          m_lex.nextToken();
        }
        else
          throw new ParserException(
            "only short and long can be unsigned", m_lex.getLine());
        break;

      case Lex.CHAR:
        TypeCode charTC = orb.get_primitive_tc(TCKind.tk_char);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, charTC);
        else
          m_currentProperty.value_type = charTC;
        m_lex.nextToken();
        break;

      case Lex.BOOLEAN:
        TypeCode booleanTC = orb.get_primitive_tc(TCKind.tk_boolean);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, booleanTC);
        else
          m_currentProperty.value_type = booleanTC;
        m_lex.nextToken();
        break;

      case Lex.SHORT:
        TypeCode shortTC = orb.get_primitive_tc(TCKind.tk_short);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, shortTC);
        else
          m_currentProperty.value_type = shortTC;
        m_lex.nextToken();
        break;

      case Lex.LONG:
        TypeCode longTC = orb.get_primitive_tc(TCKind.tk_long);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, longTC);
        else
          m_currentProperty.value_type = longTC;
        m_lex.nextToken();
        break;

      case Lex.FLOAT:
        TypeCode floatTC = orb.get_primitive_tc(TCKind.tk_float);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, floatTC);
        else
          m_currentProperty.value_type = floatTC;
        m_lex.nextToken();
        break;

      case Lex.DOUBLE:
        TypeCode doubleTC = orb.get_primitive_tc(TCKind.tk_double);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, doubleTC);
        else
          m_currentProperty.value_type = doubleTC;
        m_lex.nextToken();
        break;

      case Lex.STRING:
        TypeCode stringTC = orb.get_primitive_tc(TCKind.tk_string);
        if (seq)
          m_currentProperty.value_type = orb.create_sequence_tc(0, stringTC);
        else
          m_currentProperty.value_type = stringTC;
        m_lex.nextToken();
        break;

      default:
        throw new ParserException(
          "unknown/unsupported IDL type '" + m_lex.getLexeme() + "'",
          m_lex.getLine());
    } // switch (t)
  }


  /*********************** comment out this line to enable main()

  public static void main(String[] args)
  {
    try {
      Parser parser = new Parser();
      parser.parse(new InputStreamReader(System.in));
      System.out.println("Parse OK");
      System.out.println("Service name: " + parser.getName());
      System.out.println("Interface   : " + parser.getInterface());
      System.out.print("Super types : ");
      String[] superTypes = parser.getSuperTypes();
      for (int i = 0; i < superTypes.length; i++)
        System.out.print(superTypes[i] + " ");
      System.out.println();

      PropStruct[] props = parser.getProperties();
      System.out.println();
      for (int i = 0; i < props.length; i++) {
        System.out.println("Property: " + props[i].name);
        System.out.print("Mode    : ");
        switch (props[i].mode.value()) {
          case PropertyMode._PROP_NORMAL:
            System.out.println("Normal");
            break;
          case PropertyMode._PROP_READONLY:
            System.out.println("Read-only");
            break;
          case PropertyMode._PROP_MANDATORY:
            System.out.println("Mandatory");
            break;
          case PropertyMode._PROP_MANDATORY_READONLY:
            System.out.println("Mandatory Read-only");
            break;
        }

        System.out.println("Type    : " +
          convertType(props[i].value_type));
        System.out.println();
      }
    }
    catch (ParserException e) {
      System.out.println("Error (" + e.getLine() + ") : " + e.getMessage());
    }
  }


  protected static String convertType(TypeCode tc)
  {
    String result = null;

    TCKind kind = tc.kind();
    if (kind == TCKind.tk_sequence) {
      try {
        TypeCode elemTC = tc.content_type();
        kind = elemTC.kind();
        result = "sequence<" + convertKind(kind) + ">";
      }
      catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
        throw new RuntimeException();
      }
    }
    else
      result = convertKind(kind);

    return result;
  }


  protected static String convertKind(TCKind kind)
  {
    String result = "unknown";

    switch (kind.value()) {
      case TCKind._tk_null:
        result = "other";
        break;
      case TCKind._tk_boolean:
        result = "boolean";
        break;
      case TCKind._tk_short:
        result = "short";
        break;
      case TCKind._tk_ushort:
        result = "unsigned short";
        break;
      case TCKind._tk_long:
        result = "long";
        break;
      case TCKind._tk_ulong:
        result = "unsigned long";
        break;
      case TCKind._tk_float:
        result = "float";
        break;
      case TCKind._tk_double:
        result = "double";
        break;
      case TCKind._tk_char:
        result = "char";
        break;
      case TCKind._tk_string:
        result = "string";
        break;
    }

    return result;
  }

  /*********************** comment out this line to enable main() */
}










