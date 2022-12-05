// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.SortedNamedVector;
import org.pdgen.data.Trace;
import org.pdgen.env.Env;
import org.pdgen.env.Res;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

public class PageStyle extends TextStyle {
    private static final long serialVersionUID = 7L;
    public static String defaultPageStyleName = "*Default";//trdone
    public static String A5PName = Res.str("A5_Portrait");
    public static String LetterPName = Res.str("Letter_Portrait");
    public static String A4PName = Res.str("A4_Portrait");
    public static String A5LName = Res.str("A5_Landscape");
    public static String LetterLName = Res.str("Letter_Landscape");
    public static String A4LName = Res.str("A4_Landscape");
    public static String SmallName = Res.str("Small");
    static final Length defaultMargin = new Length(24);
    protected PaperSize paperSize;
    protected Boolean orientationPortrait;
    protected Length topMargin;
    protected Length botMargin;
    protected Length leftMargin;
    protected Length rightMargin;
    private Length backgroundImageX;
    private Length backgroundImageY;
    private SizeLimit sizeLimit;

    public PageStyle(String aName) {
        super(aName);
        TextStyle.fillDefaultTextStyle4Page(this);
    }

    public PageStyle(PageStyle from) {
        this(from, true);
    }

    public PageStyle(PageStyle from, boolean checkLocalMaster) {
        super((TextStyle) StyleBase.getLocalMaster(from, checkLocalMaster));
        copyPageStyle(from);
    }

    public static void init(SortedNamedVector<PageStyle> ps) {
        Trace.logDebug(Trace.init, "Init PageStyle");
        PredefinedStyles stat = PredefinedStyles.instance();
        stat.thePageStyleDefaultPageStyle = createDefault(stat.thePaperSizeA4, Boolean.TRUE, defaultPageStyleName);
        stat.thePageStyleDefaultPageStyle.setFloatPattern(TextStyle.defaultFloatPattern);
        stat.thePageStyleDefaultPageStyle.setIntPattern(TextStyle.defaultIntPattern);
        stat.thePageStyleDefaultPageStyle.setDatePattern(TextStyle.defaultDatePattern);
        stat.thePageStyleA5P = createDefault(stat.thePaperSizeA5, Boolean.TRUE, A5PName);
        stat.thePageStyleLetterP = createDefault(stat.thePaperSizeLetter, Boolean.TRUE, LetterPName);
        stat.thePageStyleA4P = createDefault(stat.thePaperSizeA4, Boolean.TRUE, A4PName);
        stat.thePageStyleA5L = createDefault(stat.thePaperSizeA5, Boolean.FALSE, A5LName);
        stat.thePageStyleLetterL = createDefault(stat.thePaperSizeLetter, Boolean.FALSE, LetterLName);
        stat.thePageStyleA4L = createDefault(stat.thePaperSizeA4, Boolean.FALSE, A4LName);
        ps.add(stat.thePageStyleDefaultPageStyle);
        ps.add(stat.thePageStyleA5P);
        ps.add(stat.thePageStyleA4P);
        ps.add(stat.thePageStyleLetterP);
        ps.add(stat.thePageStyleA5L);
        ps.add(stat.thePageStyleA4L);
        ps.add(stat.thePageStyleLetterL);
    }

    public static void repairStyles() {
        PredefinedStyles stat = PredefinedStyles.instance();
        if (stat.thePageStyleDefaultPageStyle == null) {
            stat.thePageStyleDefaultPageStyle = createDefault(stat.thePaperSizeA4, Boolean.TRUE, defaultPageStyleName);
            stat.thePageStyleDefaultPageStyle.setFloatPattern(TextStyle.defaultFloatPattern);
            stat.thePageStyleDefaultPageStyle.setIntPattern(TextStyle.defaultIntPattern);
            stat.thePageStyleDefaultPageStyle.setDatePattern(TextStyle.defaultDatePattern);
            SortedNamedVector<PageStyle> ps = Env.instance().repo().pageStyles;
            ps.add(stat.thePageStyleDefaultPageStyle);
        }
    }

    public void mergeDefaults() {
        if (paperSize == null)
            paperSize = PredefinedStyles.instance().thePaperSizeA4;
        if (orientationPortrait == null)
            orientationPortrait = Boolean.TRUE;
        if (topMargin == null)
            topMargin = defaultMargin;
        if (rightMargin == null)
            rightMargin = defaultMargin;
        if (botMargin == null)
            botMargin = defaultMargin;
        if (leftMargin == null)
            leftMargin = defaultMargin;
        if (backgroundImageX == null)
            backgroundImageX = Length.NULL;
        if (backgroundImageY == null)
            backgroundImageY = Length.NULL;
        if (sizeLimit == null)
            sizeLimit = SizeLimit.FlexSize;
        mergeTextStyleDefaults();
    }

    public void copyPageStyle(PageStyle from) {
        paperSize = from.getPaperSize();
        orientationPortrait = from.orientationPortrait;
        topMargin = Length.copy(from.topMargin);
        rightMargin = Length.copy(from.rightMargin);
        botMargin = Length.copy(from.botMargin);
        leftMargin = Length.copy(from.leftMargin);
        backgroundImageX = Length.copy(from.backgroundImageX);
        backgroundImageY = Length.copy(from.backgroundImageY);
        sizeLimit = from.getSizeLimit();
        copyTextStyle(from);
    }

    protected static PageStyle createDefault(PaperSize p, Boolean isPortait, String name) {
        PageStyle tjs = new PageStyle(name);
        tjs.paperSize = p;
        tjs.orientationPortrait = isPortait;
        tjs.topMargin = defaultMargin;
        tjs.botMargin = defaultMargin;
        tjs.leftMargin = defaultMargin;
        tjs.rightMargin = defaultMargin;
        TextStyle.fillDefaultTextStyle4Page(tjs);
        return tjs;
    }

    public float getHeightInsideMargin() {
        float h = getPageHeight();
        if (topMargin != null)
            h -= topMargin.getValInPoints();
        if (botMargin != null)
            h -= botMargin.getValInPoints();
        return h;
    }

    public float getHeightToBottomMargin() {
        float h = getPageHeight();
        if (botMargin != null)
            h -= botMargin.getValInPoints();
        return h;
    }

    public float getBodyWidth() {
        return getPageWidth() - leftMargin.getValInPoints() - rightMargin.getValInPoints();
    }

    public static PageStyle getDefault() {
        return PredefinedStyles.instance().thePageStyleDefaultPageStyle;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void mergePageStyle(PageStyle from) {
        from = (PageStyle) StyleBase.getLocalMaster(from, true);
        if (paperSize == null)
            paperSize = from.paperSize;
        if (orientationPortrait == null)
            orientationPortrait = from.orientationPortrait;
        if (topMargin == null)
            topMargin = from.topMargin;
        if (rightMargin == null)
            rightMargin = from.rightMargin;
        if (botMargin == null)
            botMargin = from.botMargin;
        if (leftMargin == null)
            leftMargin = from.leftMargin;
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
        mergeTextStyle(from);
    }

    public void makeUniqueName(String start) {
        int cnt = 1;
        do {
            name = start + cnt++;
        }
        while (Env.instance().repo().pageStyles.get(name) >= 0);
    }

    /**
     * ----------------------------------------------------------------------- registerInRepository
     */
    public void registerInRepository() {
        if (name == null || name.equals(""))
            makeUniqueName(Res.str("Page"));
        Env.instance().repo().pageStyles.add(this);
    }

    public void setPaperSize(PaperSize newPaperSize) {
        paperSize = newPaperSize;
    }

    public void setOrientationPortrait(Boolean newOrientationPortrait) {
        orientationPortrait = newOrientationPortrait;
    }

    public Boolean isOrientationPortrait() {
        return orientationPortrait;
    }

    public float getPageHeight() {
        if (orientationPortrait)
            return getPaperSize().getHeight().getValInPoints();
        else
            return getPaperSize().getWidth().getValInPoints();
    }

    public float getPageWidth() {
        if (orientationPortrait)
            return getPaperSize().getWidth().getValInPoints();
        else
            return getPaperSize().getHeight().getValInPoints();
    }

    public int getRowsHeight() {
        return getPaperSize().getRows();
    }

    public int getColumnsWidth() {
        return getPaperSize().getColumns();
    }

    public void setBotMargin(Length newBotMarginn) {
        botMargin = newBotMarginn;
    }

    public Length getBotMargin() {
        return botMargin;
    }

    public void setTopMargin(Length newTopMargin) {
        topMargin = newTopMargin;
    }

    public Length getTopMargin() {
        return topMargin;
    }

    public void setLeftMargin(Length newLeftMargin) {
        leftMargin = newLeftMargin;
    }

    public Length getLeftMargin() {
        return leftMargin;
    }

    public void setRightMargin(Length newRightMargin) {
        rightMargin = newRightMargin;
    }

    public Length getRightMargin() {
        return rightMargin;
    }

    public static PageStyle duplicateLocal(PageStyle from) {
        if (from == null || from.name != null)
            return from;
        else
            return new PageStyle(from);
    }

    public PageFormat getPageFormat() {
        Paper pap = new Paper();
        pap.setSize(getPageWidth(), getPageHeight());
        pap.setImageableArea(getLeftMargin().getValInPoints(), getTopMargin().getValInPoints(), getBodyWidth(), getHeightInsideMargin());
        PageFormat paf = new PageFormat();
        paf.setPaper(pap);
        boolean portrait = isOrientationPortrait();
        paf.setOrientation(portrait ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE);
        return paf;
    }

    public PageFormat getPageFormatUnrotated() {
        Paper pap = new Paper();
        boolean portrait = isOrientationPortrait();
        float pW = getPaperSize().getWidth().getValInPoints();
        float pH = getPaperSize().getHeight().getValInPoints();
        pap.setSize(pW, pH);
        pap.setImageableArea(0, 0, pW, pH);
        PageFormat paf = new PageFormat();
        paf.setPaper(pap);
        paf.setOrientation(portrait ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE);
        return paf;
    }

    public PageFormat getPageFormatUnrotated(boolean scaleToA4) {
        PredefinedStyles stat = PredefinedStyles.instance();
        PaperSize paperSize = this.paperSize;
        if (stat.thePaperSizeA3 == paperSize && scaleToA4)
            paperSize = stat.thePaperSizeA4;
        Paper pap = new Paper();
        boolean portrait = isOrientationPortrait();
        float pW = paperSize.getWidth().getValInPoints();
        float pH = paperSize.getHeight().getValInPoints();
        pap.setSize(pW, pH);
        pap.setImageableArea(0, 0, pW, pH);
        PageFormat paf = new PageFormat();
        paf.setPaper(pap);
        paf.setOrientation(portrait ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE);
        return paf;
    }

    protected Object readResolve() throws java.io.ObjectStreamException {
        Object ret = super.readResolve();
        if (orientationPortrait == null)// fix broken styles
            orientationPortrait = Boolean.TRUE;
        return ret;
    }

    protected void storeSpecialStyle() {
        PredefinedStyles stat = PredefinedStyles.instance();
        if (StyleBase.eq(name, defaultPageStyleName))
            stat.thePageStyleDefaultPageStyle = this;
        else if (StyleBase.eq(name, A4LName) || StyleBase.eq(name, "A4 Landscape"))
            stat.thePageStyleA4L = this;
        else if (StyleBase.eq(name, A4PName) || StyleBase.eq(name, "A4 Portrait"))
            stat.thePageStyleA4P = this;
        else if (StyleBase.eq(name, A5LName) || StyleBase.eq(name, "A5 Landscape"))
            stat.thePageStyleA5L = this;
        else if (StyleBase.eq(name, A5PName) || StyleBase.eq(name, "A5 Portrait"))
            stat.thePageStyleA5P = this;
        else if (StyleBase.eq(name, LetterLName) || StyleBase.eq(name, "Letter Landscape"))
            stat.thePageStyleLetterL = this;
        else if (StyleBase.eq(name, LetterPName) || StyleBase.eq(name, "Letter Portrait"))
            stat.thePageStyleLetterP = this;
    }

    public SortedNamedVector<PageStyle> getGlobalStyleList() {
        return Env.instance().repo().pageStyles;
    }

    public double getScale(boolean scaleToA4) {
        PredefinedStyles stat = PredefinedStyles.instance();
        return scaleToA4 ? (stat.thePaperSizeA3 == paperSize ? stat.thePaperSizeA4.getHeight().getValInPoints() / stat.thePaperSizeA3.getHeight().getValInPoints() : 1) : 1;
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

    public SizeLimit getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(final SizeLimit sizeLimit) {
        this.sizeLimit = sizeLimit;
    }
}
