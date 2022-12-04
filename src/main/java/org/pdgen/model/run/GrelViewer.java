// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;

public class GrelViewer
{

	int pageWidth;
	int pageHeight;
	int pageWidthMax;
	int pageHeightMax;
	DisplayList pageContents;
	float scale = 1;

	public GrelViewer(int pageWidthMax, int pageHeightMax)
	{
		this.pageWidthMax = pageWidthMax;
		this.pageHeightMax = pageHeightMax;
	}

	public void print(JoriaPrinter jp) throws IOException
	{
		jp.printDecoration(this);
		if (pageContents != null)
		{
			DisplayList.BLIterator it = pageContents.getIterator();
			while (it.more())
			{
				it.next().print(jp);
			}
		}
	}

    public Dimension getDimension()
    {
        return new Dimension(pageWidth + 30, pageHeight + 30);
    }

    public Dimension setPageContents(DisplayList pageContents)
	{
		pageWidth = (int) (pageContents.pageWidthBase * scale);
		pageHeight = (int) (pageContents.pageHeightBase * scale);
		this.pageContents = pageContents;
        return new Dimension(pageWidth + 30, pageHeight + 30);
	}

	public Dimension setScale(float scale)
	{
		this.scale = scale;
		if (pageContents != null)
		{
			pageWidth = (int) (pageContents.pageWidthBase * scale);
			pageHeight = (int) (pageContents.pageHeightBase * scale);
		}
		else
		{
			pageWidth = (int) (pageWidthMax * scale);
			pageHeight = (int) (pageHeightMax * scale);
		}
		return new Dimension(pageWidth + 30, pageHeight + 30);
	}

	public Dimension getPreferredSizeUnscaled()
	{
		return new Dimension(pageWidthMax + 40, pageHeightMax + 40);
	}

	public int getPageHeight()
	{
		return pageHeight;
	}

	public int getPageWidth()
	{
		return pageWidth;
	}

	public float getScale()
	{
		return scale;
	}

	public GraphElContent getDrillDownAt(Point point)
	{
		DisplayList.BLIterator it = pageContents.getIterator();
        point = new Point(point);
        point.x /= scale;
        point.y /= scale;
		if (it.more())
			it.next(); // skip first element which is the page background
		while (it.more())
		{
			final GraphicElement grel = it.next();
			if (grel instanceof GraphElContent && ((GraphElContent) grel).contains(point.x, point.y))
			{
				return (GraphElContent) grel;
			}
		}
		return null;
	}
}
