package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 * This class collects all ORB related constants.
 *   
 * This includes various OMG assigned values used by JacORB.
 *
 * OMG Assigned ranges:
 *
 *  1 VMCID           0x4A430xxx               ("JC\x00\x00" - "JC\x0f\xff")
 * 16 ORB type IDs    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")
 * 16 profile tags    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")
 * 16 service tags    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")
 * 16 component IDs   0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")
 */

public final class ORBConstants
{
    public static final int JACORB_ORB_ID           = 0x4A414300;
    public static final int SERVICE_PADDING_CONTEXT = 0x4A414301;
    public static final int SERVICE_PROXY_CONTEXT   = 0x4A414302;
}
