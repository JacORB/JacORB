package org.jacorb.idl;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2007 The JacORB Team
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

import java.io.PrintWriter;
import java.util.List;

/**
 * A plugin interface that allows to insert custom code (to help reuse structs and unions) 
 * into the generated java code. The following IDL is to help illustrate the code insertion 
 * points.
 * <code>
 * module test
 * {
 *   struct MyStruct
 *   {
 *       long foo;
 *   };
 *
 *   union MyUnion switch(long)
 *   {
 *     case 1: long foo;
 *     case 2: MyStruct bar;    
 *   };
 *
 *   interface MyIntface
 *   {
 *       void op(in MyStruct arg1, inout MyUnion arg2);
 *   };
 * };
 *</code>
 * @author nico
 */
public interface ObjectCachePlugin
{
    /**
     * Code insertion point right in the beginning of a struct or union 
     * helper's read method.<br/>
     * From MyUnionHelper.read(): <br/>
     * <code>   
     public static MyUnion read (org.omg.CORBA.portable.InputStream in)
     {
//  ----> code from printCheckout() goes here, instead of "MyUnion result = new MyUnion();" <----

          int disc=in.read_long();
          switch (disc)
          {
             case 1:
             {
                int _var;
                _var=in.read_long();
                result.foo (_var);
                break;
             }
             case 2:
             {
                test.MyStruct _var;
                _var=test.MyStructHelper.read(in);
                result.bar (_var);
                break;
             }
             default: result.__default (disc);
          }
       return result;
     }
     * </code>
     */
    void printCheckout(PrintWriter ps, String className, String variableName);
    
    /**
     * Code insertion poin after invoking the servant.<br/>
     * From <tt>MyInterfacePOA.invoke()</tt> <br/>
     * <code>
     * switch ( opsIndex.intValue() )
       {
          case 0: // op
          {            
             test.MyStruct _arg0=test.MyStructHelper.read(_input);
             test.MyUnionHolder _arg1= new test.MyUnionHolder();
             _arg1._read (_input);
             _out = handler.createReply();
             op(_arg0,_arg1);
             test.MyUnionHelper.write(_out,_arg1.value);
             
//  ----> code from printSkeletonCheckin() goes here <----

             break;
          }
       }
     * </code>
     */
    void printSkeletonCheckin(PrintWriter ps, List paramDecls, String variablePrefix);
    
    /**
     * Code insertion point to add methods to a struct's or union's helper class.
     */
    void printCheckinHelper(PrintWriter ps, TypeDeclaration decl);
    
    /**
     * Code insertion point in the skeleton before reading the parameters. <br/>
     * From <tt>MyInterfacePOA.invoke()</tt> <br/>
     * <code>
     * switch ( opsIndex.intValue() )
       {
          case 0: // op
          {
//  ----> code from printPreParamRead() goes here <----
              
             test.MyStruct _arg0=test.MyStructHelper.read(_input);
             test.MyUnionHolder _arg1= new test.MyUnionHolder();
             _arg1._read (_input);
             _out = handler.createReply();
             op(_arg0,_arg1);
             test.MyUnionHelper.write(_out,_arg1.value);

             break;
          }
       }
     * </code>
     */
    void printPreParamRead(PrintWriter ps, List paramDecls);
    
    /**
     * Code insertion point in the skeleton after reading the parameters, 
     * but before invoking the servant. <br/>
     * From <tt>MyInterfacePOA.invoke()</tt> <br/>
     * <code>
     * switch ( opsIndex.intValue() )
       {
          case 0: // op
          {
             test.MyStruct _arg0=test.MyStructHelper.read(_input);
             test.MyUnionHolder _arg1= new test.MyUnionHolder();
             _arg1._read (_input);
             
//  ----> code from printPostParamRead() goes here <----
              
             _out = handler.createReply();
             op(_arg0,_arg1);
             test.MyUnionHelper.write(_out,_arg1.value);
             break;
          }
       }
     * </code>
     */
    void printPostParamRead(PrintWriter ps, List paramDecls);
    
    /**
     * Code insertion point in the read method of a helper class of a 
     * struct or union before reading struct/union members.<br/>
     * From MyUnionHelper.read(): <br/>
     * <code>   
     public static MyUnion read (org.omg.CORBA.portable.InputStream in)
     {      
       MyUnion result = new MyUnion();
       
//  ----> code from printPreMemberRead() goes here <----
 
          int disc=in.read_long();
          switch (disc)
          {
             case 1:
             {
                int _var;
                _var=in.read_long();
                result.foo (_var);
                break;
             }
             case 2:
             {
                test.MyStruct _var;
                _var=test.MyStructHelper.read(in);
                result.bar (_var);
                break;
             }
             default: result.__default (disc);
          }
       return result;
     }
     * </code>
     */
    void printPreMemberRead(PrintWriter ps, TypeDeclaration decl);
    
    /**
     * Code insertion point in the read method of a helper class of a 
     * struct or union after reading struct/union members.<br/><br/>
     * From MyUnionHelper.read(): <br/>
     * <code>   
     public static MyUnion read (org.omg.CORBA.portable.InputStream in)
     {      
          MyUnion result = new MyUnion();
          int disc=in.read_long();
          switch (disc)
          {
             case 1:
             {
                int _var;
                _var=in.read_long();
                result.foo (_var);
                break;
             }
             case 2:
             {
                test.MyStruct _var;
                _var=test.MyStructHelper.read(in);
                result.bar (_var);
                break;
             }
             default: result.__default (disc);
          }

//  ----> code from printPostMemberRead() goes here <----
        
         return result;
     }
     * </code>
     */
    void printPostMemberRead(PrintWriter ps, TypeDeclaration decl, String variableName);
}
