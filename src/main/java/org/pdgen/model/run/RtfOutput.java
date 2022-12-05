// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaDataException;
import org.pdgen.env.Res;
import org.pdgen.model.*;
import org.pdgen.model.cells.*;
import org.pdgen.model.style.*;
import org.pdgen.styledtext.model.StyleRunIterator;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.styledtext.model.StyledParagraphLayouter;
import org.pdgen.styledtext.model.StyledParagraphList;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.io.*;
import java.util.*;

/**
 * User: patrick
 * Date: Mar 22, 2006
 * Time: 2:12:22 PM
 */
public class RtfOutput extends NonPagedOutput {
    private final ArrayList<CellStyle> cellStyles;
    private final ArrayList<FrameStyle> frameStyles;
    private final ArrayList<TableStyle> tableStyles;
    private final ArrayList<JoriaSimpleBorder> cellBorders;
    private final Set<String> additionalFonts;
    private final Set<Color> additionalColors;
    private final boolean multiSection;
    private final PrintStream outstream;
    private final Properties fontNameIndex = new Properties();
    private final HashMap<Color, String> colorNameIndex = new HashMap<Color, String>();
    private boolean ignoreFrame;
    private boolean headerOrFooter;
    private static final float POINT2TWIPS = 20;
    private float[] colPos;
    private OutputData[] data;
    private int dynaCols;
    private float leftMargin;
    private float[] colWidths;
    private static final String PAGENUMBER_STRING = "{\\field{\\*\\fldinst {PAGE}}{\\fldrslt 1}}";//"\\chpgn";//
    private static final String PAGENUMBER_PLACEHOLDER = "####PageNumber####";
    private static final String TOTALPAGES_STRING = "{\\field{\\*\\fldinst {NUMPAGES}}{\\fldrslt 1}}";//"\\chpgn";//
    private static final String TOTALPAGES_PLACEHOLDER = "####TotalPages####";
    private TemplateModel template;
    private float rowHeight;
    private boolean unbreakableFrame;
    private String startParagraph;
    private boolean firstLine;
    private boolean hasOneRow;
    private FrameStyle fs;
    private boolean hasFirstHeader;
    private boolean hasFirstFooter;
    private int frameType;
    private final CellStyle dummyStyle;
    private boolean lastFrameAbsolutePosition;

    protected RtfOutput(RunEnvImpl env, ArrayList<CellStyle> cellStyles, ArrayList<FrameStyle> frameStyles, ArrayList<TableStyle> tableStyles, ArrayList<JoriaSimpleBorder> cellBorders, Set<String> additionalFonts, Set<Color> additionalColors, OutputStream out, boolean multiSection) throws UnsupportedEncodingException {
        super(env);
        this.cellStyles = cellStyles;
        this.frameStyles = frameStyles;
        this.tableStyles = tableStyles;
        this.cellBorders = cellBorders;
        this.additionalFonts = additionalFonts;
        this.additionalColors = additionalColors;
        this.multiSection = multiSection;
        if (!(out instanceof BufferedOutputStream || out instanceof ByteArrayOutputStream))
            out = new BufferedOutputStream(out);
        outstream = new PrintStream(out, false, "Cp1252");
        dummyStyle = new CellStyle();
        dummyStyle.mergeDefaults();
    }

    protected boolean addNullCellInt(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat) throws JoriaDataException {
        out.colInCurrRow++;
        if (!ignoreFrame) {
            if (template.getCoverCell(row + rr.getRowOffset(), col + rr.getColOffset()) == null) {
                FlexSize rowHeight = template.getRowSizingAt(row + rr.getRowOffset());
                if (!rowHeight.isExpandable())
                    this.rowHeight = rowHeight.getVal();
                else
                    this.rowHeight = 0;
                data[col + rr.getColOffsetData()].init(rr, cd, row + rr.getRowOffset(), col + rr.getColOffsetData());
                data[col + rr.getColOffsetData()].isNullCell = true;
            }
            if (rowHeight > 0)
                hasOneRow = true;
        }
        return false;
    }

    protected void addSpanInt(int count) throws JoriaDataException {
        if (!ignoreFrame) {
        }
    }

    public static class OutputData {
        private RunRangeBase rr;
        private CellDef cd;
        private int row = -1;
        private int c = -1;
        public int iteration;
        private boolean isNullCell;

        private void init(RunRangeBase rr, CellDef cd, int row, int c) {
            this.rr = rr;
            this.cd = cd;
            this.row = row;
            this.c = c;
            iteration = rr.getIteration();
            isNullCell = false;
        }

        private void nullify() {
            rr = null;
            cd = null;
            row = -1;
            c = -1;
        }

        private boolean isNull() {
            return c == -1;
        }
    }

    protected void generateOutputInt(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException {
        if (!ignoreFrame) {
            FlexSize rowHeight = template.getRowSizingAt(row + rr.getRowOffset());
            if (!rowHeight.isExpandable())
                this.rowHeight = rowHeight.getVal();
            else
                this.rowHeight = 0;
            data[c + rr.getColOffsetData()].init(rr, cd, row + rr.getRowOffset(), c + rr.getColOffsetData());
            hasOneRow = true;
        }
    }

    protected void doOneCellPos(OutputData data) {
        float startpos = colPos[out.colInCurrRow];
        CellStyle cs = dummyStyle;
        if (data.cd != null)
            cs = data.cd.getCascadedStyle();
        if (data.isNull() || data.rr.values == null || data.cd == null) {
            out.colInCurrRow++;
        } else {
            int colspan = Math.min(cs.getSpanHorizontal().intValue(), data.cd.getGrid().getColCount());
            if (colspan > 1)
                out.colInCurrRow += colspan - 1;
            out.colInCurrRow++;
        }
        float colWidth = colPos[out.colInCurrRow] - startpos;
        if (colWidth > 0) {
            if (data.cd != null) {
                VerticalAlignment vjust = cs.getAlignmentVertical();
                if (VerticalAlignment.BOTTOM.equals(vjust))
                    outstream.print("\\clvertalb");
                else if (VerticalAlignment.MIDDLE.equals(vjust))
                    outstream.print("\\clvertalc");
                else if (VerticalAlignment.TOP.equals(vjust))
                    outstream.print("\\clvertalt");
            }
            if (!data.isNull()) {
                JoriaSimpleBorder topBorder = template.getBorderAt(data.row, data.c, true, true);
                JoriaSimpleBorder leftBorder = template.getBorderAt(data.row, data.c, false, true);
                JoriaSimpleBorder bottomBorder = template.getBorderAt(data.row + cs.getSpanVertical() - 1, data.c, true, false);
                JoriaSimpleBorder rightBorder = template.getBorderAt(data.row, data.c + cs.getSpanHorizontal() - 1, false, false);
                if (cs.getSpanHorizontal() > 1) {
                    for (int i = 1; i < cs.getSpanHorizontal(); i++) {
                        if (!template.getBorderAt(data.row, data.c + i, true, true).equals(topBorder)) {
                            topBorder = null;
                        }
                        if (!template.getBorderAt(data.row + cs.getSpanVertical() - 1, data.c + i, true, false).equals(bottomBorder)) {
                            bottomBorder = null;
                        }
                    }
                    for (int i = 1; i < cs.getSpanVertical(); i++) {
                        if (!template.getBorderAt(data.row + i, data.c, false, true).equals(leftBorder))
                            leftBorder = null;
                        if (!template.getBorderAt(data.row + i, data.c + cs.getSpanHorizontal() - 1, false, false).equals(rightBorder))
                            rightBorder = null;
                    }
                }
                outputBorder(topBorder, "t", false);
                outputBorder(leftBorder, "l", false);
                outputBorder(bottomBorder, "b", false);
                outputBorder(rightBorder, "r", false);
            }
            outstream.print("\\clftsWidth3\\clwWidth");
            outputTwips(colWidth);
            if (data.cd != null) {
                outputCellPadding(cs.getTopPadding(), "t");
                outputCellPadding(cs.getLeftPadding(), "l");
                outputCellPadding(cs.getBottomPadding(), "b");
                outputCellPadding(cs.getRightPadding(), "r");
            }
            outstream.print("\\cellx");
            outputTwips(colPos[out.colInCurrRow] - leftMargin);
            outstream.println();
        }
    }

    private void outputFramePadding(JoriaFrameBorder jb, String border) {
        if (jb != null) {
            if (jb.getInnerPadding().getValInPoints() > 0) {
                outstream.print("\\trpadd" + border);
                outputTwips(jb.getInnerPadding().getValInPoints());
                outstream.print("\\trpaddf" + border + "3");
            }
        }
    }

    private void outputCellPadding(Length length, String border) {
        if (length != null) {
            if (length.getValInPoints() > 0) {
                outstream.print("\\clpad" + border);
                outputTwips(length.getValInPoints());
                outstream.print("\\clpadf" + border + "3");
            }
        }
    }

    private void outputFrameSpacing(JoriaFrameBorder jb, String border) {
        if (jb != null) {
            if (jb.getOuterSpacing().getValInPoints() > 0) {
                outstream.print("\\trspd" + border);
                outputTwips(jb.getOuterSpacing().getValInPoints());
                outstream.print("\\trspdf" + border + "3");
            }
        }
    }

    private void outputBorder(JoriaSimpleBorder jb, String border, boolean frame) {
        if (!JoriaSimpleBorder.isNull(jb)) {
            outstream.print("\\" + (frame ? "tr" : "cl") + "brdr" + border);
            if (jb.getThickness() <= 0) {
                outstream.print("\\brdrnil");
                return;
            }
            switch (jb.getLineStyle()) {
                case JoriaBorder.NONE:
                    outstream.print("\\brdrnil");
                    return;
                case JoriaBorder.SOLID:
                    outstream.print("\\brdrs");
                    break;
                case JoriaBorder.DOT:
                    outstream.print("\\brdrdot");
                    break;
                case JoriaBorder.DASH:
                    outstream.print("\\brdrdash");
                    break;
                case JoriaBorder.DOUBLE:
                    outstream.print("\\brdrdb");
                    break;
            }
            outstream.print("\\brdrw");
            if (jb.getThickness() >= 75 / POINT2TWIPS)
                outstream.print("75");
            else
                outputTwips(jb.getThickness());
            outstream.print("\\brdrcf" + colorNameIndex.get(jb.getColor()));
        }
    }

    protected void doOneCell(OutputData data) throws JoriaDataException {
        if (data.isNull() || data.cd == null || data.isNullCell) {
            if (colWidths[out.colInCurrRow] > 0)
                outstream.println("\\pard\\intbl\\cell");
            out.colInCurrRow++;
            return;
        }
        RunRangeBase rr = data.rr;
        CellDef cd = data.cd;
        int row = data.row;
        int c = data.c;
        if (rr.values == null) {
            if (colWidths[out.colInCurrRow] > 0)
                outstream.println("\\pard\\intbl\\cell");
            out.colInCurrRow++;
            return;
        }
        outstream.print("\\pard\\intbl\\plain");
        CellStyle cs = cd.getCascadedStyle();
        if (!headerOrFooter) {
            if (fs.getWidth() != null && !fs.getWidth().isExpandable()) {
                outstream.print("\\absw");
                outputTwips(fs.getWidth().getVal());
                outstream.println();
            }
            if (fs.getHeight() != null && !fs.getHeight().isExpandable()) {
                outstream.print("\\absh");
                outputTwips(fs.getWidth().getVal());
                outstream.println();
            }
            if (fs.getXPos() != null && !fs.getXPos().isExpandable()) {
                outstream.print("\\phpg");
                float pos = fs.getXPos().getVal();
                if (pos < 0) {
                    outstream.print("\\posnegx");
                    pos = -pos;
                } else
                    outstream.print("\\posx");
                outputTwips(pos);
                outstream.println();
            }
            if (fs.getYPos() != null && !fs.getYPos().isExpandable()) {
                outstream.print("\\pvpg");
                float pos = fs.getYPos().getVal();
                if (pos < 0) {
                    outstream.print("\\posnegy");
                    pos = -pos;
                } else
                    outstream.print("\\posy");
                outputTwips(pos);
                outstream.println();
            }
        }
        int colspan = Math.min(cs.getSpanHorizontal().intValue(), cd.getGrid().getColCount());
        int rowspan = cs.getSpanVertical();
        if (colspan > 1) {
            out.colInCurrRow += colspan - 1;
        }
        if (rowspan > 1) {
        }
        if (cs.getBreakable() == null || !cs.getBreakable()) {
            outstream.print("\\keep");
        }
        HorizontalAlignment just = cs.getAlignmentHorizontal();
        if (HorizontalAlignment.LEFT.equals(just))
            outstream.print("\\ql");
        else if (HorizontalAlignment.RIGHT.equals(just))
            outstream.print("\\qr");
        else if (HorizontalAlignment.CENTER.equals(just))
            outstream.print("\\qc");
        else if (HorizontalAlignment.BLOCK.equals(just))
            outstream.print("\\qj");
        String col = colorNameIndex.get(cs.getForeground());
        if (col == null)
            throw new JoriaAssertionError("unexpected foreground color");
        outstream.print("\\cf" + col);
        col = colorNameIndex.get(cs.getBackground());
        if (col == null)
            throw new JoriaAssertionError("unexpected background color");
        //outstream.print("\\chshdng0\\chcbpat"+col+"\\shading0\\cbpat"+col+"\\cb"+col);
        outstream.print("\\shading0\\cbpat" + col + "\\cb" + col);
        String font = fontNameIndex.getProperty(cs.getFont());
        outstream.print(font);
        int fontsize = Math.round(cs.getSize().getValInPoints() * 2);
        outstream.print("\\fs" + fontsize);
        if (cs.getBold())
            outstream.print("\\b");
        if (cs.getItalic())
            outstream.print("\\i");
        if (cs.getUnderlined())
            outstream.print("\\ul");
        outstream.print(" ");// End of style
        // TODO styles
        RVAny value = rr.values.subs[row - rr.getRowOffset()][c - rr.getColOffsetData()];
        if (value instanceof RVImage) {
            RVImage ri = (RVImage) value;
            byte[] pictureData = ri.getPictureData();
            Icon icon = ri.getPicture();
            float fixWidth = 0;
            FlexSize flexWidth = ((PictureCellBase) cd).getTargetWidth();
            if (flexWidth != null && !flexWidth.isExpandable())
                fixWidth = flexWidth.getVal();
            float scale;
            if (fixWidth != 0)
                scale = fixWidth / icon.getIconWidth();
            else
                scale = Float.NaN;
            outputImage(pictureData, icon, scale);
        } else if (cd instanceof RenderingCellDef) {
            ((RenderingCellDef) cd).outputToRTF(this, data, value, colWidths[c]);
        } else if (value instanceof RVImageCol) {
            int iter = data.iteration;
            RVImageCol ric = (RVImageCol) value;
            byte[] pictureData = null;
            Object storedData = ric.storedData[iter];
            if (storedData instanceof byte[])
                pictureData = (byte[]) storedData;
            Icon image = ric.images[iter];
            float fixWidth = 0;
            FlexSize flexWidth = ((PictureCellBase) cd).getTargetWidth();
            if (flexWidth != null && !flexWidth.isExpandable())
                fixWidth = flexWidth.getVal();
            float scale;
            if (fixWidth != 0)
                scale = fixWidth / image.getIconWidth();
            else
                scale = Float.NaN;
            outputImage(pictureData, image, scale);
        } else if (value instanceof RVStyledText || value instanceof RVStyledTextCol) {
            StyledParagraphList list;
            if (value instanceof RVStyledText) {
                list = ((RVStyledText) value).getText();
            } else {
                list = ((RVStyledTextCol) value).getText(data.iteration);
            }
            for (int i = 0; list != null && i < list.length(); i++) {
                StyledParagraph paragraph = list.get(i);
                if (i != 0)
                    outstream.println("\\par");
                float spaceBelow = paragraph.getSpaceBelow();
                outstream.print("\\sa");
                outputTwips(spaceBelow);
                if (paragraph.getRowSpacing() > 1.0)
                    //                    outstream.print("\\sl0");
                    outstream.print("\\sl" + (int) (paragraph.getRowSpacing() * 240) + "\\slmult1");
                else
                    outstream.print("\\sl0");
                int alignment = paragraph.getAlignment();
                if (alignment == StyledParagraph.alignLeft)
                    outstream.print("\\ql");
                else if (alignment == StyledParagraph.alignRight)
                    outstream.print("\\qr");
                else if (alignment == StyledParagraph.alignCenter)
                    outstream.print("\\qc");
                else if (alignment == StyledParagraph.alignJustified)
                    outstream.print("\\qj");
                StyledParagraphLayouter layouter = new StyledParagraphLayouter(paragraph);
                layouter.recalc(0, new FontRenderContext(null, true, false), 2000000.0f, 0);
                StyleRunIterator iterator = new StyleRunIterator(layouter);
                boolean bold = false;
                boolean superscript = false;
                boolean italic = false;
                boolean underline = false;
                while (iterator.nextRun()) {
                    boolean set = false;
                    if (iterator.isNewForegroundColor()) {
                        Color fc;
                        if (iterator.getForegroundColor() != null) {
                            fc = iterator.getForegroundColor();
                            col = colorNameIndex.get(iterator.getForegroundColor());
                        } else {
                            fc = cs.getForeground();
                            col = colorNameIndex.get(cs.getForeground());
                        }
                        if (col == null)
                            throw new JoriaAssertionError("unexpected foreground color " + fc);
                        outstream.print("\\cf" + col);
                        set = true;
                    }
                    if (iterator.isNewBackgroundColor()) {
                        Color bc;
                        if (iterator.getBackgroundColor() != null) {
                            bc = iterator.getBackgroundColor();
                            col = colorNameIndex.get(iterator.getBackgroundColor());
                        } else {
                            bc = cs.getBackground();
                            col = colorNameIndex.get(cs.getBackground());
                        }
                        if (col == null)
                            throw new JoriaAssertionError("unexpected background color " + bc);
                        outstream.print("\\chshdng0\\chcbpat" + col + "\\shading0\\cbpat" + col + "\\cb" + col);
                        set = true;
                    }
                    if (iterator.isNewSuperscript() && !iterator.isSuperscript()) {
                        superscript = false;
                        outstream.print("}");
                        set = true;
                    }
                    if (iterator.isNewFontFamily()) {
                        String font2 = fontNameIndex.getProperty(iterator.getFontFamily());
                        outstream.print(font2);
                        set = true;
                    }
                    if (iterator.isNewSize()) {
                        int fs2 = Math.round(iterator.getSize() * 2);
                        outstream.print("\\fs" + fs2);
                        set = true;
                    }
                    if (iterator.isNewBold()) {
                        bold = iterator.isBold();
                        if (iterator.isBold())
                            outstream.print("\\b");
                        else
                            outstream.print("\\b0");
                        set = true;
                    }
                    if (iterator.isNewItalic()) {
                        italic = iterator.isItalic();
                        if (iterator.isItalic())
                            outstream.print("\\i");
                        else
                            outstream.print("\\i0");
                        set = true;
                    }
                    if (iterator.isNewUnderlined()) {
                        underline = iterator.isUnderlined();
                        if (iterator.isUnderlined())
                            outstream.print("\\ul");
                        else
                            outstream.print("\\ul0");
                        set = true;
                    }
                    if (iterator.isNewSuperscript() && iterator.isSuperscript()) {
                        superscript = true;
                        outstream.print("{\\super");
                        set = true;
                    }
                    if (set)
                        outstream.println();
                    String text = iterator.getText();
                    outputData(text);
                }
                boolean set = false;
                if (superscript) {
                    outstream.print("}");
                    set = true;
                }
                if (bold) {
                    outstream.print("\\b0");
                    set = true;
                }
                if (italic) {
                    outstream.print("\\i0");
                    set = true;
                }
                if (underline) {
                    outstream.print("\\ul0");
                    set = true;
                }
                if (set)
                    outstream.println();
            }
        } else if (value instanceof RValue) {
            RValue val = (RValue) value;
            if (val instanceof RVStringCol) {
                ((RVStringCol) val).buildFormattedStrings(cd, aggs.getRunEnv().getLocale());
            }
            if (cd instanceof DataCellDef && ((DataCellDef) cd).getAggregates() != null)
                val.accumulate(aggs, ((DataCellDef) cd).getAggregates(), data.iteration);
            boolean inPageCell = (cd instanceof PagestotalCell || cd instanceof PagenoCell);
            String fs = val.get(data.iteration);
            if (cd instanceof SummaryCell)
                fs = cd.getFormattedString(null, aggs);
            //System.out.println(fs);
            if (fs == null) {
                // nothing to output here
            } else if (BasicHTML.isHTMLString(fs) || cd.getCascadedStyle().getTextType().equals(CellStyle.htmlType)) {
                StringBuffer outBuffer = new StringBuffer(fs.length());
                for (int i = 0; i < fs.length(); i++) {
                    char ch = fs.charAt(i);
                    if (ch == '<') {
                        int end = fs.indexOf('>', i);
                        if (end != -1) {
                            String element = fs.substring(i + 1, end).toLowerCase();
                            boolean endElement = (element.charAt(0) == '/');
                            if (endElement)
                                element = element.substring(1);
                            i = end;
                            if (element.startsWith("i")) {
                                outputData(outBuffer.toString());
                                outBuffer.delete(0, outBuffer.length());
                                if (endElement)
                                    outstream.print("\\i0");
                                else
                                    outstream.print("\\i");
                            } else if (element.startsWith("br")) {
                                outputData(outBuffer.toString());
                                outBuffer.delete(0, outBuffer.length());
                                outstream.print("\\line");
                            } else if (element.startsWith("b")) {
                                outputData(outBuffer.toString());
                                outBuffer.delete(0, outBuffer.length());
                                if (endElement)
                                    outstream.print("\\b0");
                                else
                                    outstream.print("\\b");
                            }
                        }
                    } else if (ch == '&') {
                        int end = fs.indexOf('>', i);
                        if (end != -1) {
                            String replacement = fs.substring(i + 1, end).toLowerCase();
                            if (replacement.charAt(0) == '#') {
                                ch = (char) Integer.parseInt(replacement.substring(1), 16);
                                i = end;
                            } else {
                                for (HtmlReplacement htmlReplacement : RtfOutput.replace) {
                                    if (htmlReplacement.key.equals(replacement)) {
                                        ch = htmlReplacement.value;
                                        i = end;
                                        break;
                                    }
                                }
                            }
                            outBuffer.append(ch);
                        }
                    } else
                        outBuffer.append(ch);
                }
                outputData(outBuffer.toString());
            } else if (cd.getCascadedStyle().getTextType().equals(CellStyle.rtfType)) {
                throw new JoriaDataException("cannot embed rtf in a rtf document");
            } else {
                StringTokenizer st = new StringTokenizer(fs, "\n\r\f");
                boolean one = false;
                while (st.hasMoreTokens()) {
                    String t = st.nextToken();
                    if (one) {
                        outstream.println("\\line");
                    } else
                        one = true;
                    outputData(t, inPageCell);
                }
            }
        }
        outstream.println("\\cell");
        out.colInCurrRow++;
    }

    public void outputImage(byte[] pictureData, Icon icon, float scale) throws JoriaDataException {
        try {
            ImageDetection.ImageClass ic;
            if (pictureData != null) {
                ic = ImageDetection.detectImageClass(pictureData);
                if (ic == null || ic == ImageDetection.GIF) {
                    ImageDetection.ImageHolder ih = ImageDetection.recodeImage(pictureData);
                    pictureData = ih.getImageData();
                    ic = ih.getImageClass();
                }
            } else {
                ImageDetection.ImageHolder ih = ImageDetection.recodeIcon(icon);
                pictureData = ih.getImageData();
                ic = ih.getImageClass();
            }
            outstream.print("{\\*\\shppict{\\pict");
            if (ic == ImageDetection.JPEG) {
                outstream.print("\\jpegblip");
            } else if (ic == ImageDetection.PNG) {
                outstream.print("\\pngblip");
            } else {
                throw new JoriaDataException("unsupported image format for RTF " + ic.getDefaultMimeType());
            }
            outstream.print("\\picw" + icon.getIconWidth() + "\\pich" + icon.getIconHeight());
            if (Float.isNaN(scale)) {
                outstream.print("\\picwgoal");
                outputTwips(icon.getIconWidth());
                outstream.print("\\pichgoal");
                outputTwips(icon.getIconHeight());
            } else {
                outstream.print("\\picwgoal");
                outputTwips(icon.getIconWidth() * scale);
                outstream.print("\\pichgoal");
                outputTwips(icon.getIconHeight() * scale);
            }
            outstream.print(" ");
            for (int i = 0; i < pictureData.length; i++) {
                if (i % 30 == 0)
                    outstream.println();
                byte b = pictureData[i];
                outstream.print(toHex(b));
            }
            outstream.println();
            outstream.println("}}");
        } catch (IOException e) {
            throw new JoriaDataException("A problem displaying the image exists", e);
        }
    }

    protected void processOneFrame(RDBase[][] fields, RVTemplate rvt, TemplateModel template) {
        if (!ignoreFrame) {
            this.template = template;
            if (template != null) {
                fs = template.getFrame().getCascadedFrameStyle();
                boolean positioned = fs.getXPos() != null && !fs.getXPos().isExpandable() || fs.getYPos() != null && !fs.getYPos().isExpandable();
                if (fs.getOnNewPage() != null && fs.getOnNewPage()) {
                    outstream.println("\\pagebb\\par");
                } else if (lastFrameAbsolutePosition && !positioned) {
                    outstream.print("\\pard\\plain\\par");
                }
                lastFrameAbsolutePosition = positioned;
                int nCols = template.getColCount();
                if (template.getCrosstab() != null)
                    dynaCols = rvt.subs[0].length;
                else
                    dynaCols = nCols;
                if (!headerOrFooter) {
                    unbreakableFrame = fs.getBreakable() != null && !fs.getBreakable();
                }
                float[][] colReq = new float[template.getRowCount()][dynaCols];
                Repeater[][] tableHeaderCols = new Repeater[template.getRowCount()][nCols];
                template.getRepeaterList().getRepeaterForHeaders(tableHeaderCols);
                out.rdef.calcMaxWidth(colReq, rvt, env.getLocale(), env.getGraphics2D());
                colPos = new float[dynaCols + 1];
                if (template.getRowCount() == 0 || nCols == 0)
                    return;
                colWidths = new float[dynaCols];
                float[] maxColWidths = new float[dynaCols];
                ColumnLayouter layouter = new ColumnLayouter(colWidths, maxColWidths, colPos, colReq, dynaCols, template, env.getTemplate().getPage().getCascadedPageStyle());
                layouter.layout();
                data = new OutputData[dynaCols];
                for (int i = 0; i < data.length; i++) {
                    data[i] = new OutputData();
                }
            }
        }
    }

    public String getTotalPageNumberPlaceHolderInt() {
        return TOTALPAGES_PLACEHOLDER;
    }

    public String getPageNumberInt() {
        return PAGENUMBER_PLACEHOLDER;
    }

    protected void preprocess() {
        // hier kommt der Vorspann hin
        outstream.println("{\\rtf1\\ansi\\ansicpg1252\\deff0\\uc0 ");
        doDeflang();
        // font table
        outstream.println("{\\fonttbl");
        for (CellStyle cellStyle : cellStyles) {
            String font = cellStyle.getFont();
            if (fontNameIndex.get(font) == null) {
                outstream.print("{");
                String fontIndex = "\\f" + fontNameIndex.size();
                fontNameIndex.put(font, fontIndex);
                outstream.print(fontIndex + " " + font + ";}");
            }
        }
        for (String font : additionalFonts) {
            if (fontNameIndex.get(font) == null) {
                outstream.print("{");
                String fontIndex = "\\f" + fontNameIndex.size();
                fontNameIndex.put(font, fontIndex);
                outstream.print(fontIndex + " " + font + ";}");
            }
        }
        outstream.println("}");
        // color table
        outstream.print("{\\colortbl;");
        for (CellStyle cellStyle : cellStyles) {
            Color col = cellStyle.getForeground();
            addColorToTable(col);
            col = cellStyle.getBackground();
            addColorToTable(col);
        }
        for (Color additionalColor : additionalColors) {
            addColorToTable(additionalColor);
        }
        for (FrameStyle frameStyle : frameStyles) {
            Color col = frameStyle.getBackground();
            addColorToTable(col);
            JoriaFrameBorder border = frameStyle.getTopBorder();
            if (!JoriaSimpleBorder.isNull(border))
                addColorToTable(border.getColor());
            border = frameStyle.getLeftBorder();
            if (!JoriaSimpleBorder.isNull(border))
                addColorToTable(border.getColor());
            border = frameStyle.getBottomBorder();
            if (!JoriaSimpleBorder.isNull(border))
                addColorToTable(border.getColor());
            border = frameStyle.getRightBorder();
            if (!JoriaSimpleBorder.isNull(border))
                addColorToTable(border.getColor());
        }
        for (TableStyle tableStyle : tableStyles) {
            Color col = tableStyle.getTableBackground();
            addColorToTable(col);
        }
        for (JoriaSimpleBorder joriaSimpleBorder : cellBorders) {
            addColorToTable(joriaSimpleBorder.getColor());
        }
        outstream.println("}");
        outstream.println("{\\*\\generator " + Res.version + ";}");
        outstream.print("{\\info{\\title ");
        outputData(env.getTemplate().getName());
        outstream.println("}}");
        PageMaster pageMaster = env.getTemplate().getPage();
        PageStyle ps = pageMaster.getCascadedPageStyle();
        outstream.print("\\paperh");
        outputTwips(ps.getPageHeight());
        outstream.println();
        outstream.print("\\paperw");
        outputTwips(ps.getPageWidth());
        outstream.println();
        if (ps.getPageWidth() > ps.getPageHeight())
            outstream.println("\\landscape");
        outstream.print("\\margl");
        outputTwips(ps.getLeftMargin().getValInPoints());
        leftMargin = ps.getLeftMargin().getValInPoints();
        outstream.println();
        outstream.print("\\margr");
        outputTwips(ps.getRightMargin().getValInPoints());
        outstream.println();
        outstream.print("\\margt");
        outputTwips(ps.getTopMargin().getValInPoints());
        outstream.println();
        outstream.print("\\margb");
        outputTwips(ps.getBotMargin().getValInPoints());
        outstream.println();
        outstream.print("\\headery");
        float headerHeight = Math.max(pageMaster.getHeaderHeight(PageLevelBox.firstPage), pageMaster.getHeaderHeight(PageLevelBox.middlePage));
        outputTwips(ps.getTopMargin().getValInPoints() - headerHeight);
        outstream.println();
        outstream.print("\\footery");
        float footerHeight = Math.max(pageMaster.getFooterHeight(PageLevelBox.firstPage), pageMaster.getFooterHeight(PageLevelBox.middlePage));
        outputTwips(ps.getTopMargin().getValInPoints() - footerHeight);
        outstream.println();
        outstream.println("\\formshade");
        outstream.println("\\nolnhtadjtbl");
    }

    private void addColorToTable(Color col) {
        if (col != null && colorNameIndex.get(col) == null) {
            String colorIndex = Integer.toString(colorNameIndex.size() + 1);
            colorNameIndex.put(col, colorIndex);
            outstream.print("\\red" + col.getRed() + "\\green" + col.getGreen() + "\\blue" + col.getBlue() + ";");
        }
    }

    private void doDeflang() {
        Locale loc = env.getLocale();
        String lang = loc.getLanguage();
        String country = loc.getCountry();
        int langKey = 1024;
        if ("ar".equalsIgnoreCase(lang)) {
            if ("AE".equalsIgnoreCase(country))
                langKey = 14337;
            else if ("BH".equalsIgnoreCase(country))
                langKey = 15361;
            else if ("DZ".equalsIgnoreCase(country))
                langKey = 5121;
            else if ("EG".equalsIgnoreCase(country))
                langKey = 3073;
            else if ("IQ".equalsIgnoreCase(country))
                langKey = 2049;
            else if ("JO".equalsIgnoreCase(country))
                langKey = 11265;
            else if ("KW".equalsIgnoreCase(country))
                langKey = 13313;
            else if ("LB".equalsIgnoreCase(country))
                langKey = 12289;
            else if ("LY".equalsIgnoreCase(country))
                langKey = 4097;
            else if ("MA".equalsIgnoreCase(country))
                langKey = 6145;
            else if ("OM".equalsIgnoreCase(country))
                langKey = 8193;
            else if ("QA".equalsIgnoreCase(country))
                langKey = 16385;
            else if ("SA".equalsIgnoreCase(country))
                langKey = 1025;
            else if ("SD".equalsIgnoreCase(country))
                langKey = 1025;
            else if ("SY".equalsIgnoreCase(country))
                langKey = 10241;
            else if ("TN".equalsIgnoreCase(country))
                langKey = 7169;
            else if ("YE".equalsIgnoreCase(country))
                langKey = 9217;
            else
                langKey = 1025;
        } else if ("hi".equalsIgnoreCase(lang)) {
            langKey = 1081;
        } else if ("iw".equalsIgnoreCase(lang)) {
            langKey = 1024;
        } else if ("ja".equalsIgnoreCase(lang)) {
            langKey = 1041;
        } else if ("ko".equalsIgnoreCase(lang)) {
            langKey = 1042;
        } else if ("th".equalsIgnoreCase(lang)) {
            langKey = 1054;
        } else if ("vi".equalsIgnoreCase(lang)) {
            langKey = 1066;
        } else if ("zh".equalsIgnoreCase(lang)) {
            if ("CN".equalsIgnoreCase(country))
                langKey = 2052;
            else if ("HK".equalsIgnoreCase(country))
                langKey = 3076;
            else if ("TW".equalsIgnoreCase(country))
                langKey = 1028;
            else
                langKey = 1028;
        } else if ("be".equalsIgnoreCase(lang)) {
            langKey = 1059;
        } else if ("ca".equalsIgnoreCase(lang)) {
            langKey = 1027;
        } else if ("cz".equalsIgnoreCase(lang)) {
            langKey = 1029;
        } else if ("da".equalsIgnoreCase(lang)) {
            langKey = 1030;
        } else if ("de".equalsIgnoreCase(lang)) {
            if ("DE".equalsIgnoreCase(country))
                langKey = 1031;
            else if ("CH".equalsIgnoreCase(country))
                langKey = 2055;
            else if ("AT".equalsIgnoreCase(country))
                langKey = 3079;
            else if ("LU".equalsIgnoreCase(country))
                langKey = 4103;
            else
                langKey = 1031;
        } else if ("el".equalsIgnoreCase(lang)) {
            langKey = 1032;
        } else if ("en".equalsIgnoreCase(lang)) {
            if ("AU".equalsIgnoreCase(country)) {
                langKey = 3081;
            } else if ("CA".equalsIgnoreCase(country)) {
                langKey = 4105;
            } else if ("GB".equalsIgnoreCase(country)) {
                langKey = 2057;
            } else if ("IE".equalsIgnoreCase(country)) {
                langKey = 6153;
            } else if ("IN".equalsIgnoreCase(country)) {
                langKey = 1033;
            } else if ("NZ".equalsIgnoreCase(country)) {
                langKey = 5129;
            } else if ("ZA".equalsIgnoreCase(country)) {
                langKey = 7177;
            } else {
                langKey = 1033;
            }
        }
        outstream.print("\\deflang" + langKey);
        //TODO deflang
    }

    protected void postprocess() {
        // hier kommt der Nachspan hin
        outstream.println("}");
        outstream.flush();
    }

    protected boolean includeHeaderFooterFrames() {
        return true;
    }

    protected void startOneFrame(int frameType) {
        this.frameType = frameType;
        switch (frameType) {
            case NonPagedOutput.CONTENT_FRAME:
                startParagraph = "{";
                break;
            case NonPagedOutput.FIRST_HEADER_FRAME:
                startParagraph = "{\\headerf";
                headerOrFooter = true;
                hasFirstHeader = true;
                break;
            case NonPagedOutput.FURTHER_HEADER_FRAME:
                startParagraph = "{\\header";
                headerOrFooter = true;
                break;
            case NonPagedOutput.FIRST_FOOTER_FRAME:
                startParagraph = "{\\footerf";
                headerOrFooter = true;
                hasFirstFooter = true;
                break;
            case NonPagedOutput.MIDDLE_FOOTER_FRAME:
                startParagraph = "{\\footer";
                headerOrFooter = true;
                break;
            case NonPagedOutput.LAST_FOOTER_FRAME:
                ignoreFrame = true;
                break;
        }
        if (!ignoreFrame) {
            firstLine = true;
        }
    }

    protected void endOneFrame() throws JoriaDataException {
        if (!ignoreFrame && template != null) {
            outputOneRow(true);
            hasOneRow = false;
        }
        if (ignoreFrame)
            ignoreFrame = false;
        else if (startParagraph == null) {
            outstream.println("}");
        }
        headerOrFooter = false;
        unbreakableFrame = false;
    }

    protected void startOneRow() throws JoriaDataException {
        if (!ignoreFrame) {
            if (hasOneRow) {
                outputOneRow(false);
                for (OutputData aData : data) {
                    aData.nullify();
                }
            }
            hasOneRow = false;
        }
    }

    protected void endOneRow() throws JoriaDataException {
        //if(!ignoreFrame)
        //    hasOneRow = true;
    }

    private void outputOneRow(boolean lastLine) throws JoriaDataException {
        if (colPos[colPos.length - 1] - leftMargin > 0) {
            if (startParagraph != null) {
                switch (frameType) {
                    case NonPagedOutput.FIRST_HEADER_FRAME:
                        hasFirstHeader = true;
                        break;
                    case NonPagedOutput.FURTHER_HEADER_FRAME:
                        if (!hasFirstHeader)
                            outstream.println("{\\headerf\\pard\\par}");
                        break;
                    case NonPagedOutput.FIRST_FOOTER_FRAME:
                        hasFirstFooter = true;
                        break;
                    case NonPagedOutput.MIDDLE_FOOTER_FRAME:
                        if (!hasFirstFooter)
                            outstream.println("{\\footerf\\pard\\par}");
                        break;
                }
                outstream.println(startParagraph);
                startParagraph = null;
            }
            float maxPadding = 0;
            for (OutputData outputData : data) {
                if (!outputData.isNull()) {
                    CellStyle cs = dummyStyle;
                    if (outputData.cd != null)
                        cs = outputData.cd.getCascadedStyle();
                    if (cs != null) {
                        maxPadding = Math.max(maxPadding, cs.getTopPadding().getValInPoints());
                        maxPadding = Math.max(maxPadding, cs.getLeftPadding().getValInPoints());
                        maxPadding = Math.max(maxPadding, cs.getRightPadding().getValInPoints());
                        maxPadding = Math.max(maxPadding, cs.getBottomPadding().getValInPoints());
                    }
                }
            }
            outstream.print("\\trowd\\trgaph");
            //maxPadding = 0;
            outputTwips(maxPadding);
            outstream.println();
            JoriaFrameBorder top = template.getFrame().getCascadedFrameStyle().getTopBorder();
            if (firstLine) {
                firstLine = false;
            }
            outputBorder(top, "t", true);
            outputFrameSpacing(top, "t");
            JoriaFrameBorder left = template.getFrame().getCascadedFrameStyle().getLeftBorder();
            outputBorder(left, "l", true);
            outputFrameSpacing(left, "l");
            JoriaFrameBorder bottom = template.getFrame().getCascadedFrameStyle().getBottomBorder();
            //if(lastLine)
            {
                outputBorder(bottom, "b", true);
                outputFrameSpacing(bottom, "b");
            }
            JoriaFrameBorder right = template.getFrame().getCascadedFrameStyle().getRightBorder();
            outputBorder(right, "r", true);
            outputFrameSpacing(right, "r");
            if (rowHeight != 0) {
                outstream.print("\\trrh-");
                outputTwips(rowHeight);
                outstream.println();
            }
            if (unbreakableFrame) {
                outstream.print("\\trkeep");
                if (!lastLine)
                    outstream.print("\\trkeepfollow");
            }
            outputFramePadding(left, "l");
            outputFramePadding(top, "t");
            outputFramePadding(bottom, "b");
            outputFramePadding(right, "r");
            outstream.print("\\trftsWidth3\\trwWidth");
            outputTwips(colPos[colPos.length - 1] - leftMargin);
            outstream.println();
            out.colInCurrRow = 0;
            while (out.colInCurrRow < dynaCols) {
                doOneCellPos(data[out.colInCurrRow]);
            }
            outstream.println();
            out.colInCurrRow = 0;
            while (out.colInCurrRow < dynaCols) {
                doOneCell(data[out.colInCurrRow]);
            }
            outstream.println("\\row");
        }
    }

    protected boolean includeAllHeaderAndFooterFrames() {
        return true;
    }

    protected void endSection() {
        if (multiSection) {
            outstream.println("\\sect}");
        }
    }

    protected void startSection(Template template) {
        hasFirstHeader = false;
        hasFirstFooter = false;
        if (multiSection) {
            outstream.println("{\\sectd\\sbkpage");
            PageMaster pageMaster = env.getTemplate().getPage();
            PageStyle ps = pageMaster.getPageStyle();
            outputPageSize(ps);
            //            if(template.isRestartNumbering()) // TODO funktioniert nicht
            //                outstream.println("\\pgnstarts1");
			/*  TODO das scheint nicht zu funktionieren          outstream.print("\\headery");
						float headerHeight = Math.max(pageMaster.getHeaderHeight(PageLevelBox.firstPage), pageMaster.getHeaderHeight(PageLevelBox.middlePage));
						outputTwips(ps.getTopMargin().getVal()-headerHeight);
						outstream.println();
						outstream.print("\\footery");
						float footerHeight = Math.max(pageMaster.getFooterHeight(PageLevelBox.firstPage), pageMaster.getFooterHeight(PageLevelBox.middlePage));
						outputTwips(ps.getTopMargin().getVal()-footerHeight);
						outstream.println();*/
            if (template.getPage().getFirstPageFooter() != template.getPage().getMiddlePagesFooter() || template.getPage().getFirstPageHeader() != template.getPage().getFurtherPagesHeader()) {// Other Header or Footers for the first page
                outstream.println("\\titlepg");
            }
        }
    }

    private void outputPageSize(PageStyle ps) {
        outstream.print("\\pghsxn");
        outputTwips(ps.getPageHeight());
        outstream.println();
        outstream.print("\\pgwsxn");
        outputTwips(ps.getPageWidth());
        outstream.println();
        if (ps.getPageWidth() > ps.getPageHeight())
            outstream.println("\\landscape");
        outstream.print("\\marglsxn");
        outputTwips(ps.getLeftMargin().getValInPoints());
        leftMargin = ps.getLeftMargin().getValInPoints();
        outstream.println();
        outstream.print("\\margrsxn");
        outputTwips(ps.getRightMargin().getValInPoints());
        outstream.println();
        outstream.print("\\margtsxn");
        outputTwips(ps.getTopMargin().getValInPoints());
        outstream.println();
        outstream.print("\\margbsxn");
        outputTwips(ps.getBotMargin().getValInPoints());
        outstream.println();
    }

    private void outputData(String data, boolean inPageCell) {
        if (inPageCell) {
            int pageNumberIndex = 0;
            int totalPagesIndex = 0;
            int lastStart = 0;
            while ((pageNumberIndex = data.indexOf(PAGENUMBER_PLACEHOLDER, pageNumberIndex)) != -1 | (totalPagesIndex = data.indexOf(TOTALPAGES_PLACEHOLDER, totalPagesIndex)) != -1) {
                if (totalPagesIndex == -1 || pageNumberIndex < totalPagesIndex && pageNumberIndex != -1) {
                    outputData(data.substring(lastStart, pageNumberIndex));
                    outstream.print(PAGENUMBER_STRING);
                    lastStart = pageNumberIndex + PAGENUMBER_PLACEHOLDER.length();
                    pageNumberIndex = lastStart;
                    totalPagesIndex = lastStart;
                } else {
                    outputData(data.substring(lastStart, totalPagesIndex));
                    outstream.print(TOTALPAGES_STRING);
                    lastStart = totalPagesIndex + TOTALPAGES_PLACEHOLDER.length();
                    pageNumberIndex = lastStart;
                    totalPagesIndex = lastStart;
                }
            }
            if (lastStart < data.length())
                outputData(data.substring(lastStart));
        } else
            outputData(data);
    }

    private void outputData(String data) {
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (ch == '\n') {
                outstream.println("\\line");
            } else if (ch < 0x20) {
                // Ignore control characters
            } else if (ch == 0x5c || ch == 0x7b || ch == 0x7d || ch >= 0x80 && ch < 0x100) {
                char[] hex = toHex((byte) (ch));
                outstream.print("\\'");
                outstream.print(hex[0]);
                outstream.print(hex[1]);
            } else if (ch > 0xff && ch < Short.MAX_VALUE) {
                outstream.print("\\u" + Integer.toString(ch) + " ");
            } else if (ch >= Short.MAX_VALUE) {
                outstream.print("\\u" + (ch - 0x10000) + " ");
            } else {
                outstream.print(ch);
            }
        }
    }

    private void outputTwips(float val) {
        float twips = val * POINT2TWIPS;
        int outval = Math.round(twips);
        outstream.print(outval);
    }

    public static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private char[] toHex(byte b) {
        char[] ret = new char[2];
        int b1 = b & 0xff;
        ret[0] = hexDigits[b1 / 16];
        ret[1] = hexDigits[b1 % 16];
        return ret;
    }

    private static class HtmlReplacement {
        String key;

        public HtmlReplacement(String key, char value) {
            this.key = key;
            this.value = value;
        }

        char value;
    }

    private static final HtmlReplacement[] replace = {new HtmlReplacement("amp", '&'), new HtmlReplacement("lt", '<'), new HtmlReplacement("gt", '>')};
}
