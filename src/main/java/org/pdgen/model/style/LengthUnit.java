// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.io.Serializable;
import java.io.ObjectStreamException;

import org.pdgen.env.Res;


public class LengthUnit implements Serializable
{
   public static final LengthUnit MM            = new LengthUnit(Res.str("mm"), (float) (72 / 25.4));
   public static final LengthUnit CM            = new LengthUnit(Res.str("cm"), (float) (72 / 2.54));
   public static final LengthUnit INCH          = new LengthUnit(Res.str("inch"), 72);
   public static final LengthUnit POINT         = new LengthUnit(Res.str("pt"), 1f);
   public static final LengthUnit ROWS         = new LengthUnit(Res.str("Rows"), 1f);
   public static final LengthUnit COLS         = new LengthUnit(Res.str("Cols"), 1f);
    private static final long serialVersionUID = 7L;
    static LengthUnit preferredUnit = POINT;
   static LengthUnit[]            units         = {POINT, MM, CM, INCH,};
   protected float                factor;
   protected String               name = "";

	/* ----------------------------------------------------------------------- LengthUnit */

   public LengthUnit(String name, float factor)
   {
      this.name   = name;
      this.factor = factor;
   }

   /* ----------------------------------------------------------------------- getFactor */

   public float getFactor()
   {
      return factor;
   }

   /* ----------------------------------------------------------------------- getLengthUnits */

   public static LengthUnit[] getLengthUnits()
   {
      return units;
   }


   public String getName()
   {
      return name;
   }


   public String toString()
   {
      return name;
   }
	public static LengthUnit findLengthUnit(String s2)
	{
		int u = 0;
		while (u < units.length)
		{
			if (s2.equals(units[u].name))
				return units[u];
			u++;
		}
		return null;
	}
	protected Object readResolve() throws ObjectStreamException
	{
		LengthUnit newLu = findLengthUnit(name);
		if (newLu == null)
			return this;
		else
			return newLu;
	}
}
