// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.Trace;
import org.pdgen.model.style.CellStyle;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowingTextCellLayouter implements TextCellLayouter {
    SimpleTextCellDef myCell;
    ArrayList<TextLayout> layouts = new ArrayList<TextLayout>();
    ArrayList<String> subStrings = new ArrayList<String>();
    List<List<String>> subTexts = new ArrayList<List<String>>();
    List<List<Float>> subOffsets = new ArrayList<List<Float>>();
    AttributedString as;
    String myText;

    public FlowingTextCellLayouter(SimpleTextCellDef cell) {
        myCell = cell;
    }

    protected boolean stringIt(Locale loc) {
        CellStyle cs = myCell.getCascadedStyle();
        String txt = myCell.getWrappedText(loc);
        if (txt != null && txt.equals(myText))
            return false;
        myText = txt;
        if (txt != null && txt.length() != 0) {
            as = new AttributedString(txt);
            as.addAttribute(TextAttribute.FONT, cs.getStyledFont());
            if (cs.getUnderlined())
                as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
            return false;
        } else
            return true;
    }

    float breakTo(float w, Locale loc, FontRenderContext frc, Graphics2D g) {
        CellStyle cs = myCell.getCascadedStyle();
        if (w < 0)
            return 0.0f;
        if (stringIt(loc))
            return 0.0f;
        layouts.clear();
        subStrings.clear();
        subTexts.clear();
        subOffsets.clear();
        float resultHeight = 0.0f;
        if (as == null)
            return resultHeight;
        AttributedCharacterIterator ati = as.getIterator();
        LineBreakMeasurer lbm = new LineBreakMeasurer(ati, BreakIterator.getLineInstance(loc), frc);
        int lastPos = 0;
        float lineSpacing = 1;
        if (cs.getLineSpacingNumber() != null)
            lineSpacing = cs.getLineSpacingNumber();
        BreakIterator bi = BreakIterator.getWordInstance(loc);
        while (lbm.getPosition() < ati.getEndIndex()) {
            TextLayout l = lbm.nextLayout(w);
            resultHeight += (l.getAscent() + l.getDescent() + l.getLeading()) * lineSpacing;
            String text = myText.substring(lastPos, lbm.getPosition());
            subStrings.add(text);
            lastPos = lbm.getPosition();
            layouts.add(l);
            boolean isBlock = myCell.getCascadedStyle().getAlignmentHorizontal().isBlock();
            if (lbm.getPosition() < ati.getEndIndex() && isBlock) {
                List<String> subText = new ArrayList<String>();
                subTexts.add(subText);
                List<Float> subOffset = new ArrayList<Float>();
                subOffsets.add(subOffset);
                List<Float> length = new ArrayList<Float>();
                bi.setText(text);
                int start = bi.first();
                String sub = null;
                float lineWidth = 0;
                for (int end = bi.next(); BreakIterator.DONE != end; start = end, end = bi.next()) {
                    String part = text.substring(start, end).trim();
                    if (part.length() == 0 && sub != null) {
                        subText.add(sub);
                        float width = cs.getWidth(sub, g);
                        length.add(width);
                        lineWidth += width;
                        sub = null;
                    } else if (part.length() > 0) {
                        if (sub == null)
                            sub = part;
                        else
                            sub += part;
                    }
                }
                if (sub != null) {
                    subText.add(sub);
                    float width = cs.getWidth(sub, g);
                    length.add(width);
                    lineWidth += width;
                }
                if (subText.size() > 1) {
                    float delta = (w - lineWidth) / (subText.size() - 1);
                    float offset = 0;
                    for (int i = 0; i < subText.size(); i++) {
                        subOffset.add(offset);
                        offset += delta + length.get(i);
                    }
                } else {
                    subOffset.add(0.0f);
                }

            } else if (isBlock) {
                subTexts.add(null);
                subOffsets.add(null);
            }
        }
        Trace.logDebug(Trace.layout, "floatHeight = " + resultHeight);
        return resultHeight;
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        // TODO add Block Align
        if (w < 0)
            return;
        float y = y0;
        float lineSpacing = 1;
        CellStyle cs = myCell.getCascadedStyle();
        if (cs.getLineSpacingNumber() != null)
            lineSpacing = cs.getLineSpacingNumber();
        for (int i = 0; i <= layouts.size() - 1; i++) {
            TextLayout l = layouts.get(i);
            y += l.getAscent();
            if (cs.getAlignmentHorizontal().isBlock()) {
                if (subTexts.get(i) == null)
                    l.draw(p, x0, y);
                else {
                    List<String> subText = subTexts.get(i);
                    List<Float> subOffset = subOffsets.get(i);
                    for (int j = 0; j < subText.size(); j++) {
                        p.drawString(subText.get(j), x0 + subOffset.get(j), y);
                    }
                    if (cs.getUnderlined()) {
                        FontRenderContext frc = p.getFontRenderContext();
                        LineMetrics lm = cs.getStyledFont().getLineMetrics(subStrings.get(i), frc);
                        Rectangle2D.Float uline = new Rectangle2D.Float(
                                x0, lm.getUnderlineOffset() + y,
                                w, lm.getUnderlineThickness());
                        p.fill(uline);
                    }
                }
            } else {
                float offset = (w - (float) l.getBounds().getWidth()) * cs.getAlignmentHorizontal().getAlign();
                l.draw(p, x0 + offset, y);
            }
            y += l.getDescent() + l.getLeading() + (lineSpacing - 1) * (l.getAscent() + l.getDescent() + l.getLeading());
        }
    }

    public void calcSize(Locale loc, Graphics2D g) {
        float myWidth = myCell.myWidth;
        myWidth -= myCell.cascadedStyle.getLeftRightPaddingValue();
        if (myWidth < myCell.getCascadedStyle().getSize().getValInPoints())
            myWidth = myCell.getCascadedStyle().getSize().getValInPoints() * 20;
        myCell.myHeight = breakTo(myWidth, loc, g.getFontRenderContext(), g) + myCell.cascadedStyle.getTopBotPaddingValue();
    }
}
