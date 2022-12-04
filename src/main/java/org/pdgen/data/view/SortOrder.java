// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;

import java.io.Serializable;

public class SortOrder implements Serializable
{
    private static final long serialVersionUID = 7L;
    JoriaAccess baseAccess;
	boolean caseSensitive = true;
	boolean ascending = true;

	public SortOrder(JoriaAccess baseAccess)
	{
		this.baseAccess = baseAccess;
	}

	public boolean isAscending()
	{
		return ascending;
	}

	public JoriaAccess getBaseAccess()
	{
		return baseAccess;
	}

	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}


	public boolean isCaseSensitive()
	{
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
	}

	public SortOrder copy()
    {
        SortOrder ret = new SortOrder(baseAccess);
        ret.caseSensitive = caseSensitive;
        ret.ascending = ascending;
        return ret;
    }
}
