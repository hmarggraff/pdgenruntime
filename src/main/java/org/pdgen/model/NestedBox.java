// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.NestingCellDef;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.style.FrameStyle;

import java.util.Map;

public class NestedBox extends TemplateBox
{
    private static final long serialVersionUID = 7L;
    protected NestingCellDef cell;

	public NestedBox(NestingCellDef wrapper)
	{
		super(TemplateBoxInterface.nestedBox, wrapper.getGrid().getFrame().getRoot());
		cell = wrapper;
		template = new TemplateModel(2, 1, this);
	}

	public NestingCellDef getCell()
	{
		return cell;
	}

	protected void resetMaster()
	{
		cell = null;
	}

	public NestedBox duplicate(NestingCellDef newContainer, Map<Object,Object> copiedData)
	{
		NestedBox nestedBox1 = new NestedBox(newContainer);
		fillDuplicate(nestedBox1, copiedData);
		return nestedBox1;
	}

	public PageLevelBox getPageLevelParent()
	{
		return cell.getGrid().getFrame().getPageLevelParent();
	}

	public TemplateBoxInterface getTopLevelParent()
	{
		return cell.getGrid().getFrame().getTopLevelParent();
	}

	public boolean isFooter()
	{
		return getPageLevelParent().isFooter();
	}

	public boolean isHeader()
	{
		return getPageLevelParent().isHeader();
	}

	public FrameStyle getCascadedFrameStyle()
	{
		if (cascadedFrameStyle == null)
		{
			cascadedFrameStyle = new FrameStyle(getFrameStyle());			// the topmost box is a pagelevel box whic deals with cascading from the page defaults
			cascadedFrameStyle.mergeFrom(getPageLevelParent().getCascadedFrameStyle());
		}
		return cascadedFrameStyle;
	}

	public void prerun(RunEnv env)
	{		//TODO
	}

	public void postrun(RunEnv env)
	{		//TODO
	}
}
