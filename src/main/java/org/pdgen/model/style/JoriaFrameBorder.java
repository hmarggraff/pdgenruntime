// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.awt.Color;

/**
 * User: patrick
 * Date: Jun 29, 2006
 * Time: 3:50:00 PM
 */
public class JoriaFrameBorder extends JoriaSimpleBorder
{
    private static final long serialVersionUID = 7L;
    protected Length innerPadding = Length.NULL;
    protected Length outerSpacing = Length.NULL;
    public static JoriaFrameBorder NULL_FRAME = new JoriaFrameBorder(); 

    public static final JoriaFrameBorder standard = new JoriaFrameBorder(JoriaBorder.SOLID, 2f, Color.black, new Length(6f), new Length(12f));

    public JoriaFrameBorder()
    {
    }

    public JoriaFrameBorder(JoriaFrameBorder from)
    {
        super(from);
        innerPadding = from.innerPadding;
        outerSpacing = from.outerSpacing;
    }

    public JoriaFrameBorder(int lineStyle)
    {
        super(lineStyle);
    }

    public JoriaFrameBorder(int lineStyle, float thickness, Color color, Length innerPadding, Length outerSpacing)
    {
        super(lineStyle, thickness, color);
        this.innerPadding = innerPadding;
        this.outerSpacing = outerSpacing;
    }

    public Length getInnerPadding()
    {
        return innerPadding;
    }

	public float getTotalInset()
    {
        return getThickness() + innerPadding.getValInPoints() + outerSpacing.getValInPoints();
    }

    public float getInset()
    {
        return getThickness() + innerPadding.getValInPoints();
    }

    public Length getOuterSpacing()
    {
        return outerSpacing;
    }

	public String toString()
    {
        return super.toString()+ " outer: "+outerSpacing+" inner: "+innerPadding; 
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final JoriaFrameBorder that = (JoriaFrameBorder) o;

        if(!super.equals(that)) return false;
        //noinspection SimplifiableIfStatement
        if (innerPadding != null ? !innerPadding.equals(that.innerPadding) : that.innerPadding != null) return false;
        return !(outerSpacing != null ? !outerSpacing.equals(that.outerSpacing) : that.outerSpacing != null);

    }

}
