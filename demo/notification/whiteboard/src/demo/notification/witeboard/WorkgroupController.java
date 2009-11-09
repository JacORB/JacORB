package demo.notification.whiteboard;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */


import org.omg.CORBA.ORB;

/**
 * WorkgroupController.java
 *
 *
 * Created: Thu Jan 02 20:51:29 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public interface WorkgroupController {

    public void drawLine(int x0,
                         int y0,
                         int x1,
                         int y1,
                         int red,
                         int green,
                         int blue);

    public void drawLineLocal(int x0,
                              int y0,
                              int x1,
                              int y1,
                              int red,
                              int green,
                              int blue,
                              int brushsize);

    public void updateWholeImage(int[] data);

    public PixelImage getImage();

    public void clearAll();

    public void clearAllLocal();

    public void setBrushSize(int size);

    public ORB getOrb();

    public String[] getListOfWhiteboards();

    public void leaveWhiteboard();

    public void selectWhiteboard(String name);

    public int getWorkgroupId();

    public void exit();

}// WorkgroupController
