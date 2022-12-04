// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

//User: hmf
//Date: Jan 31, 2002 11:58:37 AM

public class IndexOpNode extends BinaryOperatorNode implements JoriaTypedNode
{

	public IndexOpNode(int p0, JoriaTypedNode p1, NodeInterface p2)
	{
		super(p0, p1, p2);
	}

	public String getTokenString()
	{
		return left.getTokenString() + '[' + right.getTokenString() + ']';
	}
	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 11;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append("[");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append("]");
		optBrace(bindingLevel, newLevel, collector, ')');
	}

	public JoriaType getElementType()
	{
		if (isCollection())
			return ((JoriaCollection) ((JoriaTypedNode) left).getType()).getElementType();
		else if (isLiteralCollection())
			return ((JoriaLiteralCollection) ((JoriaTypedNode) left).getType()).getElementLiteralType();
		else
			return null;
	}

	public boolean isObject()
	{
		return true;
	}

	public boolean isCollection()
	{
		return ((JoriaTypedNode) left).getElementType().isCollection();
	}

	public boolean isLiteralCollection()
	{
		return ((JoriaTypedNode) left).getElementType().isLiteralCollection();
	}

	public boolean isDictionary()
	{
		return ((JoriaTypedNode) left).getElementType().isDictionary();
	}

	public JoriaType getType()
	{
		return ((JoriaTypedNode) left).getElementType();
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		DBCollection c = (DBCollection) left.getValue(env, p0);
		if (c == null || c.isNull() || c.getLength() <= 0)
			return null;
		c.reset();
		int ix = (int) right.getIntValue(env, p0);
        if(ix == DBInt.NULL)
            return null;
		if (ix >= c.getLength() || ix < 0)
			return null;
		for (int cnt = 0;  cnt <= ix; cnt++)
            c.next();
		DBData ret =  c.current();
        c.reset();
        return ret;
	}
}
