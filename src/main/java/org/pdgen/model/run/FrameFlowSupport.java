// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

// Created 28.12.2007 by hmf
public class FrameFlowSupport
{
	float bodyheight;
	protected  FillPagedFrame fillPagedFrame;

	public FrameFlowSupport(final FillPagedFrame fillPagedFrame)
	{
		this.fillPagedFrame = fillPagedFrame;
	}

	void incrementColPositions()
	{
	}

	void startBlock(boolean line)
	{
	}

	public float initRemainingHeight()
	{
		return bodyheight;
	}


	public float getRemainingHeight()
	{
		return fillPagedFrame.remainingHeight;
	}

	public int startMultiColFrame(float bodyheight)
	{
		this.bodyheight = bodyheight;
		return 0;
	}

}
