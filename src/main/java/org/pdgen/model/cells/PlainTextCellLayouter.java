// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.style.CellStyle;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

public class PlainTextCellLayouter implements TextCellLayouter {
    SimpleTextCellDef myCell;
    protected transient float baseline;
    protected transient Rectangle2D.Float uline;
    String myTxt;

    public PlainTextCellLayouter(SimpleTextCellDef cell) {
        myCell = cell;
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        //Trace.log(0,"plainPaint x" + x0 + "y "+ y0 + "w " + w + " h " + h);
        if (myTxt == null || myTxt.length() == 0)
            return;
        CellStyle cs = myCell.getCascadedStyle();
        p.drawString(myTxt, x0, y0 - baseline);
        if (cs.getUnderlined()) {
            uline.x = x0;
            uline.y = y0 - uline.y;
            p.fill(uline);
            uline.y = y0 - uline.y;
        }
    }

    public void calcSize(Locale loc, Graphics2D g) {
        Font f = myCell.getCascadedStyle().getStyledFont();
        CellStyle cs = myCell.getCascadedStyle();
        myTxt = myCell.getWrappedText(loc);
        if (myTxt == null || myTxt.length() == 0)
            return;

//        FontRenderContext fontRenderContext = JoriaIF.instance().getFontRenderContext();
        FontRenderContext fontRenderContext = g.getFontRenderContext();
        Rectangle2D preferredSize = f.getStringBounds(myTxt, fontRenderContext);
        myCell.myWidth += preferredSize.getWidth();
        myCell.myHeight += preferredSize.getHeight();
        baseline = (float) preferredSize.getY();
        if (-baseline > preferredSize.getHeight())
            myCell.myHeight += -baseline;
        if (cs.getUnderlined()) {
            LineMetrics lm = f.getLineMetrics(myCell.myText, fontRenderContext);
            uline = new Rectangle2D.Float(0, lm.getUnderlineOffset() + baseline, (float) preferredSize.getWidth(), lm.getUnderlineThickness());
        }
    }

}
