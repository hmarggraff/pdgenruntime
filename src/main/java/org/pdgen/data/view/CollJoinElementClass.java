// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;


import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class CollJoinElementClass extends DefaultJoriaClass implements VariableProvider
{
    private static final long serialVersionUID = 7L;

    public CollJoinElementClass(JoriaAccess left, JoriaAccess right, boolean grouped)
	{
		super("Join_" + left.getCollectionTypeAsserted().getElementType().getName() + '_' + right.getCollectionTypeAsserted().getElementType().getName());
		FlattenedElementAccess leftAxs = new FlattenedElementAccess(this, "left", left.getCollectionTypeAsserted().getElementType(), false);
		final JoriaType rightType;
		if (grouped)
			rightType = new DefaultJoriaCollection(right.getCollectionTypeAsserted().getElementType());
		else
			rightType = right.getCollectionTypeAsserted().getElementType();
		FlattenedElementAccess rightAxs = new FlattenedElementAccess(this, "right", rightType, true);
		members = new JoriaAccess[]{leftAxs, rightAxs};
	}

	public String toString()
	{
		return myName;
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		for (JoriaAccess member : members)
		{
			if (member instanceof VariableProvider)
			{
				pathStack.push(member);
				((VariableProvider) member).collectVisiblePickersInScope(collection, visible, pathStack, seen);
				pathStack.pop();
			}
		}
	}

	public void collectVariables(Set<RuntimeParameter> r, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		for (JoriaAccess access : members)
		{
			if (access instanceof VariableProvider)
			{
				((VariableProvider) access).collectVariables(r, seen);
			}
		}
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		for (JoriaAccess access : members)
		{
			if (access instanceof VariableProvider)
			{
				((VariableProvider) access).collectI18nKeys2(s, seen);
			}
		}
	}
}