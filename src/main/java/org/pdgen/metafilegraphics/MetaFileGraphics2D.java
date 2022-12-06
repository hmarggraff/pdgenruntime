// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;
//MARKER The strings in this file shall not be translated

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.AttributedCharacterIterator;
import java.util.*;

public class MetaFileGraphics2D extends Graphics2D implements DrawCommands, AutoCloseable {

    protected PrintStream mWriter;
    protected MetaFileWriter out;
    ArrayList<Object> refObjects = new ArrayList<Object>(); // images are not serialized, but kept in this list and then referenced. They are only serialized when transmitted
    Stack<Graphics2D> contexts = new Stack<Graphics2D>();
    static HashMap<RenderingHints.Key, RenderingHintMapper> renderingHintKeyMap = RenderingHintMapper.createKeyMapper();
    static HashMap<Object, Byte> renderingHintValuesMap = RenderingHintMapper.createValueMapper();
    Graphics2D state;
    static final boolean debug = false;

    public MetaFileGraphics2D(Graphics2D initial) {
        state = initial;
        //state.setRenderingHints(null);
        state.setFont(new Font("default", Font.BOLD | Font.ITALIC, 12));
        try {
            mWriter = System.out;
            if (debug)
                out = new DebugMetaFileWriter();
            else
                out = new BinMetaFileWriter();
            setRenderingHints(initial.getRenderingHints());
        } catch (Error e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public byte[] getBytes() {
        if (out instanceof BinMetaFileWriter)
            return ((BinMetaFileWriter) out).getBytes();
        return null;
    }

    private void wInt(int i1, int i2, int i3, int i4) {
        out.writeInt(i1);
        out.writeInt(i2);
        out.writeInt(i3);
        out.writeInt(i4);
    }

    protected void wStr(String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(b.length);
        for (byte aB : b) {
            out.writeByte(aB);
        }
    }

    protected void wPath(PathIterator pi) {
        out.writeInt(pi.getWindingRule());
        float[] coords = new float[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    out.writeCmd(SEG_MOVETO);
                    out.writeFloat(coords[0]);
                    out.writeFloat(coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    out.writeCmd(SEG_LINETO);
                    out.writeFloat(coords[0]);
                    out.writeFloat(coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    out.writeCmd(SEG_QUADTO);
                    out.writeFloat(coords[0]);
                    out.writeFloat(coords[1]);
                    out.writeFloat(coords[2]);
                    out.writeFloat(coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    out.writeCmd(SEG_CUBICTO);
                    out.writeFloat(coords[0]);
                    out.writeFloat(coords[1]);
                    out.writeFloat(coords[2]);
                    out.writeFloat(coords[3]);
                    out.writeFloat(coords[4]);
                    out.writeFloat(coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    out.writeCmd(SEG_CLOSE);
                    break;
            }
            pi.next();
        }
        out.writeCmd(PATHEND);
    }

    protected void wObject(Object img) {
        int ix = refObjects.indexOf(img);
        if (ix < 0) {
            ix = refObjects.size();
            refObjects.add(img);
        }
        out.writeInt(ix);

    }

    protected void wTransform(AffineTransform t) {
        out.writeDouble(t.getScaleX());
        out.writeDouble(t.getShearY());
        out.writeDouble(t.getShearX());

        out.writeDouble(t.getScaleY());
        out.writeDouble(t.getTranslateX());
        out.writeDouble(t.getTranslateY());
    }

    protected void i4(String cmd, int p1, int p2, int p3, int p4) {
        //mWriter.println(cmd + "(" + p1 + "," + p2 + "," + p3 + "," + p4 + ")");
    }

    protected void debugPrint(String s) {
        //mWriter.print(s);

    }

    protected void debugPrintln(String s) {
        //mWriter.print(s);

    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        i4("drawLine", x1, y1, x2, y2);
        out.writeCmd(DRAWLINE);
        wInt(x1, y1, x2, y2);
        //mWriter.draw(new Line2D.Float(x1, y1, x2, y2));
    }

    public void drawRect(int x, int y, int width, int height) {
        i4("drawRect", x, y, width, height);
        out.writeCmd(DRAWRECT);
        wInt(x, y, width, height);
        //draw(new Rectangle2D.Float(x, y, width, height));
    }

    public void fillRect(int x, int y, int width, int height) {
        i4("fillRect", x, y, width, height);
        out.writeCmd(FILLRECT);
        wInt(x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        debugPrintln("drawRoundRect(" + x + "," + y + "," + width + "," + height + "," + arcWidth + "," + arcHeight + ")");
        //draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
        out.writeCmd(DRAWROUNDRECT);
        wInt(x, y, width, height);
        wInt(arcWidth, arcHeight);
    }

    private void wInt(int a, int b) {
        out.writeInt(a);
        out.writeInt(b);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        debugPrintln("fillRoundRect(" + x + "," + y + "," + width + "," + height + "," + arcWidth + "," + arcHeight + ")");
        //fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
        out.writeCmd(FILLROUNDRECT);
        wInt(x, y, width, height);
        wInt(arcWidth, arcHeight);
    }

    public void drawOval(int x, int y, int width, int height) {
        debugPrintln("drawOval(" + x + "," + y + "," + width + "," + height + ")");
        //draw(new Ellipse2D.Float(x, y, width, height));
        out.writeCmd(DRAWOVAL);
        wInt(x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height) {
        debugPrintln("fillOval(" + x + "," + y + "," + width + "," + height + ")");
        //fill(new Ellipse2D.Float(x, y, width, height));
        out.writeCmd(FILLOVAL);
        wInt(x, y, width, height);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        debugPrintln("drawArc(" + x + "," + y + "," + width + "," + height + "," + startAngle + "," + arcAngle + ")");
        //draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
        out.writeCmd(DRAWARC);
        wInt(x, y, width, height);
        wInt(startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        debugPrintln("fillArc(" + x + "," + y + "," + width + "," + height + "," + startAngle + "," + arcAngle + ")");
        //fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE));
        out.writeCmd(FILLARC);
        wInt(x, y, width, height);
        wInt(startAngle, arcAngle);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        if (nPoints > 0) {
            out.writeCmd(DRAWPOLYLINE);
            out.writeInt(nPoints);
            for (int i = 0; i < nPoints; i++) {
                wInt(xPoints[i], yPoints[i]);
            }
            float fromX = xPoints[0];
            float fromY = yPoints[0];
            for (int i = 1; i < nPoints; i++) {
                float toX = xPoints[i];
                float toY = yPoints[i];
                draw(new Line2D.Float(fromX, fromY, toX, toY));
                fromX = toX;
                fromY = toY;
            }
        }
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        debugPrintln("drawPolygon(" + nPoints + ")");
        out.writeCmd(DRAWPOLYGON);
        out.writeInt(nPoints);
        for (int i = 0; i < nPoints; i++) {
            wInt(xPoints[i], yPoints[i]);
        }
        //draw(new Polygon(xPoints, yPoints, nPoints));
    }

    public void drawPolygon(Polygon p) {
        draw(p);
        out.writeCmd(DRAWPOLYGON);
        out.writeInt(p.npoints);
        for (int i = 0; i < p.npoints; i++) {
            wInt(p.xpoints[i], p.ypoints[i]);
        }
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        debugPrintln("fillPolygon(" + nPoints + ")");
        out.writeCmd(FILLPOLYGON);
        out.writeInt(nPoints);
        for (int i = 0; i < nPoints; i++) {
            wInt(xPoints[i], yPoints[i]);
        }
    }

    public void fillPolygon(Polygon p) {
        fill(p);
        out.writeCmd(FILLPOLYGON);
        out.writeInt(p.npoints);
        for (int i = 0; i < p.npoints; i++) {
            wInt(p.xpoints[i], p.ypoints[i]);
        }
    }

    public void drawString(String str, int x, int y) {
        if (str == null || str.length() == 0)
            return;
        debugPrintln("drawString " + x + "," + y + " \"" + str + "\"");
        out.writeCmd(DRAWSTRING);
        wInt(x, y);
        wStr(str);
    }

    public void drawString(String str, float x, float y) {
        if (str == null || str.length() == 0)
            return;
        debugPrintln("drawString(" + x + "," + y + ",\"" + str + "\")");
        out.writeCmd(DRAWSTRINGF);
        out.writeFloat(x);
        out.writeFloat(y);
        wStr(str);
        //mWriter.drawString(str, x, y);
    }

    public void draw(Shape s) {
        debugPrintln("draw(" + s.getClass().toString() + ")");
        out.writeCmd(DRAWSHAPE);
        wPath(s.getPathIterator(null));
        //mWriter.draw(s);
    }

    public void fill(Shape s) {
        debugPrintln("fill(" + s.getClass().toString() + ")");
        out.writeCmd(FILLSHAPE);
        wPath(s.getPathIterator(null));
        //mWriter.fill(s);
    }

    public void close() {
        out.writeCmd(CLOSE);
        debugPrintln("close");
        //mWriter.close();
        out.close();
        //mWriter = null; // get rid of mwriter as soon as possible, because its a memory hog
    }

    public void drawImage(String fileName, float x, float y, float w, float h) throws IOException {
        debugPrintln("drawImage(\"" + fileName + "\"" + x + "," + y + "," + w + "," + h + ")");
        //mWriter.drawImage(fileName, x, y, w, h);
    }

    /**
     * Creates a new Instance, which writes to the same output stream
     */
    public Graphics create() {
        out.writeCmd(CREATE);
        debugPrintln("MetaFileGraphics2D.create{ " + contexts.size() + " " + getTransform().getTranslateX());
        contexts.push(state);
        state = (Graphics2D) state.create();
        return this;
    }

    public void dispose() {
        debugPrintln("dispose}" + contexts.size() + " " + getTransform().getTranslateX());
        out.writeCmd(DISPOSE);
        state.dispose();
        if (!contexts.isEmpty())   // sometimes when we are rendering empty strings, there is nothing left on the stack.
            state = contexts.pop();

        // nothing else to be done output has to be closed using the end function
        // we cannot close on dispose, because the mWriter object is shared
        // by all instances of MetafileGraphics2D that are closed using the create method.
    }

    public Color getBackground() {
        return state.getBackground();
    }

    public Shape getClip() {
        return state.getClip();
    }

    public Rectangle getClipBounds() {
        return state.getClipBounds();
    }

    public Color getColor() {
        return state.getColor();
    }

    public Font getFont() {
        return state.getFont();
    }

    public FontRenderContext getFontRenderContext() {
        return state.getFontRenderContext();
    }

    public Stroke getStroke() {
        return state.getStroke();
    }

    public FontMetrics getFontMetrics(Font f) {
        debugPrintln("getFontMetrics(" + f.getFontName() + ")");
        return state.getFontMetrics(f);
    }

    public Paint getPaint() {
        return state.getPaint();
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    public AffineTransform getTransform() {
        debugPrintln("getTransform");
        final AffineTransform transform = state.getTransform();
        return transform;
    }

    public Composite getComposite() {
        debugPrintln("getComposite");
        return state.getComposite();
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        // rendering hints are currently ignored. Tough luck.
        debugPrintln("getRenderingHint");
        return state.getRenderingHint(hintKey);
    }

    public RenderingHints getRenderingHints() {
        // rendering hints are currently ignored. Tough luck.
        debugPrintln("RenderingHints");
        return state.getRenderingHints();
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        // no hit testing. Tough luck.
        debugPrintln("hit");
        return false;
    }


    public void setBackground(Color color) {
        state.setBackground(color);
        out.writeCmd(SETBACKGROUND);
        wInt(color.getRed(), color.getGreen());
        wInt(color.getBlue(), color.getAlpha());
    }

    public void setColor(Color c) {
        state.setColor(c);
        out.writeCmd(SETCOLOR);
        wInt(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public void setFont(Font font) {
        //Font f = state.getFont();
        //if (f != null && font.getName().equalsIgnoreCase(f.getName()) && font.getStyle() == f.getStyle() && font.getSize() == f.getSize())
        //	return;
        state.setFont(font);
        debugPrintln("setFont " + font.getFontName());
        out.writeCmd(SETFONT);
        wFont(font);
    }

    private void wFont(Font font) {
        wStr(font.getName());
        out.writeInt(font.getStyle());
        out.writeFloat(font.getSize2D());
    }

    public void setStroke(Stroke s) {
        state.setStroke(s);
        if (!(s instanceof BasicStroke))
            throw new RuntimeException("Only supporting BasicStoke not: " + s.getClass().getName());
        out.writeCmd(SETSTROKE);
        BasicStroke bs = (BasicStroke) s;
        out.writeFloat(bs.getLineWidth());
        wInt(bs.getEndCap(), bs.getLineJoin());
        out.writeFloat(bs.getMiterLimit());
        final float[] dashes = bs.getDashArray();
        if (dashes != null) {
            out.writeInt(dashes.length);
            for (float dash : dashes) {
                out.writeFloat(dash);
            }
        } else
            out.writeInt(0);
        out.writeFloat(bs.getDashPhase());

    }

    public void clip(Shape s) {
        intersectClip(s);
        if (s == null)
            out.writeCmd(CLEARCLIP);
        else {
            out.writeCmd(CLIPSHAPE);
            wPath(s.getPathIterator(null));
        }
    }

    public void clipRect(int x, int y, int width, int height) {
        intersectClip(new Rectangle(x, y, width, height));
        i4("clipRect", x, y, width, height);
        out.writeCmd(CLIPRECT);
        wInt(x, y, width, height);
    }

    public void setClip(Shape clip) {
        state.setClip(clip);
        debugPrintln("setClip " + clip);
        if (clip == null)
            out.writeCmd(CLEARSETCLIP);
        else {
            out.writeCmd(SETCLIPSHAPE);
            wPath(clip.getPathIterator(null));
        }
    }

    public void setClip(int x, int y, int width, int height) {
        state.setClip(new Rectangle(x, y, width, height));
        i4("setClip", x, y, width, height);
        out.writeCmd(SETCLIPRECT);
        wInt(x, y, width, height);

    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        debugPrintln("drawGlyphVector{");
        fill(g.getOutline(x, y));
        debugPrintln("}");
        out.writeCmd(GLYPHVECTOR);
        out.writeFloat(x);
        out.writeFloat(y);
        wFont(g.getFont());
        final int[] codes = g.getGlyphCodes(0, g.getNumGlyphs(), null);
        out.writeInt(codes.length);
        for (int code : codes) {
            out.writeInt(code);
        }
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }

    public void clearRect(int x, int y, int width, int height) {
        out.writeCmd(CLEARRECT);
        wInt(x, y, width, height);
        i4("clearRect", x, y, width, height);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        System.out.println("drawString AttributedCharacterIterator");
        if (iterator == null) {
            throw new NullPointerException("AttributedCharacterIterator is null");
        }
        TextLayout layout = new TextLayout(iterator, getFontRenderContext());
        Shape textShape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
        fill(textShape);

        int bi = iterator.getBeginIndex();
        int ei = iterator.getEndIndex();
        char[] cc = new char[ei - bi];
        for (int i = bi - 1; i < ei; i++) {
            cc[i] = iterator.next();
        }
        // this ignores attributes we ill do with them later.
        String s = new String(cc);
        out.writeCmd(DRAWSTRINGF);
        out.writeFloat(x);
        out.writeFloat(y);
        wStr(s);

    }

    /**
     * a default image is so abstract that it is virtually impossible to
     * decide how to write it.
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        out.writeCmd(IMAGETR);
        wTransform(xform);
        wObject(img);
        debugPrintln("drawImage");
        return true;
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        debugPrintln("drawImage");
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        debugPrintln("drawImage");
        return true;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        debugPrintln("drawImage");
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        debugPrintln("drawImage");
        return true;
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        debugPrintln("drawImage " + x + ", " + y);
        out.writeCmd(IMAGEXY);
        wInt(x, y);
        wObject(img);
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        debugPrintln("drawImage");
        return true;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        i4("drawImage ", x, y, width, height);
        out.writeCmd(IMAGEXYWH);
        wInt(x, y, width, height);
        wObject(img);
        return true;
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        debugPrintln("drawRenderableImage");
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        debugPrintln("drawRenderedImage");
    }

    public void rotate(double theta) {
        state.rotate(theta);
        out.writeCmd(ROTATE);
        out.writeDouble(theta);
        debugPrintln("rotate(" + theta + ")");
    }

    public void rotate(double theta, double x, double y) {
        state.rotate(theta, x, y);
        if (theta == 0)
            return;
        out.writeCmd(ROTATEXY);
        out.writeDouble(theta);
        out.writeDouble(x);
        out.writeDouble(y);
        debugPrintln("rotate(" + theta + "," + x + "," + y + ")");
    }

    public void scale(double sx, double sy) {
        state.scale(sx, sy);
        out.writeCmd(SCALEXY);
        out.writeDouble(sx);
        out.writeDouble(sy);
        debugPrintln("scale");
    }

    public void setPaint(Paint paint) {
        state.setPaint(paint);
        out.writeCmd(SETPAINT);
        wObject(paint);
        debugPrintln("setPaint");
    }

    public void setTransform(AffineTransform tx) {
        state.setTransform(tx);
        out.writeCmd(SETTRANSFORM);
        wTransform(tx);
        debugPrintln("setTransform(" + tx.getClass() + ")");
    }

    public void shear(double shx, double shy) {
        state.shear(shx, shy);
        out.writeCmd(SHEAR);
        out.writeDouble(shx);
        out.writeDouble(shy);
        debugPrintln("shear(" + shx + "," + shy + ")");
    }

    public void transform(AffineTransform tx) {
        state.transform(tx);
        out.writeCmd(CONCATTRANSFORM);
        wTransform(tx);
        debugPrintln("transform");
    }

    public void translate(double tx, double ty) {
        state.translate(tx, ty);
        out.writeCmd(TRANSLATEF);
        out.writeDouble(tx);
        out.writeDouble(ty);
        debugPrintln("translate(" + tx + "," + ty + ")");
    }

    public void translate(int x, int y) {
        state.translate(x, y);
        out.writeCmd(TRANSLATE);
        wInt(x, y);
        debugPrintln("translate(" + x + "," + y + ")");
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        debugPrintln("setRenderingHint(" + hintKey + "," + hintValue + ")");
        state.getRenderingHints().put(hintKey, hintValue);
        RenderingHintMapper m = renderingHintKeyMap.get(hintKey);
        if (m.currVal == hintValue) // value already set skip it
            return;
        m.currVal = hintValue;
        out.writeCmd(SETRENDERINGHINT);
        out.writeByte(m.mapto);
        byte valIndex = renderingHintValuesMap.get(hintValue);
        out.writeByte(valIndex);
        //byte valIndex = ((Byte) renderingHintValuesMap.get(hintValue)).byteValue();
    }

    public void setRenderingHints(Map<?, ?> hints) {
        state.getRenderingHints().clear();
        state.getRenderingHints().putAll(hints);
        wRenderingHints("setRenderingHints", SETRENDERINGHINTS, hints);
    }

    public void addRenderingHints(Map<?, ?> hints) {
        // rendering hints are currently ignored. Tough luck.
        state.getRenderingHints().putAll(hints);
        wRenderingHints("addRenderingHints", ADDRENDERINGHINTS, hints);
    }

    private void wRenderingHints(String what, byte tag, Map<?, ?> hints) {
        Iterator<? extends Map.Entry<?, ?>> it = hints.entrySet().iterator();
        boolean virgin = true;

        while (it.hasNext()) {
            Map.Entry<?, ?> e = it.next();
            RenderingHints.Key key = (RenderingHints.Key) e.getKey();
            RenderingHintMapper m = renderingHintKeyMap.get(key);
			/*
			if (m.currVal == e.getValue()) // value already set skip it
			{
				continue;
			}
			*/
            if (m == null)
                continue;
            m.currVal = e.getValue();
            if (virgin) // this prevents ouput completely if there is no changed value
            {
                out.writeByte(tag);
                debugPrint(what + "(" + hints.size() + ",[");
                virgin = false;
            }
            debugPrintln(e.getKey() + ":" + e.getValue());
            out.writeByte(m.mapto);
            byte valIndex = renderingHintValuesMap.get(e.getValue());
            out.writeByte(valIndex);
        }
        if (!virgin) {
            byte b = (byte) -1;
            out.writeByte(b);
            debugPrintln("]");
        }
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // copyArea currently ignored. Tough luck.
        // how do you copy an area of vectors?
        debugPrintln("copyArea");
    }


    public void setComposite(Composite comp) {
        if (comp instanceof AlphaComposite) {
            AlphaComposite ac = (AlphaComposite) comp;
            if (state.getComposite() instanceof AlphaComposite) {
                AlphaComposite sac = (AlphaComposite) state.getComposite();
                if (ac.getAlpha() == sac.getAlpha() && ac.getRule() == sac.getRule())
                    return;
            }
            debugPrintln("setCompositeAlpha(" + ac.getAlpha() + "," + ac.getRule() + ")");
            out.writeCmd(ALPHACOMPOSITE);
            out.writeInt(ac.getRule());
            out.writeFloat(ac.getAlpha());
        } else
            debugPrintln("setComposite(" + comp.getClass() + ")");
        state.setComposite(comp);
    }

    public void setPaintMode() {
        out.writeCmd(SETPAINTMODE);
        debugPrintln("setPaintMode");
    }

    public void setXORMode(Color color) {
        out.writeCmd(SETXORMODE);
        out.writeInt(color.getRGB());
        debugPrintln("setXORMode(" + color.getRGB() + ")");
    }

    public ArrayList<Object> getReferencedObjects() {
        return refObjects;
    }

    public RenderedGraphic getRenderedGraphic() {
        return new RenderedGraphic(getBytes(), getReferencedObjects());
    }

    public void intersectClip(Shape s) {
        Shape clip = state.getClip();
        if (clip == null)
            clip = s;
        else
            clip = intersectShapes(clip, s, false, true);
        state.setClip(clip);
    }

    /*
     * Intersect two Shapes by the simplest method, attempting to produce
     * a simplified result.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     */
    Shape intersectShapes(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
            return ((Rectangle) s1).intersection((Rectangle) s2);
        }
        if (s1 instanceof Rectangle2D) {
            return intersectRectShape((Rectangle2D) s1, s2, keep1, keep2);
        } else if (s2 instanceof Rectangle2D) {
            return intersectRectShape((Rectangle2D) s2, s1, keep2, keep1);
        }
        return intersectByArea(s1, s2, keep1, keep2);
    }

    /*
     * Intersect a Rectangle with a Shape by the simplest method,
     * attempting to produce a simplified result.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     */
    Shape intersectRectShape(Rectangle2D r, Shape s, boolean keep1, boolean keep2) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2 = (Rectangle2D) s;
            Rectangle2D outrect;
            if (!keep1) {
                outrect = r;
            } else if (!keep2) {
                outrect = r2;
            } else {
                outrect = new Rectangle2D.Float();
            }
            double x1 = Math.max(r.getX(), r2.getX());
            double x2 = Math.min(r.getX() + r.getWidth(), r2.getX() + r2.getWidth());
            double y1 = Math.max(r.getY(), r2.getY());
            double y2 = Math.min(r.getY() + r.getHeight(), r2.getY() + r2.getHeight());

            if (((x2 - x1) < 0) || ((y2 - y1) < 0))
                // Width or height is negative. No intersection.
                outrect.setFrameFromDiagonal(0, 0, 0, 0);
            else
                outrect.setFrameFromDiagonal(x1, y1, x2, y2);
            return outrect;
        }
        if (r.contains(s.getBounds2D())) {
            if (keep2) {
                s = new GeneralPath(s);
            }
            return s;
        }
        return intersectByArea(r, s, keep1, keep2);
    }

    /*
     * Intersect two Shapes using the Area class.  Presumably other
     * attempts at simpler intersection methods proved fruitless.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     * @see #intersectShapes
     * @see #intersectRectShape
     */
    Shape intersectByArea(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        Area a1, a2;

        // First see if we can find an overwriteable source shape
        // to use as our destination area to avoid duplication.
        if (!keep1 && (s1 instanceof Area)) {
            a1 = (Area) s1;
        } else if (!keep2 && (s2 instanceof Area)) {
            a1 = (Area) s2;
            s2 = s1;
        } else {
            a1 = new Area(s1);
        }

        if (s2 instanceof Area) {
            a2 = (Area) s2;
        } else {
            a2 = new Area(s2);
        }

        a1.intersect(a2);
        if (a1.isRectangular()) {
            return a1.getBounds();
        }

        return a1;
    }
}
