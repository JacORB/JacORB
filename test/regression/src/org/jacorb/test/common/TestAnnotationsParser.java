package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

/**
 * Parses test annotations from javadoc comments in JUnit tests.  A single
 * instance of this class is created for each source file to be parsed.
 * After the parsing is done, the instance is still kept around because it
 * stores the annotations that were found in the file for access by other
 * parts of the program.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class TestAnnotationsParser
{
    /**
     * Maps qualified class names to the corresponding instances
     * of this class (String -> TestAnnotationsParser).
     */
    private static Map instances = new HashMap();
    
    private String       filename;
    private ByteBuffer   sourceBuffer = null;
    private CharSequence source       = null;
    
    private TestAnnotations classAnnotations = null;
    private Map methodAnnotations = null;
    
    private boolean annotationsLoaded = false;
    
    /**
     * Private constructor, use getInstance() to create instances.
     */
    private TestAnnotationsParser (Class c)
    {
        String className = c.getName();
        filename = TestUtils.testHome() 
                   + "/src/" + className.replace('.', '/') + ".java";
    }
    
    /**
     * Gets the annotations from the top-level javadoc comment
     * of the source file.  If there is no such comment, or it
     * doesn't contain JacORB-specific annotations, returns null.
     */
    public TestAnnotations getClassAnnotations()
    {
        if (!annotationsLoaded)
            if (!containsAnnotations())
                return null;
            else
                loadAnnotations();
        
        return classAnnotations;
    }

    /**
     * Gets the annotations for the method that is called methodName.
     * If there is no such method, or it doesn't have a javadoc comment
     * with JacORB-specific annotations, returns null.  If there are
     * annotations at the class level, each method of the class is considered
     * to have the same annotations by default, so you'll get an annotations
     * object with those values for every method.  The defaults are
     * overridden if the method does have annotations of its own.
     */
    public TestAnnotations getMethodAnnotations (String methodName)
    {
        if (!annotationsLoaded)
        {
            if (!containsAnnotations())
                return null;
            else
                loadAnnotations();
        }
        
        if (methodAnnotations == null)
            return null;
        else
            return (TestAnnotations)methodAnnotations.get (methodName);
    }

    private static Pattern tagPattern =
        Pattern.compile ("@jacorb-");
    
    /**
     * Returns true if the source file does contain JacORB-specific annotations.
     * This is intended as a quick check so that we needn't parse the
     * entire file when there are no annotations in it anyway.
     */
    private boolean containsAnnotations()
    {
        if (annotationsLoaded)
            return classAnnotations != null && methodAnnotations != null;
        else
        {
            // It would be even better if we could use getSourceBuffer()
            // here, because then the file would never actually be read
            // and converted into Java characters at all.  But we'd have to
            // implement a search at the byte level then.
            Matcher m = tagPattern.matcher(getSource());
            if (m.find())
                return true;
            else
            {
                annotationsLoaded = true;
                return false;
            }
        }
    }
    
    private static Pattern javadocPattern =
        Pattern.compile ("(/\\*\\*\\s.*?\\*/)\\s*([^\n]*)", Pattern.DOTALL);
    
    private static Pattern classPattern =
        Pattern.compile ("class\\s.*|.*?\\sclass\\s.*");
    
    private static Pattern methodPattern =
        Pattern.compile ("public void ([^\\s(]+)\\s*\\(.*");
    
    /**
     * This method does the actual parsing (loading) of annotations from
     * the file.
     */
    private void loadAnnotations()
    {
        Matcher m = javadocPattern.matcher (getSource());
        boolean isFirst = true;
        while (m.find())
        {
            String javadoc = m.group(1);
            String item    = m.group(2);
            if (isFirst)
            {
                Matcher cm = classPattern.matcher (item);
                if (cm.matches())
                {
                    classAnnotations = createAnnotations (javadoc);
                    isFirst = false;
                    continue;
                }
            }
            Matcher mm = methodPattern.matcher (item);
            if (mm.matches())
            {
                String methodName = mm.group(1);
                TestAnnotations ta = createAnnotations (javadoc);
                if (ta != null)
                {
                    if (methodAnnotations == null)
                        methodAnnotations = new HashMap();
                    methodAnnotations.put (methodName, ta);
                }
            }
            isFirst = false;
        }
        // unmap the source file and free resources,
        // now that we have parsed everything we were interested in
        source = null;
        sourceBuffer = null;
    }
    
    private static Pattern tagValuePattern =
        Pattern.compile ("@(jacorb-[a-z-]+)\\s+(\\S+)");
    
    /**
     * Parses a javadoc comment and creates a TestAnnotations object
     * from it, but only if the javadoc text actually contains
     * JacORB-specific annotations.  Otherwise, the method returns null.
     */
    private TestAnnotations createAnnotations (String javadoc)
    {
        String clientSince = null;
        String serverSince = null;
        if (classAnnotations != null)
        {
            // initialize from class-level annotations
            clientSince = classAnnotations.getClientSince();
            serverSince = classAnnotations.getServerSince();
        }
        Matcher m = tagValuePattern.matcher (javadoc);
        while (m.find())
        {
            if (m.group(1).equals ("jacorb-since"))
            {
                clientSince = m.group(2);
                serverSince = m.group(2);
            }
            else if (m.group(1).equals ("jacorb-client-since"))
                clientSince = m.group(2);
            else if (m.group(1).equals ("jacorb-server-since"))
                serverSince = m.group(2);
        }
        if (clientSince != null || serverSince != null)
            return new TestAnnotations (clientSince, serverSince);
        else
            return null;
    }
    
    /**
     * Returns the source code of the annotated class as a CharSequence.
     */
    private CharSequence getSource()
    {
        if (source == null)
        {
            source = Charset.forName("ISO-8859-1").decode (getSourceBuffer());
        }
        return source;
    }
    
    /**
     * Returns the source code of the annotated class as a memory-mapped
     * ByteBuffer.
     */
    private ByteBuffer getSourceBuffer()
    {
        if (sourceBuffer == null)
        {
            try
            {
                FileInputStream s = new FileInputStream (filename);
                FileChannel fc = s.getChannel();
                sourceBuffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return sourceBuffer;
    }

    /**
     * Returns the TestAnnotationsParser for the given class c.
     * There is always at most a single instance for each class.
     */
    public static TestAnnotationsParser getInstance (Class c)
    {
        TestAnnotationsParser result =
            (TestAnnotationsParser)instances.get(c.getName());
        if (result == null)
        {
            result = new TestAnnotationsParser(c);
            instances.put (c.getName(), result);
        }
        return result;
    }
    
}
