// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.Repeater;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.CellDef;

public abstract class OutputMode {

    public enum LastRowState {
        proceed, doneComplete, donePartial, doneRedo, endOfData, errorOccured
    }

    /*
        public static final int proceed = 0;// more data/cells and more space left
        public static final int doneComplete = 1;// space on page is used up completely without breaking a row
        public static final int donePartial = 2;// page is full, but the last row can be broken
        public static final int doneRedo = 3;// page is full but last row cannot be split and must be repeated on next page
        public static final int endOfData = 4;// no more data/cells (end of run reached)
        public static final int errorOccured = 5;// template run cannot continue du to an error
    */
    // these values remain the same over the lifetime of this BoxRunner
    public static final int TOP = 0;// Top border of a table
    public static final int INNER = 1;// inner border of a table
    public static final int BOTTOM = 2;// bottom border of a table
    protected TemplateModel template;
    protected DBData rootVal;
    String ind = "";
    protected RunTemplate fill;
    LastRowState lastRowState = LastRowState.proceed;
    int outRow;
    int colInCurrRow;
    Repeater[][] tableHeaderCols;
    public static byte[] lineSeparatorDefault = determinePlatformLineSeparator();// may not be final
    public static final byte[] lineSeparatorMac = {0x0D};
    public static final byte[] lineSeparatorUnix = {0x0A};
    public static final byte[] lineSeparatorWindows = {0x0D, 0x0A};

    protected OutputMode(TemplateModel template, DBData rootVal) {
        this.rootVal = rootVal;
        this.template = template;
        tableHeaderCols = new Repeater[template.getRowCount()][template.getColCount()];
    }

    public abstract void addSpan(int cols) throws JoriaDataException;

    public TemplateModel getTemplate() {
        return template;
    }

    LastRowState getEndState() {
        return lastRowState;
    }

    int getOutRow() {
        return outRow;
    }

    int getOutCol() {
        return colInCurrRow;
    }

    public abstract RunEnvImpl getRunEnv();

    // TODO hier brauchen wir stattdesen ein TBR.
    public abstract boolean addNullCell(RunRangeBase rr, int templateRow, int templateCol) throws JoriaDataException;

    public abstract boolean generateOutput(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException;

    public abstract boolean generateOutputRepeatedCell(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat, boolean lastRepeat) throws JoriaDataException;

    public abstract void startRepeater(Repeater r);

    public abstract void makeSavePoint();

    public abstract void releaseSavePoint();

    public abstract void steppedRepeater(Repeater fromRepeater);

    public abstract String getPageNumber();

    public abstract String getTotalPageNumberPlaceHolder();

    public abstract void checkSavePoints(boolean resetRepeater);

    static byte[] determinePlatformLineSeparator() {
        return System.lineSeparator().getBytes();
    }

    public abstract boolean isFirstBodyFrame();

    public abstract AggregateCollector getAggregateCollector();

    public abstract boolean isPageOutput();

    public abstract void addSpaceForTableBorderAtPageBottom(float spaceNeeded);

    public abstract void recalcSpaceForTableBorderAtPageBottom();

    public abstract float fillInner(RunRangeBase rr, final RDRange defs, final RVTemplate subVals, int row, int col) throws JoriaDataException;

}
