package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2013 Gerald Brose / The JacORB Team.
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

/**
 * Version identification file.
 */
public final class Version
{
    public static final String orbId = "jacorb";

    public static final String version = "@project.version@";
    public static final String yearString = "1997-" + "@releaseYear@";
    public static final String date = "@timestamp@";

    public static final String gitInfo = "@buildNumber@";


    public static final String versionInfo =
    (
        "JacORB V" + version +
        " (www.jacorb.org)" + System.getProperty("line.separator") +
        "\t(C) The JacORB project " + yearString + System.getProperty("line.separator") +
        "\t" + Version.date + " with SHA " + gitInfo
    );


    // Used by the version command.
    public static void main(String[] args)
    {
        System.out.println (versionInfo);
    }
}
