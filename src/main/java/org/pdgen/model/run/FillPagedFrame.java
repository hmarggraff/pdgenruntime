// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
// Created on July 5, 2000, 2:22 PM

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaUserError;
import org.pdgen.env.Res;
import org.pdgen.model.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.NestingCellDef;
import org.pdgen.model.cells.PictureCellBase;
import org.pdgen.model.cells.RenderingCellDef;
import org.pdgen.model.style.*;
import org.pdgen.styledtext.model.StyledParagraphLayouterList;
import org.pdgen.styledtext.model.StyledParagraphList;
import org.pdgen.util.RuntimeComponentFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Top level of fill process for a box
 * Created when a box is filled.
 * Disappears when box is complete.
 * May be recursive for nested boxes.
 * Header/Body/Footer are each a box.
 * First the fillbox is created.
 * Then ill/hfill is being called.
 * fill/hfill returns the Graphicselements
 * After that check whether the page overflowed
 * during processing of the box.
 */
public class FillPagedFrame extends OutputMode {
    // the output variable
    protected DisplayList dest;
    //protected DisplayList lDest;
    protected DisplayList lDestBorderLines;
    protected MutableFlexSize outRowSize = new MutableFlexSize();// consolidates the row size settings from the different columns
    protected float lineHeight;// the actual accumulated line height
    protected float lastLineHeight;
    protected float maxHeight;
    protected float verticalPos;
    protected float verticalStart;
    protected float remainingHeight;
    protected PageEnv2 pageRun;
    protected ArrayList<TableBorderRequirements> spans = new ArrayList<TableBorderRequirements>();
    protected Paragraph[] splitters;
    protected Paragraph[] saveSplitters;
    protected float[][] colReq;
    protected float[] colPos;
    protected float[] colWidths;
    protected TableBorderRequirements[] lastTableBorderReq;
    protected TableBorderRequirements[] tableBorderReq;
    protected BorderHolder[] savedBorders;
    protected boolean spannedSplittersActive;
    protected int effectiveOutRow;
    /**
     * stores the state when we have to backtrack in case some lines to be kept together wont fit on the page.
     */
    int destRowStart;
    //int lDestRowStart;
    int lDestLinesRowStart;
    protected int destBreak = -1;
    //int lDestBreak = -1;
    int lDestLinesBreak = -1;
    int keeps;
    int destStart;
    //int lDestStart;
    protected int nCols;
    protected float[] maxColWidths;
    int dynaCols;
    ArrayList<GraphElContent> tocEntries;
    //GraphElContent splitterGrel;
    // Da die Borders in der ersten und letzen Zeile auf einer Seite zus?tzlichen Platz ben?tigen k?nnen,
    // m?ssen wir diesen in die Platzberechung mit einbeziehen.
    float maxTopBorderSpace;
    float maxBottomBorderSpace;
    boolean firstBodyRowOnPage;
    protected boolean firstBodyOnPage;
    protected boolean footerOrHeader;
    protected GraphElPicture backgroundImageElement;
    protected float backgroundImageScale;
    protected float backgroundImageHeight;
    protected float backgroundImageWidth;
    protected float usedFrameHeight;
    float spaceForTableBorderAtPageBottom;
    // if the frame has multiple frame columns this is instantiated
    FrameFlowSupport multiColFrameSupport;
    int inFrameColumn;

    /**
     * creates FillBox for a breakable Frame e.g. a body or a nested frame
     * this constructor must be used in conjunction with hFill to pass in the actual remaining height on the page
     *
     * @param template       frame to run
     * @param rootVal        data root
     * @param pageRunner     the pages
     * @param footerOrHeader flag if this is a header or footer frame
     * @throws JoriaDataException in case of problems
     */
    public FillPagedFrame(TemplateModel template, DBData rootVal, PageEnv2 pageRunner, boolean footerOrHeader) throws JoriaDataException {
        super(template, rootVal);
        this.footerOrHeader = footerOrHeader;
        nCols = template.getColCount();
        pageRun = pageRunner;
        maxHeight = Float.NaN;
        verticalPos = Float.NaN;
        init();
    }

    public FillPagedFrame(TemplateModel template, FillPagedFrame outer) throws JoriaDataException {
        super(template, outer.rootVal);
        nCols = template.getColCount();
        footerOrHeader = outer.footerOrHeader;
        pageRun = outer.pageRun;
        maxHeight = Float.NaN;
        verticalPos = Float.NaN;
        tableHeaderCols = new Repeater[template.getRowCount()][nCols];
    }

    /**
     * loads the data for the frame from the datasource  stores it with a runner into fill
     *
     * @throws JoriaDataException for data access errors
     */
    protected void init() throws JoriaDataException {
        RDRangeBase rdef = template.getColumnDefs();
        long t0 = System.currentTimeMillis();
        RVTemplate rvt = (RVTemplate) rdef.buildRunValue(rootVal, this, new Stack<RDBase>(), new Stack<RVAny>(), getRunEnv().getGraphics2D());
        long t1 = System.currentTimeMillis() - t0;
        if (t1 > 100) {
            final PageLevelBox tParent = template.getFrame().getPageLevelParent();
            Trace.logDebug(Trace.fill, "Frame " + tParent.getParamString() + " load time= " + (t1));//trdone
        }
        innerInit(rdef, rvt);
    }

    protected void innerInit(final RDRangeBase rdef, final RVTemplate rvt) {
        template.getRepeaterList().getRepeaterForHeaders(tableHeaderCols);
        usedFrameHeight = 0;
        if (!footerOrHeader)// warum das?
            rootVal = null;
        if (template.getCrosstab() != null)
            dynaCols = rvt.subs[0].length;
        else
            dynaCols = nCols;
        colReq = new float[template.getRowCount()][dynaCols];
        rdef.calcMaxWidth(colReq, rvt, getRunEnv().getLocale(), getRunEnv().getGraphics2D());
        colPos = new float[dynaCols + 1];
        tableBorderReq = new TableBorderRequirements[dynaCols];
        lastTableBorderReq = new TableBorderRequirements[dynaCols];
        savedBorders = new BorderHolder[dynaCols];
        for (int i = 0; i < dynaCols; i++) {
            tableBorderReq[i] = new TableBorderRequirements();
            lastTableBorderReq[i] = new TableBorderRequirements();
            savedBorders[i] = new BorderHolder();
        }
        colWidths = new float[dynaCols];
        maxColWidths = new float[dynaCols];
        float flowingFrameColumnWidth = layout();
        fill = RuntimeComponentFactory.instance().getFillRunner(rdef, rvt, this);
        lDestBorderLines = new DisplayList();
        multiColFrameSupport = RuntimeComponentFactory.instance().getFrameFlowSupport(flowingFrameColumnWidth, this);
    }

    public float fillInner(RunRangeBase rr, final RDRange defs, final RVTemplate subVals, int row, int col) throws JoriaDataException {
        final TemplateModel innerModel = (TemplateModel) defs.getModel();
        final NestingCellDef cellDef = ((NestedBox) innerModel.getFrame()).getCell();
        FillInnerFrame inner = new FillInnerFrame(innerModel, this, defs, subVals, col, cellDef);
        final float ret = inner.hFill(remainingHeight, verticalPos, dest, false);
        if (inner.more()) {
            if (splitters == null)
                splitters = new Paragraph[dynaCols];
            splitters[col] = inner;
            lastRowState = LastRowState.donePartial;
        }
        float usedHeight = remainingHeight - ret;
        TableBorderRequirements b = getTableBorderReqCol();
        b.buildTableBorderRequirement(row, col, cellDef.getCascadedStyle(), cellDef, null, null, null, null, firstBodyRowOnPage, false, rr);
        b.hContent = usedHeight;
        updateLineHeight(b);
        colInCurrRow++;
        return ret;
    }

    /**
     * fill the data of this frame to the display list till either the frame is finished or no more space on the
     * page remains.
     *
     * @param givenHeight     space available for the current frame
     * @param verticalStart   where to start the frame on the page
     * @param dest            destination for the generated GraphEls
     * @param firstBodyOnPage if this is the topmost body frame.
     * @return the space used by this frame
     */
    public float hFill(float givenHeight, float verticalStart, DisplayList dest, boolean firstBodyOnPage) {
        if (lastRowState == LastRowState.errorOccured)
            throw new JoriaAssertionError("FillBox.fill called when in error state");
        if (Env.instance().isCancelPressed())
            throw new UserAbortError();
        this.dest = dest;
        destStart = dest.size();
        //lDestStart = lDest.size();
        lastLineHeight = lineHeight = 0;
        this.firstBodyOnPage = firstBodyOnPage;
        firstBodyRowOnPage = true;
        this.verticalStart = verticalStart;
        outRow = 0;
        effectiveOutRow = 0;
        FrameStyle fs = template.getFrame().getCascadedFrameStyle();
        inFrameColumn = multiColFrameSupport.startMultiColFrame(givenHeight - fs.getBottomBorder().getInset() - spaceForTableBorderAtPageBottom);
        destBreak = -1;
        lDestLinesBreak = -1;
        usedFrameHeight = 0;
        placeEventualBackgroundImage(fs, verticalStart, dest);
        while (inFrameColumn >= 0 && lastRowState != LastRowState.endOfData)// for each column (usually only one)
        {
            remainingHeight = multiColFrameSupport.initRemainingHeight();// normal remaining height except for last page
            keeps = 0;
            verticalPos = verticalStart;
            if (usedFrameHeight == 0) {
                this.verticalStart += fs.getTopBorder().getOuterSpacing().getValInPoints();
                verticalPos += fs.getTopBorder().getTotalInset();
                remainingHeight -= fs.getTopBorder().getTotalInset();
            } else {
                verticalPos += fs.getTopBorder().getInset();
                remainingHeight -= fs.getTopBorder().getInset();
            }
            fill.removeSavePoints();
            if (!fs.getHeight().isExpandable()) {
                if (givenHeight < fs.getHeight().getVal() && (fs.getYPos() == null || fs.getYPos().isExpandable())) {
                    if (lastRowState == LastRowState.doneRedo && firstBodyOnPage && inFrameColumn == 0) {
                        Env.instance().tell(Res.str("Data_does_not_fit_onto_any_page_Please_change_your_page_design"));
                        throw new JoriaUserError(Res.str("Data_does_not_fit_onto_any_page_Please_change_your_page_design"));
                    }
                    Trace.logDebug(Trace.run, "fix height frame does not fit page");
                    lastRowState = LastRowState.doneRedo;
                    return 0;
                }
                remainingHeight = fs.getHeight().getVal() - fs.getBottomBorder().getTotalInset() - fs.getTopBorder().getTotalInset();
            }
            if (template.getRowCount() == 0 || nCols == 0) {
                Trace.logDebug(Trace.run, "Skipped empty model: " + template.getFrame().getBoxTypeName());
                lastRowState = LastRowState.endOfData;
                return remainingHeight - fs.getBottomBorder().getOuterSpacing().getValInPoints();
            }
            boolean hadFullRow = false;
            try {
                // if this frame was started on a previous page, first output remaining data and table header.
                if (lastRowState != LastRowState.proceed && headersAndSplitters()) {
                    if (inFrameColumn == 0)
                        return remainingHeight;
                } else
                    fill.startBodyAt(outRow);
                while (lastRowState == LastRowState.proceed) {
                    if (Env.instance().isCancelPressed())
                        throw new UserAbortError();
                    oneOutRow();// try to output one data row
                    if (lastRowState != LastRowState.doneRedo)
                        hadFullRow = true;
                }
                // here we have either the end of the page or the end of the data
                // the frame did not fit on the page
                if (lastRowState != LastRowState.endOfData) {
                    checkSavePoints(true);
                    fill.pageBreak(lastRowState);// end of page
                    doTableBorderAtPageBreak();
                    if (lastRowState != LastRowState.donePartial)
                        pageRun.backOutRowAccus();
                }
            } catch (JoriaDataException ex) {
                lastRowState = LastRowState.errorOccured;
                throw new RunBreakException(ex);
            }
            if (!fs.getHeight().isExpandable()) {
                if (lastRowState == LastRowState.doneRedo || lastRowState == LastRowState.doneComplete || lastRowState == LastRowState.donePartial) {
                    lastRowState = LastRowState.endOfData;
                }
                remainingHeight = givenHeight - fs.getHeight().getVal();
            }
            if (lastRowState == LastRowState.doneRedo) {
                if (firstBodyOnPage && (effectiveOutRow == 0 || !hadFullRow) && inFrameColumn == 0) {
                    Env.instance().tell(Res.str("Data_does_not_fit_onto_any_page_Please_change_your_page_design"));
                    throw new JoriaUserError(Res.str("Data_does_not_fit_onto_any_page_Please_change_your_page_design"));
                }
                dest.truncate(destRowStart);
                //lDest.truncate(lDestRowStart);
                lDestBorderLines.truncate(lDestLinesRowStart);
                remainingHeight = 0;
            }
            if (lastRowState == LastRowState.endOfData) {
                remainingHeight -= fs.getBottomBorder().getOuterSpacing().getValInPoints();
            } else {
                usedFrameHeight += verticalStart - verticalPos;
            }
            if (effectiveOutRow > 0) {
                frameEndSpan();// TODO set height of background image
                frameBorders();
            }
            inFrameColumn--;
            multiColFrameSupport.incrementColPositions();
        }
        dest.add(lDestBorderLines);
        lDestBorderLines.clear();
        return multiColFrameSupport.getRemainingHeight();
    }

    private void placeEventualBackgroundImage(final FrameStyle fs, final float verticalStart, final DisplayList dest) {
        if (fs.getBackgroundImageName() != null) {
            ImageIcon backgroundImage = fs.getBackgroundImage(getRunEnv().getLocale());
            if (backgroundImageScale == 0) {
                backgroundImageScale = Float.NaN;
                backgroundImageHeight = backgroundImage.getIconHeight();
                backgroundImageWidth = backgroundImage.getIconWidth();
            }
            float posy = 0;
            if (fs.getBackgroundImageY() != null)
                posy = fs.getBackgroundImageY().getValInPoints();
            if (usedFrameHeight < backgroundImageHeight + posy) {
                backgroundImageElement = new GraphElPicture(fs.getBackgroundImageName(), backgroundImage, TextStyle.transparent, backgroundImageScale, null, false, null);
                posy += verticalStart + fs.getTopBorder().getOuterSpacing().getValInPoints() - usedFrameHeight;
                backgroundImageElement.yContent = posy;
                float x;
                if (fs.getXPos() != null && fs.getXPos().getUnit() != FlexSize.flex) {
                    x = fs.getXPos().getVal();
                } else {
                    x = getRunEnv().getTemplate().getPage().getCascadedPageStyle().getLeftMargin().getValInPoints();
                }
                if (fs.getBackgroundImageX() != null)
                    x += fs.getBackgroundImageX().getValInPoints();
                backgroundImageElement.xContent = x;
                backgroundImageElement.wContent = backgroundImageWidth;
                backgroundImageElement.hContent = backgroundImageHeight;
                backgroundImageElement.xEnvelope = x;
                backgroundImageElement.yEnvelope = backgroundImageElement.yContent;
                backgroundImageElement.wEnvelope = backgroundImageWidth;
                backgroundImageElement.hEnvelope = backgroundImageHeight;
                //backgroundImageElement.width = backgroundImageWidth;
                //backgroundImageElement.height = backgroundImageHeight;
                //backgroundImageElement.background = backgroundImage;
                dest.add(backgroundImageElement);
            } else
                backgroundImageElement = null;
        }
    }

    protected void oneOutRow() throws JoriaDataException {
        Trace.logDebug(Trace.fill, "outRow " + outRow + " remainingHeight: " + remainingHeight);
        initRow(true);
        fill.row();
        if (splitters != null) {
            for (int i = 0; i < dynaCols; i++) {
                savedBorders[i].save(tableBorderReq[i]);
            }
        }
        if (lastRowState == LastRowState.proceed || lastRowState == LastRowState.donePartial || lastRowState == LastRowState.doneComplete) {
            rowDone();
        } else if (lastRowState == LastRowState.doneRedo) {
            for (int i = 0; splitters != null && i < splitters.length && i < colInCurrRow; i++) {
                Paragraph splitter = splitters[i];
                if (splitter != null)
                    splitter.backupSlice();
            }
        }
        if (lastRowState == LastRowState.proceed)
            lastRowState = fill.advance();
    }

    protected void initRow(final boolean afterHeaderAndSplitter) throws JoriaDataException {
        maxBottomBorderSpace = 0;
        maxTopBorderSpace = 0;
        colInCurrRow = 0;
        if (spannedSplittersActive)
            spannedSplittersActive = false;
        else {
            destRowStart = dest.size();
            lDestLinesRowStart = lDestBorderLines.size();
            lastRowState = LastRowState.proceed;
            outRowSize.setUnit(FlexSize.pt);
            outRowSize.setVal(0);
            if (afterHeaderAndSplitter)
                rollTableBorderRequirements();
            for (TableBorderRequirements aTableBorderReq : tableBorderReq) {
                aTableBorderReq.reset();
            }
        }
    }

    private void rollTableBorderRequirements() {
        TableBorderRequirements[] t = lastTableBorderReq;// switch table Border requirements, so that we keep the last state
        lastTableBorderReq = tableBorderReq;
        tableBorderReq = t;
    }

    void rowDone() {
        // Wir haben einen auslaufenden Spanner hier, also können wir die Zeilenhöhe setzten.
        // Alle Spanner die nur noch fixe Höhen haben, berücksichtigen wir auch.
        // Sind wir parallel eines Repeater wurde der Span erhöht.
        if (fill.getSourceRow() >= 0 && template.getRowSizingAt(fill.getSourceRow()).isExpandable()) {
            for (TableBorderRequirements tableBorderRequirements : spans) {
                boolean lastFlexLine = true;
                float additionalSpace = 0;
                for (int i = 1; i <= tableBorderRequirements.vSpanCount; i++) {
                    FlexSize size = template.getRowSizingAt(i + fill.getSourceRow());
                    if (size.isExpandable())
                        lastFlexLine = false;
                    else
                        additionalSpace += size.getVal();
                }
                if (lastFlexLine)
                    lineHeight = Math.max(lineHeight, tableBorderRequirements.vSpanHeight - additionalSpace);
            }
        }
        doCellBorders();
        firstBodyRowOnPage = false;
        verticalPos += lineHeight;
        remainingHeight -= lineHeight;
        multiColFrameSupport.startBlock(true);
        Trace.logDebug(Trace.fill, "Done outRow " + outRow);
        if (lineHeight > 0)
            effectiveOutRow++;
        outRow++;
        lastLineHeight = lineHeight;
        lineHeight = 0;
        pageRun.completeRowAccus();
        for (TableBorderRequirements tableBorderRequirements : spans) {
            tableBorderRequirements.vSpanHeight -= lastLineHeight;
            tableBorderRequirements.vSpanCount--;
        }
    }

    protected boolean headersAndSplitters() throws JoriaDataException {
        Trace.logDebug(Trace.fill, "headerRow " + outRow + " remainingHeight: " + remainingHeight);
        int headerRow = 0;
        while (true) {
            initRow(false);
            if (!fill.headerRow(headerRow++))
                break;
            rowDone();
        }
        fill.startBodyAt(outRow);
        if (splitters == null)
            return false;// no splitters remain
        boolean nonSpannedSplitters = false;
        boolean spannedSplitters = false;
        for (Paragraph splitter : splitters) {
            if (splitter != null && splitter.getRemainingSpan() == 0)
                nonSpannedSplitters = true;
            if (splitter != null && splitter.getRemainingSpan() > 0)
                spannedSplitters = true;
        }
        boolean splittersRemain = false;
        if (nonSpannedSplitters) {
            for (int i = 0; i < splitters.length; i++) {
                Paragraph cell = splitters[i];
                TableBorderRequirements b = getTableBorderReqCol();
                if (cell == null || cell.getRemainingSpan() > 0)// die kommen später
                {
                    b.cd = null;
                    colInCurrRow++;
                } else {
                    final boolean fixedHeight = b.rowSizing != null && !b.rowSizing.isExpandable();
                    cell.nextSlice(b, remainingHeight, colPos[i], verticalPos, colWidths[i], getRunEnv().getGraphics2D(), fixedHeight, this);
                    b.cd = cell.getUnSplit();
                    updateLineHeight(b);
                    if (cell.more())
                        splittersRemain = true;
                    else
                        splitters[i] = null;
                    if (b.grel != null) // single cell splitter (i.e. not nested frames positioned here)
                    {
                        b.grel.x = getXPos();// x position of painted rect
                        b.grel.y = getYPos();// y position of painted rect
                        b.grel.width = cell.getEnvelopeWidth() + b.wDiff;
                        b.grel.setContentX(b.grel.x + cell.getInnerX(), cell.getEnvelopeWidth(), b.wContent, cell.getUnSplit().getCascadedStyle(), getRunEnv().getGraphics2D(), b.grel.x + b.leftBorderInset);
                        b.grel.yContent += b.grel.y + cell.getInnerY();
                        b.grel.yEnvelope += b.grel.y + b.topBorderInset;
                    }
                    colInCurrRow++;
                }
            }
            for (int i = 0; i < dynaCols; i++) {
                savedBorders[i].restore(tableBorderReq[i], getYPos());
            }
            rowDone();
        }
        if (spannedSplitters && !splittersRemain) {
            if (nonSpannedSplitters)
                initRow(false);
            // diese Splitters werden in die erste Zeile dieser Seite gemischt
            for (int i = 0; i < splitters.length; i++) {
                Paragraph cell = splitters[i];
                if (cell == null || cell.getRemainingSpan() == 0)// die habe wir schon
                {
                    TableBorderRequirements b = getTableBorderReqCol();
                    b.cd = null;
                    colInCurrRow++;
                } else {
                    TableBorderRequirements b = getTableBorderReqCol();
                    cell.nextSlice(b, remainingHeight, colPos[i], verticalPos, colWidths[i], getRunEnv().getGraphics2D(), !b.rowSizing.isExpandable(), this);
                    b.cd = cell.getUnSplit();
                    updateLineHeight(b);
                    hasSpace(b, cell.getRemainingSpan());
                    if (cell.more()) {
                        splittersRemain = true;// TODO d.h. daß diese Zelle auch nicht auf die komplette zweite Seite passt.
                    }
                    // machen wir hier dann richtig weiter
                    else
                        splitters[i] = null;
                    if (b.grel != null) {
                        b.grel.x = getXPos();// x position of painted rect
                        b.grel.y = getYPos();// y position of painted rect
                        b.grel.width = cell.getEnvelopeWidth() + b.wDiff;
                        b.grel.setContentX(b.grel.x + cell.getInnerX(), cell.getEnvelopeWidth(), b.wContent, cell.getUnSplit().getCascadedStyle(), getRunEnv().getGraphics2D(), b.grel.x + b.leftBorderInset);
                        b.grel.yContent += b.grel.y + cell.getInnerY();
                        b.grel.yEnvelope += b.grel.y + b.topBorderInset;
                        b.hContent = b.grel.width;
                    }
                    colInCurrRow++;
                }
            }
            for (int i = 0; i < dynaCols; i++) {
                savedBorders[i].restore(tableBorderReq[i], getYPos());
            }
            spannedSplittersActive = true;
        }
        if (splittersRemain) {
            lastRowState = LastRowState.donePartial;
        } else {
            splitters = null;
            lastRowState = fill.advance();
        }
        return splittersRemain;
    }

    public boolean generateOutput(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException {
        return rr.generateOutput(cd, row, c, firstBodyRowOnPage);
    }

    public boolean generateOutputRepeatedCell(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat, boolean lastRepeat) throws JoriaDataException {
        return rr.generateOutputRepeatedCell(cd, row, col, firstRepeat, lastRepeat, rr.getRepeater(), firstBodyRowOnPage);
    }

    public boolean addNullCell(RunRangeBase rr, int templateRow, int templateCol) {
        return rr.generateOutputNullCell(templateRow, templateCol, firstBodyRowOnPage);
    }

    protected void frameEndSpan() {
        if (spans == null || spans.size() == 0)
            return;
        for (TableBorderRequirements span1 : spans) {
            float delta = verticalPos - span1.vSpanStart - span1.vSpanContent.getHeightFloat();
            if (delta > 0) {
                span1.vSpanContent.setHeight(span1.vSpanContent.getHeightFloat() + delta, delta * span1.vSpanAlign, span1.vSpanContent.hContent + delta, span1.vSpanContent.hEnvelope + delta);
            }
        }
        spans.clear();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    protected void doCellBorders() {
        // Wenn wir über der Zeile eine halbe Border haben, müssen wir nun die Zeile nach unterschieben.
        // Das passiert nur, wenn dieses die erste Zeile auf einer neuen Seite ist.
        if (maxTopBorderSpace > 0.0001f) {
            for (int i = destRowStart; i < dest.size(); i++) {
                ((GraphElContent) dest.get(i)).y += maxTopBorderSpace;
                ((GraphElContent) dest.get(i)).yContent += maxTopBorderSpace;
                ((GraphElContent) dest.get(i)).yEnvelope += maxTopBorderSpace;
            }
            maxTopBorderSpace = 0;
        }
        //System.out.print('.');
        boolean spanEnd = spans.size() > 0 && spans.get(0).vSpanCount == 0;
        if (spanEnd) {
            completeSpans();
        }
        for (int i1 = 0; i1 < tableBorderReq.length; i1++) {
            TableBorderRequirements b = tableBorderReq[i1];
            float width = colWidths[i1];
            if (b.grel == null)
                continue;
            CellDef cd = b.cd;
            if (cd == null) {
                b.grel.setHeight(lineHeight, 0, 0, 0);
            } else {
                CellStyle cs = cd.getCascadedStyle();
                float remainder = lineHeight - b.hContent - b.hDiff;
                b.grel.setHeight(lineHeight, remainder * cs.getAlignmentVertical().getAlign(), Math.min(b.hContent, lineHeight - b.hDiff), lineHeight - b.hBorders);
                dest.add(b.grel);
            }
            JoriaSimpleBorder ct = b.topBorder;
            JoriaSimpleBorder cl = b.leftBorder;
            JoriaSimpleBorder cr = b.rightBorder;
            JoriaSimpleBorder cb = b.bottomBorder;
            float x0 = b.grel.x + (b.isLeftOutsideBorder ? cl.getThickness() / 2 : 0);
            float x1 = b.grel.x + width - (b.isRightOutsideBorder ? cr.getThickness() / 2 : 0);
            float y0 = b.grel.y + ct.getThickness() / 2;//(b.isTopOutsideBorder ? ct.getThickness() / 2 : 0);
            float y1 = b.grel.y + lineHeight - (b.isBottomOutsideBorder ? cb.getThickness() / 2 : 0);
            if (!JoriaSimpleBorder.isNull(ct) && b.paintTopBorder) {
                float yp = y0;
                GraphElLine tl = new GraphElLine(x0, yp, x1, yp, ct.getThickness(), ct.getLineStyle(), ct.getColor());
                lDestBorderLines.add(tl);
            }
            if (!JoriaSimpleBorder.isNull(cl) && b.paintLeftBorder) {
                float xp = x0;
                GraphElLine ll = new GraphElLine(xp, y0, xp, y1, cl.getThickness(), cl.getLineStyle(), cl.getColor());
                lDestBorderLines.add(ll);
            }
            if (!JoriaSimpleBorder.isNull(cr)) {
                float xp = x1;
                GraphElLine rl = new GraphElLine(xp, y0, xp, y1, cr.getThickness(), cr.getLineStyle(), cr.getColor());
                lDestBorderLines.add(rl);
            }
            if (!JoriaSimpleBorder.isNull(cb)) {
                float yp = y1;
                GraphElLine bl = new GraphElLine(x0, yp, x1, yp, cb.getThickness(), cb.getLineStyle(), cb.getColor());
                lDestBorderLines.add(bl);
            }
        }
    }

    protected void doTableBorderAtPageBreak() {
        if (spaceForTableBorderAtPageBottom <= 0)
            return;
        lDestBorderLines.truncate(lDestLinesRowStart);
        for (int i1 = 0; i1 < lastTableBorderReq.length; i1++) {
            TableBorderRequirements b = lastTableBorderReq[i1];
            float width = colWidths[i1];
            if (b.grel == null)
                continue;
            JoriaSimpleBorder cb = b.bottomBorderForTableAtPageBreak;
            if (cb == null)
                return;
            float x0 = b.grel.x + (b.isLeftOutsideBorder ? b.leftBorderForTableAtPageBreak.getThickness() / 2 : 0);
            float x1 = b.grel.x + width - (b.isRightOutsideBorder ? b.rightBorderForTableAtPageBreak.getThickness() / 2 : 0);
            final float y0 = b.grel.y + lineHeight;
            float y1 = y0 + cb.getThickness() / 2;// - (b.isBottomOutsideBorder ? cb.getThickness() / 2 : 0);
            if (!JoriaSimpleBorder.isNull(b.leftBorderForTableAtPageBreak) && b.paintLeftBorder) {
                final GraphElLine ll = new GraphElLine(x0, y0, x0, y1, b.leftBorderForTableAtPageBreak.getThickness(), b.leftBorderForTableAtPageBreak.getLineStyle(), b.leftBorderForTableAtPageBreak.getColor());
                lDestBorderLines.add(ll);
            }
            if (!JoriaSimpleBorder.isNull(b.rightBorderForTableAtPageBreak)) {
                final GraphElLine rl = new GraphElLine(x1, y0, x1, y1, b.rightBorderForTableAtPageBreak.getThickness(), b.rightBorderForTableAtPageBreak.getLineStyle(), b.rightBorderForTableAtPageBreak.getColor());
                lDestBorderLines.add(rl);
            }
            GraphElLine bl = new GraphElLine(x0, y1, x1, y1, cb.getThickness(), cb.getLineStyle(), cb.getColor());
            lDestBorderLines.add(bl);
        }
        lDestLinesRowStart = lDestBorderLines.size();
    }

    private void completeSpans() {
        // find spanned cols that must be stretched
        for (Iterator<TableBorderRequirements> it = spans.iterator(); it.hasNext(); ) {
            TableBorderRequirements t = it.next();
            if (t.vSpanCount > 0)
                break;
            float delta = lineHeight + verticalPos - t.vSpanStart - t.vSpanContent.getHeightFloat();
            if (delta > 0) {
                t.vSpanContent.setHeight(t.vSpanContent.getHeightFloat() + delta, delta * t.vSpanAlign, t.vSpanContent.hContent + delta, t.vSpanContent.hEnvelope + delta);
            }
            t.vSpanContent = null;
            if (!t.vSpanBreakble)
                releaseSavePoint();
            it.remove();
        }
    }

    public void addSpan(int cols) {
        colInCurrRow += cols;
    }

    public PageEnv2 getPageRun() {
        return pageRun;
    }

    public void steppedRepeater(Repeater fromRepeater) {
        for (Iterator<TableBorderRequirements> it = spans.iterator(); it.hasNext(); ) {
            TableBorderRequirements tbr = it.next();
            if (tbr.cd != null && tbr.cd.getRepeater() == fromRepeater) {
                it.remove();
            }
        }
    }

    public String getPageNumber() {
        return Integer.toString(pageRun.getDisplayPageNo() + 1);
    }

    public String getTotalPageNumberPlaceHolder() {
        return "?";
    }

    public void startRepeater(Repeater r) {
        //TODO reset group totals for this repeater only
        pageRun.resetTotals(AggregateDef.group);
    }

    public RunEnvImpl getRunEnv() {
        return pageRun.getRunEnv();
    }

    public float getYPos() {
        return verticalPos;
    }

    public float getXPos() {
        return colPos[colInCurrRow];
    }

    public void makeSavePoint() {
        keeps++;
        Trace.logDebug(Trace.run, "Making output save point. destBreak: " + destBreak + " keeps: " + keeps + " at: " + destRowStart);
        multiColFrameSupport.startBlock(false);
        if (destBreak < 0) {
            destBreak = destRowStart;
            lDestLinesBreak = lDestLinesRowStart;
            fill.makeSavePoint();
            saveSplitters = splitters;
        }
    }

    public void releaseSavePoint() {
        Trace.logDebug(Trace.run, "Savepoint released now: " + (keeps - 1));
        keeps--;
        if (keeps == 0) {
            destBreak = -1;
            lDestLinesBreak = -1;
            fill.removeSavePoints();
            splitters = saveSplitters;
        } else if (keeps < 0)
            System.out.println("oops");
    }

    public void checkSavePoints(boolean resetRepeater) {
        if (destBreak >= 0 && destBreak != pageRun.getDestBodyStart()) {
            Trace.logDebug(Trace.run, "Cannot break here: backing up to save point.");
            //lDest.truncate(lDestBreak);
            lDestBorderLines.truncate(lDestLinesBreak);
            dest.truncate(destBreak);
            splitters = saveSplitters;
            if (keeps > 0 && resetRepeater) {
                fill.restoreSavePoint();
            }
        }
    }

    public boolean isFirstBodyFrame() {
        return firstBodyOnPage;
    }

    public AggregateCollector getAggregateCollector() {
        return getPageRun();
    }

    public boolean isPageOutput() {
        return true;
    }

    TableBorderRequirements getTableBorderReqCol() {
        return tableBorderReq[colInCurrRow];
    }

    void scaleBackgroundImage(float x0, float x1, float y0, float y1) {
        if (backgroundImageElement != null) {
            FrameStyle fs = template.getFrame().getCascadedFrameStyle();
            SizeLimit limit = fs.getSizeLimit();

            final boolean mustScaleDown = limit == SizeLimit.AtMost && backgroundImageWidth > x1 - x0;
            final boolean mustScaleUp = limit == SizeLimit.AtLeast && backgroundImageWidth < x1 - x0;
            final boolean mustScale = limit == SizeLimit.Fix && backgroundImageWidth != x1 - x0;
            if (x1 - x0 != 0 && (mustScaleDown || mustScaleUp || mustScale)) {
                float scale = (x1 - x0) / backgroundImageWidth;
                backgroundImageElement.setScale(scale);
            }
            if (fs.getBackgroundImageTargetWidth() != null && !fs.getBackgroundImageTargetWidth().isExpandable()) {
                float scale = (x1 - x0) / fs.getBackgroundImageTargetWidth().getVal();
                backgroundImageElement.setScale(scale);
            }

            backgroundImageElement.x = x0;
            backgroundImageElement.y = y0;
            backgroundImageElement.width = x1 - x0;
            backgroundImageElement.height = y1 - y0;
        }

    }

    protected void frameBorders() {
        FrameStyle frameStyle = template.getFrame().getCascadedFrameStyle();
        JoriaFrameBorder t = frameStyle.getTopBorder();
        JoriaFrameBorder l = frameStyle.getLeftBorder();
        JoriaFrameBorder b = frameStyle.getBottomBorder();
        JoriaFrameBorder r = frameStyle.getRightBorder();
        PageStyle ps = getRunEnv().getTemplate().getPage().getCascadedPageStyle();
        float xOri;
        float x = colPos[0] - l.getInset();
        if (frameStyle.getXPos() != null && frameStyle.getXPos().getUnit() != FlexSize.flex) {
            xOri = frameStyle.getXPos().getVal();
        } else {
            xOri = ps.getLeftMargin().getValInPoints();
			/*
			   if (frameStyle.getWidth().isExpandable() && frameStyle.getWidth().getVal() == 0)
				   w = colPos[colPos.length - 1] - xOri + r.getTotalInset();
			   else
				   w = ps.getBodyWidth();
   */
        }
        float w;
        if (frameStyle.getWidth().isExpandable() && frameStyle.getWidth().getVal() == 0) {
            w = colPos[colPos.length - 1] - xOri;
            w = frameStyle.getColumnWidth(w);
        } else if (!frameStyle.getWidth().isExpandable())
            w = frameStyle.getWidth().getVal();
        else {
            w = ps.getPageWidth() - ps.getRightMargin().getValInPoints() - xOri;
            w = frameStyle.getColumnWidth(w);
        }
        //float x01 = xOri + l.getOuterSpacing().getVal();
        @SuppressWarnings("UnnecessaryLocalVariable") float x0 = x;
        float y0 = verticalStart;
        float x1 = x + w - l.getOuterSpacing().getValInPoints() - r.getOuterSpacing().getValInPoints();
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

    public boolean makePictureGraphEl(TableBorderRequirements b, int iter) {
        if (b.value == RVSupressHeader.instance)// a picture as header may be supressed
        {
            colInCurrRow++;
            return false;
        }
        CellStyle cs = b.cd.getCascadedStyle();
        float fixWidth = 0;
        FlexSize flexWidth = ((PictureCellBase) b.cd).getTargetWidth();
        SizeLimit limit = ((PictureCellBase) b.cd).getSizeLimit();
        if (flexWidth != null && !flexWidth.isExpandable())
            fixWidth = flexWidth.getVal();
        if (b.value instanceof RVImageBase) {
            RVImageBase picture = (RVImageBase) b.value;
            Icon icon = picture.getIcon(iter);
            Object information = picture.getInformation(iter);
            double hardScale = picture.getHardScale();
            ImageIcon backgoundImage = cs.getBackgroundImage(getRunEnv().getLocale());
            final boolean mustScaleDown = icon != null && limit == SizeLimit.AtMost && icon.getIconWidth() > fixWidth;
            final boolean mustScaleUp = icon != null && limit == SizeLimit.AtLeast && icon.getIconWidth() < fixWidth;
            final boolean mustScale = icon != null && limit == SizeLimit.Fix && icon.getIconWidth() != fixWidth;
            if (icon == null)
                b.grel = new GraphicElementRect(cs.getBackground(), b.cd, backgoundImage);
            else if (hardScale != 0 && hardScale != 1) {
                b.hContent = (float) (icon.getIconHeight() / hardScale);
                b.wContent = (float) (icon.getIconWidth() / hardScale);
                b.grel = new GraphElPicture(information, icon, cs.getBackground(), (float) (1 / hardScale), b.cd, picture.doSpread(), backgoundImage);
            } else if (fixWidth != 0 && (mustScaleDown || mustScaleUp || mustScale)) {
                final float scale = fixWidth / icon.getIconWidth();
                b.hContent = icon.getIconHeight() * scale;
                b.wContent = fixWidth;
                b.grel = new GraphElPicture(information, icon, cs.getBackground(), scale, b.cd, picture.doSpread(), backgoundImage);
            } else {
                b.hContent = icon.getIconHeight();
                b.wContent = icon.getIconWidth();
                b.grel = new GraphElPicture(information, icon, cs.getBackground(), Float.NaN, b.cd, picture.doSpread(), backgoundImage);
            }
            float contentWidth = spannedWidth(b, colInCurrRow, b.wDiff);// the width that the text has to fit into
            float envelopeWidth = spannedWidth(b, colInCurrRow, b.wBorders);
            updateLineHeight(b);
            if (hasSpace(b)) {
                setGrelParameter(b, contentWidth, cs, envelopeWidth);
            } else {
                if (firstBodyOnPage && effectiveOutRow == 0 && inFrameColumn == 0) // image can never fit. Force scaling to page space
                {
                    if (firstBodyRowOnPage)
                        maxTopBorderSpace = Math.max(maxTopBorderSpace, b.topOutsideBorder);
                    maxBottomBorderSpace = Math.max(maxBottomBorderSpace, b.bottomOutsideBorder);
                    final float scalew = contentWidth / icon.getIconWidth();
                    final float scaleh = (remainingHeight - maxTopBorderSpace - maxBottomBorderSpace) / icon.getIconHeight();
                    final float scale = Math.min(scalew, scaleh);
                    b.hContent = icon.getIconHeight() * scalew;
                    b.wContent = contentWidth;
                    b.grel = new GraphElPicture(information, icon, cs.getBackground(), scale, b.cd, picture.doSpread(), backgoundImage);
                    updateLineHeight(b);
                    setGrelParameter(b, contentWidth, cs, envelopeWidth);
                } else {
                    lastRowState = LastRowState.doneRedo;
                    return true;
                }
            }
        } else if (b.value != null) {
            Class<?> c = b.value.getClass();
            throw new JoriaAssertionError("Unknown Image value class " + c);
        }
        colInCurrRow++;
        return false;
    }

    public boolean makeGraphElFromRenderingCell(TableBorderRequirements b, int iter) {
        if (b.value == RVSupressHeader.instance || b.value == null)// a picture as header may be supressed
        {
            colInCurrRow++;
            return false;
        }
        float contentWidth = spannedWidth(b, colInCurrRow, b.wDiff);// the width that the output has to fit into
        float envelopeWidth = spannedWidth(b, colInCurrRow, b.wBorders);
        RenderingCellDef chc = (RenderingCellDef) b.cd;
        b.hContent = chc.getRunHeight(b, iter, remainingHeight, contentWidth);
        updateLineHeight(b);
        if (hasSpace(b)) {
            chc.render(b, this, iter, envelopeWidth);
            setGrelParameter(b, contentWidth, b.cd.getCascadedStyle(), envelopeWidth);
        } else {
            lastRowState = LastRowState.doneRedo;
            return true;
        }
        colInCurrRow++;
        return false;
    }

    public boolean makeStyledTextGraphel(TableBorderRequirements b, StyledParagraphList val) {
        CellStyle cs = b.cd.getCascadedStyle();
        float cw = spannedWidth(b, colInCurrRow, b.wDiff);
        float ew = spannedWidth(b, colInCurrRow, b.wBorders);
        if (!b.cd.getCascadedStyle().getBreakable())// unbreakable
        {
            StyledParagraphLayouterList layouter = new StyledParagraphLayouterList(val, getRunEnv().getGraphics2D().getFontRenderContext(), cw);
            layouter.recalc();
            b.hContent = layouter.getHeight();
            ImageIcon backgroundImage = cs.getBackgroundImage(getRunEnv().getLocale());
            updateLineHeight(b);
            if (hasSpace(b)) {
                b.wContent = cw;
                b.grel = new GraphElStyledText(layouter.getLines(), layouter.getIteratorsPerLine(), 0, cs, b.cd, backgroundImage);
            } else {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
        } else {
            StyledTextParagraph p = StyledTextParagraph.makeParagraph(b.cd, val, cw, getRunEnv().getGraphics2D());// check for line breaks
            float sliceHeight;
            if (b.rowSizing.isExpandable())
                sliceHeight = remainingHeight;
            else
                sliceHeight = b.rowSizing.getVal();
            p.nextSlice(b, sliceHeight, colPos[colInCurrRow], verticalPos, colWidths[colInCurrRow] - b.wDiff, getRunEnv().getGraphics2D(), !b.rowSizing.isExpandable(), this);
            if (b.hContent == 0 && p.more())// first line did not fit
            {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
            updateLineHeight(b);
            if (!hasSpace(b)) {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
            if (p.more() && b.rowSizing.isExpandable()) {
                if (lastRowState != LastRowState.doneRedo)
                    lastRowState = LastRowState.donePartial;
                if (splitters == null)
                    splitters = new Paragraph[dynaCols];
                splitters[colInCurrRow] = p;
                if (b.vSpanCount > 0)
                    p.remainingSpan = b.vSpanCount;
            }
            p.setup(b.contentX, b.contentY, b.contentWidth, b.innerX, b.innerY, b.hDiff, b.wDiff, cw);
        }
        setGrelParameter(b, cw, cs, ew);
        colInCurrRow++;
        return false;
    }

    public boolean makeTextGraphEl(TableBorderRequirements b, String val) {
        CellStyle cs = b.cd.getCascadedStyle();
        float ew = spannedWidth(b, colInCurrRow, b.wBorders);
        float cw = spannedWidth(b, colInCurrRow, b.wDiff);
        TextParagraph p = TextParagraph.makeParagraph(b.cd, val, cw, getRunEnv().getGraphics2D(), getRunEnv().getLocale());// check for line breaks
        if (p == null)// just one line
        {
            b.hContent = cs.getHeight(val, getRunEnv().getGraphics2D());// the actual height of the text
            updateLineHeight(b);
            if (hasSpace(b)) {
                b.wContent = cs.getWidth(val, getRunEnv().getGraphics2D());
                b.grel = new GraphElText(val, cs, b.cd, cs.getBackgroundImage(getRunEnv().getLocale()));
            } else {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
        } else {
            // 1. Was passiert wenn vspan == 3 und die ersten zwei Zellen passen auf diese Seite
            // 2. Splitters die spannen werden nicht richtig mit den neuen vorhandenen Zellen auf der n?chsten Seite
            //     gemischt.
            float sliceHeight;
            if (b.rowSizing.isExpandable())
                sliceHeight = remainingHeight;// TOOO border bottom borders
            else {
                sliceHeight = b.rowSizing.getVal();
            }
            p.nextSlice(b, sliceHeight, colPos[colInCurrRow], verticalPos, colWidths[colInCurrRow] - b.wDiff, getRunEnv().getGraphics2D(), !b.rowSizing.isExpandable(), this);
            if (b.hContent == 0 && p.more())// first line did not fit
            {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
            updateLineHeight(b);
            if (!hasSpace(b)) {
                lastRowState = LastRowState.doneRedo;
                return true;
            }
            if (p.more() && b.rowSizing.isExpandable()) {
                if (lastRowState != LastRowState.doneRedo)
                    lastRowState = LastRowState.donePartial;
                if (splitters == null)
                    splitters = new Paragraph[dynaCols];
                splitters[colInCurrRow] = p;
                if (b.vSpanCount > 0)
                    p.remainingSpan = b.vSpanCount;
            }
            p.setup(b.contentX, b.contentY, b.contentWidth, b.innerX, b.innerY, b.hDiff, b.wDiff, cw);
        }
        setGrelParameter(b, cw, cs, ew);
        colInCurrRow++;
        return false;
    }

    public void setGrelParameter(TableBorderRequirements b, float cw, CellStyle cs, float ew) {
        if (b.vSpanCount > 0 && b.vSpanContent == null)
            b.vSpanContent = b.grel;
        if (b.grel == null)
            return;
        b.grel.x = getXPos();
        b.grel.y = getYPos();
        b.grel.width = cw + b.wDiff;
        b.grel.setContentX(b.grel.x + b.innerX, cw, b.wContent, cs, getRunEnv().getGraphics2D(), b.grel.x + b.leftBorderInset);
        b.grel.wEnvelope = ew;
        b.grel.yContent += b.grel.y + b.innerY;
        b.grel.yEnvelope += b.grel.y + b.topBorderInset;
        b.grel.drilldownObject = b.drillDownObject;
    }

    public void updateLineHeight(TableBorderRequirements b) {
        if (firstBodyRowOnPage)
            maxTopBorderSpace = Math.max(maxTopBorderSpace, b.topOutsideBorder);
        maxBottomBorderSpace = Math.max(maxBottomBorderSpace, b.bottomOutsideBorder);
        if (b.rowSizing.isExpandable())// TODO warum gibt es die beiden ifs?
        {
			/*
						   only increase lineHeight if cell has no vertical span.
						   */
            if (b.cd.getCascadedStyle().getSpanVertical() < 2) {
                lineHeight = Math.max(lineHeight, Math.max(b.hContent + b.hDiff, b.hBackground));
            }
            outRowSize.set(FlexSize.flex, b.rowSizing.getVal());
        } else if (outRowSize.isExpandable()) {
			/*
						   only increase lineHeight if cell has no vertical span.
						   */
            if (b.cd.getCascadedStyle().getSpanVertical() < 2) {
                lineHeight = Math.max(lineHeight, Math.max(b.hContent + b.hDiff, b.hBackground));
            }
            outRowSize.setVal(Math.max(outRowSize.getVal(), b.rowSizing.getVal()));
        } else if (b.rowSizing.getVal() > outRowSize.getVal())// hier wurde der Inset mit gerechnet
        {
            lineHeight = b.rowSizing.getVal();// hier auch.
            outRowSize.setVal(lineHeight);
        }
    }

    public boolean hasSpace(TableBorderRequirements b) {
        return hasSpace(b, 0);
    }

    protected boolean hasSpace(TableBorderRequirements b, int remainingSpan) {
        float cellHeight = lineHeight;
        CellStyle cs = b.cd.getCascadedStyle();
        // TODO when borderspace increases, check old cells
        if (cs.getSpanVertical() > 1 || remainingSpan > 0) {
            cellHeight = b.hContent + b.hDiff;
            if (cellHeight > remainingHeight + maxBottomBorderSpace + maxTopBorderSpace)// no space for vertically spanned cell, do not create span info
                return false;
            if (remainingSpan == 0)
                remainingSpan = cs.getSpanVertical() - 1;
            addToSpans(remainingSpan, b, cellHeight);
            if (cs.getSpanVertical() > 1 && !cs.getBreakable())
                makeSavePoint();
        }
        @SuppressWarnings("UnnecessaryLocalVariable") boolean ret = cellHeight <= remainingHeight + maxBottomBorderSpace + maxTopBorderSpace;
        return ret;
    }

    private void addToSpans(int remainingSpan, TableBorderRequirements b, float cellHeight) {
        b.vSpanCount = remainingSpan;
        b.vSpanStart = verticalPos;
        b.vSpanHeight = cellHeight;
        if (spans.contains(b))
            return;
        boolean atEnd = true;
        for (int i = 0; i < spans.size(); i++) {
            TableBorderRequirements t = spans.get(i);
            if (t == b)// already added -> ignore
            {
                atEnd = false;
                break;
            }
            if (t.vSpanCount >= b.vSpanCount) {
                spans.add(i, b);
                atEnd = false;
                break;
            }
        }
        if (atEnd)
            spans.add(b);
    }

    public boolean makeEmptyGrel(TableBorderRequirements b) {
        b.hContent = 0;
        b.wContent = 0;
        if (b.rowSizing.isExpandable()) {
            lineHeight = Math.max(lineHeight, b.hBackground);
        } else if (outRowSize.isExpandable()) {
			/*
						  only increase lineHeight if cell has no vertical span.
						*/
            if (b.cd == null || b.cd.getCascadedStyle().getSpanVertical() < 2) {
                lineHeight = Math.max(lineHeight, b.hBackground);
            }
            outRowSize.setVal(Math.max(outRowSize.getVal(), b.rowSizing.getVal()));
        } else if (b.rowSizing.getVal() > outRowSize.getVal()) {
            lineHeight = b.rowSizing.getVal();
            outRowSize.setVal(lineHeight);
        }
        CellStyle cs = null;
        Color background;
        ImageIcon backgroundImage = null;
        if (b.cd != null) {
            cs = b.cd.getCascadedStyle();
            background = cs.getBackground();
            backgroundImage = cs.getBackgroundImage(getRunEnv().getLocale());
        } else
            background = template.getFrame().getCascadedCellStyle().getBackground();
        float cw = spannedWidth(b, colInCurrRow, b.wDiff);// the width that the text has to fit into
        float ew = spannedWidth(b, colInCurrRow, b.wBorders);// the width that the text has to fit into
        b.grel = new GraphicElementRect(background, b.cd, backgroundImage);
        setGrelParameter(b, cw, cs, ew);
        colInCurrRow++;
        return false;
    }

    public float spannedWidth(TableBorderRequirements b, int at, float borderUsage) {
        float agg = 0;
        for (int i = at; i < b.maxCol; i++) {
            agg += colWidths[i];
        }
        agg -= borderUsage;
        if (agg < 0)
            agg = 0;
        return agg;// the width that the text has to fit into
    }

    public void resetToBeginning() {
        dest.truncate(destStart);
        reset();
    }

    public void reset() {
        splitters = null;
        spans.clear();
        lastRowState = LastRowState.proceed;
        fill.reset();
    }

    public int getDestStart() {
        return destStart;
    }

    protected float layout() {
        if (template.getRowCount() == 0 || nCols == 0)
            return 0;
        ColumnLayouter layouter = new ColumnLayouter(colWidths, maxColWidths, colPos, colReq, dynaCols, template, getRunEnv().getTemplate().getPage().getCascadedPageStyle());
        return layouter.layout();
    }

    @SuppressWarnings("UnusedDeclaration")
    public float getRemainingHeight() {
        return remainingHeight;
    }

    public boolean makeTextRtfGraphEl(TableBorderRequirements b, String s) {
        CellStyle cs = b.cd.getCascadedStyle();
        float ew = spannedWidth(b, colInCurrRow, b.wBorders);
        float cw = spannedWidth(b, colInCurrRow, b.wDiff);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(b.cd.getCascadedStyle().getBackground());
        tc.setFont(b.cd.getCascadedStyle().getStyledFont());
        View hv1 = GraphElRtfText.buildView(s, tc);
        float wantW = hv1.getPreferredSpan(View.X_AXIS);
        hv1.setSize(cw, Float.MAX_VALUE);
        b.hContent = hv1.getPreferredSpan(View.Y_AXIS);
        updateLineHeight(b);
        if (hasSpace(b)) {
            b.wContent = hv1.getPreferredSpan(View.X_AXIS);
            if (b.wContent > wantW)
                b.wContent = wantW;
            b.grel = new GraphElRtfText(cs, s, hv1, b.cd, cs.getBackgroundImage(getRunEnv().getLocale()));
        } else {
            lastRowState = LastRowState.doneRedo;
            return true;
        }
        setGrelParameter(b, cw, cs, ew);
        colInCurrRow++;
        return false;
    }

    public boolean makeTextHtmlGraphEl(TableBorderRequirements b, String s) {
        CellStyle cs = b.cd.getCascadedStyle();
        float ew = spannedWidth(b, colInCurrRow, b.wBorders);
        float cw = spannedWidth(b, colInCurrRow, b.wDiff);
        if (s != null && s.startsWith("<!DOCTYPE"))//trdone
            s = s.substring(s.indexOf('>') + 1).trim();
        if (!BasicHTML.isHTMLString(s))
            s = "<html>" + s;//trdone
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(b.cd.getCascadedStyle().getBackground());
        tc.setFont(b.cd.getCascadedStyle().getStyledFont());
        View hv1 = BasicHTML.createHTMLView(tc, s);
        float wantW = hv1.getPreferredSpan(View.X_AXIS);
        hv1.setSize(cw, Float.MAX_VALUE);
        b.hContent = hv1.getPreferredSpan(View.Y_AXIS);
        updateLineHeight(b);
        if (hasSpace(b)) {
            b.wContent = hv1.getPreferredSpan(View.X_AXIS);
            if (b.wContent > wantW)
                b.wContent = wantW;
            b.grel = new GraphElHtmlText(cs, s, hv1, b.cd, cs.getBackgroundImage(getRunEnv().getLocale()));
        } else {
            lastRowState = LastRowState.doneRedo;
            return true;
        }
        setGrelParameter(b, cw, cs, ew);
        colInCurrRow++;
        return false;
    }

    public void addTOCPageno(GraphElContent grel) {
        if (tocEntries == null)
            tocEntries = new ArrayList<GraphElContent>();
        tocEntries.add(grel);
    }

    public void addTotalPages(GrahElPostprocess grel) {
        dest.addTotalPages(grel);
    }

    public boolean makeNull(TableBorderRequirements tblReq) {
        tblReq.hContent = 0;
        colInCurrRow++;
        return false;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setCurrRow(final int newColInCurrRow) {
        colInCurrRow = newColInCurrRow;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLastRowState(final LastRowState rowState) {
        lastRowState = rowState;
    }

    private static class BorderHolder {
        private JoriaSimpleBorder topBorder;
        private JoriaSimpleBorder leftBorder;
        private JoriaSimpleBorder rightBorder;
        private JoriaSimpleBorder bottomBorder;
        private boolean isTopOutsideBorder;
        private boolean isLeftOutsideBorder;
        private boolean isRightOutsideBorder;
        private boolean isBottomOutsideBorder;
        private boolean paintTopBorder;
        private boolean paintLeftBorder;
        private GraphElContent grel;

        private void save(TableBorderRequirements b) {
            topBorder = b.topBorder;
            leftBorder = b.leftBorder;
            rightBorder = b.rightBorder;
            bottomBorder = b.bottomBorder;
            isTopOutsideBorder = b.isTopOutsideBorder;
            isLeftOutsideBorder = b.isLeftOutsideBorder;
            isRightOutsideBorder = b.isRightOutsideBorder;
            isBottomOutsideBorder = b.isBottomOutsideBorder;
            paintTopBorder = b.paintTopBorder;
            paintLeftBorder = b.paintLeftBorder;
            grel = b.grel;
        }

        private void restore(TableBorderRequirements b, float ypos) {
            b.topBorder = topBorder;
            b.leftBorder = leftBorder;
            b.rightBorder = rightBorder;
            b.bottomBorder = bottomBorder;
            b.isTopOutsideBorder = isTopOutsideBorder;
            b.isLeftOutsideBorder = isLeftOutsideBorder;
            b.isRightOutsideBorder = isRightOutsideBorder;
            b.isBottomOutsideBorder = isBottomOutsideBorder;
            b.paintTopBorder = paintTopBorder;
            b.paintLeftBorder = paintLeftBorder;
            if (b.grel == null && grel != null) {
                b.grel = grel.copy();
                grel = null;
                b.grel.y = ypos;
            }
        }
    }

    public void reinit() throws JoriaDataException {
        init();
    }

    /**
     * A table that repeats its bottom border at the page end adds its requirement.
     * The space is subtracted from the available space on the page.
     * Other colums may have set higher values already.
     *
     * @param spaceNeeded height of table bottom border
     */
    public void addSpaceForTableBorderAtPageBottom(float spaceNeeded) {
        remainingHeight += spaceForTableBorderAtPageBottom;// remove old valuue
        spaceForTableBorderAtPageBottom = Math.max(spaceForTableBorderAtPageBottom, spaceNeeded);// change
        remainingHeight -= spaceForTableBorderAtPageBottom;// add new value
    }

    /**
     * must be called when a table with border repetition at page end is done.
     * checks all running repeaters for the highes value.
     */
    public void recalcSpaceForTableBorderAtPageBottom() {
        remainingHeight += spaceForTableBorderAtPageBottom;// remove old valuue
        spaceForTableBorderAtPageBottom = fill.getMaxSpaceForTableBorderAtPageBottom(0.0f);
        remainingHeight -= spaceForTableBorderAtPageBottom;// add new value
    }
}
