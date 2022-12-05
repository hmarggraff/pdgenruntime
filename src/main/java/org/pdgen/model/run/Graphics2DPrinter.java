// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.metafilegraphics.MetaFileGraphics2DIn;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.JoriaBorder;
import org.pdgen.styledtext.model.QSELine;
import org.pdgen.util.StringUtils;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

/**
 * User: patrick
 * Date: Jan 15, 2003
 * Time: 7:18:19 AM
 */

public class Graphics2DPrinter implements JoriaPrinter {
    Graphics2D g;
    Rectangle2D.Float contentClip = new Rectangle2D.Float();
    protected Line2D.Float tempLine = new Line2D.Float();

    public Graphics2DPrinter() {
    }

    public Graphics2DPrinter(Graphics2D gr) {
        g = gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void setGraphics2D(Graphics2D g) {
        this.g = g;
    }

    public void startPage() {
        // nothing here to be done
    }

    public void endPage() {
        // nothing here to be done
    }

    public void printDecoration(GrelViewer gv) {
        // nothing here to be done
    }

    public void printGETextLines(GraphElTextLines textLines) {
        Shape oldClip = doClip(textLines);
        g.setColor(textLines.style.getForeground());
        g.setFont(textLines.style.getStyledFont());
        float lineHeight = textLines.style.getLineSpacing();
        if (textLines.style.getLineSpacingNumber() != null)
            lineHeight *= textLines.style.getLineSpacingNumber();
        float yPos;
        boolean isBlockAlignment = textLines.style.getAlignmentHorizontal().isBlock();
        for (int i = 0; i < textLines.lines.length; i++) {
            yPos = textLines.yContent + textLines.style.getBaseLine() + i * lineHeight;
            String s = textLines.lines[i];
            if (isBlockAlignment && textLines.linesParts[i] != null) {
                for (int j = 0; j < textLines.linesParts[i].length; j++) {
                    g.drawString(textLines.linesParts[i][j], textLines.getxContent() + textLines.partOffsets[i][j], yPos);
                }
                if (textLines.style.getUnderlined()) {
                    if (textLines.linesParts[i].length == 1) {
                        FontRenderContext frc = g.getFontRenderContext();
                        LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                        Rectangle2D.Float uline = new Rectangle2D.Float(
                                textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                                textLines.style.getWidth(s, g), lm.getUnderlineThickness());
                        g.fill(uline);
                    } else {
                        FontRenderContext frc = g.getFontRenderContext();
                        LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                        Rectangle2D.Float uline = new Rectangle2D.Float(
                                textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                                textLines.wContent, lm.getUnderlineThickness());
                        g.fill(uline);
                    }
                }

            } else {
                g.drawString(s, textLines.getxContent() + textLines.lineOffsets[i], yPos);
                if (textLines.style.getUnderlined()) {
                    FontRenderContext frc = g.getFontRenderContext();
                    LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                    Rectangle2D.Float uline = new Rectangle2D.Float(
                            textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                            textLines.style.getWidth(s, g), lm.getUnderlineThickness());
                    g.fill(uline);
                }
            }
        }
        g.setClip(oldClip);
    }

    public void printGEPicture(GraphElPicture picture) {
        Shape oldClipShape = doClip(picture);
        if (picture.img instanceof ImageIcon) {
            if (picture.spread) {
                ImageIcon imageIcon = ((ImageIcon) picture.img);
                Image image = imageIcon.getImage();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(picture.getxContent(), picture.yContent);
                double widthScale = picture.wContent / imageIcon.getIconWidth();
                double heightScale = picture.hContent / imageIcon.getIconHeight();
                g2.scale(widthScale, heightScale);
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            } else if (!Float.isNaN(picture.scale)) {
                ImageIcon imageIcon = ((ImageIcon) picture.img);
                Image image = imageIcon.getImage();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(picture.getxContent(), picture.yContent);
                g2.scale(picture.scale, picture.scale);
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            } else {
                g.translate(picture.getxContent(), picture.yContent);
                g.drawImage(((ImageIcon) picture.img).getImage(), 0, 0, null);
                g.translate(-picture.getxContent(), -picture.yContent);
            }
        } else if (picture.img != null) {
            if (!Float.isNaN(picture.scale)) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(picture.getxContent(), picture.yContent);
                g2.scale(picture.scale, picture.scale);
                picture.img.paintIcon(null, g, 0, 0);
                g2.dispose();
            } else {
                g.translate(picture.getxContent(), picture.yContent);
                picture.img.paintIcon(null, g, 0, 0);
                g.translate(-picture.getxContent(), -picture.yContent);
            }
        }
        g.setClip(oldClipShape);
    }

    public Shape doClip(GraphElContent content) {
        Shape oldClipShape = g.getClip();
        Rectangle2D oldClip = oldClipShape.getBounds2D();
        Rectangle2D cr = oldClip.createIntersection(content);
        g.setClip(cr);
        g.setColor(content.color);
        g.fill(content);
        if (content.src != null && content.getBackgroundImage() != null) {
            content.setBackgroudClip(contentClip);
            cr = oldClip.createIntersection(contentClip);
            g.setClip(cr);
            CellStyle cs = content.src.getCascadedStyle();
            FlexSize imageScale = cs.getBackgroundImageTargetWidth();
            ImageIcon image = content.getBackgroundImage();
            float height = image.getIconHeight();
            float width = image.getIconWidth();
            float scale = 0;
            if (imageScale != null && !imageScale.isExpandable()) {
                scale = imageScale.getVal() / width;
                width = imageScale.getVal();
                height = height * scale;
            }
            double movex = contentClip.x;
            double movey = contentClip.y;
            if (contentClip.width > width) {
                movex = contentClip.x + (contentClip.width - width) * cs.getAlignmentHorizontal().getAlign();
            }
            if (contentClip.height > height) {
                movey = contentClip.y + (contentClip.height - height) * cs.getAlignmentVertical().getAlign();
            }
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(movex, movey);
            if (scale != 0)
                g2d.scale(scale, scale);
            g2d.drawImage(image.getImage(), 0, 0, null);
            g2d.dispose();
        }
        content.setBackgroudClip(contentClip);
//        content.setContentClipRectangle(contentClip);
        cr = oldClip.createIntersection(contentClip);
        cr = cr.createIntersection(content);
        g.setClip(cr);
        return oldClipShape;
    }

    public void printGEText(GraphElText text) {
        Shape oldClipShape = doClip(text);
        if (StringUtils.isEmpty(text.txt)) {
            g.setClip(oldClipShape);
            return;
        }
        g.setColor(text.style.getForeground());
        g.setBackground(text.style.getBackground());
        g.setFont(text.style.getStyledFont());
        AffineTransform affineTransform = null;
        //g.drawLine((int)text.getxContent()-1, (int)text.yContent, (int)text.getxContent()+1,(int)text.yContent); // paint left top corner for debugging purposes
        //g.drawLine((int)text.getxContent(), (int)text.yContent-1, (int)text.getxContent(),(int)text.yContent+1);
        if (text.style.getTextType().equals(CellStyle.vertBottomUp)) {
            affineTransform = g.getTransform();
            g.rotate(-Math.PI / 2, text.getxContent() + text.hContent / 2, text.yContent + text.hContent / 2);

        } else if (text.style.getTextType().equals(CellStyle.vertTopDown)) {
            affineTransform = g.getTransform();
            g.rotate(Math.PI / 2, text.getxContent() + text.wContent / 2, text.yContent + text.wContent / 2);
        }
        g.drawString(text.txt, text.getxContent(), text.yContent + text.style.getBaseLine());
        if (text.style.getUnderlined()) {
            FontRenderContext frc = g.getFontRenderContext();
            LineMetrics lm = text.style.getStyledFont().getLineMetrics(text.txt, frc);
            Rectangle2D.Float uline = new Rectangle2D.Float(
                    text.getxContent(), lm.getUnderlineOffset() + text.yContent + text.style.getBaseLine(),
                    text.style.getWidth(text.txt, g), lm.getUnderlineThickness());
            g.fill(uline);
        }
        if (affineTransform != null)
            g.setTransform(affineTransform);
        g.setClip(oldClipShape);
    }

    public void printGERect(GraphicElementRect rect) {
        Shape oldClipShape = doClip(rect);
        g.setClip(oldClipShape);
    }

    public void printGELine(GraphElLine line) {
        if (line.lineStyle == JoriaBorder.NONE)
            return;
        g.setColor(line.color);
        BasicStroke s;
        Stroke oldStroke = g.getStroke();
        if (line.lineStyle == JoriaBorder.DOUBLE) {
            float t3 = line.thickness / 3;
            s = new BasicStroke(t3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(s);
            final Line2D.Float tl = tempLine;
            if (line.x1 == line.x2)          // vertical
            {
                tl.y1 = line.y1;
                tl.y2 = line.y2;
                tl.x1 = line.x1 - t3;
                tl.x2 = line.x2 - t3;
                g.draw(tl);
                tl.x1 = line.x1 + t3;
                tl.x2 = line.x2 + t3;
                g.draw(tl);
            } else                   // horizontal
            {
                tl.x1 = line.x1;
                tl.x2 = line.x2;
                tl.y1 = line.y1 - t3;
                tl.y2 = line.y2 - t3;
                g.draw(tl);
                tl.y1 = line.y1 + t3;
                tl.y2 = line.y2 + t3;
                g.draw(tl);
            }
        } else {
            if (line.lineStyle == JoriaBorder.SOLID) {
                if (line.thickness < 2) {
                    s = new BasicStroke(line.thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                    g.setStroke(s);
                    g.draw(line);
                } else
                //if(line.thickness > 2)
                {
                    float x, y, w, h;
                    if (line.x1 == line.x2) {
                        x = (line.x1 - line.thickness / 2);
                        w = line.thickness;
                        h = (line.y2 - line.y1);
                        y = line.y1;
                    } else {
                        y = (line.y1 - line.thickness / 2);
                        h = line.thickness;
                        w = (line.x2 - line.x1);
                        x = line.x1;
                    }
                    g.fill(new Rectangle2D.Float(x, y, w, h));
                }
/*
                else
                {
                }
*/
            } else {
                if (line.lineStyle == JoriaBorder.DOT) {
                    float[] dashArray = {line.thickness, line.thickness};
                    s = new BasicStroke(line.thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
                } else if (line.lineStyle == JoriaBorder.DASH) {
                    float[] dashArray = {2 * line.thickness, line.thickness};
                    s = new BasicStroke(line.thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
                } else //if (line.lineStyle == JoriaBorder.DOUBLE)
                {
                    float[] dashArray = {line.thickness, line.thickness};
                    s = new BasicStroke(line.thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0);
                }
                g.setStroke(s);
                g.draw(line);
            }
        }
        g.setStroke(oldStroke);
    }

    public void printMetaFile(GraphElMetaFile mf) {
        //Shape oldClip = doClip(mf);
        try {
            Graphics2D gc = (Graphics2D) g.create();
            final float tx = mf.xContent - mf.x + mf.translateX;
            final float ty = mf.yContent - mf.y + mf.translateY;
            gc.translate(tx, ty);
            //final AffineTransform transform = gc.getTransform();
            //final double translateY = transform.getTranslateY();
            MetaFileGraphics2DIn mfg = new MetaFileGraphics2DIn(mf.data);
            mfg.loop(gc);
            gc.dispose();
        } catch (IOException e) {
            Env.instance().handle(e, Res.str("Internal_error_in_paint"));
        }
        //g.setClip(oldClip);
    }

    public void printGEHtmlText(GraphElHtmlText htmlText) {
        Shape oldClipShape = doClip(htmlText);
        final Graphics2D gc = (Graphics2D) g.create();
        gc.setColor(htmlText.myStyle.getForeground());
        gc.setFont(htmlText.myStyle.getStyledFont());
        gc.translate(htmlText.xContent - htmlText.x, htmlText.yContent - htmlText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(htmlText.myStyle.getBackground());
        tc.setFont(htmlText.myStyle.getStyledFont());
        View hv1 = htmlText.hv;
        hv1.paint(gc, htmlText.getBounds());
        gc.dispose();
        g.setClip(oldClipShape);
    }

    public void printGERtfText(GraphElRtfText graphElRtfText) {
        Shape oldClipShape = doClip(graphElRtfText);
        final Graphics2D gc = (Graphics2D) g.create();
        gc.setColor(graphElRtfText.myStyle.getForeground());
        gc.setFont(graphElRtfText.myStyle.getStyledFont());
        gc.translate(graphElRtfText.xContent - graphElRtfText.x, graphElRtfText.yContent - graphElRtfText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(graphElRtfText.myStyle.getBackground());
        tc.setFont(graphElRtfText.myStyle.getStyledFont());
        graphElRtfText.myView.paint(gc, graphElRtfText.getBounds());
        gc.dispose();
        g.setClip(oldClipShape);
    }

    public void printGEStyledText(GraphElStyledText graphElStyledText) {
        Shape oldClip = doClip(graphElStyledText);
        g.setColor(graphElStyledText.style.getForeground());
        g.setFont(graphElStyledText.style.getStyledFont());
        for (QSELine line : graphElStyledText.lines) {
            if (line != null) {
                float yPos = graphElStyledText.yContent + line.yCell +
                        line.getAscent() - graphElStyledText.offset;
                float xPos = graphElStyledText.xContent + line.x;
                line.draw(g, xPos, yPos);
            }
        }
        g.setClip(oldClip);
    }
}
