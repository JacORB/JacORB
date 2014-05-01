package org.jacorb.idl.runtime;

/** This subclass of symbol represents (at least) terminal symbols returned 
 *  by the scanner and placed on the parse stack.  At present, this 
 *  class does nothing more than its super class.
 *  
 * @see org.jacorb.idl.runtime.int_token
 * @see org.jacorb.idl.runtime.str_token
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class token 
    extends symbol 
{

    /* Simple constructor -- just delegates to the super class. */
    public token(int term_num)
    {
        /* super class does all the work */
        super(term_num);
    }
};


