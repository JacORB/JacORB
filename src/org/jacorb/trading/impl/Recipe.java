
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

package org.jacorb.trading.impl;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.util.PropUtil;

/**
 * Used for validating and rewriting proxy constraint recipes
 */

public class Recipe
{

  private Recipe()
  {
  }

  /**
   * Validates a proxy recipe; requires properties mentioned in the
   * recipe to be present in props
   */
  public static boolean validate(String recipe, Property[] props)
  {
    boolean result = true;

    int pos = 0;
    int len = recipe.length();

      // make a hashtable of properties for quicker lookup
    Hashtable propTable = new Hashtable();
    for (int i = 0; i < props.length; i++)
      propTable.put(props[i].name, props[i].value);

    boolean seenDollar = false;

    while (pos < len && result) {
      char ch = recipe.charAt(pos);

      switch (ch) {
        case '$':
          if (seenDollar)
            seenDollar = false;
          else
            seenDollar = true;
          break;

        case '*':
          if (seenDollar)
            seenDollar = false;
          break;

        case '(':
          if (seenDollar) {
            int rparen = recipe.indexOf(')', pos);
            if (rparen < 0)
              result = false;  // missing right paren
            else {
                // extract the property name
              String propName = recipe.substring(pos + 1, rparen);

                // make sure the property exists
              if (! propTable.containsKey(propName))
                result = false;
              else {  // make sure we support the property's type
                Any any = (Any)propTable.get(propName);
                TypeCode tc = any.type();
                if (PropUtil.isDynamicProperty(tc)) {
                  DynamicProp dp = DynamicPropHelper.extract(any);
                  tc = dp.returned_type;
                }

                result = checkPropertyType(tc);
              }

              pos = rparen;
            }

            seenDollar = false;
          }
          break;
      }  // switch (ch)

      pos++;
    }  // while

    if (seenDollar)
      result = false;

    return result;
  }


  /**
   * Returns the secondary constraint by following the recipe; assumes
   * the recipe has previously been validated; returns null if a value
   * for a required property could not be obtained
   */
  public static String rewrite(String recipe, SourceAdapter src, String primary)
  {
    StringBuffer result = new StringBuffer();

    boolean seenDollar = false;
    int pos = 0;
    int len = recipe.length();

    while (pos < len) {
      char ch = recipe.charAt(pos);

      switch (ch) {
        case '$':
          if (seenDollar) {
            result.append('$');
            seenDollar = false;
          }
          else
            seenDollar = true;
          break;

        case '*':
          if (seenDollar) {
            seenDollar = false;
            result.append(primary);
          }
          else
            result.append(ch);
          break;

        case '(':
          if (seenDollar) {
            int rparen = recipe.indexOf(')', pos);
              // extract the property name
            String propName = recipe.substring(pos + 1, rparen);
              // use the source to get the property's value, which
              // automatically handles dynamic properties
            Any value = src.getPropertyValue(propName);
            if (value == null)
              return null;
            result.append(convertValue(value));
            pos = rparen;
            seenDollar = false;
          }
          else
            result.append(ch);
          break;

        default:
          result.append(ch);
      }  // switch (ch)

      pos++;
    }  // while

    return result.toString();
  }


  /** Determines if the given property type is allowable in a recipe */
  protected static boolean checkPropertyType(TypeCode tc)
  {
    boolean result = false;

    TCKind kind = tc.kind();
    switch (kind.value()) {
        // these are the types of properties supported in a recipe
      case TCKind._tk_short:
      case TCKind._tk_long:
      case TCKind._tk_ushort:
      case TCKind._tk_ulong:
      case TCKind._tk_float:
      case TCKind._tk_double:
      case TCKind._tk_boolean:
      case TCKind._tk_char:
      case TCKind._tk_string:
        result = true;
        break;
    }

    return result;
  }


  /** Converts a property value to a string */
  protected static String convertValue(Any val)
  {
    String result = "<unknown>";

    TCKind kind = val.type().kind();

    try {
      switch (kind.value()) {
        case TCKind._tk_short: {
            int s = val.extract_short();
            result = "" + s;
          }
          break;

        case TCKind._tk_long: {
            int l = val.extract_long();
            result = "" + l;
          }
          break;

        case TCKind._tk_ushort: {
            int i = val.extract_ushort();
            result = "" + i;
          }
          break;

        case TCKind._tk_ulong: {
            long l = val.extract_ulong();
            result = "" + l;
          }
          break;

        case TCKind._tk_float: {
            float f = val.extract_float();
            result = "" + f;
          }
          break;

        case TCKind._tk_double: {
            double d = val.extract_double();
            result = "" + d;
          }
          break;

        case TCKind._tk_boolean: {
            boolean b = val.extract_boolean();
            result = "" + (b ? "TRUE" : "FALSE");
          }
          break;

        case TCKind._tk_char: {
            char c = val.extract_char();
            result = "'" + c + "'";
          }
          break;

        case TCKind._tk_string: {
            String s = val.extract_string();
            result = "'" + s + "'";
          }
          break;
      }
    }
    catch (BAD_OPERATION e) {
      e.printStackTrace();
    }

    return result;
  }


  /**************** comment out this line to enable main()
  public static void main(String[] args)
  {
    if (args.length != 1) {
      System.out.println("Usage: Recipe <recipe>");
      return;
    }

    ORB orb = ORB.init();
    Any any;

    Property[] props = new Property[2];
    any = orb.create_any();
    any.insert_long(2050023013);
    props[0] = new Property("prop1", any);
    any = orb.create_any();
    any.insert_char('z');
    props[1] = new Property("prop2", any);

    if (! Recipe.validate(args[0], props)) {
      System.out.println("Invalid recipe");
      return;
    }

    SourceAdapter source = new SourceAdapter(null, props);
    String result = Recipe.rewrite(args[0], source, "primary constraint");
    System.out.println(result);
  }
  /**************** comment out this line to enable main() */
}




