// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBBooleanImpl;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class TrueNode extends Node
{

	public boolean getBooleanValue(RunEnv env, DBData p0)
	{
		return true;
	}

	public String getTokenString()
	{
		return "true";
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		collector.append(" true ");
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, true);
    }

    /* ----------------------------------------------------------------------- isBoolean */
	public boolean isBoolean()
	{
		return true;
	}
}
