/*
 * @(#)LinkedList.java					0.3-2 18/06/1999
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
 * This class implements a singly linked list.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 */

class LinkedList
{
    /** head of list */
    private LinkElement head = null;

    /** tail of list (for faster adding) */
    private LinkElement tail = null;


    /**
     * Add the specified element to the head of the list.
     *
     * @param elem the object to add to the list.
     */
    public synchronized void addToHead(Object elem)
    {
	head = new LinkElement(elem, head);

	if (head.next == null)
	    tail = head;
    }


    /**
     * Add the specified element to the end of the list.
     *
     * @param elem the object to add to the list.
     */
    public synchronized void addToEnd(Object elem)
    {
	if (head == null)
	    head = tail = new LinkElement(elem, null);
	else
	    tail = (tail.next = new LinkElement(elem, null));
    }


    /**
     * Remove the specified element from the list. Does nothing if the element
     * is not in the list.
     *
     * @param elem the object to remove from the list.
     */
    public synchronized void remove(Object elem)
    {
	if (head == null)  return;

	if (head.element == elem)
	{
	    head = head.next;
	    return;
	}

	LinkElement curr = head;
	while (curr.next != null)
	{
	    if (curr.next.element == elem)
	    {
		if (curr.next == tail)  tail = curr;
		curr.next = curr.next.next;
		return;
	    }
	    curr = curr.next;
	}
    }


    /**
     * Return the first element in the list. The list is not modified in any
     * way.
     *
     * @return the first element
     */
    public synchronized Object getFirst()
    {
	if (head == null)  return null;
	return head.element;
    }


    private LinkElement next_enum = null;

    /**
     * Starts an enumeration of all the elements in this list. Note that only
     * one enumeration can be active at any time.
     *
     * @return the first element, or null if the list is empty
     */
    public synchronized Object enumerate()
    {
	if (head == null)  return null;

	next_enum = head.next;
	return head.element;
    }


    /**
     * Gets the next element in the enumeration. The enumeration must have
     * been first initalized with a call to <code>enumerate()</code>.
     *
     * @return the next element, or null if none left
     * @see #enumerate()
     */
    public synchronized Object next()
    {
	if (next_enum == null)  return null;

	Object elem = next_enum.element;
	next_enum = next_enum.next;

	return elem;
    }


    public static void main(String args[])  throws Exception
    {
	// LinkedList Test Suite

	System.err.println("\n*** Linked List Tests ...");

	LinkedList list = new LinkedList();
	list.addToHead("One");
	list.addToEnd("Last");
	if (!list.getFirst().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.enumerate().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.next().equals("Last"))
	    throw new Exception("Last element wrong");
	if (list.next() != null)
	    throw new Exception("End of list wrong");
	list.remove("One");
	if (!list.getFirst().equals("Last"))
	    throw new Exception("First element wrong");
	list.remove("Last");
	if (list.getFirst() != null)
	    throw new Exception("End of list wrong");

	list = new LinkedList();
	list.addToEnd("Last");
	list.addToHead("One");
	if (!list.getFirst().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.enumerate().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.next().equals("Last"))
	    throw new Exception("Last element wrong");
	if (list.next() != null)
	    throw new Exception("End of list wrong");
	if (!list.enumerate().equals("One"))
	    throw new Exception("First element wrong");
	list.remove("One");
	if (!list.next().equals("Last"))
	    throw new Exception("Last element wrong");
	list.remove("Last");
	if (list.next() != null)
	    throw new Exception("End of list wrong");

	list = new LinkedList();
	list.addToEnd("Last");
	list.addToHead("Two");
	list.addToHead("One");
	if (!list.getFirst().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.enumerate().equals("One"))
	    throw new Exception("First element wrong");
	if (!list.next().equals("Two"))
	    throw new Exception("Second element wrong");
	if (!list.next().equals("Last"))
	    throw new Exception("Last element wrong");
	if (list.next() != null)
	    throw new Exception("End of list wrong");
	list.remove("Last");
	list.remove("Two");
	list.remove("One");
	if (list.getFirst() != null)
	    throw new Exception("Empty list wrong");

	System.err.println("\n*** Tests finished successfuly");
    }
}


/**
 * The represents a single element in the linked list.
 */
class LinkElement
{
    Object      element;
    LinkElement next;

    LinkElement(Object elem, LinkElement next)
    {
	this.element = elem;
	this.next    = next;
    }
}

