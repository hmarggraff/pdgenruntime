// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;

import javax.swing.*;
import java.awt.*;

public class GraphElPicture extends GraphElContent {

    private static final long serialVersionUID = 7L;
    Icon img;
    Object storedData;
    float scale;
    boolean hide;
    boolean spread;

    private GraphElPicture(GraphElPicture from) {
        super(from);
        img = from.img;
        storedData = from.storedData;
        scale = from.scale;
        spread = from.spread;
    }

    GraphElPicture(Object sd, Icon img, Color background, float scale, CellDef src, boolean spread, ImageIcon backgroundImage) {
        super(background, src, backgroundImage);
        this.img = img;
        storedData = sd;
        this.scale = scale;
        this.spread = spread;
    }

    public void print(JoriaPrinter pr) {
        pr.printGEPicture(this);
    }

    public void setContentX(float xContent, float wEnvelope, float wContent, CellStyle cs, Graphics2D g, float xEnvelope) {
        if (spread) {
            this.xContent = xContent;
            this.wContent = wEnvelope;
            this.xEnvelope = xEnvelope;
            this.wEnvelope = wEnvelope;
        } else
            super.setContentX(xContent, wEnvelope, wContent, cs, g, xEnvelope);
    }

    public GraphElContent copy() {
        return new GraphElPicture(this);
    }

    public void setHeight(float h, float align, float innerHeight, float hEnvelope) {
        if (spread) {
            height = h;
            hContent = innerHeight;
            this.hEnvelope = hEnvelope;
        } else {
            super.setHeight(h, align, innerHeight, hEnvelope);
        }
    }

    public void setScale(float scale) {
        this.scale = scale;

    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(final boolean hide) {
        this.hide = hide;
    }

}
