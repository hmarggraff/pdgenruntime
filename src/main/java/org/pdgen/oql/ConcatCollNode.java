// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.FilteredDBCollection;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.ComputedDBCollectionValue;
import org.pdgen.projection.PseudoAccess;


import java.util.ArrayList;

public class ConcatCollNode extends BinaryOperatorNode implements JoriaTypedNode
{
	JoriaCollection collType;

	public ConcatCollNode(int p0, NodeInterface p1, NodeInterface p2, JoriaClass elType)
	{
		super(p0, p1, p2);
		collType = new DefaultJoriaCollection(elType, elType.getName());
	}

	public DBCollection getCollection(RunEnv env, DBData d) throws JoriaDataException
	{
		DBCollection l = left.getCollection(env, d);
		DBCollection r = right.getCollection(env, d);
		if (l == null || l.isNull())
			return r;
		else if (r == null || r.isNull())
			return l;
		ArrayList<DBData> c = new ArrayList<DBData>(l.getLength() + r.getLength());
		if (l instanceof ComputedDBCollectionValue)
		{
			c.addAll(((ComputedDBCollectionValue) l).getList());
		}
		else
		{
			l.reset();
			while (l.next())
			{
				DBData el = l.current();
				c.add(el);
			}
			l.reset();
		}
		if (r instanceof ComputedDBCollectionValue)
		{
			c.addAll(((ComputedDBCollectionValue) r).getList());
		}
		else
		{
			l.reset();
			while (r.next())
			{
				DBData el = r.current();
				c.add(el);
			}
			l.reset();
		}
		return new ComputedDBCollectionValue(c, l.getAccess());
	}

	public String getTokenString()
	{
		return left.getTokenString() + '+' + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 5;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" + ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

	public JoriaType getElementType()
	{
		if (isCollection())
		{
			return collType.getElementType();
		}
		else
			throw new JoriaAssertionError("Unhandled type in plus node escaped parser");
	}

	public JoriaType getType()
	{
		return collType;
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		JoriaTypedNode lt = (JoriaTypedNode) left; // must have been checked when parsing
		JoriaTypedNode rt = (JoriaTypedNode) right; // must have been checked when parsing
		final DBCollection lc = lt.getCollection(env, p0);
		final DBCollection rc = rt.getCollection(env, p0);
		ArrayList<DBObject> rl = new ArrayList<DBObject>();
		if (lc != null && !lc.isNull())
		{
			lc.reset();
			rl.ensureCapacity(lc.getLength());
			while (lc.next())
			{
				rl.add(lc.current());
			}
		}
		if (rc != null && !rc.isNull())
		{
			rc.reset();
			rl.ensureCapacity(rl.size() + rc.getLength());
			while (rc.next())
			{
				rl.add(rc.current());
			}
		}
		FilteredDBCollection ret = new FilteredDBCollection(rl, new PseudoAccess(lt.getType()), lt.getType());
		return ret;
	}
}