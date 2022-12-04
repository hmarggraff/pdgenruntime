// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

//MARKER The strings in this file shall not be translated

public class CellRequirements
{
	public float pref;
	public float act;
	public float off;
	public boolean hasSpecial;// reflower or span

	public float end()
	{
		return off + act;
	}

	public String toString()
	{
		return "CellRequirements[" + pref + ", " + off + ", " + act + ", " + (hasSpecial ? "*]" : "]");
	}
}
