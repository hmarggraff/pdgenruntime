// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;

public class FieldRef implements SymbolEntry
{

	// fields
	JoriaAccess axs;

	/* ----------------------------------------------------------------------- ObjectRef */
	public FieldRef(JoriaAccess field)
	{
		axs = field;
	}

	/* ----------------------------------------------------------------------- getName */
	public String getName()
	{
		return axs.getName();
	}

	/* ----------------------------------------------------------------------- getType */
	public JoriaType getType()
	{
		return axs.getType();
	}

	/* ----------------------------------------------------------------------- isEnvironment */
	public boolean isEnvironment()
	{
		return false;
	}

	/* ----------------------------------------------------------------------- isObject */
	public boolean isObject()
	{
		return true;
	}

	/* ----------------------------------------------------------------------- isPackage */
	public boolean isPackage()
	{
		return false;
	}

	public JoriaAccess getField()
	{
		return axs;
	}
}
