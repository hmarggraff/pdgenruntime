// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBData;
import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Env;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

//User: hmf
//Date: Apr 6, 2002 12:12:42 PM

public class LabeledMultiDBObject implements LabeledDBObjectBase, DBObject
{
	ArrayList<DBObject> objects;
	DBData label;

	public LabeledMultiDBObject(DBObject object, DBData label)
	{
		objects = new ArrayList<DBObject>();
		objects.add(object);
		this.label = label;
	}

	public String toString()
	{
		if (label != null)
			return label.toString();
		else
			return "null";
	}

	public JoriaAccess getAccess()
	{
		return null;
	}

	public JoriaType getActualType()
	{
		return null;
	}

    public boolean isAssignableTo(JoriaType t)
    {
        return false;
    }

    public boolean isValid()
    {
        for (DBObject dbObject : objects)
        {
            if (!dbObject.isValid())
                return false;
        }
        return true;
    }


    public boolean isNull()
	{
		return objects == null || objects.size() == 0;
	}

	public boolean same(DBData theOther)
	{
		return false;
	}

	 //TODO implement DBLiterals as comparable
	public int compareTo(LabeledDBObjectBase lo)
	{
		if (label == null || label.isNull())
		{
			if (lo.getLabel() == null || lo.getLabel().isNull())
				return 0;
			else
				return -1;
		}
		else if (lo.getLabel() == null || lo.getLabel().isNull())
			return 1;
		else
        {
            Locale locales = (Locale) Env.instance().getThreadLocalStorage().getMap().get(Locale.class);
            if(locales == null)
                locales = Env.instance().getCurrentLocale();
            Collator coll = Collator.getInstance(locales);
            return coll.compare(label.toString(), lo.getLabel().toString());
        }
	}

	public DBData getLabel()
	{
		return label;
	}

	public void add(DBObject object)
	{
		objects.add(object);
	}

	public ArrayList<DBObject> getObjects()
	{
		return objects;
	}
}
