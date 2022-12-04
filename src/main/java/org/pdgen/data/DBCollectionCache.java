// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * Creation date: (30.3.00 15:10:40)
 */
public interface DBCollectionCache extends DBObject
{
	void addCollectionToCache(DBCollection d, JoriaAccess key);
	DBCollection getCachedCollectionValue(JoriaAccess key);

	Object getCachedAggregate(AggregateKey axs);
	void addAggregate(AggregateKey axs, Object agg);

    void addDBDataToCache(JoriaAccess key, DBData data);
    DBData getCachedDBData(JoriaAccess key);

	void clearCache();
}
