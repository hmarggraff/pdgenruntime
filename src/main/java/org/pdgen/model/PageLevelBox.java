// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.MutableView;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.style.FrameStyle;
import org.pdgen.model.style.PageStyle;
import org.pdgen.data.JoriaAccess;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class PageLevelBox extends TemplateBox implements PageLevelBoxInterface
{
    private static final long serialVersionUID = 7L;
    public static int firstPage;
	public static int middlePage = 1;
	public static int lastPage = 2;
	private PageMaster page;

	public PageLevelBox(PageMaster page, JoriaAccess root, int typ)
	{
		super(typ, root);
		this.page = page;
	}

	public PageLevelBox(PageMaster page, JoriaAccess root, int typ, int rows, int cols)
	{
		super(typ, root);
		new TemplateModel(rows, cols, this);
		this.page = page;
	}

	public PageLevelBox(PageMaster page, PageLevelBox t)
	{
		super(t.boxType, t.root);
		this.page = page;
		frameStyle = t.frameStyle;
		cascadedFrameStyle = t.cascadedFrameStyle;
		cascadedCellStyle = t.cascadedCellStyle;
	}

	public PageLevelBox(JoriaAccess root, PageLevelBox t, TemplateModel m)
	{
		super(t.boxType, root);
		frameStyle = t.frameStyle;
		cascadedFrameStyle = t.cascadedFrameStyle;
		cascadedCellStyle = t.cascadedCellStyle;
		template = m;
	}

	public PageLevelBox(JoriaAccess root, PageLevelBox t, int type, TemplateModel m)
	{
		super(type, root);
		frameStyle = t.frameStyle;
		cascadedFrameStyle = t.cascadedFrameStyle;
		cascadedCellStyle = t.cascadedCellStyle;
		template = m;
	}

	//used by herag
	public PageLevelBox(PageMaster pb, int typ, int rows, int cols)
	{
		super(typ, null);
		page = pb;
		new TemplateModel(rows, cols, this);
	}

	public FrameStyle getCascadedFrameStyle()
	{
		//Trace.log(5, Trace.style, "templateBox.getCascadedFrameStyle " + getParameterString());
		if (cascadedFrameStyle == null)
		{
			cascadedFrameStyle = new FrameStyle(getFrameStyle());
			// the topmost box is a pagelevel box which deals with cascading from the page defaults
			cascadedFrameStyle.mergeFrameDefaults();
			if (page != null)
				cascadedFrameStyle.mergeTextStyle(page.getCascadedPageStyle());
			else
				cascadedFrameStyle.mergeTextStyle(PageStyle.getDefault());
		}
		return cascadedFrameStyle;
	}

	public PageMaster getPage()
	{
		return page;
	}

	public PageLevelBox getPageLevelParent()
	{
		return this;
	}

	public TemplateBoxInterface getTopLevelParent()
	{
		return this;
	}

	public PageLevelBox duplicateAll(HashMap<Object, Object> copiedData)
	{
		PageLevelBox p = new PageLevelBox(null, root, boxType);
		fillDuplicate(p, copiedData);
		return p;
	}

	public PageLevelBox duplicate(PageMaster newPage, Map<Object, Object> copiedData)
	{
		PageLevelBox p = new PageLevelBox(newPage, root, boxType);
		fillDuplicate(p, copiedData);
		return p;
	}

	public void removeObsoleteAggregates(Collection<? extends CellDef> cells, Collection<AggregateDef> obsoleteAggregates)
	{
		page.removeObsoleteAggregates(cells, obsoleteAggregates);
	}

	public void prerun(RunEnv env)
	{
		//TODO
	}

	public void postrun(RunEnv env)
	{
		//TODO
	}

	public boolean isFooter()
	{
		return boxType == TemplateBoxInterface.firstPageFooter || boxType == TemplateBoxInterface.middlePagesFooter || boxType == TemplateBoxInterface.lastPageFooter;
	}

	public boolean isHeader()
	{
		return boxType == TemplateBoxInterface.firstPageHeader || boxType == TemplateBoxInterface.furtherPagesHeader;
	}

	public void setPage(final PageMaster page)
	{
		this.page = page;
	}

	public void resetMaster()
	{
		page = null;
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsageMap, Set<MutableView> visitedViews)
	{
		RepeaterList rl = template.getRepeaterList();
		rl.collectViewUsage(viewUsageMap, visitedViews);
	}

	public void rebindByName(final JoriaAccess newScope)
	{
		template.rebindByName(newScope.getClassTypeAsserted());
		root = newScope;
	}

	public PageLevelBox newFor(PageMaster pageMaster)
	{
		return new PageLevelBox(pageMaster, this);
	}

	public JoriaAccess getRoot()
	{
		if (root == null && page != null)
			return page.getData();
		return root;
	}

	public String getParamString()
	{
		if (frameStyle != null && frameStyle.getName() != null)
			return frameStyle.getName();
		return getBoxTypeName();
	}

	public void setRoot(final JoriaAccess root)
	{
		this.root = root;
	}
}

