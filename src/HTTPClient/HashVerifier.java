/*
 * @(#)HashVerifier.java				0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschalär
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;


/**
 * This interface defines a hash verifier.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

interface HashVerifier
{
    /**
     * This method is invoked when a digest of a stream has been calculated.
     * It must verify that the hash (or some function of it) is correct and
     * throw an IOException if it is not.
     *
     * @param hash the calculated hash
     * @param len  the number of bytes read from the stream
     * @exception IOException if the verification fails.
     */
    public void verifyHash(byte[] hash, long len)  throws java.io.IOException;
}

