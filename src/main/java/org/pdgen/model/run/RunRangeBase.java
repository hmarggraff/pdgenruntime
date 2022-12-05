// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.model.RDBase;
import org.pdgen.model.Repeater;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.run.OutputMode.LastRowState;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.JoriaSimpleBorder;
import org.pdgen.util.IndCharSeq;


public abstract class RunRangeBase {
    protected final int nRow;
    protected final int nCol;
    protected final int startCol;
    protected final int endCol;
    protected final int nestingLevel;
    /**
     * This structure defines the data. There outer array is for the rows,
     * the inner for the columns. The inner array containsName a field for each top level cell
     * and a field for each inner repeater.
     */
    protected final RDRangeBase dataDef;
    protected final RVTemplate values;
    protected final OutputMode out;
    protected final IndCharSeq ind;
    // -----------------
    protected int startRow;
    protected int inRow;
    protected int inDataCol;
    protected int inHeaderRow = 1;// +1 indicates that there are no header rows to build
    protected TemplateModel template;
    protected int[] listerCounts;// len: number of template rows
    protected RunRepeater[] listers;// len: number of repeaters in template
    /**
     * indicates the line where the header region of a repeater starts. If we come across one of those we have to
     * set a savepoint in out, that we can use to backtrack if the first line of the table or the whole table wont fit onto
     * the page.
     * a value of -1 indicates that there were no breakpoints (yet)
     */
    protected int saveInRow = -1;
    protected boolean lastRowWasRepeated;
    protected boolean firstRepeat;

    protected RunRangeBase(int startCol, int endCol, int nRow, TemplateModel template, OutputMode out, int nestingLevel, RDRangeBase dataDef, RVTemplate values) {
        this.startCol = startCol;
        this.endCol = endCol;
        this.template = template;
        this.out = out;
        this.nestingLevel = nestingLevel;
        this.dataDef = dataDef;
        this.values = values;
        this.nRow = nRow;
        nCol = endCol - startCol + 1;
        firstRepeat = true;
        ind = new IndCharSeq(nestingLevel * 2);
    }

    public abstract int getIteration();

    protected abstract RunRepeater makeLister(int row, int col) throws JoriaDataException;

    public abstract void pageBreak(OutputMode.LastRowState endState);

    protected void saveIteration() {
    }

    protected void restoreIteration() {
    }

    public boolean row() throws JoriaDataException {
        if (inRow >= nRow)
            throw new JoriaAssertionError("RunRange.row called for row " + inRow + " which must be less than the number of rows which is " + nRow);
        //Trace.logDebug(4, Trace.fill, getParameterString("row"));
        //if (listerCounts != null && inRow >= 0 && listerCounts[inRow] > 0 && repeating)// next input row or repeat a subform ?
        if (listerCounts != null && inRow >= 0 && listerCounts[inRow] > 0 && !onFirstIteration() && !doRowAnyway())// next input row or repeat a subform ?
        {
            lastRowWasRepeated = true;
            return repeatedRow();
        } else {
            try {
                doRowAnyway();
                onFirstIteration();
                final boolean ret = singletonRow();
                lastRowWasRepeated = false;
                return ret;
            } catch (RunBreakException e) {
                throw e;
            } catch (Throwable ex) {
                Trace.log(ex);
                throw new RunBreakException(ex);
            }
        }
    }

    protected boolean doRowAnyway() {
        // to be overridden
        return false;
    }

    protected boolean onFirstIteration() {
        // to be overridden
        return false;
    }

    /*
     * processes one line at the top level. I.e. there are no inner Table RunRanges active.
     */
    protected boolean singletonRow() throws JoriaDataException {
        RDBase[] rowDef = dataDef.getFields()[inRow];
        inDataCol = 0;
        int inTemplateCol = 0;
        for (int i = 0; i < rowDef.length; i++) {
            RDBase rdBase = rowDef[i];
            if (rdBase instanceof RDRepeater) {
                Repeater tate = (Repeater) rdBase.getModel();
                RunRangeBase lister = listers[tate.getTopLevelIndex()];
                if (lister == null)
                    makeLister(inRow, i);
            } else if (rdBase instanceof RDRepeaterNext) {
                RDRepeaterNext next = (RDRepeaterNext) rdBase;
                RDRepeater rdRep = next.getRDRepeater();
                Repeater tate = (Repeater) rdRep.getModel();
                RunRangeBase lister = listers[tate.getTopLevelIndex()];
                if (lister == null)
                    makeLister(tate.getStartRow(), i);
            }
        }
        while (inDataCol < rowDef.length) {
            RDBase d = rowDef[inDataCol];
            CellDef coverCell = template.getCoverCell(inRow + getRowOffset(), inDataCol + getColOffset());
            if (d == null || coverCell != null) {
                addNullCell(inRow, inTemplateCol);
                inDataCol++;
                inTemplateCol++;
            } else if (d instanceof CellDef) {
                CellDef cd = (CellDef) d;
                if (out.generateOutput(this, cd, inRow, inTemplateCol))
                    return true;
                // no overflow: next col
                inDataCol++;
                inTemplateCol++;
                Trace.logDebug(Trace.fill, ind + "singletonLine col " + inDataCol);
            } else if (d instanceof RDRange)// nested frame
            {
                RVTemplate subVals = (RVTemplate) values.get(inRow, inTemplateCol);
                if (subVals != null) {
                    final RDRange defs = (RDRange) d;
                    out.fillInner(this, defs, subVals, inRow, inTemplateCol);
                } else
                    out.addNullCell(this, inRow, inTemplateCol);
                inDataCol++;
                inTemplateCol++;
            } else
            // RDRepeater or RDRepeaterNext
            {
                Repeater tate = (Repeater) d.getModel();
                RunRangeBase lister = listers[tate.getTopLevelIndex()];
                Trace.logDebug(Trace.fill, ind + "singletonRow Lister at " + inRow + "," + inDataCol);
                if (lister == null)// starting lister
                {
                    // create iterationState
                    if (d instanceof RDRepeater)// only make lister for a RDRepeater, because a RDRepeaterNext indicates the initial RDRepeater lister was empty. So leave lister at null to skip it
                    {
                        lister = makeLister(inRow, inDataCol);
                        if (lister == null)// is there anything in the collection ?
                        {
                            out.checkSavePoints(false);
                            //Trace.logWarn("Empty Table supressed");
                            // the code below exists to avoid errors when a null lister is being built as result of a supressed table
                            // If this happens null cells are generated
                            if (tate.getStartCol() == 0 && tate.getEndCol() == nCol - 1) {
                                Trace.logDebug(Trace.fill, ind + "Singleton Row empty row after col " + inDataCol);
                                return false;// empty row
                            } else {
                                for (int sfCol = tate.getStartCol(); sfCol <= tate.getEndCol(); sfCol++) {
                                    addNullCell(inRow, inTemplateCol);
                                    inTemplateCol++;
                                }
                                inDataCol++;
                                continue;
                            }
                        }
                    } else {
                        inDataCol++;
                        continue;// sure? Can an rdrepeater next be skipped like this
                    }
                }
                boolean pageBreak = lister.row();
                if (pageBreak) {
                    return true;
                }
                //out.releaseRepeaterHeaderSavePoint(tate);
                if (this instanceof RunRepeater)
                    inTemplateCol++;
                else
                    inTemplateCol += tate.getEndCol() - tate.getStartCol() + 1;
                inDataCol++;
            }
        }// for all columns
        // if we can complete all columns then our part of the line fits on page
        return false;// no page break
    }

	/*
	protected int calcNextCol(CellDef cd, int inCol) throws JoriaDataException
	{
		CellStyle bs = cd.getCascadedStyle();
		if (bs.getSpanHorizontal() > 1)
		{
			int span = bs.getSpanHorizontal();
			if (span > 1)
			{
				out.addSpan(span - 1);
				inCol += span;
			}
			else
			{
				inCol++;
			}
		}
		else
			inCol++;
		return inCol;
	}
	*/

    protected boolean addNullCell(int row, int col) throws JoriaDataException {
        return out.addNullCell(this, row, col);
    }

    protected boolean addRepeatedCells(int startCol, int endCol) throws JoriaDataException {
        for (int i = startCol; i < endCol; i++) {
            CellDef cd = template.getCellAt(inRow + getRowOffset(), i + getColOffset());
            if (cd != null) {
                if (out.generateOutputRepeatedCell(this, cd, inRow, i, firstRepeat, !hasMoreRows()))
                    return true;
            } else {
                if (out.addNullCell(this, inRow, i))
                    return true;
            }
            inDataCol++;
        }
        return false;
    }

    /*
     * add the cells of a repeated row from the active repeaters
     * returns whether a page break occured
     */
    protected boolean repeatedRow() throws JoriaDataException {
        Trace.logDebug(Trace.fill, ind + "repeating row " + inRow);
        int inCol = 0;
        inDataCol = 0;
        for (RunRangeBase activeLister : listers) {
            if (activeLister == null)
                continue;
            // hier verlängern.
            if (addRepeatedCells(inCol, activeLister.startCol - startCol))
                return true;
            if (activeLister.row()) {
                return true;// page break occured: skip remaining cells
            }
            inDataCol++;
            inCol = activeLister.endCol + 1 - startCol;
        }// cols on top level outside of range fill with nulls
        if (addRepeatedCells(inCol, endCol - startCol + 1))
            return true;
        firstRepeat = false;
        return false;// no page break
    }

    /*
     * Advances to next row or return false if it can not advance
     */
    public LastRowState advance() throws JoriaDataException {
        Trace.logDebug(Trace.fill, ind + "advance from " + inRow);
        //releaseRepeaterHeaderSavePoint();
        if (inRow < nRow && listers != null)// advance active inner listers
        {
            for (int asf = 0; asf < listers.length; asf++) {
                RunRangeBase lister = listers[asf];
                if (lister != null) {
                    LastRowState advanceResult = lister.advance();
                    if (advanceResult == OutputMode.LastRowState.endOfData) {
                        Trace.logDebug(Trace.fill, ind + " removing repeater " + asf);
                        listerCounts[lister.getRepeater().getEndRow() - listerCountIndexOffset()]--;
                        lister.resetRunningTotals();
                        listers[asf] = null;
                    }
                }
            }
        }
        if (listers != null && listerCounts[inRow] > 0)// do not advance outer
        {
            Trace.logDebug(Trace.fill, ind + " advanced inner repeater ");
            return OutputMode.LastRowState.proceed;
        }
        if (inRow < nRow - 1 - getFooterRows()) {
            inRow++;
            Trace.logDebug(Trace.fill, ind + " advanced to row " + inRow);
            return OutputMode.LastRowState.proceed;
        } else {
            LastRowState proceed = lookForNext();
            if (proceed == OutputMode.LastRowState.endOfData && getFooterRows() > 0) {
                inRow++;
                if (inRow < nRow)
                    return OutputMode.LastRowState.proceed;
            }
            return proceed;
        }
    }

    public void resetRunningTotals() {
        //base has nothing to do
    }

    protected int getFooterRows() {
        return 0;
    }

    protected void releaseRepeaterHeaderSavePoint() {
    }

    protected LastRowState lookForNext() {
        return LastRowState.endOfData;
    }

    protected int listerCountIndexOffset() {
        return 0;
    }

    public void reset() {
        inRow = 0;
        if (listers != null) {
            for (RunRangeBase lister : listers) {
                if (lister != null)
                    lister.reset();
            }
        }
        firstRepeat = true;
    }

    public void startBodyAt(int startRow) {
        if (listers != null) {
            for (RunRangeBase lister : listers) {
                if (lister != null)
                    lister.startBodyAt(startRow);
            }
        }
    }

    public boolean headerRow(int inHeaderRow) throws JoriaDataException {
        boolean hadHeader = false;
        inDataCol = 0;
        if (listers != null && listerCounts[inRow] > 0)// do inner headers
        {
            int tc = 0;
            for (RunRangeBase activeLister : listers) {
                if (activeLister == null)
                    continue;
                if (inHeaderRow >= activeLister.getRepeater().getHeaderRows())
                    continue;
                for (; tc < activeLister.getRepeater().getStartCol(); tc++, inDataCol++) {
                    if (addNullCell(inHeaderRow, tc))
                        throw new JoriaDataException("Header doesn't fit on page. This should not be allowed");
                }
                int atHeaderRow = inHeaderRow - activeLister.getRepeater().getHeaderRows() + activeLister.getRepeater().getStartRow();
                while (tc <= activeLister.getRepeater().getEndCol()) {
                    CellDef cd = template.cellAt(atHeaderRow, tc);
                    if (cd != null) {
                        if (out.generateOutput(this, cd, atHeaderRow, tc))
                            throw new JoriaDataException("Header doesn't fit on page. This should not be allowed");
                        tc++;
                        inDataCol++;
                    } else {
                        if (addNullCell(inRow, tc))
                            throw new JoriaDataException("Header doesn't fit on page. This should not be allowed");
                        tc++;
                        inDataCol++;
                    }
                }
                for (; tc <= endCol; tc++, inDataCol++) {
                    if (addNullCell(inHeaderRow, tc))
                        throw new JoriaDataException("Header doesn't fit on page. This should not be allowed");
                }
                hadHeader = true;
            }
        }
        return hadHeader;
    }

    protected void makeSavePoint() {
        if (saveInRow < 0)// only keep first savepoint
        {
            saveInRow = inRow;
            saveIteration();
            if (listers != null && listerCounts[inRow] > 0)// do inner headers
            {
                for (RunRangeBase activeLister : listers) {
                    if (activeLister == null)
                        continue;
                    activeLister.makeSavePoint();
                }
            }
        }
    }

    protected boolean restoreSavePoint() {
        Trace.logDebug(Trace.run, "Restore save point to line: " + saveInRow);
        if (saveInRow >= 0) {
            inRow = saveInRow;
            restoreIteration();
        } else {
            // restore before start
            return false;
        }
        if (listers != null)// do inner headers
        {
            for (int asf = 0; asf < listers.length; asf++)// i.e. all columns
            {
                RunRangeBase lister = listers[asf];
                if (lister == null)
                    continue;
                if (lister.getRepeater().getCascadedTableStyle().getBreakable()) {
                    if (!lister.restoreSavePoint())// lister reset to initial state -> remove it
                    {
                        listers[asf] = null;
                        listerCounts[lister.getRepeater().getEndRow() - listerCountIndexOffset()]--;
                    }
                } else {
                    listerCounts[lister.getRepeater().getEndRow() - listerCountIndexOffset()]--;
                    listers[asf] = null;
                }
            }
        }
        return true;
    }

    public void removeSavePoints() {
        saveInRow = -1;
        if (listers != null && listerCounts[inRow] > 0)// do inner headers
        {
            for (RunRangeBase activeLister : listers) {
                if (activeLister == null)
                    continue;
                activeLister.removeSavePoints();
            }
        }
    }

    public boolean generateOutput(CellDef cd, int row, int c, boolean firstRowOnPage) throws JoriaDataException {
        FillPagedFrame fpf = (FillPagedFrame) out;
        TableBorderRequirements b = fpf.getTableBorderReqCol();
        CellStyle cs = cd.getCascadedStyle();
        b.buildTableBorderRequirement(row, c, cs, cd, null, null, null, null, firstRowOnPage, false, this);
        b.maxCol = c + cs.getSpanHorizontal();
        if (cd.makeGraphicElement(b, 0, fpf))
            return true;
        saveData(b, row, c);
        return false;
    }

    protected void saveData(TableBorderRequirements tbr, int row, int col) {
        // to be overridden
    }


    protected boolean isLastCol(int c) {
        return template.isLastCol(c + getColOffset());
    }

    protected boolean isNotFirstRow() {
        return false;
    }

    protected boolean isLastRow() {
        return true;
    }

    protected boolean hasMoreRows() {
        if (listers == null)
            return false;
        for (RunRangeBase lister : listers) {
            if (lister == null)
                continue;
            if (lister.hasMoreRows())
                return true;
        }
        return false;
    }

    protected int getRowOffset() {
        return 0;
    }

    protected int getColOffset() {
        return 0;
    }

    protected int getColOffsetData() {
        return getColOffset();
    }

    public boolean generateOutputRepeatedCell(CellDef cd, int row, int col, boolean firstRepeat, boolean lastRepeat, Repeater repeater, boolean firstRowOnPage) throws JoriaDataException {
        FillPagedFrame fpf = (FillPagedFrame) out;
        CellStyle cs = cd.getCascadedStyle();
        CellStyle.SpanToInner howTo = cs.getSpanToInner();
        TableBorderRequirements b = fpf.getTableBorderReqCol();
        if (b.vSpanContent != null)
            b.vSpanCount++;
        JoriaSimpleBorder ct = null;
        if (howTo == CellStyle.SpanToInner.borderFirstCell || howTo == CellStyle.SpanToInner.spanAllCells || howTo == CellStyle.SpanToInner.borderFirstCellAndSpanAllCells && !firstRepeat)// keine Horiazontalen Border wenn
            ct = JoriaSimpleBorder.NULL;
        else if (isNotFirstRow() && row + getRowOffset() == repeater.getStartRow())
            ct = repeater.getCascadedTableStyle().getGroupSeperatorBorder();
        JoriaSimpleBorder cl = null;
        if (howTo == CellStyle.SpanToInner.borderFirstCell && !(col == startCol && col != 0))
            cl = JoriaSimpleBorder.NULL;
        JoriaSimpleBorder cr = null;
        if (howTo == CellStyle.SpanToInner.borderFirstCell && !(col == endCol && col == template.getColCount() - 1))
            cr = JoriaSimpleBorder.NULL;
        JoriaSimpleBorder cb = null;
        if (hasMoreRows() && (howTo == CellStyle.SpanToInner.borderFirstCell || howTo == CellStyle.SpanToInner.spanAllCells || howTo == CellStyle.SpanToInner.borderFirstCellAndSpanAllCells))// keine Horiazontalen Border wenn
            cb = JoriaSimpleBorder.NULL;
        else if (!isLastRow() && row + getRowOffset() + cs.getSpanVertical() - 1 == repeater.getEndRow() && !hasMoreRows())
            cb = repeater.getCascadedTableStyle().getGroupSeperatorBorder();
        final boolean showBordersAtPageBreak = repeater != null && repeater.getCascadedTableStyle().getShowBorderAtPageBreak();
        b.buildTableBorderrequirementWithoutData(row, col, cs, cd, ct, cl, cr, cb, firstRowOnPage, showBordersAtPageBreak, this);
		/*
		  if(b.vSpanContent != null)
			  b.cd = null;
  */
        b.maxCol = out.getOutCol() + cs.getSpanHorizontal();
		/* TODO
				if(savedTBRs != null && cs.getSpanToInner() == CellStyle.SpanToInner.borderFirstCellAndSpanAllCells)
				{
					savedTBRs[col] = b;
				}
				*/
        return fpf.makeEmptyGrel(b);
    }

    public boolean generateOutputNullCell(int templateRow, int templateCol, boolean firstRowOnFrame) {
        FillPagedFrame fpf = (FillPagedFrame) out;
        TableBorderRequirements b = fpf.getTableBorderReqCol();
        b.buildTableBorderRequirement(templateRow, templateCol, null, null, null, null, null, null, firstRowOnFrame, false, this);
        return fpf.makeEmptyGrel(b);
    }

    public Repeater getRepeater() {
        return null;
    }

    public int getSourceRow() {
        return inRow;
    }

    public float getMaxSpaceForTableBorderAtPageBottom(float currmax) {
        for (RunRangeBase runRepeater : listers) {
            if (runRepeater != null)
                currmax = runRepeater.getMaxSpaceForTableBorderAtPageBottom(currmax);
        }
        return currmax;
    }

    public boolean hasMoreValueElements() {
        return false;
    }

    /**
     * There are 2 types of savepoints one for normal page/column breaks and one for adjustment of multi frame columns.
     * the first type is stored directly in the runrange objects, because there is always only one of them. They disappear  with their
     * runrange objects.
     * The other type is created here. These save points keep the state of a run even if the run object has been completed.
     * It is currently used for multi frame columns.
     *
     * @return the new SavePoint with the recursive saves
     */
    SavePoint makeExternSavePoint() {
        return new SavePoint();
    }

    void restoreExternSavePoint(int row, int iteration) {
        inRow = row;
    }

    protected RunRepeater parentRunRepeater() {
        return null;
    }

    public class SavePoint {
        int row;
        int iteration;
        SavePoint[] savedListers;
        protected int[] savedListerCounts;// len: number of template rows

        public SavePoint() {
            row = inRow;
            iteration = getIteration();
            if (listers != null) {
                savedListers = new SavePoint[listers.length];
                for (int i = 0; i < listers.length; i++) {
                    RunRangeBase lister = listers[i];
                    if (lister != null)
                        savedListers[i] = lister.makeExternSavePoint();
                }
            }
            if (listerCounts != null) {
                savedListerCounts = new int[listerCounts.length];
                System.arraycopy(listerCounts, 0, savedListerCounts, 0, listerCounts.length);
            }
        }

        public RunRepeater restoreExternSavePoint() {
            RunRangeBase.this.restoreExternSavePoint(row, iteration);
            if (savedListers != null) {
                listers = new RunRepeater[savedListers.length];
                for (int i = 0; i < savedListers.length; i++) {
                    SavePoint savedLister = savedListers[i];
                    if (savedLister != null) {
                        final RunRepeater runRepeater = savedLister.restoreExternSavePoint();
                        listers[i] = runRepeater;
                    } else
                        listers[i] = null;
                }
            } else
                listers = null;
            if (savedListerCounts != null) {
                listerCounts = savedListerCounts;
            }
            return parentRunRepeater();
        }
    }
}

