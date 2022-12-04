// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;


import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class UnaryOperatorNode extends Node
{
	// fields
	protected NodeInterface sub;

	protected UnaryOperatorNode(NodeInterface p0)
	{
		sub = p0;
	}

	public boolean isBoolean()
	{
		return sub.isBoolean();
	}

	public boolean isCharacter()
	{
		return sub.isCharacter();
	}

	public boolean isCollection()
	{
		return sub.isCollection();
	}

	public boolean isLiteralCollection()
	{
		return sub.isLiteralCollection();
	}

	public boolean isDictionary()
	{
		return sub.isDictionary();
	}

	public boolean isInteger()
	{
		return sub.isInteger();
	}

	public boolean isReal()
	{
		return sub.isReal();
	}

	public boolean isObject()
	{
		return sub.isObject();
	}

	public boolean isDate()
	{
		return sub.isDate();
	}

	public boolean isString()
	{
		return sub.isString();
	}

	public boolean hasMofifiedAccess()
	{
		return sub.hasMofifiedAccess();
	}

	public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException
	{
		sub.cacheDeferredFields(env, from);
	}

	public void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect)
	{
		sub.i18nKeys(collect);
	}

	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		return sub.hasText(text, searchLabels, searchData);
	}
	public void getUsedAccessors(Set<JoriaAccess> ret)
	{
		sub.getUsedAccessors(ret);
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		sub.collectVariables(set, seen);
		if (sub instanceof VariableProvider)
		{
			VariableProvider vp = (VariableProvider) sub;
			vp.collectVariables(set, seen);
		}
	}
}
