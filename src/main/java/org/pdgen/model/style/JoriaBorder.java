// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;
//MARKER The strings in this file shall not be translated

import java.awt.*;
import java.io.Serializable;


public class JoriaBorder implements Cloneable, Serializable {
    public static final int NONE = 0;
    public static final int SOLID = NONE + 1;
    public static final int DOT = SOLID + 1;
    public static final int DASH = DOT + 1;
    public static final int DOUBLE = DASH + 1;

    public static final JoriaBorder zeroBorder = new JoriaBorder(NONE, 0, Color.black, 0, 0);
    public static final JoriaBorder spaceBorder = new JoriaBorder(NONE, 0, Color.black, 0, 4);
    private static final long serialVersionUID = 7L;

    private int lineStyle = NONE;
    private Color color = Color.black;
    private float spacing;
    private float thickness;
    private float padding;

    public JoriaBorder() {
    }

    public JoriaBorder(int lineStyle, float thickness, Color color, float indent, float padding) {
        this.lineStyle = lineStyle;
        this.thickness = thickness;
        this.color = color;
        spacing = indent;
        this.padding = padding;
    }

    public JoriaBorder(JoriaBorder from) {
        lineStyle = from.lineStyle;
        thickness = from.thickness;
        color = from.color;
        spacing = from.spacing;
        padding = from.padding;
    }

    public Color getColor() {
        return color;
    }

    public int getLineStyle() {
        if (thickness == 0.0f)
            return NONE;
        return lineStyle;
    }

    public float getThickness() {
        return thickness;
    }

    public static JoriaBorder newFrom(JoriaBorder b) {
        if (b == null)
            return null;
        else
            return new JoriaBorder(b);
    }

    public void setColor(java.awt.Color newColor) {
        color = newColor;
    }

    public void setLineStyle(int newLineStyle) {
        lineStyle = newLineStyle;
    }

    public void setThickness(float newThickness) {
        thickness = newThickness;
    }

    /**
     * the outer space
     *
     * @param newIndent how much the border is indented from the envelope
     */
    public void setSpacing(float newIndent) {
        spacing = newIndent;
    }

    /**
     * @return how much the border is indented from the envelope
     */
    public float getSpacing() {
        if (spacing >= 0)
            return spacing;
        return getThickness() / -2;
    }

    /**
     * the inner space
     *
     * @param newPadding how much the content is inset from the border
     */
    public void setPadding(float newPadding) {
        padding = newPadding;
    }

    /**
     * @return how much the content is inset from the border
     */

    public float getPadding() {
        return padding;
    }

    public float getTotalInset() {
        return padding + spacing + thickness;
    }

    public float getSpaceAndThickness() {
        return spacing + thickness;
    }

    public boolean equals(Object parm1) {
        if (parm1.getClass() != JoriaBorder.class)
            return false;
        else {
            JoriaBorder b = (JoriaBorder) parm1;
            if (b.lineStyle == NONE && lineStyle == NONE)
                return true;
            return (b.spacing - spacing) < 0.001f && (b.padding - padding) < 0.001f && b.lineStyle == lineStyle && StyleBase.eq(b.color, color);
        }
    }

    protected Object readResolve() {
        if (lineStyle == NONE)
            return zeroBorder;
        else
            return this;
    }

    public float getDrawOffsetFromOutside() {
        return spacing + thickness / 2;
    }

    public float getDrawOffsetFromInside() {
        return padding + thickness / 2;
    }

    public String toString() {
        return "lineStyle: " + lineStyle + " color: " + color + " spacing: " + spacing + " thickness: " + thickness + " padding: " + padding;
    }

    public static boolean isNull(JoriaBorder b) {
        return b == null || b == zeroBorder;
    }
}
