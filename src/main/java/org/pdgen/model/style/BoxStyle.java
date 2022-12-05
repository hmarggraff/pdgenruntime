// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

//MARKER The strings in this file shall not be translated


public abstract class BoxStyle extends TextStyle {
    private static final long serialVersionUID = 7L;
    protected Boolean breakable;
    protected CellBorder border;
    protected Integer fill;
    protected HorizontalAlignment alignmentHorizontal;
    protected VerticalAlignment alignmentVertical;
    protected Boolean onNewPage;

    protected BoxStyle() {
    }

    protected BoxStyle(String aName) {
        name = aName;
    }

    protected BoxStyle(BoxStyle b) {
        copyBoxStyle(b);
    }

    public boolean boxStyleEquals(BoxStyle s) {
        return StyleBase.eq(breakable, s.breakable) && StyleBase.eq(name, s.name) && textStyleEquals(s) && StyleBase.eq(fill, s.fill) && StyleBase.eq(onNewPage, s.onNewPage);
    }

    public void copyBoxStyle(BoxStyle s) {
        if (s == null)
            return;
        if (s.border != null)
            border = new CellBorder(s.border);
        else
            border = null;
        breakable = s.breakable;
        name = s.name;
        fill = s.fill;
        onNewPage = s.onNewPage;
        setAlignmentHorizontal(s.getAlignmentHorizontal());
        setAlignmentVertical(s.getAlignmentVertical());
        copyTextStyle(s);
    }

    public HorizontalAlignment getAlignmentHorizontal() {
        return alignmentHorizontal;
    }

    public VerticalAlignment getAlignmentVertical() {
        return alignmentVertical;
    }

    public CellBorder getBorderForUpgrade() {
        return border;
    }

    public void nullBorder() {
        border = null;
    }

    public Boolean getBreakable() {
        return breakable;
    }

    public void mergeBoxStyleFrom(BoxStyle from) {
        if (border == null)
            border = from.border;
        if (breakable == null)
            breakable = from.breakable;
        if (fill == null)
            fill = from.fill;
        if (alignmentHorizontal == null)
            alignmentHorizontal = from.alignmentHorizontal;
        if (alignmentVertical == null)
            alignmentVertical = from.alignmentVertical;
        if (onNewPage == null)
            onNewPage = from.onNewPage;
    }

    public void mergeBoxOverrides(CellStyle from) {
        if (from.border != null)
            border = from.border;
        if (from.breakable != null)
            breakable = from.breakable;
        if (from.fill != null)
            fill = from.fill;
        if (from.alignmentHorizontal != null)
            alignmentHorizontal = from.alignmentHorizontal;
        if (from.alignmentVertical != null)
            alignmentVertical = from.alignmentVertical;
        if (from.onNewPage != null)
            onNewPage = from.onNewPage;
        mergeTextOverrides(from);
    }

    public void setAlignmentHorizontal(HorizontalAlignment newAlignmentHorizontal) {
        alignmentHorizontal = newAlignmentHorizontal;
    }

    public void setAlignmentVertical(VerticalAlignment newAlignmentVertical) {
        alignmentVertical = newAlignmentVertical;
    }

    public void setBreakable(Boolean newBreakable) {
        breakable = newBreakable;
    }

    public void setFill(Integer newFill) {
        fill = newFill;
    }

    public Integer getFill() {
        return fill;
    }

    public Boolean getOnNewPage() {
        return onNewPage;
    }

    public void setOnNewPage(Boolean onNewPage) {
        this.onNewPage = onNewPage;
    }

}
