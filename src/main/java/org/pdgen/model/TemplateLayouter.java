// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;//MARKER The strings in this file shall not be translated

import org.pdgen.data.Internationalisation;
import org.pdgen.data.Trace;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.Settings;
import org.pdgen.model.cells.*;
import org.pdgen.model.style.*;
import org.pdgen.schemacheck.CheckFormulasForSchemaChange;
import org.pdgen.util.Log;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Math.round;

public class TemplateLayouter implements GridLayouter {
    private final TemplateModel myModel;
    float reqWidth;
    private float reqHeight;
    private float envelopeWidth;// the width of the envelope i.e the width available from the outside
    float envelopeHeight;// the actual height of the envelope i.e the height available from the outside
    float constrainedWidth;// the required witdth or the fixed width
    float constrainedHeight;// the required height or the fixed height
    float scaleToWidth;// the width we scale to. the envelope width if we are flexible, the fixed width if we are fixed
    private float scaleToHeight;// the height we scale to. the envelope height if we are flexible, the fixed height if we are fixed
    float xOff;// actual x position of envelope on page
    float yOff;// actual y position of envelope on page
    private CellRequirements[] rowRequirements;
    private CellRequirements[] colRequirements;
    private boolean layoutValid;
    private float[][] topBorderSpace;
    private float[][] leftBorderSpace;
    private float[][] rightBorderSpace;
    private float[][] bottomBorderSpace;
    private int[][] topRepeaterDepth;
    private int[][] leftRepeaterDepth;
    private int[][] rightRepeaterDepth;
    private int[][] bottomRepeaterDepth;
    private int[][] topRepeaterSpace;
    private int[][] leftRepeaterSpace;
    private int[][] rightRepeaterSpace;
    private int[][] bottomRepeaterSpace;
    private final HashMap<Repeater, RepeaterDepthInfo> repeaterDepth = new HashMap<>();
    GridLayouter container;
    TemplateBox myBox;
    private final Rectangle2D.Float frameRectScratch = new Rectangle2D.Float();
    private final Rectangle2D.Float tclipRectScratch = new Rectangle2D.Float();
    private final Rectangle2D.Float tcellRectScratch = new Rectangle2D.Float();
    private final Rectangle2D.Float tPaintRect = new Rectangle2D.Float();// used if we need a temp rectangle for painting and other quick operation
    private final Line2D.Float tempLine = new Line2D.Float();
    private final ArrayList<ChangeListener> changeListeners = new ArrayList<>();
    private final float[] dashArray = {5, 2};
    private final BasicStroke boxFrameStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
    private static final float repeaterSpace = 4;
    int pageNo;
    private float componentPos;
    public float scale;
    private boolean hasNestedLayouters;
    private final int nestedFramePaintIndent = 3;
    private final Color darkRed = Color.red.darker();
    private final Color darkBlue = Color.blue.darker();

    public TemplateLayouter(GridLayouter gl, TemplateBox box) {
        myModel = box.getTemplate();
        myBox = box;
        container = gl;
    }

    public Rectangle findDisplayRect(CellLocation cl) {
        if (cl.model != myModel)
            return null;
        scale = getScale();        //final Point2D.Float inOuter = container.getOffsetInOuter(this, pageNo);		//System.out.println("composy = " + componentPos + "inOuter.y = " + inOuter.y + " getPageNo() = " + getPageNo());
        final int yoff = round((componentPos + PageLayouter.displayVOffset + rowRequirements[cl.row].off) * scale) - 9;        //final int yoff = round(rowRequirements[cl.y].off) + round(yOff) + round(componentPos);
        final int height = round(rowRequirements[cl.row].act * scale) + 9;
        final int xoff = round((PageLayouter.displayHOffset + colRequirements[cl.col].off) * scale);        //final int xoff = round(colRequirements[cl.x].off) + round(xOff);
        final int width = round(colRequirements[cl.col].act * scale);
        return new Rectangle(xoff, yoff, width, height);
    }

    public PageLayouter getTopContainer() {
        return container.getTopContainer();
    }

    public TemplateBox getTemplateBox() {
        return myBox;
    }

    public void noteChange() {
        getContainer().noteChange();
    }

    public void setPageNo(final int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setComponentPos(final float componentPos) {
        this.componentPos = componentPos;
    }

    public TemplateModel getMyModel() {
        return myModel;
    }

    /*	public float getComponentPos()
        {
            return componentPos;
        }
    */
    static class RepeaterDepthInfo {
        int topDepth;
        int leftDepth;
        int rightDepth;
        int bottomDepth;
        int neckDepth;
    }

    /*
	static class NestingInfo
	{
		int row, col;
		NestingCellDef cell;
		TemplateLayouter lay;

		NestingInfo(int r, int c, NestingCellDef cd, TemplateLayouter lay)
		{
			cell = cd;
			row = r;
			col = c;
			this.lay = lay;
		}

		public void setRowCol(final int r, final int c)
		{
			row = r;
			col = c;
		}
	}
    */

    private float getRequiredCellWidth(CellDef cd, Graphics2D g) {
        try {
            if (cd instanceof DataCell || cd instanceof StyledTextCellDef)
                return cd.getCascadedStyle().getSize().getValInPoints() * 2;
            else
                return cd.getWidth(Internationalisation.NOREPLACE, g);
        } catch (Throwable t) {
            t.printStackTrace();
            return 20f;
        }
    }

    private void adjustForHorizontalSpan(Graphics2D g) {
        for (int r = 0; r < myModel.getRowCount(); r++) {
            for (int c = 0; c < myModel.getColCount(); c++) {
                CellDef cd = myModel.cellAt(r, c);
                if (cd == null)
                    continue;
                CellStyle bs = cd.getCascadedStyle();
                if (bs.getSpanHorizontal() <= 1)
                    continue;
                float cellWidth = getRequiredCellWidth(cd, g);
                int maxCol = (int) Math.min(bs.getSpanHorizontal().longValue() + c, myModel.getColCount()) - 1;
                Repeater tt = cd.getRepeater();
                if (tt != null)
                    maxCol = Math.min(maxCol, tt.getEndCol());
                float spanWidth = 0;
                for (int colIx = c; colIx <= maxCol; colIx++) {
                    spanWidth += colRequirements[colIx].pref;
                }
                if (cellWidth > spanWidth)// component wider than total width of spanned cols
                {
                    if (spanWidth == 0) {
                        float cPref = cellWidth;
                        for (int ri = maxCol; ri >= c; ri--)// search for first flexible col from last to first
                        {
                            if (myModel.getColSizingAt(ri).isExpandable()) {
                                colRequirements[ri].pref = cPref;// this col gets it all so all other cols remain at 0
                                break;
                            } else {
                                float d = myModel.getColSizingAt(ri).getVal() - colRequirements[ri].pref;
                                if (d > 0) {
                                    colRequirements[ri].pref = myModel.getColSizingAt(ri).getVal();
                                    cPref -= d;
                                }
                            }
                        }
                    } else {
                        float scale = cellWidth / spanWidth;
                        for (int ri = maxCol; ri >= c; ri--)// search for first flexible col from last to first
                        {
                            colRequirements[ri].pref = colRequirements[ri].pref * scale;
                        }
                    }
                }
            }
        }
    }

    private void adjustForVerticalSpan(Graphics2D g) {
        for (int r = 0; r < myModel.getRowCount(); r++) {
            for (int c = 0; c < myModel.getColCount(); c++) {
                CellDef cd = myModel.cellAt(r, c);
                if (cd == null)
                    continue;
                CellStyle bs = cd.getCascadedStyle();
                if (bs.getSpanHorizontal() <= 1)
                    continue;
                float cellHeight = cd.getHeight(Internationalisation.NOREPLACE, g);
                int maxRow = Math.min(r + bs.getSpanVertical(), myModel.getRowCount()) - 1;
                if (cd.getRepeater() != null) {
                    maxRow = Math.min(maxRow, cd.getRepeater().getEndRow());
                }
                float spanHeight = 0;
                for (int rowIx = r; rowIx <= maxRow; rowIx++) {
                    spanHeight += rowRequirements[rowIx].pref;
                }
                if (cellHeight > spanHeight)// component higher than total height of spanned rows
                {                    //if (spanHeight == 0)
                    {
                        float cPref = cellHeight - spanHeight;
                        boolean allFix = true;
                        for (int ri = maxRow; ri >= r; ri--)// search for first flexible row from last to first
                        {
                            if (myModel.getRowSizingAt(ri).isExpandable()) {
                                rowRequirements[ri].pref += cPref;// this row gets it all so all other rows remain at 0
                                allFix = false;
                                break;
                            } else {
                                float d = myModel.getRowSizingAt(ri).getVal() - rowRequirements[ri].pref;
                                if (d > 0) {
                                    rowRequirements[ri].pref = myModel.getRowSizingAt(ri).getVal();
                                }
                            }
                        }
                        if (allFix)
                            rowRequirements[maxRow].pref += cPref;
                    }
                }
            }
        }
    }

    private String boxKind() {
        return myBox.getBoxTypeName();
    }

    public void calcWidths(Graphics2D g) {
        int nRows = myModel.getRowCount();
        int nCols = myModel.getColCount();
        if (layoutValid && nRows == rowRequirements.length && nCols == colRequirements.length) {
            Trace.logDebug(Trace.layout, "calc " + boxKind() + " skipped");
            return;
        }
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {
                CellDef cd = myModel.getCellAt(r, c);
                if (cd != null) {
                    cd.resetLayout();
                }
            }
        }
        reqWidth = 0.0f;
        rowRequirements = new CellRequirements[nRows];
        colRequirements = new CellRequirements[nCols];
        for (int j = 0; j < nCols; j++) {
            colRequirements[j] = new CellRequirements();
        }
        topBorderSpace = new float[nRows][];
        leftBorderSpace = new float[nRows][];
        rightBorderSpace = new float[nRows][];
        bottomBorderSpace = new float[nRows][];
        topRepeaterDepth = new int[nRows][];
        leftRepeaterDepth = new int[nRows][];
        rightRepeaterDepth = new int[nRows][];
        bottomRepeaterDepth = new int[nRows][];
        topRepeaterSpace = new int[nRows][];
        leftRepeaterSpace = new int[nRows][];
        rightRepeaterSpace = new int[nRows][];
        bottomRepeaterSpace = new int[nRows][];
        repeaterDepth.clear();
        myModel.getRepeaterList().getRepeaterForHeaders(null);        // first pass calculate repeater depth
        for (int r = 0; r < nRows; r++) {
            topBorderSpace[r] = new float[nCols];
            leftBorderSpace[r] = new float[nCols];
            rightBorderSpace[r] = new float[nCols];
            bottomBorderSpace[r] = new float[nCols];
            topRepeaterDepth[r] = new int[nCols];
            leftRepeaterDepth[r] = new int[nCols];
            rightRepeaterDepth[r] = new int[nCols];
            bottomRepeaterDepth[r] = new int[nCols];
            topRepeaterSpace[r] = new int[nCols];
            leftRepeaterSpace[r] = new int[nCols];
            rightRepeaterSpace[r] = new int[nCols];
            bottomRepeaterSpace[r] = new int[nCols];
            for (int c = 0; c < nCols; c++) {
                CellDef cd = myModel.cellAt(r, c);
                CellStyle cs;
                if (cd != null) {
                    cs = cd.getCascadedStyle();                    // nun brauchen wir noch Platz fuer die Repeatermarkierung
                    if (isShowRepeaters() && (cd.getRepeater() != null || cd.getHeadedRepeater() != null)) {
                        Repeater rep;
                        if (cd.getHeadedRepeater() != null) {
                            rep = cd.getHeadedRepeater();
                            calculateRepeaterSpace(r, cs.getSpanVertical() - 1, c, cs.getSpanHorizontal() - 1, rep);
                        }
                        if (cd.getRepeater() != null) {
                            rep = cd.getRepeater();
                            calculateRepeaterSpace(r, cs.getSpanVertical() - 1, c, cs.getSpanHorizontal() - 1, rep);
                        }
                    }
                }
            }
        }        // second pass calculate repeater space		// first horizontal
        for (int r = 0; r < nRows; r++) {
            int maxTop = 0;
            int maxBottom = 0;
            for (int c = 0; c < nCols; c++) {
                maxTop = Math.max(topRepeaterDepth[r][c], maxTop);
                maxBottom = Math.max(bottomRepeaterDepth[r][c], maxBottom);
            }
            for (int c = 0; c < nCols; c++) {
                topRepeaterSpace[r][c] = maxTop;
                bottomRepeaterSpace[r][c] = maxBottom;
            }
        }        // second vertical
        for (int c = 0; c < nCols; c++) {
            int maxLeft = 0;
            int maxRight = 0;
            for (int r = 0; r < nRows; r++) {
                maxLeft = Math.max(leftRepeaterDepth[r][c], maxLeft);
                maxRight = Math.max(rightRepeaterDepth[r][c], maxRight);
            }
            for (int r = 0; r < nRows; r++) {
                leftRepeaterSpace[r][c] = maxLeft;
                rightRepeaterSpace[r][c] = maxRight;
            }
        }
        hasNestedLayouters = false;// reset		// third pass, raw width calculation
        int nRows1 = myModel.getRowCount();
        int nCols1 = myModel.getColCount();
        for (int r1 = 0; r1 < nRows1; r1++) {
            CellRequirements rr = rowRequirements[r1] = new CellRequirements();
            FlexSize rh = myModel.getRowSizingAt(r1);
            if (rh.getUnit() > FlexSize.flex)// fixed
                rr.pref = rh.getVal();
            for (int c1 = 0; c1 < nCols1; c1++) {
                FlexSize cw = myModel.getColSizingAt(c1);
                boolean fixWidth = cw != null && !cw.isExpandable() && cw.getVal() > 0;
                CellDef cd1 = myModel.cellAt(r1, c1);
                CellStyle cs1;
                float cellWidth;
                if (cd1 != null) {
                    cs1 = cd1.getCascadedStyle();
                    if (cd1 instanceof NestingCellDef) {
                        NestingCellDef ncd = (NestingCellDef) cd1;
                        TemplateLayouter innerLayouter = ncd.getInnerBox().getTemplate().getLayouter();
                        if (innerLayouter == null) {
                            innerLayouter = new TemplateLayouter(this, ncd.getInnerBox());
                            ncd.getInnerBox().getTemplate().setLayouter(innerLayouter);
                        }
                        innerLayouter.calcWidths(g);
                        cellWidth = innerLayouter.getReqWidth() + 6;// space for frame marker
                        hasNestedLayouters = true;
                    } else if (cd1.isReflowing()) {
                        if (fixWidth)
                            cellWidth = cw.getVal();
                        else
                            cellWidth = getRequiredCellWidth(cd1, g);
                        cd1.reFlow(cellWidth, Internationalisation.NOREPLACE, g);
                    } else {
                        if (fixWidth)
                            cellWidth = cw.getVal();
                        else
                            cellWidth = getRequiredCellWidth(cd1, g);
                    }                    // nun brauchen wir noch Platz fuer die Repeatermarkierung
                    if (isShowRepeaters() && (cd1.getRepeater() != null || cd1.getHeadedRepeater() != null)) {
                        cellWidth += repeaterSpace * leftRepeaterSpace[r1][c1];
                        cellWidth += repeaterSpace * rightRepeaterSpace[r1][c1];
                    }
                } else {
                    cs1 = myBox.getCascadedCellStyle();
                    cellWidth = cs1.getLeftRightPaddingValue();
                }                // hier werden nun die Borders aufgerechnet
                JoriaSimpleBorder left = myModel.getBorderAt(r1, c1, false, true);
                JoriaSimpleBorder right = myModel.getBorderAt(r1, c1 + cs1.getSpanHorizontal() - 1, false, false);
                leftBorderSpace[r1][c1] = left.getThickness() / (c1 == 0 ? 1f : 2f);
                rightBorderSpace[r1][c1] = right.getThickness() / ((c1 + 1 == nCols1) ? 1f : 2f);
                cellWidth += leftBorderSpace[r1][c1] + rightBorderSpace[r1][c1];
                if (isShowingEmptyCells() && cellWidth < 9) {
                    cellWidth = 9;// Minimum size that an empty cell is shown with
                }
                if (fixWidth)
                    colRequirements[c1].pref = Math.max(cw.getVal(), 6f);// if it is fixed use fixed width
                else if (cs1.getSpanHorizontal() <= 1)
                    colRequirements[c1].pref = Math.max(cellWidth, colRequirements[c1].pref);
            }
        }
        adjustForHorizontalSpan(g);        // second pass: sum total width
        for (int j = 0; j < myModel.getColCount(); j++) {
            reqWidth = reqWidth + colRequirements[j].pref;
        }
        FrameStyle fs = myBox.getCascadedFrameStyle();
        reqWidth = reqWidth + fs.getLeftBorder().getTotalInset() + fs.getRightBorder().getTotalInset();        // width
        if (fs.getWidth().getUnit() <= FlexSize.flex)
            constrainedWidth = reqWidth;
        else
            constrainedWidth = fs.getWidth().getVal();
        Trace.logDebug(Trace.layout, "Requirements " + boxKind() + " reqWidth: " + reqWidth + " reqHeight: " + reqHeight);
    }

    void layoutHorizontal(final float w, final float x, final Graphics2D g) {
        if (myModel.getRowCount() == 0 || myModel.getColCount() == 0)
            return;
        if (myBox.getBoxType() == TemplateBoxInterface.pageBodyBox)
            Trace.logDebug(Trace.layout, "Layout inner body");
        else
            Trace.logDebug(Trace.layout, "Layout inner header/footer");
        if (xOff != x && envelopeWidth == w && layoutValid) {
            repositionX(x);
            return;
        }
        FrameStyle fs = myBox.getCascadedFrameStyle();
        int nRows = myModel.getRowCount();
        Integer fill = fs.getFill();
        envelopeWidth = w;
        xOff = x;
        int nCols = myModel.getColCount();
        FlexSize fw = fs.getWidth();// calc width for scaling
        if (fw.getUnit() == FlexSize.flex) {
            if (fw.getVal() == 0)
                scaleToWidth = Math.min(constrainedWidth, reqWidth);
            else
                scaleToWidth = w;
        } else
            scaleToWidth = fw.getVal();
        scaleToWidth = fs.getColumnWidth(scaleToWidth);
        float addWidth = 0;
        float widthFactor = 1;
        if (reqWidth > scaleToWidth)// squeeze
        {
            float ibox = (scaleToWidth - fs.getHorizontalInset());
            float ireq = (reqWidth - fs.getVerticalInset());            //heightFactor = ibox / ireq;
            Trace.logDebug(Trace.layout, "scaling hori from " + ireq + " to " + ibox + " total req " + reqWidth + " scale box " + scaleToWidth + " scale " + widthFactor);
        } else        // stretch
        {
            int flexCols = 0;
            for (int c = 0; c < nCols; c++) {
                FlexSize ch = myModel.getColSizingAt(c);
                if (ch == null || ch.isExpandable())
                    flexCols++;
            }
            if (flexCols > 0 && reqWidth < scaleToWidth && (StyleBase.FillBoth.equals(fill) || StyleBase.FillHorizontal.equals(fill) || StyleBase.FillSymmetric.equals(fill))) {
                addWidth = (scaleToWidth - reqWidth) / flexCols;
                Trace.logDebug(Trace.layout, "scaling hori from " + reqWidth + " to " + scaleToWidth + " adding " + addWidth + " to " + flexCols);
            }
        }
        float fPos = xOff + fs.getLeftBorder().getTotalInset();
        for (int c = 0; c < nCols; c++) {
            CellRequirements cr = colRequirements[c];
            cr.off = fPos;
            float cellWidth;
            FlexSize rh = myModel.getColSizingAt(c);
            if (rh != null && !rh.isExpandable() && rh.getVal() > 0)
                cellWidth = rh.getVal();
            else
                cellWidth = cr.pref * widthFactor + addWidth;//
            fPos += cellWidth;
            cr.act = cellWidth;
        }        //boolean readjustHorizontalSpans = false;
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {
                CellDef cd = myModel.getCellAt(r, c);
                if (cd == null)
                    continue;
                final CellStyle cs = cd.getCascadedStyle();
                if (cd.isReflowing()) {
                    float width = -(leftBorderSpace[r][c] + leftRepeaterDepth[r][c] * repeaterSpace + rightBorderSpace[r][c] - rightRepeaterDepth[r][c] * repeaterSpace);
                    int span = cs.getSpanHorizontal();
                    for (int i = c; i < c + span; i++) {
                        width += colRequirements[i].act;
                    }
                    cd.reFlow(width, Internationalisation.NOREPLACE, g);
                } else if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    final float widthForInner = colRequirements[c].act - cs.getLeftRightPaddingValue() - 2 * nestedFramePaintIndent;
                    final float innerX = colRequirements[c].off + cs.getLeftPadding().getValInPoints() + nestedFramePaintIndent;
                    ncd.getInnerBox().getTemplate().getLayouter().layoutHorizontal(widthForInner, innerX, g);
                }
            }
        }
    }

    public void layoutVertical(float y, Graphics2D g) {
        if (myModel.getRowCount() == 0 || myModel.getColCount() == 0)
            return;
        if (myBox.getBoxType() == TemplateBoxInterface.pageBodyBox)
            Trace.logDebug(Trace.layout, "Layout inner body");
        else
            Trace.logDebug(Trace.layout, "Layout inner header/footer");
        if (yOff != y && layoutValid) {
            repositionY(y);
            return;
        }
        try {
            yOff = y;
            calcHeights(g);
            FrameStyle fs = myBox.getCascadedFrameStyle();
            int nRows = myModel.getRowCount();
            Integer fill = fs.getFill();
            FlexSize fh = fs.getHeight();// height for scaling
            if (fh.getUnit() != FlexSize.flex)
                scaleToHeight = fh.getVal();
            else
                scaleToHeight = Math.min(constrainedHeight, reqHeight);
            envelopeHeight = scaleToHeight;
            Trace.logDebug(Trace.layout, "layout " + boxKind() + " y: " + y + " width: " + envelopeWidth + " height: " + envelopeHeight);
            float addHeight = 0;
            float heightFactor = 1;
            if (reqHeight > scaleToHeight)// shrink
                heightFactor = (scaleToHeight - fs.getVerticalInset()) / (reqHeight - fs.getVerticalInset());
            else if (StyleBase.FillBoth.equals(fill) || StyleBase.FillVertical.equals(fill)) {
                int flexRows = 0;
                for (int i = 0; i < nRows; i++) {
                    FlexSize rh = myModel.getRowSizingAt(i);
                    CellRequirements rr = rowRequirements[i];
                    if (rh.isExpandable() && rr.pref > 0)
                        flexRows++;
                }
                if (flexRows > 0) {
                    addHeight = (scaleToHeight - reqHeight) / flexRows;
                    Trace.logDebug(Trace.layout, "scaling vert from " + reqHeight + " to " + scaleToHeight + " scale " + heightFactor);
                }
            }
            float yPos = yOff + fs.getTopBorder().getTotalInset();
            for (int i = 0; i < nRows; i++) {
                CellRequirements rr = rowRequirements[i];
                rr.off = yPos;
                float cellHeight;// default to show errors
                FlexSize rh = myModel.getRowSizingAt(i);
                if (!rh.isExpandable() && rh.getVal() > 0)
                    cellHeight = rh.getVal();
                else
                    cellHeight = rr.pref * heightFactor + addHeight;//
                rr.act = cellHeight;
                yPos += cellHeight;
            }
            if (hasNestedLayouters) {
                Rectangle2D.Float alloc = new Rectangle2D.Float();
                for (int r = 0; r < myModel.getRowCount(); r++) {
                    final ArrayList<CellDef> rowcells = myModel.getRow(r);
                    for (int c = 0; c < myModel.getColCount(); c++) {
                        CellDef cd = rowcells.get(c);
                        if (cd instanceof NestingCellDef) {
                            NestingCellDef ncd = (NestingCellDef) cd;
                            getCellAllocationAt(r, c, alloc);
                            final float innerY = alloc.y + nestedFramePaintIndent + ncd.getCascadedStyle().getTopPadding().getValInPoints();
                            ncd.getInnerBox().getTemplate().getLayouter().layoutVertical(innerY, g);
                        }
                    }
                }
            }
        } finally {
            layoutValid = true;
        }
    }

    void calcHeights(final Graphics2D g) {
        reqHeight = 0.0f;
        int nRows = myModel.getRowCount();
        int nCols = myModel.getColCount();
        for (int r = 0; r < nRows; r++) {
            CellRequirements rrq = rowRequirements[r] = new CellRequirements();
            FlexSize rh = myModel.getRowSizingAt(r);
            if (rh.getUnit() > FlexSize.flex)// fixed
                rrq.pref = rh.getVal();
            for (int c = 0; c < nCols; c++) {
                //noinspection UnusedDeclaration
                final float colWidth = colRequirements[c].pref;
                CellDef cd = myModel.cellAt(r, c);
                CellStyle cs;
                float cellHeight;
                if (cd != null) {
                    cs = cd.getCascadedStyle();
                    if (cd instanceof NestingCellDef) {
                        NestingCellDef ncd = (NestingCellDef) cd;
                        TemplateLayouter innerLayouter = ncd.getInnerBox().getTemplate().getLayouter();
                        innerLayouter.calcHeights(g);
                        cellHeight = innerLayouter.reqHeight + 6;// space for frame marker
                    } else {
                        cellHeight = cd.getHeight(Internationalisation.NOREPLACE, g);
                    }                    // nun brauchen wir noch Platz fuer die Repeatermarkierung
                    if (isShowRepeaters() && (cd.getRepeater() != null || cd.getHeadedRepeater() != null))
                        cellHeight += repeaterSpace * (topRepeaterSpace[r][c] + bottomRepeaterSpace[r][c]);
                } else {
                    cs = myBox.getCascadedCellStyle();
                    cellHeight = cs.getTopBotPaddingValue();
                }                // hier werden nun die Borders aufgerechnet
                JoriaSimpleBorder top = myModel.getBorderAt(r, c, true, true);
                JoriaSimpleBorder bottom = myModel.getBorderAt(r + cs.getSpanVertical() - 1, c, true, false);
                topBorderSpace[r][c] = top.getThickness() / (r == 0 ? 1f : 2f);
                bottomBorderSpace[r][c] = bottom.getThickness() / ((r + 1 == nRows) ? 1f : 2f);
                cellHeight += topBorderSpace[r][c] + bottomBorderSpace[r][c];
                if ((rh.isExpandable() || rh.getVal() == 0) && cs.getSpanVertical() <= 1)
                    rrq.pref = Math.max(cellHeight, rrq.pref);
            }
            if (!rh.isExpandable()) {
                rrq.pref = Math.max(rh.getVal(), 6f);// if it is fixed go back to fixed height
            } else if (isShowingEmptyCells() && rrq.pref < 9)
                rrq.pref = 9;// Size of empty cells
        }
        adjustForVerticalSpan(g);
        for (int r = 0; r < myModel.getRowCount(); r++) {
            reqHeight = reqHeight + rowRequirements[r].pref;
        }
        FrameStyle fs = myBox.getCascadedFrameStyle();
        reqHeight = reqHeight + fs.getTopBorder().getTotalInset() + fs.getBottomBorder().getTotalInset();
        if (fs.getHeight().getUnit() <= FlexSize.flex)
            constrainedHeight = reqHeight;
        else
            constrainedHeight = fs.getHeight().getVal();
    }

    private void calculateRepeaterSpace(int r, int vSpan, int c, int hSpan, Repeater rep) {
        if (rep.getOuterRepeater() != null)
            calculateRepeaterSpace(r, vSpan, c, hSpan, rep.getOuterRepeater());
        RepeaterDepthInfo info = repeaterDepth.get(rep);
        if (info == null) {
            info = new RepeaterDepthInfo();
            repeaterDepth.put(rep, info);
        }
        if (rep.getStartCol() == c) {
            leftRepeaterDepth[r][c]++;
            if (info.leftDepth == 0)
                info.leftDepth = leftRepeaterDepth[r][c];
        }
        if (rep.getEndCol() + hSpan == c) {
            rightRepeaterDepth[r][c]++;
            if (info.rightDepth == 0)
                info.rightDepth = rightRepeaterDepth[r][c];
        }
        if (rep.getStartRow() - rep.getHeaderRows() == r) {
            topRepeaterDepth[r][c]++;
            if (info.topDepth == 0)
                info.topDepth = topRepeaterDepth[r][c];
        }
        if (rep.getStartRow() == r && rep.getHeaderRows() > 0) {
            topRepeaterDepth[r][c] = 1;
            if (info.neckDepth == 0)
                info.neckDepth = topRepeaterDepth[r][c];
        }
        if (rep.getEndRow() + vSpan == r) {
            bottomRepeaterDepth[r][c]++;
            if (info.bottomDepth == 0)
                info.bottomDepth = bottomRepeaterDepth[r][c];
        }
    }

    /*
     * Diese Methode malt den blauen Rahmen um das selektierte Element
     *
     */
    public void paintSelectionBorder(Graphics2D g, boolean on, int c1, int r1, int c2, int r2) {
        if (!layoutValid)
            layoutOuter(this, g);
        float scale = getScale();        //Point2D.Float pageOffset = container.getOffsetInOuter(this, pageNo);		//final float hOff = (pageOffset.x + PageLayouter.displayHOffset);		//final float vertOff = (pageOffset.y + PageLayouter.displayVOffset);
        final float hOff = PageLayouter.displayHOffset;
        final float vertOff = componentPos + PageLayouter.displayVOffset;
        if (on) {
            g.setColor(Color.blue);
            CellDef cd = myModel.cellAt(r1, c1);
            if (cd != null) {
                CellStyle bs = cd.getCascadedStyle();
                c2 = Math.min(Math.max(c2, c1 + bs.getSpanHorizontal() - 1), myModel.getColCount() - 1);
                r2 = Math.min(Math.max(r2, r1 + bs.getSpanVertical() - 1), myModel.getRowCount() - 1);
            }            // add page display offset
            final float vertPos = vertOff + rowRequirements[r1].off;
            g.setStroke(new BasicStroke(2 / scale));
            frameRectScratch.setRect(scale * (hOff + colRequirements[c1].off), //
                    scale * vertPos, //
                    scale * (colRequirements[c2].end() - colRequirements[c1].off), //
                    scale * (rowRequirements[r2].end() - rowRequirements[r1].off));//
            g.draw(frameRectScratch);
            //noinspection UnusedDeclaration
            final float vo2 = componentPos + PageLayouter.displayVOffset + rowRequirements[r1].off;			/*
									g.setColor(Color.yellow);
									frameRectScratch.setRect(scale * (hOff + colRequirements[c1].off), //
															 scale * vo2, //
															 scale * (colRequirements[c2].end() - colRequirements[c1].off), //
															 scale * (rowRequirements[r2].end() - rowRequirements[r1].off));//
						*/
            g.draw(frameRectScratch);
        } else {
            g = (Graphics2D) g.create();
            g.translate(hOff * scale, vertOff * scale);
            g.scale(scale, scale);
            HashSet<Repeater> repeaters = new HashSet<>();
            for (int r = r1; r <= r2; r++) {
                for (int c = c1; c <= c2; c++) {
                    paintCell(r, c, g);
                    if (myModel.getCellAt(r, c) != null) {
                        CellDef cd = myModel.getCellAt(r, c);
                        if (cd.getHeadedRepeater() != null) {
                            repeaters.add(cd.getHeadedRepeater());
                            for (Repeater rr = cd.getHeadedRepeater(); rr != null; rr = rr.getOuterRepeater()) {
                                repeaters.add(rr);
                            }
                        }
                        if (cd.getRepeater() != null) {
                            repeaters.add(cd.getRepeater());
                            for (Repeater rr = cd.getRepeater(); rr != null; rr = rr.getOuterRepeater()) {
                                repeaters.add(rr);
                            }
                        }
                    }
                }
            }
            paintRepeaters0(g, repeaters.toArray(new Repeater[repeaters.size()]));
            g.setStroke(new BasicStroke(1 / scale));
            paintGrid(g);
            g.dispose();
        }
    }

    private void getCellAllocationAt(int row, int col, Rectangle2D.Float r) {
        CellDef cd = myModel.cellAt(row, col);
        CellDef cov = myModel.getCoverCell(row, col);
        if (cov != null)
            cd = cov;
        r.y = rowRequirements[row].off;
        r.x = colRequirements[col].off;		/*
				r.width = colRequirements[col].end() - r.x;
				r.height = rowRequirements[row].end() - r.y;
				*/
        r.width = colRequirements[col].act;
        final float act = rowRequirements[row].act;
        if (act < 0)
            r.height = 0;
        else
            r.height = act;
        if (cd != null) {
            CellStyle bs = cd.getCascadedStyle();
            if (bs.getSpanHorizontal() > 1)
                r.width = colRequirements[FreeCellDef.getSpanEnd(cd, col, true)].end() - r.x;
            if (bs.getSpanVertical() > 1)
                r.height = rowRequirements[FreeCellDef.getSpanEnd(cd, row, false)].end() - r.y;
        }
    }

    /**
     * ----------------------------------------------------------------------- getCellAt
     */
    public void getCellAt(Point pk, CellLocation was) {        // TODO hier muessen wir ran, wenn wir die RepeaterIndicators aktiv machen.
        was.model = null;
        was.col = -1;
        was.row = -1;
        was.relative = CellLocation.NOWHERE;
        float px = pk.x / getScale();
        float py = pk.y;// /getScale();
        if (px < xOff - 20 || px > xOff + envelopeWidth + 20)
            return;
        if (py < yOff || py > yOff + envelopeHeight)
            return;
        was.model = myModel;
        was.relative = CellLocation.FRAME;
        if (px <= xOff || px > xOff + scaleToWidth || py > yOff + scaleToHeight || myModel.getRowCount() == 0 || myModel.getColCount() == 0)
            return;
        Trace.logDebug(Trace.layout, "getCellAt? " + px + ", " + py);
        int x = -1;        // find the col of the click position
        for (int j = 0; j < myModel.getColCount(); j++) {
            if (px < colRequirements[j].end()) {
                x = j;
                break;
            }
        }
        if (x < 0)
            return;
        if (py < yOff && py > rowRequirements[myModel.getRowCount() - 1].end()) {
            was.model = myModel;
            was.col = x;
            was.row = myModel.getRowCount() - 1;
            was.relative = CellLocation.FARBELOW;
            return;
        }        // find the row of the click position
        int y = -1;
        for (int i = 0; i < myModel.getRowCount(); i++) {
            if (py < rowRequirements[i].end()) {
                y = i;
                break;
            }
        }
        if (y < 0)
            return;
        CellDef cd = myModel.cellAt(y, x);
        CellDef cov = myModel.getCoverCell(y, x);
        if (cov != null) {
            cd = cov;
            Point pcov = myModel.getCellPosition(cov);
            x = pcov.x;
            y = pcov.y;
        }
        if (cd instanceof NestingCellDef) {
            NestingCellDef ncd = (NestingCellDef) cd;
            ncd.getInnerBox().getTemplate().getLayouter().getCellAt(pk, was);// recurse into nested grid
            return;
        }
        int newTracker;
        {
            CellRequirements cr = colRequirements[x];
            CellRequirements rr = rowRequirements[y];
            float ox = px - cr.off;
            float oy = py - rr.off;            //TemplateSelectionModel s = myTarget.getSel();
            float cellwidth = cr.act;
            float rowheight = rr.act;
            if (cd != null) {
                int hspan = cd.getCascadedStyle().calcSpanHorizontal();
                int vspan = cd.getCascadedStyle().calcSpanVertical();
                for (int i = 1; i < hspan; i++) {
                    CellRequirements cr2 = colRequirements[x + i];
                    cellwidth += cr2.act;
                }
                for (int i = 1; i < vspan; i++) {
                    CellRequirements rr2 = rowRequirements[y + i];
                    rowheight += rr2.act;
                }
            }
            int hcenterOffset = Math.min((int) (cellwidth / 5), 15);
            int vcenterOffset = Math.min((int) (rowheight / 5), 15);
            if ((ox > hcenterOffset && ox < cellwidth - hcenterOffset && oy > vcenterOffset && oy < rowheight - vcenterOffset) || rowheight == 0)                /*|| (trackRange && s.isInRange(nTrackCell))*/ {
                newTracker = CellLocation.CENTER;
            } else if (oy == 0)
                newTracker = CellLocation.ABOVE;
            else {
                float a = cellwidth / rowheight;
                boolean below_or_left = ox / oy < a;
                float nox = px - cr.end();// <0
                boolean above_or_left = nox / oy <= -a;
                if (below_or_left) {
                    if (above_or_left)
                        newTracker = CellLocation.LEFT;
                    else
                        newTracker = CellLocation.BELOW;
                } else if (above_or_left)
                    newTracker = CellLocation.ABOVE;
                else
                    newTracker = CellLocation.RIGHT;
            }
        }
        Trace.logDebug(Trace.layout, "getCellAt! " + x + ", " + y);
        was.model = myModel;
        was.col = x;
        was.row = y;
        was.relative = newTracker;
    }

    public GridLayouter getContainer() {
        return container;
    }

    public float getHeight() {
        return envelopeHeight;
    }

    public float getActualWidth() {
        return scaleToWidth;
    }

    /*
	float getRangeHeight(int s, int e)
	{
		return rowRequirements[e].end() - rowRequirements[e].off;
	}

	float getRangeLeft(int at)
	{
		return colRequirements[at].off;
	}

	float getRangeTop(int at)
	{
		return rowRequirements[at].off;
	}

	float getRangeWidth(int s, int e)
	{
		return colRequirements[e].end() - colRequirements[s].off;
	}
    */
    public void invalidateLayout() {
        container.invalidateLayout();
        invalidateInner();
    }

    private void invalidateInner() {
        layoutValid = false;
        if (hasNestedLayouters) {
            for (int r = 0; r < myModel.getRowCount(); r++) {
                ArrayList<CellDef> row = myModel.getRow(r);
                for (CellDef cellDef : row) {
                    if (cellDef instanceof NestingCellDef) {
                        NestingCellDef nestingCellDef = (NestingCellDef) cellDef;
                        final TemplateLayouter innerLayouter = nestingCellDef.getInnerBox().getTemplate().getLayouter();
                        if (innerLayouter != null)
                            innerLayouter.invalidateInner();                        // else it will be created by calculate requirements
                    }
                }
            }
        }
    }

    /*
	protected boolean isInFrame(Point p)
	{
		return p.x >= xOff && p.x <= xOff + envelopeWidth && p.y >= yOff && p.y <= yOff + envelopeHeight;
	}
    */
    public boolean isShowGrid() {
        return container.isShowGrid();
    }

    public boolean isShowingEmptyCells() {
        return container.isShowingEmptyCells();
    }

    public boolean isShowRepeaters() {
        return container.isShowRepeaters();
    }

    public float getScale() {
        return container.getScale();
    }

    public void layoutOuter(GridLayouter inner, Graphics2D g) {        // Trace.log(6, Trace.layout, "layoutOuter");
        container.layoutOuter(this, g);
    }

    public void paintSelectionBorderBox(Graphics2D g, boolean on) {
        Trace.logDebug(Trace.layout, "frameBox: " + boxKind());
        if (!layoutValid)
            layoutOuter(this, g);
        Stroke oldStroke = g.getStroke();
        if (on) {
            g.setColor(Color.blue);
            g.setStroke(boxFrameStroke);
        } else if (isShowGrid()) {
            Color color = Color.lightGray;
            if (Settings.INSTANCE.getShowDebugColors())
                color = Color.green;
            g.setColor(color);
        } else
            g.setColor(myBox.getCascadedFrameStyle().getBackground());
        float paintWidth = envelopeWidth;
        final FrameStyle frameStyle = myBox.getCascadedFrameStyle();
        final FlexSize width = frameStyle.getWidth();
        if (!width.isExpandable()) {
            paintWidth = width.getVal();
            Trace.logDebug(Trace.layout, "Fixed box " + width.getVal() + " " + envelopeWidth);
        }
        float scale = getScale();        //Point2D.Float offset = container.getOffsetInOuter(this, pageNo);		//frameRectScratch.setRect(scale * (offset.x + xOff + PageLayouter.displayHOffset), scale * (offset.y + yOff + PageLayouter.displayVOffset), scale * paintWidth, scale * envelopeHeight);
        frameRectScratch.setRect(scale * (xOff + PageLayouter.displayHOffset), scale * (componentPos + yOff + PageLayouter.displayVOffset), scale * paintWidth, scale * envelopeHeight);
        g.draw(frameRectScratch);
        g.setStroke(oldStroke);
    }

    private int paintCell(int r, int c, Graphics2D g) {
        if (myModel.getCoverCell(r, c) != null)
            return 1;
        int span = 1;
        CellDef cell = null;
        Shape oldClipShape = g.getClip();
        Rectangle2D oldClip = oldClipShape != null ? oldClipShape.getBounds2D() : null;
        Error hadError = null;
        try {
            cell = myModel.cellAt(r, c);
            if (cell instanceof CoveredCellDef)
                return 1;
            else if (cell instanceof NestingCellDef) {
                System.currentTimeMillis();// just for debug
            }
            CellStyle cs;
            if (cell == null) {
                cs = myBox.getCascadedCellStyle();
            } else {
                cs = cell.getCascadedStyle();
                span = cs.getSpanHorizontal();
            }
            getCellAllocationAt(r, c, frameRectScratch);
            Trace.check(frameRectScratch.height >= 0);
            if (oldClip == null) {
                tclipRectScratch.setRect(frameRectScratch);
            } else {
                Rectangle2D.intersect(oldClip, frameRectScratch, tclipRectScratch);
            }
            tcellRectScratch.setRect(frameRectScratch);
            g.setClip(tclipRectScratch);
            boolean anyBorderSet = false;
            for (int i = c; i < c + cs.getSpanHorizontal(); i++) {
                if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(r, i, true, true)) || !JoriaSimpleBorder.isNull(myModel.getBorderAt(r + cs.getSpanVertical() - 1, i, true, false)))
                    anyBorderSet = true;
            }
            for (int i = r; i < r + cs.getSpanVertical(); i++) {
                if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(i, c, false, true)) || !JoriaSimpleBorder.isNull(myModel.getBorderAt(i, c + cs.getSpanHorizontal() - 1, false, false)))
                    anyBorderSet = true;
            }
            if (anyBorderSet) {                // hier wird auch der Platz für die Border berücksichtigt
                paintCellBorderAndBackground(g, cs.getBackground(), r, c, cs);
            } else if (cell != null) {
                g.setColor(cs.getBackground());
                g.fill(frameRectScratch);
            }
            if (cell != null) {                // repeater indicator
                frameRectScratch.x += leftRepeaterSpace[r][c] * repeaterSpace;
                frameRectScratch.y += topRepeaterSpace[r][c] * repeaterSpace;
                frameRectScratch.width -= (leftRepeaterSpace[r][c] + rightRepeaterSpace[r][c]) * repeaterSpace;
                frameRectScratch.height -= (topRepeaterSpace[r][c] + bottomRepeaterSpace[r][c]) * repeaterSpace;                // background picture
                if (cs.getBackgroundImageName() != null) {
                    FlexSize imageScale = cs.getBackgroundImageTargetWidth();
                    ImageIcon image = cs.getBackgroundImage(Internationalisation.NOREPLACE);
                    float height = image.getIconHeight();
                    float width = image.getIconWidth();
                    float scale = 0;
                    if (imageScale != null && !imageScale.isExpandable()) {
                        scale = imageScale.getVal() / width;
                        width = imageScale.getVal();
                        height = height * scale;
                    }
                    double movex = frameRectScratch.x;
                    double movey = frameRectScratch.y;
                    if (frameRectScratch.width > width) {
                        movex = frameRectScratch.x + (frameRectScratch.width - width) * cs.getAlignmentHorizontal().getAlign();
                    }
                    if (frameRectScratch.height > height) {
                        movey = frameRectScratch.y + (frameRectScratch.height - height) * cs.getAlignmentVertical().getAlign();
                    }
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.translate(movex, movey);
                    if (scale != 0)
                        g2d.scale(scale, scale);
                    g2d.drawImage(image.getImage(), 0, 0, null);
                    g2d.dispose();
                }                // inner padding
                frameRectScratch.x += cs.getLeftPadding().getValInPoints();
                frameRectScratch.y += cs.getTopPadding().getValInPoints();
                frameRectScratch.width -= cs.getLeftRightPaddingValue();
                frameRectScratch.height -= cs.getTopBotPaddingValue();
                float cellWidth = cell.getWidth(Internationalisation.NOREPLACE, g);
                float bX, bW;
                if (cellWidth < frameRectScratch.width) {
                    float remainder = frameRectScratch.width - cellWidth;
                    float align = cs.getAlignmentHorizontal().getAlign();
                    if (cs.getAlignmentHorizontal().isBlock())
                        align = 0;
                    bX = remainder * align + frameRectScratch.x;
                    bW = cellWidth;
                } else {
                    bX = frameRectScratch.x;
                    bW = frameRectScratch.width;
                }
                float bH, bY;
                float cellHeight = cell.getHeight(Internationalisation.NOREPLACE, g);
                if (cellHeight < frameRectScratch.height) {
                    float remainder = frameRectScratch.height - cellHeight;
                    float align = cs.getAlignmentVertical().getAlign();
                    bY = frameRectScratch.y + remainder * align;
                    bH = cellHeight;
                } else {
                    bY = frameRectScratch.y;
                    bH = frameRectScratch.height;
                }
                Rectangle2D.intersect(tclipRectScratch, frameRectScratch, tclipRectScratch);
                g.setClip(tclipRectScratch);
                cell.paint(g, bX, bY, bW, bH, Internationalisation.NOREPLACE);
                if (cell.getVisibilityCondition() != null) {
                    g.setClip(tcellRectScratch);

                    Color color = g.getColor();
                    if (cell.getVisibilityCondition().contains(CheckFormulasForSchemaChange.erroneousFormula))
                        g.setColor(darkRed);
                    else
                        g.setColor(darkBlue);

                    g.setStroke(new BasicStroke(2));
                    g.draw(new Line2D.Float(colRequirements[c].off, rowRequirements[r].off + 12, colRequirements[c].off + 12, rowRequirements[r].off));
                    g.setColor(color);
                }
            }
        } catch (Error ex) {
            g.setClip(oldClip);
            hadError = ex;
            Trace.log(ex);
        }
        g.setClip(oldClip);
        if (hadError != null)
            throw new JoriaInternalError("error in paintCell(" + r + "/" + c + ", " + cell + ")", hadError);
        return span;
    }

    private void paintFrameBorderAndBackground(FrameStyle fs, Graphics2D g, Color background) {
        JoriaFrameBorder t = fs.getTopBorder();
        JoriaFrameBorder l = fs.getLeftBorder();
        JoriaFrameBorder b = fs.getBottomBorder();
        JoriaFrameBorder r = fs.getRightBorder();
        frameRectScratch.setRect(xOff, yOff, scaleToWidth, scaleToHeight);
        float x0 = frameRectScratch.x + l.getOuterSpacing().getValInPoints();
        float y0 = frameRectScratch.y + t.getOuterSpacing().getValInPoints();
        float x1 = frameRectScratch.x + frameRectScratch.width - r.getOuterSpacing().getValInPoints();
        float y1 = frameRectScratch.y + frameRectScratch.height - b.getOuterSpacing().getValInPoints();        // subtract margin to get filled rect
        frameRectScratch.x += l.getOuterSpacing().getValInPoints();
        if (l.getThickness() == 0) {
            frameRectScratch.x += 1 / getScale();
            frameRectScratch.width -= 1 / getScale();
        }
        frameRectScratch.y += t.getOuterSpacing().getValInPoints();
        frameRectScratch.width = frameRectScratch.width - l.getOuterSpacing().getValInPoints() - r.getOuterSpacing().getValInPoints();
        frameRectScratch.height = frameRectScratch.height - b.getOuterSpacing().getValInPoints() - t.getOuterSpacing().getValInPoints();        // paint cell background using text (content) background color
        Trace.check(background);
        g.setColor(background);
        g.fill(frameRectScratch);        // paint background image
        if (fs.getBackgroundImageName() != null) {
            ImageIcon backgroundImage = fs.getBackgroundImage(Internationalisation.NOREPLACE);
            float width = backgroundImage.getIconWidth();
            float scale = 0;
            FlexSize imageScale = fs.getBackgroundImageTargetWidth();
            SizeLimit limit = fs.getSizeLimit();

            final boolean mustScaleDown = limit == SizeLimit.AtMost && width > x1 - x0;
            final boolean mustScaleUp = limit == SizeLimit.AtLeast && width < x1 - x0;
            final boolean mustScale = limit == SizeLimit.Fix && width != x1 - x0;
            if (x1 - x0 != 0 && (mustScaleDown || mustScaleUp || mustScale)) {
                scale = (x1 - x0) / width;
            }
            if (imageScale != null && !imageScale.isExpandable()) {
                scale = imageScale.getVal() / width;
            }
            double movex = frameRectScratch.x;
            double movey = frameRectScratch.y;
            if (fs.getBackgroundImageX() != null)
                movex += fs.getBackgroundImageX().getValInPoints();
            if (fs.getBackgroundImageY() != null)
                movey += fs.getBackgroundImageY().getValInPoints();
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setClip(frameRectScratch);
            g2d.translate(movex, movey);
            if (scale != 0)
                g2d.scale(scale, scale);
            g2d.drawImage(backgroundImage.getImage(), 0, 0, null);
            g2d.dispose();
        }
        if (frameRectScratch.height < 0.0001 || frameRectScratch.width < 0.0001)
            return;
        if (l.getThickness() > 0) {
            float x2 = x0 + l.getThickness() / 2;
            paintCellBorder(l, g, x2, y0, x2, y1);
        }
        if (r.getThickness() > 0) {
            float x2 = x1 - r.getThickness() / 2;
            paintCellBorder(r, g, x2, y0, x2, y1);
        }
        if (t.getThickness() > 0) {
            float y2 = y0 + t.getThickness() / 2;
            paintCellBorder(t, g, x0, y2, x1, y2);
        }
        if (b.getThickness() > 0) {
            float y2 = y1 - b.getThickness() / 2;
            paintCellBorder(b, g, x0, y2, x1, y2);
        }
        frameRectScratch.x += l.getInnerPadding().getValInPoints() + l.getThickness();
        frameRectScratch.y += t.getInnerPadding().getValInPoints() + t.getThickness();
        frameRectScratch.width = frameRectScratch.width - l.getInnerPadding().getValInPoints() - r.getInnerPadding().getValInPoints() - l.getThickness() - r.getThickness();
        frameRectScratch.height = frameRectScratch.height - t.getInnerPadding().getValInPoints() - b.getInnerPadding().getValInPoints() - t.getThickness() - b.getThickness();
    }

    private void paintCellBorderAndBackground(Graphics2D g, Color background, int r, int c, CellStyle cs) {
        float x0 = frameRectScratch.x;
        float y0 = frameRectScratch.y;
        float x1 = frameRectScratch.x + frameRectScratch.width;
        float y1 = frameRectScratch.y + frameRectScratch.height;        // paint cell background using text (content) background color
        Trace.check(background);
        g.setColor(background);
        g.fill(frameRectScratch);
        if (frameRectScratch.height < 0.0001 || frameRectScratch.width < 0.0001)
            return;
        float topInset = 0;
        float leftInset = 0;
        float rightInset = 0;
        float bottomInset = 0;
        float leftTopInset = 0;// topBorder left inset
        float rightTopInset = 0;// topBorder right inset
        float leftBottomInset = 0;
        float rightBottomInset = 0;
        float topLeftInset = 0;
        float bottomLeftInset = 0;
        float topRightInset = 0;
        float bottomRightInset = 0;
        if (c == 0) {
            leftTopInset = myModel.getBorderAt(r, c, false, true).getThickness() / 2;
            leftBottomInset = myModel.getBorderAt(r + cs.getSpanVertical() - 1, c, false, true).getThickness() / 2;
        }
        if (c + cs.getSpanHorizontal() - 1 == myModel.getColCount() - 1) {
            rightTopInset = myModel.getBorderAt(r, c + cs.getSpanHorizontal() - 1, false, false).getThickness() / 2;
            rightBottomInset = myModel.getBorderAt(r + cs.getSpanVertical() - 1, c + cs.getSpanHorizontal() - 1, false, false).getThickness() / 2;
        }
        if (r == 0) {
            topLeftInset = myModel.getBorderAt(r, c, true, true).getThickness() / 2;
            topRightInset = myModel.getBorderAt(r, c + cs.getSpanHorizontal() - 1, true, true).getThickness() / 2;
        }
        if (r + cs.getSpanVertical() - 1 == myModel.getRowCount() - 1) {
            bottomLeftInset = myModel.getBorderAt(r + cs.getSpanVertical() - 1, c, true, false).getThickness() / 2;
            bottomRightInset = myModel.getBorderAt(r + cs.getSpanVertical() - 1, c + cs.getSpanHorizontal() - 1, true, false).getThickness() / 2;
        }
        for (int i = c; i < c + cs.getSpanHorizontal(); i++) {
            JoriaSimpleBorder top = myModel.getBorderAt(r, i, true, true);
            if (!JoriaSimpleBorder.isNull(top)) {
                topInset = Math.max(topInset, top.getThickness() / (r == 0 ? 2 : 1));
                float topOffset = r == 0 ? top.getThickness() / 2 : 0;
                float xx0 = colRequirements[i].off;
                if (c == i)
                    xx0 += leftTopInset;
                float xx1 = colRequirements[i].end();
                if (c == c + cs.getSpanHorizontal() - 1)
                    xx1 -= rightTopInset;
                float yy0 = y0 + topOffset;
                paintCellBorder(top, g, xx0, yy0, xx1, yy0);
            }
            JoriaSimpleBorder bottom = myModel.getBorderAt(r + cs.getSpanVertical() - 1, i, true, false);
            if (!JoriaSimpleBorder.isNull(bottom)) {
                bottomInset = Math.max(bottomInset, bottom.getThickness() / (r == myModel.getRowCount() - 1 ? 2 : 1));
                float bottomOffset = r == myModel.getRowCount() - 1 ? bottom.getThickness() / 2 : 0;
                float xx0 = colRequirements[i].off;
                if (c == i)
                    xx0 += leftBottomInset;
                float xx1 = colRequirements[i].end();
                if (i == c + cs.getSpanHorizontal() - 1)
                    xx1 -= rightBottomInset;
                float yy1 = y1 - bottomOffset;
                paintCellBorder(bottom, g, xx0, yy1, xx1, yy1);
            }
        }
        for (int i = r; i < r + cs.getSpanVertical(); i++) {
            JoriaSimpleBorder left = myModel.getBorderAt(i, c, false, true);
            if (!JoriaSimpleBorder.isNull(left)) {
                leftInset = Math.max(leftInset, left.getThickness() / (r == 0 ? 2 : 1));
                float leftOffset = c == 0 ? left.getThickness() / 2 : 0;
                float yy0 = rowRequirements[i].off;
                if (r == i)
                    yy0 += topLeftInset;
                float yy1 = rowRequirements[i].end();
                if (i == r + cs.getSpanVertical() - 1)
                    yy1 -= bottomLeftInset;
                float xx0 = x0 + leftOffset;
                paintCellBorder(left, g, xx0, yy0, xx0, yy1);
            }
            JoriaSimpleBorder right = myModel.getBorderAt(i, c + cs.getSpanHorizontal() - 1, false, false);
            if (!JoriaSimpleBorder.isNull(right)) {
                rightInset = Math.max(rightInset, right.getThickness() / (c == myModel.getColCount() - 1 ? 2 : 1));
                float rightOffset = c == myModel.getColCount() - 1 ? right.getThickness() / 2 : 0;
                float yy0 = rowRequirements[i].off;
                if (r == i)
                    yy0 += topRightInset;
                float yy1 = rowRequirements[i].end();
                if (i == r + cs.getSpanVertical() - 1)
                    yy1 -= bottomRightInset;
                float xx1 = x1 - rightOffset;
                paintCellBorder(right, g, xx1, yy0, xx1, yy1);
            }
        }
        frameRectScratch.x += leftInset;
        frameRectScratch.y += topInset;
        frameRectScratch.width -= leftInset + rightInset;
        frameRectScratch.height -= topInset + bottomInset;
    }

    private void paintCellBorder(JoriaSimpleBorder b, Graphics2D g, float x1, float y1, float x2, float y2) {
        if (JoriaSimpleBorder.isNull(b))
            return;
        Stroke oldStroke = g.getStroke();
        g.setColor(b.getColor());
        BasicStroke s;
        if (b.getLineStyle() == JoriaBorder.DOUBLE) {
            float t3 = b.getThickness() * 0.4f;
            s = new BasicStroke(t3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(s);
            if (t3 < 1)
                t3 = 1f;// adjust for low resolutions
            if (x1 == x2)// vertical
            {
                tempLine.y1 = y1;
                tempLine.y2 = y2;
                tempLine.x1 = x1 - t3;
                tempLine.x2 = x2 - t3;
                g.draw(tempLine);
                tempLine.x1 = x1 + t3;
                tempLine.x2 = x2 + t3;
                g.draw(tempLine);
            } else            // horizontal
            {
                tempLine.x1 = x1;
                tempLine.x2 = x2;
                tempLine.y1 = y1 - t3;
                tempLine.y2 = y2 - t3;
                g.draw(tempLine);
                tempLine.y1 = y1 + t3;
                tempLine.y2 = y2 + t3;
                g.draw(tempLine);
            }
        } else {
            if (b.getLineStyle() == JoriaSimpleBorder.DOT) {
                float[] dashArray = {b.getThickness(), b.getThickness()};
                s = new BasicStroke(b.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
            } else if (b.getLineStyle() == JoriaSimpleBorder.DASH) {
                float[] dashArray = {2 * b.getThickness(), b.getThickness()};
                s = new BasicStroke(b.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
            } else if (b.getLineStyle() == JoriaSimpleBorder.DOUBLE) {
                float[] dashArray = {b.getThickness(), b.getThickness()};
                s = new BasicStroke(b.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
            } else {
                s = new BasicStroke(b.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            }
            g.setStroke(s);
            tempLine.x1 = x1;
            tempLine.y1 = y1;
            tempLine.x2 = x2;
            tempLine.y2 = y2;
            g.draw(tempLine);
        }
        g.setStroke(oldStroke);
    }

    private void paintCells(Graphics2D g) {        //System.out.println("paintcells");
        Stroke myStroke = g.getStroke();
        for (int r = 0; r < myModel.getRowCount(); r++) {
            int c = 0;
            while (c < myModel.getColCount()) {
                c += paintCell(r, c, g);
            }
        }
        g.setStroke(myStroke);
    }

    public void paintCellTracker(Graphics2D g, CellLocation trackCell) {
        CellRequirements cr = colRequirements[trackCell.col];
        CellRequirements rr = rowRequirements[trackCell.row];
        CellDef cd = myModel.cellAt(trackCell.row, trackCell.col);
        float cellwidth = cr.act;
        float rowheight = rr.act;
        if (cd != null) {
            int hspan = cd.getCascadedStyle().calcSpanHorizontal();
            int vspan = cd.getCascadedStyle().calcSpanVertical();
            for (int i = 1; i < hspan; i++) {
                CellRequirements cr2 = colRequirements[trackCell.col + i];
                cellwidth += cr2.act;
            }
            for (int i = 1; i < vspan; i++) {
                CellRequirements rr2 = rowRequirements[trackCell.row + i];
                rowheight += rr2.act;
            }
        }
        float scale = getScale();        //Point2D.Float offset = container.getOffsetInOuter(this, trackCell.pageNo);
        float offset_y = componentPos;
        float offset_x = 0;
        g.translate(scale * offset_x, scale * offset_y);
        switch (trackCell.relative) {
            case CellLocation.ABOVE:
                frameRectScratch.setRect(scale * (cr.off + 4), scale * (rr.off + 6), scale * (cellwidth + 7), 3);
                g.fill(frameRectScratch);
                break;
            case CellLocation.BELOW:
            case CellLocation.FARBELOW:
                frameRectScratch.setRect(scale * (cr.off + 4), scale * (rr.off + rowheight + 6), scale * (cellwidth + 7), 3);
                g.fill(frameRectScratch);
                break;
            case CellLocation.LEFT:
                frameRectScratch.setRect(scale * (cr.off + 6), scale * (rr.off + 4), 3, scale * (rowheight + 7));
                g.fill(frameRectScratch);
                break;
            case CellLocation.RIGHT:
                frameRectScratch.setRect(scale * (cr.off + cellwidth + 6), scale * (rr.off + 4), 3, scale * (rowheight + 7));
                g.fill(frameRectScratch);
                break;
            case CellLocation.CENTER:
                frameRectScratch.setRect(scale * (cr.off + 9), scale * (rr.off + 9), scale * (cellwidth - 4), scale * (rowheight - 4));
                g.draw(frameRectScratch);
                break;
        }
        g.translate(-scale * offset_x, -scale * offset_y);
    }

    private void paintGrid(Graphics2D g) {
        if (isShowGrid()) {
            g.setColor(Color.lightGray);
            tPaintRect.setRect(xOff, yOff, envelopeWidth, envelopeHeight);
            g.draw(tPaintRect);

            if (container instanceof TemplateLayouter) // nested
            {
                g.setColor(Color.darkGray);
                tPaintRect.setRect(xOff - 1, yOff - 1, 5, 5);
            } else {

                if (myBox.getVisibilityCondition() != null) {
                    tPaintRect.setRect(xOff - 6, yOff, 6, 6);
                    if (myBox.getVisibilityCondition().contains(CheckFormulasForSchemaChange.erroneousFormula))
                        g.setColor(darkRed);
                    else
                        g.setColor(darkBlue);
                } else {
                    tPaintRect.setRect(xOff - 5, yOff, 5, 5);
                    g.setColor(Color.lightGray);
                }
            }

            g.fill(tPaintRect);
            g.setColor(Color.lightGray);
        }
        int nCols = myModel.getColCount();
        int nRows = myModel.getRowCount();
        int startCol = 0;
        int startRow = 0;
        float bottom = rowRequirements[nRows - 1].end();
        float right = colRequirements[nCols - 1].end();
        paintGridPiece(startCol, nCols, startRow, nRows, g, right, bottom);
    }

    private void paintGridPiece(int startCol, int nCols, int startRow, int nRows, Graphics2D g, float right, float bottom) {
        for (int col = startCol; col < nCols; col++)// vertical
        {
            tempLine.x1 = tempLine.x2 = colRequirements[col].off;
            for (int row = startRow; row < nRows; row++) {
                CellDef cellDef = myModel.getCoverCell(row, col);
                if (cellDef != null && cellDef.getCascadedStyle().getSpanHorizontal() > 1)
                    continue;
                if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(row, col, false, true)))
                    continue;
                tempLine.y1 = rowRequirements[row].off;
                tempLine.y2 = rowRequirements[row].end();
                g.draw(tempLine);
            }
        }
        tempLine.x1 = tempLine.x2 = right;
        for (int row = startRow; row < nRows; row++) {
            if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(row, nCols - 1, false, false)))
                continue;
            tempLine.y1 = rowRequirements[row].off;
            tempLine.y2 = rowRequirements[row].end();
            g.draw(tempLine);
        }
        for (int row = startRow; row < nRows; row++)// horizontal
        {
            tempLine.y1 = tempLine.y2 = rowRequirements[row].off;
            for (int col = startCol; col < nCols; col++) {
                CellDef cellDef = myModel.getCoverCell(row, col);
                if (cellDef != null && cellDef.getCascadedStyle().getSpanVertical() > 1)
                    continue;
                if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(row, col, true, true)))
                    continue;
                tempLine.x1 = colRequirements[col].off;
                tempLine.x2 = colRequirements[col].end();
                g.draw(tempLine);
            }
        }
        tempLine.y1 = tempLine.y2 = bottom;
        for (int col = startCol; col < nCols; col++) {
            CellDef cellDef = myModel.getCoverCell(nRows - 1, col);
            if (cellDef != null && cellDef.getCascadedStyle().getSpanVertical() <= 1)
                continue;
            if (cellDef != null) {
                Point pos = myModel.getCellPosition(cellDef);
                if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(pos.y, pos.x, true, false)))
                    continue;
            }
            if (!JoriaSimpleBorder.isNull(myModel.getBorderAt(nRows - 1, col, true, false)))
                continue;
            tempLine.x1 = colRequirements[col].off;
            tempLine.x2 = colRequirements[col].end();
            g.draw(tempLine);
        }
    }

    public void paintIt(Graphics2D g) {
        if (myModel.getColCount() <= 0 || myModel.getRowCount() <= 0)
            return;
        if (!layoutValid)
            layoutOuter(this, g);
        if (scaleToWidth < 0.0001 || scaleToHeight < 0.0001)
            return;
        Graphics2D gComp = (Graphics2D) g.create();
        Error hadError = null;
        try {
            FrameStyle fs = myBox.getCascadedFrameStyle();
            frameRectScratch.setRect(xOff, yOff, scaleToWidth, scaleToHeight);
            if (myBox.getBoxType() == TemplateBoxInterface.pageBodyBox) {
                Trace.logDebug(Trace.layout, "painting body " + frameRectScratch + " frame " + fs.getTopBorder().getThickness() + " with " + gComp.getClass().getName());
            } else if (myBox instanceof PageLevelBox && myBox.isFooter()) {
                Trace.logDebug(Trace.layout, "painting footer [" + round(xOff) + " ," + round(yOff) + " ;" + round(scaleToWidth) + " ," + round(scaleToHeight) + "]");
            }
            paintFrameBorderAndBackground(fs, gComp, fs.getBackground());
            paintCells(gComp);
            paintRepeaters0(gComp, myModel.getRepeaters());
            if (isShowGrid())
                paintGrid(gComp);
        } catch (Error r) {
            System.err.println("Error painting template " + r.getMessage());
            Trace.log(r);            //r.printStackTrace();
            hadError = r;
        } finally {
            gComp.dispose();
        }
        if (hadError != null)
            throw new JoriaInternalError("Error painting template " + hadError.getMessage(), hadError);
    }

    private final RoundRectangle2D.Float tableGrid = new RoundRectangle2D.Float();
    private final BasicStroke repeaterIndicatorStroke = new BasicStroke(2);

    private void paintRepeaters0(Graphics2D g, Repeater[] ra) {
        tableGrid.archeight = tableGrid.arcwidth = 8;
        Stroke oldStroke = g.getStroke();
        g.setStroke(repeaterIndicatorStroke);
        paintRepeaters(g, ra);
        g.setStroke(oldStroke);
    }

    private void paintRepeaters(Graphics2D g, Repeater[] ra) {
        if (ra == null)
            return;
        for (Repeater sf : ra) {
            if (isShowRepeaters())
                paintRepeaterIndicator(g, sf);
        }
    }

    private void paintRepeaterIndicator(Graphics2D g, Repeater sf) {
        g.setColor(Color.green.darker().darker());        //g.setStroke(repeaterIndicatorStroke);
        RepeaterDepthInfo info = repeaterDepth.get(sf);
        if (info == null) {
            Trace.logError("depthInfo null in paintRepeaters " + sf.getAccessor().getLongName() + " " + sf.getStartRow() + "-" + sf.getEndRow() + " " + sf.getStartCol() + "-" + sf.getEndCol());
            //noinspection UnusedDeclaration
            RepeaterDepthInfo info2 = repeaterDepth.get(sf);
            return;
        }
        Trace.logDebug(Trace.layout, "paintRepeaters " + sf.getAccessor().getLongName() + " " + sf.getStartRow() + "-" + sf.getEndRow() + " " + sf.getStartCol() + "-" + sf.getEndCol());
        float topInset = topBorderSpace[sf.getStartRow() - sf.getHeaderRows()][sf.getStartCol()] + (info.topDepth - 1) * repeaterSpace;
        float leftInset = leftBorderSpace[sf.getStartRow() - sf.getHeaderRows()][sf.getStartCol()] + (info.leftDepth - 1) * repeaterSpace;
        float rightInset = rightBorderSpace[sf.getEndRow()][sf.getEndCol()] + (info.rightDepth - 1) * repeaterSpace;
        float bottomInset = bottomBorderSpace[sf.getEndRow()][sf.getEndCol()] + (info.bottomDepth - 1) * repeaterSpace;
        tableGrid.x = colRequirements[sf.getStartCol()].off + 2 + leftInset;
        tableGrid.y = rowRequirements[sf.getStartRow() - sf.getHeaderRows()].off + 2 + topInset;
        tableGrid.width = colRequirements[sf.getEndCol()].end() - tableGrid.x - 2 - rightInset;
        tableGrid.height = rowRequirements[sf.getEndRow() + sf.getFooterRows()].end() - tableGrid.y - 2 - bottomInset;
        if (sf.getHeaderRows() > 0) {
            float hh = rowRequirements[sf.getStartRow()].off + leftBorderSpace[sf.getStartRow()][sf.getStartCol()] + 2 + (info.neckDepth - 1) * repeaterSpace;
            tempLine.setLine(tableGrid.x, hh, tableGrid.x + tableGrid.width, hh);
            g.draw(tempLine);
        }
        if (sf.getFooterRows() > 0)// das wird nicht funktionieren
        {
            float fh = rowRequirements[sf.getEndRow() + 1].off;
            tempLine.setLine(tableGrid.x, fh, tableGrid.x + tableGrid.width, fh);
            g.draw(tempLine);
        }
        g.draw(tableGrid);
        paintRepeaters(g, sf.getRepeaterList().get());
    }

    public void positionEditorComponent(int col, int row, Graphics2D g) {
        if (!layoutValid)
            layoutOuter(this, g);
        EditableCellDef cell = (EditableCellDef) myModel.cellAt(row, col);
        Trace.check(cell, "no cell at r=" + row + " c=" + col);
        getCellAllocationAt(row, col, frameRectScratch);
        float scale = getScale();
        Component cellComp = cell.getEditor(scale);		/*
						final Color oldColor = g.getColor();
						g.setColor(Color.yellow);
						g.drawRect(Math.round(scale * (offset.x + frameRectScratch.x + 7)), Math.round(scale * (offset.y + frameRectScratch.y + 7)), Math.round(scale * (frameRectScratch.width)), Math.round(scale * (frameRectScratch.height)));
						g.setColor(oldColor);
				*/        //Point2D.Float offset = container.getOffsetInOuter(this, pageNo);		//cellComp.setBounds(round(scale * (offset.x + frameRectScratch.x + 7)), round(scale * (offset.y + frameRectScratch.y + 7)), round(scale * (frameRectScratch.width)), round(scale * (frameRectScratch.height)));
        cellComp.setBounds(round(scale * (frameRectScratch.x + PageLayouter.displayHOffset))-1, round(scale * (componentPos + frameRectScratch.y + PageLayouter.displayVOffset))-1, round(scale * (frameRectScratch.width))+2, round(scale * (frameRectScratch.height))+2);
        Trace.logDebug(Trace.layout, "positionEditorComponent " + round(frameRectScratch.x) + " " + round(frameRectScratch.y) + " " + round(frameRectScratch.width) + " " + round(frameRectScratch.height));        //CellStyle b = cell.getCascadedStyle();		//cellComp.setBackground(b.getBackground());		//cellComp.setForeground(b.getForeground());		//cellComp.setFont(b.getStyledFont());
    }

    private void repositionX(float x) {
        float dx = x - xOff;
        Trace.logDebug(Trace.layout, "repositionX " + boxKind() + " x: " + x + " xoff: " + xOff);
        for (int j = 0; j < myModel.getColCount(); j++) {
            colRequirements[j].off += dx;
        }
        xOff = x;
        if (hasNestedLayouters) {
            for (int r = 0; r < myModel.getRowCount(); r++) {
                final ArrayList<CellDef> rowcells = myModel.getRow(r);
                for (int c = 0; c < myModel.getColCount(); c++) {
                    CellDef cd = rowcells.get(c);
                    if (cd instanceof NestingCellDef) {
                        NestingCellDef ncd = (NestingCellDef) cd;
                        ncd.getInnerBox().getTemplate().getLayouter().repositionX(colRequirements[c].off);
                    }
                }
            }
        }
    }

    public void repositionY(float y) {
        float dy = y - yOff;
        Trace.logDebug(Trace.layout, "reposition " + boxKind() + " y: " + y + " yoff: " + yOff);
        for (int i = 0; i < myModel.getRowCount(); i++) {
            rowRequirements[i].off += dy;
        }
        yOff = y;
        if (hasNestedLayouters) {
            for (int r = 0; r < myModel.getRowCount(); r++) {
                final ArrayList<CellDef> rowcells = myModel.getRow(r);
                for (int c = 0; c < myModel.getColCount(); c++) {
                    CellDef cd = rowcells.get(c);
                    if (cd instanceof NestingCellDef) {
                        NestingCellDef ncd = (NestingCellDef) cd;
                        ncd.getInnerBox().getTemplate().getLayouter().repositionY(rowRequirements[r].off);
                    }
                }
            }
        }
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void fireChange() {
        getContainer().noteChange();
        Trace.logDebug(Trace.layout, "TemplateLayout requirements changed");        //requirementsValid = false;
        invalidateLayout();
        if (changeListeners.size() == 0)
            return;
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener changeListener : changeListeners) {
            changeListener.stateChanged(e);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public float getReqWidth() {
        return reqWidth;
    }
}
