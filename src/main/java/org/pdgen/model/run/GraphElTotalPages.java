// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DeferredTotalPagesCell;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class GraphElTotalPages extends GraphElContent implements GrahElPostprocess {
    private static final long serialVersionUID = 7L;
    GraphElText inner;
    public int posInDisplayList;

    private GraphElTotalPages(GraphElTotalPages from) {
        super(from);
        inner = (GraphElText) from.inner.copy();
        posInDisplayList = from.posInDisplayList;
    }

    public GraphElTotalPages(GraphElText from, CellDef config, ImageIcon backgroundImage) {
        super(from.style.getBackground(), config, backgroundImage);
        inner = from;
        x = from.x;
        y = from.y;
        width = from.width;
        height = from.height;
        xContent = from.xContent;
        yContent = from.yContent;
        color = from.color;
        color = from.color;
        drilldownObject = from.drilldownObject;
    }

    public void dump(PrintStream w) {
        inner.dump(w);
    }

    public float getHeightFloat() {
        return inner.getHeightFloat();
    }

    public void translate(float offsetx, float offsety) {
        super.translate(offsetx, offsety);
        inner.translate(offsetx, offsety);
    }

    public GraphElContent copy() {
        return new GraphElTotalPages(this);
    }

    public void print(JoriaPrinter pr) {
        inner.print(pr);
    }

    public void setHeight(float h, float align, float innerHeight, float hEnvelope) {
        inner.setHeight(h, align, innerHeight, hEnvelope);
    }

    public void setContentX(float xContent, float wEnvelope, float wContent, CellStyle cs, Graphics2D g, float xEnvelope) {
        super.setContentX(xContent, wEnvelope, wContent, cs, g, xEnvelope);
        inner.setContentX(xContent, wEnvelope, wContent, cs, g, xEnvelope);
    }

    /**
     * this method is used to patch the text after all pages have been built.
     *
     * @param newText new text must be shorter than the old text (6 digits)
     * @param g       the graphics context
     */

    public void setText(String newText, Graphics2D g) {
        final HorizontalAlignment ah = inner.style.getAlignmentHorizontal();
        inner.txt = newText;
        if (ah.isMid()) {
            float delta = inner.wContent - inner.getContentWidth(g);
            inner.xContent = inner.xContent + delta / 2;
        } else if (ah.isEnd()) {

            float delta = inner.wContent - inner.getContentWidth(g);
            inner.xContent = inner.xContent + delta;
        }
    }

    public DeferredTotalPagesCell getPostprocessSource() {
        return (DeferredTotalPagesCell) src;
    }

    public int getPosInDisplayList() {
        return posInDisplayList;
    }

    public void setPosInDisplayList(final int posInDisplayList) {
        this.posInDisplayList = posInDisplayList;
    }
}
