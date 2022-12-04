// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.*;

import org.pdgen.model.TemplateModel;
import org.pdgen.data.Trace;

/**
 * User: Patrick
 * Date: Apr 24, 2006
 * Time: 7:47:32 AM
 */
public class ColumnLayouter
{
    protected float[] colWidths;
    protected float[] maxColWidths;
    protected float[] colPos;
    protected float[][] colReq;
    protected int dynaCols;
    protected TemplateModel template;
    private final PageStyle ps;

    public ColumnLayouter(float[] colWidths, float[] maxColWidths, float[] colPos, float[][] colReq, int dynaCols, TemplateModel template, PageStyle ps)
    {
        this.colWidths = colWidths;
        this.maxColWidths = maxColWidths;
        this.colPos = colPos;
        this.colReq = colReq;
        this.dynaCols = dynaCols;
        this.template = template;
        this.ps = ps;
    }

    public float layout()
    {
        boolean[] flowerCol = calcColWidths();

        float minWidth = 0;
        float maxWidth = 0;
        for (int cix = 0; cix < dynaCols; cix++)
        {
            minWidth += colWidths[cix];
            maxWidth += maxColWidths[cix];
        }

        FrameStyle frameStyle = template.getFrame().getCascadedFrameStyle();
        float fPos;
        float w;
        {
            FlexSize frameFlexWidth = frameStyle.getWidth();
            if (frameStyle.getXPos() != null && frameStyle.getXPos().getUnit() != FlexSize.flex)
                w = ps.getPageWidth() - frameStyle.getXPos().getVal() - ps.getRightMargin().getValInPoints();
            else
                w = ps.getBodyWidth();
            if (frameFlexWidth.getUnit() == FlexSize.flex)
            {
                if (frameFlexWidth.getVal() == 0)
                    w = Math.min(w, maxWidth + frameStyle.getHorizontalInset());
            }
            else
                w = frameFlexWidth.getVal();
	        w = frameStyle.getColumnWidth(w);
	        w = w - frameStyle.getHorizontalInset();
            // Hier wird die horizontale Position des Frames berechnet: relativ zu dieser Position werden die Zellen plaziert.
            if (frameStyle.getXPos() != null && frameStyle.getXPos().getUnit() != FlexSize.flex)
                fPos = frameStyle.getXPos().getVal() + frameStyle.getLeftBorder().getTotalInset();
            else
                fPos = ps.getLeftMargin().getValInPoints() + frameStyle.getLeftBorder().getTotalInset();
        }
        Integer fill = frameStyle.getFill();
        //------------
        if (maxWidth == w || (!StyleBase.FillBoth.equals(fill) && !StyleBase.FillHorizontal.equals(fill) && !StyleBase.FillSymmetric.equals(fill))) // space matches requirement
        {
            for (int cix = 0; cix < dynaCols; cix++)
            {
                colPos[cix] = fPos;
                fPos += colWidths[cix];
            }
        }
        else if (maxWidth <= w) // spread all
        {
            spread(w, maxWidth, fPos);
        }
        else if (minWidth <= w) // spread flowing cells only
        {
            if (!spread4Reflow(flowerCol, w, minWidth, fPos)) // no reflowers, just spans
                spread(w, minWidth, fPos);
        }
        else
        // squeeze
        {
            squeeze(w, minWidth, fPos);
        }
        colPos[dynaCols] = colPos[dynaCols - 1] + colWidths[dynaCols - 1]; // right end of columns: keeping this makes life easier for frame borders etc.
	    return w;
   }
    protected boolean[] calcColWidths()
    {
        boolean[] flowerCol = new boolean[dynaCols];
        // calc column width taking spans and flowed cell into account
        boolean spans1 = false;
        for (int rix = 0; rix < colReq.length; rix++)
        {
            int tcIx = 0;
            for (int cix = 0; cix < dynaCols; cix++)
            {
                CellDef cd;
                cd = template.cellAt(rix, tcIx);
                if (cd != null)
                {
                    CellStyle cs = cd.getCascadedStyle();
                    int hSpan = cs.getSpanHorizontal();
                    final boolean flowCell = cd.isReflowing();
                    final FlexSize colSizingAt = template.getColSizingAt(tcIx);
                    final boolean expandCol = colSizingAt.isExpandable();
                    if (expandCol)
                    {
                        flowerCol[cix] |= flowCell;

                        if (hSpan != 1)
                            spans1 = true;
                        else if (flowCell)
                        {
                            colWidths[cix] = Math.max(colWidths[cix], Math.min(colReq[rix][cix], cs.getSize().getValInPoints() * 4));
                            maxColWidths[cix] = Math.max(maxColWidths[cix], colReq[rix][cix]);
                        }
                        else
                        {
                            colWidths[cix] = Math.max(colWidths[cix], colReq[rix][cix]);
                            maxColWidths[cix] = Math.max(maxColWidths[cix], colReq[rix][cix]);
                        }
                    }
                    else
                    {
                        flowerCol[cix] |= cd.isReflowing();
                        colWidths[cix] = colSizingAt.getVal();
                        maxColWidths[cix] = colWidths[cix];
                        if (hSpan != 1)
                            spans1 = true;
                    }
                }
                else
                {
                    final FlexSize colSizingAt = template.getColSizingAt(tcIx);
                    final boolean expandCol = colSizingAt.isExpandable();
                    if(!expandCol)
                    {
                        colWidths[cix] = colSizingAt.getVal();
                        maxColWidths[cix] = colSizingAt.getVal();
                    }
                }
                tcIx = nextDefCol(tcIx, cix, dynaCols);
            }
        }
        if (spans1) // expand cols covered by span to accommodate the spanning cell
        {
            for (int rix = 0; rix < colReq.length; rix++)
            {
                float[] row = colReq[rix];
                for (int cix = 0; cix < row.length; cix++)
                {
                    float rw = row[cix];
                    CellDef cd = template.cellAt(rix, cix); // TODO has funktioniert nur wenn im Crosstab keine Spans gesetzt werden.
                    if (cd != null)
                    {
                        CellStyle cs = cd.getCascadedStyle();
                        int hs = cs.getSpanHorizontal();
                        final boolean flowCell = cd.isReflowing();
                        //exp hmf 040310 also adjust if hs = 1 (span in last col of grid)
                        //if (hs > 1 && cs.getTextType() != CellStyle.reFlowType) // span but not a flower
                        if (!cd.isReflowing() && cs.getSpanHorizontal() > 1) // span but not a flower
                            adjustForSpan(cix, hs, rw, flowCell? cs.getSize().getValInPoints() * 4: 0);
                    }
                }
            }
        }
        return flowerCol;
    }

    protected int nextDefCol(int tcIx, int cix, int dynaCols)
    {
        if(template.getCrosstab() != null)
            return template.getCrosstab().nextDefCol(tcIx, cix, dynaCols);
        else
            return tcIx+1;
    }

    protected void spread(float w, float maxWidth, float fPos)
    {
        int flexCols = 0;
        int emptyFlexCols = 0;
        float totalWeight = 0;
        float emptyWeight = 0;
        int tcIx = 0;
        for (int cix = 0; cix < dynaCols; cix++)
        {
            FlexSize ch = template.getColSizingAt(tcIx);
            if (ch.isExpandable() && ch.getVal() > 0 && colWidths[cix] > 0)
            {
                flexCols++;
                totalWeight += ch.getVal();
            }
            else if(ch.isExpandable() && ch.getVal() > 0)
            {
                emptyFlexCols++;
                emptyWeight += ch.getVal();
            }
            tcIx = nextDefCol(tcIx, cix, dynaCols);
        }

        boolean spreadEmptys = (flexCols == 0 && emptyFlexCols > 0);
        if(spreadEmptys)
            totalWeight = emptyWeight;

        //if (flexCols > 0)
        {
            tcIx = 0;
            float addWidth = w - maxWidth;
            Trace.logDebug(Trace.fill, "scaling hori from " + maxWidth + " to " + w + " adding " + addWidth + " to " + flexCols);
            for (int cix = 0; cix < dynaCols; cix++)
            {
                colPos[cix] = fPos;
                final float cellWidth;
                if (colWidths[cix] == 0 && spreadEmptys)
                {
                    FlexSize ch = template.getColSizingAt(tcIx);
                    cellWidth = addWidth * ch.getVal() / totalWeight;   // expand
                }
                else if (colWidths[cix] == 0)
                    cellWidth = 0;
                else
                {
                    FlexSize ch = template.getColSizingAt(tcIx);
                    if (ch != null && ch.isExpandable() && ch.getVal() > 0 && colWidths[cix] > 0.001)
                        cellWidth = maxColWidths[cix] + addWidth * ch.getVal() / totalWeight;   // expand
                    else
                        cellWidth = maxColWidths[cix];
                }

                fPos += cellWidth;
                colWidths[cix] = cellWidth;
                tcIx = nextDefCol(tcIx, cix, dynaCols);
            }
        }
    }
    protected void squeeze(float w, float minWidth, float fPos)
    {
        // TODO fix cols berücksichtigen
        float shrinkFaktor = w / minWidth;
        Trace.logWarn("Warning: Cell sizes exceed page width. Cells will be cropped by " + ((1 - shrinkFaktor) * 100) + "%.");
        for (int cix = 0; cix < dynaCols; cix++)
        {
            colPos[cix] = fPos;
            float cellWidth = colWidths[cix] * shrinkFaktor;
            fPos += cellWidth;
            colWidths[cix] = cellWidth;
        }
    }

    protected boolean spread4Reflow(boolean[] flowerCol, float w, float minWidth, float fPos)
    {
        int flexCols = 0;
        float totalWeight = 0;
        for (int cix = 0; cix < dynaCols; cix++)
        {
            FlexSize ch = template.getColSizingAt(cix);
            if (flowerCol[cix] && colWidths[cix] > 0 && ch.isExpandable() && ch.getVal() > 0)
            {
                flexCols++;
                totalWeight += ch.getVal();
            }
        }
        if (flexCols > 0)
        {
            int tcIx = 0;
            // TODO platz auf dem Bedarf beschränken
            float addWidth = w - minWidth;
            Trace.logDebug(Trace.fill, "scaling hori from " + minWidth + " to " + w + " adding " + addWidth + " to " + flexCols);
            for (int cix = 0; cix < dynaCols; cix++)
            {
                colPos[cix] = fPos;
                float cellWidth;
                cellWidth = colWidths[cix];
                FlexSize ch = template.getColSizingAt(cix);
                if (flowerCol[cix] && colWidths[cix] > 0 && ch.isExpandable() && ch.getVal() > 0)
                {
                    final float colWeight = ch.getVal();
                    cellWidth += addWidth * colWeight / totalWeight;   // expand
                }

                fPos += cellWidth;
                colWidths[cix] = cellWidth;
                tcIx = nextDefCol(tcIx, cix, dynaCols);
            }
            return true;
        }
        else
            return false;
    }

    protected void adjustForSpan(int cix, int hs, float rw, float flowMin)
    {
        int stretchables = 0;
        boolean stretchable = false;
        int rightMostStretchable = -1;
        float baseWidth = 0;
        for (int si = cix; si < cix + hs; si++)
        {
            FlexSize cfs = template.getColSizingAt(si);
            float cw = colWidths[si];
            baseWidth += cw;
            if (cfs.getUnit() == FlexSize.flex)
            {
                if (cw > 0)
                    stretchables++;
                stretchable = true;
                rightMostStretchable = si;
            }
        }
        if (flowMin > 0)
            baseWidth = Math.max(baseWidth, Math.min(rw, flowMin));
        float delta = rw - baseWidth;
        if (delta > 0 && stretchable) // must expand and can expand
        {
            if (stretchables > 0) // there are flexible cells with a base size > 0: they get the space, empty cells remain at 0
            {
                delta = delta / stretchables;
                for (int si = cix; si < cix + hs; si++)
                {
                    FlexSize cfs = template.getColSizingAt(si);
                    float cw = colWidths[si];
                    baseWidth += cw;
                    if (cw > 0 && cfs.getUnit() == FlexSize.flex)
                    {
                        colWidths[si] += delta;
                        maxColWidths[si] += delta;
                    }
                }
            }
            else
            // all flexible cells are 0 wide: the rightmost gets it all
            {
                colWidths[rightMostStretchable] += delta;
                maxColWidths[rightMostStretchable] += delta;
            }
        }
    }
}
