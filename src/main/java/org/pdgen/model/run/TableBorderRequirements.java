// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.JoriaSimpleBorder;
import org.pdgen.model.CoverInfo;
import org.pdgen.model.TemplateModel;

import javax.swing.*;

public class TableBorderRequirements
{
	// basic input values: unmodified
	public CellDef cd;// definition of the cell
	public RVAny value;
	public DrillDownLink drillDownObject;
	public FlexSize rowSizing;
	public int maxCol;// the max value for spans (limted by template/repater width)
	// working values in order of assignment
	// set by RunRangeBase/RunRepeater
	public float wDiff;// left & right padding and border insets
	public float hDiff;// top & botton padding and border insets
	public float contentX;// thickness of the left border
	public float contentY;// thickness of the top border
	public float contentWidth;// left and right inner padding
	public float innerX;// left padding
	public float innerY;// top padding
	public float topOutsideBorder;// space needed over the current row, if this cell would be on the first row on the current page
	public float bottomOutsideBorder;// space needed under the current row, if this cell would be on the last row on the current page
	// set by FillPagedFrame
	public GraphElContent grel;
	public float hContent;
	public float wContent;
	public float hBackground;
	public float hBorders;
	public float wBorders;
	public float topBorderInset;
	public float leftBorderInset;
	public JoriaSimpleBorder topBorder;// wird nur auf der ersten Zeile des Template oder der ersten Zeile der Seite ausgegeben.
	public JoriaSimpleBorder leftBorder;// wird nur am linken Rand des Templates ausgegeben.
	public JoriaSimpleBorder rightBorder;// wird immer ausgegeben
	public JoriaSimpleBorder bottomBorder;// wird immer ausgegeben.
	public boolean isTopOutsideBorder;// regelt den Inset der Top-Border
	public boolean isLeftOutsideBorder;// siehe oben
	public boolean isRightOutsideBorder;// siehe oben
	public boolean isBottomOutsideBorder;// siehe oben
	public boolean paintTopBorder;// Steuert die Ausgabe
	public boolean paintLeftBorder;// Steuert die Ausgabe
	public JoriaSimpleBorder spanBottomBorder;// bei vspan Zellen wird noch die unter Border des Spans mit gespeichert.
	public JoriaSimpleBorder bottomBorderForTableAtPageBreak;// if table wants to repeat the border at page break, we need to have it on hand
	public JoriaSimpleBorder leftBorderForTableAtPageBreak;// if table wants to repeat the border at page break, we need to have it on hand to calculate the horizontal offset
	public JoriaSimpleBorder rightBorderForTableAtPageBreak;// if table wants to repeat the border at page break, we need to have it on hand to calculate the horizontal offset
	// Dann kann wenn der Seitenrand den Span unterbricht eine Linie gezeichnet werden.
	// fields that deal with vertical spanning
	// TODO was brauchen ich noch für den vspan?
	public int vSpanCount;//Letzte Zeile in der noch Span ist. Bei jeder nicht repeated row wird eins begezogen.
	// Wenn 0 ist, wird das Grel ausgerichtet.
	public float vSpanHeight;// Damit kann icht den nötige Höhe für die letzte Zeile berechnen. Bei jeder Zeile wird die Höhe
	// abgezogen.
	public GraphElContent vSpanContent;// zum Ausrichten nach der letzten Zeile.
	public float vSpanStart;// zum Ausrichten nach der letzten Zeile
	public float vSpanAlign;// zum Ausrichten nach der letzten Zeile
	public boolean vSpanBreakble;// um einen Save-Point löschen zu können

	public void reset()
	{
		hContent = wContent = 0;
		hDiff = 0;
		grel = null;
		hBorders = 0;
	}

	protected void buildTableBorderRequirement(int row, int col, CellStyle cs, CellDef cellDef, JoriaSimpleBorder ct, JoriaSimpleBorder cl, JoriaSimpleBorder cr, JoriaSimpleBorder cb, boolean firstRowOnFrame, final boolean wantsTableBorderatPageBreak, RunRangeBase rrb)
	{
		buildTableBorderrequirementWithoutData(row, col, cs, cellDef, ct, cl, cr, cb, firstRowOnFrame, wantsTableBorderatPageBreak, rrb);
		if (rrb.values != null)
			value = rrb.values.subs[row][rrb.inDataCol];
	}

	protected void buildTableBorderrequirementWithoutData(final int row, final int col, CellStyle cs, CellDef cellDef, JoriaSimpleBorder ct, JoriaSimpleBorder cl, JoriaSimpleBorder cr, JoriaSimpleBorder cb, final boolean firstRowOnFrame, final boolean wantsTableBorderatPageBreak, final RunRangeBase rrb)
	{
		final TemplateModel template = rrb.out.getTemplate();
		final int templateRow = row + rrb.getRowOffset();
		final int templateCol = col + rrb.getColOffset();
		if (ct == null)
			ct = template.getBorderAt(templateRow, templateCol, true, true);
		if (cl == null)
			cl = template.getBorderAt(templateRow, templateCol, false, true);
		if (cr == null)
			cr = template.getBorderAt(templateRow, templateCol, false, false);
		if (cb == null)
			cb = template.getBorderAt(templateRow, templateCol, true, false);
		CoverInfo cover = template.getCoverInfo(templateRow, templateCol);
		boolean covered = cover != null;
		if (cover != null || cs != null && (cs.getSpanHorizontal() > 1 || cs.getSpanVertical() > 1))
		{
			if (cover == null)
				cover = new CoverInfo(cellDef, templateRow, templateCol);
			else
				cellDef = null;
			cs = cover.cell.getCascadedStyle();
			if (templateRow != cover.row)
				ct = JoriaSimpleBorder.NULL;
			if (templateCol != cover.col)
				cl = JoriaSimpleBorder.NULL;
			if (templateRow != cover.row + cs.getSpanVertical() - 1)
				cb = JoriaSimpleBorder.NULL;
			if (templateCol != cover.col + cs.getSpanHorizontal() - 1)
				cr = JoriaSimpleBorder.NULL;
		}
		hBackground = 0;
		hBorders = 0;
		topBorderInset = 0;
		leftBorderInset = 0;
		wBorders = 0;
		isTopOutsideBorder = true;//template.isFirstRow(row + getRowOffset()) && !isNotFirstRow();
		float topThickness = ct.getThickness() / (isTopOutsideBorder ? 1 : 2);
		isLeftOutsideBorder = template.isFirstCol(templateCol);
		float leftThickness = cl.getThickness() / (isLeftOutsideBorder ? 1 : 2);
		isRightOutsideBorder = rrb.isLastCol(col);
		float rightThickness = cr.getThickness() / (isRightOutsideBorder ? 1 : 2);
		isBottomOutsideBorder = template.isLastRow(templateRow) && !rrb.isLastRow();
		float bottomThickness = cb.getThickness() / (isBottomOutsideBorder ? 1 : 2);
		float topPadding = 0;
		float leftPadding = 0;
		float bottomPadding = 0;
		float rightPadding = 0;
		if (cs != null && !covered)
		{
			topPadding = cs.getTopPadding().getValInPoints();
			leftPadding = cs.getLeftPadding().getValInPoints();
			rightPadding = cs.getRightPadding().getValInPoints();
			bottomPadding = cs.getBottomPadding().getValInPoints();
			if (cs.getBackgroundImageName() != null)
			{
				hBorders = topThickness + bottomThickness;
				wBorders = leftThickness + rightThickness;
				leftBorderInset = leftThickness;
				topBorderInset = topThickness;
				if (cs.getBackgroundImageTargetWidth() != null && !cs.getBackgroundImageTargetWidth().isExpandable())
				{
					ImageIcon backgroundImage = cs.getBackgroundImage(rrb.out.getRunEnv().getLocale());
					hBackground = backgroundImage.getIconHeight() / backgroundImage.getIconWidth() * cs.getBackgroundImageTargetWidth().getVal();
				}
				else
				{
					hBackground = cs.getBackgroundImage(rrb.out.getRunEnv().getLocale()).getIconHeight();
				}
			}
		}
		float topInset = topThickness + topPadding;
		float leftInset = leftThickness + leftPadding;
		float rightInset = rightThickness + rightPadding;
		float bottomInset = bottomThickness + bottomPadding;
		paintTopBorder = row == 0 && rrb.lastRowWasRepeated || firstRowOnFrame;
		paintLeftBorder = col == 0;
		topBorder = ct;
		leftBorder = cl;
		rightBorder = cr;
		bottomBorder = cb;
		if (wantsTableBorderatPageBreak)
		{
			leftBorderForTableAtPageBreak = template.getBorderAt(templateRow, templateCol, false, true);
			rightBorderForTableAtPageBreak = template.getBorderAt(templateRow, templateCol, false, false);
			bottomBorderForTableAtPageBreak = template.getBorderAt(templateRow, templateCol, true, false);
		}
		else
			bottomBorderForTableAtPageBreak = null;// indicates, that this column does not want table borders at page break
		// left
		wDiff = leftInset + rightInset;
		topOutsideBorder = isTopOutsideBorder ? 0f : ct.getThickness() / 2;
		bottomOutsideBorder = isBottomOutsideBorder ? 0f : cb.getThickness() / 2;
		// top
		//bottom
		hDiff = topInset + bottomInset;
		// this is the extra space needed to place the table bottom border if the row is at the end of a page
		cd = cellDef;
		rowSizing = template.getRowSizingAt(templateRow);
		contentX = leftThickness;// x position of painted rect
		contentY = topThickness;
		if (cs != null)
		{
			contentWidth = cs.getLeftRightPaddingValue();
		}
		else
		{
			contentWidth = 0;
		}
		innerX = leftPadding + leftThickness;
		innerY = topPadding + topThickness;
	}
}
