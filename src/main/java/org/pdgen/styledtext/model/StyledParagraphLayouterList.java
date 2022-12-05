// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import org.pdgen.util.ArrayUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.ArrayList;

public class StyledParagraphLayouterList {
    StyledParagraphLayouter[] paragraphs;
    boolean layoutValid;
    ArrayList<LayoutListener> layoutListeners;
    QSELine[] lines;
    private StyledParagraphList srcText;
    private FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);
    protected float targetWidth = 10000;// a large initial value just to keep things qiet, before we have the proper target width
    private float totalHeight;
    public boolean inMouseHighlight;

    public StyledParagraphLayouterList(StyledParagraphList val, float targetWidth) {
        srcText = val;
        paragraphs = new StyledParagraphLayouter[srcText.length()];
        for (int i = 0; i < srcText.length(); i++) {
            StyledParagraph t = srcText.get(i);
            paragraphs[i] = new StyledParagraphLayouter(t);
            paragraphs[i].addlayoutListener(this);
            paragraphs[i].outerLayouter = this;
        }
        this.targetWidth = targetWidth;
    }

    public StyledParagraphLayouterList(StyledParagraphList val, FontRenderContext fontRenderContext, float targetWidth) {
        this(val, targetWidth);
        this.fontRenderContext = fontRenderContext;
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h) {
        if (w < 0)
            return;
        recalc();
        for (StyledParagraphLayouter layouter : paragraphs) {
            layouter.paint(p, x0, y0, w, h);
        }
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, int mark, int dot) {
        if (w < 0)
            return;
        recalc();
        for (StyledParagraphLayouter layouter : paragraphs) {
            layouter.paint(p, x0, y0, w, h, mark, dot);
        }
    }

    public QSELine[] getLines() {
        recalc();
        if (lines != null)
            return lines;
        ArrayList<QSELine> lines = new ArrayList<QSELine>(20);
        for (StyledParagraphLayouter paragraph : paragraphs) {
            paragraph.getLines(lines);
        }
        QSELine[] retVal = new QSELine[lines.size()];
        lines.toArray(retVal);
        this.lines = retVal;
        return retVal;
    }

    public boolean isLayoutValid() {
        return layoutValid;
    }

    public void setTargetWidth(float targetWidth) {
        if (inMouseHighlight)
            System.currentTimeMillis();
        layoutValid = false;
        this.targetWidth = targetWidth;
        for (StyledParagraphLayouter paragraph : paragraphs) {
            paragraph.forceLayoutQuiet();
        }
    }

    public int findParagraphAt(int yAtComp) {
        for (int i = paragraphs.length - 1; i >= 0; i--) {
            StyledParagraphLayouter paragraph = paragraphs[i];
            if (paragraph.getY0() <= yAtComp) {
                return i;
            }
        }
        throw new RuntimeException("Point could not be mapped to paragraph in styled text");
    }

    public void forceLayout() {
        layoutValid = false;
    }

    public void recalc() {
        if (layoutValid || targetWidth < 0)
            return;
        lines = null;
        float y = 0;
        int charCnt = 0;
        for (StyledParagraphLayouter layouter : paragraphs) {
            layouter.recalc(y, fontRenderContext, targetWidth, charCnt);
            y += layouter.getHeight() + layouter.getText().getSpaceBelow();
            charCnt += layouter.getText().length();
        }
        layoutValid = true;
        y -= paragraphs[paragraphs.length - 1].getText().getSpaceBelow();// last space below is not shown
        totalHeight = y;
        fireLayoutDone();
    }

    protected void fireLayoutDone() {
        if (layoutListeners == null)
            return;
        for (LayoutListener layoutListener : layoutListeners) {
            layoutListener.layoutDone();
        }
    }

    public void addlayoutListener(LayoutListener l) {
        if (layoutListeners == null)
            layoutListeners = new ArrayList<LayoutListener>();
        layoutListeners.add(l);
    }

    public void removelayoutListener(LayoutListener l) {
        if (layoutListeners == null)
            return;
        layoutListeners.remove(l);
    }

    public StyledParagraphLayouter get(int ix) {
        if (ix < 0) {
            return null;
        }
        return paragraphs[ix];
    }

    public StyleRunIterator[] getIteratorsPerLine() {
        QSELine[] lines = getLines();
        StyleRunIterator[] retVal = new StyleRunIterator[lines.length];
        if (lines.length == 0)
            return retVal;
        int lineIndex = 0;
        for (StyledParagraphLayouter paragraph : paragraphs) {
            StyleRunIterator iter = new StyleRunIterator(paragraph);
            int linesForParagraph = paragraph.getLines().size();
            for (int j = 0; j < linesForParagraph; j++) {
                retVal[lineIndex++] = iter;
            }
        }
        return retVal;
    }

    public StyleRunIterator[] getIteratorsPerParagraph() {
        StyleRunIterator[] retVal = new StyleRunIterator[paragraphs.length];
        for (int i = 0; i < paragraphs.length; i++) {
            StyledParagraphLayouter paragraph = paragraphs[i];
            retVal[i] = new StyleRunIterator(paragraph);
        }
        return retVal;
    }

	/*
	 public int getCharPosInPar(int charPos)
	 {
		 for (int i = 0; i < paragraphs.length; i++)
		 {
			 StyledParagraphLayouter paragraph = paragraphs[i];
			 int tLen = paragraph.text.length();
			 if (tLen >= charPos)
				 return charPos;
			 charPos -= tLen;
		 }
		 return -1;
	 }
 */

    public int getParCount() {
        return paragraphs.length;
    }

    public void addAttribute(TextAttribute attribute, Object val, int anf, int end, int anfParIx, int endParIx) {
        StyledParagraphLayouter anfPar = paragraphs[anfParIx];
        int anfParAnfCharPosInPar = anf - anfPar.getStartCharPos();
        int anfParEndCharPosInPar = Math.min(end - anfPar.getStartCharPos(), anfPar.getText().length());
        anfPar.getText().addAttribute(attribute, val, anfParAnfCharPosInPar, anfParEndCharPosInPar);
        anfPar.forceLayout();
        for (int i = anfParIx + 1; i < endParIx; i++) {
            StyledParagraphLayouter paragraph = paragraphs[i];
            paragraph.getText().addAttribute(attribute, val);
            paragraph.forceLayoutQuiet();
        }
        if (endParIx > anfParIx) {
            StyledParagraphLayouter endPar = paragraphs[endParIx];
            int endParEndCharPosInPar = end - endPar.getStartCharPos();//  getCharPosInPar(end);
            if (endParEndCharPosInPar > 0) {
                endPar.getText().addAttribute(attribute, val, 0, endParEndCharPosInPar);
                endPar.forceLayoutQuiet();
            }
        }
    }

    public void removeAttribute(TextAttribute attribute, int markCharPos, int dotCharPos, int markParIx, int dotParIx) {
        final int anf, end, anfParIx, endParIx;
        if (markCharPos < dotCharPos) {
            anf = markCharPos;
            anfParIx = markParIx;
            end = dotCharPos;
            endParIx = dotParIx;
        } else {
            anf = dotCharPos;
            anfParIx = dotParIx;
            end = markCharPos;
            endParIx = markParIx;
        }
        StyledParagraphLayouter anfPar = paragraphs[anfParIx];
        int anfParAnfCharPosInPar = anf - anfPar.getStartCharPos();
        int anfParEndCharPosInPar = Math.min(end - anfPar.getStartCharPos(), anfPar.getText().length());
        anfPar.getText().removeAttribute(attribute, anfParAnfCharPosInPar, anfParEndCharPosInPar);
        anfPar.forceLayout();
        for (int i = anfParIx + 1; i < endParIx; i++) {
            StyledParagraphLayouter paragraph = paragraphs[i];
            paragraph.getText().removeAttribute(attribute);
            paragraph.forceLayoutQuiet();
        }
        if (anfParIx < endParIx) {
            StyledParagraphLayouter endPar = paragraphs[endParIx];
            int endParAnfCharPosInPar = Math.max(anf - endPar.getStartCharPos(), 0);
            int endParEndCharPosInPar = end - endPar.getStartCharPos();
            endPar.getText().removeAttribute(attribute, endParAnfCharPosInPar, endParEndCharPosInPar);
            endPar.forceLayoutQuiet();
        }
        forceLayout();
    }

    public void deleteText(int markCharPos, int dotCharPos, int markParIx, int dotParIx) {
        final int anf, end, anfParIx, endParIx;
        if (markCharPos < dotCharPos) {
            anf = markCharPos;
            anfParIx = markParIx;
            end = dotCharPos;
            endParIx = dotParIx;
        } else {
            anf = dotCharPos;
            anfParIx = dotParIx;
            end = markCharPos;
            endParIx = markParIx;
        }
        final boolean includesLast = end == getSrcText().getCharCount();
        StyledParagraphLayouter anfPar = paragraphs[anfParIx];
        int anfParAnfCharPosInPar = anf - anfPar.getStartCharPos();
        int lineLength = anfPar.getEndCharPos() - anf - 1;
        int textLength = end - anf;
        int len = Math.min(textLength, lineLength);
        if (includesLast && anfParIx == endParIx && len != lineLength)
            len--;
        if (len <= 0)
            return;
        anfPar.getText().delete(anfParAnfCharPosInPar, len);
        if (endParIx > anfParIx) {
            StyledParagraphLayouter endPar = paragraphs[endParIx];
            if (end == endPar.getEndCharPos())// range ends at end of endPar: delete endPar as a whole
            {
                srcText.removeParagraphs(anfParIx + 1, endParIx + 1);
                paragraphs = (StyledParagraphLayouter[]) ArrayUtils.remove(paragraphs, anfParIx + 1, endParIx + 1);
            } else {
                int len1 = end - endPar.getStartCharPos();
                endPar.getText().delete(0, len1);
                srcText.removeParagraphs(anfParIx + 1, endParIx);
                paragraphs = (StyledParagraphLayouter[]) ArrayUtils.remove(paragraphs, anfParIx + 1, endParIx);
                endPar.forceLayout();
            }
            mergeParagraphs(anfParIx);
        } else if (end == anfPar.getEndCharPos()) {
            mergeParagraphs(anfParIx);
        } else if (anfParIx + 1 < endParIx) {
            srcText.removeParagraphs(anfParIx + 1, endParIx);
            paragraphs = (StyledParagraphLayouter[]) ArrayUtils.remove(paragraphs, anfParIx + 1, endParIx);
        }
        anfPar.forceLayout();
    }

    /**
     * implementation of a newline action.
     *
     * @param pos   the character position where the break shall occur
     * @param parNo index of paragraph to split
     */
    public void splitParagraph(int pos, int parNo) {
        splitParagraphInner(pos, parNo);
        recalc();
    }

    /**
     * does the paragraph splitting without recalc
     *
     * @param pos   the character position where the break shall occur
     * @param parNo index of paragraph to split
     */
    public void splitParagraphInner(final int pos, final int parNo) {
        srcText.splitParagraphs(pos, parNo);
        paragraphs = (StyledParagraphLayouter[]) ArrayUtils.add(paragraphs, parNo + 1, new StyledParagraphLayouter(srcText.getParagraphs()[parNo + 1]));
        paragraphs[parNo + 1].addlayoutListener(this);
        paragraphs[parNo + 1].outerLayouter = this;
        paragraphs[parNo].forceLayout();
    }

    void checkRuns() {
    }

    public StyledParagraphList getSrcText() {
        return srcText;
    }

    public void setSrcText(StyledParagraphList srcText) {
        this.srcText = srcText;
        forceLayout();
    }

    public float getHeight() {
        return totalHeight;
    }

    public float getTargetWidth() {
        return targetWidth;
    }

    public void setFontRenderContext(FontRenderContext fontRenderContext) {
        this.fontRenderContext = fontRenderContext;
        forceLayout();
    }

    public StyledParagraphLayouter[] getParagraphs() {
        return paragraphs;
    }

    public int findLineAt(float yAtComp) {
        final QSELine[] ll = getLines();
        for (int i = 0; i < ll.length - 1; i++) {
            QSELine l0 = ll[i];
            QSELine l1 = ll[i + 1];
            if (l0.yCell <= yAtComp && l1.yCell > yAtComp)
                return i;
        }
        return ll.length - 1;
    }

    public void mergeParagraphs(int parNo) {
        if (parNo + 1 == paragraphs.length)
            return;
        srcText.mergeParagraphs(parNo);
        paragraphs = (StyledParagraphLayouter[]) ArrayUtils.remove(paragraphs, parNo + 1);
        forceLayout();
        paragraphs[parNo].forceLayoutQuiet();
        recalc();
    }

    public StyledParagraphList getSrcTextNoEdit() {
        return srcText.getNoEditing();
    }

    public String getTextSlice(int c1, int c2, int p1, int p2) {
        final int a, e, ax, ex;
        if (c1 < c2) {
            a = c1;
            ax = p1;
            e = c2;
            ex = p2;
        } else {
            a = c2;
            ax = p2;
            e = c1;
            ex = p1;
        }
        final StyledParagraphLayouter ap = paragraphs[ax];
        int ac = a - ap.getStartCharPos();
        int lineLength = ap.getEndCharPos() - a - 1;
        int textLength = e - a;
        int alen = Math.min(textLength, lineLength);
        final String as = ap.getText().get(ac, alen);
        StringBuffer sb = new StringBuffer(as);
        if (lineLength < textLength)
            sb.append('\n');
        for (int i = ax + 1; i < ex; i++) {
            final GapCharList charList = paragraphs[i].getText();
            final String str = charList.get(0, charList.length() - 1);
            sb.append(str);
            sb.append('\n');
        }
        if (ex > ax) {
            StyledParagraphLayouter ep = paragraphs[ex];
            final GapCharList etl = ep.getText();
            if (e == ep.getEndCharPos()) {
                final String estr = etl.get(0, etl.length() - 1);
                sb.append(estr);
                sb.append('\n');
            } else {
                int elen = e - ep.getStartCharPos();
                final String estr = etl.get(0, elen);
                sb.append(estr);
            }
        }
        return sb.toString();
    }

    public void setSpacing(float newSpacing, int p1, int p2) {
        final int ax, ex;
        if (p1 < p2) {
            ax = p1;
            ex = p2;
        } else {
            ax = p2;
            ex = p1;
        }
        if (ax < 0) {
            paragraphs[ex].getText().setSpaceBelow(newSpacing);
            paragraphs[ex].forceLayoutQuiet();
        } else {
            for (int i = ax; i <= ex; i++) {
                paragraphs[i].getText().setSpaceBelow(newSpacing);
                paragraphs[i].forceLayoutQuiet();
            }
        }
        forceLayout();
    }

    public void setRowSpacing(float newSpacing, int p1, int p2) {
        final int ax, ex;
        if (p1 < p2) {
            ax = p1;
            ex = p2;
        } else {
            ax = p2;
            ex = p1;
        }
        if (ax < 0) {
            paragraphs[ex].getText().setRowSpacing(newSpacing);
            paragraphs[ex].forceLayoutQuiet();
        } else {
            for (int i = ax; i <= ex; i++) {
                paragraphs[i].getText().setRowSpacing(newSpacing);
                paragraphs[i].forceLayoutQuiet();
            }
        }
        forceLayout();
    }

    public void insertTextsAsParagraphs(StyledParagraph[] texts, int at) {
        srcText.insertTextsAsParagraphs(texts, at);
        StyledParagraphLayouter[] newLayouters = new StyledParagraphLayouter[texts.length];
        for (int i = 0; i < texts.length; i++) {
            StyledParagraph text = texts[i];
            newLayouters[i] = new StyledParagraphLayouter(text);
            newLayouters[i].outerLayouter = this;
            newLayouters[i].addlayoutListener(this);
        }
        paragraphs = (StyledParagraphLayouter[]) ArrayUtils.addArray(paragraphs, at, newLayouters);
    }

    public void setCharSpacing(final float val, final int anf, final int end, final int anfParIx, final int endParIx) {
        StyledParagraphLayouter anfPar = paragraphs[anfParIx];
        int anfParAnfCharPosInPar = anf - anfPar.getStartCharPos();
        int anfParEndCharPosInPar = Math.min(end - anfPar.getStartCharPos(), anfPar.getText().length());
        anfPar.getText().addAttribute(TextAttribute.WIDTH, val, anfParAnfCharPosInPar, anfParEndCharPosInPar);
        anfPar.forceLayout();
        for (int i = anfParIx + 1; i < endParIx; i++) {
            StyledParagraphLayouter paragraph = paragraphs[i];
            paragraph.getText().addAttribute(TextAttribute.WIDTH, val);
            paragraph.forceLayoutQuiet();
        }
        if (endParIx > anfParIx) {
            StyledParagraphLayouter endPar = paragraphs[endParIx];
            int endParEndCharPosInPar = end - endPar.getStartCharPos();//  getCharPosInPar(end);
            if (endParEndCharPosInPar > 0) {
                endPar.getText().addAttribute(TextAttribute.WIDTH, val, 0, endParEndCharPosInPar);
                endPar.forceLayoutQuiet();
            }
        }
    }

    public void clearHighlites() {
        for (StyledParagraphLayouter layouter : paragraphs) {
            layouter.clearHighlites();
        }
        forceLayout();

    }

    public void highlightText(final String s) {
        for (StyledParagraphLayouter layouter : paragraphs) {
            layouter.highlightText(s);
        }
        //forceLayout();
    }
}
