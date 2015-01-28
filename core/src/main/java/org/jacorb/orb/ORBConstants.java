package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
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


/**
 * This class collects all ORB related constants.
 * <p>
 * This includes various OMG assigned values used by JacORB.
 * OMG Assigned ranges:
 * </p>
 * <ul>
 * <li>1 VMCID            0x4A430xxx               ("JC\x00\x00" - "JC\x0f\xff")</li>
 * <li>16 ORB type IDs    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")</li>
 * <li>16 profile tags    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")</li>
 * <li>16 service tags    0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")</li>
 * <li>16 component IDs   0x4A414300 - 0x4A41430f  ("JAC\x00" - "JAC\x0f")</li>
 * </ul>
 */
public final class ORBConstants
{
    /**
     * <code>VMCID</code> is the Vendor Minor Codeset ID. According to
     * ftp://ftp.omg.org/pub/docs/ptc/01-08-35.txt the VMCID is equivalent
     * to the VSCID
     */
    public static final int VMCID                   = 0x4A430000;
    public static final int JACORB_ORB_ID           = 0x4A414300;
    public static final int SERVICE_PADDING_CONTEXT = 0x4A414301;
    public static final int SERVICE_PROXY_CONTEXT   = 0x4A414302;

    /**
     * <code>JAC_SSL_PROFILE_ID/JAC_NOSSL_PROFILE_ID</code> are used to
     * distinguish between IIOP profiles that do and do not support SSL.
     */
    public static final int JAC_SSL_PROFILE_ID      = VMCID;
    public static final int JAC_NOSSL_PROFILE_ID    = VMCID | 0x01;

    // Added to support iterop with another non-jacorb
    public static final int TAO_ORB_ID              = 0x54414F00;
}
