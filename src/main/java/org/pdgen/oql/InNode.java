// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class InNode extends BinaryOperatorNode
{
	public InNode(NodeInterface l, NodeInterface r)
	{
		super(booleanType, l, r);
	}

	public boolean getBooleanValue(RunEnv env, DBData parm1) throws JoriaDataException
	{
		DBData o = left.getValue(env, parm1);
		if (o == null || o.isNull())
		{
			return false;
		}
		DBData rv = right.getValue(env, parm1);
		if (right.isDictionary())
		{
			DBDictionary dbd = (DBDictionary) rv;
			return dbd.getValueForKey(o) != null;
		}
		else if (right.isLiteralCollection())
		{
			DBLiteralCollection dbc = (DBLiteralCollection) rv;
			return dbc != null && dbc.contains(o);
		}
		else
		{
			DBCollection dbc = (DBCollection) rv;
			dbc.reset();
			while (dbc.next())
			{
				DBData el = dbc.current();
				if (o.same(el))
					return true;
			}
			return false;
		}
	}

	public String getTokenString()
	{
		return left.getTokenString() + " in " + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 7;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" in ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }
}
