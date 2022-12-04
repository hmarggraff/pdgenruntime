// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.view.GroupingAccess;
import org.pdgen.data.JoriaAccess;

public class RegroupTempAccess extends PseudoAccess
{
    private static final long serialVersionUID = 7L;
    private final GroupingAccess inner;
	private final int ix;
	private final JoriaAccess key;

	public RegroupTempAccess(GroupingAccess inner, int ix)
	{
		super(inner.getKeyAccess().getType(), inner.getKeyAccess().getBaseAccess().getName());
		this.inner = inner;
		this.ix = ix;
		key = inner.getKeyAccess().getBaseAccess();
	}

	public GroupingAccess getInner()
	{
		return inner;
	}

	public int getIx()
	{
		return ix;
	}

	public JoriaAccess getKey()
	{
		return key;
	}
}
