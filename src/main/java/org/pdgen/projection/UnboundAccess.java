// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.*;
import org.pdgen.data.view.CollectUsedViewsAccess;
import org.pdgen.data.view.MutableView;
import org.pdgen.model.run.RunEnv;


import java.util.Set;
import java.util.Map;

public class UnboundAccess extends AbstractTypedJoriaMember implements CollectUsedViewsAccess
{
    private static final long serialVersionUID = 7L;

    public UnboundAccess(JoriaClass aParent, String name)
	{
		super(aParent, name);
		type = DefaultStringLiteral.instance();
		makeLongName();
	}

	public UnboundAccess(JoriaClass aParent, String name, JoriaType t)
	{
		super(aParent, name, t);
		makeLongName();
	}


	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		return null;
	}

	public boolean isRoot()
	{
		return false;
	}

	public void setName(String newName)
	{
		name = newName;
		makeLongName();
	}

	public JoriaType getSourceTypeForChildren()
	{
		return getType();
	}

	public void makeName()
	{
		makeLongName();
	}

	protected Object readResolve()
	{
		UnboundAccess match = (UnboundAccess) UnboundMembersClass.instance().findMember(name);
		if (match != null)
			return match;
		return this;
	}

	public void collectUsedViews(Set<MutableView> s)
	{
		//empty
	}

	public void collectViewUsage(Map<MutableView,Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
		//empty
	}
}
