// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Set;


public abstract class BinaryOperatorNode extends Node
{
	// fields
	protected NodeInterface left;
	protected NodeInterface right;
	protected int type;

	public BinaryOperatorNode(int p0, NodeInterface p1, NodeInterface p2)
	{
		left = p1;
		right = p2;
		type = p0;
	}

	public int getTypeCode()
	{
		return type;
	}

	public boolean isBoolean()
	{
		return type == NodeInterface.booleanType;
	}

	public boolean isCharacter()
	{
		return type == NodeInterface.charType;
	}

	public boolean isCollection()
	{
		return type == NodeInterface.collectionType || type == NodeInterface.dictionaryType;
	}

	public boolean isLiteralCollection()
	{
		return type == NodeInterface.literalCollectionType;
	}

	public boolean isDictionary()
	{
		return type == NodeInterface.dictionaryType;
	}

	public boolean isInteger()
	{
		return type == NodeInterface.intType;
	}

	public boolean isReal()
	{
		return type == NodeInterface.realType || type == NodeInterface.intType;
	}

	public boolean isObject()
	{
		return type == NodeInterface.objectType;
	}

	public boolean isString()
	{
		return type == NodeInterface.stringType;
	}

	public boolean isDate()
	{
		return type == NodeInterface.dateType;
	}
	public boolean isBlob()
	{
		return type == NodeInterface.blobType;
	}

	public boolean hasMofifiedAccess()
	{
		return left.hasMofifiedAccess() || right.hasMofifiedAccess();
	}

	public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException
	{
		left.cacheDeferredFields(env, from);
		right.cacheDeferredFields(env, from);
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
