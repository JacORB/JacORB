package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
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

import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.*;
import org.jacorb.orb.*;

import java.math.BigDecimal;

/**
 * CORBA DynFixed
 *
 * Written by Jason Courage
 *
 * @author Jason Courage, PrismTech Ltd, March 2002 
 * $Id$
 *
 */

public final class DynFixed
   extends DynAny
   implements org.omg.DynamicAny.DynFixed
{
   /* our representation of a fixed type any is the any itself */
   private org.omg.CORBA.Any anyRepresentation = null;


   DynFixed( org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.omg.CORBA.TypeCode tc)
      throws TypeMismatch
   {
      org.omg.CORBA.TypeCode _type = TypeCode.originalType( tc );

      if( _type.kind().value() != org.omg.CORBA.TCKind._tk_fixed )
         throw new TypeMismatch();

      type = _type;
      this.orb = org.omg.CORBA.ORB.init();
      this.dynFactory = dynFactory;
      pos = -1;

      anyRepresentation = orb.create_any ();
      anyRepresentation.insert_fixed (new BigDecimal ("0"), tc);
   }

   /**
    * Overrides from_any() in DynAny
    */

   public void from_any(org.omg.CORBA.Any value) 
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! value.type().equivalent( type()) )
         throw new TypeMismatch();

      type = TypeCode.originalType( value.type() );

      try
      {
         anyRepresentation = (org.jacorb.orb.Any)orb.create_any();
         anyRepresentation.read_value( value.create_input_stream(), type());
      }
      catch( Exception e)
      {
         e.printStackTrace();
         throw new InvalidValue();
      }
   }

   public String get_value()
   {
      return anyRepresentation.extract_fixed().toString();
   }

   public boolean set_value( String val )
      throws TypeMismatch, InvalidValue
   {
      if ( val == null )
      {
         throw new TypeMismatch();
      }

      val = val.trim();
      if ( val.endsWith ("D") || val.endsWith ("d") )
      {
         val = val.substring( 0, val.length() - 1 );
      }

      BigDecimal fixed_value = null;
      try
      {
         fixed_value = new BigDecimal( val );
      }
      catch ( NumberFormatException ex )
      {
         throw new TypeMismatch();
      }
      
      boolean truncate = false;
      try
      {
         int extra = fixed_value.scale() - type().fixed_scale();
         if ( extra > 0 )
         {
            // truncate the value to fit the scale of the typecode
            val = val.substring( 0, val.length() - extra );
            truncate = true;
         }
         else if ( extra < 0 )
         {
            StringBuffer sb = new StringBuffer (val);

            // add the decimal point if necessary
            if ( val.indexOf('.') == -1 )
            {
               sb.append(".");
            }

            // pad the value with zeros to fit the scale of the typecode
            for ( int i = extra; i < 0; i++ )
            {
               sb.append("0");
            }
            val = sb.toString();
         }
         fixed_value = new BigDecimal( val );
      
         org.omg.CORBA.FixedHolder holder =
            new org.omg.CORBA.FixedHolder( fixed_value );
         org.omg.CORBA.TypeCode tc = holder._type();

         if ( tc.fixed_digits() > type().fixed_digits() )
         {
            throw new InvalidValue();
         }
         anyRepresentation.insert_fixed( fixed_value, tc );
      }
      catch ( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
          bk.printStackTrace();
         // should never happen
      }
      return( ! truncate );
   }
   
   protected org.omg.CORBA.Any getRepresentation()
   {
      return anyRepresentation;
   }

}
