// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBBooleanImpl;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class NotNode extends UnaryOperatorNode
{
	public NotNode(NodeInterface p0)
	{
		super(p0);
	}

	public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return !sub.getBooleanValue(env, p0);
	}

	public String getTokenString()
	{
		return "not " + sub.getTokenString();
	}
	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 8;
		optBrace(bindingLevel, newLevel, collector, '(');
		collector.append(" not ");
		sub.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }

	public boolean isBoolean()
	{
		return true;
	}
}
