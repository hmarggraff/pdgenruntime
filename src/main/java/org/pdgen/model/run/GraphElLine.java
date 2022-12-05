// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.style.JoriaSimpleBorder;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.PrintStream;

public class GraphElLine extends Line2D.Float implements GraphicElement {

    private static final long serialVersionUID = 7L;
    int lineStyle;
    Color color;
    float thickness;

    public GraphElLine(float x1, float y1, float x2, float y2, float thickness, int lineStyle, Color color) {
        super(x1, y1, x2, y2);
        this.thickness = thickness;
        this.lineStyle = lineStyle;
        this.color = color;
    }

    public void print(JoriaPrinter pr) {
        pr.printGELine(this);
    }

    public float getContentWidth() {
        return x2 - x1;
    }

    public float getPreferredHeight() {
        return y2 - y1;
    }

    public void setHeight(float h) {
        // ignore
    }

    public void dump(PrintStream ww) {
        float x, y, w, h;
        if (x1 == x2) {
            x = (x1 - thickness / 2);
            w = thickness;
            h = (y2 - y1);
            y = y1;
        } else {
            y = (y1 - thickness / 2);
            h = thickness;
            w = (x2 - x1);
            x = x1;
        }
        ww.println("line\t" + x + "\t" + y + "\t" + w + "\t" + h + "\t" + thickness + "\t" + x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);

    }

    public float getHeightFloat() {
        return 0;
    }

    public void translate(float offsetx, float offsety) {
        x1 += offsetx;
        x2 += offsetx;
        y1 += offsety;
        y2 += offsety;
    }

    boolean isHorizantal() {
        return x1 != x2;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GraphElLine))
            return false;
        GraphElLine gel = (GraphElLine) obj;
        return super.equals(obj) && color.equals(gel.color) && thickness == gel.thickness && lineStyle == gel.lineStyle;
    }

    public String toString() {
        float x, y, w, h;
        if (x1 == x2) {
            x = (x1 - thickness / 2);
            w = thickness;
            h = (y2 - y1);
            y = y1;
        } else {
            y = (y1 - thickness / 2);
            h = thickness;
            w = (x2 - x1);
            x = x1;
        }
        return ("line x:" + x + " y:" + y + " w:" + w + " h:" + h + " thickness:" + thickness + " x1:" + x1 + " y1;" + y1 + " x2;" + x2 + " y2;" + y2 + " color;" + color + " style;" + JoriaSimpleBorder.styleToString(lineStyle));

    }

    public int hashCode() {
        return super.hashCode() ^ color.hashCode() ^ java.lang.Float.floatToRawIntBits(thickness) ^ lineStyle;
    }

    public boolean extend(GraphElLine newLine) {
        if (lineStyle != newLine.lineStyle || !color.equals(newLine.color) || thickness != newLine.thickness || isHorizantal() != newLine.isHorizantal())
            return false;
        if (x2 == newLine.x1 && y2 == newLine.y1 || x1 <= newLine.x1 && y1 < newLine.y1 && x2 >= newLine.x2 && y2 >= newLine.y2) {
            x2 = newLine.x2;
            y2 = newLine.y2;
            return true;
        } else
            return false;
    }
}
