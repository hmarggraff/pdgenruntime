// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class BooleanOperatorNode extends Node
{
	// fields
	protected NodeInterface left;
	protected NodeInterface right;
	protected int commonOperandType;

	protected BooleanOperatorNode(int p0, NodeInterface p1, NodeInterface p2)
	{
		commonOperandType = p0;
		left = p1;
		right = p2;
	}

	/**
	 * Compares 2 strings and takes care of potential null values
	 * Comparison is case sensitive
	 * Creation date: (18.1.00 11:09:19)
	 *
	 * @param p   java.lang.String
	 * @param q   java.lang.String
	 * @param env the environment with locale etc
	 * @return int returns -1 if p is less than q, 0 if they are equal, +1 if p is greater than q, returns 2 if one of the values is null
	 */
	protected int compareStrings(String p, String q, RunEnv env)
	{
		int ret;
		if (p != null)
		{
			if (q != null)
			{
				ret = Collator.getInstance(env.getLocale()).compare(p, q);
			}
			else
				ret = 1;
		}
		else
		{
			if (q == null)
				ret = 0;
			else
				ret = -1;
		}
		return ret;
	}

	public boolean isBoolean()
	{
		return true;
	}

	public boolean hasMofifiedAccess()
	{
		return left.hasMofifiedAccess() || right.hasMofifiedAccess();
	}

	public void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect)
	{
		left.i18nKeys(collect);
		right.i18nKeys(collect);
	}

	public void getUsedAccessors(Set<JoriaAccess> ret)
	{
		left.getUsedAccessors(ret);
		right.getUsedAccessors(ret);
	}

	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		return left.hasText(text, searchLabels, searchData) || right.hasText(text, searchLabels, searchData);
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		left.collectVariables(set, seen);
		right.collectVariables(set, seen);
	}

}
