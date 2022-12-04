// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.Length;
import org.pdgen.model.style.LengthUnit;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.metafilegraphics.MetaFileGraphics2D;
import org.pdgen.metafilegraphics.RenderedGraphic;
import org.pdgen.env.Res;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.RtfOutput.OutputData;
import org.pdgen.oql.JoriaDeferedQuery;
import org.pdgen.projection.ComputedField;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.krysalis.barcode4j.ChecksumMode;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;

import javax.swing.*;// Created by User: hmf on 14.12.2005

public class Barcode4jCell extends DataPictureCell implements  RenderingCellDef ,DeferredTotalPagesCell
{
	protected static final String editorText = "123456789012";
    private static final long serialVersionUID = 7L;

    public enum BarcodeBeans
	{
		Code39(Code39Bean.class),
		Interleaved2Of5(Interleaved2Of5Bean.class),
		Codabar(CodabarBean.class),
		Upca(UPCABean.class),
		Ean13(EAN13Bean.class),
		Ean8(EAN8Bean.class),
		Upce(UPCEBean.class),
		Code128(Code128Bean.class),
		Postnet(POSTNETBean.class),
		Pdf417(PDF417Bean.class),
		DataMatrix(DataMatrixBean.class),
		Ean128(EAN128Bean.class);
		transient Class<?> klass;

		BarcodeBeans(final Class<?> klass)
		{
			this.klass = klass;
		}
	}

	protected BarcodeBeans barcodeType;
	protected Length barHeight;
	protected Length barWidth;
	protected int direction;
	protected boolean pdf147Binary;
	protected boolean totalPages;
	protected transient AbstractBarcodeBean barcodeBean;
	protected transient BarcodeDimension bardim; // cacheable

	public Barcode4jCell(TemplateModel containerGrid, JoriaAccess dataAxs)
	{
		super(containerGrid, dataAxs);
	}

	public Barcode4jCell(TemplateModel containerGrid)
	{
		super(containerGrid, null);
	}

	public Barcode4jCell(FreeCellDef from, TemplateModel containerGrid)
	{
		super(from, containerGrid);
		Barcode4jCell barcodeCell = ((Barcode4jCell) from);
		barcodeType = barcodeCell.barcodeType;
		pdf147Binary = barcodeCell.pdf147Binary;
		barHeight = barcodeCell.barHeight;
		direction = barcodeCell.direction;
		barWidth = barcodeCell.barWidth;
		totalPages = barcodeCell.totalPages;
	}

	protected void buildEditorDimensions(final boolean renew)
	{
		if (!renew && barcodeBean != null)
			return;		//System.out.println("BarcodeCell2.buildEditorDimensions " + renew + " " + bardim);
		setupBarcode();
		bardim = barcodeBean.calcDimensions(editorText);		//System.out.println("bardim.height = " + bardim.getHeight(direction) + " w=" + bardim.getWidth(direction));
	}

	protected void setupBarcode()
	{
		if (barcodeBean != null)
			return;
		try
		{
			barcodeBean = (AbstractBarcodeBean) type().klass.getDeclaredConstructor().newInstance();
		}
		catch (Throwable e)
		{
			throw new JoriaAssertionError("cannot instatiate barcode of type " + type().name(), e);
		}
		switch (barcodeType)
		{
			case Pdf417:
			{
				PDF417Bean b417 = (PDF417Bean) barcodeBean;
				b417.setQuietZone(0); // b.leftMarginCM = 0;
				b417.setColumns(2); // dmb.PDFColumns = 2;
				b417.setErrorCorrectionLevel(2); // dmb.PDFECLevel = 2;
				b417.setMinRows(3); // dmb.PDFRows = 0;				// //TODO test pdf147Binary				// // dmb.PDFMode = pdf147Binary ? BarCodePDF417Bean.PDF_BINARY : BarCodePDF417Bean.PDF_TEXT;
				break;
			}
			case Interleaved2Of5:
			{
				Interleaved2Of5Bean b2 = (Interleaved2Of5Bean) barcodeBean;
				b2.setChecksumMode(ChecksumMode.CP_IGNORE);
				break;
			}
			case Code39:
			{
				Code39Bean b2 = (Code39Bean) barcodeBean;
				b2.setChecksumMode(ChecksumMode.CP_IGNORE);
				break;
			}			// TODO set check sum mode of other beans that have it
			default:
			{
				barcodeBean.setQuietZone(0.01);
				break;
			}
		}
		barcodeBean.setModuleWidth(barWidth.inUnit(LengthUnit.MM));
		barcodeBean.setVerticalQuietZone(0); // b.topMarginCM = 0;
		barcodeBean.setBarHeight(barHeight.inUnit(LengthUnit.MM)); // b.barHeightCM = barHeight;
		CellStyle cs = getCascadedStyle();
		barcodeBean.setFontName(cs.getFont());
		barcodeBean.setFontSize(cs.getSize().getValInPoints());		//barcodeBean.setFontSize(FlexSize.inUnits(cs.getSize().getVal(), FlexSize.mm));		//b.textFont = cs.getStyledFont().deriveFont((float) (cs.getStyledFont().getSize2D() * scale));		//b.resolution = (int) ((92 / 2.54f) * scale);		//b.rotate = direction;
	}

	public void paint(Graphics2D g, float x0, float y0, float w, float h, Locale loc)
	{
		doPaint(g, x0, y0, w, h, editorText);
	}

	protected void doPaint(final Graphics2D g, final float x0, final float y0, final float w, final float h, final String text)
	{
		final AffineTransform transform = g.getTransform();
		try
		{
			buildEditorDimensions(false);
			final Java2DCanvasProvider painter = new Java2DCanvasProvider(g, direction);
			//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			//g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.setColor(Color.black);
			//b.code = txt;			//b.paint(pg);			//final int tw = getMinTargetWidth();			//double horzScale = tw / bardim.getWidth();
			g.translate(x0, y0);
			barcodeBean.generateBarcode(painter, text);  //now paint the barcode
		}
		catch (Throwable x)
		{
			x.printStackTrace();
			g.drawImage(Res.missingImageIcon.getImage(), (int) Math.round(x0 + 0.4), (int) Math.round(y0 + 0.4), (int) Math.round(w + 0.4), (int) Math.round(h + 0.4), null);
		}
		finally
		{
			g.setTransform(transform);
		}
	}

	protected void adjustSize(Locale loc, Graphics2D g)
	{
		super.adjustSize(loc, g);
		buildEditorDimensions(true);
		final double width = bardim.getWidth(direction) * Length.pointPerMm;
		final double height = bardim.getHeight(direction) * Length.pointPerMm;
		if (targetWidth == null || targetWidth.isExpandable())
		{
			myWidth += width;
			myHeight += height;
		}
		else
		{
			float twidth = targetWidth.getVal();
			myWidth += twidth;
			myHeight += height / width * twidth;
		}
	}

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new Barcode4jCell(this, newContainerGrid);
	}

	public float getMaxWidth(RVAny value, Locale loc, Graphics2D g)
	{
		if (value == null || value == RVSupressHeader.instance)
			return 0;
		if (targetWidth != null && !targetWidth.isExpandable())
			return targetWidth.getVal();
		setupBarcode();
		if (value instanceof RVBarcodeStringCol)
		{
			RVBarcodeStringCol rvBarcodeCol = (RVBarcodeStringCol) value;
			double width = 0;
			for (int i = 0; i < rvBarcodeCol.getSize(); i++)
			{
				final String s = rvBarcodeCol.get(i);
				if (s == null || s.length() == 0)
					continue;
				final BarcodeDimension barcodeDimension = barcodeBean.calcDimensions(s);
				width = Math.max(width, barcodeDimension.getWidth(direction));
			}
			return (float) width;
		}
		else if (value instanceof RVBarcodeString)
		{
			RVBarcodeString rvBarcodeString = (RVBarcodeString) value;
			final String s = rvBarcodeString.get(0);
			if (s == null || s.length() == 0)
				return 0;			
			final BarcodeDimension barcodeDimension = barcodeBean.calcDimensions(s);
			return (float) barcodeDimension.getWidth(direction);
		}
		else
			throw new JoriaAssertionError("Unhandled data value: " + value.getClass());
	}

	private BarcodeBeans type()
	{
		return barcodeType;
	}

	public String getDisplayMode()
	{
		return Res.str("Barcode");
	}

	public BarcodeBeans getBarcodeType()
	{
		return barcodeType;
	}

	public String getFormattedString(final DBData from, final AggregateCollector into) throws JoriaDataException
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
			return new RVBarcodeStringCol(length, this);
		}
		else
		{
			if (from != null)
			{
				if (!isVisible(outMode, from))
					return RVSupressHeader.instance;
				try
				{
					String textValue = getFormattedString(from, outMode.getRunEnv().getPager());
					if (textValue != null)
					{
						setupBarcode();
						bardim = barcodeBean.calcDimensions(textValue);
						return new RVBarcodeString(textValue, (float) bardim.getWidth(direction), this);
					}
				}
				catch (JoriaDataRetrievalExceptionInUserMethod e)
				{
					return new RVImage(Res.missingImageIcon, outMode.getRunEnv().getLocale(), false);
				}
			}
		}
		return null;
	}

	public void setType(BarcodeBeans newBarcodeType)
	{
		barcodeType = newBarcodeType;
		changed("barcode type changed");
	}

	protected void changed(final String reason)
	{
		getGrid().fireChange(reason);
		myHeight = Float.NaN;
		bardim = null;
		barcodeBean = null;
	}

	public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException
	{
		final boolean ret = out.makeGraphElFromRenderingCell(tblReq, iter);
		if (hasTotalPages())
			out.addTotalPages((GrahElPostprocess) tblReq.grel);
		return ret;
	}

	public void setAccessor(JoriaAccess accessor)
	{
		data = accessor;
		changed("barcode formula changed");
	}

	public float getBarHeightMM()
	{
		return barHeight.inUnit(LengthUnit.MM);
	}

	public Length getBarHeight()
	{
		return barHeight;
	}

	public void setBarHeight(Length barHeight)
	{
		this.barHeight = barHeight;
		changed("barcode height changed");
	}

	public int getDirection()
	{
		return direction;
	}

	public void setDirection(int direction)
	{
		this.direction = direction;
		changed("barcode direction changed");
	}

	public boolean isPdf147Binary()
	{
		return pdf147Binary;
	}

	public void setPdf147Binary(boolean pdf147Binary)
	{
		this.pdf147Binary = pdf147Binary;
		changed("barcode pd47binary changed");
	}

	public float getBarWidthMM()
	{
		return barWidth.inUnit(LengthUnit.MM);
	}

	public Length getBarWidth()
	{
		return barWidth;
	}

	public void setBarWidth(Length barWidth)
	{
		this.barWidth = barWidth;
		changed("barcode width changed");
	}

	public boolean hasTotalPages()
	{
		return totalPages;
	}

	public void postProcess(final GrahElPostprocess grel, final RunEnvImpl env, final int page, final int totalPages) throws JoriaDataException
	{
		JoriaDeferedQuery deferredQuery = ComputedField.getIfDeferredQuery(true, data);
		Trace.check(deferredQuery);
		try
		{
			if (!deferredQuery.isNeedsAllPages())
				throw new JoriaAssertionError("does not request totalpages builtin.");
				//DBData cv = valueGetter.getValue(from, valueGetter, out.getRunEnv());
				DBData cv = deferredQuery.getDeferredValue(env);
				if (cv == null || cv.isNull())
					grel.setText("", env.getGraphics2D());
				String into = env.getPager().format(cv, getCascadedStyle());
				String v = SimpleTextCellDef.wrapText(into, getCascadedStyle(), env.getLocale());
				grel.setText(v, env.getGraphics2D());
		}
		catch (JoriaDataException e)
		{
			Env.instance().handle(e);
		}
	}

	public void setTotalPages(final boolean totalPages)
	{
		this.totalPages = totalPages;
	}

	public float getRunHeight(final TableBorderRequirements b, final int iter, final float remainingHeight, final float contentWidth)
	{
		if (b.value == null)
			return 0;
		RValue rvBarcodeCol = (RValue) b.value;
		final String s = rvBarcodeCol.get(iter);
		if (s == null)
			return 0;
		setupBarcode();
		final BarcodeDimension barcodeDimension = barcodeBean.calcDimensions(s);
		return FlexSize.getPoints(FlexSize.mm, (float) barcodeDimension.getHeight(direction));
	}

	public void render(final TableBorderRequirements b, final FillPagedFrame out, final int iter, final float envelopeWidth)
	{

		JoriaDeferedQuery deferredQuery = ComputedField.getIfDeferredQuery(false, data);
		if (deferredQuery != null)
		{
			try
			{
				DBData cv = deferredQuery.getDeferredValue(out.getRunEnv());
				if (cv == null || cv.isNull())
					return;
				String into = out.getRunEnv().getPager().format(cv, getCascadedStyle());
				String v = SimpleTextCellDef.wrapText(into, getCascadedStyle(), out.getRunEnv().getLocale());
				b.value = new RVString(v, 1);
			}
			catch (JoriaDataException e)
			{
				Env.instance().handle(e);
				return;
			}
		}
		if (b.value == null)
			return;

		final String val = ((RValue) b.value).get(iter);
		if (val == null || val.length() == 0)
		{
			b.hContent = 0;
		}
		else
		{
			setupBarcode();
			final BarcodeDimension barcodeDimension = barcodeBean.calcDimensions(val);
			b.wContent =  FlexSize.getPoints(FlexSize.mm, (float) barcodeDimension.getWidth(direction));

			float x0 = out.getXPos() + b.contentX;
			float y0 = out.getYPos() + b.contentY;
			RenderedGraphic data = renderIntoMetaFile(out.getRunEnv().getGraphics2D(), val, x0, y0, b.wContent, b.hContent);
			ImageIcon backgroundImage = cascadedStyle.getBackgroundImage(out.getRunEnv().getLocale());
			GraphElContent ge = new GraphElMetaFile(data, getCascadedStyle().getBackground(), this, backgroundImage);
			b.grel = ge;
		}
	}

	public RenderedGraphic renderIntoMetaFile(final Graphics2D graphics2D, final String val, final float x0, final float y0, final float w, final float h)
	{
		if (val == null)
			return null;
		MetaFileGraphics2D mfg = new MetaFileGraphics2D(graphics2D);
		doPaint(mfg, x0, y0, w, h, val);
		mfg.close();
		mfg.dispose();
		RenderedGraphic data = mfg.getRenderedGraphic();
		return data;
	}

	public void outputToRTF(final RtfOutput rtfOutput, final OutputData data, final RVAny value, final float colWidth) throws JoriaDataException
	{
	}

	public void outputToHtml(final HtmlOutput2 htmlOutput2, final RunRangeBase rr, final RVAny value)
	{
	}

	protected Object readResolve()
	{
		if (targetWidth != null)
			targetWidth = null;
		return this;
	}
}