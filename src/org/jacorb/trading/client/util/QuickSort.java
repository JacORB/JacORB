
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


public class QuickSort
{
  private QuickSort()
  {
  }


  public static void sort(String[] arr)
  {
    sortImpl(arr, 0, arr.length - 1);
  }


  protected static void sortImpl(String[] arr, int l, int h)
  {
    int low = l;
    int hi = h;
    String mid;

    if (h > l) {
      mid = arr[(l + h) / 2];

      while (low <= hi) {
        while (low < h && arr[low].compareTo(mid) < 0)
          low += 1;

        while (hi > l && arr[hi].compareTo(mid) > 0)
          hi -= 1;

        if (low <= hi) {
          String tmp = arr[low];
          arr[low] = arr[hi];
          arr[hi] = tmp;

          low += 1;
          hi -= 1;
        }
      }

      if (l < hi)
        sortImpl(arr, l, hi);

      if (low < h)
        sortImpl(arr, low, h);
    }
  }
}










