package org.jacorb.notification.util;

class GNUPatternWrapper extends PatternWrapper {

    gnu.regexp.RE pattern_;

    public void compile(String patternString) {
	try {
	    pattern_ = new gnu.regexp.RE(patternString);
	} catch (gnu.regexp.REException e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    public int match(String text) {
	gnu.regexp.REMatch[] _match = pattern_.getAllMatches(text);
	if (_match.length > 0) {
	    int _last = _match.length - 1;
	    return _match[_last].getEndIndex();
	} else {
	    return 0;
	}
    }
}
