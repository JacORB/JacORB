package org.jacorb.notification.util;

abstract class PatternWrapper {

    static boolean sGnuRegexpAvailable = false;
    static Class sDefaultInstance;

    static {
	try {
	    Class.forName("gnu.regexp.RE");
	    sDefaultInstance = Class.forName("org.jacorb.notification.util.GNUPatternWrapper");
	    sGnuRegexpAvailable = true;
	} catch (Exception e) {
	    // ignore
	}

	if (!sGnuRegexpAvailable) {
	    try {
		Class.forName("java.util.regex.Pattern");
		sDefaultInstance = Class.forName("org.jacorb.notification.util.JDK14PatternWrapper");
	    } catch (Exception e) {
		throw new RuntimeException("Neither java.util.regex.Pattern nor gnu.regexp available !");
	    }
	}
    }

    static PatternWrapper init(String patternString) {
	try {
	    PatternWrapper _wrapper;
	    _wrapper = (PatternWrapper) sDefaultInstance.newInstance();
	    _wrapper.compile(patternString);
	    return _wrapper;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e.getMessage());
	}
    }

    public abstract void compile(String pattern);

    public abstract int match(String text);
}
