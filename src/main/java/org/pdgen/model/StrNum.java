// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import java.io.Serializable;

public class StrNum implements Comparable<StrNum>, Serializable
{
    private static final long serialVersionUID = 7L;
    public String name;
	public int num;

	public StrNum(String s, int num)
	{
		name = s;
		this.num = num;
	}

	public int compareTo(StrNum to)
	{
		return name.compareTo(((StrNum) to).name);
	}

	public String toString()
	{
		return name;
	}
}
