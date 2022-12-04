// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.schemacheck.ViewChecker;
import org.pdgen.env.Res;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupCollection extends DefaultJoriaCollection implements MutableCollection
{
    private static final long serialVersionUID = 7L;
    Filter filter;
	int topN;
	RuntimeParameter topNVariable;

	public GroupCollection(GroupClass groupType)
	{
		super(groupType);
	}
	public GroupCollection(GroupClass groupType, GroupCollection from)
	{
		super(groupType);
		name = from.name;
		topNVariable = from.topNVariable;
		if (filter != null)
			filter = from.filter.dup();
	}

	protected GroupCollection(String newName)
	{
		name = newName;
	}

	
	public MutableView dup(Map<Object, Object> alreadyCopiedViews)
	{
		throw new JoriaAssertionError("dup of GroupCollection must be handled by parent.");
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
		if (visitedViews.contains(this))
			return;
		visitedViews.add(this);
		if (elementType instanceof MutableView)
		{
			Repository.addViewUsage(viewUsage, (MutableView) elementType, this);
			((MutableView) elementType).collectViewUsage(viewUsage, visitedViews);
		}
	}

	public void collectUsedViews(Set<MutableView> s)
	{
		if (s.contains(this))
			return;
		s.add(this);
		if (elementType instanceof MutableView)
			((MutableView) elementType).collectUsedViews(s);
	}

	protected String getTag()
	{
		return Res.asis("Groups");
	}

	public void addChild(JoriaAccess f)
	{
		((ClassView) elementType).addChild(f);
	}

	public JoriaAccess findMember(String f)
	{
		return elementType.findMember(f);
	}

	public JoriaType getOriginalType()
	{
		return ((ClassView) elementType).getOriginalType();
	}

	public void removeChild(JoriaAccess f)
	{
		((ClassView) elementType).removeChild(f);
	}

	public void replaceChild(JoriaAccess f, JoriaAccess fNew)
	{
		((ClassView) elementType).replaceChild(f, fNew);
	}

	public void setBase(JoriaClass c)
	{
		((ClassView) elementType).setBase(c);
	}

	public void setFilter(Filter f)
	{
		filter = f;
	}

	public boolean fixAccess()
	{
		return false;
	}

	public void setSorting(SortOrder[] so)
	{
		Trace.logWarn("Trying to set sorting for a group collection. Ignored.");
	}

	public void setTopN(int topN)
	{
		this.topN = topN;
	}

	public void setTopNVariable(RuntimeParameter topNFormula)
	{
		topNVariable = topNFormula;
	}

	public MutableCollection copyReportPrivate(final Map<Object, Object> copiedData)
	{
		String newName = name;
		if (newName != null)
			newName = getName();
		GroupCollection ret = new GroupCollection(newName);
		ret.elementType = elementType;
		ret.topN = topN;
		if (filter != null)
			ret.filter = filter.copy(copiedData);
		return ret;
	}

	//public Set getUsedVariables();
	public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		if (filter != null && filter.getVariables() != null)
			s.addAll(filter.getVariables());
		((ClassView) elementType).collectVariables(s, seen);
		if (topNVariable != null)
			s.add(topNVariable);
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		((ClassView) elementType).collectI18nKeys2(s, seen);
		if (topNVariable != null)
			topNVariable.collectI18nKeys2(s);
	}

	public JoriaCollection getBaseCollection()
	{
		GroupClass gec = (GroupClass) getElementType();
		JoriaCollection baseCollection = gec.getGroupValueAxs().getCollectionTypeAsserted();

		return baseCollection;
	}

	public Filter getFilter()
	{
		return filter;
	}

	protected Object readResolve()
	{
		name = null;// groupcolls may have no name, but some old ones have one in spite
		ViewChecker.add(getElementType());
		return this;
	}

	public String getName()
	{
		if (elementType == null || elementType.getName() == null)// this should only happen during copy, when element type has not already be set
			return name;
		return "G_" + elementType.toString();
	}

	public int getTopN()
	{
		return topN;
	}

	public RuntimeParameter getTopNVariable()
	{
		return topNVariable;
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	public int getMinTopN(RunEnv env) throws JoriaDataException
	{
		final int ret = CollectionProjection.computeTopN(this, env);
		return ret;
	}

	public boolean hasName()
	{
		return name != null;
	}

	public void sortMembers()
	{
		((ClassView)elementType).sortMembers();
	}
	
}
