// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.DBIntImpl;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class IntNode extends Node
{
	// fields
	protected long value;

	public IntNode(long p0)
	{
		value = p0;
	}

	public double getFloatValue(RunEnv env, DBData p0)
	{
		return value;
	}

	public long getIntValue(RunEnv env, DBData p0)
	{
		return value;
	}

	public String getTokenString()
	{
		return String.valueOf(value);
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		collector.append(value);
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBIntImpl(null, value);
    }

    public boolean isInteger()
	{
		return true;
	}

	public boolean isReal()
	{
		return true;
	}
}
