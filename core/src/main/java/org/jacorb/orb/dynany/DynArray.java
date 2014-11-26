package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.orb.Any;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.TypeCode;
import org.omg.DynamicAny.DynArrayHelper;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.slf4j.Logger;

/**
 * CORBA DynArray
 *
 * @author Gerald Brose
 */

public final class DynArray
    extends DynAny
    implements org.omg.DynamicAny.DynArray
{
    private org.omg.CORBA.TypeCode elementType;
    private org.omg.CORBA.Any[] members;

    DynArray( org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode type,
              org.jacorb.orb.ORB orb,
              Logger logger)
              throws TypeMismatch
   {
        super(dynFactory, orb, logger);

        org.omg.CORBA.TypeCode _type =
            TypeCode.originalType( type );

        if(  _type.kind() != org.omg.CORBA.TCKind.tk_array )
        {
            throw new TypeMismatch();
        }

        try
        {
            typeCode = _type;
            elementType = TypeCode.originalType( typeCode.content_type() );

            limit = typeCode.length();
            members = new Any[limit];
            try
            {
                for( int i = limit; i-- > 0;)
                {
                    members[i] = dynFactory.create_dyn_any_from_type_code( elementType ).to_any();
                }
            }
            catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
            {
                throw unexpectedException(e);
            }
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind e )
        {
            throw unexpectedException(e);
        }
   }

   public void from_any(org.omg.CORBA.Any value)
      throws InvalidValue, TypeMismatch
   {
       from_any_internal (false, value);
   }

    void from_any_internal( boolean useCurrentRepresentation, org.omg.CORBA.Any value )
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! typeCode.equivalent( value.type() ))
      {
         throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
      }

      super.from_any_internal( useCurrentRepresentation, value );

      try
      {
         limit = type().length();
         elementType = TypeCode.originalType( typeCode.content_type() );

         if( limit > 0 )
         {
            pos = 0;
         }

         org.omg.CORBA.portable.InputStream in = value.create_input_stream();
         members = new org.omg.CORBA.Any[limit];

         for( int i = 0 ; i < limit; i++ )
         {
            members[i] = orb.create_any();
            members[i].read_value(in, elementType);
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind e )
      {
          throw unexpectedException(e);
      }
   }

   public org.omg.CORBA.Any to_any()
   {
       checkDestroyed ();
       final org.omg.CORBA.Any out_any = orb.create_any();
       out_any.type( type());

       final CDROutputStream out = new CDROutputStream(orb);

       try
       {
           for( int i = 0; i < limit; i++)
           {
               out.write_value( elementType,
                       members[i].create_input_stream());
           }

           final CDRInputStream in = new CDRInputStream(orb, out.getBufferCopy());
           try
           {
               out_any.read_value( in, type());
               return out_any;
           }
           finally
           {
               in.close();
           }
       }
       finally
       {
           out.close();
       }
   }

   /**
     * Overrides  equal() in DynAny
     */

   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type())  )
      {
         return false;
      }

      org.omg.DynamicAny.DynArray other =  DynArrayHelper.narrow( dyn_any );

      org.omg.CORBA.Any[] elements = get_elements();
      org.omg.CORBA.Any[] other_elements = other.get_elements();

      for( int i = 0; i < elements.length; i++ )
      {
         if( ! (elements[i].equal( other_elements[i] )))
         {
            return false;
         }
      }

      return true;
   }


   public org.omg.CORBA.Any[] get_elements()
   {
      checkDestroyed ();
      return members;
   }


   public void set_elements(org.omg.CORBA.Any[] value)
      throws TypeMismatch, InvalidValue
   {
      checkDestroyed ();
      if( value.length != limit )
      {
         throw new InvalidValue();
      }

      for( int i = value.length; i-- > 0 ;)
      {
         org.omg.CORBA.TypeCode tc =
            TypeCode.originalType( value[i].type() );

         if( tc.kind() != elementType.kind() )
         {
            throw new TypeMismatch();
         }
      }

      /** ok now */
      members = value;
   }

   public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
   {
      checkDestroyed ();
      org.omg.DynamicAny.DynAny[] result =
         new org.omg.DynamicAny.DynAny[ members.length ];
      try
      {
         for( int i = members.length; i-- > 0; )
         {
            result[i] = dynFactory.create_dyn_any( members[i]);
         }
         return result;
      }
      catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
      {
          throw unexpectedException(e);
      }
   }


   public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[] value)
      throws TypeMismatch, InvalidValue
   {
      checkDestroyed ();
      org.omg.CORBA.Any [] any_seq =
         new org.omg.CORBA.Any[value.length];
      for( int i = value.length; i-- > 0; )
      {
         any_seq[i] = value[i].to_any();
      }

      set_elements( any_seq );
   }

   public void destroy()
   {
      super.destroy();
      members = null;
      elementType = null;
   }

   /**
     * Returns the DynAny's internal any representation.
     * <p>
     * Overrides getRepresentation() in DynAny
     */

   protected org.omg.CORBA.Any getRepresentation()
   {
      return members[pos];
   }

   /* iteration interface */

   public org.omg.DynamicAny.DynAny current_component()
   {
      checkDestroyed ();
      if( pos == -1 )
      {
         return null;
      }

      try
      {
          org.omg.DynamicAny.DynAny result = dynFactory.create_dyn_any_from_type_code (members[pos].type());
         ((org.jacorb.orb.dynany.DynAny)result).from_any_internal(true, members[pos]);
         return result;
      }
      catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
      {
          logger.error("unable to create DynAny", iv);
          throw unexpectedException(iv);
      }
      catch( org.omg.DynamicAny.DynAnyPackage.TypeMismatch itc )
      {
          logger.error("unable to create DynAny", itc);
          throw unexpectedException(itc);
      }
      catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e )
      {
          logger.error("unable to create DynAny", e);
          throw unexpectedException(e);
      }
   }
}
