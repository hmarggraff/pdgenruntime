// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.SortedNamedVector;
import org.pdgen.data.Trace;
import org.pdgen.env.Env;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: May 28, 2003
 * Time: 10:59:53 AM
 * To change this template use Options | File Templates.
 */
public class PredefinedStyles
{
	public static String defaultTableHeaderStyleName = "*TableHeader";//trdone
	public static String defaultTitleStyleName = "*Title";//trdone
	public static String defaultProblemStyleName = "*Problem";//trdone
	public static String defaultNumberStyleName = "*Number";//trdone
	public static String defaultTotalStyleName = "*Total";//trdone
	public static String defaultTotalNumberStyleName = "*TotalNumber";//trdone
	public static String defaultGroupKeyStyleName = "*GroupKey";//trdone
	public static String defaultCrosstabHorizontalHeaderStyleName = "*CrosstabHorizontalHeader";// trdone
	public static String defaultCrosstabVerticalHeaderStyleName = "*CrosstabVerticalHeader";// trdone
	public CellStyle theCellStyleDefaultTableHeaderStyle;
	public CellStyle theCellStyleDefaultTitleStyle;
	public CellStyle theCellStyleDefaultProblemStyle;
	public CellStyle theCellStyleDefaultNumberStyle;
	public CellStyle theCellStyleDefaultTotalStyle;
	public CellStyle theCellStyleDefaultTotalNumberStyle;
	public CellStyle theCellStyleDefaultGroupKeyStyle;
    public CellStyle theCellStyleDefaultCrosstabHorizonalHeaderStyle;
    public CellStyle theCellStyleDefaultCrosstabVerticalHeaderStyle;
    public FrameStyle theFrameStyleDefaultBodyStyle;
	public FrameStyle theFrameStyleDefaultHeaderStyle;
	public FrameStyle theFrameStyleDefaultFooterStyle;
	public FrameStyle theFrameStyleDefaultNestedStyle;
	public PageStyle thePageStyleDefaultPageStyle;
	public PageStyle thePageStyleA5P;
	public PageStyle thePageStyleLetterP;
	public PageStyle thePageStyleA4P;
	public PageStyle thePageStyleA5L;
	public PageStyle thePageStyleLetterL;
	public PageStyle thePageStyleA4L;
	public PaperSize thePaperSizeA4;
	public PaperSize thePaperSizeA3;
	public PaperSize thePaperSizeLetter;
	public PaperSize thePaperSizeA5;
	public TableStyle theTableStyleDefault;

	static PredefinedStyles theInstance = new PredefinedStyles();
	public static PredefinedStyles instance() {
		return theInstance;
	}

	public static void init(SortedNamedVector<CellStyle> cellStyles)
	{
		Trace.logDebug(Trace.init, "Init Cell Styles");
		PredefinedStyles stat = theInstance;
		stat.theCellStyleDefaultTableHeaderStyle = new CellStyle(defaultTableHeaderStyleName);
		stat.theCellStyleDefaultProblemStyle = new CellStyle(defaultProblemStyleName);
		stat.theCellStyleDefaultTitleStyle = new CellStyle(defaultTitleStyleName);
		stat.theCellStyleDefaultNumberStyle = new CellStyle(defaultNumberStyleName);
		stat.theCellStyleDefaultTotalStyle = new CellStyle(defaultTotalStyleName);
		stat.theCellStyleDefaultTotalNumberStyle = new CellStyle(defaultTotalNumberStyleName);
		stat.theCellStyleDefaultGroupKeyStyle = new CellStyle(defaultGroupKeyStyleName);
		//
		stat.theCellStyleDefaultTableHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
		stat.theCellStyleDefaultTableHeaderStyle.setBold(Boolean.TRUE);
		//
		stat.theCellStyleDefaultTitleStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
		stat.theCellStyleDefaultTitleStyle.setBold(Boolean.TRUE);
		stat.theCellStyleDefaultTitleStyle.setSize(new PointSize(14));
		stat.theCellStyleDefaultTitleStyle.setSpanHorizontal(Integer.MAX_VALUE / 2);
		//
		stat.theCellStyleDefaultProblemStyle.setBackground(Color.pink);
		//
		stat.theCellStyleDefaultNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
		//
		stat.theCellStyleDefaultTotalNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
		stat.theCellStyleDefaultTotalNumberStyle.setItalic(Boolean.TRUE);
		//
		stat.theCellStyleDefaultTotalStyle.setItalic(Boolean.TRUE);
		//
		stat.theCellStyleDefaultGroupKeyStyle.setItalic(Boolean.TRUE);
		stat.theCellStyleDefaultGroupKeyStyle.setBold(Boolean.TRUE);
		//
		cellStyles.add(stat.theCellStyleDefaultTitleStyle);
		cellStyles.add(stat.theCellStyleDefaultTableHeaderStyle);
		cellStyles.add(stat.theCellStyleDefaultProblemStyle);
		cellStyles.add(stat.theCellStyleDefaultNumberStyle);
		cellStyles.add(stat.theCellStyleDefaultTotalStyle);
		cellStyles.add(stat.theCellStyleDefaultTotalNumberStyle);
		cellStyles.add(stat.theCellStyleDefaultGroupKeyStyle);
	}

	public static void initCrosstab()
	{
		Trace.logDebug(Trace.init, "Init Cell Styles");
		PredefinedStyles stat = theInstance;
		SortedNamedVector<CellStyle> cellStyles = Env.instance().repo().cellStyles;
		if (cellStyles.find(defaultCrosstabHorizontalHeaderStyleName) == null)
		{
			stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle = new CellStyle(defaultCrosstabHorizontalHeaderStyleName);
			stat.theCellStyleDefaultCrosstabVerticalHeaderStyle = new CellStyle(defaultCrosstabVerticalHeaderStyleName);
			//
			stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
			stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle.setBold(Boolean.TRUE);
			//
			stat.theCellStyleDefaultCrosstabVerticalHeaderStyle.setAlignmentHorizontal(HorizontalAlignment.CENTER);
			stat.theCellStyleDefaultCrosstabVerticalHeaderStyle.setBold(Boolean.TRUE);
			//
			cellStyles.add(stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle);
			cellStyles.add(stat.theCellStyleDefaultCrosstabVerticalHeaderStyle);
		}
		if (cellStyles.find(defaultTotalNumberStyleName) == null)
		{
			stat.theCellStyleDefaultTotalStyle = new CellStyle(defaultTotalStyleName);
			stat.theCellStyleDefaultTotalNumberStyle = new CellStyle(defaultTotalNumberStyleName);
			stat.theCellStyleDefaultGroupKeyStyle = new CellStyle(defaultGroupKeyStyleName);
			//
			stat.theCellStyleDefaultTotalNumberStyle.setAlignmentHorizontal(HorizontalAlignment.RIGHT);
			stat.theCellStyleDefaultTotalNumberStyle.setItalic(Boolean.TRUE);
			//
			stat.theCellStyleDefaultTotalStyle.setItalic(Boolean.TRUE);
			stat.theCellStyleDefaultGroupKeyStyle.setItalic(Boolean.TRUE);
			stat.theCellStyleDefaultGroupKeyStyle.setBold(Boolean.TRUE);
			cellStyles.add(stat.theCellStyleDefaultTotalStyle);
			cellStyles.add(stat.theCellStyleDefaultTotalNumberStyle);
			cellStyles.add(stat.theCellStyleDefaultGroupKeyStyle);
		}
	}

	public void loadCellStyles()
	{
		SortedNamedVector<CellStyle> cellStyles = Env.instance().repo().cellStyles;
		if (cellStyles.find(defaultTableHeaderStyleName) != null) {
			theCellStyleDefaultTableHeaderStyle = cellStyles.find(defaultTableHeaderStyleName);
		}
		/*
		ModelStaticStorage stat = ModelStaticStorage.getActive();
		if (StyleBase.eq(name, PredefinedStyles.defaultTableHeaderStyleName))
			stat.theCellStyleDefaultTableHeaderStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultTitleStyleName))
			stat.theCellStyleDefaultTitleStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultNumberStyleName))
			stat.theCellStyleDefaultNumberStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultProblemStyleName))
			stat.theCellStyleDefaultProblemStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultTotalNumberStyleName))
			stat.theCellStyleDefaultTotalNumberStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultTotalStyleName))
			stat.theCellStyleDefaultProblemStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultGroupKeyStyleName))
			stat.theCellStyleDefaultGroupKeyStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultCrosstabHorizontalHeaderStyleName))
			stat.theCellStyleDefaultCrosstabHorizonalHeaderStyle = this;
		else if (StyleBase.eq(name, PredefinedStyles.defaultCrosstabVerticalHeaderStyleName))
			stat.theCellStyleDefaultCrosstabVerticalHeaderStyle = this;

		 */
	}

	protected void loadFrameStyles()
	{
		/*
		PredefinedStyles stat = PredefinedStyles.instance();
		if (StyleBase.eq(name, bodyStyleName))
			stat.theFrameStyleDefaultBodyStyle = this;
		else if (StyleBase.eq(name, headerStyleName))
			stat.theFrameStyleDefaultHeaderStyle = this;
		else if (StyleBase.eq(name, footerStyleName))
			stat.theFrameStyleDefaultFooterStyle = this;
		else if (StyleBase.eq(name, nestedStyleName))
			stat.theFrameStyleDefaultNestedStyle = this;
		 */
	}



}
