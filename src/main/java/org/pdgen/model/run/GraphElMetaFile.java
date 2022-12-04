// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.Barcode4jCell;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DeferredTotalPagesCell;
import org.pdgen.metafilegraphics.RenderedGraphic;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;

public class GraphElMetaFile extends GraphElContent  implements GrahElPostprocess
{
    private static final long serialVersionUID = 7L;
    RenderedGraphic data;
	private int posInDisplayList;
	float translateX;
	float translateY;

	private GraphElMetaFile(GraphElMetaFile from)
    {
        super(from);
        data = from.data;
    }
    public GraphElMetaFile(RenderedGraphic data, Color background, CellDef src, ImageIcon backgroundImage)
	{
		super(background, src, backgroundImage);
		this.data = data;
	}

	public void print(JoriaPrinter pr)
	{
		pr.printMetaFile(this);
	}

    public GraphElContent copy()
    {
        return new GraphElMetaFile(this);
    }

	public void setText(final String newText, final Graphics2D g)
	{
		if (src instanceof Barcode4jCell)
		{
			Barcode4jCell bc = (Barcode4jCell) src;
			data = bc.renderIntoMetaFile(g, newText, x-translateX,y-translateY,width,height);
		}
	}

	public DeferredTotalPagesCell getPostprocessSource()
	{
		return (DeferredTotalPagesCell) src;
	}

	public int getPosInDisplayList()
	{
		return posInDisplayList;
	}

	public void setPosInDisplayList(final int posInDisplayList)
	{
		this.posInDisplayList = posInDisplayList;
	}

	public void translate(final float offsetx, final float offsety)
	{
		super.translate(offsetx, offsety);
		translateX += offsetx;
		translateY += offsety;
	}
}
