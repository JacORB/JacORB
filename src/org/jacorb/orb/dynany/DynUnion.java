package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose, FU Berlin.
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

import org.omg.DynamicAny.NameValuePair;
import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;
import org.omg.DynamicAny.*;

import org.jacorb.orb.*;
import org.omg.CORBA.TCKind;

/**
 * CORBA DynUnion
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id$
 *
 */

public final class DynUnion
   extends DynAny
   implements org.omg.DynamicAny.DynUnion
{
   private org.omg.CORBA.Any discriminator;
   private org.omg.DynamicAny.DynAny member;
   private String member_name;
   private int member_index;
    
   /** 
    * constructor from TypeCode
    */
    
   DynUnion( org.omg.DynamicAny.DynAnyFactory dynFactory,
             org.omg.CORBA.TypeCode tc )
      throws InvalidValue, TypeMismatch
   {
      org.jacorb.orb.TypeCode _type = 
         ((org.jacorb.orb.TypeCode)tc).originalType();

      if( _type.kind() != org.omg.CORBA.TCKind.tk_union )
         throw new TypeMismatch();

      type = _type;

      this.orb = org.omg.CORBA.ORB.init();
      this.dynFactory = dynFactory;

      pos = 0;
      limit = 2;

      try
      {
         for( int i = 0; i < type.member_count(); i++ )
         {
            discriminator = type.member_label(i);

            if( discriminator.type().kind().value() != 
                org.omg.CORBA.TCKind._tk_octet )
            {
               break;
            }
         }

         // rare case when the union only has a default case label
         if( discriminator.type().kind().value() ==
             org.omg.CORBA.TCKind._tk_octet )
         {
            try
            {
               set_to_unlisted_label ();
            }
            catch (TypeMismatch ex)
            {
               // should never happen         
               ex.printStackTrace ();
            }
         }

         select_member();
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         bk.printStackTrace();
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {
         b.printStackTrace();
      }   	
   }

   /**
    * @overrides from_any() in DynAny
    */

   public void from_any( org.omg.CORBA.Any value ) 
      throws InvalidValue, TypeMismatch
   {
      checkDestroyed ();
      if( ! type().equivalent( value.type() ))
         throw new TypeMismatch();

      try
      {
         type = ((org.jacorb.orb.TypeCode)value.type()).originalType();
         super.from_any( value );

         limit = 2;
         org.omg.CORBA.portable.InputStream is = 
            value.create_input_stream();

         discriminator = org.omg.CORBA.ORB.init().create_any();
         discriminator.type( type().discriminator_type());
            
         discriminator.read_value(is, type().discriminator_type());

         org.omg.CORBA.Any member_any = null;
         for( int i = 0; i < type ().member_count (); i++ )
         {
            if( type().member_label(i).equal( discriminator ))
            {
               member_any = org.omg.CORBA.ORB.init().create_any();
               member_any.read_value( is, type().member_type(i) );
               member_name = type().member_name(i);
               member_index = i;
               break;		   
            }
         }

         if( member_any == null )
         {
            int def_idx = type().default_index();
            if( def_idx != -1 )
            {
               member_any = org.omg.CORBA.ORB.init().create_any();
               member_any.read_value( is, type().member_type(def_idx) );
               member_name = type().member_name(def_idx);
               member_index = def_idx;
            }
         }

         if( member_any != null )
         {
            try
            {
               member = dynFactory.create_dyn_any( member_any );
            }
            catch( InconsistentTypeCode itc )
            {
               // should never happen
               itc.printStackTrace();
            }		
         }

         org.jacorb.util.Debug.output( 3, "DynUnion.from_any(), member == null? " + 
                                       ( member == null ));
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {
         b.printStackTrace();
      }   	
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }
   }

   /** 
    * @return an Any that holds a copy of this union
    */

   public org.omg.CORBA.Any to_any() 
   {
      checkDestroyed ();
      CDROutputStream os = new CDROutputStream();

      os.write_value( discriminator.type(), 
                      (CDRInputStream)discriminator.create_input_stream() );

      os.write_value( member.type(), 
                      (CDRInputStream) member.to_any().create_input_stream());

      org.jacorb.orb.Any out_any = 
         (org.jacorb.orb.Any)org.omg.CORBA.ORB.init().create_any();

      out_any.type( type() );
      out_any.read_value( new CDRInputStream( orb, os.getBufferCopy()), type());
      return out_any;
   }

   /**
    * @overrides component_count() in DynAny
    */
   public int component_count ()
   {
      if (has_no_active_member ())
      {
         return 1;
      }
      return limit;
   }
   
   /**
    * @overrides next() in DynAny
    */
   public boolean next()
   {
      checkDestroyed ();
      if( pos < component_count () - 1 )
      {
         pos++;
         return true;
      }
      pos = -1;
      return false;
   }

   /**
    * @overrides seek() in DynAny
    */
   public boolean seek(int index)    
   {
      checkDestroyed ();
      if( index < 0 )
      {
         pos = -1;
         return false;
      }
      if( index < component_count () )
      {
         pos = index;
         return true;
      }
      pos = -1;
      return false;
   }

   
   /**
    * @overrides  equal() in DynAny
    */
   public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
   {
      checkDestroyed ();
      if( !type().equal( dyn_any.type())  )
         return false;

      org.omg.DynamicAny.DynUnion other =  DynUnionHelper.narrow( dyn_any );

      if( ! get_discriminator().equal( other.get_discriminator()))
         return false;

      if( !has_no_active_member() ) 
      {
         // other must have the same here because we know the 
         // discriminators are equal

         try
         {
            if( ! member.equal( other.member()))
               return false;
         }
         catch( Exception e )
         {
            // should not happen
            e.printStackTrace();
         }
      }
      return true;
   }



   /** 
    * @return the current discriminator value
    */

   public org.omg.DynamicAny.DynAny get_discriminator()
   {
      checkDestroyed ();
      try
      {
         return dynFactory.create_dyn_any( discriminator );
      }
      catch( InconsistentTypeCode itc )
      {
         // should never happen
         itc.printStackTrace();
      }
      return null;
   }

   /** 
    * sets the discriminator to d  
    * @throws  TypeMismatch if  the TypeCode of the d parameter 
    * is not equivalent to  the TypeCode of the union's discriminator
    */

   public void set_discriminator( org.omg.DynamicAny.DynAny d ) 
      throws TypeMismatch
   {
      checkDestroyed ();
      if( ! d.type().equivalent( discriminator.type() ))
      {
         throw new TypeMismatch();
      }
      discriminator = d.to_any();
      pos = 1;
      
      /* check if the new discriminator is consistent with the 
         currently active member. If not, select a new one */

      try
      {
         if (member_index == type.default_index ())
         {
            // default member, only change the member if a non-default
            // discriminator value is specified
            for (int i = 0; i < type.member_count (); i++)
            {
               if( type().member_label(i).equal( discriminator ))
               {
                  select_member();
                  break;
               }
            }
         }
         else
         {
            // non-default member, check if the member has changed
            if( ! type().member_label( member_index ).equal( discriminator ))
               select_member();
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {
         b.printStackTrace();
      }   	
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         bk.printStackTrace();
      }

      // set the current position to zero if there is no active member
      if (has_no_active_member ())
      {
         pos = 0;
      }      
   }

   /**
    * updates the private  instance variables member, member_name and
    * member_index according to the current discriminator value 
    */

   private void select_member()
   {
      member = null;
      try
      {
         /* search through all members and compare their label with
            the discriminator */
         for( int i = 0; i < type().member_count(); i++ )
         {
            if( type().member_label(i).equal( discriminator ))
            {
               try
               {
                  member = 
                     dynFactory.create_dyn_any_from_type_code( 
                                                              type().member_type(i));
               }
               catch( InconsistentTypeCode itc )
               {
                  itc.printStackTrace();
               }
               member_name = type().member_name(i);
               member_index = i;
               break;
            }
         }

         /* none found, use default, if there is one */

         if( member == null )
         {
            int def_idx = type().default_index();
            if( def_idx != -1 )
            {
               try
               {
                  member = 
                     dynFactory.create_dyn_any_from_type_code(type().member_type(def_idx));
               }
               catch( InconsistentTypeCode itc )
               {
                  itc.printStackTrace();
               }  
               member_name = type().member_name(def_idx);
               member_index = def_idx;
            }
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {
         b.printStackTrace();
      }   	
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }
   }

   /**
    * sets the discriminator to  a value that is consistent with the
    * value  of the  default case  of a union;  it sets  the current
    * position to  zero  and causes  component_count  to return  2.
    *
    * @throws TypeMismatch if the union  does not have an explicit 
    * default case.
    */

   public void set_to_default_member() 
      throws TypeMismatch
   {
      checkDestroyed ();
      try
      {
         int def_idx = type().default_index();
         if( def_idx == -1 )
            throw new TypeMismatch();
         pos = 0;

         // do nothing if the discriminator is already set to a default value
         if (member_index != def_idx)
         {
            try
            {
               set_to_unlisted_label ();
            }
            catch (TypeMismatch ex)
            {
               // should never happen         
               ex.printStackTrace ();
            }      
            select_member();
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }
   }

   /**
    * sets the  discriminator to a value that  does not correspond to
    * any of the  union's case labels; it sets the  current position 
    * to zero and     causes     component_count     to    return 1. 
    *
    * @throws TypeMismatch if the union  has an explicit default 
    * case or uses the entire range of discriminator values for 
    * explicit case labels.  
    */

   public void set_to_no_active_member() 
      throws TypeMismatch
   {
      checkDestroyed ();
      try
      {
         /* if there is a default index, we do have active members */
         if( type().default_index() != -1 )
            throw new TypeMismatch();
         pos = 0;

         // do nothing if discriminator is already set to no active member
         if (!has_no_active_member ())
         {
            set_to_unlisted_label ();
         }
      }
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }	
   }

   /* find a discriminator value that is not an explicit case label */
   private void set_to_unlisted_label ()
      throws TypeMismatch
   {
      try
      {
         switch( type().discriminator_type().kind().value() )
         {
         case TCKind._tk_boolean:
            {
               boolean found;
               boolean cur_bool;
               org.omg.CORBA.Any check_val = null;
               org.omg.CORBA.Any cur_label = null;

               for (int i = 0; i < 2; i++)
               {
                  if (i == 0)
                  {
                     cur_bool = true;
                  }
                  else
                  {
                     cur_bool = false;
                  }
                  check_val = orb.create_any ();
                  check_val.insert_boolean (cur_bool);

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( check_val.equal( type().member_label(j)) )
                     {
                        found = true;
                     }
                  }
               
                  if( !found )
                  {
                     // the value is not found among the union's label
                     discriminator = check_val;
                     return;
                  }
               }
               // no unused value found
               throw new TypeMismatch();
            }
         case TCKind._tk_char:
            {
               // assume there is a printable char not used as a label!
               boolean found;
               org.omg.CORBA.Any check_val = null;
               org.omg.CORBA.Any cur_label = null;

               // 33 to 126 defines a reasonable set of printable chars
               for (int i = 0; i < 127; i++)
               {
                  check_val = orb.create_any ();
                  check_val.insert_char ((char) i);

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( check_val.equal( type().member_label(j)) )
                     {
                        found = true;
                     }
                  }

                  if( !found )
                  {
                     // the value is not found among the union's label
                     discriminator = check_val;
                     return;
                  }                  
               }
               // no unused value found, should not happen
               throw new TypeMismatch();
            }
         case TCKind._tk_short:
            {	
               // assume there is an unsigned short not used as a label!
               boolean found;
               org.omg.CORBA.Any check_val = null;
               org.omg.CORBA.Any cur_label = null;

               short max_short = 32767;
               for (short i = 0; i < max_short; i++)
               {
                  check_val = orb.create_any ();
                  check_val.insert_short (i);

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( check_val.equal( type().member_label(j)) )
                     {
                        found = true;
                     }
                  }

                  if( !found )
                  {
                     // the value is not found among the union's label
                     discriminator = check_val;
                     return;
                  }                  
               }
               // no unused value found, should not happen
               throw new TypeMismatch();                
            }
         case TCKind._tk_long:
            {	
               // assume there is an unsigned int not used as a label!
               boolean found;
               org.omg.CORBA.Any check_val = null;
               org.omg.CORBA.Any cur_label = null;

               int max_int = 2147483647;
               for (int i = 0; i < max_int; i++)
               {
                  check_val = orb.create_any ();
                  check_val.insert_long (i);

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( check_val.equal( type().member_label(j)) )
                     {
                        found = true;
                     }
                  }

                  if( !found )
                  {
                     // the value is not found among the union's label
                     discriminator = check_val;
                     return;
                  }                  
               }
               // no unused value found, should not happen
               throw new TypeMismatch();                
            }
         case TCKind._tk_longlong:
            {	
               // assume there is an unsigned long not used as a label!
               boolean found;
               org.omg.CORBA.Any check_val = null;
               org.omg.CORBA.Any cur_label = null;

               long max_long = 2147483647; // this should be sufficient!
               for (long i = 0; i < max_long; i++)
               {
                  check_val = orb.create_any ();
                  check_val.insert_longlong (i);

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( check_val.equal( type().member_label(j)) )
                     {
                        found = true;
                     }
                  }

                  if( !found )
                  {
                     // the value is not found among the union's label
                     discriminator = check_val;
                     return;
                  }                  
               }
               // no unused value found, should not happen
               throw new TypeMismatch();                
            }
         case TCKind._tk_enum:
            {
               org.omg.DynamicAny.DynEnum enum = null;
               try
               {         
                  enum = (org.omg.DynamicAny.DynEnum) dynFactory.create_dyn_any_from_type_code (discriminator.type());
               }
               catch( InconsistentTypeCode it )
               {
                  it.printStackTrace();
               }   	

               boolean found;
               for( int i = 0; i < discriminator.type().member_count(); i++ )
               {    
                  try
                  {
                     enum.set_as_string( discriminator.type().member_name(i) );
                  }
                  catch( InvalidValue iv )
                  {
                     // should not happen anymore
                     iv.printStackTrace();
                  }

                  found = false; // is the value used as a label?
                  for( int j = 0; j < type().member_count() && !found; j++ )
                  {
                     if( enum.to_any ().equal( type().member_label(j) ) )
                     {
                        found = true;
                     }
                  }

                  if( !found )
                  {
                     // the enum value is not found among the union's label
                     discriminator = enum.to_any();
                     return;
                  }                    
               }
               // no unused value found
               throw new TypeMismatch();                
            }
         default:
            throw new TypeMismatch();
         }
      }
      catch (org.omg.CORBA.TypeCodePackage.BadKind bk)
      {
         // should never happen
         bk.printStackTrace ();
      }
      catch (org.omg.CORBA.TypeCodePackage.Bounds b)
      {
         // should never happen
         b.printStackTrace ();
      }
   }
   
   /** 
    * @returns true, if the union  has no active member (that is, the
    * union's  value consists solely of  its discriminator because
    * the discriminator has a value that is not listed as an explicit
    * case label).  Calling this  operation on  a union  that  has a
    * default case  returns false. Calling this operation  on a union
    * that uses the entire range of discriminator values for explicit
    * case labels returns false.
    */

  public boolean has_no_active_member()
   {
      checkDestroyed ();
      try
      {
         if( type().default_index() != -1 )
         {
            return false;
         }

         for( int i = 0; i < type.member_count(); i++ )
         {
            if( discriminator.equal( type.member_label(i) ))
            {
               return false;
            }
         }
         return true;
      }
      catch( org.omg.CORBA.TypeCodePackage.Bounds b )
      {
         // should not happen anymore
         b.printStackTrace();
      }	
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
      {
         // should not happen anymore
         bk.printStackTrace();
      }	
      return( member == null );
   }

   /** 
    * @returns the TCKind value of the discriminator's TypeCode.
    */

   public org.omg.CORBA.TCKind discriminator_kind()
   {
      checkDestroyed ();
      return discriminator.type().kind();
   }

   /** 
    * @returns the currently active member. 
    * @throws InvalidValue if the union has no active  member
    */

   public org.omg.DynamicAny.DynAny member() 
      throws InvalidValue
   {
      checkDestroyed ();
      if( has_no_active_member() )
         throw new InvalidValue();

      return member;
   }

   /** 
    * @returns the TypeCode kind of the currently active member. 
    * @throws InvalidValue if the union has no active  member
    */

   public org.omg.CORBA.TCKind member_kind() 
      throws InvalidValue
   {
      checkDestroyed ();
      if( has_no_active_member() )
         throw new InvalidValue();

      return member.type().kind();
   }

   /** 
    * @returns the name of the currently active member. 
    * @throws InvalidValue if the union has no active member
    */

   public String member_name() 
      throws InvalidValue
   {
      checkDestroyed ();
      if( has_no_active_member() )
         throw new InvalidValue();
      return member_name;
   }

   public void destroy()
   {
      super.destroy();
      discriminator = null;
      member = null;
      member_name = null;
      member_index = -1;
   }

   /* iteration interface */

   public org.omg.DynamicAny.DynAny current_component()
   {	
      checkDestroyed ();
      if( pos == -1 )
         return null;

      if( pos == 0 )
         return get_discriminator();
      else
      {
         try
         {
            return member();
         }
         catch( org.omg.DynamicAny.DynAnyPackage.InvalidValue iv )
         {
            iv.printStackTrace();
         }
         return null;
      }
   }

}
