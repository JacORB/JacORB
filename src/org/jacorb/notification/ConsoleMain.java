package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.util.Properties;
import java.util.StringTokenizer;

import org.jacorb.notification.conf.Attributes;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class ConsoleMain
{
    private static void help()
    {
        System.out.println("Usage: ntfy [-printIOR] [-printCorbaloc] " + "[-writeIOR <filename>] "
                + "[-registerName <nameId>[.<nameKind>]] "
                + "[-port <oaPort>] [-channels <channels>] [-help]");
        
        System.exit(0);
    }

    private static class CmdLineParser
    {
        private boolean doHelp = false;

        private Properties props = new Properties();

        public boolean getDoHelp()
        {
            return doHelp;
        }

        public Properties getProps()
        {
            return props;
        }

        CmdLineParser(String[] args)
        {
            perform(args);
        }

        private void perform(String[] args) throws IllegalArgumentException
        {
            try
            {
                // process arguments
                for (int i = 0; i < args.length; i++)
                {
                    if (args[i].equals("-printIOR"))
                    {
                        props.put(Attributes.PRINT_IOR, "on");
                    }
                    else if (args[i].equals("-printCorbaloc"))
                    {
                        props.put(Attributes.PRINT_CORBALOC, "on");
                    }
                    else if (args[i].equals("-help"))
                    {
                        doHelp = true;
                    }
                    else if (args[i].equals("-port"))
                    {
                        props.put("OAPort", args[++i]);
                    }
                    else if (args[i].equals("-channels"))
                    {
                        props.put(Attributes.START_CHANNELS, args[++i]);
                    }
                    else if (args[i].equals("-writeIOR"))
                    {
                        props.put(Attributes.IOR_FILE, args[++i]);
                    }
                    else if (args[i].equals("-registerName"))
                    {
                        String name = args[++i];

                        addCOSNamingName(props, name);
                    }
                    else if (args[i].equals("-typed"))
                    {
                        props.put(Attributes.ENABLE_TYPED_CHANNEL, "on");
                    }
                    else
                    {
                        System.out.println("Unknown argument: " + args[i]);

                        doHelp = true;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e)
            {
                doHelp = true;
            }
        }
    }

    public static void addCOSNamingName(Properties props, String name)
    {
        int index = name.indexOf(".");
        if (name.lastIndexOf(".") != index)
        {
            throw new IllegalArgumentException(name
                    + ": argument to -registerName should be "
                    + "<nameId> or <nameId>.<nameKind>");
        }
        if (index != -1)
        {
            props.put(Attributes.REGISTER_NAME_ID, name.substring(0, index));

            props.put(Attributes.REGISTER_NAME_KIND, name.substring(index + 1));
        }
        else
        {
            props.put(Attributes.REGISTER_NAME_ID, name);
        }
    }
    
    public static AbstractChannelFactory newFactory(String[] args) throws Exception
    {
        Properties props = parseProperties(args);
        
        return AbstractChannelFactory.newFactory(props);
    }

    public static Properties parseProperties(String[] args)
    {
        if (args == null)
        {
            return new Properties();
        }
        
        CmdLineParser _cmdLineParser = new CmdLineParser(args);

        if (_cmdLineParser.getDoHelp())
        {
            help();

            System.exit(0);
        }
        
        return _cmdLineParser.getProps();
    }
    
    public static String[] splitArgs(String argString)
    {
        if (argString == null)
        {
            return new String[0];
        }
        
        StringTokenizer tokenizer = new StringTokenizer(argString, " ");
        String[] result = new String[tokenizer.countTokens()];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = tokenizer.nextToken();
        }
        
        return result;
    }
    
    public static final void main(String[] args) throws Exception
    {
        newFactory(args);
    }
}