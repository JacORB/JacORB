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
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.orb.*;

import java.math.BigDecimal;

/**
 * CORBA DynFixed
 *
 * @author Jason Courage
 * @version $Id$
 */

public final class DynFixed
   extends DynAny
   implements org.omg.DynamicAny.DynFixed
{
   /**
    *  our representation of a fixed type any is the any itself
    */
   private org.omg.CORBA.Any anyRepresentation;

   DynFixed( org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.omg.CORBA.TypeCode type,
             ORB orb,
             Logger logger)
      throws TypeMismatch
   {
       super(dynFactory, orb, logger);

      org.omg.CORBA.TypeCode _type = TypeCode.originalType( type );

      if( _type.kind().value() != org.omg.CORBA.TCKind._tk_fixed )
      {
         throw new TypeMismatch();
      }

      typeCode = _type;
      pos = -1;

      anyRepresentation = orb.create_any ();
      anyRepresentation.insert_fixed (new BigDecimal ("0"), type);
   }

   /**
    * Overrides from_any() in DynAny
    */

   public void from_any(org.omg.CORBA.Any value)
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! value.type().equivalent( type()) )
      {
         throw new TypeMismatch();
      }

      typeCode = TypeCode.originalType( value.type() );

      try
      {
         anyRepresentation = (org.jacorb.orb.Any)orb.create_any();
         anyRepresentation.read_value( value.create_input_stream(), type());
      }
      catch( Exception e)
      {
         throw new InvalidValue(e.getMessage());
      }
   }

   public String get_value()
   {
      return anyRepresentation.extract_fixed().toString();
   }

   public boolean set_value( String value )
      throws TypeMismatch, InvalidValue
   {
      if ( value == null )
      {
         throw new TypeMismatch();
      }
      String val = value.trim();
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
            StringBuffer buffer = new StringBuffer (val);

            // add the decimal point if necessary
            if ( val.indexOf('.') == -1 )
            {
               buffer.append('.');
            }

            // pad the value with zeros to fit the scale of the typecode
            for ( int i = extra; i < 0; i++ )
            {
               buffer.append('0');
            }
            val = buffer.toString();
         }
         fixed_value = new BigDecimal( val );

         org.omg.CORBA.FixedHolder holder =
            new org.omg.CORBA.FixedHolder( fixed_value );

         org.omg.CORBA.TypeCode type = holder._type();

         if ( type.fixed_digits() > type().fixed_digits() )
         {
            throw new InvalidValue();
         }
         anyRepresentation.insert_fixed( fixed_value, type );
      }
      catch ( org.omg.CORBA.TypeCodePackage.BadKind e )
      {
          throw unexpectedException(e);
      }

      return( ! truncate );
   }

   protected org.omg.CORBA.Any getRepresentation()
   {
      return anyRepresentation;
   }
}
