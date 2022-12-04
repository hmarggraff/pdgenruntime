// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.IndirectAccess;
import org.pdgen.data.view.PickAccess;

import java.util.Hashtable;

public abstract class AbstractDBObject extends AbstractDBData implements DBCollectionCache {

	protected Hashtable<Object,Object> collectionCache;

	public AbstractDBObject(JoriaAccess axs)
	{
		super(axs);
	}

	public AbstractDBObject()
	{
	}


    public void clearCache()
    {
        if(collectionCache != null)
            collectionCache.clear();
    }

    public void addCollectionToCache(DBCollection d, JoriaAccess key)
	{
		if (key.getType().isCollection() && ((JoriaCollection)key.getType()).isLarge())
			return;
		if (collectionCache == null)
			collectionCache = new Hashtable<Object,Object>();
        key = getCacheKey(key);
        if (d != null)
            collectionCache.put(key, d);
	}

    public DBCollection getCachedCollectionValue(JoriaAccess key)
    {
        if (collectionCache == null)
            return null;
        key = getCacheKey(key);
        DBCollection dbCollection = (DBCollection) collectionCache.get(key);
        if(dbCollection != null)
            Trace.logDebug(Trace.view, "found in cache");
        return dbCollection;
    }

    public static JoriaAccess getCacheKey(JoriaAccess key)
    {
        while(key instanceof PickAccess)
        {
            JoriaCollection coll = key.getSourceCollection();
            if(coll.hasFilterOrSortingOrTopN())
                break;
            key = ((IndirectAccess)key).getBaseAccess();
        }
        return key;
    }

    public void addAggregate(AggregateKey axs, Object agg)
	{
		if (collectionCache == null)
			collectionCache = new Hashtable<Object,Object>();
		if (axs != null)
			collectionCache.put(axs, agg);
	}

	public Object getCachedAggregate(AggregateKey axs)
	{
		if (collectionCache == null)
			return null;
		return collectionCache.get(axs);
	}

    public void addDBDataToCache(JoriaAccess key, DBData data)
    {
	    if (collectionCache == null)
	        collectionCache = new Hashtable<Object,Object>();
	    if (key != null)
	        collectionCache.put(key, data);
    }

    public DBData getCachedDBData(JoriaAccess key)
    {
	    final Object result;
	    if(collectionCache == null)
		    result = null;
	    else
		    result = collectionCache.get(key);
	    return (DBData) result;
    }

	public boolean isValid()
    {
        return true;
    }
}
