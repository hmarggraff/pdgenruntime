// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.SortedNamedVector;
import org.pdgen.env.Env;
import org.pdgen.env.Res;

import java.awt.*;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class TableStyle extends StyleBase implements java.io.Serializable {
    private static final long serialVersionUID = 7L;

    enum SpanToInner implements Serializable {
        borderAllCells,
        spanAllCells,
        borderFirstCell,
        borderFirstCellAndSpanAllCells;

        public String toString() {
            return Res.str(super.toString());
        }
    }

    public static String defaultTableStyleName = "*DefaultTableStyle";
    protected Boolean headerOnEachPage;
    protected Boolean totalOnLastPage;
    protected Boolean breakable;
    protected Color tableBackground;
    protected TableBorder tableBorder;
    protected JoriaSimpleBorder groupSeperatorBorder;
    protected Boolean breakableGroup;
    protected Boolean suppressEmpty;
    protected Boolean dataOnEachPage;
    protected RowOnNewPage dataOnNewPage;
    protected Boolean extendToInner;
    protected SpanToInner spanToInner;
    protected Boolean showBorderAtPageBreak;

    public TableStyle() {
    }

    public TableStyle(String aName) {
        super(aName);
    }

    public TableStyle(TableStyle from) {
        this(from, true);
    }

    public TableStyle(TableStyle from, boolean checkLocalMaster) {
        copyTableStyle((TableStyle) StyleBase.getLocalMaster(from, checkLocalMaster));
    }

    public void copyTableStyle(TableStyle from) {
        if (from == null)
            return;
        tableBorder = from.tableBorder;
        headerOnEachPage = from.headerOnEachPage;
        groupSeperatorBorder = from.groupSeperatorBorder;
        tableBackground = from.tableBackground;
        totalOnLastPage = from.totalOnLastPage;
        breakable = from.breakable;
        breakableGroup = from.breakableGroup;
        suppressEmpty = from.suppressEmpty;
        dataOnEachPage = from.dataOnEachPage;
        dataOnNewPage = from.dataOnNewPage;
        spanToInner = from.spanToInner;
        showBorderAtPageBreak = from.showBorderAtPageBreak;
    }

    public static void init(SortedNamedVector<TableStyle> tableStyles) {
        PredefinedStyles stat = PredefinedStyles.instance();
        TableStyle s = new TableStyle(defaultTableStyleName);
        s.setHeaderOnEachPage(Boolean.TRUE);
        s.setTotalOnLastPage(Boolean.TRUE);
        s.setBreakable(Boolean.TRUE);
        s.setBreakableGroup(Boolean.TRUE);
        s.setSuppressEmpty(Boolean.FALSE);
        s.setGroupSeperatorBorder(JoriaSimpleBorder.NULL);
        s.setDataOnEachPage(Boolean.TRUE);
        s.setDataOnNewPage(RowOnNewPage.never);
        s.setShowBorderAtPageBreak(Boolean.FALSE);
        stat.theTableStyleDefault = s;
        tableStyles.add(stat.theTableStyleDefault);
    }

    public void mergeFrom(TableStyle from) {
        from = (TableStyle) StyleBase.getLocalMaster(from, true);
        if (tableBorder == null)
            tableBorder = from.tableBorder;
        if (headerOnEachPage == null)
            headerOnEachPage = from.headerOnEachPage;
        if (totalOnLastPage == null)
            totalOnLastPage = from.totalOnLastPage;
        if (breakable == null)
            breakable = from.breakable;
        if (breakableGroup == null)
            breakableGroup = from.breakableGroup;
        if (suppressEmpty == null)
            suppressEmpty = from.suppressEmpty;
        if (tableBackground == null)
            tableBackground = from.tableBackground;
        if (groupSeperatorBorder == null)
            groupSeperatorBorder = from.groupSeperatorBorder;
        if (dataOnEachPage == null)
            dataOnEachPage = from.dataOnEachPage;
        if (dataOnNewPage == null)
            dataOnNewPage = from.dataOnNewPage;
        if (showBorderAtPageBreak == null)
            showBorderAtPageBreak = from.showBorderAtPageBreak;
    }

    public void mergeDefaults() {
        mergeFrom(PredefinedStyles.instance().theTableStyleDefault);
        if (headerOnEachPage == null)
            headerOnEachPage = Boolean.TRUE;
        if (totalOnLastPage == null)
            totalOnLastPage = Boolean.TRUE;
        if (breakable == null)
            breakable = Boolean.TRUE;
        if (tableBackground == null)
            tableBackground = Color.white;
        if (tableBorder == null)
            tableBorder = TableBorder.zeroBorder;
        if (breakableGroup == null)
            breakableGroup = Boolean.TRUE;
        if (suppressEmpty == null)
            suppressEmpty = Boolean.FALSE;
        if (groupSeperatorBorder == null)
            groupSeperatorBorder = JoriaSimpleBorder.NULL;
        if (dataOnEachPage == null)
            dataOnEachPage = Boolean.TRUE;
        if (dataOnNewPage == null)
            dataOnNewPage = RowOnNewPage.never;
        if (showBorderAtPageBreak == null)
            showBorderAtPageBreak = Boolean.TRUE;
    }

    public static TableStyle duplicateLocal(TableStyle from) {
        if (from == null)
            return null;
        else if (from.name != null)// named style dont duplicate
            return from;
        else
            return new TableStyle(from);
    }

    public void makeUniqueName(String start) {
        int cnt = 1;
        do {
            name = start + cnt++;
        }
        while (Env.instance().repo().tableStyles.get(name) >= 0);
    }

    public Boolean getBreakableGroup() {
        return breakableGroup;
    }

    public void setBreakableGroup(Boolean breakableGroup) {
        this.breakableGroup = breakableGroup;
    }

    public Boolean getSuppressEmpty() {
        return suppressEmpty;
    }

    public void setSuppressEmpty(Boolean suppressEmpty) {
        this.suppressEmpty = suppressEmpty;
    }

    public TableBorder getTableBorderForUpgrade() {
        return tableBorder;
    }

    public Boolean getHeaderOnEachPage() {
        return headerOnEachPage;
    }

    public void setHeaderOnEachPage(Boolean newHeaderOnEachPage) {
        headerOnEachPage = newHeaderOnEachPage;
    }

    public void setTotalOnLastPage(Boolean newTotalOnLastPage) {
        totalOnLastPage = newTotalOnLastPage;
    }

    public Boolean getTotalOnLastPage() {
        return totalOnLastPage;
    }

    public Boolean getBreakable() {
        return breakable;
    }

    public void setBreakable(Boolean breakable) {
        this.breakable = breakable;
    }

    public Color getTableBackground() {
        return tableBackground;
    }

    public void setTableBackground(Color tableBackground) {
        this.tableBackground = tableBackground;
    }

    protected void storeSpecialStyle() {
        if (StyleBase.eq(name, defaultTableStyleName)) {
            PredefinedStyles.instance().theTableStyleDefault = this;
            if (breakable == null)
                breakable = Boolean.TRUE;
        }
    }

    @Override
    public SortedNamedVector<TableStyle> getGlobalStyleList() {
        return Env.instance().repo().tableStyles;
    }

    public JoriaSimpleBorder getGroupSeperatorBorder() {
        return groupSeperatorBorder;
    }

    public void setGroupSeperatorBorder(JoriaSimpleBorder groupSeperatorBorder) {
        this.groupSeperatorBorder = groupSeperatorBorder;
    }

    public Boolean getDataOnEachPage() {
        return dataOnEachPage;
    }

    public void setDataOnEachPage(Boolean dataOnEachPage) {
        this.dataOnEachPage = dataOnEachPage;
    }

    public RowOnNewPage getDataOnNewPage() {
        return dataOnNewPage;
    }

    public void setDataOnNewPage(RowOnNewPage dataOnNewPage) {
        this.dataOnNewPage = dataOnNewPage;
    }

    protected Object readResolve() throws ObjectStreamException {
        Object ret = super.readResolve();
        if (extendToInner != null) {
            if (extendToInner)
                spanToInner = SpanToInner.borderAllCells;
            else
                spanToInner = SpanToInner.spanAllCells;
            extendToInner = null;
        }
        return ret;
    }

    public void nullBorder() {
        tableBorder = null;
    }

    public Boolean getShowBorderAtPageBreak() {
        return showBorderAtPageBreak;
    }

    public void setShowBorderAtPageBreak(final Boolean showBorderAtPageBreak) {
        this.showBorderAtPageBreak = showBorderAtPageBreak;
    }

}

