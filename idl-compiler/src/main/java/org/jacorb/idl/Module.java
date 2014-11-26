/*
 *        JacORB - a free Java ORB
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

package org.jacorb.idl;

/**
 * JacORB  IDL compiler classes
 *
 * @author Gerald Brose
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

/**
 * Note: a module's name is its package name!
 */

public class Module
    extends Declaration
    implements Scope
{
    public Definitions spec;

    private ScopeData scopeData;
    private String unreplacedName = null;

    public Module(int num)
    {
        super(num);
        pack_name = "";
    }

    public void setScopeData(ScopeData data)
    {
        scopeData = data;
    }

    public ScopeData getScopeData()
    {
        return scopeData;
    }

    public void setPackage(String s)
    {
        if (unreplacedName == null)
            unreplacedName = s;
        s = parser.pack_replace(s);

        if (pack_name.length() > 0)
        {
            pack_name = s + "." + pack_name;
            spec.setPackage(s);
        }
        else
        {
            pack_name = s;

            if (lexer.needsJavaEscape(this))
                pack_name = "_" + s;

            name = pack_name;
            spec.setPackage(pack_name);
        }
    }

    String full_name()
    {
        return pack_name;
    }

    public void set_included(boolean i)
    {
        included = i;
        spec.set_included(i);
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
        spec.setEnclosingSymbol(this);
    }

    public void parse()
    {
        try
        {
            NameTable.define(full_name(), IDLTypes.MODULE);
        }
        catch (NameAlreadyDefined nad)
        {
            parser.error("Module name " + full_name() + " already defined", token);
        }
        spec.parse();
    }

    public void print(PrintWriter ps)
    {
        if (parser.generateIR)
        {
            try
            {
                // Java Interface file

                String path =
                    parser.out_dir + fileSeparator + pack_name.replace('.', fileSeparator);
                File dir = new File(path);
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        org.jacorb.idl.parser.fatal_error("Unable to create " + path, null);
                    }
                }

                File f = new File(dir, "_" + originalModuleName() + "Module.java");
                if (GlobalInputStream.isMoreRecentThan(f))
                {

                    PrintWriter moduleWriter = new PrintWriter(new java.io.FileWriter(f ));
                    moduleWriter.println("package " + pack_name + ";" + Environment.NL);
                    moduleWriter.println("/**"  + Environment.NL 
                                         + "* IR module information, generated by the JacORB IDL compiler" 
                                         + Environment.NL + " */");
                    moduleWriter.println("public class _" + originalModuleName() + "Module {}");
                    moduleWriter.close();
                }
            }
            catch (IOException io)
            {
                parser.logger.log(Level.WARNING, "Exception: ", io);
            }
        }
        spec.print(ps);
    }

    /**
     * @return the original, unreplaced module name
     * (needed to build a repositoryID that is untouched by
     * the compiler option -i2jpackage
     */

    public String originalModuleName()
    {
        return unreplacedName;
    }

    public Definitions getDefinitions()
    {
        return spec;
    }

    /**
     */

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitModule(this);
    }

    public String toString()
    {
        return "module " + name;
    }
}
