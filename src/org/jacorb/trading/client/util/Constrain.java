
// Copyright (C) 1998-2001
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.client.util;

import java.awt.*;

public class Constrain
{
  /**
   * Convenience routine for setting gridbag constraints.
   */
  public static void constrain(Container container, Component component,
    int grid_x, int grid_y, int grid_width, int grid_height,
    int fill, int anchor, double weight_x, double weight_y,
    int top, int left, int bottom, int right, int ipadx,
    int ipady)
  {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = grid_x;
    c.gridy = grid_y;
    c.gridwidth = grid_width;
    c.gridheight = grid_height;
    c.fill = fill;
    c.anchor = anchor;
    c.weightx = weight_x;
    c.weighty = weight_y;

    if (top + bottom + left + right > 0)
      c.insets = new Insets(top, left, bottom, right);
    c.ipadx = ipadx;
    c.ipady = ipady;

    ((GridBagLayout)container.getLayout()).setConstraints(component, c);
    container.add(component);
  }


  /**
   * Convenience routine for setting gridbag constraints.
   */
  public static void constrain(Container container, Component component,
    int grid_x, int grid_y, int grid_width, int grid_height,
    int fill, int anchor, double weight_x, double weight_y,
    int top, int left, int bottom, int right)
  {
    constrain(container, component, grid_x, grid_y, grid_width,
      grid_height, fill, anchor, weight_x, weight_y, top, left,
      bottom, right, 0, 0);
  }

  /**
   * Convenience routine for setting gridbag constraints.
   */
  public static void constrain(Container container, Component component,
          int grid_x, int grid_y, int grid_width, int grid_height)
  {
    constrain(container, component, grid_x, grid_y,
        grid_width, grid_height, GridBagConstraints.NONE,
        GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 0, 0);
  }


  /**
   * Convenience routine for setting gridbag constraints.
   */
  public static void constrain(Container container, Component component,
          int grid_x, int grid_y, int grid_width, int grid_height,
          int fill, int anchor)
  {
    constrain(container, component, grid_x, grid_y,
        grid_width, grid_height, fill, anchor,
        0.0, 0.0, 0, 0, 0, 0);
  }


  /**
   * Convenience routine for setting gridbag constraints.
   */
  public static void constrain(Container container, Component component,
    int grid_x, int grid_y, int grid_width, int grid_height,
    int top, int left, int bottom, int right)
  {
    constrain(container, component, grid_x, grid_y,
        grid_width, grid_height, GridBagConstraints.NONE,
        GridBagConstraints.NORTHWEST,
        0.0, 0.0, top, left, bottom, right);
  }
}










