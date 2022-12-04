// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.*;
import org.pdgen.model.cells.*;
import org.pdgen.model.run.html.HtmlChildDocumentHandler;
import org.pdgen.model.style.CellStyle;
import org.pdgen.styledtext.model.StyleRunIterator;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.styledtext.model.StyledParagraphLayouter;
import org.pdgen.styledtext.model.StyledParagraphList;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.env.JoriaException;
import org.pdgen.env.Res;


import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * User: patrick
 * Date: Jul 11, 2005
 * Time: 11:43:06 AM
 */
public class HtmlOutput2 extends NonPagedOutput
{
	protected boolean xhtml;
	protected boolean includeHeaderFooter;
	protected HtmlChildDocumentHandler docHandler;
	protected PrintWriter contentWriter;
	protected Map<CellDef, String> styleMap;
	protected PrintWriter stylesWriter;
	protected Properties styledTextStyleMap = new Properties();

	public static void writeHtml(boolean xhtml, boolean includeHeaderFooter, RunEnvImpl env, HtmlChildDocumentHandler docHandler) throws JoriaException
	{
		HtmlOutput2 out = new HtmlOutput2(xhtml, includeHeaderFooter, env, docHandler);
		out.doOutput();
	}

	protected HtmlOutput2(boolean xhtml, boolean includeHeaderFooter, RunEnvImpl env, HtmlChildDocumentHandler docHandler)
	{
		super(env);
		this.xhtml = xhtml;
		this.includeHeaderFooter = includeHeaderFooter;
		this.docHandler = docHandler;
	}

	protected boolean addNullCellInt(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat)
	{
		if (out.template.getCoverCell(row, col) != null)// gespante Zellen werden nicht nach html ausgegeben.
			return false;
		if (xhtml)
			contentWriter.print("<td/>");
		else
			contentWriter.print("<td></td>");
		out.colInCurrRow++;
		return false;
	}

	protected void addSpanInt(int count)
	{
	}

	protected void generateOutputInt(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException
	{
		if (rr.values == null)
		{
			out.colInCurrRow++;
			Trace.logWarn("generateOutput no values");
			return;
		}
		contentWriter.print("<td");
		CellStyle cs = cd.getCascadedStyle();
		int colspan = Math.min(cs.getSpanHorizontal().intValue(), cd.getGrid().getColCount());
		int rowspan = cs.getSpanVertical();
		if (colspan > 1)
		{
			contentWriter.print(" colspan=\"");
			contentWriter.print(colspan);
			out.colInCurrRow += colspan - 1;
			contentWriter.print("\"");
		}
		if (rowspan > 1)
		{
			contentWriter.print(" rowspan=\"");
			contentWriter.print(rowspan);
			contentWriter.print("\"");
		}
		DrillDownLink ddl = rr.values.getDrillDownKey(rr.getIteration());
		String drilldownUrl = null;
		String styleClass = styleMap.get(cd);
		if (styleClass != null)
		{
			contentWriter.print(" class=\"");
			contentWriter.print(styleClass);
			contentWriter.print("\"");
		}
		contentWriter.print(">");
		if (drilldownUrl != null)
			contentWriter.print("<a href=\"" + drilldownUrl + "\">");
		RVAny value = rr.values.subs[row][c];
		if (value instanceof RVImage)
		{
			RVImage ri = (RVImage) value;
			String fileName = ri.getPictureFileName();
			String url = null;
			try
			{
				if (fileName != null)
					url = docHandler.mapFileNameToUrl(fileName);
				if (url == null)
				{
					byte[] pictureData;
					ImageDetection.ImageClass ic;
					if (ri.getPictureData() != null)
					{
						pictureData = ri.getPictureData();
						ic = ImageDetection.detectImageClass(ri.getPictureData());
						if (ic == null)
						{
							ImageDetection.ImageHolder ih = ImageDetection.recodeImage(pictureData);
							pictureData = ih.getImageData();
							ic = ih.getImageClass();
						}
					}
					else
					{
						ImageDetection.ImageHolder ih = ImageDetection.recodeIcon(ri.getPicture());
						pictureData = ih.getImageData();
						ic = ih.getImageClass();
					}
					HtmlChildDocumentHandler.StreamData docStream;
					docStream = docHandler.getDocumentStream(ic);
					OutputStream outStream = docStream.getStream();
					outStream.write(pictureData);
					outStream.close();
					url = docStream.getUrl();
				}
				contentWriter.print("<img src=\"");
				contentWriter.print(url);
				if (((PictureCellBase) cd).getTargetWidth() != null && !((PictureCellBase) cd).getTargetWidth().isExpandable())
				{
					contentWriter.print("\" width=\"" + ((PictureCellBase) cd).getTargetWidth().getVal());
				}
				if (xhtml)
					contentWriter.print("\"/>");
				else
					contentWriter.print("\">");
			}
			catch (IOException e)
			{
				contentWriter.print(Res.str("A_problem_displaying_the_image_exists"));
			}
		}
		else if (cd instanceof RenderingCellDef)
		{
			((RenderingCellDef)cd).outputToHtml(this, rr, value);
		}
		else if (value instanceof RVImageCol)
		{
			int iter = rr.getIteration();
			RVImageCol ric = (RVImageCol) value;
			Object storedData = ric.storedData[iter];
			Icon image = ric.images[iter];
			String url = null;
			try
			{
				if (storedData instanceof String)
					url = docHandler.mapFileNameToUrl((String) storedData);
				if (url == null)
				{
					byte[] pictureData;
					ImageDetection.ImageClass ic;
					if (storedData instanceof byte[])
					{
						pictureData = (byte[]) storedData;
						ic = ImageDetection.detectImageClass(pictureData);
						if (ic == null)
						{
							ImageDetection.ImageHolder ih = ImageDetection.recodeImage(pictureData);
							if (ih == null)
							{
								ih = ImageDetection.recodeIcon(image);
								pictureData = ih.getImageData();
								ic = ih.getImageClass();
							}
							else
							{
								pictureData = ih.getImageData();
								ic = ih.getImageClass();
							}
						}
					}
					else if (image != null)
					{
						ImageDetection.ImageHolder ih = ImageDetection.recodeIcon(image);
						pictureData = ih.getImageData();
						ic = ih.getImageClass();
					}
					else
					{
						return;
					}
					HtmlChildDocumentHandler.StreamData docStream;
					docStream = docHandler.getDocumentStream(ic);
					OutputStream outStream = docStream.getStream();
					outStream.write(pictureData);
					outStream.close();
					url = docStream.getUrl();
				}
				contentWriter.print("<img src=\"");
				contentWriter.print(url);
				if (((PictureCellBase) cd).getTargetWidth() != null && !((PictureCellBase) cd).getTargetWidth().isExpandable())
				{
					contentWriter.print("\" width=\"" + ((PictureCellBase) cd).getTargetWidth().getVal());
				}
				if (xhtml)
					contentWriter.print("\"/>");
				else
					contentWriter.print("\">");
			}
			catch (IOException e)
			{
				contentWriter.print(Res.str("A_problem_displaying_the_image_exists"));
			}
		}
		else if (value instanceof RVStyledText || value instanceof RVStyledTextCol)
		{
			StyledParagraphList list;
			if (value instanceof RVStyledText)
			{
				list = ((RVStyledText) value).getText();
			}
			else
			{
				list = ((RVStyledTextCol) value).getText(rr.getIteration());
			}
			StringBuffer styleData = new StringBuffer(100);
			for (int i = 0; i < list.length(); i++)
			{
				if (i != 0)
				{
					if (xhtml)
						contentWriter.println("<br/>");
					else
						contentWriter.println("<br>");
				}
				StyledParagraph paragraph = list.get(i);
				StyledParagraphLayouter layouter = new StyledParagraphLayouter(paragraph);
				layouter.recalc(0, new FontRenderContext(null, true, false), 2000000, 0);
				StyleRunIterator iterator = new StyleRunIterator(layouter);
				while (iterator.nextRun())
				{
					styleData.setLength(0);
					styleData.append("{font-family: ");
					styleData.append(iterator.getFontFamily());
					styleData.append("; font-size: ");
					styleData.append(iterator.getSize());
					styleData.append("pt; font-style: ");
					styleData.append(iterator.isItalic() ? "italic" : "normal");
					styleData.append("; font-weight: ");
					styleData.append(iterator.isBold() ? "bold" : "normal");
					styleData.append("; text-decoration: ");
					styleData.append(iterator.isUnderlined() ? "underline" : "none");
					styleData.append("; }");
					String data = styleData.toString();
					String name = styledTextStyleMap.getProperty(data);
					if (name == null)
					{
						name = "t" + (styledTextStyleMap.size() + 1);
						styledTextStyleMap.setProperty(data, name);
						stylesWriter.println("." + name + " " + data);
					}
					contentWriter.print("<span class=\"");
					contentWriter.print(name);
					contentWriter.print("\">");
					StringBuffer escaped = new StringBuffer();
					addEscapedString(iterator.getText(), escaped);
					contentWriter.print(escaped);
					contentWriter.println("</span>");
				}
			}
		}
		else if (value instanceof RValue)
		{
			RValue val = (RValue) value;
			if (val instanceof RVStringCol)
			{
				((RVStringCol) val).buildFormattedStrings(cd, aggs.getRunEnv().getLocale());
			}
			if (cd instanceof DataCellDef && ((DataCellDef) cd).getAggregates() != null)
				val.accumulate(aggs, ((DataCellDef) cd).getAggregates(), rr.getIteration());
			String fs = val.get(rr.getIteration());
			if (cd instanceof SummaryCell)
				fs = cd.getFormattedString(null, aggs);
			//System.out.println(fs);
			if (fs == null)
			{
				// nothing to output here
			}
			else if (BasicHTML.isHTMLString(fs))
			{
				fs = fs.substring(fs.indexOf('>') + 1);
				contentWriter.print(fs);
			}
			else if (cd.getCascadedStyle().getTextType().equals(CellStyle.htmlType))
			{
				String upcase = fs.toUpperCase(aggs.getRunEnv().getLocale());
				int index = upcase.indexOf("<HTML");
				if (index != -1)
				{
					index = upcase.indexOf(">", index);
					fs = fs.substring(index + 1);
					upcase = upcase.substring(index + 1);
				}
				index = upcase.indexOf("<BODY");
				if (index != -1)
				{
					index = upcase.indexOf(">", index);
					fs = fs.substring(index + 1);
					upcase = upcase.substring(index + 1);
				}
				index = upcase.indexOf("</BODY>");
				if (index != -1)
				{
					fs = fs.substring(0, index);
					upcase = upcase.substring(0, index);
				}
				index = upcase.indexOf("</HTML>");
				if (index != -1)
				{
					fs = fs.substring(0, index);
					//upcase = upcase.substring(0, index);
				}
				contentWriter.print(fs);
			}
			else if (cd.getCascadedStyle().getTextType().equals(CellStyle.rtfType))
			{
				try
				{
					HtmlChildDocumentHandler.StreamData docStream = docHandler.getDocumentStream(ImageDetection.PNG);
					//                    .createPngImage(docStream.getStream(), null);
					contentWriter.print("<img src=\"");
					contentWriter.print(docStream.getUrl());
					if (xhtml)
						contentWriter.print("\"/>");
					else
						contentWriter.print("\">");
				}
				catch (IOException e)
				{
					contentWriter.print(Res.str("A_problem_displaying_a_rtf_text_exists"));
				}
			}
			else
			{
				StringBuffer escaped = new StringBuffer();
				StringTokenizer st = new StringTokenizer(fs, "\n\r\f");
				boolean one = false;
				while (st.hasMoreTokens())
				{
					String t = st.nextToken();
					if (one)
					{
						if (xhtml)
							escaped.append("<br/>");
						else
							escaped.append("<br>");
					}
					else
						one = true;
					addEscapedString(t, escaped);
				}
				contentWriter.print(escaped);
			}
		}
		if (drilldownUrl != null)
			contentWriter.print("</a>");
		contentWriter.print("</td>");
		out.colInCurrRow++;
	}


	protected void addEscapedString(String s, StringBuffer b)
	{
		int l = s.length();
		int i = 0;
		while (i < l)
		{
			char c = s.charAt(i);
			if (c == '\"')
				b.append("&quot;");
			else if (c == '<')
				b.append("&lt;");
			else if (c == '>')
				b.append("&gt;");
			else if (c == '&')
				b.append("&amp;");
				//			else if (c == '\'')
				//				b.append("&apos;");
			else if (c >= 127 && c < 256)
				b.append("&#").append(getF3().format(c)).append(";");
			else if (c > 255)
				b.append("&#").append(getF4().format(c)).append(";");
			else
				b.append(c);
			i++;
		}
	}

	protected void processOneFrame(RDBase[][] fields, RVTemplate rvt, TemplateModel template)
	{
		// not needed here
	}

	protected void preprocess()
	{
		Properties styleDataMap = new Properties();
		docHandler.setTitle(env.getTemplate().getName());
		styleMap = buildStyleMap(env.getTemplate(), includeHeaderFooter, styleDataMap);
		stylesWriter = docHandler.getStyleWriter();
		for (Map.Entry<Object, Object> entry : styleDataMap.entrySet())
		{
			String data = (String) entry.getKey();
			String name = (String) entry.getValue();
			stylesWriter.println("." + name + " " + data);
		}
		contentWriter = docHandler.getContentWriter();
	}

	protected void postprocess()
	{
		stylesWriter.close();
		contentWriter.flush();
	}

	protected boolean includeHeaderFooterFrames()
	{
		return includeHeaderFooter;
	}

	protected void startOneFrame(int frameType)
	{
		contentWriter.println("<tr><td><table border=\"1\" width=\"100%\" style=\"border-style:solid; border-collapse: collapse\">");
	}

	protected void endOneFrame()
	{
		contentWriter.println("</table></td></tr>");
	}

	protected void startOneRow()
	{
		contentWriter.println("<tr>");
	}

	protected void endOneRow()
	{
		contentWriter.println("</tr>");
	}

	protected boolean includeAllHeaderAndFooterFrames()
	{
		return false;
	}

	private Map<CellDef, String> buildStyleMap(Template template, boolean includeHeaderFooter, Properties styleDataMap)
	{
		HashMap<CellDef, String> ret = new HashMap<CellDef, String>();
		PageMaster p = template.getPage();
		if (includeHeaderFooter)
		{
			PageLevelBox box = p.getFirstPageHeader();
			if (box != null)
			{
				addStyleMap(box, ret, styleDataMap);
			}
		}
		ArrayList<PageLevelBox> l = p.getFrames();
		for (PageLevelBox aL : l)
		{
			addStyleMap(aL, ret, styleDataMap);
		}
		if (includeHeaderFooter)
		{
			PageLevelBox box = p.getLastPageFooter();
			if (box != null)
			{
				addStyleMap(box, ret, styleDataMap);
			}
		}
		return ret;
	}

	private void addStyleMap(PageLevelBox box, HashMap<CellDef, String> ret, Properties styleDataMap)
	{
		TemplateModel model = box.getTemplate();
		int rows = model.getRowCount();
		int cols = model.getColCount();
		StringBuffer styleData = new StringBuffer();
		for (int ri = 0; ri < rows; ri++)
		{
			for (int ci = 0; ci < cols; ci++)
			{
				CellDef cd = model.cellAt(ri, ci);
				if (cd != null)
				{
					CellStyle cs = cd.getCascadedStyle();
					styleData.setLength(0);
					styleData.append("{text-align: ");
					if (cs.getAlignmentHorizontal().isBegin())
						styleData.append("left");
					else if (cs.getAlignmentHorizontal().isMid())
						styleData.append("center");
					else if (cs.getAlignmentHorizontal().isEnd())
						styleData.append("right");
					styleData.append("; vertical-align: ");
					if (cs.getAlignmentVertical().isBegin())
						styleData.append("text-top");
					else if (cs.getAlignmentVertical().isMid())
						styleData.append("midlle");
					else if (cs.getAlignmentVertical().isEnd())
						styleData.append("text-bottom");
					styleData.append("; background-color: rgb(");
					styleData.append(cs.getBackground().getRed());
					styleData.append(",");
					styleData.append(cs.getBackground().getGreen());
					styleData.append(",");
					styleData.append(cs.getBackground().getBlue());
					styleData.append("); color: rgb(");
					styleData.append(cs.getForeground().getRed());
					styleData.append(",");
					styleData.append(cs.getForeground().getGreen());
					styleData.append(",");
					styleData.append(cs.getForeground().getBlue());
					styleData.append("); margin-left: ");
					styleData.append(cs.getLeftPadding().getValInPoints());
					styleData.append("pt; margin-right: ");
					styleData.append(cs.getRightPadding().getValInPoints());
					styleData.append("pt; margin-top: ");
					styleData.append(cs.getTopPadding().getValInPoints());
					styleData.append("pt; margin-bottom: ");
					styleData.append(cs.getBottomPadding().getValInPoints());
					styleData.append("pt; font-family: ");
					styleData.append(cs.getFont());
					styleData.append("; font-size: ");
					styleData.append(cs.getSize().inUnits());
					styleData.append("pt; font-style: ");
					styleData.append(cs.getItalic().booleanValue() ? "italic" : "normal");
					styleData.append("; font-weight: ");
					styleData.append(cs.getBold().booleanValue() ? "bold" : "normal");
					styleData.append("; text-decoration: ");
					styleData.append(cs.getUnderlined().booleanValue() ? "underline" : "none");
					// TODO weitere attribute hier
					styleData.append("; }");
					String data = styleData.toString();
					String name = styleDataMap.getProperty(data);
					if (name == null)
					{
						name = "s" + (styleDataMap.size() + 1);
						styleDataMap.setProperty(data, name);
					}
					ret.put(cd, name);
				}
			}
		}
	}

	private static final Formates formates = new Formates();

	public static class Formates extends ThreadLocal<NumberFormat[]>
	{
		protected NumberFormat[] initialValue()
		{
			return new NumberFormat[]{new DecimalFormat("000"), new DecimalFormat("0000")};
		}
	}

	public static NumberFormat getF3()
	{
		return formates.get()[0];
	}

	public static NumberFormat getF4()
	{
		return formates.get()[1];
	}

	public HtmlChildDocumentHandler getDocHandler()
	{
		return docHandler;
	}

	public PrintWriter getContentWriter()
	{
		return contentWriter;
	}

	public PrintWriter getStylesWriter()
	{
		return stylesWriter;
	}

	public Properties getStyledTextStyleMap()
	{
		return styledTextStyleMap;
	}

	public Map<CellDef, String> getStyleMap()
	{
		return styleMap;
	}

	public boolean isXhtml()
	{
		return xhtml;
	}
}
