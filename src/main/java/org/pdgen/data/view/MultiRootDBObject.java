// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.AbstractDBObject;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaType;
import org.pdgen.data.JoriaAccess;

public class MultiRootDBObject extends AbstractDBObject
{
	public MultiRootDBObject(JoriaAccess axs)
	{
		super(axs);
	}

	public boolean isNull()
	{
		return false;
	}

	public boolean same(DBData theOther)
	{
		return false;
	}

	public boolean isAssignableTo(JoriaType t)
	{
		return false;
	}
}
