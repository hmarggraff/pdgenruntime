// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;
//MARKER The strings in this file shall not be translated


import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;

public class MetaFileGraphics2DIn implements DrawCommands
{


	protected MetaFileReader in;
    ArrayList<?> refObjects; // images and other heavy objects are not serialized, but kept in this list and then referenced.
	Stack<Graphics2D> contexts = new Stack<Graphics2D>();

	public MetaFileGraphics2DIn(RenderedGraphic data)
	{
		refObjects = data.refObjects;
		in = new BinMetaFileReader(data.data);
	}

	public MetaFileGraphics2DIn(ArrayList<?> refObjects)
	{
		this.refObjects = refObjects;
		in = new DebugMetaFileReader();
	}

	public void loop(Graphics backup) throws IOException
	{
		Graphics2D b = (Graphics2D) backup;
		in.reset();
		for (int kc = 0; ; kc++)
		{

			byte cmd = in.nextCmd();
			//System.out.println(kc + " = " + cmd);
			switch (cmd)
			{
				case DrawCommands.DRAWLINE:
					{
						int x1 = in.readInt();
						int y1 = in.readInt();
						int x2 = in.readInt();
						int y2 = in.readInt();
						b.drawLine(x1, y1, x2, y2);
						break;
					}
				case DrawCommands.DRAWRECT:
					{
						int x = in.readInt();
						int y = in.readInt();
						int w = in.readInt();
						int h = in.readInt();
						b.drawRect(x, y, w, h);
						break;
					}
				case DrawCommands.FILLRECT:
					{
						int x = in.readInt();
						int y = in.readInt();
						int w = in.readInt();
						int h = in.readInt();
						b.fillRect(x, y, w, h);
						break;
					}
				case DrawCommands.DRAWROUNDRECT:
					{
						int x1 = in.readInt();
						int x2 = in.readInt();
						int y1 = in.readInt();
						int y2 = in.readInt();
						int aw = in.readInt();
						int ah = in.readInt();
						b.drawRoundRect(x1, y1, x2, y2, aw, ah);
						break;
					}
				case DrawCommands.FILLROUNDRECT:
					{
						int x1 = in.readInt();
						int x2 = in.readInt();
						int y1 = in.readInt();
						int y2 = in.readInt();
						int aw = in.readInt();
						int ah = in.readInt();
						b.fillRoundRect(x1, y1, x2, y2, aw, ah);
						break;
					}
				case DrawCommands.DRAWOVAL:
					{
						int x1 = in.readInt();
						int x2 = in.readInt();
						int y1 = in.readInt();
						int y2 = in.readInt();
						b.drawOval(x1, y1, x2, y2);
						break;
					}
				case DrawCommands.FILLOVAL:
					{
						int x1 = in.readInt();
						int x2 = in.readInt();
						int y1 = in.readInt();
						int y2 = in.readInt();
						b.fillOval(x1, y1, x2, y2);
						break;
					}
				case DrawCommands.DRAWARC:
					{
						int x = in.readInt();
						int y = in.readInt();
						int w = in.readInt();
						int h = in.readInt();
						int a0 = in.readInt();
						int a = in.readInt();
						b.drawArc(x,y,w,h,a0,a);
						break;
					}
				case DrawCommands.FILLARC:
					{
						int x = in.readInt();
						int y = in.readInt();
						int w = in.readInt();
						int h = in.readInt();
						int a0 = in.readInt();
						int a = in.readInt();
						b.fillArc(x,y,w,h,a0,a);
						break;
					}
				case DrawCommands.DRAWPOLYLINE:
					{
						int np = in.readInt();
						int[] xPoints = new int[np];
						int[] yPoints = new int[np];
						for (int i = 0; i < np; i++)
						{
							xPoints[i] = in.readInt();
							yPoints[i] = in.readInt();
						}
						b.drawPolyline(xPoints, yPoints, np);
						break;
					}
				case DrawCommands.DRAWPOLYGON:
					{
						int np = in.readInt();
						int[] xPoints = new int[np];
						int[] yPoints = new int[np];
						for (int i = 0; i < np; i++)
						{
							xPoints[i] = in.readInt();
							yPoints[i] = in.readInt();
						}
						b.drawPolygon(xPoints, yPoints, np);
						break;
					}
				case DrawCommands.FILLPOLYGON:
					{
						int np = in.readInt();
						int[] xPoints = new int[np];
						int[] yPoints = new int[np];
						for (int i = 0; i < np; i++)
						{
							xPoints[i] = in.readInt();
							yPoints[i] = in.readInt();
						}
						b.fillPolygon(xPoints, yPoints, np);
						break;
					}
				case DrawCommands.DRAWSTRING:
					{
						int x = in.readInt();
						int y = in.readInt();
						String s = readString();
						//System.out.println("text: " + s);
						b.drawString(s, x, y);
						break;
					}
				case DrawCommands.DRAWSTRINGF:
					{
						float x = in.readFloat();
						float y = in.readFloat();
						String s = readString();
						b.drawString(s, x, y);
						break;
					}
				case DrawCommands.DRAWSHAPE:
					{
						GeneralPath p = readPath();
						b.draw(p);
						break;
					}
				case DrawCommands.FILLSHAPE:
					{
						GeneralPath p = readPath();
						b.fill(p);
						break;
					}
				case DrawCommands.SETBACKGROUND:
					{
						Color c = new Color(in.readInt(), in.readInt(), in.readInt(), in.readInt());
						b.setBackground(c);
						break;
					}
				case DrawCommands.SETCOLOR:
					{
						Color c = new Color(in.readInt(), in.readInt(), in.readInt(), in.readInt());
						b.setColor(c);
						break;
					}
				case DrawCommands.SETFONT:
					{
						Font f = readFont();
						//Trace.logDebug("font: " + f.getName() + " " + f.getSize());
						b.setFont(f);
						break;
					}
				case DrawCommands.SETSTROKE:
					{
						float lw = in.readFloat();
						int ec = in.readInt();
						int lj = in.readInt();
						float ml = in.readFloat();
						int dashl = in.readInt();
						float[] dashes = null;
						if (dashl > 0)
						{
							dashes = new float[dashl];
							for (int i = 0; i < dashl; i++)
							{
								dashes[i] = in.readFloat();
							}
						}
						float dashPhase = in.readFloat();
						BasicStroke bs = new BasicStroke(lw, ec, lj, ml, dashes, dashPhase);
						b.setStroke(bs);
						break;
					}
				case DrawCommands.CLEARCLIP:
					{
						b.clip(null);
						break;
					}
				case DrawCommands.CLIPSHAPE:
					{
						GeneralPath gp = readPath();
						b.clip(gp);
						break;
					}
				case DrawCommands.SETCLIPSHAPE:
					{
						GeneralPath gp = readPath();
						b.setClip(gp);
						break;
					}
				case DrawCommands.CLIPRECT:
					{
						final int x = in.readInt();
						final int y = in.readInt();
						final int width = in.readInt();
						final int height = in.readInt();
						b.clipRect(x, y, width, height);
						break;
					}
				case DrawCommands.SETCLIPRECT:
					{
						b.setClip(in.readInt(), in.readInt(), in.readInt(), in.readInt());
						break;
					}
				case DrawCommands.CLEARRECT:
					{
						b.clearRect(in.readInt(), in.readInt(), in.readInt(), in.readInt());
						break;
					}
				case DrawCommands.GLYPHVECTOR:
					{
						float x = in.readFloat();
						float y = in.readFloat();
						Font f = readFont();
						int cl = in.readInt();
						int[] codes = new int[cl];
						for (int i = 0; i < cl; i++)
						{
							codes[i] = in.readInt();
						}
						GlyphVector gv = f.createGlyphVector(b.getFontRenderContext(), codes);
						b.drawGlyphVector(gv, x, y);
						break;
					}
				case DrawCommands.IMAGETR:
					{
						AffineTransform t = readTransform();
						int objid = in.readInt();
						b.drawImage((Image) refObjects.get(objid), t, new ImageObserver()
						{
							public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
							{
								return false;
							}
						});
						break;
					}
				case DrawCommands.IMAGEXY:
					{
						int x = in.readInt();
						int y = in.readInt();
						int objid = in.readInt();
						b.drawImage((Image) refObjects.get(objid), x, y, new ImageObserver()
						{
							public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
							{
								return false;
							}
						});
						break;
					}
				case DrawCommands.IMAGEXYWH:
					{
						int x = in.readInt();
						int y = in.readInt();
						int w = in.readInt();
						int h = in.readInt();
						int objid = in.readInt();
						b.drawImage((Image) refObjects.get(objid), x, y, w, h, new ImageObserver()
						{
							public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
							{
								return false;
							}
						});
						break;
					}
				case DrawCommands.ROTATE:
					{
						b.rotate(in.readDouble());
						break;
					}
				case DrawCommands.ROTATEXY:
					{
						b.rotate(in.readDouble(), in.readDouble(), in.readDouble());
						break;
					}
				case DrawCommands.SCALEXY:
					{
						b.scale(in.readDouble(), in.readDouble());
						break;
					}
				case DrawCommands.SHEAR:
					{
						b.shear(in.readDouble(), in.readDouble());
						break;
					}
				case DrawCommands.TRANSLATE:
					{
						b.translate(in.readInt(), in.readInt());
						break;
					}
				case DrawCommands.TRANSLATEF:
					{
						final double tx = in.readDouble();
						final double ty = in.readDouble();
						b.translate(tx, ty);
						break;
					}
				case DrawCommands.SETPAINT:
					{
						int objid = in.readInt();
						Paint p = (Paint) refObjects.get(objid);
						b.setPaint(p);
						break;
					}
				case DrawCommands.SETTRANSFORM:
					{
						AffineTransform t = readTransform();
						b.setTransform(t);
						break;
					}
				case DrawCommands.CONCATTRANSFORM:
					{
						AffineTransform t = readTransform();
						b.transform(t);
						break;
					}
				case DrawCommands.SETXORMODE:
					{
						final int rgb = in.readInt();
						Color c = new Color(rgb);
						b.setXORMode(c);
						break;
					}
				case DrawCommands.ALPHACOMPOSITE:
					{
						final int rule = in.readInt();
						final float alpha = in.readFloat();
						AlphaComposite ac = AlphaComposite.getInstance(rule, alpha);
						b.setComposite(ac);
						break;
					}
				case DrawCommands.CREATE:
					{
						//System.out.println("create " + b.getTransform().getTranslateX());
						contexts.push(b);
						b = (Graphics2D) b.create();
						break;
					}
				case DrawCommands.DISPOSE:
					{
						//System.out.println("create " + b.getTransform().getTranslateX());
						b.dispose();
						b = contexts.pop();
						break;
					}
				case DrawCommands.SETRENDERINGHINT:
					{
						int k = in.readByte();
						int v = in.readByte();
						b.setRenderingHint(DrawCommands.renderingHintKeyTable[k], DrawCommands.renderingHintValueTable[v]);
						break;
					}
				case DrawCommands.ADDRENDERINGHINTS:
					{
						HashMap<RenderingHints.Key,Object> newHints = new HashMap<RenderingHints.Key,Object>();
						for (; ;)
						{
							int k = in.readByte();
							if (k == -1)
								break;
							int v = in.readByte();
							newHints.put(DrawCommands.renderingHintKeyTable[k], DrawCommands.renderingHintValueTable[v]);
						}
						b.addRenderingHints(newHints);
						break;
					}
				case DrawCommands.SETRENDERINGHINTS:
					{
						HashMap<RenderingHints.Key,Object> newHints = new HashMap<RenderingHints.Key,Object>();
						for (; ;)
						{
							int k = in.readByte();
							if (k == -1)
								break;
							int v = in.readByte();
							newHints.put(DrawCommands.renderingHintKeyTable[k], DrawCommands.renderingHintValueTable[v]);
						}
						b.setRenderingHints(newHints);
						break;
					}
				case DrawCommands.CLOSE:
					{
						//Trace.logDebug("paint done");
						b.setClip(null);
						b.dispose();
						in.close();
						return;
					}
                case DrawCommands.CLEARSETCLIP:
                    {
                        b.setClip(null);
                        break;
                    }
				default:
					{
						/* if in is a DebugMetaFileReader then use this:
						throw new RuntimeException("Unknown command " + cmd + " at " + in.line);
						*/
						throw new RuntimeException("Unknown command " + cmd);
					}

			}
		}
	}

	protected GeneralPath readPath() throws IOException
	{
		final int windingRule = in.readInt();
		GeneralPath p = new GeneralPath(windingRule);
		//final float[] coords = new float[6];
		int seg = in.nextCmd();
		while (seg != DrawCommands.PATHEND)
		{
			switch (seg)
			{
				case DrawCommands.SEG_MOVETO:
					{
						p.moveTo(in.readFloat(), in.readFloat());
						break;
					}
				case DrawCommands.SEG_LINETO:
					{
						p.lineTo(in.readFloat(), in.readFloat());
						break;
					}
				case DrawCommands.SEG_QUADTO:
					{
						p.quadTo(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
						break;
					}
				case DrawCommands.SEG_CUBICTO:
					{
						p.curveTo(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
						break;
					}
				case DrawCommands.SEG_CLOSE:
					{
						p.closePath();
						break;
					}
			}
			seg = in.nextCmd();
		}
		return p;
	}

	protected String readString() throws IOException
	{
		int l = in.readInt();
		byte[] bb = new byte[l];
		for (int i = 0; i < l; i++)
		{
			bb[i] = in.readByte();
		}
		String s = new String(bb, StandardCharsets.UTF_8);
		return s;
	}

	protected Font readFont() throws IOException
	{
		String fontFamily = readString();
		int style = in.readInt();
		float size = in.readFloat();
		Hashtable<TextAttribute, Object> fRequestedAttributes = new Hashtable<TextAttribute, Object>(5, (float) 0.9);
		fRequestedAttributes.put(TextAttribute.FAMILY, fontFamily);
		fRequestedAttributes.put(TextAttribute.SIZE, size);
		fRequestedAttributes.put(TextAttribute.WEIGHT, (style & Font.BOLD) != 0 ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
		fRequestedAttributes.put(TextAttribute.POSTURE, (style & Font.ITALIC) != 0 ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
		Font f = new Font(fRequestedAttributes);
		return f;
	}

	protected AffineTransform readTransform() throws IOException
	{
		AffineTransform t = new AffineTransform(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
		return t;
	}

}
