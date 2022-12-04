// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.Map;
import java.util.Set;

public class IRootValuesAccess extends AbstractTypedJoriaMember implements CollectUsedViewsAccess
{
    private static final long serialVersionUID = 7L;

    public IRootValuesAccess(JoriaClass definingClass, JoriaAccess base)
	{
		super(definingClass, base.getName(), base.getType());
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		DBCollectionCache cv = (DBCollectionCache) from;
		return cv.getCachedCollectionValue(this); // this will look in the cache.
	}

    public void collectUsedViews(Set<MutableView> s)
    {
        if(type instanceof MutableView)
            ((MutableView)type).collectUsedViews(s);
        if(definingClass instanceof MutableView)
            ((MutableView)definingClass).collectUsedViews(s);
    }

    public void collectViewUsage(Map<MutableView,Set<Object>> viewUsage, Set<MutableView> visitedViews)
    {
        if(type instanceof MutableView)
        {
            Repository.addViewUsage(viewUsage, (MutableView) type, this);
            ((MutableView)type).collectViewUsage(viewUsage, visitedViews);
        }
        if(definingClass instanceof MutableView)
        {
            Repository.addViewUsage(viewUsage, (MutableView) definingClass, this);
            ((MutableView)definingClass).collectViewUsage(viewUsage, visitedViews);
        }
    }
}
