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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import org.hamcrest.core.AllOf;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.util.Diagnostic;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.TestName;

public class DiagnosticTest
{
    @Rule
    public final StandardOutputStreamLog log = new StandardOutputStreamLog
    (
       TestUtils.verbose ? LogMode.LOG_AND_WRITE_TO_STREAM : LogMode.LOG_ONLY
    );

    @Rule
    public TestName name = new TestName();

    /**
     * Verify the Diagnostic class (and use it to output logging on the framework).
     */
    @Test
    public void testDiagnostic () throws Exception
    {
        Diagnostic.main(new String[] {});

        assertThat (log.getLog(),
                AllOf.allOf(
                        containsString("JacORB Version"),
                        containsString("Preferred non-loopback address")));

    }
}
