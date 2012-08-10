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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

/**
 * This class deals with IDL input files and their inclusion relationships.
 *
 * @author Gerald Brose <mailto:gerald.brose@acm.org>
 * @version $Id$
 */

public class GlobalInputStream
{
    private static InputStream stream;
    private static Stack lookahead_stack;
    private static boolean included;
    private static StringBuffer expandedText;
    private static int pos;
    private static boolean eof;
    private static File currentFile;
    private static String[] path_names;
    private static org.jacorb.idl.util.IDLLogger logger;

    /** stack of information for lexical scopes */
    static java.util.Stack positions;

    public static void init()
    {
        lookahead_stack = new Stack();
        included = false;
        expandedText = new StringBuffer();
        pos = 0;
        eof = false;
        currentFile = null;
        positions = new java.util.Stack();
        logger = parser.getLogger();
    }

    public static void cleanUp()
    {
        stream = null;
        lookahead_stack = null;
        included = false;
        expandedText = null;
        pos = 0;
        eof = false;
        currentFile = null;
        path_names = null;
        logger = null;
    }

    public static void setInput(String fname)
        throws java.io.IOException
    {
        currentFile = new File(fname);
        stream = new java.io.FileInputStream(currentFile);
    }

    /**
     * Test if this input stream (or rather the underlying IDL file)
     * is more recent (was modified at a later time than) another
     * file.  (Used, e.g., to determine if code has been previosuly
     * generated from an IDL file).
     *
     * @param other the file to compare this stream against
     * @return true, if this stream's IDL file is more recent than the other file  .
     */

    public static boolean isMoreRecentThan(File other)
    {
        return (parser.forceOverwrite ||
                 (other.lastModified() < currentFile.lastModified())) ;
    }

    public static boolean includeState()
    {
        return included;
    }

    public static void insert(String str)
    {
        expandedText.insert(pos, str);
    }

    public static void include(String fname, int lookahead, boolean useIncludePath)
        throws FileNotFoundException
    {
        included = true;
        PositionInfo position = lexer.getPosition();
        position.file = currentFile();
        position.stream = stream;

        stream = find(fname, useIncludePath);

        positions.push(position);
        lookahead_stack.push(new Integer(lookahead));

        if (logger.isInfoEnabled())
            logger.info("Including " + fname);
        /* files form their own scopes, so we have to open a new one here */
    }

    public static void setIncludePath(String path)
    {
        java.util.StringTokenizer strtok =
                new java.util.StringTokenizer(path, File.pathSeparator);

        int i;
        if (path_names == null)
        {
            path_names = new String[ strtok.countTokens() ];
            i = 0;
        }
        else
        {
            i = path_names.length;

            String[] _path_names = new String[ strtok.countTokens() + path_names.length ];
            for (int j = 0; j < path_names.length; j++)
                _path_names[ j ] = path_names[ j ];
            path_names = _path_names;
        }

        while(strtok.hasMoreTokens())
        {
            path_names[ i++ ] = strtok.nextToken();
        }
    }

    /**
     *  tries to locate and open a new file for "fname",
     *  updates currentFile if successful
     */

    private static FileInputStream find(String fname, boolean useIncludePath)
            throws FileNotFoundException
    {
        if (!useIncludePath)
        {
            if (fname.indexOf(File.separator) != 0)
            {
                String dir = null;
                try
                {
                    dir = currentFile.getCanonicalPath();
                    if (dir.indexOf(File.separator) > -1)
                    {
                        dir = dir.substring(0, dir.lastIndexOf(File.separator));
                    }
                }
                catch(java.io.IOException ioe)
                {
                    logger.error("Caught error finding file ", ioe);
                }

                if (logger.isInfoEnabled())
                    logger.info("opening " + dir + File.separator + fname);
                currentFile = new File(dir + File.separator + fname);
            }
            else
                currentFile = new File(fname);

            try
            {
                return new FileInputStream(currentFile);
            }
            catch(java.io.IOException iof)
            {
                return find(fname, true);
            }
        }
        else
        {
            if (path_names == null)
            {
                org.jacorb.idl.parser.fatal_error("File " + fname +
                        " not found in include path", null);
            }
            else
            {
                for (int i = 0; i < path_names.length; i++)
                {
                    try
                    {
                        if (logger.isInfoEnabled())
                            logger.info("opening " + path_names[ i ] + File.separator + fname);

                        currentFile = new File(path_names[ i ] + File.separator + fname);
                        return new FileInputStream(currentFile);
                    }
                    catch(FileNotFoundException fnfex)
                    {
                    }
                }
            }

            org.jacorb.idl.parser.fatal_error("File " + fname +
                    " not found in include path", null);
            return null;
        }
    }

    public static File currentFile()
    {
        return currentFile;
    }

    public static InputStream currentStream()
    {
        return stream;
    }

    public static int read()
        throws IOException
    {
        int ch = 0;

        // Might be the end of file for main file but still have an included file
        // to process.
        if (eof && positions.size() == 0)
        {
            return -1;
        }

        if (expandedText.length() > 0)
        {
            if (pos < expandedText.length())
                ch = (int)expandedText.charAt(pos++);
            if (pos == expandedText.length())
            {
                expandedText = new StringBuffer();
                pos = 0;
            }
        }
        else
        {
            ch = currentStream().read();

            /*
             * if eof is reached, see whether we were reading
             * from the main input stream or from an include file.
             * If the latter, switch back to the main stream
             */

            if (ch == -1)
            {
                // the following line was moved here to fix bug #385
                currentStream().close();
                if (included)
                {

                    // undo effects of inhibition pragma
                    parser.setInhibitionState(false);

                    // return to last position in previous file
                    PositionInfo positionInfo = (PositionInfo)positions.pop();
                    stream = positionInfo.stream;
                    currentFile = positionInfo.file;
                    ch = ((Integer)lookahead_stack.pop()).intValue();

                    included = !(positions.empty());

                    if (logger.isInfoEnabled())
                        logger.info("returning to " + currentFile + " included: " + included);

                    lexer.restorePosition(positionInfo);
                }
                else
                {
                    eof = true;
                }
            }
        }
        return ch;
    }

}
