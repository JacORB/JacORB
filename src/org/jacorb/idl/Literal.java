package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import java.util.*;
import java.io.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

class Literal
    extends IdlSymbol
{
    public String string;
    public boolean wide;
    public java_cup.runtime.token token;

    private ConstDecl declared_in;

    public Literal(int num)
    {
        super(num);
    }

    public void setDeclaration( ConstDecl declared_in )
    {
        this.declared_in = declared_in;
    }

    public void parse()
    {
        // const expressions containing literals can be declared
        // outside cons declarations (e.g, in sequence bounds),
        // but we care only for const declarations here.

        if (declared_in != null)
        {
            TypeSpec ts = declared_in.const_type.symbol.typeSpec();

            Environment.output(2, "Literal " + ts.getClass().getName() + " " +
                               ( token != null? token.getClass().getName() :"<no token>"));

            if( ts instanceof FloatPtType &&
                !(token instanceof java_cup.runtime.float_token  ))
            {
                parser.error("Expecting float/double constant!" );
            }
            else if( ts instanceof FixedPointConstType &&
                     !( token instanceof fixed_token  ) )
            {
                parser.error("Expecting fixed point constant (perhaps a missing \"d\")!" );
            }
            else if( ts instanceof StringType )
            {
                if( wide && !((StringType)ts).isWide())
                    parser.error("Illegal assignment of wide string constant to string!" );

            }
            else if ((ts instanceof LongType) || (ts instanceof ShortType))
            {
               if (token instanceof java_cup.runtime.long_token)
               {
                   parser.error ("Illegal assignment from long long");
               }
            }
        }
    }

    public String toString()
    {
       return escapeBackslash (string);
    }

    public void print( PrintWriter ps )
    {
       ps.print( string );
    }


   /**
    * Doubles up instances of the backslash character in a
    * string, to avoid them being interpreted as escape sequences
    *
    * @param name a <code>String</code> value
    * @return string
    */
   public static String escapeBackslash (String name)
   {
      StringBuffer result = new StringBuffer();

      char[] chrs = name.toCharArray();

      // Don't bother escaping if we have "xxx"
      if (chrs[0] == '\"')
      {
         return name;
      }

      for (int i=0; i<chrs.length; i++)
      {
         switch (chrs[i])
         {
            case '\n':
            {
               result.append ('\\');
               result.append ('n');
               break;
            }
            case '\t':
            {
               result.append ('\\');
               result.append ('t');
               break;
            }
            case '\013':
            {
               result.append ('\\');
               result.append ("013");
               break;
            }
            case '\b':
            {
               result.append ('\\');
               result.append ('b');
               break;
            }
            case '\r':
            {
               result.append ('\\');
               result.append ('r');
               break;
            }
            case '\f':
            {
               result.append ('\\');
               result.append ('f');
               break;
            }
            case '\007':
            {
               result.append ('\\');
               result.append ("007");
               break;
            }
            case '\\':
            {
               result.append ('\\');
               result.append ('\\');
               break;
            }
            case '\0':
            {
               result.append ('\\');
               result.append ('0');
               break;
            }
            case '\'':
            {
               if (i == 1)
               {
                  result.append ('\\');
                  result.append ('\'');
               }
               else
               {
                  result.append (chrs[i]);
               }
               break;
            }
            case '\"':
            {
               if (i == 1)
               {
                  result.append ('\\');
                  result.append ('\"');
               }
               else
               {
                  result.append (chrs[i]);
               }
               break;
            }
            default:
            {
               result.append (chrs[i]);
            }
         }
      }
      return result.toString();
   }
}
