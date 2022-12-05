// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.cells.CellDef;
import org.pdgen.projection.NumberAsDate;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;
import java.util.*;


public class CellStyle extends BoxStyle {
    private static final long serialVersionUID = 7L;

    public enum SpanToInner implements Serializable {
        borderAllCells,
        spanAllCells,
        borderFirstCell,
        borderFirstCellAndSpanAllCells;

        public String toString() {
            return Res.str(super.toString());
        }
    }

    public static final Integer plainType = 0;
    public static final Integer reFlowType = 1;
    public static final Integer htmlType = 2;
    public static final Integer rtfType = 3;
    public static final Integer vertBottomUp = 4;
    public static final Integer vertTopDown = 5;
    public static final String[] textStyleNames = {Res.str("Plain"), Res.str("Flowing"), Res.str("Html"), Res.str("Rtf"), Res.str("vertBottomUp"), Res.str("vertTopDown")};
    public static final String[] textStyleNames4Selection = {Res.str("Plain"), Res.str("Flowing"), Res.str("Html"), Res.str("Rtf"), Res.str("vertBottomUp"), Res.str("vertTopDown"), Res.str("inherit")};
    protected Boolean autoSpan;
    protected Integer spanHorizontal;
    protected Integer spanVertical;
    protected String numberConversion;
    protected Integer textType;
    protected String prefix;
    protected String appendix;
    protected Boolean supressRepeatedCellInTable;
    protected CellStyle baseStyle;
    protected Length topPadding;
    protected Length leftPadding;
    protected Length rightPadding;
    protected Length bottomPadding;
    protected SpanToInner spanToInner;

    public CellStyle() {
    }

    public CellStyle(String aName) {
        super(aName);
    }

    public CellStyle(CellStyle b) {
        copyCellStyle(b);
    }

    public static void init() {
        Trace.logDebug(Trace.init, "Init Cell Styles");
        PredefinedStyles stat = PredefinedStyles.instance();
        stat.theCellStyleDefaultTableHeaderStyle = new CellStyle(PredefinedStyles.defaultTableHeaderStyleName);
        stat.theCellStyleDefaultProblemStyle = new CellStyle(PredefinedStyles.defaultProblemStyleName);
        stat.theCellStyleDefaultTitleStyle = new CellStyle(PredefinedStyles.defaultTitleStyleName);
        stat.theCellStyleDefaultNumberStyle = new CellStyle(PredefinedStyles.defaultNumberStyleName);
        stat.theCellStyleDefaultTotalStyle = new CellStyle(PredefinedStyles.defaultTotalStyleName);
        stat.theCellStyleDefaultTotalNumberStyle = new CellStyle(PredefinedStyles.defaultTotalNumberStyleName);
        stat.theCellStyleDefaultGroupKeyStyle = new CellStyle(PredefinedStyles.defaultGroupKeyStyleName);
        //
        stat.theCellStyleDefaultTableHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
        stat.theCellStyleDefaultTableHeaderStyle.setBold(Boolean.TRUE);
        //
        stat.theCellStyleDefaultTitleStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
        stat.theCellStyleDefaultTitleStyle.setBold(Boolean.TRUE);
        stat.theCellStyleDefaultTitleStyle.setSize(new PointSize(14));
        stat.theCellStyleDefaultTitleStyle.setSpanHorizontal(Integer.MAX_VALUE / 2);
        //
        stat.theCellStyleDefaultProblemStyle.setBackground(Color.pink);
        //
        stat.theCellStyleDefaultNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
        //
        stat.theCellStyleDefaultTotalNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
        stat.theCellStyleDefaultTotalNumberStyle.setItalic(Boolean.TRUE);
        //
        stat.theCellStyleDefaultTotalStyle.setItalic(Boolean.TRUE);
        //
        stat.theCellStyleDefaultGroupKeyStyle.setItalic(Boolean.TRUE);
        stat.theCellStyleDefaultGroupKeyStyle.setBold(Boolean.TRUE);
        //
        SortedNamedVector<CellStyle> cellStyles = Env.instance().repo().cellStyles;
        cellStyles.add(stat.theCellStyleDefaultTitleStyle);
        cellStyles.add(stat.theCellStyleDefaultTableHeaderStyle);
        cellStyles.add(stat.theCellStyleDefaultProblemStyle);
        cellStyles.add(stat.theCellStyleDefaultNumberStyle);
        cellStyles.add(stat.theCellStyleDefaultTotalStyle);
        cellStyles.add(stat.theCellStyleDefaultTotalNumberStyle);
        cellStyles.add(stat.theCellStyleDefaultGroupKeyStyle);
    }

    public static void initCrosstab() {
        Trace.logDebug(Trace.init, "Init Cell Styles");
        PredefinedStyles stat = PredefinedStyles.instance();
        SortedNamedVector<CellStyle> cellStyles = Env.instance().repo().cellStyles;
        if (cellStyles.find(PredefinedStyles.defaultCrosstabHorizontalHeaderStyleName) == null) {
            stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle = new CellStyle(PredefinedStyles.defaultCrosstabHorizontalHeaderStyleName);
            stat.theCellStyleDefaultCrosstabVerticalHeaderStyle = new CellStyle(PredefinedStyles.defaultCrosstabVerticalHeaderStyleName);
            //
            stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
            stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle.setBold(Boolean.TRUE);
            //
            stat.theCellStyleDefaultCrosstabVerticalHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
            stat.theCellStyleDefaultCrosstabVerticalHeaderStyle.setBold(Boolean.TRUE);
            //
            cellStyles.add(stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle);
            cellStyles.add(stat.theCellStyleDefaultCrosstabVerticalHeaderStyle);
        }
        if (cellStyles.find(PredefinedStyles.defaultTotalNumberStyleName) == null) {
            stat.theCellStyleDefaultTotalStyle = new CellStyle(PredefinedStyles.defaultTotalStyleName);
            stat.theCellStyleDefaultTotalNumberStyle = new CellStyle(PredefinedStyles.defaultTotalNumberStyleName);
            stat.theCellStyleDefaultGroupKeyStyle = new CellStyle(PredefinedStyles.defaultGroupKeyStyleName);
            //
            stat.theCellStyleDefaultTotalNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
            stat.theCellStyleDefaultTotalNumberStyle.setItalic(Boolean.TRUE);
            //
            stat.theCellStyleDefaultTotalStyle.setItalic(Boolean.TRUE);
            stat.theCellStyleDefaultGroupKeyStyle.setItalic(Boolean.TRUE);
            stat.theCellStyleDefaultGroupKeyStyle.setBold(Boolean.TRUE);
            cellStyles.add(stat.theCellStyleDefaultTotalStyle);
            cellStyles.add(stat.theCellStyleDefaultTotalNumberStyle);
            cellStyles.add(stat.theCellStyleDefaultGroupKeyStyle);
        }
    }

    public void copyCellStyle(CellStyle s) {
        if (s == null)
            return;
        copyBoxStyle(s);
        autoSpan = s.getAutoSpan();
        spanHorizontal = s.spanHorizontal;
        spanVertical = s.spanVertical;
        numberConversion = s.numberConversion;
        textType = s.getTextType();
        prefix = s.getPrefix();
        appendix = s.appendix;
        spanToInner = s.spanToInner;
        backgroundImageName = s.backgroundImageName;
        baseStyle = s.baseStyle;
        topPadding = s.topPadding; // paddings are immutabe no copying needed
        leftPadding = s.leftPadding; // paddings are immutabe no copying needed
        rightPadding = s.rightPadding; // paddings are immutabe no copying needed
        bottomPadding = s.bottomPadding; // paddings are immutabe no copying needed
    }

    public void mergeDefaults() {
        if (onNewPage == null)
            onNewPage = Boolean.FALSE;
        if (autoSpan == null)
            autoSpan = Boolean.TRUE;
        if (alignmentHorizontal == null)
            alignmentHorizontal = HorizontalAlignment.LEFT;
        if (alignmentVertical == null)
            alignmentVertical = VerticalAlignment.TOP;
        if (spanHorizontal == null)
            spanHorizontal = ONE;
        if (spanVertical == null)
            spanVertical = ONE;
        if (numberConversion == null)
            numberConversion = NumberAsDate.NUMBER;
        if (breakable == null)
            breakable = Boolean.TRUE;
        if (fill == null)
            fill = FillBoth;
        if (textType == null)
            textType = plainType;
        if (topPadding == null || topPadding == Length.NULL)
            topPadding = Length.TWO;
        if (leftPadding == null || leftPadding == Length.NULL)
            leftPadding = Length.TWO;
        if (rightPadding == null || rightPadding == Length.NULL)
            rightPadding = Length.TWO;
        if (bottomPadding == null || bottomPadding == Length.NULL)
            bottomPadding = Length.TWO;
        if (spanToInner == null)
            spanToInner = SpanToInner.borderFirstCell;
    }

    public Boolean getAutoSpan() {
        return autoSpan;
    }

    public Boolean getAutoSpanTrans() {
        if (autoSpan != null)
            return autoSpan;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getAutoSpanTrans();
    }

    public Integer getSpanHorizontal() {
        return spanHorizontal;
    }

    public Integer getSpanHorizontalTrans() {
        if (spanHorizontal != null)
            return spanHorizontal;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getSpanHorizontalTrans();
    }

    public Integer getSpanVertical() {
        return spanVertical;
    }

    public Integer getSpanVerticalTrans() {
        if (spanVertical != null)
            return spanVertical;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getSpanVerticalTrans();
    }

    public int calcSpanHorizontal() {
        if (spanHorizontal == null && baseStyle != null)
            return baseStyle.calcSpanHorizontal();
        return spanHorizontal != null ? spanHorizontal : 1;
    }

    public int calcSpanVertical() {
        if (spanVertical == null && baseStyle != null)
            return baseStyle.calcSpanVertical();
        return spanVertical != null ? spanVertical : 1;
    }

    public String getNumberConversion() {
        return numberConversion;
    }

    public String getNumberConversionTrans() {
        if (numberConversion != null)
            return numberConversion;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getNumberConversionTrans();
    }

    public String getDatePatternTrans() {
        if (datePattern != null)
            return datePattern;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getDatePatternTrans();
    }

    public String getFloatPatternTrans() {
        if (floatPattern != null)
            return floatPattern;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getFloatPatternTrans();
    }

    public String getIntPatternTrans() {
        if (intPattern != null)
            return intPattern;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getIntPatternTrans();
    }

    public boolean equals(Object o) {
        if (o == null || (o.getClass() != CellStyle.class))
            return false;
        CellStyle from = (CellStyle) o;
        return eq(baseStyle, from.baseStyle) && eq(backgroundImageName, from.backgroundImageName)
                && eq(spanToInner, from.spanToInner) && eq(supressRepeatedCellInTable, from.supressRepeatedCellInTable)
                && eq(backgroundImageTargetWidth, from.backgroundImageTargetWidth) && eq(autoSpan, from.autoSpan)
                && eq(alignmentHorizontal, from.alignmentHorizontal) && eq(alignmentVertical, from.alignmentVertical)
                && eq(spanHorizontal, from.spanHorizontal) && eq(spanVertical, from.spanVertical)
                && eq(numberConversion, from.numberConversion) && eq(topPadding, from.topPadding)
                && eq(leftPadding, from.leftPadding) && eq(rightPadding, from.rightPadding)
                && eq(bottomPadding, from.bottomPadding) && eq(textType, from.textType)
                && eq(prefix, from.prefix) && eq(appendix, from.appendix) && boxStyleEquals(from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), supressRepeatedCellInTable, baseStyle, backgroundImageName, backgroundImageTargetWidth, autoSpan, alignmentHorizontal, alignmentVertical, spanHorizontal, spanVertical, numberConversion, topPadding, leftPadding, bottomPadding, rightPadding, textType, prefix, appendix);
    }

    public void mergeCellStyleFrom(CellStyle from) {
        from = (CellStyle) getLocalMaster(from, true);
        if (autoSpan == null)
            autoSpan = from.autoSpan;
        if (spanHorizontal == null)
            spanHorizontal = from.spanHorizontal;
        if (spanVertical == null)
            spanVertical = from.spanVertical;
        if (numberConversion == null)
            numberConversion = from.numberConversion;
        if (textType == null)
            textType = from.textType;
        if (appendix == null)
            appendix = from.appendix;
        if (prefix == null)
            prefix = from.prefix;
        if (baseStyle == null)
            baseStyle = from.baseStyle;
        if (topPadding == null)
            topPadding = from.topPadding;
        if (leftPadding == null)
            leftPadding = from.leftPadding;
        if (rightPadding == null)
            rightPadding = from.rightPadding;
        if (bottomPadding == null)
            bottomPadding = from.bottomPadding;
        if (spanToInner == null)
            spanToInner = from.spanToInner;
        if (supressRepeatedCellInTable == null)
            supressRepeatedCellInTable = from.supressRepeatedCellInTable;
        if (backgroundImageName == null)
            backgroundImageName = from.backgroundImageName;
        if (backgroundImageTargetWidth == null)
            backgroundImageTargetWidth = from.backgroundImageTargetWidth;
        mergeBoxStyleFrom(from);
        mergeTextStyle(from);
    }

    public void registerInRepository() {
        if (name == null || name.equals(""))
            makeUniqueName(Res.str("CellStyle"));
        Env.instance().repo().cellStyles.add(this);
    }

    public void setAutoSpan(Boolean newAutoSpan) {
        autoSpan = newAutoSpan;
    }

    public void setSpanHorizontal(Integer newSpanHorizontal) {
        spanHorizontal = newSpanHorizontal;
    }

    public void setSpanVertical(Integer newSpanVertical) {
        spanVertical = newSpanVertical;
    }

    public void setNumberConversion(String val) {
        numberConversion = val;
    }

    public void makeUniqueName(String start) {
        int cnt = 1;
        do {
            name = start + cnt++;
        }
        while (Env.instance().repo().cellStyles.get(name) >= 0);
    }

    public Integer getTextType() {
        return textType;
    }

    public Integer getTextTypeTrans() {
        if (textType != null)
            return textType;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getTextTypeTrans();
    }

    public String getTextTypeName() {
        if (textType != null)
            return textStyleNames[textType];
        return null;
    }

    public String getTextTypeNameTrans() {
        Integer tt = getTextTypeTrans();
        if (tt != null)
            return textStyleNames[tt];
        return null;
    }

    public void setTextType(Integer textType) {
        this.textType = textType;
    }

    protected void storeSpecialStyle() {
        PredefinedStyles stat = PredefinedStyles.instance();
        if (eq(name, PredefinedStyles.defaultTableHeaderStyleName))
            stat.theCellStyleDefaultTableHeaderStyle = this;
        else if (eq(name, PredefinedStyles.defaultTitleStyleName))
            stat.theCellStyleDefaultTitleStyle = this;
        else if (eq(name, PredefinedStyles.defaultNumberStyleName))
            stat.theCellStyleDefaultNumberStyle = this;
        else if (eq(name, PredefinedStyles.defaultProblemStyleName))
            stat.theCellStyleDefaultProblemStyle = this;
        else if (eq(name, PredefinedStyles.defaultTotalNumberStyleName))
            stat.theCellStyleDefaultTotalNumberStyle = this;
        else if (eq(name, PredefinedStyles.defaultTotalStyleName))
            stat.theCellStyleDefaultProblemStyle = this;
        else if (eq(name, PredefinedStyles.defaultGroupKeyStyleName))
            stat.theCellStyleDefaultGroupKeyStyle = this;
        else if (eq(name, PredefinedStyles.defaultCrosstabHorizontalHeaderStyleName))
            stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle = this;
        else if (eq(name, PredefinedStyles.defaultCrosstabVerticalHeaderStyleName))
            stat.theCellStyleDefaultCrosstabVerticalHeaderStyle = this;
    }

    @Override
    public SortedNamedVector<CellStyle> getGlobalStyleList() {
        return Env.instance().repo().cellStyles;
    }

    public static CellStyle duplicateLocal(CellStyle c) {
        if (c == null || c.name != null)
            return c;
        else
            return new CellStyle(c);
    }

    public CellStyle getBaseStyle() {
        return baseStyle;
    }

    public CellStyle getBaseStyleTrans() {
        if (baseStyle != null)
            return baseStyle;
        else
            return null;
    }

    public String getBaseStyleName() {
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getName();
    }

    public void setBaseStyle(CellStyle baseStyle) {
        this.baseStyle = baseStyle;
    }

    public String getSuffix() {
        return appendix;
    }

    public String getSuffixTrans() {
        if (appendix != null)
            return appendix;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getSuffixTrans();
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefixTrans() {
        if (prefix != null)
            return prefix;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getPrefixTrans();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void fillFromBase(CellStyle b) {
        if (b == null)
            return;
        mergeCellStyleFrom(b);
        fillFromBase(b.getBaseStyle());
    }

    public Boolean getSupressRepeatedCellInTable() {
        return supressRepeatedCellInTable;
    }

    public Boolean getSupressRepeatedCellInTableTrans() {
        if (supressRepeatedCellInTable != null)
            return supressRepeatedCellInTable;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getSupressRepeatedCellInTableTrans();
    }

    public void setSupressRepeatedCellInTable(Boolean supressRepeatedCellInTable) {
        this.supressRepeatedCellInTable = supressRepeatedCellInTable;
    }

    public void mergeOverrides(CellStyle from) {
        if (from.autoSpan != null)
            autoSpan = from.autoSpan;
        if (from.spanHorizontal != null)
            spanHorizontal = from.spanHorizontal;
        if (from.spanVertical != null)
            spanVertical = from.spanVertical;
        if (from.numberConversion != null)
            numberConversion = from.numberConversion;
        if (from.textType != null)
            textType = from.textType;
        if (from.appendix != null)
            appendix = from.appendix;
        if (from.prefix != null)
            prefix = from.prefix;
        if (from.topPadding != null)
            topPadding = from.topPadding;
        if (from.leftPadding != null)
            leftPadding = from.leftPadding;
        if (from.rightPadding != null)
            rightPadding = from.rightPadding;
        if (from.bottomPadding != null)
            bottomPadding = from.bottomPadding;
        if (from.spanToInner != null)
            spanToInner = from.spanToInner;
        if (backgroundImageName != null)
            backgroundImageName = from.backgroundImageName;
        if (backgroundImageTargetWidth != null)
            backgroundImageTargetWidth = from.backgroundImageTargetWidth;
        mergeBoxOverrides(from);
    }

    public void collectI18nKeys(HashSet<String> keySet) {
        super.collectI18nKeys(keySet);
        Internationalisation.collectI18nKeys(prefix, keySet);
        Internationalisation.collectI18nKeys(appendix, keySet);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet, final CellDef cdx) {
        collectI18nKeys2(keySet);
        Internationalisation2.collectI18nKeys(prefix, keySet, new I18nKeyHolder() {
            public void setI18nKey(String newVal) {
                prefix = newVal;
                Env.instenv().reflectStyleChange(CellStyle.this);
            }
        });
        Internationalisation2.collectI18nKeys(appendix, keySet, new I18nKeyHolder() {
            public void setI18nKey(String newVal) {
                appendix = newVal;
                Env.instenv().reflectStyleChange(CellStyle.this);
            }
        });
    }

    public float getWidth(String s, Graphics2D g) {
        if (s == null)
            return 0;
        Font f = getStyledFont();
        double maxWidth = 0;
        for (StringTokenizer stringTokenizer = new StringTokenizer(s, "\r\n"); stringTokenizer.hasMoreTokens(); )//trdone
        {
            String s1 = stringTokenizer.nextToken();
            FontRenderContext frc = g.getFontRenderContext();
            Rectangle2D r = f.getStringBounds(s1, frc);
            if (textType == vertBottomUp || textType == vertTopDown) {
                double height = r.getHeight();
                if (height < -r.getY())
                    height += -r.getY();
                //noinspection SuspiciousNameCombination
                maxWidth += height;
            } else
                maxWidth = Math.max(maxWidth, r.getWidth());
        }
        return (float) maxWidth;
    }

    public float getHeight(String s, Graphics2D g) {
        if (s == null)
            return 0;
        Font f = getStyledFont();
        double height = 0;
        for (StringTokenizer stringTokenizer = new StringTokenizer(s, "\r\n"); stringTokenizer.hasMoreTokens(); )//trdone
        {
            String s1 = stringTokenizer.nextToken();
            Rectangle2D r = f.getStringBounds(s1, g.getFontRenderContext());
            if (textType == vertBottomUp || textType == vertTopDown)
                height = Math.max(height, r.getWidth());
            else {
                height += r.getHeight();
                if (-r.getY() > r.getHeight())
                    height += -r.getY();
            }
        }
        return (float) height;
    }

    public Length getBottomPadding() {
        return bottomPadding;
    }

    public Length getBottomPaddingTrans() {
        if (bottomPadding != null)
            return bottomPadding;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getBottomPaddingTrans();
    }

    public void setBottomPadding(Length bottomPadding) {
        this.bottomPadding = bottomPadding;
    }

    public Length getLeftPadding() {
        return leftPadding;
    }

    public Length getLeftPaddingTrans() {
        if (leftPadding != null)
            return leftPadding;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getLeftPaddingTrans();
    }

    public void setLeftPadding(Length leftPadding) {
        this.leftPadding = leftPadding;
    }

    public Length getRightPadding() {
        return rightPadding;
    }

    public Length getRightPaddingTrans() {
        if (rightPadding != null)
            return rightPadding;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getRightPaddingTrans();
    }

    public void setRightPadding(Length rightPadding) {
        this.rightPadding = rightPadding;
    }

    public Length getTopPadding() {
        return topPadding;
    }

    public Length getTopPaddingTrans() {
        if (topPadding != null)
            return topPadding;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getTopPaddingTrans();
    }

    public void setTopPadding(Length topPadding) {
        this.topPadding = topPadding;
    }

    public float getWidth(char[] s, int start, int limit, Graphics2D g) {
        return getWidth(new String(s, start, limit - start), g);
    }

    public float getTopBotPaddingValue() {
        return getTopPadding().getValInPoints() + getBottomPadding().getValInPoints();
    }

    public float getLeftRightPaddingValue() {
        return getLeftPadding().getValInPoints() + getRightPadding().getValInPoints();
    }

    public PointSize getSizeTrans() {
        if (size != null)
            return size;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getSizeTrans();
    }

    public Boolean getItalicTrans() {
        if (italic != null)
            return italic;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getItalicTrans();
    }

    public Color getBackgroundTrans() {
        if (background != null)
            return background;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getBackgroundTrans();
    }

    public Boolean getBoldTrans() {
        if (bold != null)
            return bold;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getBoldTrans();
    }

    public String getFontTrans() {
        if (font != null)
            return font;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getFontTrans();
    }

    public Color getForegroundTrans() {
        if (foreground != null)
            return foreground;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getForegroundTrans();
    }

    public Boolean getUnderlinedTrans() {
        if (underlined != null)
            return underlined;
        if (baseStyle == null)
            return null;
        else
            return baseStyle.getUnderlinedTrans();
    }

    public HorizontalAlignment getAlignmentHorizontalTrans() {
        if (alignmentHorizontal != null)
            return alignmentHorizontal;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getAlignmentHorizontalTrans();
    }

    public VerticalAlignment getAlignmentVerticalTrans() {
        if (alignmentVertical != null)
            return alignmentVertical;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getAlignmentVerticalTrans();
    }

    public Boolean getBreakableTrans() {
        if (breakable != null)
            return breakable;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getBreakableTrans();
    }

    public Integer getFillTrans() {
        if (fill != null)
            return fill;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getFillTrans();
    }

    /**
     * need this as a method for use in reflection
     *
     * @return this
     */
    public CellStyle myself() {
        return this;
    }

    public Boolean getOnNewPageTrans() {
        if (onNewPage != null)
            return onNewPage;
        else if (baseStyle == null)
            return null;
        else
            return baseStyle.getOnNewPageTrans();
    }

    public SpanToInner getSpanToInner() {
        return spanToInner;
    }

    public void setSpanToInner(SpanToInner spanToInner) {
        this.spanToInner = spanToInner;
    }

}
