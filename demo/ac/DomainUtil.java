package demo.ac;

import org.omg.CORBA.*;
import java.util.*;
import jacorb.orb.domain.*;
/**
 * DomainUtil.java
 *
 *
 * Created: Wed Jul 12 17:02:17 2000
 *
 * @author Nicolas Noffke
 * $Id$
 */

public class DomainUtil  {
    
    /**
     * Does a simple DFS run over the graph.
     * The result will contain the start node.
     */
    public static Vector getAllParents(Domain start)
    {
        Vector result = new Vector();
        
        Hashtable visited = new Hashtable();
        Stack stack = new Stack();

        stack.addElement(start);
        
        while(stack.size() > 0)
        {
            //get first
            Domain current = DomainHelper.narrow
                ((org.omg.CORBA.Object) stack.pop());

            //add to result
            result.addElement(current);
            
            //get parents
            Domain[] parents = current.getParents();
            for(int i = 0; i < parents.length; i++)
            {
                if (! visited.containsKey(parents[i]))
                {
                    //if not already visited, push
                    visited.put(parents[i], parents[i]);
                    stack.push(parents[i]);
                }
            }
        }

        return result;
    }                       
    
    /**
     * This method will return the first policy found in the 
     * DFS tree. <br>
     * Optimization: check for policy while doing DFS run!
     */
    public static Policy getPolicy( org.omg.CORBA.Object start,
                                    int type )
    {
        DomainManager[] dm_list = start._get_domain_managers();

        //find starting domain, that is the first not orb-domain
        Domain dm = null;        
        for (int i = 0; i < dm_list.length; i++)
        {
            Domain d = DomainHelper.narrow( dm_list[i] );

            if ( d.name().indexOf( "orb domain" ) == -1 )
            {
                dm = d;
                break;
            }
        }

        Vector parents = getAllParents( dm );
        
        for(int i = 0; i < parents.size(); i++)
        {
            Domain d = ( Domain ) parents.elementAt( i );
            
            if( d.hasPolicyOfType( type ) )
                return d._get_policy( type );
        }

        return null;
    }
} // DomainUtil
