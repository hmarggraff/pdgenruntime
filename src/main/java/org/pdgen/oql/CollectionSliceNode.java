// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.ComputedDBCollectionValue;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionSliceNode extends BinaryOperatorNode implements JoriaTypedNode
{
	NodeInterface end;// the index that returns the end of the slice

	public CollectionSliceNode(final JoriaTypedNode collectionNode, final NodeInterface startIndex, final NodeInterface endIndex)
	{
		super(NodeInterface.collectionType, collectionNode, startIndex);
		end = endIndex;
	}

	public String getTokenString()
	{
		return left.getTokenString() + '[' + right.getTokenString() + " .. " + end.getTokenString()+ ']';
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 11;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append("[");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" .. ");
		end.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append("]");
		optBrace(bindingLevel, newLevel, collector, ')');
	}


	public JoriaType getElementType()
	{
		if (isLiteralCollection())
			return ((JoriaLiteralCollection) ((JoriaTypedNode) left).getType()).getElementLiteralType();
		else if (isCollection())
			return ((JoriaCollection) ((JoriaTypedNode) left).getType()).getElementType();
		else
			return null;
	}

	public boolean isObject()
	{
		return true;
	}

	public boolean isCollection()
	{
		return true;
	}

	public boolean isLiteralCollection()
	{
		return left.isLiteralCollection();
	}

	public boolean isDictionary()
	{
		return false;
	}

	public JoriaType getType()
	{
		return ((JoriaTypedNode) left).getType();
	}

	public DBCollection getCollection(RunEnv env, DBData d) throws JoriaDataException
	{
		DBCollection src = (DBCollection) left.getValue(env, d);
		final int srcLen;
		if (src == null || src.isNull() || (srcLen = src.getLength()) <= 0)
			return null;
		src.reset();
		int startIx = (int) right.getIntValue(env, d);
		if (startIx == DBInt.NULL)
			return null;
		if (startIx >= srcLen || startIx < 0)
			return null;
		int endIx = (int) end.getIntValue(env, d);
		if (endIx == DBInt.NULL)
			return null;
		if (endIx < 0)
			return null;
		if (endIx >= srcLen)
			endIx = srcLen - 1;
		int len = endIx - startIx;
		if (len <= 0)
			return null;
		ArrayList<DBData> c = new ArrayList<DBData>(len);
		if (src instanceof ComputedDBCollectionValue)
		{
			final List<DBData> srcList = ((ComputedDBCollectionValue) src).getList();
			srcList.subList(startIx, endIx);
			c.addAll(srcList);
		}
		else
		{
			src.reset();
			int at = 0;
			while (src.next())
			{
				DBData el = src.current();
				if (at >= startIx)
					c.add(el);
				at++;
				if (at >= endIx)
					break;
			}
			src.reset();
		}
		return new ComputedDBCollectionValue(c, src.getAccess());
	}

	public DBData getValue(RunEnv env, DBData d) throws JoriaDataException
	{
		return getCollection(env, d);
	}

	@Override
	public void getUsedAccessors(final Set<JoriaAccess> ret)
	{
		super.getUsedAccessors(ret);
		end.getUsedAccessors(ret);
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		super.collectVariables(set, seen);
		end.collectVariables(set, seen);
	}

	@Override
	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		return super.hasText(text, searchLabels, searchData) || end.hasText(text, searchLabels, searchData);
	}
}
