// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.style.FlexSize;

public class MutableFlexSize extends FlexSize
{

    private static final long serialVersionUID = 7L;

    public void setVal(float newVal)
	{
		val = newVal;
	}
	public void setUnit(int unit)
	{
		this.unit = unit;
	}
	public void set(int unit, float unNormalized)
	{
		val = unNormalized * FlexSize.factors[unit];
		this.unit = unit;
	}
	
}
