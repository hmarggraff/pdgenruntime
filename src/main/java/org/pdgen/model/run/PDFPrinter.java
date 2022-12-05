// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.env.Env;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.Res;
import org.pdgen.metafilegraphics.MetaFileGraphics2DIn;
import org.pdgen.model.Template;
import org.pdgen.model.run.pdf.PDFGraphics2D;
import org.pdgen.model.run.pdf.PdfOutput;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.JoriaBorder;
import org.pdgen.styledtext.model.QSELine;
import org.pdgen.styledtext.model.StyleRunIterator;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.util.StringUtils;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Jan 14, 2003
 * Time: 2:21:59 PM
 */
public class PDFPrinter implements JoriaPrinter {
    PdfOutput myOutputter;
    private final Rectangle2D.Float clipRectangle = new Rectangle2D.Float();
    protected int[] pages = new int[1024];
    protected Color color;
    protected Color background;
    private static final FontRenderContext frc = new FontRenderContext(null, false, true);

    private Template lastTemplate;
    float ph;
    private final Graphics2D g2d;

    PDFPrinter(OutputStream file, PageFormat pap, String title, Template template, Graphics2D g2d, final String userName) throws IOException {
        this.g2d = g2d;
        ph = (float) pap.getPaper().getHeight();
        if (template.getNextSection() != null)
            lastTemplate = template;
        myOutputter = new PdfOutput(file, pap.getPaper(), title, g2d);
    }

    public void end(boolean doEndpage) throws IOException {
        if (doEndpage) {
            endPage();
        }
        myOutputter.end();
    }

    public void startPage() {
    }

    public void endPage() throws IOException {
        myOutputter.endPage();
    }

    public void printDecoration(GrelViewer gv) throws IOException {
        if (lastTemplate != null && gv.pageContents.currentTemplate != lastTemplate) {
            lastTemplate = gv.pageContents.currentTemplate;
            myOutputter.startSection(lastTemplate.getPage().getCascadedPageStyle().getPageFormat().getPaper());
            ph = (float) lastTemplate.getPage().getCascadedPageStyle().getPageFormat().getPaper().getHeight();
        }
        myOutputter.startPage();
    }

    public void printGETextLines(GraphElTextLines textLines) {
        startContentElement(textLines);
        if (textLines.lines.length == 0) {
            endContentElement();
            return;
        }
        PdfOutput.RegFont rf = myOutputter.setFont(textLines.style.getStyledFont());
        myOutputter.setNonStrokeColor(textLines.style.getForeground());
        myOutputter.writeBeginTextToPage();
        float lastX = 0;
        float lastY = 0;
        float lineHeight = textLines.style.getLineSpacing();
        if (textLines.style.getLineSpacingNumber() != null)
            lineHeight *= textLines.style.getLineSpacingNumber();
        boolean isBlockAlignment = textLines.style.getAlignmentHorizontal().isBlock();
        for (int i = 0; i < textLines.lines.length; i++) {
            if (isBlockAlignment && textLines.linesParts[i] != null) {
                for (int j = 0; j < textLines.linesParts[i].length; j++) {
                    float thisX = textLines.xContent + textLines.partOffsets[i][j];
                    float thisY = ph - (textLines.yContent + i * lineHeight + textLines.style.getBaseLine());
                    myOutputter.writeTextWithPositionToPage(thisX - lastX, thisY - lastY, textLines.linesParts[i][j], rf);
                    lastX = thisX;
                    lastY = thisY;
                }
            } else {
                float thisX = textLines.xContent + textLines.lineOffsets[i];
                float thisY = ph - (textLines.yContent + i * lineHeight + textLines.style.getBaseLine());
                myOutputter.writeTextWithPositionToPage(thisX - lastX, thisY - lastY, textLines.lines[i], rf);
                lastX = thisX;
                lastY = thisY;
            }
        }
        myOutputter.writeEndTextToPage();
        if (textLines.style.getUnderlined()) {
            for (int i = 0; i < textLines.lines.length; i++) {
                LineMetrics lm = myOutputter.getFont().getLineMetrics(textLines.lines[i], frc);
                Rectangle2D.Float uline = new Rectangle2D.Float(textLines.xContent + textLines.lineOffsets[i], ph - (textLines.yContent + i * lineHeight + textLines.style.getBaseLine() + lm.getUnderlineOffset() + lm.getUnderlineThickness()), textLines.style.getWidth(textLines.lines[i], g2d), lm.getUnderlineThickness());
                myOutputter.fillRectangle(uline, false);
            }
        }
        endContentElement();
    }

    public void printGEPicture(GraphElPicture picture) {
        startContentElement(picture);
        if (picture.img != null) {
            float scaleWidth = Float.NaN;
            float scaleHeight = Float.NaN;
            float iconHeight = picture.img.getIconHeight();
            if (picture.spread) {
                scaleWidth = picture.wContent / picture.img.getIconWidth();
                scaleHeight = picture.hContent / picture.img.getIconHeight();
                iconHeight *= scaleHeight;
            } else if (!Float.isNaN(picture.scale)) {
                iconHeight *= picture.scale;
                scaleWidth = picture.scale;
                scaleHeight = picture.scale;
            }
            myOutputter.paintImage(picture.img, picture.storedData, picture.getxContent(), ph - picture.yContent - iconHeight, scaleWidth, scaleHeight);
        }
        endContentElement();
    }

    public void printGEText(GraphElText text) {
        startContentElement(text);
        if (StringUtils.isEmpty(text.txt)) {
            endContentElement();
            return;
        }
        PdfOutput.RegFont rf = myOutputter.setFont(text.style.getStyledFont());
        myOutputter.setNonStrokeColor(text.style.getForeground());
        float x = text.xContent;
        float y = ph - text.yContent - text.style.getBaseLine();
        if (text.style.getTextType().equals(CellStyle.vertBottomUp)) {
            x += text.style.getBaseLine();
            y -= text.hContent - text.style.getBaseLine();
            myOutputter.writeRotate(false, x, y);
            x = 0;
            y = 0;
        } else if (text.style.getTextType().equals(CellStyle.vertTopDown)) {
            x += text.style.getLineSpacing() - text.style.getBaseLine();
            y += text.style.getBaseLine();
            myOutputter.writeRotate(true, x, y);
            x = 0;
            y = 0;
        }
        myOutputter.writeBeginTextToPage();
        myOutputter.writeTextWithPositionToPage(x, y, text.txt, rf);
        myOutputter.writeEndTextToPage();
        if (text.style.getUnderlined()) {
            LineMetrics lm = myOutputter.getFont().getLineMetrics(text.txt, frc);
            Rectangle2D.Float uline = new Rectangle2D.Float(x, y - lm.getUnderlineOffset() - lm.getUnderlineThickness(), text.style.getWidth(text.txt, g2d), lm.getUnderlineThickness());
            myOutputter.fillRectangle(uline, false);
        }
        endContentElement();
    }

    public void printGERect(GraphicElementRect rect) {
        startContentElement(rect);
        endContentElement();
    }

    public void printGELine(GraphElLine line) {
        if (line.lineStyle == JoriaBorder.NONE)
            return;
        myOutputter.setStrokeColor(line.color);
        if (line.lineStyle == JoriaBorder.DOUBLE) {
            float width = line.thickness / 3;
            myOutputter.writeLineDashPatternToPage(0, 0);
            myOutputter.writeLineWidthToPage(width);
            if (line.isHorizantal()) {
                myOutputter.writeMoveToToPage(line.x1, ph - line.y1 - width);
                myOutputter.writeLineToToPage(line.x2, ph - line.y1 - width);
                myOutputter.writeStrokeToPage();
                myOutputter.writeMoveToToPage(line.x1, ph - line.y1 + width);
                myOutputter.writeLineToToPage(line.x2, ph - line.y1 + width);
                myOutputter.writeStrokeToPage();
            } else {
                myOutputter.writeMoveToToPage(line.x1 - width, ph - line.y1);
                myOutputter.writeLineToToPage(line.x1 - width, ph - line.y2);
                myOutputter.writeStrokeToPage();
                myOutputter.writeMoveToToPage(line.x1 - width, ph - line.y1);
                myOutputter.writeLineToToPage(line.x1 - width, ph - line.y2);
                myOutputter.writeStrokeToPage();
            }
        } else {
            if (line.lineStyle == JoriaBorder.SOLID) {
                myOutputter.writeLineDashPatternToPage(0, 0);
            } else if (line.lineStyle == JoriaBorder.DOT) {
                myOutputter.writeLineDashPatternToPage(line.thickness, line.thickness);
            } else if (line.lineStyle == JoriaBorder.DASH) {
                myOutputter.writeLineDashPatternToPage(2 * line.thickness, line.thickness);
            } else {
                throw new JoriaInternalError("unknown line style " + line.lineStyle);
            }
            myOutputter.writeLineWidthToPage(line.thickness);
            if (line.isHorizantal()) {
                myOutputter.writeMoveToToPage(line.x1, ph - line.y1);
                myOutputter.writeLineToToPage(line.x2, ph - line.y1);
                myOutputter.writeStrokeToPage();
            } else {
                myOutputter.writeMoveToToPage(line.x1, ph - line.y1);
                myOutputter.writeLineToToPage(line.x1, ph - line.y2);
                myOutputter.writeStrokeToPage();
            }
        }
    }

    public void printMetaFile(GraphElMetaFile mf) {
        startContentElement(mf);
        try {
            PDFGraphics2D g = new PDFGraphics2D(myOutputter, ph, mf.getBounds());
            g.translate(mf.xContent - mf.x + mf.translateX, mf.yContent - mf.y + mf.translateY);
            MetaFileGraphics2DIn mfg = new MetaFileGraphics2DIn(mf.data);
            mfg.loop(g);
            g.dispose();
        } catch (IOException e) {
            Env.instance().handle(e, Res.str("Internal_error_in_paint"));
        } finally {
            endContentElement();
        }
    }

    public void printGEHtmlText(GraphElHtmlText htmlText) {
        startContentElement(htmlText);
        PDFGraphics2D gc = new PDFGraphics2D(myOutputter, ph, htmlText.getBounds());
        gc.setColor(htmlText.myStyle.getForeground());
        gc.setFont(htmlText.myStyle.getStyledFont());
        gc.translate(htmlText.xContent - htmlText.x, htmlText.yContent - htmlText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(htmlText.myStyle.getBackground());
        tc.setFont(htmlText.myStyle.getStyledFont());
        View hv1;
        hv1 = htmlText.hv;
        hv1.paint(gc, htmlText.getBounds());
        gc.dispose();
        endContentElement();
    }

    public void printGERtfText(GraphElRtfText graphElRtfText) {
        startContentElement(graphElRtfText);
        PDFGraphics2D gc = new PDFGraphics2D(myOutputter, ph, graphElRtfText.getBounds());
        gc.setColor(graphElRtfText.myStyle.getForeground());
        gc.setFont(graphElRtfText.myStyle.getStyledFont());
        gc.translate(graphElRtfText.xContent - graphElRtfText.x, graphElRtfText.yContent - graphElRtfText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(graphElRtfText.myStyle.getBackground());
        tc.setFont(graphElRtfText.myStyle.getStyledFont());
        View hv1;
        hv1 = graphElRtfText.myView;
        hv1.paint(gc, graphElRtfText.getBounds());
        gc.dispose();
        endContentElement();
    }

    public void printGEStyledText(GraphElStyledText graphElStyledText) {
        startContentElement(graphElStyledText);
        if (graphElStyledText.lines.length == 0) {
            endContentElement();
            return;
        }
        myOutputter.setFont(graphElStyledText.style.getStyledFont());
        myOutputter.setNonStrokeColor(graphElStyledText.style.getForeground());
        myOutputter.writeBeginTextToPage();
        float lastX = 0;
        float lastY = 0;
        boolean inText = true;
        ArrayList<Rectangle2D.Float> underLines = new ArrayList<>();
        ArrayList<StyledTextComponent> components = new ArrayList<>();
        for (int i = 0; i < graphElStyledText.lines.length; i++)// Über alle Zeilen
        {
            QSELine line = graphElStyledText.lines[i];
            boolean isLastLine = line.isLastLineOfParagraph();
            boolean justify = (line.text.getAlignment() == StyledParagraph.alignJustified && !isLastLine);
            float thisX = graphElStyledText.xContent + line.x;
            float thisY = ph - (graphElStyledText.yContent + line.yCell + line.getAscent() - graphElStyledText.offset);
            if (!justify) {
                myOutputter.writeTextPositionToPage(thisX - lastX, thisY - lastY);
                lastY = thisY;
                lastX = thisX;
            }
            float startX = thisX;
            StyleRunIterator iter = graphElStyledText.iterators[i];
            float fullWidth = 0;
            int countWhiteSpaceComponents = 0;
            while (iter.nextRun())// Über alle Runs der Zeile.
            {
                Hashtable<AttributedCharacterIterator.Attribute, Object> fa = new Hashtable<>();
                float fontSize = iter.getSize();
                if (iter.isNewSuperscript()) {
                    if (iter.isSuperscript()) {
                        fontSize *= 2. / 3.;
                    }
                }
                if (iter.isBold())
                    fa.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                if (iter.isItalic())
                    fa.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                fa.put(TextAttribute.SIZE, fontSize);
                fa.put(TextAttribute.FAMILY, iter.getFontFamily());
                Font font = new Font(fa);
                String text = iter.getText();
                if (justify) {
                    int lastStart = 0;
                    boolean nonWhitespace = false;
                    for (int j = 0; j < text.length(); j++) {
                        char c = text.charAt(j);
                        if (Character.isWhitespace(c)) {
                            if (!nonWhitespace) {
                                if (lastStart != 0) {
                                    lastStart = j + 1;
                                }
                            } else {
                                String t = text.substring(lastStart, j);
                                Rectangle2D r = font.getStringBounds(t, g2d.getFontRenderContext());
                                fullWidth += (float) r.getWidth();
                                components.add(new StyledTextComponent(font, iter.isNewSuperscript() && lastStart == 0, iter.isSuperscript(), t, iter.isUnderlined(), r, iter.getSize(), iter.getForegroundColor(), iter.getBackgroundColor(), iter.getWidth(), iter.isNewForegroundColor() && lastStart == 0, iter.isNewBackgroundColor() && lastStart == 0, true));
                                lastStart = j + 1;
                                nonWhitespace = false;
                                countWhiteSpaceComponents++;
                            }
                        } else
                            nonWhitespace = true;
                    }
                    if (nonWhitespace) {
                        String t = text.substring(lastStart);
                        Rectangle2D r = font.getStringBounds(t, g2d.getFontRenderContext());
                        fullWidth += (float) r.getWidth();
                        components.add(new StyledTextComponent(font, iter.isNewSuperscript() && lastStart == 0, iter.isSuperscript(), t, iter.isUnderlined(), r, iter.getSize(), iter.getForegroundColor(), iter.getBackgroundColor(), iter.getWidth(), iter.isNewForegroundColor() && lastStart == 0, iter.isNewBackgroundColor() && lastStart == 0, false));
                    }
                } else {
                    Rectangle2D r = font.getStringBounds(text, g2d.getFontRenderContext());
                    components.add(new StyledTextComponent(font, iter.isNewSuperscript(), iter.isSuperscript(), text, iter.isUnderlined(), r, iter.getSize(), iter.getForegroundColor(), iter.getBackgroundColor(), iter.getWidth(), iter.isNewForegroundColor(), iter.isNewBackgroundColor(), false));
                }
                if (iter.isEndOfLine()) {
                    break;
                }
            }
            float extra = 0;
            if (justify && countWhiteSpaceComponents > 1) {
                extra = graphElStyledText.wContent - fullWidth;
                extra /= countWhiteSpaceComponents;
            }
            double runningX = graphElStyledText.xContent + line.x;
            for (StyledTextComponent textComponent : components) {
                Rectangle2D r = textComponent.rect;
                if (textComponent.endsInWhiteSpace)
                    r.setRect(r.getX(), r.getY(), r.getWidth() + extra, r.getHeight());
                Color color = textComponent.backgroundColor;
                Color testColor = graphElStyledText.color;
                if (isValidColor(color, testColor)) {
                    if (inText) {
                        myOutputter.writeEndTextToPage();
                        inText = false;
                    }
                    myOutputter.setNonStrokeColor(color);
                    double superScriptOffset = textComponent.superScript ? (textComponent.fontSize / 2) : 0;
                    clipRectangle.setRect(runningX + r.getX(), ph - (graphElStyledText.yContent + line.getAscent() + line.yCell - graphElStyledText.offset) - r.getY() - r.getHeight() + superScriptOffset, r.getWidth(), r.getHeight());
                    myOutputter.fillRectangle(clipRectangle, false);
                    myOutputter.setNonStrokeColor(testColor);
                }
                runningX += r.getWidth();
            }
            myOutputter.setNonStrokeColor(graphElStyledText.style.getForeground());
            Color lastForegound = graphElStyledText.style.getForeground();
            for (StyledTextComponent textComponent : components) {
                Font font = textComponent.font;
                String text = textComponent.text;
                Rectangle2D r = textComponent.rect;
                if (!lastForegound.equals(textComponent.foregroundColor) && !(lastForegound == graphElStyledText.style.getForeground() && textComponent.foregroundColor == null)) {
                    if (inText) {
                        myOutputter.writeEndTextToPage();
                        inText = false;
                    }
                    lastForegound = isValidColor(textComponent.foregroundColor, graphElStyledText.style.getForeground()) ? textComponent.foregroundColor : graphElStyledText.style.getForeground();
                    myOutputter.setNonStrokeColor(lastForegound);
                }
                if (!inText) {
                    lastY = 0;
                    lastX = 0;
                    myOutputter.writeBeginTextToPage();
                    myOutputter.writeTextPositionToPage(thisX - lastX, thisY - lastY);
                    lastY = thisY;
                    lastX = thisX;
                    inText = true;
                } else if (justify) {
                    myOutputter.writeTextPositionToPage(thisX - lastX, thisY - lastY);
                    lastY = thisY;
                    lastX = thisX;
                }
                if (textComponent.newSuperScript) {
                    if (textComponent.superScript) {
                        myOutputter.writeTextRise(textComponent.fontSize / 2);
                    } else {
                        myOutputter.writeTextRise(0);
                    }
                }
                PdfOutput.RegFont rf = myOutputter.setFont(font);
                myOutputter.writeFontToPage();
                myOutputter.writeTextToPage(text, rf);
                if (textComponent.underlined) {
                    LineMetrics lm = font.getLineMetrics(text, g2d.getFontRenderContext());
                    Rectangle2D.Float uline = new Rectangle2D.Float(startX, thisY - lm.getUnderlineOffset() - lm.getUnderlineThickness(), (float) r.getWidth(), lm.getUnderlineThickness());
                    underLines.add(uline);
                }
                startX += (float) r.getWidth();
                thisX += (float) r.getWidth();
            }
            components.clear();
            myOutputter.writeEOLToPage();
        }
        if (inText)
            myOutputter.writeEndTextToPage();
        for (Rectangle2D.Float aFloat : underLines) {
            myOutputter.fillRectangle(aFloat, false);
        }
        endContentElement();
    }

    private boolean isValidColor(Color color, Color testColor) {
        return color != null && !color.equals(testColor) && color.getAlpha() != 0;
    }

    private class StyledTextComponent {
        boolean newSuperScript;
        boolean superScript;
        String text;
        boolean underlined;
        Rectangle2D rect;
        float fontSize;
        Font font;
        Color foregroundColor;
        Color backgroundColor;
        float characterSpacing;
        boolean newForegroundColor;
        boolean newBackgroundColor;
        boolean endsInWhiteSpace;

        StyledTextComponent(Font font, boolean newSuperScript, boolean superScript, String text, boolean underlined, Rectangle2D rect, float fontSize, Color foregroundColor, Color backgroundColor, float characterSpacing, boolean newForegroundColor, boolean newBackgroundColor, boolean endsInWhiteSpace) {
            this.endsInWhiteSpace = endsInWhiteSpace;
            this.newBackgroundColor = newBackgroundColor;
            this.newForegroundColor = newForegroundColor;
            this.characterSpacing = characterSpacing;
            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
            this.font = font;
            this.newSuperScript = newSuperScript;
            this.superScript = superScript;
            this.text = text;
            this.underlined = underlined;
            this.rect = rect;
            this.fontSize = fontSize;
        }
    }

    protected void startContentElement(GraphElContent elem) {
        myOutputter.writePushContext();
        if (elem.color.getAlpha() != 0) {
            myOutputter.setNonStrokeColor(elem.color);
            clipRectangle.setRect(elem.x, ph - elem.y - elem.height, elem.width, elem.height);
            myOutputter.fillRectangle(clipRectangle, true);
        }
        myOutputter.writeRectangleToPage(elem.x, ph - elem.y - elem.height, elem.width, elem.height);
        myOutputter.writeClipToPage();
        myOutputter.writeNewPathToPage();
        if (elem.background != null) {
            myOutputter.writeRectangleToPage(elem.xEnvelope, ph - elem.yEnvelope - elem.hEnvelope, elem.wEnvelope, elem.hEnvelope);
            myOutputter.writeClipToPage();
            myOutputter.writeNewPathToPage();
            float scaleWidth = Float.NaN;
            float scaleHeight = Float.NaN;
            float iconHeight = elem.background.getIconHeight();
            float iconWidth = elem.background.getIconWidth();
            CellStyle cellStyle = elem.src.getCascadedStyle();
            if (cellStyle.getBackgroundImageTargetWidth() != null && !cellStyle.getBackgroundImageTargetWidth().isExpandable()) {
                float scale = cellStyle.getBackgroundImageTargetWidth().getVal() / iconWidth;
                iconHeight *= scale;
                iconWidth = cellStyle.getBackgroundImageTargetWidth().getVal();
                scaleWidth = scale;
                scaleHeight = scale;
            }
            float x = elem.xEnvelope + (elem.wEnvelope - iconWidth) * cellStyle.getAlignmentHorizontal().getAlign();
            float y = elem.yEnvelope + (elem.hEnvelope - iconHeight) * cellStyle.getAlignmentVertical().getAlign();
            myOutputter.paintImage(elem.background, cellStyle.getBackgroundImageName(), x, ph - y - elem.hEnvelope, scaleWidth, scaleHeight);
        }
        myOutputter.writeRectangleToPage(elem.xEnvelope, ph - elem.yEnvelope - elem.hEnvelope, elem.wEnvelope, elem.hEnvelope);
        //        myOutputter.writeRectangleToPage(elem.xContent, ph - elem.yContent - elem.hContent, elem.wContent, elem.hContent);
        myOutputter.writeClipToPage();
        myOutputter.writeNewPathToPage();
    }

    protected void endContentElement() {
        myOutputter.writePopContext();
    }
}
