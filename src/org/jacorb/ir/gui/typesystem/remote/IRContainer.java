package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import javax.swing.tree.*;
import org.jacorb.ir.gui.typesystem.*;

/**
 * Abstrakte Oberklasse für alle Klassen, die in unserem Baum Children
 * haben sollen.  Neben  den "echten" CORBA-Container-Klassen soll das
 * z.B.  auch StructDef  sein.  Letztere Klassen  sollen also  bei uns
 * konzeptionell  Container sein, weil sie member  besitzen (auch wenn
 * sie nicht von CORBA::Container erben)
 * 
 */

public abstract class IRContainer 
    extends IRNode 
    implements AbstractContainer
{

    /**
     * AbstractContainer constructor comment.
     */
    protected IRContainer() {
	super();
    }
 
   /**
    * @param irObject org.omg.CORBA.IRObject
    */

    protected IRContainer ( IRObject irObject) 
    {
	super(irObject);
    }

    /**
     * Erzeugt   TypeSystemNodes   für   alle   contained   Objekte.
     * Default-Implementierung,  die  für   "echte"  CORBA-Container
     * funktionert.  Für  andere Klassen  (z.B. IRStruct),  die keine
     * echten  CORBA-Container sind, wird  diese Methode überschrieben
     * mit individuellem Code zum Auslesen der members.
     * @return org.omg.CORBA.Object 
     */

    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents () 
    {
	Container container = 
            ContainerHelper.narrow((org.omg.CORBA.Object)this.irObject);
	Contained[] contents = 
            container.contents(DefinitionKind.dk_all, true);	

	org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];

	for (int i=0; i<contents.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
	}	
	return result;
    }
}





