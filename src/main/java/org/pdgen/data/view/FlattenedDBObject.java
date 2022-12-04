// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.AbstractDBObject;
import org.pdgen.data.DBData;
import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;

public class FlattenedDBObject extends AbstractDBObject
{
	DBObject outer;
	DBData inner;

	public FlattenedDBObject(JoriaAccess axs, DBObject outer, DBData inner)
	{
		super(axs);
		this.outer = outer;
		this.inner = inner;
	}

	public FlattenedDBObject(DBObject outer, DBData inner)
	{
		this.outer = outer;
		this.inner = inner;
	}

	public boolean isNull()
	{
		return false; 
	}

	public boolean same(DBData theOther)
	{
		return this == theOther;
	}

    public boolean isAssignableTo(JoriaType t)
    {
        return getActualType() == t;
    }
}
