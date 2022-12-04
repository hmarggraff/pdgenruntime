// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;


import org.pdgen.data.Trace;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.env.Res;

import java.io.Serializable;

/**
 * FlexSize encodes Jorias sizing behaiour.
 * If unit is a unit, then the val is interpreted as the fixed size.
 * If unit is flex then the val is interpreted as the weight factor for expanding.
 * Thus unit=flex and val = 0 makes an element as small as possible because it does not get
 * any of the free space.
 * The default is expanable = flex and val = 1.
 */
public class FlexSize implements Cloneable, Serializable
{

	public static final int none = 0;
	public static final int flex = 1;
	public static final int mm = 2;
	public static final int cm = 3;
	public static final int inch = 4;
	public static final int pt = 5;
	public static final int asis = 6;
	public static final int wfactor = 7;
    private static final long serialVersionUID = 7L;

    public static String[] flexNames = {Res.str("Default"), Res.str("Flex"), Res.str("mm"), Res.str("cm"), Res.str("inch"), Res.str("pt"), Res.str("As_Is") /*, Res.str("Font_Size")*/};
	public static int[] flexUnitsMap = {none, flex, mm, cm, inch, pt, asis};

	public static String[] fixUnitsNames = {Res.str("mm"), Res.str("cm"), Res.str("inch"), Res.str("pt")};
	public static int[] fixUnitsMap = {mm, cm, inch, pt};
	public static String[] scaleUnitNames = {Res.str("mm"), Res.str("cm"), Res.str("inch"), Res.str("pt"), Res.str("PercentWidth")};
	public static int[] scaleMap = {mm, cm, inch, pt, asis, wfactor};
	public static String[] posNames = {Res.str("As_Is"), Res.str("mm"), Res.str("cm"), Res.str("inch"), Res.str("pt")};
	public static int[] posMap = {flex, mm, cm, inch, pt};

	public static float[] factors = {1,1,72f/25.4f, 72f/2.54f, 72, 1, 1, 0.01f};
	protected float val = 100;  // im allgemeinen als Points
	protected int unit = flex;
	public static FlexSize FLEX = new FlexSize(100, flex);
	public static FlexSize MIN = new FlexSize(0, flex);

	public FlexSize()
	{
	}

	public FlexSize(float val, int unit)
	{
		this.val = val;
		this.unit = unit;
	}

	public FlexSize(FlexSize from)
	{
		val = from.val;
		unit = from.unit;
	}

	public float getVal()
	{
		return val;
	}

	public boolean isExpandable()
	{
		return unit <= flex;
	}

	public static FlexSize newFlexSize(FlexSize from)
	{
		if (from == null)
			return null;
        else if(FLEX.equals(from) || MIN.equals(from))
            return from;
		else
			return new FlexSize(from);
	}


	public int getUnit()
	{
		return unit;
	}

	public int hashCode()
	{
		return unit * 1000 + ((int)(val *100));
	}

	public boolean equals(Object c)
	{
		if (c == null || (c.getClass() != FlexSize.class))
			return false;
		FlexSize s = (FlexSize) c;
		return unit == s.unit && (val - s.val < 0.0001);
	}

	public String toString()
	{
		if (unit <= flex)
		{
			if (val == 0)
				return Res.str("As_Is");
			return Res.strb("Flex");// + Float.toString(val);
		}
		else
			return "Fix " + val / factors[unit] + " " + flexNames[unit];
	}
	public static boolean eq(FlexSize a, FlexSize b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		return a.unit == b.unit && ((a.val - b.val) < 0.001);
	}

	public static float getPoints(int unit, float val)
	{
        return val * factors[unit];
	}

	public static float inUnits(final float val, final int unit)
	{
		return val / factors[unit];
	}


	public float getInUnits()
	{
		return val / factors[unit];
	}


	public static FlexSize parse(String s) throws NumberFormatException
	{
		if (s == null)
			return null;
		if (s.startsWith(Res.strb("Flex")))
		{
			s = s.substring(Res.strb("Flex").length());
			float f = Float.parseFloat(s);
			return new FlexSize(f, flex);
		} else if (s.startsWith("Fix "))
		{
			int us = s.lastIndexOf(' ');

			String s1 = s.substring("Fix ".length(), us);
			float f = Float.parseFloat(s1);
			String unitStr = s1.substring(us);
			for (int i = 0; i < flexNames.length; i++)
			{
				if (flexNames.equals(unitStr))
					return new FlexSize(f, i);
			}
		}
		throw new NumberFormatException(Res.strp("Syntax_Error_in_FlexSize", s));


	}

	public static boolean eqLog(String msg, FlexSize s1, FlexSize s2)
	{
		if (eq(s1,s2))
			return true;
		Trace.log(Trace.copy, msg + ":" + s1 + " =? " + s2);
		return false;
	}

	public float getAdjusted(float flexval)
	{
		if (unit == flex)
			return flexval;
		else
			return Math.min(val, flexval);
	}

	public float getAdjusted(float max, float prop, FlexSize limit)
	{
		float ret;
		if (unit == flex)
			ret = max;
		else if (unit == wfactor)
			ret = prop * val;
		else
			ret = val;
		if (!limit.isExpandable())
			return Math.min(ret, limit.val);
		return ret;

	}

	public static int inverse(int[] map, int v)
	{
		for (int i = 0; i < map.length; i++)
		{
			int m = map[i];
			if (m == v)
				return i;
		}
		throw new JoriaAssertionError("Flexsize unit not in map: " + v);
	}

	public Object readResolve()
	{
        if(FLEX.equals(this))
            return FLEX;
        else if(MIN.equals(this))
            return MIN;
        return this;
	}

}
