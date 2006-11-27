package org.jacorb.test.common;

import java.io.OutputStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

public class JUnitTestFormatter implements JUnitResultFormatter
{
	public void endTestSuite(JUnitTest junittest) throws BuildException {
		// TODO Auto-generated method stub

	}

	public void setOutput(OutputStream outputstream) {
		// TODO Auto-generated method stub

	}

	public void setSystemError(String s) {
		// TODO Auto-generated method stub

	}

	public void setSystemOutput(String s) {
		// TODO Auto-generated method stub

	}

	public void startTestSuite(JUnitTest junittest) throws BuildException {
		// TODO Auto-generated method stub

	}

	public void addError(Test arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

	public void addFailure(Test arg0, AssertionFailedError arg1) {
		// TODO Auto-generated method stub

	}

	public void endTest(Test arg0) {
		// TODO Auto-generated method stub

	}

	public void startTest(Test arg0)
	{
		if (TestUtils.verbose)
		{
			System.err.println(arg0);
		}
	}

}
