// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.style.FrameStyle;
import org.pdgen.util.BucketListOfDoubles;
import org.pdgen.model.run.OutputMode.LastRowState;

// Created 22.11.2007 by hmf
/**
 * deals with multiple flowing columns at the frame level. (i.e. newspaper style columns, as opposed to the grid columns)
 * This class is only instantiated to do something special, if the frame does indeed have newspaper style columns.
 */
public class MultiColFrameSupport extends FrameFlowSupport
{
	/**
	 * indicates the current column of a multi column frame
	 * -1 if there is only on column.
	 */
	protected float[] colPosBackupForMulticolumnFrame;
	BucketListOfDoubles blocks;
	RunRangeBase.SavePoint savepoint;
	float totalHeight;
	boolean lastPageSmoothingPreparation = true;
	float smoothColHeight;
	final float flowingFrameColumnWidth;
	Paragraph[] splitters;

	public MultiColFrameSupport(final FillPagedFrame fillPagedFrame, final float flowingFrameColumnWidth)
	{
		super(fillPagedFrame);
		this.flowingFrameColumnWidth = flowingFrameColumnWidth;
		colPosBackupForMulticolumnFrame = new float[fillPagedFrame.dynaCols + 1];
		System.arraycopy(fillPagedFrame.colPos, 0, colPosBackupForMulticolumnFrame, 0, colPosBackupForMulticolumnFrame.length);
		blocks = new BucketListOfDoubles();
	}

	/**
	 * at the end of a frame column, we shift all cell columns right.
	 * At the end of a page in a multi column frame the cell column positions must be reset to the frame column 0
	 */
	void incrementColPositions()
	{
/*
		if (fillPagedFrame.inFrameColumn < 0)
			return;
*/
		totalHeight += fillPagedFrame.verticalPos - fillPagedFrame.verticalStart;
		final FrameStyle fs = fillPagedFrame.template.getFrame().getCascadedFrameStyle();
		if (fillPagedFrame.lastRowState != LastRowState.endOfData)
		{
			float inc = flowingFrameColumnWidth + fs.getFlowingColumnSpacing().getValInPoints() + fs.getHorizontalInset();
			for (int i = 0; i < fillPagedFrame.colPos.length; i++)
			{
				fillPagedFrame.colPos[i] += inc;
			}
		}
		else if (lastPageSmoothingPreparation && fs.getFlowingColumnDistribution())
		{
			fillPagedFrame.dest.truncate(fillPagedFrame.destStart);
			fillPagedFrame.lDestBorderLines.clear();
			savepoint.restoreExternSavePoint();
			final int columns = fs.getFlowingColumns();
			smoothColHeight = (totalHeight / columns)+9;
			for (int i = 0; splitters != null && i < splitters.length && i < fillPagedFrame.colInCurrRow; i++)
			{
				Paragraph splitter = splitters[i];
				if (splitter != null)
					splitter.restoreSliceState();
			}
			fillPagedFrame.splitters = splitters;
			fillPagedFrame.inFrameColumn = columns - 1;
			fillPagedFrame.lastRowState = LastRowState.doneRedo;
			lastPageSmoothingPreparation = false;
			System.arraycopy(colPosBackupForMulticolumnFrame, 0, fillPagedFrame.colPos, 0, fillPagedFrame.colPos.length);
		}
	}

	void startBlock(boolean line)
	{
		if (line && fillPagedFrame.keeps > 0)// only single column or keeps are active beceause then we dont block at line level.
			return;
		if (fillPagedFrame.inFrameColumn < fillPagedFrame.getTemplate().getFrame().getCascadedFrameStyle().getFlowingColumns() - 1)// not first column
			return;
		blocks.add(fillPagedFrame.verticalPos - fillPagedFrame.verticalStart);
	}

	public int startMultiColFrame(float bodyheight)
	{
		this.bodyheight = bodyheight;
		System.arraycopy(colPosBackupForMulticolumnFrame, 0, fillPagedFrame.colPos, 0, fillPagedFrame.colPos.length);
		savepoint = fillPagedFrame.fill.makeExternSavePoint();
		blocks.clear();
		totalHeight = 0;
		if (fillPagedFrame.splitters != null)
		{
			splitters = new Paragraph[fillPagedFrame.splitters.length];
			System.arraycopy(fillPagedFrame.splitters, 0, splitters, 0, splitters.length);
			for (int i = 0; splitters != null && i < splitters.length && i < fillPagedFrame.colInCurrRow; i++)
			{
				Paragraph splitter = splitters[i];
				if (splitter != null)
					splitter.saveSliceState();
			}
		}
		else
			splitters = null;
		return fillPagedFrame.getTemplate().getFrame().getCascadedFrameStyle().getFlowingColumns() - 1;
	}

	public float initRemainingHeight()
	{
		if (smoothColHeight > 0 && fillPagedFrame.inFrameColumn != 0) // the last column on the page being evened out (smoothed) gets full height allocation too
		{
			return smoothColHeight;
		}
		return bodyheight;
	}

	public float getRemainingHeight()
	{
		return bodyheight - smoothColHeight;
	}
}
