// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.style.CellStyle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Locale;

public class VerticalTextCellLayouter extends PlainTextCellLayouter {
    public VerticalTextCellLayouter(SimpleTextCellDef cell) {
        super(cell);
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        if (myTxt == null || myTxt.length() == 0)
            return;
        CellStyle cs = myCell.getCascadedStyle();
        final AffineTransform affineTransform = p.getTransform();
        if (cs.getTextType().equals(CellStyle.vertBottomUp)) {
            p.rotate(-Math.PI / 2, x0 + h / 2, y0 + h / 2);
            //p.translate(0,myCell.myHeight-);
        } else if (cs.getTextType().equals(CellStyle.vertTopDown)) {
            p.rotate(Math.PI / 2, x0 + w / 2, y0 + w / 2);
            //p.rotate(Math.PI/2);
            //p.translate(w,0);
        }
        p.drawString(myTxt, x0, y0 - baseline);
        if (cs.getUnderlined()) {
            uline.x = x0;
            uline.y = y0 - uline.y;
            p.fill(uline);
            uline.y = y0 - uline.y;
        }
        p.setTransform(affineTransform);
    }

    public void calcSize(Locale loc, Graphics2D g) {
        super.calcSize(loc, g);
        final float height = myCell.myHeight;
        //noinspection SuspiciousNameCombination
        myCell.myHeight = myCell.myWidth;
        //noinspection SuspiciousNameCombination
        myCell.myWidth = height;
    }
}
