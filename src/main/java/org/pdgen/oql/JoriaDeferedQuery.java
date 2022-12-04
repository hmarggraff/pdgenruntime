// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.DBDeferredQueryData;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;


import java.util.HashSet;


public class JoriaDeferedQuery extends JoriaQuery
{
	HashSet<DeferredFieldNode> deferredFields = new HashSet<DeferredFieldNode>();
    private final boolean needsAllPages;

    public JoriaDeferedQuery(JoriaClass p0, NodeInterface topNode, String query, boolean needsAllPages)
	{
		super(p0, topNode, query);
        this.needsAllPages = needsAllPages;
	}

    public DBData getValue(RunEnv env, DBData from) throws JoriaDataException
	{
		sub.cacheDeferredFields(env, from);
		return new DBDeferredQueryData(this);
	}

    public boolean isNeedsAllPages()
    {
        return needsAllPages;
    }

	public DBData getDeferredValue(final RunEnv env) throws JoriaDataException
	{
		return sub.getWrappedValue(env, null);
	}
}
