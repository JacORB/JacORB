/*
 *        JacORB  - a free Java ORB
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

package org.jacorb.orb;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractBufferManager implements IBufferManager
{
   /**
    * Default size for getPreferredMemoryBuffer
    */
   static final int MEM_BUFSIZE = 1023;


   public void returnBuffer (byte[] buf)
   {
      returnBuffer (buf, false);
   }


   /**
    * <code>getPreferredMemoryBuffer</code> returns a new buffer
    * with the default (<code>MEM_BUFSIZE</code> = 1023) size.
    *
    * Method is used when there is no possibility to determine
    * what exactly size of buffer is required. Thus, just create
    * new buffer with the minimum cacheable size.
    *
    * @return a <code>byte[]</code> value
    */
   public byte[] getPreferredMemoryBuffer ()
   {
      return getBuffer (MEM_BUFSIZE);
   }


   public void release ()
   {
   }


   public void returnBuffer (byte[] buffer, boolean b)
   {
   }

   public byte[] getExpandedBuffer (int size)
   {
       // No expansion policy defined just return the 
       // buffer with the requested size
       return getBuffer (size);
   }
}
