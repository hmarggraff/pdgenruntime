// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.Internationalisation;
import org.pdgen.data.NotYetImplementedError;
import org.pdgen.data.Trace;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.util.StringUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.util.Locale;

public class HtmlLayouter implements TextCellLayouter {
    View hv1;
    SimpleTextCellDef myCell;
    static ThreadData tc = new ThreadData();

    public HtmlLayouter(SimpleTextCellDef cell) {
        myCell = cell;
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        Trace.logDebug(Trace.layout, "hPaint x" + x0 + "y " + y0 + "w " + w + " h " + h);
        if (hv1 == null)
            calcSize(Internationalisation.NOREPLACE, p);
        if (hv1 == null)
            return;
        Rectangle r = new Rectangle(Math.round(x0), Math.round(y0), Math.round(w), Math.round(h));
        hv1.paint(p, r);
    }

    public static boolean checkTextCompatible(String toCheck) {
        try {
            if (toCheck != null && toCheck.startsWith("<!DOCTYPE"))
                toCheck = toCheck.substring(toCheck.indexOf('>') + 1).trim();
            if (toCheck != null && !BasicHTML.isHTMLString(toCheck))
                toCheck = "<html>" + toCheck;
            BasicHTML.createHTMLView(getTc(), toCheck);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void calcSize(Locale loc, Graphics2D g) {
        String myTxt = StringUtils.trimNull(myCell.getWrappedText(loc));
        if (myTxt != null && myTxt.startsWith("<!DOCTYPE"))
            myTxt = myTxt.substring(myTxt.indexOf('>') + 1).trim();
        if (myTxt != null && !BasicHTML.isHTMLString(myTxt))
            myTxt = "<html>" + myTxt;
        if (myTxt == null)
            return;
        getTc().setBackground(myCell.getCascadedStyle().getBackground());
        getTc().setFont(myCell.getCascadedStyle().getStyledFont());
        hv1 = BasicHTML.createHTMLView(getTc(), myTxt);
        CellStyle cs = myCell.getCascadedStyle();
        boolean hasFixWidth = true;
        int fixedWidth = 0;
        int col = myCell.getGrid().getCellPosition(myCell).x;
        for (int i = 0; i < cs.getSpanHorizontal(); i++) {
            if (myCell.getGrid().getColSizingAt(col + i).getUnit() > FlexSize.flex)
                fixedWidth += myCell.getGrid().getColSizingAt(col + i).getVal();
            else {
                hasFixWidth = false;
                break;
            }
        }
        if (hasFixWidth) {
            hv1.setSize(fixedWidth, Float.MAX_VALUE);
            myCell.myWidth = fixedWidth;
            myCell.myHeight = hv1.getPreferredSpan(View.Y_AXIS);
        } else {
            myCell.myWidth = hv1.getPreferredSpan(View.X_AXIS);
            myCell.myHeight = hv1.getPreferredSpan(View.Y_AXIS);
        }
        Trace.logDebug(Trace.layout, "HTMLsize w " + myCell.myWidth + " h " + myCell.myWidth);
    }

    public String[] getStrings() {
        throw new NotYetImplementedError("Not yet implemented");
    }

    public static JComponent getTc() {

        return (JComponent) tc.get();
    }

    static class ThreadData extends ThreadLocal {

        protected Object initialValue() {
            return new JPanel();
        }
    }
}
