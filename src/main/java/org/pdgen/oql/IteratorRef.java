// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.JoriaCollection;

public class IteratorRef extends ObjectRef
{

	JoriaCollection collType;

	/** ----------------------------------------------------------------------- IteratorRef */
	public IteratorRef(String refName, JoriaCollection c)
	{
		super(refName, c.getElementType());
		collType = c;
	}

	/** ----------------------------------------------------------------------- getCollectionType */
	public JoriaCollection getCollectionType()
	{
		return collType;
	}

	/** ----------------------------------------------------------------------- getName */
	public String getName()
	{
		return name;
	}

	/** ----------------------------------------------------------------------- isEnvironment */
	public boolean isEnvironment()
	{
		return false;
	}

	/** ----------------------------------------------------------------------- isObject */
	public boolean isObject()
	{
		return true;
	}

	public boolean isPackage()
	{
		return false;
	}
}
