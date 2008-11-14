package org.jacorb.idl.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.apache.log.Logger;
import org.jacorb.idl.parser;
import org.jacorb.util.ObjectUtil;

/**
 * Tidies up the java source code using Jalopy.
 */
public class PrettyPrinter {

    /**
     * Pretty prints contents of the java source file.
     *
     * @param file
     */
    public static void prettify(File file) {
        if (true)
        {
            return;
        }

        final Logger logger = parser.getLogger();
        try {
            // Create an instance of the Jalopy bean
            Class clazz = ObjectUtil.classForName("de.hunsicker.jalopy.Jalopy");
            Object prettifier = clazz.newInstance();

            // Set the input file
            Method input = clazz.getMethod("setInput", new Class[]{File.class});
            input.invoke(prettifier, new Object[]{file});

            // Set the output file
            Method output = clazz.getMethod("setOutput", new Class[]{File.class});
            output.invoke(prettifier, new Object[]{file});

            Class clazz2 = ObjectUtil.classForName("de.hunsicker.jalopy.storage.Convention");
            Method instance = clazz2.getMethod("getInstance", new Class[]{});
            Object settings = instance.invoke(null, new Object[]{});

            Class clazz3 = ObjectUtil.classForName("de.hunsicker.jalopy.storage.ConventionKeys");
            Field field = clazz3.getField("COMMENT_JAVADOC_PARSE");
            Object key = field.get(null);

            Method put = clazz2.getMethod("put", new Class[]{ key.getClass(), String.class});
            put.invoke(settings, new Object[]{key, "true"});

            // format and overwrite the given input file
            Method format = clazz.getMethod("format", new Class[]{});
            format.invoke(prettifier, new Object[]{});
            logger.info("prettyprinted " + file);
        } catch (ClassNotFoundException e) {
            logger.debug("unable to prettyprint: " + file, e);
        } catch (Exception e) {
            logger.debug("unable to prettyprint: " + file, e);
        } catch (Throwable t) {
            logger.debug("unable to prettyprint: " + file, t);
        }
    }
}
