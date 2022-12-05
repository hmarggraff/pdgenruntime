// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.env.Res;

import java.awt.*;
import java.io.Serializable;


public class JoriaSimpleBorder implements Cloneable, Serializable {
    public static final int NONE = 0;
    public static final int SOLID = NONE + 1;
    public static final int DOT = SOLID + 1;
    public static final int DASH = DOT + 1;
    public static final int DOUBLE = DASH + 1;
    /**
     * used temporarily in the editor if there are several differing borders to be edited at once
     */
    public static final int AMBIGUOUS = DOUBLE + 1;
    /**
     * used temporarily in the editor to mark borders that have not yet been set.
     */
    public static final int UNINITIALIZED = AMBIGUOUS + 1;
    private static final long serialVersionUID = 7L;

    private int lineStyle = NONE;
    private Color color = Color.black;
    private float thickness;
    public static final JoriaSimpleBorder NULL = new JoriaSimpleBorder(NONE, 0, Color.black);
    public static final int INHERIT = UNINITIALIZED + 1;

    public JoriaSimpleBorder() {
    }

    public static JoriaSimpleBorder copy(JoriaSimpleBorder src) {
        if (isNull(src))
            return null;
        else
            return new JoriaSimpleBorder(src.lineStyle, src.thickness, src.color);
    }

    public JoriaSimpleBorder(int lineStyle) {
        this.lineStyle = lineStyle;
    }

    public JoriaSimpleBorder(int lineStyle, float thickness, Color color) {
        this.lineStyle = lineStyle;
        this.thickness = thickness;
        this.color = color;
    }

    public JoriaSimpleBorder(JoriaSimpleBorder from) {
        set(from);
    }

    public Color getColor() {
        return color;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    public float getThickness() {
        if (lineStyle == NONE)
            return 0f;
        return thickness;
    }

    public static JoriaSimpleBorder newFrom(JoriaSimpleBorder b) {
        if (b == null)
            return null;
        else
            return new JoriaSimpleBorder(b);
    }

    public void setColor(Color newColor) {
        color = newColor;
    }

    public void setLineStyle(int newLineStyle) {
        lineStyle = newLineStyle;
    }

    public void setThickness(float newThickness) {
        thickness = newThickness;
    }

    public boolean equals(Object o) {
        if (o == null) // null and none are equivalent
            return lineStyle == NONE;
        if (o.getClass() != JoriaSimpleBorder.class)
            return false;
        else {
            JoriaSimpleBorder b = (JoriaSimpleBorder) o;
            // Wenn die Line nicht gemalt wird, wird anders verglichen
            //noinspection SimplifiableIfStatement
            if (b.lineStyle == NONE && lineStyle == NONE ||
                    b.thickness == 0.0f && thickness == 0.0f)
                return true;
            return (b.thickness - thickness) < 0.001f && b.lineStyle == lineStyle && StyleBase.eq(b.color, color);
        }
    }

    public String toString() {
        return "border linestyle: " + styleToString(lineStyle) + " color: " + color + " thickness: " + thickness; //trdone
    }

    public void set(JoriaSimpleBorder from) {
        lineStyle = from.lineStyle;
        thickness = from.thickness;
        color = from.color;
    }

    public static boolean isNull(JoriaSimpleBorder test) {
        return test == null || test.getLineStyle() == NONE || test.thickness == 0.0f;
    }

    public boolean isHeavier(JoriaSimpleBorder other) {
        if (isNull(other))
            return true;
        if (isNull(this))
            return false;
        if (lineStyle == other.lineStyle)
            return thickness >= other.thickness;
        if (lineStyle == DOUBLE)
            return true;
        if (other.lineStyle == DOUBLE)
            return false;
        if (lineStyle == SOLID)
            return true;
        //noinspection SimplifiableIfStatement
        if (other.lineStyle == SOLID)
            return false;
        return lineStyle == DASH;
    }

    public static String styleToString(int style) {
        if (style == NONE)
            return Res.str("None");
        if (style == SOLID)
            return Res.str("Solid");
        if (style == DOT)
            return Res.str("Dot");
        if (style == DASH)
            return Res.str("Dash");
        if (style == DOUBLE)
            return Res.str("Double");
        if (style == AMBIGUOUS)
            return Res.str("Ambigous");
        if (style == UNINITIALIZED)
            return Res.str("Uninitialized");
        return Res.strp("unknown", style);
    }

    public int hashCode() {
        return lineStyle * 1000 + ((int) (thickness * 10)) + color.getRGB();
    }
}
