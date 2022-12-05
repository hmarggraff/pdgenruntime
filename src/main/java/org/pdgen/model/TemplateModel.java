// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.GroupKeyAccess;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.JoriaUserException;
import org.pdgen.env.Res;
import org.pdgen.model.cells.*;
import org.pdgen.model.run.RDRange;
import org.pdgen.model.run.RDRangeBase;
import org.pdgen.model.run.RDRepeaterNext;
import org.pdgen.model.style.*;
import org.pdgen.oql.OQLParseException;
import org.pdgen.projection.UnboundAccess;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;

public class TemplateModel implements Serializable, ModelBase {
    private static final long serialVersionUID = 7L;
    protected RepeaterList myRepeaters;
    protected ArrayList<ArrayList<CellDef>> myRows;
    protected int rowCount;
    protected int colCount;
    protected TemplateBox myContainer;
    protected ArrayList<FlexSize> rowSizing;
    protected ArrayList<FlexSize> colSizing;
    protected RDCrossTab crosstab;
    protected ArrayList<ArrayList<JoriaSimpleBorder>> horizontalBorders;//  size is rowCount+1 and colCount
    protected ArrayList<ArrayList<JoriaSimpleBorder>> verticalBorders;// size is colCount+1 and rowCount.
    protected transient TemplateLayouter myLayouter;

    protected TemplateModel() {
    }

    public TemplateModel(int r, int c, TemplateBox container) {
        colCount = c;
        rowCount = r;
        myContainer = container;
        if (container != null)
            container.setTemplate(this);
        myRows = new ArrayList<ArrayList<CellDef>>(r);
        // expandersFlattened = new RepeaterList();
        myRepeaters = new RepeaterList(this);
        rowSizing = new ArrayList<FlexSize>(r);
        colSizing = new ArrayList<FlexSize>(c);
        for (int i = 0; i < r; i++) {
            ArrayList<CellDef> row = new ArrayList<CellDef>(c);
            rowSizing.add(FlexSize.FLEX);
            for (int j = 0; j < c; j++) {
                row.add(null);
            }
            myRows.add(row);
        }
        for (int j = 0; j < c; j++) {
            colSizing.add(FlexSize.FLEX);
        }
    }

    public void addRepeaterUnchecked(Repeater newSF) {
        Trace.logDebug(Trace.template, "Adding Box " + " sr:" + newSF.getStartRow() + " sc:" + newSF.getStartCol() + " h:" + newSF.getHeight() + " w:" + newSF.getWidth() + " hr:" + newSF.getHeaderRows() + " fr:" + newSF.getFooterRows());
        if (newSF.getOuterRepeater() == null)
            myRepeaters.addRepeater(newSF);
    }

    public void addRepeater(Repeater newSF) {
        Trace.logDebug(Trace.template, "Adding Box " + " sr:" + newSF.getStartRow() + " sc:" + newSF.getStartCol() + " h:" + newSF.getHeight() + " w:" + newSF.getWidth() + " hr:" + newSF.getHeaderRows() + " fr:" + newSF.getFooterRows());
        Trace.check(newSF.getEndRow() <= rowCount);
        Trace.check(newSF.getEndCol() <= colCount);
        if (newSF.getOuterRepeater() == null)
            myRepeaters.addRepeater(newSF);
        newSF.nest();
        myRepeaters.ownCells0(null);
    }

    public void addChangeListener(ChangeListener l) {
        if (myLayouter != null)
            myLayouter.addChangeListener(l);
        //else if there is no layout nothing can change interactively
    }

    public CellDef cellAt(int r, int c) {
        if (r >= rowCount || r < 0)
            throw new JoriaAssertionError("Accessing invalid row " + r + " rowCount " + rowCount);
        if (c >= colCount && c < 0)
            throw new JoriaAssertionError("Accessing invalid col " + c + " colCount " + colCount);
        try {
            ArrayList<CellDef> row = getRow(r);
            if (row == null)
                return null;
            else
                return row.get(c);
        } catch (ArrayIndexOutOfBoundsException ex) {
            String s = "cellAt bad row:" + r + " col:" + c + " height:" + rowCount + " width:" + colCount;//trdone
            System.out.println(s);
            throw new JoriaAssertionError(s);
        }
    }

    public void deleteRange(int sc, int sr, int ec, int er) {
        deleteRange(sc, sr, ec, er, false);
    }

    public void deleteRange(int sc, int sr, int ec, int er, boolean justClear) {
        HashSet<CellDef> removeToo = null;// removed an aggregated Cell?
        ArrayList<AggregateDef> obsoleteAggregates = null;
        for (int r = sr; r <= er; r++) {
            ArrayList<CellDef> row = myRows.get(r);
            for (int colIx = sc; colIx <= ec; colIx++) {
                CellDef old = row.get(colIx);
                if (old instanceof DataCellDef) {
                    DataCellDef dcd = (DataCellDef) old;
                    ArrayList<SummaryCell> sums = dcd.getTotalsCells();
                    if (sums != null) {
                        if (removeToo == null)
                            removeToo = new HashSet<CellDef>();
                        removeToo.addAll(sums);
                    }
                    ArrayList<AggregateDef> aggl = dcd.getAggregates();
                    if (aggl != null) {
                        if (obsoleteAggregates == null)
                            obsoleteAggregates = new ArrayList<AggregateDef>(aggl);
                        else
                            obsoleteAggregates.addAll(aggl);
                    }
                    final Repeater cellRepeater = dcd.getRepeater();
                    if (cellRepeater != null && dcd.getAccessor() instanceof GroupKeyAccess && !justClear) {
                        final Repeater outerRepeater = cellRepeater.getOuterRepeater();
                        if (outerRepeater == null) {
                            if (cellRepeater.isContained(sr, er, sc, ec))
                                myRepeaters.unlinkGroup(cellRepeater);
                        } else if (cellRepeater.isContained(sr, er, sc, ec))
                            outerRepeater.getRepeaterList().unlinkGroup(cellRepeater);
                    }
                }
                if (old != null) {
                    old.removed();
                }
            }
        }
        if (sc == 0 && ec == colCount - 1 && !justClear)// one or multiple full rows
        {
            for (int i = er; i >= sr; i--) {
                myRows.remove(i);
                rowSizing.remove(i);
            }
            if (sr == 0 && er == rowCount - 1) {
                // nothing left so add one element again
                ArrayList<CellDef> row = new ArrayList<CellDef>(1);
                row.add(0, null);
                myRows.add(0, row);
                rowCount = 1;
                colCount = 1;
                rowSizing.add(new FlexSize());
                colSizing.add(new FlexSize());
            } else
                rowCount += sr - er - 1;
            myRepeaters.adjustRows(sr, sr - er - 1);
            // löschen der Borders
            for (int i = er; verticalBorders != null && i >= sr; i--) {
                if (verticalBorders.size() > i)
                    verticalBorders.remove(i);
            }
            for (int i = 0; horizontalBorders != null && i < colCount; i++) {
                if (horizontalBorders.size() <= i)
                    continue;
                ArrayList<JoriaSimpleBorder> borders = horizontalBorders.get(i);
                if (borders == null || borders.size() <= sr)
                    continue;
                JoriaSimpleBorder first = borders.get(sr);
                JoriaSimpleBorder last;
                if (borders.size() <= er + 1)
                    last = null;
                else
                    last = borders.get(er + 1);
                int startIndex;
                int endIndex;
                if (first != null && first.isHeavier(last)) {
                    startIndex = sr + 1;
                    endIndex = er + 1;
                } else {
                    startIndex = sr;
                    endIndex = er;
                }
                for (int j = endIndex; j >= startIndex; j--) {
                    if (borders.size() > j)
                        borders.remove(j);
                }
            }
        } else if (sr == 0 && er == rowCount - 1 && !justClear)// one or multiple full columns
        {
            for (int i = 0; i < rowCount; i++) {
                ArrayList<CellDef> r = getRow(i);
                for (int j = ec; j >= sc; j--) {
                    r.remove(j);
                }
            }
            colCount = colCount - ec + sc - 1;
            for (int j = ec; j >= sc; j--) {
                colSizing.remove(j);
            }
            myRepeaters.adjustCols(sc, sc - ec - 1);
            // löschen der Borders
            for (int i = ec; horizontalBorders != null && i >= sc; i--) {
                if (horizontalBorders.size() > i)
                    horizontalBorders.remove(i);
            }
            for (int i = 0; verticalBorders != null && i < rowCount; i++) {
                if (verticalBorders.size() <= i)
                    continue;
                ArrayList<JoriaSimpleBorder> borders = verticalBorders.get(i);
                if (borders == null || borders.size() <= sc)
                    continue;
                JoriaSimpleBorder first = borders.get(sc);
                JoriaSimpleBorder last;
                if (borders.size() <= ec + 1)
                    last = null;
                else
                    last = borders.get(ec + 1);
                int startIndex;
                int endIndex;
                if (first != null && first.isHeavier(last)) {
                    startIndex = sc + 1;
                    endIndex = ec + 1;
                } else {
                    startIndex = sc;
                    endIndex = ec;
                }
                for (int j = endIndex; j >= startIndex; j--) {
                    if (borders.size() > j)
                        borders.remove(j);
                }
            }
        } else
        // clearing of cells, does not remove cells
        {
            // if neither complete rows or cols, just clear the cells
            for (int r = sr; r <= er; r++) {
                for (int c = sc; c <= ec; c++) {
                    CellDef cd = cellAt(r, c);
                    if (cd != null && cd.getRepeater() != null) {
                        FreeCellDef ncd = new FreeCellDef(cd.getGrid());
                        ncd.setRepeater(cd.getRepeater());
                        setCellAt(ncd, r, c);
                        ncd.setStyle(null);
                    } else
                        setCellAt(null, r, c);
                }
            }
			/*
			CellDef tlcd = cellAt(sr, sc);
			Repeater rp = null;
			if (tlcd != null)
				rp = tlcd.getRepeater();
			if (rp != null && er - sr < rp.getEndRow() - rp.getStartRow() && ec - sc < rp.getEndCol() - rp.getStartCol())
			{
				// range completely inside Repeater: just clear cells
				for (int i = sr; i <= er; i++)
				{
					Vector r = getRow(i);
					for (int j = sc; j <= ec; j++)
					{
						r.setElementAt(new FreeCellDef(this, rp), j);
					}
				}
			}
			else
			{
				myRepeaters.adjustDeletedRange(sr, er, sc, ec);
				int shiftDistance = ec - sc + 1; // shift distance
				int lastShiftedCell = colCount - 1 - shiftDistance;
				Trace.log(Trace.template, "deletecells shifting " + shiftDistance + " cells from " + sc + " to " + lastShiftedCell);
				for (int i = sr; i <= er; i++)
				{
					Vector r = getRow(i);

					//old hmf 28.7.2001			for (int j = sc; j <= Math.min(ec, colCount - 1 - dx); j++)
					for (int j = sc; j <= lastShiftedCell; j++)
					{
						r.setElementAt(r.elementAt(j + shiftDistance), j);
					}
					for (int j = colCount - 1; j >= colCount - shiftDistance; j--)
					{
						r.setElementAt(null, j);
					}
				}
			}
			*/
        }
        if (removeToo != null)
            myContainer.removeObsoleteAggregates(removeToo, obsoleteAggregates);
        //myRepeaters.ownCells0();
        clearCachedStyles();
        fireChange("delRange " + sr + "/" + er + "," + sc + "/" + ec);//trdone
    }

    public void deleteRepeater(int ix) {
        Repeater b = myRepeaters.get(ix);
        for (int i = b.getStartRow(); i <= b.getEndRow(); i++) {
            for (int j = b.getStartCol(); j <= b.getEndCol(); j++) {
                setCellAt(null, i, j);
            }
        }
        myRepeaters.remove(ix);
        if (myLayouter != null)
            myLayouter.invalidateLayout();
    }

    public void fireChange() {
        fireChange(null);
    }

    public void fireChange(String reason) {
        if (myContainer != null && reason != null)
            Trace.logDebug(Trace.template, "Template changed " + myContainer.getBoxTypeName() + ": " + reason);
        Env.repoChanged();
        if (myLayouter != null)
            myLayouter.fireChange();
    }

    public int getColCount() {
        return colCount;
    }

    public FlexSize getColSizingAt(int at) {
        return colSizing.get(at);
    }

    public TemplateBox getFrame() {
        return myContainer;
    }

    public Repeater getRepeaterAt(int sRow, int sCol) {
        CellDef cd = cellAt(sRow, sCol);
        if (cd != null) {
            final Repeater repeater = cd.getRepeater();
            return repeater;
        }
        return null;// not nested: done
    }

    public Repeater getOuterRepeaterForNestedFrame() {
        if (!(myContainer instanceof NestedBox))   // not nested
            return null;
        final NestedBox nestedContainer = (NestedBox) myContainer;
        return nestedContainer.getCell().getContainingRepeater();
    }

    public Repeater[] getRepeaters() {
        return myRepeaters.get();
    }

    public RepeaterList getRepeaterList() {
        return myRepeaters;
    }

    public int getRepeaterCount() {
        return myRepeaters.length();
    }

    public ArrayList<CellDef> getRow(int r) {
        return myRows.get(r);
    }

    public int getRowCount() {
        return rowCount;
    }

    public FlexSize getRowSizingAt(int at) {
        return rowSizing.get(at);
    }

    public FlexSize getSizingAt(int at, boolean isRow) {
        if (isRow)
            return rowSizing.get(at);
        else
            return colSizing.get(at);
    }

    public void insertCol(int col) {
        addCols(col, 1, 0);
    }

    public void insertCols(int at, int n) {
        addCols(at, n, 0);
    }

    public void appCol(int col) {
        addCols(col, 1, 1);
    }

    public void appendCols(int at, int n) {
        addCols(at, n, 1);
    }

    /**
     * the generic method for adding colums to the left or right of a column
     *
     * @param at  the column relative to which the new cols are added
     * @param n   the number of added cols
     * @param off the offset from the pivot column: 0 to insert left, 1 to insert to the right; all other bad
     */
    public void addCols(int at, int n, int off) {
        addColsUnadjusted(n, at, off, off == 0);
        myRepeaters.adjustCols(at, n);
        clearCachedStyles();
        fireChange("add cols " + at + " n: " + n + " off:" + off);//trdone
    }

    public void addColsUnadjusted(int n, int at, int off, boolean fromLeft) {
        colCount += n;
        for (ArrayList<CellDef> myRow : myRows) {
            ArrayList<CellDef> r = myRow;
            //r.ensureCapacity(colCount);
            //r.setSize(colCount);
            for (int i = 0; i < n; i++) {
                r.add(at + off, null);
            }
        }
        colSizing.ensureCapacity(colCount);
        //colSizing.setSize(colCount);
        ArrayList<JoriaSimpleBorder> src = null;
        int index = at + (fromLeft ? 1 : 0);
        if (horizontalBorders != null && index < horizontalBorders.size())
            src = horizontalBorders.get(index);
        for (int i = 0; i < n; i++) {
            colSizing.add(at + off, new FlexSize());
            if (src != null) {
                ArrayList<JoriaSimpleBorder> dest = new ArrayList<JoriaSimpleBorder>(src.size());
                for (JoriaSimpleBorder aSrc : src) {
                    dest.add(JoriaSimpleBorder.copy(aSrc));
                }
                horizontalBorders.add(at + off, dest);
            }
        }
        for (int i = 0; verticalBorders != null && i < verticalBorders.size() && i < rowCount; i++) {
            ArrayList<JoriaSimpleBorder> borders = verticalBorders.get(i);
            if (borders == null)
                continue;
            if (index >= borders.size())
                continue;
            JoriaSimpleBorder s = borders.get(index);
            for (int j = 0; j < n; j++) {
                borders.add(at + off, JoriaSimpleBorder.copy(s));
            }
        }
    }

    public void appCol(Repeater inside) {
        addColsUnadjusted(1, colCount, 0, false);
        Repeater r = inside;
        while (r != null) {
            r.endCol++;
            r = r.getOuterRepeater();
        }
        myRepeaters.ownCells0(null);
        fireChange("add col inside " + inside);//trdone
    }

    public void insertRow(int r) {
        addRows(r, 1, 0);
    }

    public void insertRows(int r, int n) {
        addRows(r, n, 0);
    }

    public void addRowOutsideBelow(Repeater repeater) {
        int endRow = repeater.getEndRow() + 1;
        addRowsUnadjusted(1, endRow, 0, false);
        Repeater outer = repeater.getOuterRepeater();
        while (outer != null) {
            outer.setEndRowUnchecked(outer.getEndRow() + 1);
            outer = outer.getOuterRepeater();
        }
        shiftInnerRow(myRepeaters, endRow);
        myRepeaters.ownCells0(null);
        clearCachedStyles();
        fireChange("Row outside table");//trdone
    }

    private void shiftInnerRow(RepeaterList l, int at) {
        Repeater[] repeaters = l.get();
        if (repeaters == null)
            return;
        for (Repeater r : repeaters) {
            if (r.getStartRow() >= at) {
                r.setStartRowUnchecked(r.getStartRow() + 1);
                r.setEndRowUnchecked(r.getEndRow() + 1);
            }
            shiftInnerRow(r.getRepeaterList(), at);
        }
    }

    public void addColOutside(Repeater repeater) {
        int endCol = repeater.getEndCol() + 1;
        addColsUnadjusted(1, endCol, 0, false);
        Repeater outer = repeater.getOuterRepeater();
        while (outer != null) {
            outer.setEndColUnchecked(outer.getEndCol() + 1);
            outer = outer.getOuterRepeater();
        }
        shiftInnerCol(getRepeaterList(), endCol);
        myRepeaters.ownCells0(null);
        clearCachedStyles();
        fireChange("Col outside table");//trdone
    }

    private void shiftInnerCol(RepeaterList l, int at) {
        Repeater[] repeaters = l.get();
        if (repeaters == null)
            return;
        for (Repeater r : repeaters) {
            if (r.getStartCol() >= at) {
                r.setStartColUnchecked(r.getStartCol() + 1);
                r.setEndColUnchecked(r.getEndCol() + 1);
            }
        }
    }

    public void addRows(int r, int n, int off) {
        addRowsUnadjusted(n, r, off, off == 0);
        myRepeaters.adjustRows(r, n);
        clearCachedStyles();
        fireChange("add rows " + r + " n: " + n + " off:" + off);//trdone
    }

    public void addRowsUnadjusted(int n, int r, int off, boolean fromTop) {
        myRows.ensureCapacity(myRows.size() + n);
        for (int i = 0; i < n; i++) {
            ArrayList<CellDef> newRow = new ArrayList<CellDef>(getColCount());
            //noinspection RedundantCast
            newRow.addAll(Collections.nCopies(getColCount(), (CellDef) null));
            // elements will initialized to null. ownCells will add any required cells
            myRows.add(r + off, newRow);
        }
        rowCount += n;
        //rowSizing.ensureCapacity(rowCount + n);
        ArrayList<JoriaSimpleBorder> src = null;
        int index = r + (fromTop ? 1 : 0);
        if (verticalBorders != null && index < verticalBorders.size())
            src = verticalBorders.get(index);
        for (int i = 0; i < n; i++) {
            rowSizing.add(r + off, new FlexSize());
            if (src != null) {
                ArrayList<JoriaSimpleBorder> dest = new ArrayList<JoriaSimpleBorder>(src.size());
                for (JoriaSimpleBorder aSrc : src) {
                    dest.add(JoriaSimpleBorder.copy(aSrc));
                }
                verticalBorders.add(r + off, dest);
            }
        }
        for (int i = 0; horizontalBorders != null && i < horizontalBorders.size() && i < colCount; i++) {
            ArrayList<JoriaSimpleBorder> borders = horizontalBorders.get(i);
            if (borders == null)
                continue;
            if (index >= borders.size())
                continue;
            JoriaSimpleBorder s = borders.get(index);
            for (int j = 0; j < n; j++) {
                borders.add(r + off, JoriaSimpleBorder.copy(s));
            }
        }
    }

    public void addRow(boolean above, Repeater outsideOf) {
        addRowsUnadjusted(1, rowCount, 0, above);
        Repeater r = outsideOf.getOuterRepeater();
        if (above) {
            shiftInCols(outsideOf.getStartRow() - outsideOf.getHeaderRows(), outsideOf.getEndRow(), outsideOf.getStartCol(), outsideOf.getEndCol(), 1, r);
            outsideOf.shift(1, 0);
        }
        while (r != null) {
            r.endRow++;
            r = r.getOuterRepeater();
        }
        myRepeaters.ownCells0(null);
        fireChange("add row outside " + outsideOf);//trdone
    }

    public void appRow(Repeater inside) {
        addRowsUnadjusted(1, inside.endRow + 1, 0, false);
        Repeater r = inside;
        while (r != null) {
            r.endRow++;
            r = r.getOuterRepeater();
        }
        myRepeaters.ownCells0(null);
        fireChange("add row inside " + inside);//trdone
    }

    public void appRow(int r) {
        addRows(r, 1, 1);
    }

    public void appRow() {
        addRows(rowCount, 1, 0);
    }

    public void appRow(int r, int n) {
        addRows(r, n, 1);
    }

    public boolean isOverlapping(int sr, int er, int sc, int ec) {
        return myRepeaters.isOverlapping(sr, er, sc, ec);
    }

    public boolean namedStyleChanged(CellStyle b) {
        boolean changed = false;
        for (ArrayList<CellDef> myRow : myRows) {
            for (CellDef aMyRow : myRow) {
                CellDef cd = aMyRow;
                if (cd != null && cd.getStyle() != null) {
                    CellStyle cs = cd.getStyle();
                    if (cs.getName() != null && cs.getName().equals(b.getName())) {
                        cd.setStyle(b);
                        changed = true;
                    }
                    cs = cs.getBaseStyle();
                    while (cs != null) {
                        if (cs.getName().equals(b.getName())) {
                            cd.clearCachedStyle();
                            changed = true;
                        }
                        cs = cs.getBaseStyle();
                    }
                }
                if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    changed |= ncd.getInnerBox().getTemplate().namedStyleChanged(b);
                }
            }
        }
        return changed;
    }

    public void namedFrameStyleChanged(FrameStyle f) {
        for (ArrayList<CellDef> r : myRows) {
            for (CellDef cd : r) {
                if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    ncd.getInnerBox().namedFrameStyleChanged(f);
                }
            }
        }
    }

    public void clearCachedStyles() {
        for (ArrayList<CellDef> myRow : myRows) {
            for (CellDef aMyRow : myRow) {
                CellDef cd = aMyRow;
                if (cd != null) {
                    cd.clearCachedStyle();
                }
                if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    ncd.getInnerBox().clearCachedStyles();
                }
            }
        }
    }

    public void removeChangeListener(ChangeListener l) {
        if (myLayouter != null)
            myLayouter.removeChangeListener(l);
    }

    public void setCellAt(CellDef comp, int r, int c) {
        try {
            CellDef old = cellAt(r, c);
            if (old instanceof DataCellDef) {
                DataCellDef dold = (DataCellDef) old;
                ArrayList<AggregateDef> aggls = dold.getAggregates();
                if (aggls != null) {
                    myContainer.removeObsoleteAggregates(dold.getTotalsCells(), aggls);
                }
            }
            if (old != null) {
                old.removed();
            }
            setCellAtInternal(comp, r, c);
			/*
						Vector row = getRow(r);
						if (comp != null)
							comp.setGrid(this);
						row.setElementAt(comp, c);
						*/
        } catch (ArrayIndexOutOfBoundsException ex) {
            String s = "setCellAt bad pos row:" + r + " col:" + c + " height:" + rowCount + " width:" + colCount;//trdone
            System.out.println(s);
            throw new JoriaAssertionError(s);
        }
        fireChange("set cell" + r + "/" + c);//trdone
    }

    protected void setCellAtInternal(CellDef newCell, int r, int c) {
        // no checks: consistency must be maintained by caller
        CellDef oldCell;
        try {
            Repeater rep = myRepeaters.innerMostRepeaterAt(r, c);
            if (rep != null) {
                if (newCell == null) {
                    Trace.log(Trace.template, "setCellAtInternal: Try to set null cell in repeater " + r + "/" + c);
                    newCell = new FreeCellDef(this, rep);
                } else if (rep != newCell.getRepeater()) {
                    Trace.log(Trace.template, "setCellAtInternal: Try to set cell with bad repeater " + r + "/" + c);
                    newCell.setRepeater(rep);
                }
            } else if (newCell != null && newCell.getRepeater() != null) {
                Trace.log(Trace.template, "setCellAtInternal: Try to set cell with bad repeater " + r + "/" + c);
            }
            ArrayList<CellDef> row = getRow(r);
            oldCell = row.get(c);
            if (oldCell != null && oldCell != newCell && newCell != null) {
                final CellStyle style = oldCell.getStyle();
                final CellStyle newStyle = newCell.getStyle();
                if (style != null && (newStyle == null || newStyle == PredefinedStyles.instance().theCellStyleDefaultNumberStyle || newStyle == PredefinedStyles.instance().theCellStyleDefaultProblemStyle))
                    newCell.setStyle(style);
            }
            row.set(c, newCell);
        } catch (ArrayIndexOutOfBoundsException ex) {
            String s = "setCellAtInternal bad pos row:" + r + " col:" + c + " height:" + rowCount + " width:" + colCount;//trdone
            System.out.println(s);
            throw new JoriaAssertionError(s);
        }
        if (newCell != oldCell)
            fireChange("cell change " + r + "/" + c);//trdone
        if (crosstab != null)
            crosstab.rebuildFields();
    }

    public void setColSizingAt(int col, FlexSize size) {
        Trace.check(size);
        colSizing.set(col, size);
        fireChange("colSizing " + col);//trdone
    }

    public void setContainer(TemplateBox newContainer) {
        myContainer = newContainer;
    }

    public void setRowSizingAt(int row, FlexSize size) {
        Trace.check(size);
        rowSizing.set(row, size);
        fireChange("rowsizing row " + row);//trdone
    }

    /**
     * check if range overlaps any repeater but is not contaioned in it
     *
     * @param sr start row
     * @param er end row
     * @param sc start column
     * @param ec end column
     * @return if repeater overlaps
     */
    public boolean checkRange(int sr, int er, int sc, int ec) {
        return myRepeaters.checkRange(sr, er, sc, ec);
    }

    public void clearCellList(Collection<? extends CellDef> cells) {
        for (int r = 0; r < rowCount; r++) {
            ArrayList<CellDef> row = myRows.get(r);
            for (int c = 0; c < colCount; c++) {
                CellDef cd = row.get(c);
                if (cd == null)
                    continue;
                if (cells.contains(cd)) {
                    setCellAtInternal(null, r, c);
                }
            }
        }
    }

    public boolean setStyle(int sr, int sc, int er, int ec, CellStyle s, boolean dry) {
        setStyleSimple(sr, sc, er, ec, s);
        return false;
		/* check span collision here is to late
				int h = 1;
				int v = 1;
				if (s != null)
				{
					h = s.calcSpanHorizontal();
					v = s.calcSpanVertical();
				}
				if (h == 1 && v == 1)// no conflict possible
				{
					if (!dry)
						setStyleSimple(sr, sc, er, ec, s);
					return false;
				}
				int r = sr;
				int offset = 1; // skip the top left cell. (its the one we keep)
				CellDef cover = null;
				CoveredCellDef ccd = null;
				while (r <= er)
				{
					int mr = Math.min(r + v, rowCount);
					for (int i = r; i < mr; i++)
					{
						Vector row = (Vector) myRows.get(i);
						int c = sc;
						while (c <= ec)
						{
							if (!dry)
							{
								if (i == r)
									cover = (CellDef) row.get(c);
								if (cover == null || cover instanceof CoveredCellDef)
								{
									cover = new FreeCellDef(this);
									setCellAtInternal(cover, r, c);
								}
								cover.setStyle(CellStyle.duplicateLocal(s));
								ccd = new CoveredCellDef(this, cover);
								ccd.setRepeater(myRepeaters.innerMostRepeaterAt(r,c));
							}
							int mc = Math.min(c + h, colCount);
							CellDef lcd = (CellDef) row.get(c);
							if (lcd != null)
							{
								Repeater lr = lcd.getRepeater();
								if (lr != null)
								{
									mc = Math.min(mc, lr.getEndCol() + 1);
								}
							}
							for (int j = c + offset; j < mc; j++)
							{
								if (dry)
								{
									CellDef cd = (CellDef) row.get(j);
									if (cd != null && cd.getClass() != FreeCellDef.class && !(cd instanceof CoveredCellDef))
										return true;
								}
								else
								{
									row.set(j, ccd);
								}
							}
							c += h;
							offset = 0;
						}
					}
					r += v;
				}
				return false;
				*/
    }

    protected void setStyleSimple(int sr, int sc, int er, int ec, CellStyle s) {
        for (int r = sr; r <= er; r++) {
            ArrayList<CellDef> row = myRows.get(r);
            for (int c = sc; c <= ec; c++) {
                CellDef cd = row.get(c);
                if (getCoverCell(r, c) != null)
                    continue;
                if (cd == null) {
                    Repeater rep = myRepeaters.innerMostRepeaterAt(r, c);
                    if (rep != null || s != null) {
                        cd = new FreeCellDef(this, rep);
                        setCellAtInternal(cd, r, c);
                    }
                }
                if (cd != null)
                    cd.setStyle(CellStyle.duplicateLocal(s));
            }
        }
    }

    public TemplateLayouter getLayouter() {
        return myLayouter;
    }

    public void setLayouter(TemplateLayouter layouter) {
        myLayouter = layouter;
    }

    public GridSlice copy() throws JoriaUserException {
        return mslice(0, rowCount, 0, colCount, false);
    }

    public boolean isEmpty(int sr, int er, int sc, int ec) {
        for (int i = sr; i <= er; i++) {
            ArrayList<CellDef> r = myRows.get(i);
            for (int j = sc; j <= ec; j++) {
                CellDef cd = r.get(j);
                if (cd != null && cd.getClass() != FreeCellDef.class)
                    return false;
            }
        }
        return true;
    }

    public boolean isEmptyOrUnbound(int sr, int er, int sc, int ec) {
        er = Math.min(er, rowCount - 1);// ensure range does not exceed grid size. This may happen as the result of a span
        ec = Math.min(ec, colCount - 1);
        for (int i = sr; i <= er; i++) {
            ArrayList<CellDef> r = myRows.get(i);
            for (int j = sc; j <= ec; j++) {
                CellDef cd = r.get(j);
                if (cd != null) {
                    if (cd instanceof DataCellDef) {
                        DataCellDef dataCellDef = (DataCellDef) cd;
                        if (dataCellDef.getAccessor() instanceof UnboundAccess)
                            continue;
                        return false;
                    } else if (cd.getClass() != FreeCellDef.class)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * create a slice for moving a cell range. Leaves referenced totals alone
     *
     * @param sr  start row
     * @param er  end row
     * @param sc  start column
     * @param ec  end column
     * @param cut if the slice is removed
     * @return new slice
     * @throws JoriaUserException problem
     */
    public GridSlice mslice(int sr, int er, int sc, int ec, boolean cut) throws JoriaUserException {
        //String res = null;
        Trace.logDebug(Trace.dnd, "slice: " + sr + "/" + er + " " + sc + "/" + ec);
        GridSlice gs = new GridSlice(this, sr, er, sc, ec, cut);
        if (cut)
            fireChange("cut");//trdone
        return gs;
    }

    public void mpaste(CellLocation cp, GridSlice cells) throws JoriaUserException {
        int w = cells.cols;
        int h = cells.cells.length / cells.cols;
        if (cp.relative == CellLocation.LEFT) {
            insertCols(cp.col, w);
        } else if (cp.relative == CellLocation.RIGHT) {
            appendCols(cp.col, w);
            cp.col++;
        } else if (cp.relative == CellLocation.ABOVE) {
            insertRows(cp.row, h);
        } else if (cp.relative == CellLocation.BELOW || cp.relative == CellLocation.FARBELOW) {
            appRow(cp.row, h);
            cp.row++;
        } else if (cp.relative != CellLocation.CENTER)
            throw new JoriaAssertionError("Invalid relative CellLocation: " + cp.relative);
        cells.paste(cp.model, cp.row, cp.col);
        myRepeaters.ownCells0(null);
        fireChange("paste");//trdone
    }

    public void restore(TemplateModel k) {
        rowCount = k.rowCount;
        colCount = k.colCount;
        myRows = k.myRows;
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> vector = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef def = vector.get(j);
                if (def != null) {
                    def.setGrid(this);
                }
            }
        }
        rowSizing = k.rowSizing;
        colSizing = k.rowSizing;
        myRepeaters = k.myRepeaters;
    }

    public void checkConsistency() {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> vector = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef def = vector.get(j);
                if (def != null) {
                    Repeater r = def.getRepeater();
                    Repeater r2 = getRepeaterAt(i, j);
                    Trace.check(r == r2, "Cell at " + i + "," + j + " points to wrong repeater");
                    if (def instanceof DataCellDef) {
						/*
						DataCellDef dcd = (DataCellDef) def;
						if (r != null)
							Trace.check(AbstractJoriaClass.isSameOrViewOrDerived(dcd.getValueGetter().getDefiningClass(),(JoriaClass)r.getRoot().getCollectionTypeAsserted().getElementType()),
								  "Repeated cell at " + i + "," + j + " has wrong type. Cell: " + dcd.getValueGetter().getDefiningClass() + " Repeater " + r.getRoot().getType()); //trdone
						else
							Trace.check(AbstractJoriaClass.isSameOrViewOrDerived(dcd.getValueGetter().getDefiningClass(), myContainer.getPageLevelParent().getRoot().getClassTypeAsserted()),
								  "Cell at " + i + "," + j + " has wrong type. Cell: " + dcd.getValueGetter().getDefiningClass() + " Model " + myContainer.getPageLevelParent().getRoot().getType()); //trdone
						*/
                    }
                }
            }
        }
    }

    public static final int NO_COLLISION = 0;
    public static final int NON_EMPTY_CELL_IN_SPAN = 1;
    public static final int SPAN_COLLISION = 2;
    public static final int SETTING_SPAN_ON_RANGE = 3;

    /*
            test if the span setting of a named style will cause a collision with a non empty cell
        */
    public int testSpanCollision(CellStyle oldStyle, int newHSpan, int newVSpan) {
        // non span extension -> no collision possible
        int oldHSpan = oldStyle.calcSpanHorizontal();
        int oldVSpan = oldStyle.calcSpanVertical();
        //if(oldHSpan <= newHSpan && oldVSpan <= newVSpan)
        //return NO_COLLISION;
        int maxColl = NO_COLLISION;
        outer:
        for (int r = 0; r < rowCount; r++) {
            ArrayList<CellDef> row = myRows.get(r);
            for (int c = 0; c < colCount; c++) {
                CellDef cd = row.get(c);
                if (cd == null || cd.getStyle() != oldStyle)
                    continue;
                int coll = testSpanOnCell(cd, r, c, oldHSpan, newHSpan, oldVSpan, newVSpan);
                maxColl = Math.max(maxColl, coll);
                if (coll > NON_EMPTY_CELL_IN_SPAN)
                    break outer;
            }
        }
        return maxColl;
    }

    /*
            test if the span setting of an unnamed style of a cell will cause a collision with a non empty cell
        */
    public int testSpanCollision(int r, int c, int newHSpan, int newVSpan) {
        CellDef cd = cellAt(r, c);
        int oldHSpan = 1;
        int oldVSpan = 1;
        if (cd != null) {
            oldHSpan = cd.getCascadedStyle().calcSpanHorizontal();
            oldVSpan = cd.getCascadedStyle().calcSpanVertical();
        }
        if (newHSpan < 1)
            newHSpan = oldHSpan;
        if (newVSpan < 1)
            newVSpan = oldVSpan;
        //if(oldHSpan <= newHSpan && oldVSpan <= newVSpan)
        //  return NO_COLLISION;
        return testSpanOnCell(cd, r, c, oldHSpan, newHSpan, oldVSpan, newVSpan);
    }

    /*
            test if the setting of a named style in a range will cause a collision with a non empty cell
        */
    public int testSpanCollision(int br, int er, int bc, int ec, CellStyle newStyle) {
        int newHSpan = newStyle.calcSpanHorizontal();
        int newVSpan = newStyle.calcSpanVertical();
        if (newHSpan > 0 || newVSpan > 0) {
            int maxColl = NO_COLLISION;
            outer:
            for (int ri = br; ri <= er; ri++) {
                for (int ci = bc; ci <= ec; ci++) {
                    CellDef cd = cellAt(ri, ci);
                    if (cd != null && cd.getClass() != FreeCellDef.class) {
                        int oldHSpan = cd.getCascadedStyle().calcSpanHorizontal();
                        int oldVSpan = cd.getCascadedStyle().calcSpanVertical();
                        int coll = testSpanOnCell(cd, ri, ci, oldHSpan, newHSpan, oldVSpan, newVSpan);
                        maxColl = Math.max(maxColl, coll);
                        if (coll > NON_EMPTY_CELL_IN_SPAN)
                            break outer;
                    }
                }
            }
            return maxColl;
        } else
            return NO_COLLISION;
    }

    protected int testSpanOnCell(CellDef cd, int r, int c, int oldHSpan, int newHSpan, int oldVSpan, int newVSpan) {
        // check for collision right of current span
        int maxColl = NO_COLLISION;
        if (cd == null)
            cd = new FreeCellDef(this, myRepeaters.innerMostRepeaterAt(r, c));
        newHSpan = cd.trimHSpan(newHSpan, r, c, newVSpan);
        newVSpan = cd.trimVSpan(newVSpan, r, c, newHSpan);
        for (int ri = r; ri < r + oldVSpan; ri++) {
            ArrayList<CellDef> row = myRows.get(ri);
            for (int ci = c + oldHSpan; ci < c + newHSpan; ci++) {
                CellDef test = getCoverCell(ri, ci);
                if (test != null && test != cd)
                    return SPAN_COLLISION;
                test = row.get(ci);
                if (test != null && test.getClass() != FreeCellDef.class)
                    maxColl = NON_EMPTY_CELL_IN_SPAN;
            }
        }
        // check for collision below of current span
        for (int ri = r + oldVSpan; ri < r + newVSpan; ri++) {
            ArrayList<CellDef> row = myRows.get(ri);
            for (int ci = c; ci < c + oldHSpan; ci++) {
                CellDef test = getCoverCell(ri, ci);
                if (test != null && test != cd)
                    return SPAN_COLLISION;
                test = row.get(ci);
                if (test != null && test.getClass() != FreeCellDef.class)
                    maxColl = NON_EMPTY_CELL_IN_SPAN;
            }
        }
        // check for collision right & below of current span
        for (int ri = r + oldVSpan; ri < r + newVSpan; ri++) {
            ArrayList<CellDef> row = myRows.get(ri);
            for (int ci = c + oldHSpan; ci < c + newHSpan; ci++) {
                CellDef test = getCoverCell(ri, ci);
                if (test != null && test != cd)
                    return SPAN_COLLISION;
                test = row.get(ci);
                if (test != null && test.getClass() != FreeCellDef.class)
                    maxColl = NON_EMPTY_CELL_IN_SPAN;
            }
        }
        // set new span in cascaded style to track collision between modified cells
        CellStyle cs = cd.getCascadedStyle();
        cs.setSpanHorizontal(newHSpan);
        cs.setSpanVertical(newVSpan);
        return maxColl;
    }

    /*
           get the cell, which covers the current one
       */
    public CoverInfo getCoverInfo(int r, int c) {
        if (r >= rowCount || c >= colCount)
            return null;
        for (int ri = 0; ri <= r; ri++) {
            ArrayList<CellDef> row = myRows.get(ri);
            for (int ci = 0; ci <= c; ci++) {
                if (ri == r && ci == c)
                    break;
                CellDef cd = row.get(ci);
                if (cd == null) {
                    continue;
                }
                CellStyle cs = cd.getCascadedStyle();
                int hSpan = cs.calcSpanHorizontal();
                int vSpan = cs.calcSpanVertical();
                if (ci + hSpan > c && ri + vSpan > r)
                    return new CoverInfo(cd, ri, ci);
            }
        }
        return null;
    }

    /*
           get the cell, which covers the current one
       */
    public CellDef getCoverCell(int r, int c) {
        if (r >= rowCount || c >= colCount)
            return null;
        for (int ri = 0; ri <= r; ri++) {
            ArrayList<CellDef> row = myRows.get(ri);
            for (int ci = 0; ci <= c; ci++) {
                if (ri == r && ci == c)
                    break;
                CellDef cd = row.get(ci);
                if (cd == null) {
                    continue;
                }
                CellStyle cs = cd.getCascadedStyle();
                int hSpan = cs.calcSpanHorizontal();
                int vSpan = cs.calcSpanVertical();
                if (ci + hSpan > c && ri + vSpan > r)
                    return cd;
            }
        }
        return null;
    }

    public Point getCellPosition(CellDef cd) {
        for (int r = 0; r < rowCount; r++) {
            ArrayList<CellDef> row = myRows.get(r);
            for (int c = 0; c < colCount; c++) {
                if (row.get(c) == cd)
                    return new Point(c, r);
            }
        }
        return null;
    }

    public TemplateModel duplicate(TemplateBox newContainer, final Map<Object, Object> copiedData) {
        TemplateModel t = new TemplateModel();
        t.rowCount = rowCount;
        t.colCount = colCount;
        t.myContainer = newContainer;
        t.myRows = new ArrayList<ArrayList<CellDef>>(rowCount);
        t.myRepeaters = RepeaterList.duplicate(myRepeaters, t, null, copiedData);
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> vector = myRows.get(i);
            ArrayList<CellDef> rv = new ArrayList<CellDef>(colCount);
            t.myRows.add(rv);
            for (int j = 0; j < colCount; j++) {
                CellDef def = vector.get(j);
                if (def != null) {
                    CellDef dup = def.duplicate(t, copiedData);
                    rv.add(dup);
                    copiedData.put(def, dup);
                } else
                    rv.add(null);
            }
        }
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> vector = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef def = vector.get(j);
                if (def instanceof RelatedCell) {
                    RelatedCell oFrom = (RelatedCell) def;
                    CellDef oTo = oFrom.getDest();
                    if (oTo != null) {
                        RelatedCell nFrom = (RelatedCell) copiedData.get(oFrom);
                        Trace.check(nFrom, oFrom.getClass());
                        CellDef nTo = (CellDef) copiedData.get(oTo);
                        nFrom.setDest(nTo);
                    } else
                        Trace.logError("Related cell missing for " + def.getClass().getName() + " row:" + i + " col:" + j);
                }
            }
        }
        t.rowSizing = new ArrayList<FlexSize>(rowCount);
        for (int r = 0; r < rowCount; r++) {
            FlexSize s = rowSizing.get(r);
            t.rowSizing.add(s);     // FlexSize is immutable: reuse it
        }
        t.colSizing = new ArrayList<FlexSize>(colCount);
        for (int c = 0; c < colSizing.size(); c++) {
            FlexSize size = colSizing.get(c);
            t.colSizing.add(size);         // FlexSize is immutable: reuse it
        }
        t.getRepeaterList().ownCells0(copiedData);
        if (horizontalBorders != null) {
            t.horizontalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(horizontalBorders.size());
            for (int i = 0; i < horizontalBorders.size(); i++) {
                ArrayList<JoriaSimpleBorder> oldBorders = horizontalBorders.get(i);
                if (oldBorders != null) {
                    ArrayList<JoriaSimpleBorder> newBorders = new ArrayList<JoriaSimpleBorder>(oldBorders.size());
                    newBorders.addAll(oldBorders);   // cell borders are immutable they can be copied without duplication
					/*
						 for (int j = 0; j < oldBorders.size(); j++)
						 {
							 JoriaSimpleBorder joriaSimpleBorder = oldBorders.get(j);
							 final JoriaSimpleBorder newBorder = JoriaSimpleBorder.copy(joriaSimpleBorder);
							 if (newBorder == null && joriaSimpleBorder != null)// happens if the border is empty (thickness = 0)
								 oldBorders.set(j, null);//(clean it away to prevent the undo checker from failing)
							 newBorders.add(newBorder);
						 }
	 */
                    t.horizontalBorders.add(newBorders);
                } else
                    t.horizontalBorders.add(null);
            }
        }
        if (verticalBorders != null) {
            t.verticalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(verticalBorders.size());
            for (int i = 0; i < verticalBorders.size(); i++) {
                ArrayList<JoriaSimpleBorder> oldVerticalBorders = verticalBorders.get(i);
                if (oldVerticalBorders != null) {
                    ArrayList<JoriaSimpleBorder> newBorders = new ArrayList<JoriaSimpleBorder>(oldVerticalBorders.size());
                    newBorders.addAll(oldVerticalBorders);
					/*
						 for (int j = 0; j < oldVerticalBorders.size(); j++)
						 {
							 JoriaSimpleBorder joriaSimpleBorder = oldVerticalBorders.get(j);
							 final JoriaSimpleBorder newBorder = JoriaSimpleBorder.copy(joriaSimpleBorder);
							 if (newBorder == null && joriaSimpleBorder != null)// happens if the border is empty (thickness = 0)
								 oldVerticalBorders.set(j, null);//(clean it away to prevent the undo checker from failing)
							 newBorders.add(newBorder);
						 }
	 */
                    t.verticalBorders.add(newBorders);
                } else
                    t.verticalBorders.add(null);
            }
        }
        return t;
    }

    public void fixAccess() {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof DataCellDef) {
                    ((DataCellDef) cd).fixAccess();
                } else if (cd instanceof NestingCellDef) {
                    ((NestingCellDef) cd).fixAccess();
                } else if (cd instanceof SummaryCell) {
                    final SummaryCell summaryCell = ((SummaryCell) cd);
                    if (!summaryCell.fixAccess()) {
                        String myText = Res.strp("deactivated", summaryCell.getText());
                        cd = new LabelCell(this, myText);
                        CellStyle ps = new CellStyle();
                        ps.setBaseStyle(summaryCell.getStyle());
                        ps.setBackground(Color.orange);
                        cd.setStyle(ps);
                        Env.instance().repo().logFix(Res.str("Summary_Field"), summaryCell.getAggregateDef(), Res.str("Aggregated_field_deactivated"));
                    }
                }
            }
        }
        myRepeaters.fixAccess();
    }

    public void namedTableStyleChanged(TableStyle ts) {
        myRepeaters.namedTableStyleChanged(ts);
    }

    protected void collectVariables(Set<RuntimeParameter> v, Set<Object> seen) {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColCount(); j++) {
                CellDef cd = cellAt(i, j);
                if (cd == null)
                    continue;
                String vc = cd.getVisibilityCondition();
                if (vc != null) {
                    Set<RuntimeParameter> vcv = ((FreeCellDef) cd).getVisibilityConditionVars();
                    RuntimeParameter.addAll(v, vcv, seen);
                }
                if (cd instanceof CellWithVariables) {
                    CellWithVariables dcd = (CellWithVariables) cd;
                    dcd.collectVariables(v, seen);
                }
            }
        }
        myRepeaters.collectFilterVariables(v, seen);
        if (crosstab != null && crosstab.collection instanceof VariableProvider)
            ((VariableProvider) crosstab.collection).collectVariables(v, seen);
    }

    public void reloadStyles() {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd == null)
                    continue;
                CellStyle cs = cd.getStyle();
                if (cs != null && cs.getName() != null)
                    cd.setStyle(Env.instance().repo().cellStyles.find(cs.getName()));
                if (cd instanceof NestingCellDef) {
                    ((NestingCellDef) cd).reloadStyles();
                }
            }
        }
    }

    public void addCol(boolean left, Repeater outsideOf) {
        addColsUnadjusted(1, colCount, 0, left);// append one col
        Repeater r = outsideOf.getOuterRepeater();
        if (left) {
            shiftInRows(outsideOf.getStartRow() - outsideOf.getHeaderRows(), outsideOf.getEndRow(), outsideOf.getStartCol(), outsideOf.getEndCol(), 1, r);
            outsideOf.shift(0, 1);
        }
        while (r != null) {
            r.endCol++;
            r = r.getOuterRepeater();
        }
        myRepeaters.ownCells0(null);
        fireChange("add col outside " + outsideOf);//trdone
    }

    protected void shiftInRows(int startRow, int endRow, int startCol, int endCol, int cols, Repeater r) {
        for (int i = startRow; i <= endRow; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = endCol; j <= startCol; j++) {
                row.set(j + cols, row.get(j));
            }
            if (r != null) {
                for (int k = startCol; k < startCol + cols; k++) {
                    row.set(k, new FreeCellDef(this, r));
                }
            } else {
                for (int k = startCol; k < startCol + cols; k++) {
                    row.set(k, null);
                }
            }
        }
    }

    protected void shiftInCols(int startRow, int endRow, int startCol, int endCol, int rows, Repeater r) {
        for (int i = endRow; i >= startRow; i--) {
            ArrayList<CellDef> from = myRows.get(i);
            ArrayList<CellDef> to = myRows.get(i + rows);
            for (int j = endCol; j >= startCol; j--) {
                to.set(j, from.get(j));
            }
        }
        if (r != null) {
            for (int k = startRow; k < startRow + rows; k++) {
                ArrayList<CellDef> row = myRows.get(k);
                for (int f = startCol; f <= endCol; f++) {
                    row.set(f, new FreeCellDef(this, r));
                }
            }
        } else {
            for (int k = startRow; k < startRow + rows; k++) {
                ArrayList<CellDef> row = myRows.get(k);
                for (int f = startCol; f <= endCol; f++) {
                    row.set(f, null);
                }
            }
        }
    }

    public RDRangeBase getColumnDefs() {
        //ArrayList extraFields = collectExtraFields();
        RDBase[][] dest = new RDBase[getRowCount()][];
        if (crosstab != null)
            return crosstab;
        final RDRangeBase ret = new RDRange(this, dest);
        for (int r = 0; r < rowCount; r++) {
            ArrayList<RDBase> row = new ArrayList<RDBase>(getColCount());
            int rdc = 0;// the number of cols covered by the repater definitions in the line above that we refer to
            for (int c = 0; c < colCount; c++) {
                Repeater inner = myRepeaters.getRepeaterHere(r, c);
                if (inner != null) {
                    RDBase ri;
                    int ec = inner.getEndCol();
                    if (r == inner.startRow) {
                        ri = inner.getColumnDefs();
                    } else
                        ri = new RDRepeaterNext(dest[r - 1][c - rdc]);// using c fails if there is more than one Repeater at the same level. then c does not point to the column of the repeater def in the line above
                    c = ec;
                    row.add(ri);
                    rdc = inner.getEndCol() - inner.getStartCol();
                } else {
                    CellDef cd = cellAt(r, c);
                    if (cd instanceof NestingCellDef)
                        row.add(((NestingCellDef) cd).getColumnDefs());
                    else
                        row.add(cd);
                }
            }
            dest[r] = row.toArray(new RDBase[row.size()]);
        }
        return ret;
    }

    public int getEndCol() {
        return colCount - 1;
    }

    public int getEndRow() {
        return rowCount - 1;
    }

    public int getStartCol() {
        return 0;
    }

    public int getStartRow() {
        return 0;
    }

    public JoriaAccess getAccessor() {
        return myContainer.getPageLevelParent().getRoot();
    }

    public void makeUnbound() {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd != null)
                    cd.makeUnboundCell();
            }
        }
        myRepeaters.makeUnbound();
    }

    public void collectI18nKeys(HashSet<String> keySet) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd != null) {
                    cd.collectI18nKeys(keySet);
                }
            }
        }
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> map) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd != null) {
                    cd.collectI18nKeys2(map);
                }
            }
        }
    }

    public void collectExternalFiles(ExternalFileUsage dest, Template r) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd != null && cd instanceof PictureCellDef) {
                    PictureCellDef pcd = (PictureCellDef) cd;
                    dest.add(pcd.getPictureFileName(), ExternalFileUsage.icon, r.getName());
                }
            }
        }
    }

    public RDCrossTab getCrosstab() {
        return crosstab;
    }

    public void setCrosstab(RDCrossTab crosstab) {
        this.crosstab = crosstab;
    }

    public void fixVisibiltyCondition() {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof FreeCellDef) {
                    FreeCellDef fcd = (FreeCellDef) cd;
                    fcd.checkVisibilityCondition();
                }
            }
        }
    }

    public void makeFilenameRelative(JoriaFileService fs) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof PictureCellDef) {
                    PictureCellDef pcd = (PictureCellDef) cd;
                    pcd.makeFilenameRelative(fs);
                } else if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    ncd.getInnerBox().makeFilenameRelative(fs);
                } else if (cd != null) {
                    final CellStyle style = cd.getStyle();
                    if (style != null)
                        style.makeFileName(fs, true);
                }
            }
        }
    }

    public void makeFilenameAbsolute(JoriaFileService fs) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof PictureCellDef) {
                    PictureCellDef pcd = (PictureCellDef) cd;
                    pcd.makeFilenameAbsolute(fs);
                } else if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    ncd.getInnerBox().makeFilenameAbsolute(fs);
                } else if (cd != null) {
                    final CellStyle style = cd.getStyle();
                    if (style != null)
                        style.makeFileName(fs, false);
                }
            }
        }
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd != null) {
                    cd.getUsedAccessors(s);
                }
            }
        }
        myRepeaters.getUsedAccessors(s);
    }

    public void getAllReferencedFiles(HashSet<String> ret) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof PictureCellDef) {
                    PictureCellDef pcd = (PictureCellDef) cd;
                    pcd.getAllReferencedFiles(ret);
                } else if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    ncd.getInnerBox().getTemplate().getAllReferencedFiles(ret);
                }
            }
        }
    }

    public boolean visitCells(CellVisitor vis) {
        boolean b = true;
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                b &= vis.visit(cd, i, j);
                if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    b &= ncd.getInnerBox().getTemplate().visitCells(vis);
                }
            }
        }
        return b;
    }

    public boolean visitRepeaters(RepeaterVisitor repVisistor) {
        boolean b = myRepeaters.visit(repVisistor);
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof NestingCellDef) {
                    NestingCellDef ncd = (NestingCellDef) cd;
                    b = b && ncd.getInnerBox().getTemplate().visitRepeaters(repVisistor);
                }
            }
        }
        return b;
    }

    public CellDef getCellAt(int r, int c) {
        return getRow(r).get(c);
    }

    protected Object readResolve() {
        if (horizontalBorders == null) {
            horizontalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(rowCount + 1);
            verticalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(rowCount);
        }
        return this;
    }

    public ArrayList<ArrayList<JoriaSimpleBorder>> getHorizontalBorders() {
        return horizontalBorders;
    }

    public ArrayList<ArrayList<JoriaSimpleBorder>> getVerticalBorders() {
        return verticalBorders;
    }

    public JoriaSimpleBorder getBorderAt(int r, int c, boolean horizontal, boolean lefttop_vs_rightbottom) {
        //final int off = lefttop_vs_rightbottom ? 0 : 1;
        if (horizontal) {
            r = r + (lefttop_vs_rightbottom ? 0 : 1);
            if (horizontalBorders == null || c >= horizontalBorders.size())
                return JoriaSimpleBorder.NULL;
            final ArrayList<JoriaSimpleBorder> row = horizontalBorders.get(c);
            if (row == null || r >= row.size())
                return JoriaSimpleBorder.NULL;
            JoriaSimpleBorder joriaSimpleBorder = row.get(r);
            return joriaSimpleBorder != null ? joriaSimpleBorder : JoriaSimpleBorder.NULL;
        } else {
            c = c + (lefttop_vs_rightbottom ? 0 : 1);
            if (verticalBorders == null || r >= verticalBorders.size())
                return JoriaSimpleBorder.NULL;
            final ArrayList<JoriaSimpleBorder> col = verticalBorders.get(r);
            if (col == null || c >= col.size())
                return JoriaSimpleBorder.NULL;
            JoriaSimpleBorder joriaSimpleBorder = col.get(c);
            return joriaSimpleBorder != null ? joriaSimpleBorder : JoriaSimpleBorder.NULL;
        }
    }

    public boolean isFirstRow(int r) {
        return r == 0;
    }

    public boolean isLastRow(int r) {
        return r == rowCount - 1;
    }

    public boolean isFirstCol(int c) {
        return c == 0;
    }

    public boolean isLastCol(int c) {
        return c == colCount - 1;
    }

    public void setBorderAt(int r, int c, JoriaSimpleBorder border, boolean horizontal, boolean lefttop_vs_rightbottom) {
        if (getBorderAt(r, c, horizontal, lefttop_vs_rightbottom) == border)
            return;
        final int off = lefttop_vs_rightbottom ? 0 : 1;
        if (horizontal) {
            if (horizontalBorders == null)
                horizontalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(c + 1);
            setBorder1(horizontalBorders, c, r + off, border);
        } else {
            if (verticalBorders == null)
                verticalBorders = new ArrayList<ArrayList<JoriaSimpleBorder>>(r + 1);
            setBorder1(verticalBorders, r, c + off, border);
        }
        fireChange("set border" + r + "/" + c + border);//trdone
    }

    private void setBorder1(ArrayList<ArrayList<JoriaSimpleBorder>> l, int d1, int d2, JoriaSimpleBorder border) {
        if (d1 >= l.size()) {
            while (d1 > l.size()) {
                l.add(null);
            }
            l.add(new ArrayList<JoriaSimpleBorder>());
        }
        ArrayList<JoriaSimpleBorder> k = l.get(d1);
        if (k == null) {
            k = new ArrayList<JoriaSimpleBorder>(d2);
            l.set(d1, k);
        }
        if (k.size() <= d2) {
            while (d2 > k.size()) {
                k.add(null);
            }
            k.add(border);
        } else
            k.set(d2, border);
    }

    public void upgradeBorders(List<Template> problematicTemplates, Template template) {
        float[] colBorders = new float[colCount];
        float[] rowBorders = new float[rowCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                CellDef cd = getCellAt(r, c);
                if (cd != null) {
                    Repeater headedRepeater = cd.getHeadedRepeater();
                    TableBorder tbHeaded = null;
                    if (headedRepeater != null) {
                        TableStyle tableStyle = headedRepeater.getCascadedTableStyle();
                        tbHeaded = tableStyle.getTableBorderForUpgrade();
                    }
                    Repeater repeater = cd.getRepeater();
                    TableBorder tb = null;
                    if (repeater != null) {
                        TableStyle tableStyle = repeater.getCascadedTableStyle();
                        tb = tableStyle.getTableBorderForUpgrade();
                    }
                    CellStyle cs = cd.getCascadedStyle();
                    CellBorder cb = cs.getBorderForUpgrade();
                    if (cd.getStyle() != null && !cd.getStyle().hasName())
                        cd.getStyle().nullBorder();
                    JoriaSimpleBorder top = getBorderAt(r, c, true, true);
                    JoriaSimpleBorder left = getBorderAt(r, c, false, true);
                    JoriaSimpleBorder right = getBorderAt(r, c + cs.getSpanHorizontal() - 1, false, false);
                    JoriaSimpleBorder bottom = getBorderAt(r + cs.getSpanVertical() - 1, c, true, false);
                    if (cb != null) {
                        JoriaSimpleBorder cbTop = null;
                        if (cb.getTop() != null && cb.getTop().getLineStyle() != JoriaSimpleBorder.NONE)
                            cbTop = new JoriaSimpleBorder(cb.getTop().getLineStyle(), cb.getTop().getThickness(), cb.getTop().getColor());
                        if (JoriaSimpleBorder.isNull(cbTop))
                            cbTop = null;
                        if (cbTop != null) {
                            if (top != null && !top.equals(cbTop))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            top = cbTop;
                        }
                        JoriaSimpleBorder cbLeft = null;
                        if (cb.getLeft() != null && cb.getLeft().getLineStyle() != JoriaSimpleBorder.NONE)
                            cbLeft = new JoriaSimpleBorder(cb.getLeft().getLineStyle(), cb.getLeft().getThickness(), cb.getLeft().getColor());
                        if (JoriaSimpleBorder.isNull(cbLeft))
                            cbLeft = null;
                        if (cbLeft != null) {
                            if (left != null && !left.equals(cbLeft))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            left = cbLeft;
                        }
                        JoriaSimpleBorder cbRight = null;
                        if (cb.getRight() != null && cb.getRight().getLineStyle() != JoriaSimpleBorder.NONE)
                            cbRight = new JoriaSimpleBorder(cb.getRight().getLineStyle(), cb.getRight().getThickness(), cb.getRight().getColor());
                        if (JoriaSimpleBorder.isNull(cbRight))
                            cbRight = null;
                        if (cbRight != null) {
                            if (right != null && !right.equals(cbRight))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            right = cbRight;
                        }
                        JoriaSimpleBorder cbBottom = null;
                        if (cb.getBottom() != null && cb.getBottom().getLineStyle() != JoriaSimpleBorder.NONE)
                            cbBottom = new JoriaSimpleBorder(cb.getBottom().getLineStyle(), cb.getBottom().getThickness(), cb.getBottom().getColor());
                        if (JoriaSimpleBorder.isNull(cbBottom))
                            cbBottom = null;
                        if (cbBottom != null) {
                            if (bottom != null && !bottom.equals(cbBottom))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            bottom = cbBottom;
                        }
                    }
                    if (tb != null) {
                        JoriaSimpleBorder tbTop;
                        if (repeater.getStartRow() == r)
                            tbTop = new JoriaSimpleBorder(tb.neckStyle, tb.neck, tb.neckColor);
                        else
                            tbTop = new JoriaSimpleBorder(tb.hInsideBodyStyle, tb.hInsideBody, tb.hInsideBodyColor);
                        if (JoriaSimpleBorder.isNull(tbTop))
                            tbTop = null;
                        if (tbTop != null) {
                            if (top != null && !top.equals(tbTop))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            top = tbTop;
                        }
                        JoriaSimpleBorder tbLeft;
                        if (repeater.getStartCol() == c)
                            tbLeft = new JoriaSimpleBorder(tb.leftBodyStyle, tb.leftBody, tb.leftBodyColor);
                        else
                            tbLeft = new JoriaSimpleBorder(tb.vInsideBodyStyle, tb.vInsideBody, tb.vInsideBodyColor);
                        if (JoriaSimpleBorder.isNull(tbLeft))
                            tbLeft = null;
                        if (tbLeft != null) {
                            if (left != null && !left.equals(tbLeft))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            left = tbLeft;
                        }
                        JoriaSimpleBorder tbRight;
                        if (repeater.getEndCol() == c + cs.getSpanHorizontal() - 1)
                            tbRight = new JoriaSimpleBorder(tb.rightBodyStyle, tb.rightBody, tb.rightBodyColor);
                        else
                            tbRight = new JoriaSimpleBorder(tb.vInsideBodyStyle, tb.vInsideBody, tb.vInsideBodyColor);
                        if (JoriaSimpleBorder.isNull(tbRight))
                            tbRight = null;
                        if (tbRight != null) {
                            if (right != null && !right.equals(tbRight))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            right = tbRight;
                        }
                        JoriaSimpleBorder tbBottom;
                        if (repeater.getEndRow() == r + cs.getSpanVertical() - 1)
                            tbBottom = new JoriaSimpleBorder(tb.bottomStyle, tb.bottom, tb.bottomColor);
                        else
                            tbBottom = new JoriaSimpleBorder(tb.hInsideBodyStyle, tb.hInsideBody, tb.hInsideBodyColor);
                        if (JoriaSimpleBorder.isNull(tbBottom))
                            tbBottom = null;
                        if (tbBottom != null) {
                            if (bottom != null && !bottom.equals(tbBottom))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            bottom = tbBottom;
                        }
                    }
                    if (tbHeaded != null) {
                        JoriaSimpleBorder tbhTop;
                        if (headedRepeater.getStartRow() - headedRepeater.getHeaderRows() == r)
                            tbhTop = new JoriaSimpleBorder(tbHeaded.topStyle, tbHeaded.top, tbHeaded.topColor);
                        else
                            tbhTop = new JoriaSimpleBorder(tbHeaded.hInsideHeaderStyle, tbHeaded.hInsideHeader, tbHeaded.hInsideHeaderColor);
                        if (JoriaSimpleBorder.isNull(tbhTop))
                            tbhTop = null;
                        if (tbhTop != null) {
                            if (top != null && !top.equals(tbhTop))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            top = tbhTop;
                        }
                        JoriaSimpleBorder tbhLeft;
                        if (headedRepeater.getStartCol() == c)
                            tbhLeft = new JoriaSimpleBorder(tbHeaded.leftHeaderStyle, tbHeaded.leftHeader, tbHeaded.leftHeaderColor);
                        else
                            tbhLeft = new JoriaSimpleBorder(tbHeaded.vInsideHeaderStyle, tbHeaded.vInsideHeader, tbHeaded.vInsideHeaderColor);
                        if (JoriaSimpleBorder.isNull(tbhLeft))
                            tbhLeft = null;
                        if (tbhLeft != null) {
                            if (left != null && !left.equals(tbhLeft))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            left = tbhLeft;
                        }
                        JoriaSimpleBorder tbhRight;
                        if (headedRepeater.getEndCol() == c + cs.getSpanHorizontal() - 1)
                            tbhRight = new JoriaSimpleBorder(tbHeaded.rightHeaderStyle, tbHeaded.rightHeader, tbHeaded.rightHeaderColor);
                        else
                            tbhRight = new JoriaSimpleBorder(tbHeaded.vInsideHeaderStyle, tbHeaded.vInsideHeader, tbHeaded.vInsideHeaderColor);
                        if (JoriaSimpleBorder.isNull(tbhRight))
                            tbhRight = null;
                        if (tbhRight != null) {
                            if (right != null && !right.equals(tbhRight))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            right = tbhRight;
                        }
                        JoriaSimpleBorder tbhBottom;
                        if (headedRepeater.getStartRow() - 1 == r + cs.getSpanVertical() - 1)
                            tbhBottom = new JoriaSimpleBorder(tbHeaded.neckStyle, tbHeaded.neck, tbHeaded.neckColor);
                        else
                            tbhBottom = new JoriaSimpleBorder(tbHeaded.hInsideHeaderStyle, tbHeaded.hInsideHeader, tbHeaded.hInsideHeaderColor);
                        if (JoriaSimpleBorder.isNull(tbhBottom))
                            tbhBottom = null;
                        if (tbhBottom != null) {
                            if (bottom != null && !bottom.equals(tbhBottom))
                                if (!problematicTemplates.contains(template))
                                    problematicTemplates.add(template);
                            bottom = tbhBottom;
                        }
                    }
                    setBorderAt(r, c, top, true, true);
                    setBorderAt(r, c, left, false, true);
                    setBorderAt(r, c + cs.getSpanHorizontal() - 1, right, false, false);
                    setBorderAt(r + cs.getSpanVertical() - 1, c, bottom, true, false);
                    if (cs.getSpanHorizontal() > 1) {
                        colBorders[c] = Math.max(colBorders[c], left.getThickness());
                        colBorders[c + cs.getSpanHorizontal() - 1] = Math.max(colBorders[c + cs.getSpanHorizontal() - 1], right.getThickness());
                    } else {
                        colBorders[c] = Math.max(colBorders[c], left.getThickness() + right.getThickness());
                    }
                    if (cs.getSpanVertical() > 1) {
                        rowBorders[r] = Math.max(rowBorders[r], top.getThickness());
                        rowBorders[r + cs.getSpanVertical() - 1] = Math.max(rowBorders[r + cs.getSpanVertical() - 1], bottom.getThickness());
                    } else {
                        rowBorders[r] = Math.max(rowBorders[r], top.getThickness() + bottom.getThickness());
                    }
                }
            }
        }
        for (int i = 0; i < rowBorders.length; i++) {
            float rowBorder = rowBorders[i];
            FlexSize rowSize = rowSizing.get(i);
            if (!rowSize.isExpandable()) {
                rowSizing.set(i, new FlexSize(rowSize.getVal() + rowBorder / FlexSize.factors[rowSize.getUnit()], rowSize.getUnit()));
            }
        }
        for (int i = 0; i < colBorders.length; i++) {
            float colBorder = colBorders[i];
            FlexSize colSize = colSizing.get(i);
            if (!colSize.isExpandable()) {
                colSizing.set(i, new FlexSize(colSize.getVal() + colBorder / FlexSize.factors[colSize.getUnit()], colSize.getUnit()));
            }
        }
    }

    public boolean isCrosstabFrame() {
        return crosstab != null;
    }

    public void rebindByName(final JoriaClass newScope) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd instanceof DataCell) {
                    ((DataCell) cd).rebindByName(newScope);
                }
            }
        }
        myRepeaters.rebindByName(newScope);
    }

    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        for (int i = 0; i < rowCount; i++) {
            ArrayList<CellDef> row = myRows.get(i);
            for (int j = 0; j < colCount; j++) {
                CellDef cd = row.get(j);
                if (cd == null)
                    continue;
                final String s = cd.getVisibilityCondition();
                if (s != null) {
                    try {
                        final Set<JoriaAccess> vars = ((FreeCellDef) cd).getVisibilityConditionUsedAccessors();
                        for (JoriaAccess axs : vars) {
                            if (seen.contains(axs))
                                continue;
                            if (!visitor.visit(axs))
                                return false;
                            if (axs instanceof VisitableAccess) {
                                if (!((VisitableAccess) axs).visitAllAccesses(visitor, seen))
                                    return false;
                            }
                        }
                    } catch (OQLParseException ex) {
                        Trace.logError("getVisibilityConditionUsedAccessors: " + ex.getMessage() + " at " + ex.pos + " in " + ex.query);
                        //ex.printStackTrace();
                        if (visitor.stopAccessSearchOnError())
                            throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
                    }
                }
                if (cd instanceof CellWithVariables) {
                    if (!((CellWithVariables) cd).visitAccesses(visitor, seen))
                        return false;
                }
            }
        }
        return myRepeaters.visitAccesses(visitor, seen);
    }

    public void cleanupRowColSizing(Map<FlexSize, FlexSize> seen) {
        cleanUpSizingList(seen, rowSizing);
        cleanUpSizingList(seen, colSizing);
    }

    private void cleanUpSizingList(final Map<FlexSize, FlexSize> seen, List<FlexSize> sizing) {
        for (int i = 0; i < sizing.size(); i++) {
            FlexSize flexSize = sizing.get(i);
            if (flexSize == null)
                continue;
            final FlexSize consolidated = seen.get(flexSize);
            if (consolidated != null)
                sizing.set(i, consolidated);
            else
                seen.put(flexSize, flexSize);
        }
    }
}
