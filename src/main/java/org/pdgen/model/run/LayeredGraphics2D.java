// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.util.Map;
import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: May 2, 2007
 * Time: 3:45:25 PM
 */
public class LayeredGraphics2D extends Graphics2D
{
    private final Graphics2D base;
    private FontRenderContext frc;

    public LayeredGraphics2D(Graphics2D base)
    {
        this.base = base;
    }

    public LayeredGraphics2D(Graphics2D base, FontRenderContext frc)
    {
        this.base = base;
        this.frc = frc;
    }

    public void draw(Shape s)
    {
        base.draw(s);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs)
    {
        return base.drawImage(img, xform, obs);
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y)
    {
        base.drawImage(img, op, x, y);
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform)
    {
        base.drawRenderedImage(img, xform);
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform)
    {
        base.drawRenderableImage(img, xform);
    }

    public void drawString(String str, int x, int y)
    {
        base.drawString(str, x, y);
    }

    public void drawString(String s, float x, float y)
    {
        base.drawString(s, x, y);
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y)
    {
        base.drawString(iterator, x, y);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer)
    {
        return base.drawImage(img, x, y, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
    {
        return base.drawImage(img, x, y, width, height, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer)
    {
        return base.drawImage(img, x, y, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer)
    {
        return base.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
    {
        return base.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer)
    {
        return base.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    public void dispose()
    {
        base.dispose();
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y)
    {
        base.drawString(iterator, x, y);
    }

    public void drawGlyphVector(GlyphVector g, float x, float y)
    {
        base.drawGlyphVector(g, x, y);
    }

    public void fill(Shape s)
    {
        base.fill(s);
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke)
    {
        return base.hit(rect, s, onStroke);
    }

    public GraphicsConfiguration getDeviceConfiguration()
    {
        return base.getDeviceConfiguration();
    }

    public void setComposite(Composite comp)
    {
        base.setComposite(comp);
    }

    public void setPaint(Paint paint)
    {
        base.setPaint(paint);
    }

    public void setStroke(Stroke s)
    {
        base.setStroke(s);
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue)
    {
        base.setRenderingHint(hintKey, hintValue);
    }

    public Object getRenderingHint(RenderingHints.Key hintKey)
    {
        return base.getRenderingHint(hintKey);
    }

    public void setRenderingHints(Map<?, ?> hints)
    {
        base.setRenderingHints(hints);
    }

    public void addRenderingHints(Map<?, ?> hints)
    {
        base.addRenderingHints(hints);
    }

    public RenderingHints getRenderingHints()
    {
        return base.getRenderingHints();
    }

    public Graphics create()
    {
        return new LayeredGraphics2D((Graphics2D) base.create(), frc);
    }

    public void translate(int x, int y)
    {
        base.translate(x, y);
    }

    public Color getColor()
    {
        return base.getColor();
    }

    public void setColor(Color c)
    {
        base.setColor(c);
    }

    public void setPaintMode()
    {
        base.setPaintMode();
    }

    public void setXORMode(Color c1)
    {
        base.setXORMode(c1);
    }

    public Font getFont()
    {
        return base.getFont();
    }

    public void setFont(Font font)
    {
        base.setFont(font);
    }

    public FontMetrics getFontMetrics(Font f)
    {
        return base.getFontMetrics(f);
    }

    public Rectangle getClipBounds()
    {
        return base.getClipBounds();
    }

    public void clipRect(int x, int y, int width, int height)
    {
        base.clipRect(x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height)
    {
        base.setClip(x, y, width, height);
    }

    public Shape getClip()
    {
        return base.getClip();
    }

    public void setClip(Shape clip)
    {
        base.setClip(clip);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy)
    {
        base.copyArea(x, y, width, height, dx, dy);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
        base.drawLine(x1, y1, x2, y2);
    }

    public void fillRect(int x, int y, int width, int height)
    {
        base.fillRect(x, y, width, height);
    }

    public void clearRect(int x, int y, int width, int height)
    {
        base.clearRect(x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        base.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {
        base.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawOval(int x, int y, int width, int height)
    {
        base.drawOval(x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height)
    {
        base.fillOval(x, y, width, height);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        base.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
    {
        base.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints)
    {
        base.drawPolyline(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints)
    {
        base.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints)
    {
        base.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void translate(double tx, double ty)
    {
        base.translate(tx, ty);
    }

    public void rotate(double theta)
    {
        base.rotate(theta);
    }

    public void rotate(double theta, double x, double y)
    {
        base.rotate(theta, x, y);
    }

    public void scale(double sx, double sy)
    {
        base.scale(sx, sy);
    }

    public void shear(double shx, double shy)
    {
        base.shear(shx, shy);
    }

    public void transform(AffineTransform Tx)
    {
        base.transform(Tx);
    }

    public void setTransform(AffineTransform Tx)
    {
        base.setTransform(Tx);
    }

    public AffineTransform getTransform()
    {
        return base.getTransform();
    }

    public Paint getPaint()
    {
        return base.getPaint();
    }

    public Composite getComposite()
    {
        return base.getComposite();
    }

    public void setBackground(Color color)
    {
        base.setBackground(color);
    }

    public Color getBackground()
    {
        return base.getBackground();
    }

    public Stroke getStroke()
    {
        return base.getStroke();
    }

    public void clip(Shape s)
    {
        base.clip(s);
    }

    public FontRenderContext getFontRenderContext()
    {
        if(frc != null)
            return frc;
        else
            return base.getFontRenderContext();
    }
}
