// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.JoriaAccess;

import java.io.Serializable;
import java.util.Set;

public class CrosstabAggregate implements Serializable
{
    private static final long serialVersionUID = 7L;
    int function;
	JoriaAccess field;

	public CrosstabAggregate(int function, JoriaAccess f)
	{
		this.function = function;
		field = f;
	}

	public int getFunction()
	{
		return function;
	}

	public String toString()
	{
		if (field != null)
			return field.getLongName();
		return "";
	}

	public JoriaAccess getAccess()
	{
		return field;
	}

	public void getUsedAccessors(Set<JoriaAccess> s)
	{
		s.add(field);
	}

	public boolean hasText(final String text)
	{
		return field.getName().toLowerCase().contains(text);
	}
}
