/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class FixedPointType
    extends TemplateTypeSpec
{
    public ConstExpr digit_expr = null;
    public ConstExpr scale_expr = null;
    public int digits = 0;
    public int scale = 0;

    public FixedPointType(int num)
    {
        super(num);
    }

    public Object clone()
    {
        FixedPointType ft = new FixedPointType(new_num());
        ft.name = this.name;
        ft.pack_name = this.pack_name;
        ft.digit_expr = this.digit_expr;
        ft.digits = this.digits;
        ft.scale_expr = this.scale_expr;
        ft.scale = this.scale;
        ft.included = this.included;
        ft.typedefd = this.typedefd;
        ft.set_token(this.get_token());
        ft.setEnclosingSymbol(this.getEnclosingSymbol());
        return ft;
    }

    public String helperName()
    {
        if (pack_name.length() > 0)
        {
            String s = ScopedName.unPseudoName(pack_name + "." + name);

            return getFullName(s);
        }
        else
        {
            return ScopedName.unPseudoName(name);
        }
    }

    public String typeName()
    {
        return "java.math.BigDecimal";
    }


    public int getTCKind()
    {
        return org.omg.CORBA.TCKind._tk_fixed;
    }


    public TypeSpec typeSpec()
    {
        return this;
    }


    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
    }


    private void printHelperClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
            ps.println("package " + pack_name + ";");

        ps.println("public" + parser.getFinalString() + " class " + className + "Helper");
        ps.println("{");
        ps.println("\tprivate static org.omg.CORBA.TypeCode _type = " + getTypeCodeExpression() + ";");

        ps.println("\tpublic static void insert(org.omg.CORBA.Any any, java.math.BigDecimal s)");
        ps.println("\t{");
        ps.println("\t\tany.insert_fixed(s, type());");
        ps.println("\t}");

        ps.println("\tpublic static java.math.BigDecimal extract(org.omg.CORBA.Any any)");
        ps.println("\t{");
        ps.println("\t\treturn any.extract_fixed();");
        ps.println("\t}");

        ps.println("\tpublic static org.omg.CORBA.TypeCode type()");
        ps.println("\t{");
        ps.println("\t\treturn _type;");
        ps.println("\t}");

        printIdMethod(ps); // from IdlSymbol

        /** read */

        ps.println("\tpublic static java.math.BigDecimal read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\tjava.math.BigDecimal result = in.read_fixed();");
        ps.println("\t\treturn result.movePointLeft(" + scale + ");");
        ps.println("\t}");

        /** write */

        ps.println("\tpublic static void write (final org.omg.CORBA.portable.OutputStream out, final java.math.BigDecimal s)");
        ps.println("\t{");

        StringBuffer mb = new StringBuffer("1");
        for (int m = 0; m < digits - scale; m++)
            mb.append("0");

        ps.println("\t\tif (s.scale() != " + scale + ")");
        ps.println("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION();");

        ps.println("\t\tjava.math.BigDecimal max = new java.math.BigDecimal(\"" + mb.toString() + "\");");
        ps.println("\t\tif (s.compareTo(max) != -1)");
        ps.println("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION();");
        ps.println("\t\tout.write_fixed(s);");
        ps.println("\t}");

        ps.println("}");
    }

    public void print(java.io.PrintWriter pw)
    {
        try
        {
            // write helper file

            String fullName = helperName();
            String className;
            if (fullName.indexOf('.') > 0)
            {
                pack_name = fullName.substring(0, fullName.lastIndexOf('.'));
                className = fullName.substring(fullName.lastIndexOf('.') + 1);
            }
            else
            {
                pack_name = "";
                className = fullName;
            }

            String path = parser.out_dir + fileSeparator + pack_name.replace('.', fileSeparator);
            File dir = new File(path);
            if (!dir.exists())
            {
                if (!dir.mkdirs())
                {
                    org.jacorb.idl.parser.fatal_error("Unable to create " + path, null);
                }
            }

            String fname = className + "Helper.java";
            File f = new File(dir, fname);

            if (GlobalInputStream.isMoreRecentThan(f))
            {
                PrintWriter ps = new PrintWriter(new java.io.FileWriter(f));
                printHelperClass(className, ps);
                ps.close();
            }

        }
        catch(java.io.IOException i)
        {
            throw new RuntimeException("File IO error" + i);
        }
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    public String toString()
    {
        return typeName();
    }

    public String holderName()
    {
        return "org.omg.CORBA.FixedHolder";
    }

    public String getTypeCodeExpression()
    {
        return "org.omg.CORBA.ORB.init().create_fixed_tc((short)" + digits + ",(short)" + scale + ")";
    }

    public String getTypeCodeExpression(Set knownTypes)
    {
        return getTypeCodeExpression();
    }

    public String printReadExpression(String strname)
    {
        return helperName() + "Helper.read(" + strname + ")";
    }

    public String printReadStatement(String var_name, String strname)
    {
        String fixedName = null;
        if (hashCode() > 0)
            fixedName = "_fixed" + hashCode();
        else
            fixedName = "_fixed" + (-1 * hashCode());

        StringBuffer sb = new StringBuffer();

        if (parser.generatedHelperPortability == parser.HELPER_DEPRECATED)
        {
            sb.append("\t\tjava.math.BigDecimal " + fixedName + "=" + strname + ".read_fixed();" + Environment.NL);
            sb.append("\t\t" + var_name + " = " + fixedName + ".movePointLeft(" + scale + ");" + Environment.NL);
        }
        else if (parser.generatedHelperPortability == parser.HELPER_PORTABLE)
        {
            sb.append("\t\t" + var_name + "=" + strname + ".read_fixed((short)"
                      + digits + ", (short)" + scale+ ");" + Environment.NL);
        }
        else if (parser.generatedHelperPortability == parser.HELPER_JACORB)
        {
            sb.append("\t\t" + var_name + "=((org.jacorb.orb.CDRInputStream)"
                      + strname + ").read_fixed((short)" + digits
                      + ", (short)" + scale + ");" + Environment.NL);
        }
        else
        {
            assert false;
        }

        return sb.toString();
    }

    public String printWriteStatement(String var_name, String strname)
    {
        StringBuffer mb = new StringBuffer("1");
        for (int m = 0; m < digits - scale; m++)
            mb.append("0");

        StringBuffer sb = new StringBuffer();
        sb.append(Environment.NL);
        sb.append("\t\tif (" + var_name + ".scale() != " + scale + ")" + Environment.NL);
        sb.append("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION(\"wrong scale in fixed point value, expecting "
                  + scale + ", got \" + " + var_name + ".scale());" + Environment.NL);

        String max = null;
        if (hashCode() > 0)
            max = "_max" + hashCode();
        else
            max = "_max" + (-1 * hashCode());

        sb.append("\t\tjava.math.BigDecimal " + max + "= new java.math.BigDecimal(\""
                  + mb.toString() + "\");" + Environment.NL);
        sb.append("\t\tif (" + var_name + ".compareTo(" + max + ") != -1)" + Environment.NL);
        sb.append("\t\t\tthrow new org.omg.CORBA.DATA_CONVERSION(\"more than " + digits
                  + " digits in fixed point value\");" + Environment.NL);

        if (parser.generatedHelperPortability == parser.HELPER_DEPRECATED)
        {
            sb.append("\t\t" + strname + ".write_fixed(" + var_name + ");" + Environment.NL);
        }
        else if (parser.generatedHelperPortability == parser.HELPER_PORTABLE)
        {
            sb.append("\t\t" + strname + ".write_fixed(" + var_name
                      + ", (short)" + digits + ", (short)" + scale + ");" + Environment.NL);
        }
        else if (parser.generatedHelperPortability == parser.HELPER_JACORB)
        {
            sb.append("\t\t((org.jacorb.orb.CDROutputStream)" + strname
                      + ").write_fixed(" + var_name + ", (short)" + digits
                      + ", (short)" + scale + ");" + Environment.NL);
        }
        else
        {
            assert false;
        }

        return sb.toString();
    }

    public void parse()
    {
        digits = digit_expr.pos_int_const();
        scale = scale_expr.pos_int_const();

        if ((scale < 0) || (digits <= 0) || (scale > digits))
        {
            parser.error("Error in fixed point type " + typeName() +
                          ", invalid format: <" + digits + "," + scale + ">");
        }
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        String fullName = helperName();
        String className;
        if (fullName.indexOf('.') > 0)
        {
            className = fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        else
        {
            className = fullName;
        }
        String helpername = className + "Helper";

        ps.println("\t\t" + helpername + ".insert("+anyname + ", "+ varname +" )");
    }

    public void printExtractResult(PrintWriter ps,
                                    String resultname,
                                    String anyname,
                                    String resulttype)
    {
        throw new RuntimeException ( "DII-stubs not completely implemented for fixed-point types" );

    }

}
