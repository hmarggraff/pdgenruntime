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
    Graphics2D graphics2D;
    Rectangle2D.Float contentClip = new Rectangle2D.Float();
    protected Line2D.Float tempLine = new Line2D.Float();

    public Graphics2DPrinter() {
    }

    public Graphics2DPrinter(Graphics2D gr) {
        graphics2D = gr;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void setGraphics2D(Graphics2D g) {
        this.graphics2D = g;
    }

    public Graphics2D getGraphics2D() {
        return g;
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
        graphics2D.setColor(textLines.style.getForeground());
        graphics2D.setFont(textLines.style.getStyledFont());
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
                    graphics2D.drawString(textLines.linesParts[i][j], textLines.getxContent() + textLines.partOffsets[i][j], yPos);
                }
                if (textLines.style.getUnderlined()) {
                    if (textLines.linesParts[i].length == 1) {
                        FontRenderContext frc = graphics2D.getFontRenderContext();
                        LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                        Rectangle2D.Float uline = new Rectangle2D.Float(
                                textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                                textLines.style.getWidth(s, graphics2D), lm.getUnderlineThickness());
                        graphics2D.fill(uline);
                    } else {
                        FontRenderContext frc = graphics2D.getFontRenderContext();
                        LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                        Rectangle2D.Float uline = new Rectangle2D.Float(
                                textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                                textLines.wContent, lm.getUnderlineThickness());
                        graphics2D.fill(uline);
                    }
                }

            } else {
                graphics2D.drawString(s, textLines.getxContent() + textLines.lineOffsets[i], yPos);
                if (textLines.style.getUnderlined()) {
                    FontRenderContext frc = graphics2D.getFontRenderContext();
                    LineMetrics lm = textLines.style.getStyledFont().getLineMetrics(s, frc);
                    Rectangle2D.Float uline = new Rectangle2D.Float(
                            textLines.getxContent() + textLines.lineOffsets[i], lm.getUnderlineOffset() + yPos,
                            textLines.style.getWidth(s, graphics2D), lm.getUnderlineThickness());
                    graphics2D.fill(uline);
                }
            }
        }
        graphics2D.setClip(oldClip);
    }

    public void printGEPicture(GraphElPicture picture) {
        Shape oldClipShape = doClip(picture);
        if (picture.img instanceof ImageIcon) {
            if (picture.spread) {
                ImageIcon imageIcon = ((ImageIcon) picture.img);
                Image image = imageIcon.getImage();
                Graphics2D g2 = (Graphics2D) graphics2D.create();
                g2.translate(picture.getxContent(), picture.yContent);
                double widthScale = picture.wContent / imageIcon.getIconWidth();
                double heightScale = picture.hContent / imageIcon.getIconHeight();
                g2.scale(widthScale, heightScale);
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            } else if (!Float.isNaN(picture.scale)) {
                ImageIcon imageIcon = ((ImageIcon) picture.img);
                Image image = imageIcon.getImage();
                Graphics2D g2 = (Graphics2D) graphics2D.create();
                g2.translate(picture.getxContent(), picture.yContent);
                g2.scale(picture.scale, picture.scale);
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
            } else {
                graphics2D.translate(picture.getxContent(), picture.yContent);
                graphics2D.drawImage(((ImageIcon) picture.img).getImage(), 0, 0, null);
                graphics2D.translate(-picture.getxContent(), -picture.yContent);
            }
        } else if (picture.img != null) {
            if (!Float.isNaN(picture.scale)) {
                Graphics2D g2 = (Graphics2D) graphics2D.create();
                g2.translate(picture.getxContent(), picture.yContent);
                g2.scale(picture.scale, picture.scale);
                picture.img.paintIcon(null, graphics2D, 0, 0);
                g2.dispose();
            } else {
                graphics2D.translate(picture.getxContent(), picture.yContent);
                picture.img.paintIcon(null, graphics2D, 0, 0);
                graphics2D.translate(-picture.getxContent(), -picture.yContent);
            }
        }
        graphics2D.setClip(oldClipShape);
    }

    public Shape doClip(GraphElContent content) {
        Shape oldClipShape = graphics2D.getClip();
        Rectangle2D oldClip = oldClipShape.getBounds2D();
        Rectangle2D cr = oldClip.createIntersection(content);
        graphics2D.setClip(cr);
        graphics2D.setColor(content.color);
        graphics2D.fill(content);
        if (content.src != null && content.getBackgroundImage() != null) {
            content.setBackgroudClip(contentClip);
            cr = oldClip.createIntersection(contentClip);
            graphics2D.setClip(cr);
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
            Graphics2D g2d = (Graphics2D) graphics2D.create();
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
        graphics2D.setClip(cr);
        return oldClipShape;
    }

    public void printGEText(GraphElText text) {
        Shape oldClipShape = doClip(text);
        if (StringUtils.isEmpty(text.txt)) {
            graphics2D.setClip(oldClipShape);
            return;
        }
        graphics2D.setColor(text.style.getForeground());
        graphics2D.setBackground(text.style.getBackground());
        graphics2D.setFont(text.style.getStyledFont());
        AffineTransform affineTransform = null;
        //g.drawLine((int)text.getxContent()-1, (int)text.yContent, (int)text.getxContent()+1,(int)text.yContent); // paint left top corner for debugging purposes
        //g.drawLine((int)text.getxContent(), (int)text.yContent-1, (int)text.getxContent(),(int)text.yContent+1);
        if (text.style.getTextType().equals(CellStyle.vertBottomUp)) {
            affineTransform = graphics2D.getTransform();
            graphics2D.rotate(-Math.PI / 2, text.getxContent() + text.hContent / 2, text.yContent + text.hContent / 2);

        } else if (text.style.getTextType().equals(CellStyle.vertTopDown)) {
            affineTransform = graphics2D.getTransform();
            graphics2D.rotate(Math.PI / 2, text.getxContent() + text.wContent / 2, text.yContent + text.wContent / 2);
        }
        graphics2D.drawString(text.txt, text.getxContent(), text.yContent + text.style.getBaseLine());
        if (text.style.getUnderlined()) {
            FontRenderContext frc = graphics2D.getFontRenderContext();
            LineMetrics lm = text.style.getStyledFont().getLineMetrics(text.txt, frc);
            Rectangle2D.Float uline = new Rectangle2D.Float(
                    text.getxContent(), lm.getUnderlineOffset() + text.yContent + text.style.getBaseLine(),
                    text.style.getWidth(text.txt, graphics2D), lm.getUnderlineThickness());
            graphics2D.fill(uline);
        }
        if (affineTransform != null)
            graphics2D.setTransform(affineTransform);
        graphics2D.setClip(oldClipShape);
    }

    public void printGERect(GraphicElementRect rect) {
        Shape oldClipShape = doClip(rect);
        graphics2D.setClip(oldClipShape);
    }

    public void printGELine(GraphElLine line) {
        if (line.lineStyle == JoriaBorder.NONE)
            return;
        graphics2D.setColor(line.color);
        BasicStroke s;
        Stroke oldStroke = graphics2D.getStroke();
        if (line.lineStyle == JoriaBorder.DOUBLE) {
            float t3 = line.thickness / 3;
            s = new BasicStroke(t3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            graphics2D.setStroke(s);
            final Line2D.Float tl = tempLine;
            if (line.x1 == line.x2)          // vertical
            {
                tl.y1 = line.y1;
                tl.y2 = line.y2;
                tl.x1 = line.x1 - t3;
                tl.x2 = line.x2 - t3;
                graphics2D.draw(tl);
                tl.x1 = line.x1 + t3;
                tl.x2 = line.x2 + t3;
                graphics2D.draw(tl);
            } else                   // horizontal
            {
                tl.x1 = line.x1;
                tl.x2 = line.x2;
                tl.y1 = line.y1 - t3;
                tl.y2 = line.y2 - t3;
                graphics2D.draw(tl);
                tl.y1 = line.y1 + t3;
                tl.y2 = line.y2 + t3;
                graphics2D.draw(tl);
            }
        } else {
            if (line.lineStyle == JoriaBorder.SOLID) {
                if (line.thickness < 2) {
                    s = new BasicStroke(line.thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                    graphics2D.setStroke(s);
                    graphics2D.draw(line);
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
                    graphics2D.fill(new Rectangle2D.Float(x, y, w, h));
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
                graphics2D.setStroke(s);
                graphics2D.draw(line);
            }
        }
        graphics2D.setStroke(oldStroke);
    }

    public void printMetaFile(GraphElMetaFile mf) {
        //Shape oldClip = doClip(mf);
        try {
            Graphics2D gc = (Graphics2D) graphics2D.create();
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
        final Graphics2D gc = (Graphics2D) graphics2D.create();
        gc.setColor(htmlText.myStyle.getForeground());
        gc.setFont(htmlText.myStyle.getStyledFont());
        gc.translate(htmlText.xContent - htmlText.x, htmlText.yContent - htmlText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(htmlText.myStyle.getBackground());
        tc.setFont(htmlText.myStyle.getStyledFont());
        View hv1 = htmlText.hv;
        hv1.paint(gc, htmlText.getBounds());
        gc.dispose();
        graphics2D.setClip(oldClipShape);
    }

    public void printGERtfText(GraphElRtfText graphElRtfText) {
        Shape oldClipShape = doClip(graphElRtfText);
        final Graphics2D gc = (Graphics2D) graphics2D.create();
        gc.setColor(graphElRtfText.myStyle.getForeground());
        gc.setFont(graphElRtfText.myStyle.getStyledFont());
        gc.translate(graphElRtfText.xContent - graphElRtfText.x, graphElRtfText.yContent - graphElRtfText.y);
        JComponent tc = GraphElHtmlText.tc();
        tc.setBackground(graphElRtfText.myStyle.getBackground());
        tc.setFont(graphElRtfText.myStyle.getStyledFont());
        graphElRtfText.myView.paint(gc, graphElRtfText.getBounds());
        gc.dispose();
        graphics2D.setClip(oldClipShape);
    }

    public void printGEStyledText(GraphElStyledText graphElStyledText) {
        Shape oldClip = doClip(graphElStyledText);
        graphics2D.setColor(graphElStyledText.style.getForeground());
        graphics2D.setFont(graphElStyledText.style.getStyledFont());
        for (QSELine line : graphElStyledText.lines) {
            if (line != null) {
                float yPos = graphElStyledText.yContent + line.yCell +
                        line.getAscent() - graphElStyledText.offset;
                float xPos = graphElStyledText.xContent + line.x;
                line.draw(graphics2D, xPos, yPos);
            }
        }
        graphics2D.setClip(oldClip);
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }
}
