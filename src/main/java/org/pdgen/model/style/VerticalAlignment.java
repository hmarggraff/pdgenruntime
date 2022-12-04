// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.io.Serializable;
import java.util.Objects;

import org.pdgen.env.Res;


public class VerticalAlignment implements Serializable
{
   public static final VerticalAlignment TOP    = new VerticalAlignment(0f);
   public static final VerticalAlignment MIDDLE = new VerticalAlignment(0.5f);
   public static final VerticalAlignment BOTTOM = new VerticalAlignment(1f);
    private static final long serialVersionUID = 7L;
    private float                         align;

   /** ----------------------------------------------------------------------- VerticalAlignment */

   public VerticalAlignment()
   {
      align = 0;
   }
   public VerticalAlignment(float a)
   {
      align = a;
   }
   public float getAlign()
   {
      return align;
   }
   public boolean isBegin()
   {
      return align < 0.0001f;
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

   public boolean equals(Object parm1)
   {
      if (parm1 == null || (parm1.getClass() != VerticalAlignment.class))
         return false;
       float diff = Math.abs(((VerticalAlignment) parm1).align - align);
       return diff < 0.001f;
   }

    @Override
    public int hashCode() {
        return Objects.hash(align);
    }

    public String toString()
    {
        if (isBegin())
            return Res.str("Top");
        else if (isMid())
            return Res.str("Middle");
        else if (isEnd())
            return Res.str("Bottom");
        else
            return String.valueOf(align);
    }
}
