package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 * This class collects all POA related constants.
 *
 * @author Reimo Tiedemann, FU Berlin
 */
public class POAConstants
{
    /* shutdown states */
    public final static int NOT_CALLED = 0;
    public final static int SHUTDOWN_IN_PROGRESS = 1;
    public final static int DESTRUCTION_APPARENT = 2;
    public final static int DESTRUCTION_COMPLETE = 3;

    /* separator char for qualified poa names */
    public final static char OBJECT_KEY_SEPARATOR = '/';
    public final static byte OBJECT_KEY_SEP_BYTE = (byte)OBJECT_KEY_SEPARATOR;

    public final static byte MASK_BYTE = (byte) '&';
    public final static byte MASK_MASK_BYTE = (byte) '&';
    public final static byte SEPA_MASK_BYTE = (byte) '%';

    /* root POA name */
    public final static String ROOT_POA_NAME = "RootPOA";

    /* poa states */
    public final static int ACTIVE = 0;
    public final static int HOLDING = 1;
    public final static int DISCARDING = 2;
    public final static int INACTIVE = 3;
    public final static int DESTROYED = 4;
}
