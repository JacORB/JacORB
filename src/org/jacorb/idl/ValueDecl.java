/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

package org.jacorb.idl;

import java.util.*;
import java.io.*;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
class ValueDecl 
    extends Value
{
    private MemberList stateMembers;
    private List       operations;
    private List       exports;

    private boolean    isCustomMarshalled = false;
    
    public ValueDecl (int num)
    {
	super(num);
        stateMembers = new MemberList (new_num());
        operations   = new ArrayList();
        exports      = new ArrayList();
    }

    public void setValueElements (Definitions d)
    {
        for (Iterator i = d.v.iterator(); i.hasNext();) 
        {
            Declaration dec = ((Definition)(i.next())).get_declaration();
            if (dec instanceof StateMember)
                stateMembers.v.add (dec);
            else if (dec instanceof OpDecl)
                operations.add (dec);
            else
                exports.add (dec);
        }
    }

    public void isCustomMarshalled (boolean flag)
    {
        this.isCustomMarshalled = flag;
    }

    public boolean isCustomMarshalled()
    {
        return this.isCustomMarshalled;
    }

    public void setPackage (String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        stateMembers.setPackage (s);

        for (Iterator i = operations.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage (s);

        for (Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage (s);
    }

    public void parse()
    {	
        stateMembers.parse();
        for (Iterator i = operations.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).parse();
        for (Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).parse();
    }

    public void print (PrintWriter ps)
    {
        String path = parser.out_dir 
                      + fileSeparator 
                      + pack_name.replace ('.', fileSeparator);

        File dir = new File (path);

        if (!dir.exists())
            if (!dir.mkdirs())
                org.jacorb.idl.parser.fatal_error ("Unable to create " + path,
                                                   null);

        printClass (dir);
        printHelper (dir);
        printHolder (dir);
    }

    public String printWriteStatement (String var_name, String streamname)
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
             + ".write_value (" + var_name + ", " + javaName() + ".class);"
    }

    public String printReadExpression (String streamname)
    {
        return null; // delegate to Helper
    }

    public String printReadStatement (String var_name, String streamname)
    {
        return null; // delegate to Helper
    }

    private void printClassComment (PrintWriter out)
    {
        out.println("/**");
	out.println(" *\tGenerated from IDL definition of valuetype " + 
                    "\"" + name + "\"" );
        out.println(" *\t@author JacORB IDL compiler ");
        out.println(" */\n");
    }

    /**
     * Prints the abstract Java class to which this valuetype is mapped.
     */
    private void printClass (File dir)
    {
        File        outfile = new File (dir, name + ".java");
        PrintWriter out     = new PrintWriter (new FileWriter (outfile));

        if (pack_name.length() > 0)
            out.println ("package " + pack_name + ";\n");

        printClassComment (out);

        out.println ("public abstract class " + name);
        if (this.isCustomMarshalled())
            out.println ("\textends org.omg.CORBA.portable.CustomValue");
        else
            out.println ("\textends org.omg.CORBA.portable.StreamableValue");
        
        out.println ("{");

        for (Iterator i = stateMembers.v.iterator(); i.hasNext();)
            ((StateMember)i.next()).print (out);

        if (!this.isCustomMarshalled())
        {
            printWriteMethod (out);
            printReadMethod (out);
        }

        out.println ("}");
    }

    /**
     * Prints the _write() method required by 
     * org.omg.CORBA.portable.StreamableValue.
     */
    private void printWriteMethod (PrintWriter out)
    {
        out.println ("\tpublic void _write " + 
                     "(org.omg.CORBA.portable.OutputStream os");
        out.println ("\t{");
        for (Iterator = stateMembers.v.iterator(); i.hasNext();)
            out.print ("\t\t" + ((StateMember)i.next()).writeStatement("os"));
        out.println ("\t}\n");
    }

    /**
     * Prints the _read() method required by 
     * org.omg.CORBA.portable.StreamableValue.
     */
    private void printReadMethod (PrintWriter out)
    {
        out.println ("\tpublic void _read " + 
                     "(org.omg.CORBA.portable.InputStream os");
        out.println ("\t{");
        for (Iterator = stateMembers.v.iterator(); i.hasNext();)
            out.print ("\t\t" + ((StateMember)i.next()).readStatement("os"));
        out.println ("\t}\n");
    }

    private void printHelper (File dir)
    {
        File        outfile = new File (dir, name + "Helper.java");
        PrintWriter out     = new PrintWriter (new FileWriter (outfile));

        if (pack_name.length() > 0)
            out.println ("package " + pack_name + ";\n");

        printClassComment (out);

        out.println ("public abstract class " + name + "Helper");
        out.println ("{");
        out.println ("\tpublic static void insert " + 
                     "(org.omg.CORBA.Any a, " + javaName() + " v)");
        out.println ("\t{");
        out.println ("\t\ta.insert_Value (v, v._type());");
        out.println ("\t}");
        out.println ("\tpublic static " + javaName() + " extract " +
                     "(org.omg.CORBA.Any a)");
        out.println ("\t{");
        out.println ("\t\treturn a.extract_Value();");
        out.println ("\t}");
        out.println ("\tpublic static org.omg.CORBA.TypeCode type()");
        out.println ("\t{");
        out.println ("\t\treturn null;"); // FIXME
        out.println ("\t}");
        // ...
    }

    private void printHolder (File dir)
    {
        File        outfile = new File (dir, name + "Holder.java");
        PrintWriter out     = new PrintWriter (new FileWriter (outfile));

        if (pack_name.length() > 0)
            out.println ("package " + pack_name + ";\n");

        printClassComment (out);

        out.println ("public final class " + name + "Holder");
        out.println ("\timplements org.omg.CORBA.portable.Streamable");
        out.println ("{");
        out.println ("\tpublic " + javaName() + " value;");
        out.println ("\tpublic " + name + "Holder() {}");
        out.println ("\tpublic " + name + "Holder (final " 
                     + javaName() + " initial)");
        out.println ("\t{"); 
        out.println ("\t\tvalue = initial;");
        out.println ("\}");
        out.println ("\tpublic void _read " +
                     "(final org.omg.CORBA.portable.InputStream is)");
        out.println ("\t{");
        out.println ("\t\tvalue = " + javaName() + "Helper.read (is);");
        out.println ("\t}");
        out.println ("\tpublic void _write " +
                     "(final org.omg.CORBA.portable.OutputStream os)");
        out.println ("\t{");
        out.println ("\t\tvalue._write (os);");
        out.println ("\t}");
        out.println ("\tpublic org.omg.CORBA.TypeCode _type()");
        out.println ("\t{");
        out.println ("\t\treturn value._type();");
        out.println ("\t}");
    }

    

}
