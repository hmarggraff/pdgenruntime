// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.*;
import org.pdgen.model.cells.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.util.BucketList;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

public class RDCrossTab extends RDRangeBase implements Serializable {
    private static final long serialVersionUID = 7L;
    JoriaAccess collection;
    CrosstabLabelCell[] xDims;
    CrosstabLabelCell[] yDims;
    CrosstabAggregate[] aggs;
    boolean doRowColSums;

    public RDCrossTab(TemplateModel model, JoriaAccess collection, CrosstabLabelCell[] xDims, CrosstabLabelCell[] yDims, CrosstabAggregate[] aggs, boolean doRowColSums) {
        super(model, buildFields(model));
        this.doRowColSums = doRowColSums;
        this.xDims = xDims;
        this.yDims = yDims;
        this.aggs = aggs;
        this.collection = collection;
    }

    public void rebuildFields() {
        fields = buildFields((TemplateModel) myModel);
    }

    static RDBase[][] buildFields(TemplateModel m) {
        final int rowCount = m.getRowCount();
        final int colCount = m.getColCount();
        RDBase[][] dest = new RDBase[rowCount][];
        //final int cols = endCol - startCol + 1;
        for (int r = 0; r < rowCount; r++) {
            CellDef[] row = new CellDef[colCount];
            dest[r] = row;
            for (int c = 0; c < colCount; c++) {
                row[c] = m.cellAt(r, c);
            }
        }
        return dest;
    }

    public RVAny buildRunValue(DBData from, OutputMode om, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        final RunEnvImpl env = om.getRunEnv();
        final DBCollection coll = (DBCollection) collection.getValue(from, collection, env);
        if (coll == null)
            return null;
        HashMap<DBData, CrosstabAggregator> colMap = new HashMap<DBData, CrosstabAggregator>();
        HashMap<DBData, CrosstabAggregator> rowMap = new HashMap<DBData, CrosstabAggregator>();
        BucketList<BucketList<CrosstabAggregator>> rows = new BucketList<BucketList<CrosstabAggregator>>();
        int valueRows = -1;
        int valueCols = -1;
        double[] tempAggVals = new double[aggs.length];
        final DBStringImpl nullKey = new DBStringImpl(null, "(null)");


        while (coll.next()) {
            HashMap<DBData, CrosstabAggregator> drill = colMap;
            DBObject f = coll.current();
            int colIndex = -1;
            double[] aggVals = aggVals(f, env, tempAggVals);
            boolean lastWasNew = false;
            for (CrosstabLabelCell xDim : xDims) {
                JoriaAccess dim = xDim.getAccessor();
                DBData k = dim.getValue(f, dim, env);
                if (k == null || k.isNull())
                    k = nullKey;
                CrosstabAggregator elems = drill.get(k);
                if (elems == null) {
                    if (!lastWasNew)
                        valueCols++;
                    elems = new CrosstabAggregator(aggs, true, valueCols);
                    drill.put(k, elems);
                    lastWasNew = true;
                } else
                    lastWasNew = false;
                elems.accumulate(aggVals);
                colIndex = elems.index;
                drill = elems.getElems();
            }
            lastWasNew = false;
            int rowIndex = -1;
            drill = rowMap;
            for (int i = 0; i < yDims.length; i++) {
                JoriaAccess dim = yDims[i].getAccessor();
                DBData k = dim.getValue(f, dim, env);
                if (k == null || k.isNull())
                    k = nullKey;
                CrosstabAggregator elems = drill.get(k);
                if (elems == null) {
                    if (!lastWasNew)
                        valueRows++;
                    elems = new CrosstabAggregator(aggs, i < xDims.length - 1, valueRows);
                    drill.put(k, elems);
                    lastWasNew = true;
                } else
                    lastWasNew = false;
                elems.accumulate(aggVals);
                rowIndex = elems.index;
                drill = elems.getElems();
            }
            BucketList<CrosstabAggregator> row;
            if (rows.size() <= rowIndex || (row = rows.getObject(rowIndex)) == null) {
                row = new BucketList<CrosstabAggregator>();
                rows.set(rowIndex, row);
            }

            CrosstabAggregator cta;
            if (row.size() <= colIndex || (cta = row.getObject(colIndex)) == null) {
                cta = new CrosstabAggregator(aggs, colIndex);
                row.set(colIndex, cta);
            }
            cta.accumulate(aggVals);
        }
        valueCols++;
        valueRows++;
        RVAny[][] fill = new RVAny[xDims.length + aggs.length + (doRowColSums ? xDims.length * aggs.length : 0)][];
        RVTemplate ret = new RVTemplate(fill);
        ret.setElementCount(valueRows);
        // Allocate space for header rows
        for (int r = 0; r < xDims.length; r++) {
            fill[r] = new RVAny[valueCols + yDims.length + (doRowColSums ? yDims.length : 0)];
        }
        // Allocate space for data rows
        for (int r = 0; r < aggs.length; r++) {
            RVStringCol[] row = new RVStringCol[valueCols + yDims.length * (doRowColSums ? 2 : 1)];
            for (int i = 0; i < row.length; i++) {
                row[i] = new RVStringCol(valueRows + (aggs.length * xDims.length * (doRowColSums ? 1 : 0)));
            }
            fill[r + xDims.length] = row;
        }
        if (doRowColSums) {
            for (int r = 0; r < aggs.length * xDims.length; r++) {
                fill[xDims.length + aggs.length + r] = new RVAny[valueCols + yDims.length * 2];
            }
        }
        int[] colSorter = new int[valueCols];
        int[] rowSorter = new int[valueRows];
        fillXHeaders(colMap, 0, yDims.length, fill, om.getRunEnv(), colSorter, g);
        fillYHeaders(rowMap, 0, 0, (RVStringCol[]) fill[xDims.length], om.getRunEnv(), rowSorter);
        for (int i = 0; i < aggs.length; i++) {
            fillCol((RVStringCol[]) fill[xDims.length + i], rows, colSorter, rowSorter, i, env);
            if (doRowColSums) {
                fillRowSums((RVStringCol[]) fill[xDims.length + i], rowMap, colSorter, rowSorter, i, 0, env);
                fillColSums(fill, colMap, colSorter, i, 0, om.getRunEnv(), g);
            }
        }
        fillCorners(fill, from, om, defs, outerVals, g);
        return ret;
    }

    private void fillCorners(RVAny[][] fill, DBData from, OutputMode om, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        for (int r = 0; r < myModel.getRowCount(); r++) {
            int targetRow = r;
            if (r > xDims.length)
                targetRow = fill.length - myModel.getRowCount() + r;
            RVAny[] dataRow = fill[targetRow];
            for (int c = 0; c < myModel.getColCount(); c++) {
                int targetCol = c;
                if (c > yDims.length)
                    targetCol = dataRow.length - myModel.getColCount() + c;
                CellDef cd = myModel.cellAt(r, c);
                if (cd != null && !(cd instanceof CrosstabCell)) {
                    RVAny data = cd.buildRunValue(from, om, defs, outerVals, g);
                    dataRow[targetCol] = data;
                }
            }
        }
    }

    private void fillColSums(RVAny[][] fill, HashMap<DBData, CrosstabAggregator> colMap, int[] colSorter, int aggIndex, int level, RunEnv env, Graphics2D g) {
        RVAny[] myFill = fill[xDims.length + aggs.length + (xDims.length - level - 1) * aggs.length + aggIndex];
        Collection<CrosstabAggregator> values = colMap.values();
        CellDef cd = (CellDef) fields[xDims.length + aggs.length + (xDims.length - level - 1) * aggs.length + aggIndex][yDims.length];
        Trace.check(cd, CrosstabSumCellDef.class);
        final CellStyle style = cd.getCascadedStyle();
        for (CrosstabAggregator crosstabAggregator : values) {
            int colIndex = -1;
            for (int i = 0; i < colSorter.length; i++) {
                if (colSorter[i] == crosstabAggregator.index)
                    colIndex = i;
            }
            double number = crosstabAggregator.get(aggIndex);
            myFill[colIndex + yDims.length] = new RVString(SimpleTextCellDef.wrapText(env.getPager().format(number, style), style, env.getLocale()), style, g);
            if (crosstabAggregator.elems != null && crosstabAggregator.elems.size() > 0) {
                fillColSums(fill, crosstabAggregator.elems, colSorter, aggIndex, level + 1, env, g);
            }

        }
    }

    private void fillRowSums(RVStringCol[] rvStringCols, HashMap<DBData, CrosstabAggregator> rowMap, int[] colSorter, int[] rowSorter, int aggIndex, int level, RunEnv env) {
        Collection<CrosstabAggregator> values = rowMap.values();
        CellDef cd = (CellDef) fields[xDims.length + aggIndex][yDims.length * 2 - level];
        Trace.check(cd, CrosstabSumCellDef.class);
        CellStyle style = cd.getCascadedStyle();
        for (CrosstabAggregator crosstabAggregator : values) {
            int rowIndex = -1;
            for (int i = 0; i < rowSorter.length; i++) {
                if (rowSorter[i] == crosstabAggregator.index)
                    rowIndex = i;
            }
            int colIndex = colSorter.length + yDims.length * 2 - level - 1;
            RVStringCol dataCol = rvStringCols[colIndex];
            double number = crosstabAggregator.get(aggIndex);
            dataCol.get()[rowIndex] = SimpleTextCellDef.wrapText(env.getPager().format(number, style), style, env.getLocale());
            if (crosstabAggregator.elems != null) {
                fillRowSums(rvStringCols, crosstabAggregator.elems, colSorter, rowSorter, aggIndex, level + 1, env);
            }
        }
    }

    private int fillXHeaders(HashMap<DBData, CrosstabAggregator> spreader, int row, int col, RVAny[][] fill, RunEnv env, int[] colSorter, Graphics2D g) throws JoriaDataException {
        final CellStyle style = xDims[row].getCascadedStyle();
        //noinspection unchecked
        final Map.Entry<DBData, CrosstabAggregator>[] entries = spreader.entrySet().toArray(new Map.Entry[spreader.size()]);
        Arrays.sort(entries, RDCrossTab.ctKeyComparator);
        for (Map.Entry<DBData, CrosstabAggregator> k : entries) {
            fill[row][col] = new RVString(SimpleTextCellDef.wrapText(k.getKey().toString(), style, env.getLocale()), style, g);
            final CrosstabAggregator cta = k.getValue();
            colSorter[col - yDims.length] = cta.index;
            if (row < xDims.length - 1)
                col = fillXHeaders(cta.getElems(), row + 1, col, fill, env, colSorter, g);
            else
                col++;
        }

        return col;
    }

    private int fillYHeaders(HashMap<DBData, CrosstabAggregator> spreader, int row, int col, RVStringCol[] fill, RunEnv env, int[] rowSorter) {
        //noinspection unchecked
        final Map.Entry<DBData, CrosstabAggregator>[] entries = spreader.entrySet().toArray(new Map.Entry[spreader.size()]);
        Arrays.sort(entries, RDCrossTab.ctKeyComparator);
        final String[] hCol = fill[col].get();
        for (Map.Entry<DBData, CrosstabAggregator> k : entries) {
            final CrosstabAggregator cta = k.getValue();
            rowSorter[row] = cta.index;
            String coretext;
            final DBData key = k.getKey();
            if (key == null || key.isNull())
                coretext = "";
            else
                coretext = key.toString();

            hCol[row] = SimpleTextCellDef.wrapText(coretext, yDims[col].getCascadedStyle(), env.getLocale());
            if (col < yDims.length - 1)
                row = fillYHeaders(cta.getElems(), row, col + 1, fill, env, rowSorter);
            else
                row++;
        }

        return row;
    }

    private void fillCol(RVStringCol[] fill, BucketList<BucketList<CrosstabAggregator>> rows, int[] colSorter, int[] rowSorter, int aggIndex, RunEnv env) {
        CellDef cd = (CellDef) fields[xDims.length + aggIndex][yDims.length];
        Trace.check(cd, CrosstabValueCellDef.class);
        CellStyle style = cd.getCascadedStyle();
        for (int row = 0; row < rows.size(); row++) {
            BucketList<CrosstabAggregator> rowVals = rows.getObject(rowSorter[row]);
            for (int col = 0; col < colSorter.length; col++) {
                RVStringCol dataCol = fill[yDims.length + col];
                CrosstabAggregator v = rowVals.getObject(colSorter[col]);
                if (v != null) {
                    final double number = v.get(aggIndex);
                    dataCol.get()[row] = SimpleTextCellDef.wrapText(env.getPager().format(number, style), style, env.getLocale());
                }
            }
        }
    }

    public double[] aggVals(DBObject from, RunEnv env, double[] tempAggVals) throws JoriaDataException {
        for (int i = 0; i < aggs.length; i++) {
            CrosstabAggregate agg = aggs[i];
            final JoriaAccess access = agg.getAccess();
            final double val;
            if (access == null)
                continue;
            else if (access.isAccessTyped()) {
                if (access.getType().isIntegerLiteral())
                    val = ((JoriaAccessTyped) access).getIntValue(from, env);
                else if (access.getType().isRealLiteral())
                    val = ((JoriaAccessTyped) access).getFloatValue(from, env);
                else
                    throw new JoriaDataException("Non numeric type for crosstab aggregate: " + agg);
            } else {
                final DBData value = access.getValue(from, access, env);
                if (value == null || value.isNull())
                    val = 0;
                else if (access.getType().isIntegerLiteral())
                    val = ((DBInt) value).getIntValue();
                else if (access.getType().isRealLiteral())
                    val = ((DBReal) value).getRealValue();
                else
                    throw new JoriaDataException("Non numeric type for crosstab aggregate: " + agg);
            }
            tempAggVals[i] = val;
        }
        return tempAggVals;
    }

    public int nextDefCol(int defCol, int dCol, int length) {
        if (doRowColSums) {
            if (dCol < getHeaderCols())
                return defCol + 1;
            else if (length - dCol - 2 < getHeaderCols())
                return defCol + 1;
            return defCol;
        } else {
            if (dCol < getHeaderCols())
                return defCol + 1;
            if (dCol % getHeaderCols() == 0)
                return getHeaderCols();
            else
                return defCol + 1;
        }
    }

    public void calcMaxWidth(float[][] widths, RVAny values, Locale loc, Graphics2D g) {
        RVTemplate v = (RVTemplate) values;
        if (v == null)
            return;
        int rBase;
        if (myModel == null)
            rBase = 0;
        else
            rBase = myModel.getStartRow();
        int dynCols = widths[0].length;
        for (int r = 0; r < fields.length; r++) {
            RDBase[] row = fields[r];
            int tcIx = 0;
            for (int c = 0; c < dynCols; c++) {
                RDBase rdb = row[tcIx];
                if (rdb == null) {
                    tcIx = nextDefCol(tcIx, c, dynCols);
                    continue;
                }
                RVAny vv = v.get(r, c);
                if (rdb instanceof CellDef) {
                    CellDef cd = (CellDef) rdb;
                    FlexSize cfs = myModel.getColSizingAt(tcIx);
                    float bw = getHorizontalBorderWidth(r, c);
                    float pictureWidth = 0;
                    CellStyle cs = cd.getCascadedStyle();
                    bw += cs.getLeftRightPaddingValue();
                    if (cs.getBackgroundImageName() != null) {
                        if (cs.getBackgroundImageTargetWidth() != null && !cs.getBackgroundImageTargetWidth().isExpandable())
                            pictureWidth = cs.getBackgroundImageTargetWidth().getVal();
                        else
                            pictureWidth = cs.getBackgroundImage(loc).getIconWidth();
                    }
                    // TODO
                    if (cfs.getUnit() == FlexSize.flex)
                        widths[r + rBase][c] = Math.max(widths[r + rBase][c], Math.max(cd.getMaxWidth(vv, loc, g) + bw, pictureWidth + bw));
                    else {
                        if (vv instanceof RVStringCol) // force conversion of int, boolean, float columns to strings
                            ((RVStringCol) vv).buildFormattedStrings(cd, loc);
                        widths[r + rBase][c] = cfs.getVal() + bw;
                    }
                } else
                    throw new JoriaAssertionError("Unexpected RD Class in Crosstab: " + rdb.getClass().getName());
                tcIx = nextDefCol(tcIx, c, dynCols);
            }
        }
    }

    static Comparator<Map.Entry<DBData, CrosstabAggregator>> ctKeyComparator = new Comparator<Map.Entry<DBData, CrosstabAggregator>>() {
        public int compare(Map.Entry<DBData, CrosstabAggregator> o1, Map.Entry<DBData, CrosstabAggregator> o2) {
            String a;
            String b;
            final DBData k1 = o1.getKey();
            final DBData k2 = o2.getKey();
            if (k1 == null)
                a = "";
            else
                a = k1.toString();

            if (k2 == null)
                b = "";
            else
                b = k2.toString();
            return a.compareTo(b);
        }
    };

    public int getHeaderRows() {
        return xDims.length;
    }

    public int getHeaderCols() {
        return yDims.length;
    }

    public int getDataRows() {
        return aggs.length;
    }

    public boolean isDoRowColSums() {
        return doRowColSums;
    }
}
