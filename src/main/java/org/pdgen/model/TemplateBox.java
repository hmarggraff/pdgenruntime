// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;/*
 * TemplateBox.java
 *
 * Created on October 6, 1999, 2:26 PM
 */

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FrameStyle;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.JoriaUserError;

import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.oql.JoriaDeferedQuery;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParseException;
import org.pdgen.oql.OQLParser;
import org.pdgen.util.StringUtils;

import java.util.*;

public abstract class TemplateBox implements TemplateBoxInterface
{
    private static final long serialVersionUID = 7L;
    protected FrameStyle frameStyle;
	protected int boxType;
	protected TemplateModel template;
	private String visibilityCondition;
	protected transient FrameStyle cascadedFrameStyle;
	protected transient CellStyle cascadedCellStyle;
	protected JoriaAccess root;

	protected TemplateBox(int typ, JoriaAccess root)
	{
		this.root = root;
		boxType = typ;
	}

	protected void fillDuplicate(TemplateBox p, Map<Object, Object> copiedData)
	{
		p.frameStyle = FrameStyle.duplicateLocal(frameStyle);
		p.visibilityCondition = visibilityCondition;
		p.template = template.duplicate(p, copiedData);
		p.boxType = boxType;
		p.root = root;
	}

	public void cascadeCellStyle(CellStyle aCellStyle)
	{		// we only cascade the text styles
		aCellStyle.mergeTextStyle(getCascadedFrameStyle());
		aCellStyle.mergeDefaults();
	}

	public FrameStyle getFrameStyle()
	{
		return frameStyle;
	}

	public int getBoxType()
	{
		return boxType;
	}

	public String getBoxTypeName()
	{
		Trace.check(getBoxType() < TemplateBoxInterface.boxNames.length);
		return TemplateBoxInterface.boxNames[getBoxType()];
	}

	public TemplateModel getTemplate()
	{
		return template;
	}

	public void setFrameStyle(FrameStyle newFrameStyle)
	{
		frameStyle = newFrameStyle;
		cascadedFrameStyle = null;
		template.fireChange("frame style changed");
		if (template != null)// template can be null in a RunCanvas
			template.clearCachedStyles();
	}

	public void namedFrameStyleChanged(FrameStyle f)
	{
		if (frameStyle == f)
			clearCachedStyles();// clear all cached styles within this box, no need to serch further
		else
			template.namedFrameStyleChanged(f);// check nested frames
	}

	public void clearCachedStyles()
	{
		cascadedFrameStyle = null;
		cascadedCellStyle = null;
		if (template != null)// template can be null in a run canvas
			template.clearCachedStyles();
	}

	public void setBoxType(int newBoxType)
	{
		boxType = newBoxType;
	}

	public void setTemplate(TemplateModel newtemplate)
	{
		template = newtemplate;
		template.setContainer(this);
	}

	public void removeObsoleteAggregates(Collection<? extends CellDef> cells, Collection<AggregateDef> obsoleteAggregates)
	{
		getPageLevelParent().removeObsoleteAggregates(cells, obsoleteAggregates);
	}

	public CellStyle getCascadedCellStyle()
	{
		synchronized (this)
		{
			if (cascadedCellStyle == null)
			{
				cascadedCellStyle = new CellStyle();
				cascadeCellStyle(cascadedCellStyle);
			}
		}
		return cascadedCellStyle;
	}

	public void fixAccess()
	{
		if (root != null)
		{
			JoriaAccess fixed = root.getPlaceHolderIfNeeded();
			if (fixed instanceof JoriaPlaceHolderAccess)
			{
				Trace.logWarn("TemplateBox root missing: " + ((JoriaPlaceHolderAccess) fixed).getInfo());
                Env.instance().repo().logFix(Res.str("FrameRoot"), root, Res.str("Root_deactivated"));
				root = null;
			}
		}
		template.fixAccess();
	}

	public void collectVariables(Set<RuntimeParameter> variables, Set<Object> seen)
	{
		template.collectVariables(variables, seen);
		RuntimeParameter.addAll(variables, getVisibilityConditionVars(), seen);
	}

	private Set<RuntimeParameter> getVisibilityConditionVars()
	{
		JoriaType scope = getScope();
		try
		{
			if (StringUtils.isEmpty(visibilityCondition))
				return null;
			return OQLParser.checkForVars(visibilityCondition, scope);
		}
		catch (Exception ex)
		{
			throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
		}
	}

	private Set<JoriaAccess> getVisibilityConditionUsedAccessors()
	{
		try
		{
			if (StringUtils.isEmpty(visibilityCondition))
				return null;
			JoriaType scope = getScope();
			if (scope == null)
				return null;
			return OQLParser.checkForUsedAccessors(visibilityCondition, scope);
		}
		catch (Exception ex)
		{
			throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
		}
	}

	public void fixVisibiltyCondition()
	{
		if (!StringUtils.isEmpty(visibilityCondition))
		{
			try
			{
				OQLParser.parse(visibilityCondition, getScope(), true);
			}
			catch (Exception ex)
			{
				try
				{
					Trace.log(ex);
					OQLParser.parse(visibilityCondition, getScope(), true);
				}
				catch (Throwable e)
				{
					Trace.log(e);
				}
                Env.instance().repo().logFix(Res.str("Visibility_Condition_on_a_frame"), visibilityCondition, Res.str("Visibility_condition_deactivated"));
				visibilityCondition = null;// TODO bessere Loesung notwendig
			}
		}
		template.fixVisibiltyCondition();
	}

	public String getVisibilityCondition()
	{
		return visibilityCondition;
	}

	public void setVisibilityCondition(String visibilityCondition)
	{
		this.visibilityCondition = visibilityCondition;
	}

	public JoriaQuery makeVisibilityQuery()
	{
		if (StringUtils.isEmpty(visibilityCondition))
			return null;
		JoriaQuery q;
		try
		{
			q = OQLParser.parse(visibilityCondition, getScope(), true);
		}
		catch (Exception ex)
		{
			throw new JoriaInternalError("Visibility condition of frame failed: Unexpected parse exception in run", ex);
		}
		return q;
	}

	public JoriaClass getScope()
	{
		if (root != null)
			return root.getType().getAsParent();
		else if (getPageLevelParent().getRoot().getType() != null)
			return getPageLevelParent().getRoot().getType().getAsParent();
		else
			return null;
	}

	public boolean isVisible(RunEnv env, DBData from) throws JoriaDataException
	{
		JoriaQuery q = makeVisibilityQuery();
		if (q == null)
			return true;
		if (q instanceof JoriaDeferedQuery)
			throw new JoriaUserError("cannot_use_totalpages_in_visibility");
		return q.getBooleanValue(env, from);
	}

	public boolean isVisibleDeferred(RunEnv env) throws JoriaDataException
	{
		JoriaQuery q = makeVisibilityQuery();
		if (q == null)
			return true;
		if (q instanceof JoriaDeferedQuery)
		{
			JoriaDeferedQuery dq = (JoriaDeferedQuery) q;
			if (dq.isNeedsAllPages())
				throw new JoriaUserError("cannot_use_totalpages_in_visibility");
			return dq.getBooleanValue(env, null);
		}
		return true;
	}

	public void getAllReferencedFiles(HashSet<String> ret)
	{
		template.getAllReferencedFiles(ret);
	}

	public void getUsedAccessors(Set<JoriaAccess> ret) throws OQLParseException
	{
		OQLParser.getUsedAccessors(ret, visibilityCondition, getPageLevelParent().getRoot().getType());
		template.getUsedAccessors(ret);
	}

	public boolean visit(FrameVisitor frameVisitor)
	{
		return frameVisitor.visitFrame(this);
	}

	public boolean isHeaderOrFooter()
	{
		return isHeader() || isFooter();
	}

	public void makeFilenameRelative(JoriaFileService fs)
	{
		template.makeFilenameRelative(fs);
		if (frameStyle != null)
			frameStyle.makeFileName(fs, true);
	}

	public void makeFilenameAbsolute(JoriaFileService fs)
	{
		template.makeFilenameAbsolute(fs);
		if (frameStyle != null)
			frameStyle.makeFileName(fs, false);
	}


	public boolean visitAccess(AccessVisitor visitor, Set<JoriaAccess> seen)
	{
		if (!seen.contains(root))
		{
			seen.add(root);
			if (!visitor.visit(root))
				return false;
			if (root instanceof VisitableAccess)
			{
				if (!((VisitableAccess) root).visitAllAccesses(visitor, seen))
					return false;
			}
		}
		Set<JoriaAccess> pov = getVisibilityConditionUsedAccessors();
		if (pov != null)
		{
			for (JoriaAccess joriaAccess : pov)
			{
				if (!visitor.visit(joriaAccess))
					return false;
				if (joriaAccess instanceof VisitableAccess)
				{
					if (!((VisitableAccess) joriaAccess).visitAllAccesses(visitor, seen))
						return false;
				}
			}
		}
		return template.visitAllAccesses(visitor, seen);
	}

	public FrameStyle getCascadedFrameStyle()
	{
		return cascadedFrameStyle;
	}

	public JoriaAccess getRoot()
	{
		if (root != null)
			return root;
		return getPageLevelParent().getRoot();
	}


	protected abstract void resetMaster();

}
