// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.SortedNamedVector;
import org.pdgen.data.Trace;
import org.pdgen.env.Env;
import org.pdgen.env.Res;

import java.util.Objects;

public class FrameStyle extends BoxStyle {
    private static final String headerStyleName = "*HeaderDefault";//trdone
    private static final String footerStyleName = "*FooterDefault";//trdone
    private static final String bodyStyleName = "*BodyDefault";//trdone
    private static final String nestedStyleName = "*Nested";//trdone
    private static final long serialVersionUID = 7L;
    //protected Color boxBackground;
    protected FlexSize height;
    protected FlexSize width;
    protected FlexSize xPos;
    protected FlexSize yPos;
    protected JoriaFrameBorder topBorder;
    protected JoriaFrameBorder leftBorder;
    protected JoriaFrameBorder rightBorder;
    protected JoriaFrameBorder bottomBorder;
    private Length backgroundImageX;
    private Length backgroundImageY;
    private SizeLimit sizeLimit;
    protected Integer flowingColumns = ONE;
    protected Length flowingColumnSpacing;
    protected Boolean flowingColumnDistribution;

    public FrameStyle() {
    }

    public FrameStyle(String aName) {
        super(aName);
    }

    public FrameStyle(FrameStyle b) {
        this(b, true);
    }

    public FrameStyle(FrameStyle b, boolean checkLocalMaster) {
        copyFrameStyle((FrameStyle) getLocalMaster(b, checkLocalMaster));
    }

    public static void init(SortedNamedVector<FrameStyle> frameStyles) {
        Trace.logDebug(Trace.init, "Init FrameStyles");
        //localStyle = new FrameStyle(localStyleName);
        //inheritStyle = new FrameStyle(inheritStyleName);
        PredefinedStyles stat = PredefinedStyles.instance();
        stat.theFrameStyleDefaultBodyStyle = new FrameStyle(bodyStyleName);
        stat.theFrameStyleDefaultBodyStyle.setFill(FillHorizontal);
        stat.theFrameStyleDefaultBodyStyle.setBreakable(Boolean.TRUE);
        stat.theFrameStyleDefaultBodyStyle.setFlowingColumns(ONE);
        stat.theFrameStyleDefaultBodyStyle.setOnNewPage(Boolean.FALSE);
        stat.theFrameStyleDefaultFooterStyle = new FrameStyle(footerStyleName);
        stat.theFrameStyleDefaultFooterStyle.setFill(FillSymmetric);
        stat.theFrameStyleDefaultFooterStyle.setTopBorder(JoriaFrameBorder.standard);
        stat.theFrameStyleDefaultFooterStyle.setBreakable(Boolean.FALSE);
        stat.theFrameStyleDefaultFooterStyle.setOnNewPage(Boolean.FALSE);
        stat.theFrameStyleDefaultHeaderStyle = new FrameStyle(headerStyleName);
        stat.theFrameStyleDefaultHeaderStyle.setFill(FillSymmetric);
        stat.theFrameStyleDefaultHeaderStyle.setBottomBorder(JoriaFrameBorder.standard);
        stat.theFrameStyleDefaultHeaderStyle.setBreakable(Boolean.FALSE);
        stat.theFrameStyleDefaultHeaderStyle.setOnNewPage(Boolean.FALSE);
        stat.theFrameStyleDefaultNestedStyle = createDefaultNestedFrameStyle(nestedStyleName);
        //Repository.instance().frameStyles.add(localStyle);
        //Repository.instance().frameStyles.add(inheritStyle);
        frameStyles.add(stat.theFrameStyleDefaultBodyStyle);
        frameStyles.add(stat.theFrameStyleDefaultFooterStyle);
        frameStyles.add(stat.theFrameStyleDefaultHeaderStyle);
    }

    public boolean equals(Object o) {
        if (o == null || o.getClass() != FrameStyle.class)
            return false;
        FrameStyle s = (FrameStyle) o;
        return boxStyleEquals(s)//
                //&& eq(boxBackground, s.boxBackground)//
                && FlexSize.eq(s.width, width)//
                && FlexSize.eq(s.height, height)//
                && FlexSize.eq(s.xPos, xPos)//
                && FlexSize.eq(s.yPos, yPos)//
                && FlexSize.eq(s.backgroundImageTargetWidth, backgroundImageTargetWidth)//
                && eq(s.backgroundImageName, backgroundImageName)//
                && Length.eq(s.backgroundImageX, backgroundImageX)//
                && Length.eq(s.backgroundImageY, backgroundImageY)//
                && s.sizeLimit == sizeLimit //
                && Length.eq(s.flowingColumnSpacing, flowingColumnSpacing)//
                && eq(s.flowingColumns, flowingColumns)//
                && eq(s.flowingColumnDistribution, flowingColumnDistribution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, xPos, yPos, backgroundImageTargetWidth, backgroundImageName, backgroundImageX, backgroundImageY, sizeLimit, flowingColumnSpacing, flowingColumns, flowingColumnDistribution);
    }

    private void copyFrameStyle(FrameStyle s) {
        if (s == null)
            return;
        //boxBackground = s.boxBackground;
        height = FlexSize.newFlexSize(s.height);
        width = FlexSize.newFlexSize(s.width);
        xPos = FlexSize.newFlexSize(s.xPos);
        yPos = FlexSize.newFlexSize(s.yPos);
        topBorder = s.topBorder; // frame borders are immutable
        leftBorder = s.leftBorder;
        rightBorder = s.rightBorder;
        bottomBorder = s.bottomBorder;
        backgroundImageName = s.backgroundImageName;
        backgroundImageTargetWidth = FlexSize.newFlexSize(s.backgroundImageTargetWidth);
        backgroundImageX = Length.copy(s.backgroundImageX);
        backgroundImageY = Length.copy(s.backgroundImageY);
        sizeLimit = s.sizeLimit;
        flowingColumns = s.flowingColumns;
        flowingColumnSpacing = Length.copy(s.flowingColumnSpacing);
        flowingColumnDistribution = s.flowingColumnDistribution;
        copyBoxStyle(s);
    }

    private static FrameStyle createDefaultNestedFrameStyle(String name) {
        FrameStyle s = new FrameStyle(name);
        //s.setBorder(CellBorder.plainBorder);
        s.breakable = Boolean.TRUE;
        s.fill = FillHorizontal;
        s.onNewPage = Boolean.FALSE;
        s.flowingColumns = ONE;
        return s;
    }

    public void mergeFrom(FrameStyle from) {
        from = (FrameStyle) getLocalMaster(from, true);
        if (width == null)
            width = from.width;
        if (height == null)
            height = from.height;
        if (xPos == null)
            xPos = from.xPos;
        if (yPos == null)
            yPos = from.yPos;
        if (topBorder == null)
            topBorder = from.topBorder;
        if (leftBorder == null)
            leftBorder = from.leftBorder;
        if (rightBorder == null)
            rightBorder = from.rightBorder;
        if (bottomBorder == null)
            bottomBorder = from.bottomBorder;
        if (backgroundImageName == null)
            backgroundImageName = from.backgroundImageName;
        if (backgroundImageTargetWidth == null)
            backgroundImageTargetWidth = from.backgroundImageTargetWidth;
        if (backgroundImageX == null)
            backgroundImageX = from.backgroundImageX;
        if (backgroundImageY == null)
            backgroundImageY = from.backgroundImageY;
        if (sizeLimit == null)
            sizeLimit = from.sizeLimit;
        if (flowingColumns == null)
            flowingColumns = from.flowingColumns;
        if (flowingColumnSpacing == null)
            flowingColumnSpacing = from.flowingColumnSpacing;
        if (flowingColumnDistribution == null)
            flowingColumnDistribution = from.flowingColumnDistribution;
        mergeBoxStyleFrom(from);
        mergeTextStyle(from);
    }

    public void mergeFrameDefaults() {
        if (breakable == null)
            breakable = Boolean.TRUE;
        if (fill == null)
            fill = FillHorizontal;
        if (width == null)
            width = FlexSize.FLEX;
        if (height == null)
            height = FlexSize.FLEX;
        if (onNewPage == null)
            onNewPage = Boolean.FALSE;
        if (topBorder == null)
            topBorder = JoriaFrameBorder.NULL_FRAME;
        if (leftBorder == null)
            leftBorder = JoriaFrameBorder.NULL_FRAME;
        if (rightBorder == null)
            rightBorder = JoriaFrameBorder.NULL_FRAME;
        if (bottomBorder == null)
            bottomBorder = JoriaFrameBorder.NULL_FRAME;
        if (background == null)
            background = transparent;
        if (flowingColumns == null)
            flowingColumns = ONE;
        if (flowingColumnSpacing == null)
            flowingColumnSpacing = Length.NULL;
        if (flowingColumnDistribution == null)
            flowingColumnDistribution = Boolean.TRUE;
    }

    public String toString() {
        if (name == null)
            return Res.str("noname");
        return name;
    }

    public void makeUniqueName() {
        int cnt = 1;
        do {
            name = Res.strp("FrameStyle", cnt++);
        }
        while (Env.instance().repo().frameStyles.get(name) >= 0);
    }

    protected void storeSpecialStyle() {
        PredefinedStyles stat = PredefinedStyles.instance();
        if (eq(name, bodyStyleName))
            stat.theFrameStyleDefaultBodyStyle = this;
        else if (eq(name, headerStyleName))
            stat.theFrameStyleDefaultHeaderStyle = this;
        else if (eq(name, footerStyleName))
            stat.theFrameStyleDefaultFooterStyle = this;
        else if (eq(name, nestedStyleName))
            stat.theFrameStyleDefaultNestedStyle = this;
    }

    public FlexSize getHeight() {
        return height;
    }

    public void setHeight(FlexSize height) {
        this.height = height;
    }

    public FlexSize getWidth() {
        return width;
    }

    public void setWidth(FlexSize width) {
        this.width = width;
    }

    public FlexSize getXPos() {
        return xPos;
    }

    public void setXPos(FlexSize xPos) {
        this.xPos = xPos;
    }

    public FlexSize getYPos() {
        return yPos;
    }

    public void setYPos(FlexSize yPos) {
        this.yPos = yPos;
    }

    public static FrameStyle duplicateLocal(FrameStyle from) {
        if (from == null)
            return null;
        else if (from.name != null)// named style doesnt duplicate
            return from;
        else
            return new FrameStyle(from);
    }


    public JoriaFrameBorder getBottomBorder() {
        return bottomBorder;
    }

    public void setBottomBorder(JoriaFrameBorder bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    public JoriaFrameBorder getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(JoriaFrameBorder leftBorder) {
        this.leftBorder = leftBorder;
    }

    public JoriaFrameBorder getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(JoriaFrameBorder rightBorder) {
        this.rightBorder = rightBorder;
    }

    public JoriaFrameBorder getTopBorder() {
        return topBorder;
    }

    public void setTopBorder(JoriaFrameBorder topBorder) {
        this.topBorder = topBorder;
    }

    public float getHorizontalInset() {
        return leftBorder.getTotalInset() + rightBorder.getTotalInset();
    }

    public float getVerticalInset() {
        return topBorder.getTotalInset() + bottomBorder.getTotalInset();
    }

    public Length getBackgroundImageX() {
        return backgroundImageX;
    }

    public void setBackgroundImageX(Length backgroundImageX) {
        this.backgroundImageX = backgroundImageX;
    }

    public Length getBackgroundImageY() {
        return backgroundImageY;
    }

    public void setBackgroundImageY(Length backgroundImageY) {
        this.backgroundImageY = backgroundImageY;
    }

    public Integer getFlowingColumns() {
        return flowingColumns;
    }

    public void setFlowingColumns(final Integer flowingColumns) {
        this.flowingColumns = flowingColumns;
    }

    public Length getFlowingColumnSpacing() {
        return flowingColumnSpacing;
    }

    public void setFlowingColumnSpacing(final Length flowingColumnSpacing) {
        this.flowingColumnSpacing = flowingColumnSpacing;
    }

    public Boolean getFlowingColumnDistribution() {
        return flowingColumnDistribution;
    }

    public void setFlowingColumnDistribution(Boolean flowingColumnDistribution) {
        this.flowingColumnDistribution = flowingColumnDistribution;
    }

    public float getColumnWidth(float givenWidth) {
        if (hasFlowingColumns()) {
            final float columnSpace = givenWidth - (flowingColumns - 1) * flowingColumnSpacing.getValInPoints();
            return columnSpace / flowingColumns;
        } else
            return givenWidth;
    }

    public boolean hasFlowingColumns() {
        return flowingColumns != null && flowingColumns > 1;
    }

    public SizeLimit getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(final SizeLimit sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Override
    public SortedNamedVector<FrameStyle> getGlobalStyleList() {
        return Env.instance().repo().frameStyles;
    }

}
