// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.FlexSize;

import org.pdgen.model.RDBase;
import org.pdgen.env.Res;
import org.pdgen.model.TemplateModel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

public class HorizontalBarCell extends DataPictureCell
{
    private static final long serialVersionUID = 7L;
    transient BufferedImage editorImage;
	FlexSize presetWidth;
	FlexSize presetHeight;
	private boolean percentageSum;
	private boolean percentageMax;

	// TODO
	// Folgende Probleme haben wir noch:
	// 2. Die Groessenberechnung koennte besser seien. Der rechte Rand wird ignoriert.
	// 3. HBCP.onShow setzt die Werte zurueck.
	// 4. was machen wir bei > 1?
	// 5. Der FlexSizeEditor ist nicht rund.
	// 6. Was macht der Export ?
	// TODO
	public HorizontalBarCell(TemplateModel containerGrid, JoriaAccess dataAxs)
	{
		super(containerGrid, dataAxs);
		presetWidth = new FlexSize(20, FlexSize.mm);
		presetHeight = new FlexSize(0.3f, FlexSize.wfactor);
	}

	public HorizontalBarCell(TemplateModel containerGrid)
	{
		super(containerGrid, null);
		presetWidth = FlexSize.FLEX;
		presetHeight = new FlexSize(0.71f, FlexSize.wfactor);
	}

	public HorizontalBarCell(FreeCellDef from, TemplateModel containerGrid)
	{
		super(from, containerGrid);
		HorizontalBarCell ori = (HorizontalBarCell) from;
		presetHeight = FlexSize.newFlexSize(ori.presetHeight);
		presetWidth = FlexSize.newFlexSize(ori.presetWidth);
		percentageMax = ori.percentageMax;
		percentageSum = ori.percentageSum;
	}

	public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc)
	{
		makeEditorImage();
		if (w < editorImage.getWidth(null) || h < editorImage.getHeight(null))
		{
			Image scaledInstance = editorImage.getScaledInstance(Math.round(x0), Math.round(x0), Image.SCALE_SMOOTH);
			p.drawImage(scaledInstance, (int) Math.round(x0 + 0.4), (int) Math.round(y0 + 0.4), (int) Math.round(w + 0.4), (int) Math.round(h + 0.4), null);
		}
		else
			p.drawImage(editorImage, (int) Math.round(x0 + 0.4), (int) Math.round(y0 + 0.4), (int) Math.round(w + 0.4), (int) Math.round(h + 0.4), null);
	}

	protected void adjustSize(Locale loc, Graphics2D g)
	{
		super.adjustSize(loc, g);
		editorImage = null;
		makeEditorImage();
		myWidth += editorImage.getWidth(null);
		myHeight += editorImage.getHeight(null);
	}

	public void resetLayout()
	{
		super.resetLayout();
		editorImage = null;
	}

	public void clearCachedStyle()
	{
		super.clearCachedStyle();
		editorImage = null;
	}

	private void makeEditorImage()
	{
		if (editorImage == null)
		{
			editorImage = makeImage(0.7);
		}
	}

	public BufferedImage makeImage(double val)
	{
		if (Double.isNaN(val))
			return null;
		if (val < 0.0)
			val = 0.0;
		if (val > 1.0)
			val = 1.0;
		float width = presetWidth.getVal();
		float height = presetWidth.getVal() / 0.71f;
		if (presetHeight.getUnit() > FlexSize.flex && presetHeight.getUnit() < FlexSize.asis)
		{
			height = presetHeight.getVal();
		}
		else if (presetHeight.getUnit() == FlexSize.wfactor)
		{
			height = presetWidth.getVal() * presetHeight.getVal();
		}
		BufferedImage bi = new BufferedImage(Math.round(width), Math.round(height), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = bi.getGraphics();
		g.setColor(getCascadedStyle().getBackground());
		g.fillRect(0, 0, Math.round(width), Math.round(height));
		g.setColor(getCascadedStyle().getForeground());
		g.fillRect(0, 0, Math.round(width * (float) val), Math.round(height));
		g.dispose();
		return bi;
	}

	public double getValueDouble(DBData from, AggregateCollector into) throws JoriaDataException
	{
		if (data instanceof JoriaAccessTyped)
			return ((JoriaAccessTyped) data).getFloatValue((DBObject) from, into.getRunEnv());
		DBData cv = data.getValue(from, data, into.getRunEnv());
		if (cv == null || cv.isNull())
			return DBReal.NULL;
		if (cv instanceof DBReal)
			return ((DBReal) cv).getRealValue();
		else if (cv instanceof DBInt)
			return ((DBInt)cv).getIntValue();
		throw new JoriaAssertionError("value type for a horizontal bar must be a float. " + cv.getClass().getName());
	}

	public String getValueString(DBData from, AggregateCollector into) throws JoriaDataException
	{
		DBData cv = data.getValue(from, data, into.getRunEnv());
		if (cv == null)
			return null;
		return SimpleTextCellDef.wrapText(into.format(cv, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
	}

	public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
	{
		if (repeater != null)
		{
			DBCollection source = (DBCollection) from;
			if (source == null)
				return null;
			final int length = Math.max(source.getLength(), RVStringCol.startSize);
			return new RVHorizontalBarCol(length, this);
		}
		else
		{
			if (from != null)
			{
				if (!isVisible(outMode, from))
					return RVSupressHeader.instance;
				try
				{
					if (data == null)
						return null;
					double v = getValueDouble(from, outMode.getRunEnv().getPager());
					BufferedImage pict = makeImage(v);
					if (pict != null)
						return new RVImage(pict, outMode.getRunEnv().getLocale(), true);
					else
						return null;
				}
				catch (JoriaDataRetrievalExceptionInUserMethod e)
				{
					return new RVImage(Res.missingImageIcon, outMode.getRunEnv().getLocale(), true);
				}
			}
		}
		return null;
	}

	public void setAccessor(JoriaAccess accessor)
	{
		data = accessor;
	}

	public FlexSize getPresetWidth()
	{
		return presetWidth;
	}

	public void setPresetWidth(FlexSize presetWidth)
	{
		this.presetWidth = presetWidth;
	}

	public FlexSize getPresetHeight()
	{
		return presetHeight;
	}

	public void setPresetHeight(FlexSize presetHeight)
	{
		this.presetHeight = presetHeight;
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new HorizontalBarCell(this, newContainerGrid);
	}

    public boolean isPercentage()
	{
		return percentageSum || percentageMax;
	}

	public boolean isPercentageSum()
	{
		return percentageSum;
	}

	public void setPercentageSum(boolean percentageSum)
	{
		this.percentageSum = percentageSum;
		if (percentageSum)
			percentageMax = false;
	}

	public boolean isPercentageMax()
	{
		return percentageMax;
	}

	public void setPercentageMax(boolean percentageMax)
	{
		this.percentageMax = percentageMax;
		if (percentageMax)
			percentageSum = false;
	}
}
