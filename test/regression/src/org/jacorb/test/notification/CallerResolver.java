package org.jacorb.test.notification;

import java.util.StringTokenizer;
import java.io.StringWriter;
import java.io.PrintWriter;

public class CallerResolver {

    public String getCaller() {
        String stackTrace = makeStackTrace();
        StringTokenizer lineTokenizer = new StringTokenizer(stackTrace, "\n");
        while (lineTokenizer.hasMoreTokens()) {
            String line = lineTokenizer.nextToken().trim();
            if (line.startsWith("at " + getClass().getName() + ".getCaller")) {
                if (lineTokenizer.hasMoreTokens()) {
                    return extractClassName(lineTokenizer.nextToken());
                }
            }
        }
        return null;
    }


    public String makeStackTrace() {
        try {
            throw new RuntimeException("INTENTIONAL");
        } catch (RuntimeException re) {
            StringWriter sw = new StringWriter();
            re.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }


    String extractClassName(String stackTraceLine) {
        //get rid of the leading indentation
        String className = stackTraceLine.trim();
        // get rid of the "at " before the class name
        className = className.substring(3);
        // get rid of the source file/line number info
        className = className.substring(0, className.indexOf('('));
        // get rid of the method name
        className = className.substring(0, className.lastIndexOf('.'));

        return className;
    }
}
