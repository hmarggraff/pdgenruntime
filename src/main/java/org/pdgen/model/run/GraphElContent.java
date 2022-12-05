// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;

public abstract class GraphElContent extends Rectangle2D.Float implements GraphicElement {
    private static final long serialVersionUID = 7L;
    protected float xContent;// horizontal offset in bounding text rectangle, includes alignment
    protected float yContent;// offset in bounding text rectangle, includes alignment
    protected float wContent;
    protected float hContent;
    Color color;
    DrillDownLink drilldownObject;
    CellDef src;
    protected float hEnvelope;
    protected float wEnvelope;
    protected float xEnvelope;
    protected float yEnvelope;
    protected ImageIcon background;

    protected GraphElContent(Color color, CellDef srcCell, ImageIcon background) {
        //Trace.check(srcCell);
        src = srcCell;
        this.color = color;
        this.background = background;
    }

    protected GraphElContent(GraphElContent from) {
        copy(from);
    }

    public void setHeight(float h, float align, float innerHeight, float hEnvelope) {
        yContent += align;
        yEnvelope += align;
        height = h;
        hContent = innerHeight;
        this.hEnvelope = hEnvelope;
    }

    public float getxContent() {
        return xContent;
    }

    public CellDef getSource() {
        return src;
    }

    public void setContentX(float xContent, float wEnvelope, float wContent, CellStyle cs, Graphics2D g, float xEnvelope) {
        float align = 0;
        if (cs != null)
            align = cs.getAlignmentHorizontal().getAlign();
        this.xContent = xContent + Math.max(0, (wEnvelope - wContent)) * align;
        this.wContent = Math.min(wContent, wEnvelope);
        this.xEnvelope = xEnvelope;
    }

    public DrillDownLink getDrilldownObject() {
        return drilldownObject;
    }

    public void dump(PrintStream w) {
        w.println("text\t" + x + "\t" + y + "\t" + width + "\t" + height + "\t" + xContent + "\t" + yContent);
    }

    public float getHeightFloat() {
        return height;
    }

    public void translate(float offsetx, float offsety) {
        x += offsetx;
        xContent += offsetx;
        xEnvelope += offsetx;
        y += offsety;
        yContent += offsety;
        yEnvelope += offsety;
    }

    public void setContentClipRectangle(Rectangle2D clip) {
        clip.setRect(xContent, yContent, wContent, hContent);
    }

    public abstract GraphElContent copy();

    protected void copy(GraphElContent from) {
        height = from.height;
        width = from.width;
        x = from.x;
        y = from.y;
        xContent = from.xContent;
        yContent = from.yContent;
        wContent = from.wContent;
        hContent = from.hContent;
        color = from.color;
        drilldownObject = from.drilldownObject;
        src = from.src;
        xEnvelope = from.xEnvelope;
        yEnvelope = from.yEnvelope;
        wEnvelope = from.wEnvelope;
        hEnvelope = from.hEnvelope;
        background = from.background;
    }

    public void setBackgroudClip(Rectangle2D clip) {
        clip.setRect(xEnvelope, yEnvelope, wEnvelope, hEnvelope);
    }

    public ImageIcon getBackgroundImage() {
        return background;
    }
}
