// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaDataException;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.FrameStyle;
import org.pdgen.model.style.JoriaFrameBorder;

import java.awt.*;

// Created 13.01.2008 by hmf
public class FillInnerFrame extends FillPagedFrame implements Paragraph {
    FillPagedFrame outer;
    int col;
    CellDef unSplit;
    int linePos;
    int lastLinePos;
    float contentX;// border modifier for pos of graphic content
    float contentY;// border modifier for pos of graphic content
    float contentWidth;// border modifier for pos of graphic content
    float innerX;// border modifier for pos of graphic content
    float innerY;// border modifier for pos of graphic content
    float envelopeWidth;// border modifier for pos of graphic content
    float hDiff;
    float wDiff;
    int remainingSpan;
    int savedLinePos;

    /**
     * creates FillBox for a breakable Frame e.g. a body or a nested frame
     * this constructor must be used in conjunction with hFill to pass in the actual remaining height on the page
     *
     * @param template frame to run
     * @param outer    the parent
     * @param defs     the structure definitions
     * @param subVals  the values for this inner frame
     * @param col      the column index in the outer template model
     * @param unSplit  the cell def that this splitter is based on
     * @throws JoriaDataException in case of problems
     */
    public FillInnerFrame(final TemplateModel template, OutputMode outer, final RDRange defs, final RVTemplate subVals, int col, CellDef unSplit) throws JoriaDataException {
        super(template, (FillPagedFrame) outer);
        this.col = col;
        this.outer = (FillPagedFrame) outer;
        this.unSplit = unSplit;
        innerInit(defs, subVals);
    }

    public void setup(float contentX, float contentY, float contentWidth, float innerX, float innerY, float hDiff, float wDiff, float envelopeWidth) {
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentWidth = contentWidth;
        this.innerX = innerX;
        this.innerY = innerY;
        this.envelopeWidth = envelopeWidth;
        this.hDiff = hDiff;
        this.wDiff = wDiff;
    }

    public float nextSlice(TableBorderRequirements b, float height, float x, float y, float w, Graphics2D g, boolean fixedHeight, FillPagedFrame fpf) {
        if (colPos[0] != x) // newspaper style columns in outer frame: adjust
        {
            final float shift = x - colPos[0];
            for (int i = 0; i < colPos.length; i++) {
                colPos[i] += shift;
            }
        }
        final float ret = hFill(height, y, outer.dest, outer.firstBodyOnPage);
        float usedHeight = outer.remainingHeight - ret;
        b.hContent = usedHeight;
        return ret;
    }

    public boolean more() {
        return lastRowState != LastRowState.doneComplete && lastRowState != LastRowState.endOfData;
    }

    public void backupSlice() {
        linePos = lastLinePos;
        lastLinePos = 0;
    }

    public void saveSliceState() {
        savedLinePos = linePos;
    }

    public void restoreSliceState() {
        linePos = savedLinePos;
    }


    public CellDef getUnSplit() {
        return unSplit;
    }

    public float getInnerX() {
        return innerX;
    }

    public float getInnerY() {
        return innerY;
    }

    public float roundUp(float smoothColHeight) {
        return smoothColHeight + 24; // just guess
    }

    public float getContentWidth() {
        return contentWidth;
    }

    public float getContentX() {
        return contentX;
    }

    public float getContentY() {
        return contentY;
    }

    public float getEnvelopeWidth() {
        return envelopeWidth;
    }

    public int getRemainingSpan() {
        return remainingSpan;
    }

    protected float layout() {
        if (template.getRowCount() == 0 || nCols == 0)
            return 0;
        final float x0 = outer.colPos[col];
        final float w = outer.colWidths[col];
        NestedFrameColumnLayouter layouter = new NestedFrameColumnLayouter(colWidths, maxColWidths, colPos, colReq, dynaCols, template, getRunEnv().getTemplate().getPage().getCascadedPageStyle());
        return layouter.layout(x0, w);
    }

    protected void frameBorders() {
        FrameStyle frameStyle = template.getFrame().getCascadedFrameStyle();
        JoriaFrameBorder t = frameStyle.getTopBorder();
        JoriaFrameBorder l = frameStyle.getLeftBorder();
        JoriaFrameBorder b = frameStyle.getBottomBorder();
        JoriaFrameBorder r = frameStyle.getRightBorder();
        float w = frameStyle.getColumnWidth(outer.colWidths[col]);
        float x0 = colPos[0] + l.getOuterSpacing().getValInPoints();
        float y0 = verticalStart;
        float x1 = x0 + w - r.getOuterSpacing().getValInPoints();
        float y1;
        if (frameStyle.getHeight().isExpandable())
            y1 = verticalPos + b.getInnerPadding().getValInPoints();
        else {
            y1 = verticalStart + frameStyle.getHeight().getVal() - b.getOuterSpacing().getValInPoints();
        }
        scaleBackgroundImage(x0, x1, y0, y1);
        if (t.getThickness() > 0)
            lDestBorderLines.add(new GraphElLine(x0, y0 + t.getThickness() / 2, x1, y0 + t.getThickness() / 2, t.getThickness(), t.getLineStyle(), t.getColor()));
        if (l.getThickness() > 0)
            lDestBorderLines.add(new GraphElLine(x0 + l.getThickness() / 2, y0, x0 + l.getThickness() / 2, y1, l.getThickness(), l.getLineStyle(), l.getColor()));
        if (r.getThickness() > 0)
            lDestBorderLines.add(new GraphElLine(x1 - r.getThickness() / 2, y0, x1 - r.getThickness() / 2, y1, r.getThickness(), r.getLineStyle(), r.getColor()));
        final float yBot = y1 + b.getThickness() / 2;
        if (b.getThickness() > 0)
            lDestBorderLines.add(new GraphElLine(x0, yBot, x1, yBot, b.getThickness(), b.getLineStyle(), b.getColor()));
    }
}
