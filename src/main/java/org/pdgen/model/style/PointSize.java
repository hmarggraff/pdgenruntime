// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.util.Objects;

public class PointSize extends Length
{

    private static final long serialVersionUID = 7L;

    public PointSize()
	{
	}

	public PointSize(float v)
	{
		super(v);
	}

	public static PointSize newPointSize(PointSize l)
	{
		if (l == null)
			return null;
		else
			return new PointSize(l.getValInPoints());
	}

	public boolean equals(Object o)
	{
		if (o == null || o.getClass() != PointSize.class)
			return false;
		final float val1 = ((PointSize) o).getValInPoints();
		final float val2 = getValInPoints();
		return Math.abs(val1 - val2) < 0.1;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getValInPoints());
	}
}
