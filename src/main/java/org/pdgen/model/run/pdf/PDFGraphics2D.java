// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaAssertionError;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * User: patrick
 * Date: Nov 15, 2004
 * Time: 2:58:02 PM
 */
public class PDFGraphics2D extends Graphics2D {
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;
    private static final AffineTransform IDENTITY = new AffineTransform();

    private AffineTransform transform;
    private Stroke stroke;
    private Stroke oldStroke;
    private Stroke originalStroke;
    private BasicStroke strokeOne = new BasicStroke(1);
    private boolean disposeCalled;
    private final PdfOutput myOutputter;
    private Paint paint;
    private Color background;
    private Font font;
    private Area clip;
    protected int currentFillGState = 255;
    protected int currentStrokeGState = 255;

    private final float pageHeight;
    private Paint paintFill;
    private Paint paintStroke;
    private final RenderingHints rhints = new RenderingHints(null);
    private final Rectangle bounds;
    private boolean ignoreFill;
    private boolean ignoreStroke;

    public PDFGraphics2D(PdfOutput output, float ph, Rectangle b) {
        myOutputter = output;
        pageHeight = ph;
        paint = Color.black;
        transform = new AffineTransform();
        background = Color.white;
        setFont(new Font("default", Font.BOLD | Font.ITALIC, 8));
        myOutputter.writePushContext();
        bounds = b;
        clip = new Area(bounds);
        clip(clip);
        originalStroke = stroke = oldStroke = strokeOne;
        setStrokeDiff(stroke, null);
        myOutputter.writePushContext();
    }

    private PDFGraphics2D(PDFGraphics2D master) {
        pageHeight = master.pageHeight;
        myOutputter = master.myOutputter;
        transform = new AffineTransform(master.transform);
        font = master.font;
        paint = master.paint;
        background = master.background;
        myOutputter.writePushContext();
        bounds = master.bounds;
        followPath(new Area(bounds), CLIP);
        if (master.clip != null)
            clip = new Area(master.clip);
        stroke = master.stroke;
        strokeOne = (BasicStroke) transformStroke(strokeOne);
        oldStroke = strokeOne;
        setStrokeDiff(oldStroke, null);
        myOutputter.writePushContext();
        if (clip != null)
            followPath(clip, CLIP);
    }

    public void rotate(double theta) {
        transform.rotate(theta);
    }

    public void scale(double sx, double sy) {
        transform.scale(sx, sy);
        stroke = transformStroke(originalStroke);
    }

    private Stroke transformStroke(Stroke originalStroke) {
        if (!(originalStroke instanceof BasicStroke))
            return originalStroke;
        BasicStroke st = (BasicStroke) originalStroke;
        float scale = (float) Math.sqrt(Math.abs(transform.getDeterminant()));
        float[] dash = st.getDashArray();
        if (dash != null) {
            for (int k = 0; k < dash.length; ++k)
                dash[k] *= scale;
        }
        return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash, st.getDashPhase() * scale);
    }

    public void shear(double shx, double shy) {
        transform.shear(shx, shy);
    }

    public void translate(double tx, double ty) {
        transform.translate(tx, ty);
    }

    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
    }

    public void dispose() {
        if (!disposeCalled) {
            disposeCalled = true;
            myOutputter.writePopContext();
            myOutputter.writePopContext();
        }
    }

    public void setPaintMode() {
        throw new JoriaAssertionError("not implemented");
    }

    public void translate(int x, int y) {
        translate(x, (double) y);
    }

    public void clearRect(int x, int y, int width, int height) {
        Paint temp = paint;
        setPaint(background);
        fillRect(x, y, width, height);
        setPaint(temp);
    }

    public void clipRect(int x, int y, int width, int height) {
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        clip(rect);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        draw(line);
    }

    public void drawOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float((float) x, (float) y, (float) width, (float) height);
        draw(oval);
    }

    public void fillOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float((float) x, (float) y, (float) width, (float) height);
        fill(oval);
    }

    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle(x, y, width, height));
    }

    public void setClip(int x, int y, int width, int height) {
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        setClip(rect);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        throw new JoriaAssertionError("not implemented");
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(arc);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        draw(rect);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
        fill(arc);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        fill(rect);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Polygon poly = new Polygon();
        for (int i = 0; i < nPoints; i++) {
            poly.addPoint(xPoints[i], yPoints[i]);
        }
        draw(poly);
    }

    public void drawPolyline(int[] x, int[] y, int nPoints) {
        Line2D line = new Line2D.Double(x[0], y[0], x[0], y[0]);
        for (int i = 1; i < nPoints; i++) {
            line.setLine(line.getX2(), line.getY2(), x[i], y[i]);
            draw(line);
        }
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Polygon poly = new Polygon();
        for (int i = 0; i < nPoints; i++) {
            poly.addPoint(xPoints[i], yPoints[i]);
        }
        fill(poly);
    }

    public Color getColor() {
        if (paint instanceof Color) {
            return (Color) paint;
        } else {
            return Color.black;
        }
    }

    public void setColor(Color c) {
        setPaint(c);
    }

    public void setXORMode(Color c1) {
        //throw new JoriaAssertionError("not implemented");
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font f) {
        if (f == null)
            return;
        font = f;
        myOutputter.setFont(font);
    }

    public Graphics create() {
        return new PDFGraphics2D(this);
    }

    public Rectangle getClipBounds() {
        if (clip == null)
            return null;
        return getClip().getBounds();
    }

    public Shape getClip() {
        try {
            return transform.createInverse().createTransformedShape(clip);
        } catch (NoninvertibleTransformException e) {
            return null;
        }
    }

    public void setClip(Shape s) {
        myOutputter.writePopContext();
        myOutputter.writePushContext();
        if (s != null)
            s = transform.createTransformedShape(s);
        if (s == null) {
            clip = null;
        } else {
            clip = new Area(s);
            followPath(s, CLIP);
        }
        currentFillGState = currentStrokeGState = 255;
        oldStroke = strokeOne;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color color) {
        background = color;
    }

    public Composite getComposite() {
        return null;
    }

    public void setComposite(Composite comp) {
        if (comp instanceof AlphaComposite) {
            int alpha = (int) (((AlphaComposite) comp).getAlpha() * 255);
            myOutputter.setStrokeAlpha(alpha);
            myOutputter.setFillAlpha(alpha);
        } else
            throw new JoriaAssertionError("not implemented");
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return myOutputter.g2d.getDeviceConfiguration();
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        if (paint == null)
            return;
        this.paint = paint;
    }

    public RenderingHints getRenderingHints() {
        return rhints;
    }

    public void clip(Shape s) {
        if (s != null)
            s = transform.createTransformedShape(s);
        if (clip == null)
            clip = new Area(s);
        else
            clip.intersect(new Area(s));
        followPath(s, CLIP);
    }

    public void draw(Shape s) {
        followPath(s, STROKE);
    }

    public void fill(Shape s) {
        followPath(s, FILL);
    }

    public Stroke getStroke() {
        return originalStroke;
    }

    public void setStroke(Stroke s) {
        originalStroke = s;
        stroke = transformStroke(s);
    }

    public FontRenderContext getFontRenderContext() {
        return new FontRenderContext(null, true, true);
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        Shape s = g.getOutline(x, y);
        fill(s);
    }

    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    public void setTransform(AffineTransform t) {
        transform = new AffineTransform(t);
        stroke = transformStroke(originalStroke);
    }

    public void transform(AffineTransform tx) {
        transform.concatenate(tx);
        stroke = transformStroke(originalStroke);
    }

    private AffineTransform normalizeMatrix() {
        double[] mx = new double[6];
        AffineTransform result = AffineTransform.getTranslateInstance(0, 0);
        result.getMatrix(mx);
        mx[3] = -1;
        mx[5] = pageHeight;
        result = new AffineTransform(mx);
        result.concatenate(transform);
        return result;
    }

    public void drawString(String s, float x, float y) {
        if (setFillPaint())
            return;
        AffineTransform at = getTransform();
        AffineTransform at2 = getTransform();
        at2.translate(x, y);
        at2.concatenate(font.getTransform());
        setTransform(at2);
        AffineTransform inverse = normalizeMatrix();
        AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        inverse.concatenate(flipper);
        double[] mx = new double[6];
        inverse.getMatrix(mx);
        myOutputter.writeBeginTextToPage();
        PdfOutput.RegFont rf = myOutputter.setFont(font);
        myOutputter.writeTextMatrixToPage((float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
        myOutputter.writeTextToPage(s, rf);
        myOutputter.writeEndTextToPage();
        setTransform(at);
    }

    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        throw new JoriaAssertionError("not implemented");
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        throw new JoriaAssertionError("not implemented");
    }

    public FontMetrics getFontMetrics(Font f) {
        return myOutputter.g2d.getFontMetrics(f);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        throw new JoriaAssertionError("not implemented");
    }

    public void addRenderingHints(Map<?, ?> hints) {
        rhints.putAll(hints);
    }

    public void setRenderingHints(Map<?, ?> hints) {
        rhints.clear();
        rhints.putAll(hints);
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            s = stroke.createStrokedShape(s);
        }
        s = transform.createTransformedShape(s);
        Area area = new Area(s);
        if (clip != null)
            area.intersect(clip);
        return area.intersects(rect.x, rect.y, rect.width, rect.height);
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        throw new JoriaAssertionError("not implemented");
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        throw new JoriaAssertionError("not implemented");
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        throw new JoriaAssertionError("not implemented");
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return rhints.get(hintKey);
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        rhints.put(hintKey, hintValue);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        throw new JoriaAssertionError("not implemented");
    }

    private void followPath(Shape s, int drawType) {
        if (s == null) return;
        if (drawType == STROKE) {
            if (!(stroke instanceof BasicStroke)) {
                s = stroke.createStrokedShape(s);
                followPath(s, FILL);
                return;
            }
        }
        if (drawType == STROKE) {
            setStrokeDiff(stroke, oldStroke);
            oldStroke = stroke;
            if (setStrokePaint())
                return;
        } else if (drawType == FILL) {
            if (setFillPaint())
                return;
        }
        PathIterator points;
        if (drawType == CLIP)
            points = s.getPathIterator(IDENTITY);
        else
            points = s.getPathIterator(transform);
        float[] coords = new float[6];
        int traces = 0;
        while (!points.isDone()) {
            ++traces;
            int segtype = points.currentSegment(coords);
            normalizeY(coords);
            switch (segtype) {
                case PathIterator.SEG_CLOSE:
                    myOutputter.writeCloseToPage();
                    break;

                case PathIterator.SEG_CUBICTO:
                    myOutputter.writeCubeToToPage(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;

                case PathIterator.SEG_LINETO:
                    myOutputter.writeLineToToPage(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_MOVETO:
                    myOutputter.writeMoveToToPage(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_QUADTO:
                    myOutputter.writeQuadToToPage(coords[0], coords[1], coords[2], coords[3]);
                    break;
            }
            points.next();
        }

        switch (drawType) {
            case FILL:
                if (traces > 0) {
                    if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                        myOutputter.writeEoFillToPage();
                    else
                        myOutputter.writeFillToPage();
                }
                break;
            case STROKE:
                if (traces > 0)
                    myOutputter.writeStrokeToPage();
                break;
            default: //drawType==CLIP
                if (traces == 0)
                    myOutputter.writeRectangleToPage(0, 0, 0, 0);
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                    myOutputter.writeEoClipToPage();
                else
                    myOutputter.writeClipToPage();
                myOutputter.writeNewPathToPage();
        }
    }

    private void setStrokeDiff(Stroke newStroke, Stroke oldStroke) {
        if (newStroke == oldStroke)
            return;
        if (!(newStroke instanceof BasicStroke))
            return;
        BasicStroke nStroke = (BasicStroke) newStroke;
        boolean oldOk = (oldStroke instanceof BasicStroke);
        BasicStroke oStroke = null;
        if (oldOk)
            oStroke = (BasicStroke) oldStroke;
        if (!oldOk || nStroke.getLineWidth() != oStroke.getLineWidth())
            myOutputter.writeLineWidthToPage(nStroke.getLineWidth());
        if (!oldOk || nStroke.getEndCap() != oStroke.getEndCap()) {
            switch (nStroke.getEndCap()) {
                case BasicStroke.CAP_BUTT:
                    myOutputter.writeLineCapToPage(0);
                    break;
                case BasicStroke.CAP_SQUARE:
                    myOutputter.writeLineCapToPage(2);
                    break;
                default:
                    myOutputter.writeLineCapToPage(1);
            }
        }
        if (!oldOk || nStroke.getLineJoin() != oStroke.getLineJoin()) {
            switch (nStroke.getLineJoin()) {
                case BasicStroke.JOIN_MITER:
                    myOutputter.writeLineJoinToPage(0);
                    break;
                case BasicStroke.JOIN_BEVEL:
                    myOutputter.writeLineJoinToPage(2);
                    break;
                default:
                    myOutputter.writeLineJoinToPage(1);
            }
        }
        if (!oldOk || nStroke.getMiterLimit() != oStroke.getMiterLimit())
            myOutputter.writeMilterLimitToPage(nStroke.getMiterLimit());
        boolean makeDash;
        if (oldOk) {
            if (nStroke.getDashArray() != null) {
                makeDash = nStroke.getDashPhase() != oStroke.getDashPhase()
                        || !java.util.Arrays.equals(nStroke.getDashArray(), oStroke.getDashArray());
            } else makeDash = oStroke.getDashArray() != null;
        } else {
            makeDash = true;
        }
        if (makeDash) {
            float[] dash = nStroke.getDashArray();
            if (dash == null || dash.length == 0)
                myOutputter.writeLineDashPatternToPage(0, 0);
            else {
                myOutputter.writeLineDashPatternToPage(dash, nStroke.getDashPhase());
            }
        }
    }

    private boolean checkNewPaint(Paint oldPaint) {
        return paint != oldPaint && !((paint instanceof Color) && paint.equals(oldPaint));
    }

    private boolean setFillPaint() {
        if (checkNewPaint(paintFill)) {
            paintFill = paint;
            ignoreFill = setPaint(true);
        }
        return ignoreFill;
    }

    private boolean setStrokePaint() {
        if (checkNewPaint(paintStroke)) {
            paintStroke = paint;
            ignoreStroke = setPaint(false);
        }
        return ignoreStroke;
    }

    private boolean setPaint(boolean fill) {
        boolean retVal = false;
        if (paint instanceof Color) {
            Color color = (Color) paint;
            int alpha = color.getAlpha();
            if (alpha == 0)
                retVal = true;
            if (fill) {
                if (alpha != currentFillGState) {
                    currentFillGState = alpha;
                    myOutputter.setFillAlpha(alpha);
                }
                myOutputter.setNonStrokeColor(color);
            } else {
                if (alpha != currentStrokeGState) {
                    currentStrokeGState = alpha;
                    myOutputter.setStrokeAlpha(alpha);
                }
                myOutputter.setStrokeColor(color);
            }
        } else if (paint instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint) paint;
            Point2D p1 = gp.getPoint1();
            transform.transform(p1, p1);
            Point2D p2 = gp.getPoint2();
            transform.transform(p2, p2);
            Color c1 = gp.getColor1();
            Color c2 = gp.getColor2();
            ((Point2D.Float) p1).y = normalizeY(((Point2D.Float) p1).y);
            ((Point2D.Float) p2).y = normalizeY(((Point2D.Float) p2).y);
            if (fill)
                myOutputter.setFillShade(c1, c2, p1, p2);
            else
                myOutputter.setStrokeShade(c1, c2, p1, p2);
        } else
            throw new JoriaAssertionError("unsupported paint " + paint.getClass());
        return retVal;
    }

    private float normalizeY(float y) {
        return pageHeight - y;
    }

    private void normalizeY(float[] coords) {
        coords[1] = normalizeY(coords[1]);
        coords[3] = normalizeY(coords[3]);
        coords[5] = normalizeY(coords[5]);
    }
}
