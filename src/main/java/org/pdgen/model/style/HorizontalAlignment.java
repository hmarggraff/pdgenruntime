// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.env.Res;

import java.io.Serializable;
import java.util.Objects;

public class HorizontalAlignment implements Serializable
{

	public static final HorizontalAlignment LEFT = new HorizontalAlignment(0f);
	public static final HorizontalAlignment CENTER = new HorizontalAlignment(0.5f);
	public static final HorizontalAlignment RIGHT = new HorizontalAlignment(1f);
	public static final HorizontalAlignment BLOCK = new HorizontalAlignment(-1f);
    private static final long serialVersionUID = 7L;
    private float align;

	/** ----------------------------------------------------------------------- HorizontalAlignment */
	public HorizontalAlignment()
	{
		align = 0;
	}

	public HorizontalAlignment(float a)
	{
		align = a;
	}

	public float getAlign()
	{
		return align;
	}

	public boolean isBegin()
	{
		return align < 0.0001f && align > -0.0001f;
	}

	public boolean isBlock()
	{
		return align < 0f;
	}

	public boolean isEnd()
	{
		return align > 0.999f;
	}

	public boolean isMid()
	{
		return align < 0.5001f && align >= 0.4999f;
	}

	public void setAlign(float newAlign)
	{
		align = newAlign;
	}

	public String toString()
	{
		if (isBegin())
			return Res.str("Left");
		else if (isMid())
			return Res.str("Center");
		else if (isEnd())
			return Res.str("Right");
		else if (isBlock())
			return Res.str("Block");
		else
			return String.valueOf(align);
	}

	public boolean equals(Object parm1)
	{
        return !(parm1 == null || parm1.getClass() != HorizontalAlignment.class) &&
               Math.abs(((HorizontalAlignment) parm1).align - align) < 0.001f;
    }

	@Override
	public int hashCode() {
		return Objects.hash(align);
	}
}
