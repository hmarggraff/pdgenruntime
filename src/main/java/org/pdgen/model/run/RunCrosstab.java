// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.data.UserAbortError;
import org.pdgen.model.RDBase;
import org.pdgen.model.RDCrossTab;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.run.OutputMode.LastRowState;
import org.pdgen.model.style.CellStyle;

public class RunCrosstab extends RunTemplate {
    int iteration;
    int colOffset;

    protected int getHeaderRows() {
        return ((RDCrossTab) dataDef).getHeaderRows();
    }

    protected int getFooterRows() {
        RDCrossTab crosstab = ((RDCrossTab) dataDef);
        return crosstab.isDoRowColSums() ? crosstab.getHeaderCols() * crosstab.getDataRows() : 0;
    }

    /**
     * Creates new RunRange for a crosstab
     *
     * @param out      output module
     * @param values   datavalues to be outputed
     * @param defs     definition how to output
     * @param level    level not used
     * @param template the source template
     */
    public RunCrosstab(OutputMode out, RVTemplate values, RDRangeBase defs, int level, TemplateModel template) {
        super(out, values, defs, level, template);
    }

    /**
     * makes a lister (RunRepeater) from the top level
     */
    protected RunRepeater makeLister(int row, int col) throws JoriaDataException {
        throw new JoriaAssertionError("makeLister not relevant in Crosstab");
    }

    public int getIteration() {
        return iteration;
    }

    protected LastRowState lookForNext() {
        if (iteration < values.getElementCount() - 1) {
            iteration++;
            inRow = getHeaderRows();
            return LastRowState.proceed;
        } else
            return LastRowState.endOfData;
    }

    public boolean row() throws JoriaDataException {
        if (inRow >= nRow)
            throw new JoriaAssertionError("RunRange.row called for row " + inRow + " which must be less than the number of rows which is " + nRow);
        try {
            return dataRow();
        } catch (UserAbortError a) {
            throw a;
        } catch (Throwable ex) {
            Trace.log(ex);
            throw new RunBreakException(ex);
        }
    }

    /**
     * processes the data line
     *
     * @return if no more space
     * @throws JoriaDataException in case of problems
     */
    protected boolean dataRow() throws JoriaDataException {
        RDBase[] rowDef = dataDef.getFields()[inRow];
        final RVAny[] rowVals = values.subs[inRow];
        int defCol = 0;
        int dCol = 0;
        RDCrossTab def = (RDCrossTab) dataDef;
        while (dCol < rowVals.length) {
            RDBase d = rowDef[defCol];
            if (d == null) {
                if (addNullCell(inRow, dCol))
                    return true;
                defCol = nextDefCol(defCol, dCol, rowVals.length, def);
            } else if (d instanceof CellDef) {
                colOffset = defCol - dCol;
                CellDef cd = (CellDef) d;
                if (out.generateOutput(this, cd, inRow, dCol))
                    return true;
                defCol = nextDefCol(defCol, dCol, rowVals.length, def);
                // no overflow: next col
                Trace.logDebug(Trace.fill, ind + "crosstab singletonLine col " + dCol);
            } else
                throw new JoriaAssertionError("Unknown rundef in crosstab");
            dCol++;
        }// for all columns
        // if we can complete all columns then our part of the line fits on page
        return false;// no page break
    }

    private int nextDefCol(int defCol, int dCol, int length, RDCrossTab def) {
        return def.nextDefCol(defCol, dCol, length);
    }

    protected int getColOffset() {
        return colOffset;
    }

    protected int calcNextCol(CellDef cd, int inCol) throws JoriaDataException {
        CellStyle bs = cd.getCascadedStyle();
        if (bs.getSpanHorizontal() > 1)// TODO das gibt Probleme. Span sollte in Crosstab Ignoriert werden.
        {
            int span = bs.getSpanHorizontal();
            if (span > 1) {
                out.addSpan(span - 1);
                inCol += span;
            } else {
                inCol++;
            }
        } else
            inCol++;
        return inCol;
    }

    protected boolean isLastCol(int c) {
        final RVAny[] rowVals = values.subs[inRow];
        return c == rowVals.length - 1;
    }

    protected boolean isLastRow() {
        return iteration >= values.getElementCount() - 1;
    }

    public boolean generateOutput(CellDef cd, int row, int c, boolean firstRowOnPage) throws JoriaDataException {
        FillPagedFrame fpf = (FillPagedFrame) out;
        TableBorderRequirements b = fpf.getTableBorderReqCol();
        CellStyle cs = cd.getCascadedStyle();
        b.buildTableBorderRequirement(row, c, cs, cd, null, null, null, null, firstRowOnPage, false, this);
        b.maxCol = c + 1;
        return cd.makeGraphicElement(b, iteration, fpf);
    }

    public boolean headerRow(int inHeaderRow) throws JoriaDataException {
        if (inHeaderRow < getHeaderRows()) {
            int saveInRow = inRow;
            inRow = inHeaderRow;
            dataRow();
            inRow = saveInRow;
            return true;
        } else
            return false;
    }

    protected int getColOffsetData() {
        return 0;
    }
}
