// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.JoriaType;

public class ObjectRef implements SymbolEntry
{

	// fields
	JoriaType typ;
	String name;

	/* ----------------------------------------------------------------------- ObjectRef */
	public ObjectRef(String n, JoriaType t)
	{
		name = n;
		typ = t;
	}

	/* ----------------------------------------------------------------------- getName */
	public String getName()
	{
		return name;
	}

	public JoriaType getType()
	{
		return typ;
	}

	public boolean isEnvironment()
	{
		return false;
	}

	public boolean isObject()
	{
		return true;
	}

	public boolean isPackage()
	{
		return false;
	}
}
