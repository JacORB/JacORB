package org.jacorb.test.util;

/*
 *        JacORB  - a free Java ORB
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.jacorb.test.common.TestUtils;
import org.jacorb.util.Diagnostic;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;


public class DiagnosticTest
{
    private PrintStream originalOut = System.out;
    private PrintStream originalErr = System.err;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp()
    {
        System.setOut(new PrintStream(new LoggingOutputStream(TestUtils.getLogger())));
        System.setErr(new PrintStream(new LoggingOutputStream(TestUtils.getLogger())));
    }

    @After
    public void tearDown()
    {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Verify the Diagnostic class (and use it to output logging on the framework).
     */
    @Test
    public void testDiagnostic () throws Exception
    {
        Diagnostic.main(new String[] {});
    }

    class LoggingOutputStream extends OutputStream
    {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        private final Logger logger;

        public LoggingOutputStream(Logger logger)
        {
            this.logger = logger;
        }

        @Override
        public void write(int b)
        {
            if (b == '\n')
            {
                String line = baos.toString();
                baos.reset();

                logger.debug(line);
            }
            else
            {
                baos.write(b);
            }
        }
    }
}
