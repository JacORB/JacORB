package org.jacorb.notification.util;

class JDK14PatternWrapper extends PatternWrapper {

    java.util.regex.Pattern pattern_;

    public void compile(String patternString) {
	pattern_ = java.util.regex.Pattern.compile(patternString);
    }

    public int match(String text) {
	java.util.regex.Matcher _m = pattern_.matcher(text);
	if (_m.find()) {
	    return _m.end();
	} else {
	    return 0;
	}
    }
}
