// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
// Created on July 5, 2000, 4:50 PM

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.model.Repeater;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.run.OutputMode.LastRowState;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.JoriaSimpleBorder;
import org.pdgen.model.style.RowOnNewPage;
import org.pdgen.model.style.TableStyle;

import java.util.Arrays;

public class RunRepeater extends RunRangeBase {
    protected Repeater myRepeater;
    protected int repeaterStartRow = -1;// -1 = not yet known
    protected int atElem;// iterator
    protected int savedAtElem;
    protected int displayPageNo;
    protected RunRepeater parent;
    protected int tocElementOffset;
    protected boolean inTemplateTocEntry;
    protected TableBorderRequirements[] savedTBRs;// funktioniert nur bei einzeiligen Repeatern
    protected boolean repeatData;
    private boolean abortNextRow;
    protected boolean[] outputRow;
    private boolean saveTry;
    private boolean ignoredSave;

    /*
     * Creates new RunRange for a collection using an Repeater
     */
    public RunRepeater(Repeater tate, RDRepeater fields, RVTemplate values, OutputMode out, int level, RunRepeater parent) {
        super(tate.getStartCol(), tate.getEndCol(), tate.getEndRow() - tate.getStartRow() + 1, tate.getModel(), out, level, fields, values);
        myRepeater = tate;
        startRow = tate.getStartRow();
        repeaterStartRow = out.getOutRow();
        out.startRepeater(tate);
        if (tate.getRepeaterList().length() > 0) {
            listerCounts = new int[nRow];
            // the index of each Repeater in explist matches the index in this array. (Hopefully)
            listers = new RunRepeater[tate.getRepeaterList().length()];
        }
        final TableStyle tableStyle = myRepeater.getCascadedTableStyle();
        if (!tableStyle.getBreakable()) {
            out.makeSavePoint();
        }
        if (tableStyle.getShowBorderAtPageBreak()) {
            out.addSpaceForTableBorderAtPageBottom(getMaxSpaceForTableBorderAtPageBottom(0));
        }
        this.parent = parent;
        if (listers != null) {
            int endCol = myRepeater.getEndCol();
            int startCol = myRepeater.getStartCol();
            savedTBRs = new TableBorderRequirements[endCol - startCol + 1];
        }
        outputRow = new boolean[nRow];
        Arrays.fill(outputRow, true);
    }

    protected boolean doRowAnyway() {
        boolean retVal = repeatData;
        repeatData = false;
        return retVal;
    }

    protected boolean onFirstIteration() {
        boolean ret = outputRow[inRow];
        outputRow[inRow] = false;
        return ret;
    }

    public OutputMode.LastRowState lookForNext() {
        boolean moreElems = atElem < valuesElementCount() - 1;
        if (moreElems) {
            next();
            Trace.logDebug(Trace.fill, ind + " advanced to next element ");
            out.steppedRepeater(myRepeater);
            displayPageNo = out.getRunEnv().getDisplayPageNo();
            if (isLastRow()) {
                out.recalcSpaceForTableBorderAtPageBottom();
            }
            return LastRowState.proceed;
        } else {
            if (!myRepeater.getCascadedTableStyle().getBreakable() || !myRepeater.getCascadedTableStyle().getBreakableGroup()) {
                if (!ignoredSave)
                    out.releaseSavePoint();
                ignoredSave = false;
            }
            return LastRowState.endOfData;
        }
    }

    public void next() {
        if (!myRepeater.getCascadedTableStyle().getBreakableGroup() && myRepeater.getCascadedTableStyle().getBreakable()) {
            if (!ignoredSave)
                out.releaseSavePoint();
            saveTry = false;
            ignoredSave = false;
        }
        if (listers != null) {
            for (int i = 0; i < listerCounts.length; i++) {
                if (listerCounts[i] != 0)
                    throw new JoriaAssertionError("RunRange.next called but listerCounts[" + i + "] is not zero: " + listerCounts[i]);
            }
            for (int i = 0; i < listers.length; i++) {
                if (listers[i] != null)
                    throw new JoriaAssertionError("RunRange.next called but listers[" + i + "] is not null");
            }
        }
        Arrays.fill(outputRow, true);
        firstRepeat = true;
        atElem++;
        abortNextRow = myRepeater.getCascadedTableStyle().getDataOnNewPage().getLinesBeforeNewLine() != RowOnNewPage.NEVER;
        tocElementOffset = 0;
        inRow = 0;
		/*
				for (int i = 0; savedTBRs != null && i < savedTBRs.length; i++)
				{
					savedTBRs[i] = null;
				}
				*/
    }

    public Repeater findRepeaterStart(int r, int c) {
        return myRepeater.getRepeaterList().getRepeaterHere(r, c);
    }

    protected RunRepeater makeLister(int row, int col) throws JoriaDataException {
        if (values == null)
            return null;
        RDRepeater listDef = (RDRepeater) dataDef.getFields()[row][col];
        Repeater tate = (Repeater) listDef.getModel();
        RVObjects listVals = (RVObjects) values.get(row, col);
        RVTemplate lve = null;
        if (listVals != null && listVals.elems != null && listVals.elems.length > 0)
            lve = listVals.elems[atElem];
        final TableStyle tableStyle = tate.getCascadedTableStyle();
        if ((lve == null || lve.getElementCount() == 0) && tableStyle.getSuppressEmpty())
            return null;
        RunRepeater lister = new RunRepeater(tate, listDef, lve, out, nestingLevel + 1, this);
        lister.startBodyAt(out.getOutRow());
        listers[tate.getTopLevelIndex()] = lister;
        listerCounts[tate.getEndRow() - myRepeater.getStartRow()]++;
        return lister;
    }


    public void pageBreak(OutputMode.LastRowState endState) {
        Trace.logDebug(Trace.fill, ind + "RunRange pageBreak " + endState);
        if (myRepeater.getCascadedTableStyle().getHeaderOnEachPage())
            inHeaderRow = -myRepeater.getHeaderRows();
        if (listers == null)
            return;
        for (RunRangeBase activeLister : listers) {
            if (activeLister != null// null --> completed
                    // header of active lister inside header of myRepeater
                    // --> do not reset, because it will be output when header of my repeater is processed
                    && (myRepeater == null || myRepeater.getStartRow() < activeLister.getRepeater().getStartRow() - activeLister.getRepeater().getHeaderRows())) {
                activeLister.pageBreak(endState);
            }
        }
    }

    public Repeater getRepeater() {
        return myRepeater;
    }

    public void startBodyAt(int startRow) {
		/*
				for (int i = 0; savedTBRs != null && i < savedTBRs.length; i++)
				{
					savedTBRs[i] = null;
				}
				*/
        repeaterStartRow = startRow;
        displayPageNo = out.getRunEnv().getDisplayPageNo();
        super.startBodyAt(startRow);
        repeatData = myRepeater.getCascadedTableStyle().getDataOnEachPage() && atElem > 0 && (myRepeater.getStartRow() == myRepeater.getEndRow() || !myRepeater.getCascadedTableStyle().getBreakable());
        int newPageForRow = myRepeater.getCascadedTableStyle().getDataOnNewPage().getLinesBeforeNewLine();
        abortNextRow = (newPageForRow == RowOnNewPage.ALWAYS || newPageForRow == RowOnNewPage.ON_SECOND_ROW) && atElem > 0 && !out.isFirstBodyFrame() && startRow > myRepeater.getStartRow();
    }

    public boolean row() throws JoriaDataException {
        if (abortNextRow && out.isPageOutput()) {
            out.lastRowState = LastRowState.doneRedo;
            return true;
        } else {
            return super.row();
        }
    }

    /*
     * processes one line at the top level. I.e. there are no inner Table RunRanges active.
     */
    protected boolean singletonRow() throws JoriaDataException {
        if (inRow == 0 && !myRepeater.getCascadedTableStyle().getBreakableGroup() && myRepeater.getCascadedTableStyle().getBreakable()) {
            if (saveTry)
                ignoredSave = true;
            else
                out.makeSavePoint();
        }
        return super.singletonRow();
    }

    protected int listerCountIndexOffset() {
        return myRepeater.getStartRow();
    }

    public LastRowState hasInitialData() {
        if (valuesElementCount() > 0)//(next()) // is collection empty
        {
            next();
            return LastRowState.proceed;
        } else
            return LastRowState.endOfData;// empty collection
    }

    public int getIteration() {
        return atElem;
    }

    public void reset() {
        ignoredSave = false;
        saveTry = false;
        atElem = 0;
        Arrays.fill(outputRow, true);
        super.reset();
    }

    protected void saveIteration() {
        savedAtElem = atElem;
        saveTry = true;
    }

    protected void restoreIteration() {
        if (savedAtElem < atElem)// we have advanced since the savepoint restart listers
        {
            for (int i = 0; listers != null && i < listers.length; i++) {
                listers[i] = null;
            }
        }
        atElem = savedAtElem;
    }

    protected boolean isNotFirstRow() {
        return atElem != 0 || parent != null && parent.isNotFirstRow();
    }

    protected boolean isLastRow() {
        //final boolean b = atElem != valuesElementCount() - 1 || parent != null && !parent.isLastRow();
        final boolean b1 = (atElem >= valuesElementCount() - 1) && (parent == null || parent.isLastRow());
        return b1;
    }

    /* Algorithm:
        At Start of Repeater/following pages: Allocate Vertical Table Lines in dest and keep them
        At start of Repeater draw Top Border
        Generate content
        At end of row
        */
    public boolean generateOutput(CellDef cd, int row, int c, boolean firstRowOnPage) throws JoriaDataException {
        FillPagedFrame fpf = (FillPagedFrame) out;
        if (values == null || values.getElementCount() == 0) {
            out.addNullCell(this, inRow, c);
            return false;
        }
        TableBorderRequirements b = fpf.getTableBorderReqCol();
        CellStyle cs = cd.getCascadedStyle();
        JoriaSimpleBorder ct = null;
        final TableStyle tableStyle = myRepeater.getCascadedTableStyle();
        if (atElem != 0) {
            if (firstRowOnPage && tableStyle.getShowBorderAtPageBreak())
                ct = template.getBorderAt(row, c, true, true);
            else if (row + getRowOffset() == myRepeater.getStartRow())
                ct = tableStyle.getGroupSeperatorBorder();
        }
        JoriaSimpleBorder cb = null;
        if (cs.getSpanToInner() == CellStyle.SpanToInner.spanAllCells && row + getRowOffset() + cs.getSpanVertical() - 1 == myRepeater.getEndRow() && hasMoreRows())
            cb = JoriaSimpleBorder.NULL;
        else if (atElem != valuesElementCount() - 1 && row + getRowOffset() + cs.getSpanVertical() - 1 == myRepeater.getEndRow())
            cb = tableStyle.getGroupSeperatorBorder();
        b.buildTableBorderRequirement(row, c, cs, cd, ct, null, null, cb, firstRowOnPage, tableStyle.getShowBorderAtPageBreak(), this);
        b.value = values.subs[row][c];
        b.drillDownObject = values.getDrillDownKey(atElem);
        b.maxCol = out.getOutCol() + cs.getSpanHorizontal();
		/*
				if(savedTBRs != null && cd.getCascadedStyle().getSpanToInner() == CellStyle.SpanToInner.spanAllCells)
				{
					boolean activeLister = false;
					for (RunRepeater lister : listers)
					{
						if (lister != null && lister.myRepeater.getEndRow() == row)
						{
							activeLister = true;
						}
					}
					if(activeLister)
					{
						b.vSpanRepeater = true;
						savedTBRs[c] = b;
					}
				}
				*/
        return cd.makeGraphicElement(b, atElem, fpf);
    }

    public int valuesElementCount() {
        if (values != null)
            return values.getElementCount();
        return 0;
    }

    protected int getRowOffset() {
        return myRepeater.getStartRow();
    }

    protected int getColOffset() {
        return myRepeater.getStartCol();
    }

    protected void saveData(TableBorderRequirements tbr, int row, int col) {
        if (listers != null)
            savedTBRs[col] = tbr;
    }

    protected boolean hasMoreRows() {
        if (atElem < valuesElementCount() - 1)
            return true;
        if (listers == null)
            return false;
        for (RunRangeBase lister : listers) {
            if (lister == null)
                continue;
            if (lister.hasMoreValueElements())
                return true;
            if (lister.hasMoreRows())
                return true;
        }
        return false;
    }

    public boolean hasMoreValueElements() {
        return getIteration() < valuesElementCount() - 1;
    }

    public void resetRunningTotals() {
        for (int r = startRow; r < startRow + nRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                CellDef cd = template.getCellAt(r, c);
                if (cd instanceof DataCellDef) {
                    ((DataCellDef) cd).resetRunningTotals(out.getAggregateCollector());
                }
            }
        }
    }

    public float getMaxSpaceForTableBorderAtPageBottom(float currmax) {
        if (isLastRow())
            return 0;
        final int bottomRow = myRepeater.getRowCount() - 1;
        for (int i = 0; i < myRepeater.getColCount(); i++) {
            final JoriaSimpleBorder bottom = myRepeater.getBorderAt(bottomRow, i, true, false);
            if (myRepeater.getCascadedTableStyle().getShowBorderAtPageBreak())
                currmax = Math.max(currmax, bottom.getThickness());
        }
        return currmax;
    }

    void restoreExternSavePoint(int row, int iteration) {
        super.restoreExternSavePoint(row, iteration);
        atElem = iteration;
    }

    protected RunRepeater parentRunRepeater() {
        return this;
    }

}
