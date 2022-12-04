// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.SizeLimit;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.FillPagedFrame;
import org.pdgen.model.run.TableBorderRequirements;

public abstract class PictureCellBase extends FreeCellDef
{
    private static final long serialVersionUID = 7L;
    /**
	 * the width that the picture shall appear with
	 * if it is null then use the resultant from 72 dpi.
	 */
	protected FlexSize targetWidth;
	protected SizeLimit sizeLimit;


	protected PictureCellBase(TemplateModel containerGrid)
	{
		super(containerGrid);
	}

	protected PictureCellBase(FreeCellDef from, TemplateModel containerGrid)
	{
		super(from, containerGrid);
		PictureCellBase pictureCellBase = (PictureCellBase) from;
		targetWidth = pictureCellBase.targetWidth; // immutable: reuse
		sizeLimit = pictureCellBase.sizeLimit; // enum: reuse
	}

	public FlexSize getTargetWidth()
	{
		return targetWidth;
	}

	public void setTargetWidth(FlexSize targetWidth)
	{
		this.targetWidth = targetWidth;
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange();
    }

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException
	{
		return out.makePictureGraphEl(tblReq, iter);
	}

	public SizeLimit getSizeLimit()
	{
		return sizeLimit;
	}

	public void setSizeLimit(final SizeLimit sizeLimit)
	{
		this.sizeLimit = sizeLimit;
		grid.fireChange();
	}

	public boolean mustScale(final int pictWidth)
	{
		if (targetWidth == null || targetWidth.isExpandable() || sizeLimit == null) // as is
			return false;
		float width = targetWidth.getVal();
		final boolean mustScaleDown = sizeLimit == SizeLimit.AtMost && pictWidth > width;
		final boolean mustScaleUp = sizeLimit == SizeLimit.AtLeast && pictWidth < width;
		final boolean mustScale = sizeLimit == SizeLimit.Fix && pictWidth != width;
		return mustScale || mustScaleUp || mustScaleDown;
	}

}
