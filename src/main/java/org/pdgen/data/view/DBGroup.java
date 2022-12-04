// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;


import java.util.Hashtable;

public class DBGroup implements DBCollectionCache
{
	CollectionValueAccess myAccess;
	DBData myKey;
	DBGroupValues myValues;
	Hashtable<Object, Object> cache;

	public DBGroup(CollectionValueAccess access, DBData key, DBGroupValues values)
	{
		myAccess = access;
		myKey = key;
		myValues = values;
	}

	public JoriaAccess getAccess()
	{
		return myAccess;
	}

	public boolean isNull()
	{
		return false;//
	}

	public DBData getKeyValue()
	{
		return myKey;
	}

	public DBGroupValues getGroupValues()
	{
		return myValues;
	}

	public JoriaType getActualType()
	{
		return myAccess.getType();
	}

	public boolean isAssignableTo(JoriaType t)
	{
		return myAccess.getType() == t;
	}

    public boolean isValid()
    {
        myValues.reset();
        while(myValues.next())
        {
            DBObject o = myValues.current();
            if(!o.isValid())
                return false;
        }
        return true;
    }

    public boolean same(DBData theOther)
	{
		return false;
	}

	public void addAggregate(AggregateKey axs, Object agg)
	{
		if (cache == null)
			cache = new Hashtable<Object, Object>();
		cache.put(axs, agg);
	}

	public void addDBDataToCache(JoriaAccess key, DBData data)
	{
	}

	public DBData getCachedDBData(JoriaAccess key)
	{
		return null;
	}

	public void clearCache()
	{
		if (cache != null)
			cache.clear();
	}

	public void addCollectionToCache(DBCollection d, JoriaAccess key)
	{
		if (cache == null)
			cache = new Hashtable<Object, Object>();
		cache.put(key, d);
	}

	public Object getCachedAggregate(AggregateKey axs)
	{
		if (cache == null)
			return null;
		return cache.get(axs);
	}

	public DBCollection getCachedCollectionValue(JoriaAccess key)
	{
		if (cache == null)
			return null;
		return  (DBCollection) cache.get(key);
	}

	public String toString()
	{
		return super.toString();
		//return myKey.toString();
	}
}
