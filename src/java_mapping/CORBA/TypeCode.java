package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 *
 * TypeCode has a holder and a helper class.
 * The helper class shall be in the same Java package as the implementation class
 * for TypeCode.
 *
 * last modified: 10/09/99 GB
 */
abstract public class TypeCode implements org.omg.CORBA.portable.IDLEntity {

	//for all TypeCode kinds
	abstract public boolean equal( org.omg.CORBA.TypeCode tc );
	abstract public org.omg.CORBA.TCKind kind();
	
	//for objref, struct, union, enum, alias and except
	abstract public java.lang.String id() throws org.omg.CORBA.TypeCodePackage.BadKind;
	abstract public java.lang.String name() throws org.omg.CORBA.TypeCodePackage.BadKind;

	//for struct, union, enum and except
	abstract public int member_count() throws org.omg.CORBA.TypeCodePackage.BadKind;
	abstract public java.lang.String member_name( int index) throws	org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds;

	//for struct, union and except
	abstract public org.omg.CORBA.TypeCode member_type( int index) throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds;

	//for union
	abstract public org.omg.CORBA.Any member_label( int index) throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds;
	abstract public org.omg.CORBA.TypeCode discriminator_type() throws org.omg.CORBA.TypeCodePackage.BadKind;
	abstract public int default_index() throws org.omg.CORBA.TypeCodePackage.BadKind;

	//for string, sequence and array
	abstract public int length() throws org.omg.CORBA.TypeCodePackage.BadKind;
	abstract public org.omg.CORBA.TypeCode content_type() throws org.omg.CORBA.TypeCodePackage.BadKind; 

    // for fixed
    public abstract short fixed_digits() throws org.omg.CORBA.TypeCodePackage.BadKind;
    public abstract short fixed_scale() throws org.omg.CORBA.TypeCodePackage.BadKind;

} 


