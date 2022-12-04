// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;

import org.pdgen.model.RDBase;
import org.pdgen.env.Res;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.OutputMode;
import org.pdgen.model.run.RVAny;
import org.pdgen.model.run.RVImage;
import org.pdgen.model.run.RVSupressHeader;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

public class PictureCellDef extends PictureCellBase implements I18nKeyHolder
{
    private static final long serialVersionUID = 7L;
    private String pictureFileName;
	protected transient ImageIcon myPicture;

	public PictureCellDef(PictureCellDef from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
		myPicture = from.myPicture;
		pictureFileName = from.pictureFileName;
	}

	public PictureCellDef(TemplateModel parentGrid, String s)
	{
		super(parentGrid);
		pictureFileName =  s;
		myPicture = getPicture(Internationalisation.NOREPLACE);
	}

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new PictureCellDef(this, newContainerGrid);
	}

	public void paint(Graphics2D g, float x0, float y0, float w, float h, Locale loc)
	{
		Shape oldClipShape = g.getClip();
		Rectangle2D oldClip = oldClipShape.getBounds2D();
		Rectangle2D cr = oldClip.createIntersection(new Rectangle2D.Float(x0, y0, w, h));
		g.setClip(cr);
		final ImageIcon picture = getPicture(loc);
		int pictWidth = picture.getIconWidth();
		if (mustScale(pictWidth))
		{
			float width = targetWidth.getVal();
			float scale = width / pictWidth;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.translate(x0, y0);
			g2.scale(scale, scale);
			g2.drawImage(picture.getImage(), 0, 0, null);
			g2.dispose();
		}
		else
		{
			g.translate(x0, y0);
			g.drawImage(picture.getImage(), 0, 0, null);
			g.translate(-x0, -y0);
		}
		g.setClip(oldClip);
	}

	public ImageIcon getPicture(Locale loc)
	{
		if (myPicture == null || myPicture == Res.missingImageIcon)
		{
			myPicture = RVImage.buildImageFromFileName(pictureFileName, loc, myPicture == null);
		}
		return myPicture;
	}

	public String getPictureFileName()
	{
		return pictureFileName;
	}

	public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
	{
		if (!isVisible(outMode, from))
			return RVSupressHeader.instance;
		else
			return new RVImage(pictureFileName, getPicture(outMode.getRunEnv().getLocale()));
	}

	public void setI18nKey(String newVal)
	{
		pictureFileName = newVal;
	}

	protected Object readResolve() throws java.io.ObjectStreamException
	{
		cascadedStyle = null;
		myWidth = Float.NaN;
		myHeight = Float.NaN;
		return this;
	}

	public float getMaxWidth(RVAny value, Locale loc, Graphics2D g)
	{
		if (value == RVSupressHeader.instance)
			return 0;
		if (targetWidth != null && !targetWidth.isExpandable())
			return targetWidth.getVal();
		if (value instanceof RVImage)
		{
			//System.out.println("Warning SimpleTextCellDef.getMaxWidth() round!");
			//return (int)(((RVString) values).getWidth()+1);
			return ((RVImage) value).getPicture().getIconWidth();
		}
		else
			throw new JoriaAssertionError("PictureCellDef not a image col: " + value.getClass().getName());
	}

	public void makeFilenameRelative(JoriaFileService fs)
	{
		pictureFileName = fs.makeFilenameRelative(pictureFileName);
	}

	public void makeFilenameAbsolute(JoriaFileService fs)
	{
		pictureFileName = fs.makeFilenameAbsolute(pictureFileName);
	}

	public void getAllReferencedFiles(HashSet<String> ret)
	{
		ret.add(pictureFileName);
	}

	public void adjustSize(Locale loc, Graphics2D g)
	{
		super.adjustSize(loc, g);
		final ImageIcon picture = getPicture(loc);
		final int pictWidth = picture.getIconWidth();
		if (mustScale(pictWidth))
		{
			float width = targetWidth.getVal();
			myWidth += width;
			myHeight += (float) picture.getIconHeight() / pictWidth * width;
		}
		else
		{
			myWidth += pictWidth;
			myHeight += picture.getIconHeight();
		}
	}


	public void setPictureFileName(String pictureFileName)
	{
		this.pictureFileName = pictureFileName;
		myPicture = null;
		grid.fireChange("image file changed");
		cascadedStyle = null;
		myWidth = Float.NaN;
		myHeight = Float.NaN;
	}
}
