// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBBooleanImpl;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class AndNode extends BooleanOperatorNode
{

	public AndNode(int p0, NodeInterface p1, NodeInterface p2)
	{
		super(p0, p1, p2);
	}

	public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return left.getBooleanValue(env, p0) && right.getBooleanValue(env, p0);
	}

	public String getTokenString()
	{
		return left.getTokenString() + " and " + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 2;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" and ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, left.getBooleanValue(env, p0) && right.getBooleanValue(env, p0));
    }
}
