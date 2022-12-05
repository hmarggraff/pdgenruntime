// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.*;
import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.CoveredCellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.GroupGrandTotalCell;
import org.pdgen.model.run.RDRepeater;
import org.pdgen.model.run.RDRepeaterNext;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.JoriaSimpleBorder;
import org.pdgen.model.style.TableStyle;
import org.pdgen.projection.UnboundAccess;
import org.pdgen.projection.UnboundMembersClass;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Repeater implements Serializable, ModelBase {
    private static final long serialVersionUID = 7L;
    protected int startRow;// occupied space in the container
    protected int endRow;
    protected int startCol;
    protected int endCol;
    /**
     * Number of header rows
     */
    protected int headerRows;
    /**
     * Number of footer rows
     */
    protected int footerRows;
    /**
     * index in my outer range
     * if outer range is null Inline box is at the top level and
     * index refers to the template model.
     */
    protected int myIndex;
    protected Repeater outerRepeater;
    protected TemplateModel container;
    protected JoriaAccess myData;
    protected TableStyle tableStyle;
    protected transient TableStyle cascadedTableStyle;
    protected RepeaterList myRepeaters;
    protected Template drillDown;
    protected ArrayList<GroupGrandTotalCell> totals;

    public Repeater duplicate(TemplateModel parent, Repeater newOuter, Map<Object, Object> copiedData) {
        JoriaAccess newData = (JoriaAccess) copiedData.get(myData);
        if (newData == null) {
            if (myData instanceof JoriaReportPrivateAccess) {
                newData = ((JoriaReportPrivateAccess) myData).copyReportPrivateAccess(copiedData);
            } else
                newData = myData;
        }
        Repeater r = new Repeater(startRow, startCol, endRow - startRow + 1, endCol - startCol + 1, parent, newData);
        r.headerRows = headerRows;
        r.footerRows = footerRows;
        r.myIndex = myIndex;
        r.outerRepeater = newOuter;
        r.tableStyle = TableStyle.duplicateLocal(tableStyle);
        r.myRepeaters = RepeaterList.duplicate(myRepeaters, parent, r, copiedData);
        return r;
    }

    public Repeater(int sRow, int sCol, int rows, int cols, TemplateModel container, JoriaAccess data) {
        Trace.check(container == null || sRow + rows <= container.getRowCount());
        Trace.check(container == null || sCol + cols <= container.getColCount());
        this.container = container;
        myRepeaters = new RepeaterList(container);
        Trace.check(data.getType().isCollection(), "Repeaters must be created with collections");
        myData = data;
        startRow = sRow;
        startCol = sCol;
        endRow = sRow + rows - 1;
        endCol = sCol + cols - 1;
    }

    public JoriaAccess getAccessor() {
        return myData;
    }

    public int getEndCol() {
        return endCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getHeight() {
        return endRow - startRow + 1;
    }

    public int getTopLevelIndex() {
        return myIndex;
    }

    public TemplateModel getModel() {
        return container;
    }

    public Repeater getOuterRepeater() {
        return outerRepeater;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public TableStyle getTableStyle() {
        return tableStyle;
    }

    public TableStyle getCascadedTableStyle() {
        if (cascadedTableStyle == null) {
            cascadedTableStyle = new TableStyle(tableStyle);
            if (outerRepeater != null)
                cascadedTableStyle.mergeFrom(outerRepeater.getCascadedTableStyle());
            else
                cascadedTableStyle.mergeDefaults();
        }
        return cascadedTableStyle;
    }

    public int getWidth() {
        return 1 + endCol - startCol;
    }

    public boolean isConflicting(int sr, int er, int sc, int ec) {
        if (sr > getEndRow() || er < getStartRow() || sc > getEndCol() || ec < getStartCol()) {
            return false;// not overlapping at all
        } else if (er <= getEndRow() && sr >= getStartRow() && ec <= getEndCol() && sc >= getStartCol())
            return false;// completely contained
        else
            return !(er >= getEndRow() && sr <= getStartRow() && ec >= getEndCol() && sc <= getStartCol());
    }

    public boolean isContained(int sr, int er, int sc, int ec) {
        return startRow >= sr && endRow <= er && startCol >= sc && endCol <= ec;
    }

    public boolean isContaining(int sr, int er, int sc, int ec) {
        return startRow <= sr && endRow >= er && startCol <= sc && endCol >= ec;
    }

    public boolean isEmpty(int sr, int er, int sc, int ec) {
        // required range must fit in model otherwise ArrayIndexOutOfBoundsException is thrown
        // TemplateModel m = getContainer();
        for (int r = sr; r <= er; r++) {
            for (int c = sc; c <= ec; c++) {
                if (!(container.cellAt(r, c) instanceof CoveredCellDef))
                    return false;
            }
        }
        return true;
    }

    public boolean isOverlapping(int sr, int er, int sc, int ec) {
        boolean t = sr >= getStartRow() && sr <= getEndRow();
        boolean b = er >= getStartRow() && er <= getEndRow();
        boolean l = sc >= getStartCol() && sc <= getEndCol();
        boolean r = ec >= getStartCol() && ec <= getEndCol();
        boolean colOverlap = r || l;
        boolean rowOverlap = b || t;
        // Trace.logDebug(4,"Overlap? r " + r+" l "+l+" b "+b+" t "+t+" row "+rowOverlap+" col "+colOverlap);
        return (rowOverlap && colOverlap);
    }

    public boolean isCrossing(int sr, int er, int sc, int ec) {
        boolean t = sr >= getStartRow() && sr <= getEndRow();
        boolean b = er >= getStartRow() && er <= getEndRow();
        boolean l = sc >= getStartCol() && sc <= getEndCol();
        boolean r = ec >= getStartCol() && ec <= getEndCol();
        return t && !b || b && !t || l && !r || r && !l;
    }

    public boolean isRange(int sr, int er, int sc, int ec) {
        return (er == getEndRow() && sr == getStartRow() && ec == getEndCol() && sc == getStartCol());
    }

    public void setFooterRows(int newFooterRows) {
        footerRows = newFooterRows;
    }

    public void setHeaderRows(int newHeaderRows) {
        headerRows = newHeaderRows;
    }

    public void setEndCol(int newEndCol) {
        endCol = newEndCol;
        container.getRepeaterList().ownCells0(null);
    }

    public void setEndRow(int newEndRow) {
        endRow = newEndRow;
        container.getRepeaterList().ownCells0(null);
    }

    public void setEndColUnchecked(int newEndCol) {
        endCol = newEndCol;
    }

    public void setEndRowUnchecked(int newEndRow) {
        endRow = newEndRow;
    }

    public void setIndex(int newIndex) {
        myIndex = newIndex;
    }

    public void setModel(TemplateModel newTemplateModel) {
        container = newTemplateModel;
    }

    public void setOuterRepeater(Repeater newOuterRepeater) {
        if (outerRepeater == newOuterRepeater)
            return;
        if (outerRepeater != null && !(outerRepeater.getAccessor() instanceof GroupingAccess)) {
            throw new JoriaAssertionError("cannot change outer range of a Repeater");
        }
        outerRepeater = newOuterRepeater;
        if (outerRepeater != null)
            outerRepeater.getRepeaterList().addRepeater(this);
        else
            getModel().addRepeaterUnchecked(this);
    }

    public void setStartColUnchecked(int newStartCol) {
        startCol = newStartCol;
    }

    public void setStartRowUnchecked(int newStartRow) {
        startRow = newStartRow;
    }

    public void setStartCol(int newStartCol) {
        Trace.check(endCol >= newStartCol);
        startCol = newStartCol;
        container.getRepeaterList().ownCells0(null);
    }

    public void setStartRow(int newStartRow) {
        Trace.check(endRow >= newStartRow);
        startRow = newStartRow;
        container.getRepeaterList().ownCells0(null);
    }

    public void setTableStyle(TableStyle newTableStyle) {
        cascadedTableStyle = null;
        tableStyle = newTableStyle;
        container.clearCachedStyles();
        getCascadedTableStyle();
    }

    public void namedTableStyleChanged(TableStyle ts) {
        cascadedTableStyle = null;
        myRepeaters.namedTableStyleChanged(ts);
    }

    public void nest() {
        if (outerRepeater != null && outerRepeater.getModel() == container) {
            outerRepeater.startCol = Math.min(startCol, outerRepeater.startCol);
            outerRepeater.endCol = Math.max(endCol, outerRepeater.endCol);
            outerRepeater.startRow = Math.min(startRow, outerRepeater.startRow);
            outerRepeater.endRow = Math.max(endRow, outerRepeater.endRow);
            outerRepeater.nest();
        }
    }

    public RepeaterList getRepeaterList() {
        return myRepeaters;
    }

    public int getHeaderRows() {
        return headerRows;
    }

    public int getFooterRows() {
        return footerRows;
    }

    public String toString() {
        return "Repeater " + myData.getLongName() + " at " + startRow + "/" + startCol + " -- " + endRow + "/" + endCol;//trdone
    }

    protected void shift(int rowDelta, int colDelta) {
        startRow += rowDelta;
        endRow += rowDelta;
        startCol += colDelta;
        endCol += colDelta;
        myRepeaters.shift(rowDelta, colDelta);
    }

    public RDRepeater getColumnDefs() {
        RDBase[][] dest = new RDBase[endRow - startRow + 1][];
        RDRepeater ret = new RDRepeater(this, dest);
        for (int r = startRow; r <= endRow; r++) {
            ArrayList<RDBase> row = new ArrayList<RDBase>(endCol - startCol + 1);
            int rdc = 0;// the number of cols covered by the repater definitions in the line above that we refer to
            for (int c = startCol; c <= endCol; c++) {
                Repeater inner = myRepeaters.getRepeaterHere(r, c);
                if (inner != null) {
                    RDBase ri;
                    if (r == inner.startRow)
                        ri = inner.getColumnDefs();
                    else
                        ri = new RDRepeaterNext(dest[r - startRow - 1][c - startCol - rdc]);// using c fails if there is more than one Repeater at the same level. then c does not point to the column of the repeater def in the line above
                    c = inner.getEndCol();
                    row.add(ri);
                    rdc = inner.getEndCol() - inner.getStartCol();
                } else {
                    CellDef cd = container.cellAt(r, c);
                    row.add(cd);
                }
            }
            dest[r - startRow] = row.toArray(new RDBase[row.size()]);
        }
        return ret;
    }

    public CellDef cellAt(int r, int c) {
        return container.cellAt(startRow + r, startCol + c);
    }

    public int getColCount() {
        return endCol - startCol + 1;
    }

    public Repeater getRepeaterAt(int sRow, int sCol) {
        return myRepeaters.getRepeaterHere(sRow, sCol);
    }

    public int getRepeaterCount() {
        return myRepeaters.length();
    }

    public int getRowCount() {
        return endRow - startRow + 1;
    }

    public FlexSize getColSizingAt(int at) {
        return container.getColSizingAt(at + startCol);
    }

    public FlexSize getRowSizingAt(int at) {
        return container.getRowSizingAt(at + startRow);
    }

    public RDCrossTab getCrosstab() {
        return null;
    }

    public JoriaSimpleBorder getBorderAt(int r, int c, boolean horizontal, boolean lefttop_vs_rightbottom) {
        return container.getBorderAt(r + startRow, c + startCol, horizontal, lefttop_vs_rightbottom);
    }

    public boolean isFirstRow(int r) {
        return container.isFirstRow(r + startRow);
    }

    public boolean isLastRow(int r) {
        return container.isLastRow(r + startRow);
    }

    public boolean isFirstCol(int c) {
        return container.isFirstCol(c + startCol);
    }

    public boolean isLastCol(int c) {
        return container.isLastCol(c + startCol);
    }

    public TemplateBoxInterface getFrame() {
        return container.getFrame();
    }

    public void makeUnbound() {
        myData = UnboundMembersClass.unboundCollection;
        myRepeaters.makeUnbound();
    }

    public void setAccessor(JoriaAccess newAccessor) {
        Trace.check(newAccessor.getType() == myData.getType(), "Cannot replace repeater accessor with one of a different type");//trdone
        myData = newAccessor;
    }

    /*
     * sets the accessor to a new value if the repeater was previously unbound
     *
     */
    public void bind(JoriaAccess newAccessor) {
        Trace.check(myData, UnboundAccess.class);
        //exp 030401 hmf was mydata = newaccessor failed when addind more members by d&d
        CollectionProjection ip = new CollectionProjection(newAccessor.getCollectionTypeAsserted());
        myData = new PickAccess(newAccessor, ip);
    }

    public void fixAccess() {
        if (myData == null)
            return;
        JoriaAccess fixed = myData.getPlaceHolderIfNeeded();
        if (fixed instanceof JoriaPlaceHolderAccess) {
            Env.instance().repo().logFix(Res.strb("Table_for"), myData, Res.stri("deactivated_"));
            makeUnbound();
            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    CellDef cd = container.cellAt(r, c);
                    if (cd instanceof DataCellDef) {
                        final DataCellDef dataCellDef = ((DataCellDef) cd);
                        dataCellDef.makeModified();
                    }
                }
            }
        }
        myRepeaters.fixAccess();
    }

    public void collectViewUsage(Map<MutableView, Set<Object>> viewUsageMap, Set<MutableView> visitedViews) {
        if (myRepeaters != null)
            myRepeaters.collectViewUsage(viewUsageMap, visitedViews);
        if (myData instanceof CollectUsedViewsAccess)
            ((CollectUsedViewsAccess) myData).collectViewUsage(viewUsageMap, visitedViews);
    }

    public Template getDrillDown() {
        return drillDown;
    }

    public void setDrillDown(Template drillDown) {
        this.drillDown = drillDown;
    }

    public void getUsedAccessors(Set<JoriaAccess> s) {
        s.add(myData);
        myRepeaters.getUsedAccessors(s);
    }

    public boolean visit(RepeaterVisitor repVisistor) {
        //noinspection SimplifiableIfStatement
        if (!repVisistor.visit(this))
            return false;
        else
            return myRepeaters.visit(repVisistor);
    }

    public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (!visitor.visit(myData))
            return false;
        if (myData instanceof VisitableAccess) {
            if (!((VisitableAccess) myData).visitAllAccesses(visitor, seen))
                return false;
        }
        return myRepeaters.visitAccesses(visitor, seen);
    }

    public int getStackDepth() {
        if (outerRepeater != null)
            return outerRepeater.getStackDepth() + 1;
        else
            return 1;
    }

    public void addGroupGrandTotalCell(GroupGrandTotalCell cell) {
        if (totals == null)
            totals = new ArrayList<GroupGrandTotalCell>();
        totals.add(cell);
    }

    public void removeGroupGrandTotalCell(GroupGrandTotalCell cell) {
        if (totals != null) {
            totals.remove(cell);
        }
    }

    public boolean hasGroupGrandTotalCell(GroupGrandTotalCell cell) {
        return totals != null && totals.contains(cell);
    }

    public ArrayList<GroupGrandTotalCell> getTotalCells() {
        return totals;
    }

    public void remove() {
        RepeaterList list;
        if (outerRepeater != null)
            list = outerRepeater.getRepeaterList();
        else
            list = container.getRepeaterList();
        list.remove(list.getIndex(this));
        if (totals != null) {
            ArrayList<GroupGrandTotalCell> tempTotals = new ArrayList<GroupGrandTotalCell>(totals);
            for (GroupGrandTotalCell groupGrandTotalCell : tempTotals) {
                Point pos = container.getCellPosition(groupGrandTotalCell);
                container.setCellAt(null, pos.y, pos.x);
            }
        }
    }

    public boolean isSharesHeaderWithOuter() {
        return outerRepeater != null && (headerRows == 0 && startRow == outerRepeater.startRow || headerRows != 0 && startRow - headerRows < outerRepeater.startRow);
    }

    public void rebindByName(final JoriaClass newScope) {
        final String pName = myData.getName();
        final JoriaAccess newAccess = newScope.findMemberIncludingSuperclass(pName);
        if (newAccess != null)
            myData = newAccess;
        else
            myData = UnboundMembersClass.unboundCollection;
        myRepeaters.rebindByName(newScope);
    }
}
