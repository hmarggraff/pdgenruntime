// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.env.Env;
import org.pdgen.oql.OQLNode;
import org.pdgen.env.Res;
import org.pdgen.oql.OQLParseException;
import org.pdgen.env.JoriaUserError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.pdgen.data.Trace.logDebug;

public class CollectionProjection extends DefaultJoriaCollection implements CollectionView, Nameable
{
	public static final int schemaOK = 0;
	public static final int schemaChangeSortingOrFilter = 2;
	public static final int schemaChangeFatal = 3;
    private static final long serialVersionUID = 7L;
    protected static int viewCount;
	protected JoriaClass elementMatchType;
	protected JoriaCollection base;
	protected SortOrder[] sorting;
	protected Filter filter;
	protected String elementXmlTag;
	protected boolean large;
	protected int topN;// only show the first N elements
	protected RuntimeParameter topNVariable;// if formula is null use topN otherwise use topN as Default
	// transients
    protected transient int isSchemaModified;
	protected transient boolean isCentral;

	public CollectionProjection(JoriaCollection aBase)
	{
		this(aBase, aBase.getElementType());
	}

	public CollectionProjection(JoriaCollection aBase, JoriaClass elType)
	{
		super(elType);
		base = aBase;
		if (base.isView())
		{
			CollectionProjection dp = (CollectionProjection) base;
			elementMatchType = dp.getElementMatchType();
			sorting = dp.getSorting();
			topN = dp.topN;
		}
	}

	public boolean fixAccess()
	{
		if (isSchemaModified == schemaChangeFatal)
			return true;
		else if (isSchemaModified == schemaChangeSortingOrFilter)
		{
            Env.instance().repo().logFix(Res.str("Collection_"), this, Res.str("had_filter_sorting_decativated_earlier_Please_check"));
			return false;
		}
		if (elementMatchType instanceof JoriaUnknownType)
		{
            Env.instance().repo().logFix(Res.asis("Collection"), this, Res.strp("deactivated_element_match_class_was_removed", elementMatchType.getName()));
			isSchemaModified = schemaChangeFatal;
			return true;// totally unusable
		}
		if (filter != null && filter.getOqlString() != null)
		{
			final String expression = filter.getOqlString();
			try
			{
				OQLNode oqlNode = Env.instance().parseUnparented(expression, getElementType(), false);
				if (oqlNode.hasMofifiedAccess())
				{
                    Env.instance().repo().logFix(Res.str("Collection_"), this, Res.strp("filter_deactivated_expression_was", expression));
					isSchemaModified = schemaChangeSortingOrFilter;
					filter = null;
				}
			}
			catch (OQLParseException ex)
			{
				Trace.log(ex);				//ex.printStackTrace();
                Env.instance().repo().logFix(Res.str("Collection_"), this, Res.strp("filter_deactivated_expression_was", expression));
				isSchemaModified = schemaChangeSortingOrFilter;
				filter = null;
			}
		}
		if (sorting != null)
		{
			int nulledCnt = 0;
			for (int i = 0; i < sorting.length; i++)
			{
				JoriaAccess m = sorting[i].getBaseAccess();
				if (m instanceof JoriaPlaceHolderAccess)
				{
					nulledCnt++;
					sorting[i] = null;
                    Env.instance().repo().logFix(Res.str("Sorting_by"), m, Res.str("Sorting_using_changed_member_has_been_removed"));
					isSchemaModified = schemaChangeSortingOrFilter;
					Env.repoChanged();
					// forget this member
				}
				else
				{
					JoriaAccess fixed = m.getPlaceHolderIfNeeded();
					if (fixed instanceof JoriaPlaceHolderAccess)
					{
                        Env.instance().repo().logFix(Res.str("Sorting_by"), m, Res.str("Sorting_using_changed_member_has_been_removed"));
						nulledCnt++;
						sorting[i] = null;
						// forget this member
						isSchemaModified = schemaChangeSortingOrFilter;
						Env.repoChanged();
					}
					else if (fixed != null)
					{
						Env.repoChanged();
						SortOrder so = new SortOrder(fixed);
						so.setAscending(sorting[i].isAscending());
						so.setCaseSensitive(sorting[i].isCaseSensitive());
						sorting[i] = so;
					}
				}
			}
			if (nulledCnt > 0)
			{
				if (nulledCnt == sorting.length)
					sorting = null;
				else
				{
					SortOrder[] newSorters = new SortOrder[sorting.length - nulledCnt];
					int newIx = 0;
					for (SortOrder m : sorting)
					{
						if (m != null)
						{
							newSorters[newIx] = m;
							newIx++;
						}
					}
                    sorting = newSorters;
                }
				return false;
			}
		}
		return false;
	}

	public JoriaCollection getBase()
	{
		return base;
	}

	public JoriaCollection getBaseCollection()
	{
		if (base.isView())
		{
			return ((CollectionProjection) base).getBaseCollection();
		}
		else
			return base;
	}

	public JoriaClass getElementMatchType()
	{
		return elementMatchType;
	}

	public JoriaType getKeyMatchType()
	{
		return null;
	}

	public void setElementXmlTag(String newTag)
	{
		elementXmlTag = newTag;
	}

	public String getElementXmlTag()
	{
		if (elementXmlTag != null)
			return elementXmlTag;
		else
			return super.getElementXmlTag();
	}

	/**
	 * ----------------------------------------------------------------------- getParamString
	 */
	public String getParamString()
	{
		return getClass().getName() + "[" + elementMatchType.getParamString() + ", " + elementType.getParamString() + ", " + base.getParamString() + "]";
	}

	/**
	 * ----------------------------------------------------------------------- removeChild
	 */
	public void removeChild(JoriaAccess f)
	{
		if (!(getElementType() instanceof ClassView))
			throw new JoriaAssertionError("removeChild form CollectionProjection only possible for class typed elements");
		((ClassView) getElementType()).removeChild(f);
	}

	public boolean isLarge()
	{
		return large;
	}

	public void setLarge(boolean large)
	{
		this.large = large;
	}

	/**
	 * ----------------------------------------------------------------------- setElementMatchType
	 */
	public void setElementMatchType(JoriaClass elementMatchType)
	{
		if (elementMatchType != null)
		{
			logDebug(Trace.view, "setElementMatchType " + elementMatchType.getName() + Res.asis(" from ") + getElementType().getName());
			((ClassView) elementType).setBase(elementMatchType);
		}
		else
		{
			logDebug(Trace.view, "setElementMatchType null");
			((ClassView) elementType).setBase(getBaseCollection().getElementType());
		}
		this.elementMatchType = elementMatchType;
		//makeName();
	}

	/**
	 * ----------------------------------------------------------------------- setKeyMatchType
	 */
	public void setKeyMatchType(JoriaType newKeyMatchType)
	{
	}

	/**
	 * ----------------------------------------------------------------------- getSorting
	 */
	public SortOrder[] getSorting()
	{
		return sorting;
	}

	/**
	 * ----------------------------------------------------------------------- setSorting
	 */
	public void setSorting(SortOrder[] so)
	{
		sorting = so;
	}

	public boolean isView()
	{
		return true;
	}

	protected void makeName()
	{
		// leaves the name as null
	}

	protected String getElementName()
	{
		if (elementMatchType != null)
			return elementMatchType.getName();
		else if (elementType.isView())
			return ((ClassView) elementType).getBase().getName();
		else
			return elementType.getName();
	}

	public void addChild(JoriaAccess f)
	{
		Trace.check(elementType, MutableView.class);
		((MutableView) elementType).addChild(f);
	}

	/*
	public void addChild(JoriaAccess f, int at)
	{
		Trace.check(elementType, MutableView.class);
		((MutableView) elementType).addChild(f, at);
	}
	*/

	public JoriaAccess findMember(String f)
	{
		Trace.check(elementType, MutableView.class);
		return elementType.findMember(f);
	}

	public void replaceChild(JoriaAccess f, JoriaAccess fNew)
	{
		Trace.check(elementType, MutableView.class);
		((MutableView) elementType).replaceChild(f, fNew);
	}

	public Filter getFilter()
	{
		return filter;
	}

	public void setFilter(Filter newFilter)
	{
		filter = newFilter;
	}

	public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		if (filter != null)
		{
			final Set<RuntimeParameter> variables = filter.getVariables();
			RuntimeParameter.addAll(s, variables, seen);
		}
		if (topNVariable != null)
		{
			s.add(topNVariable);
		}
		if (base instanceof VariableProvider)
		{
			VariableProvider vp = (VariableProvider) base;
			vp.collectVariables(s, seen);
		}
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		if (filter != null && filter.getOqlString() != null)
		{
			final String expression = filter.getOqlString();
            Env.instance().collectI18nKeys(expression, getElementType(), Env.instance().repo().loadingCentralRepository, Res.strp("Collection_Filter_in", getName()), s);
		}
		if (base instanceof VariableProvider)
		{
			VariableProvider vp = (VariableProvider) base;
			vp.collectI18nKeys2(s, seen);
		}
		if (elementType instanceof VariableProvider)
		{
			VariableProvider vp = (VariableProvider) elementType;
			vp.collectI18nKeys2(s, seen);
		}
	}

	public boolean isFlattening()
	{
		return false;
	}

	public boolean isDictionary()
	{
		return base.isDictionary();
	}

	public JoriaType getOriginalType()
	{
		return base;
	}

	public JoriaClass getAsParent()
	{
		return getElementType();
	}

	public void setBase(JoriaClass c)
	{
		((ClassView) getElementType()).setBase(c);
	}

	public void setName(String aName)
	{
		NameableTracer.notifyListenersPre(this);
		super.setName(aName);
		NameableTracer.notifyListenersPost(this);
	}

	public MutableView dup(Map<Object, Object> alreadyCopiedViews)
	{
		final Object duplicate = alreadyCopiedViews.get(this);
		if (duplicate != null)
			return (MutableView) duplicate;

		CollectionProjection ret;
		if (elementType instanceof ClassView)
		{
            MutableView newType = (MutableView) alreadyCopiedViews.get(elementType);
			if (newType == null)
			{
				newType = ((MutableView) elementType).dup(alreadyCopiedViews);
			}
			ret = new CollectionProjection(base, (JoriaClass) newType);
		}
		else
			ret = new CollectionProjection(base, elementType);
		alreadyCopiedViews.put(this, ret);
		ret.elementMatchType = elementMatchType;
		ret.sorting = sorting;
		if (filter != null)
			ret.filter = filter.dup();
		ret.elementXmlTag = elementXmlTag;
		ret.large = large;
		return ret;
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
		logDebug(Trace.copy, "collectViewUsage collectionProjection1: " + getName());
		if (visitedViews.contains(this))
		{
			logDebug(Trace.copy, "collectViewUsage stop " + getName());
			return;
		}
		visitedViews.add(this);
		if (base instanceof MutableView)
		{
			Repository.addViewUsage(viewUsage, (MutableView) base, this);
			((MutableView) base).collectViewUsage(viewUsage, visitedViews);
		}
		if (elementType instanceof MutableView)
		{
			Repository.addViewUsage(viewUsage, (MutableView) elementType, this);
			((MutableView) elementType).collectViewUsage(viewUsage, visitedViews);
		}
		if (sorting != null)
			for (SortOrder sortOrder : sorting)
			{
				JoriaAccess soAXS = sortOrder.getBaseAccess();
				if (soAXS instanceof MutableAccess)
					((MutableAccess) soAXS).collectViewUsage(viewUsage, visitedViews);
			}
	}

	public void collectUsedViews(Set<MutableView> s)
	{
		logDebug(Trace.copy, "collectUsedViews collectionProjection2: " + getName());
		if (s.contains(this))
		{
			logDebug(Trace.copy, "collectUsedViews stop " + getName());
			return;
		}
		int ssize = s.size();
		s.add(this);
		if (ssize + 1 != s.size())
			throw new JoriaAssertionError("add failed");
		if (base instanceof MutableView)
			((MutableView) base).collectUsedViews(s);
		if (elementType instanceof MutableView)
			((MutableView) elementType).collectUsedViews(s);
		if (sorting != null)
			for (SortOrder sortOrder : sorting)
			{
				JoriaAccess soAXS = sortOrder.getBaseAccess();
				if (soAXS instanceof MutableAccess)
					((MutableAccess) soAXS).collectUsedViews(s);
			}
	}

	protected CollectionProjection(String newName)
	{
		name = newName;
		//if(name != null)
		//   curtain.addCollectionView(this);
	}


	public CollectionProjection copyReportPrivate(Map<Object, Object> copiedData)
	{
		String newName = name;
		if (newName != null)
			newName = getName();
		CollectionProjection ret = new CollectionProjection(newName);
		ret.elementType = elementType;
		ret.elementMatchType = elementMatchType;
		ret.base = base;
		ret.topN = topN;
		if (sorting != null)
		{
			ret.sorting = new SortOrder[sorting.length];
			for (int i = 0; i < sorting.length; i++)
			{
				ret.sorting[i] = sorting[i].copy();
			}
		}
		if (filter != null)
			ret.filter = filter.copy(copiedData);
		ret.elementXmlTag = elementXmlTag;
		ret.large = large;
		return ret;
	}

	public String getName()
	{
		if (name == null)
			return Res.asis("V_") + base.getName();
		return name;
	}

	public JoriaCollection getPhysical()
	{
		if (base instanceof CollectionProjection)
			return ((CollectionProjection) base).getPhysical();
		else
			return base;
	}

	public int getTopN()
	{
		return topN;
	}

	public void setTopN(int topN)
	{
		this.topN = topN;
	}

	public int getMinTopN(RunEnv env) throws JoriaDataException
	{
		int ret = 0;
		if (base instanceof MutableCollection)
		{
			MutableCollection mt = (MutableCollection) base;
			ret = computeTopN(mt, env);
		}
		int myTopN = computeTopN(this, env);
		if (ret == 0)
			return myTopN;
		else if (myTopN == 0)
			return ret;
		else
			return Math.min(myTopN, ret);
	}

	public boolean hasFilterOrSortingOrTopN()
	{
		return getTopN() != 0 || getTopNVariable() != null || getSorting() != null || getFilter() != null && getFilter().getOqlString() != null || getElementMatchType() != null;
	}

	public static int computeTopN(MutableCollection mt, RunEnv env) throws JoriaDataException
	{
		int ret;
		RuntimeParameter f = mt.getTopNVariable();
		if (f != null)
		{
			DBData parameterValue = env.getRuntimeParameterValue(f);
			if (parameterValue == null || parameterValue.isNull())
				return mt.getTopN();
			if (!(parameterValue instanceof DBInt))
				throw new JoriaUserError("Paramater Value used as TopN must be an Integer. But is: " +parameterValue.getActualType());
			ret = (int) ((DBInt)parameterValue).getIntValue();
		}
		else
			ret = mt.getTopN();
		return ret;
	}

	public String toString()
	{
		if (name == null)
			return Res.asis("Collection of ") + elementType.toString();
		return name;
	}

	public RuntimeParameter getTopNVariable()
	{
		return topNVariable;
	}

	public void setTopNVariable(RuntimeParameter topNVariable)
	{
		this.topNVariable = topNVariable;
	}

	public boolean hasName()
	{
		return name != null;
	}

	public void unbind()
	{
		super.unbind();
		base = new UnboundCollectionSentinel(base);
		elementMatchType = null;
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		base = (JoriaCollection) newBinding.getType();
		super.rebind(newBinding, newParentBinding);
	}

	public boolean unbound()
	{
		return base instanceof UnboundCollectionSentinel;
	}

	public void sortMembers()
	{
		((ClassView)elementType).sortMembers();
	}


}
