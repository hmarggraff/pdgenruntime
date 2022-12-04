// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;


import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Stack;

public class UnboundCollectionSentinel extends AbstractJoriaCollection implements MutableCollection
{
    private static final long serialVersionUID = 7L;

    public UnboundCollectionSentinel(JoriaCollection sourceCollection)
	{
		super(new UnboundClassSentinel(sourceCollection.getElementType()));
	}
	public UnboundCollectionSentinel(UnboundCollectionSentinel from)
	{
		super(new UnboundClassSentinel(from.elementType));
	}

	public JoriaCollection getBaseCollection()
	{
		return null;
	}

	public void setSorting(SortOrder[] so)
	{
	}

	public void setTopN(int topN)
	{
	}

	public int getTopN()
	{
		return 0;
	}

	public RuntimeParameterLiteral getTopNVariable()
	{
		return null;
	}

	public void setTopNVariable(RuntimeParameter param)
	{
	}

	public MutableCollection copyReportPrivate(final Map<Object, Object> copiedData)
	{
		return new UnboundCollectionSentinel(this);
	}

	public void addChild(JoriaAccess f)
	{
	}

	public JoriaAccess findMember(String f)
	{
		return null;
	}

	public void removeChild(JoriaAccess f)
	{
	}

	public void replaceChild(JoriaAccess f, JoriaAccess fNew)
	{
	}

	public void setName(String name)
	{
	}

	public JoriaType getOriginalType()
	{
		return null;
	}

	public JoriaClass getAsParent()
	{
		return null;
	}

	public void setBase(JoriaClass c)
	{
	}

	public boolean fixAccess()
	{
		return false;
	}

	public MutableView dup(Map<Object, Object> alreadyCopiedViews)
	{
		return this;
	}

	public void collectUsedViews(Set<MutableView> s)
	{
	}

	public void collectViewUsage(Map<MutableView,Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
	}

	public boolean hasName()
	{
		return false;
	}

	public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
	{
	}

	public void collectI18nKeys2(HashMap<String,List<I18nKeyHolder>> s, Set<Object> seen)
	{
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
	}

	public void unbind()
	{
	}

	public boolean unbound()
	{
		return false;
	}

	public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		return false;
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
	}
	public void sortMembers()
	{
		if (elementType instanceof ClassView)
			((ClassView)elementType).sortMembers();
	}
	
}
