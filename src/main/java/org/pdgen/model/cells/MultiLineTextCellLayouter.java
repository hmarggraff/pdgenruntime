// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.style.CellStyle;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Locale;

public class MultiLineTextCellLayouter implements TextCellLayouter {
    // TODO LineSpacing & BLOCK Layout
    SimpleTextCellDef myCell;
    protected transient float baseline;
    protected transient Rectangle2D.Float uline;
    ArrayList<String> lines = new ArrayList<String>();
    float lHeight;

    public MultiLineTextCellLayouter(SimpleTextCellDef cell) {
        myCell = cell;
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        if (w < 0)
            return;
        float y = y0;
        for (int i = 0; i <= lines.size() - 1; i++) {
            String chars = lines.get(i);
            p.drawString(chars, x0, y - baseline);
            if (myCell.getCascadedStyle().getUnderlined()) {
                uline.x = x0;
                uline.y = y - uline.y;
                p.fill(uline);
                uline.y = y - uline.y;
            }
            y += lHeight;
        }
    }

    public void calcSize(Locale loc, Graphics2D g) {
        Rectangle2D preferredSize;
        double h = 0;
        double w = 0;
        CellStyle cs = myCell.getCascadedStyle();
        Font f = cs.getStyledFont();
        String t = myCell.getWrappedText(loc);
        lines.clear();
        int ix = 0;
        int start = 0;
        int end = t.length();
        FontRenderContext fontRenderContext = g.getFontRenderContext();
        while (ix < end) {
            char ch = t.charAt(ix);
            if (ch == '\n') {
                String l = t.substring(start, ix);
                lines.add(l);
                preferredSize = f.getStringBounds(l, fontRenderContext);
                h += preferredSize.getHeight();
                w = Math.max(w, preferredSize.getWidth());
                start = ix + 1;
            }
            ix++;
        }
        String l = t.substring(start, end);
        lines.add(l);
        preferredSize = f.getStringBounds(l, fontRenderContext);
        h += preferredSize.getHeight();
        w = Math.max(w, preferredSize.getWidth());

        myCell.myWidth += w;
        myCell.myHeight += h;
        lHeight = (float) preferredSize.getHeight();
        baseline = (float) preferredSize.getY();
        if (cs.getUnderlined()) {
            LineMetrics lm = f.getLineMetrics(t, fontRenderContext);
            uline = new Rectangle2D.Float(0, lm.getUnderlineOffset() + baseline, (float) w, lm.getUnderlineThickness());
        }
    }

}
