// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.*;

/**
 * joins its parent collection with another collection from any root.
 */
public class CollJoinAccess extends AbstractTypedJoriaMember implements VariableProvider
{
    private static final long serialVersionUID = 7L;
    protected JoriaAccess root;
	protected JoriaAccess rightKeyAccess;
	protected JoriaAccess leftKeyAccess;
	private final JoriaAccess rightCollAxs;
	private final JoriaAccess leftCollAxs;
	boolean grouped;
	boolean inner_join;

	public CollJoinAccess(JoriaClass parent, JoriaAccess root, JoriaAccess rightCollAxs, final JoriaAccess rightKeyAccess, JoriaAccess leftCollAxs, final JoriaAccess leftKeyAccess, String initialName, final boolean grouped, final boolean inner_join)
	{
		super(parent, initialName);
		this.rightCollAxs = rightCollAxs;
		this.leftCollAxs = leftCollAxs;
		this.grouped = grouped;
		this.inner_join = inner_join;
		CollJoinElementClass elType = new CollJoinElementClass(leftCollAxs, rightCollAxs, grouped);
		type = new DefaultJoriaCollection(elType, "Joint_");
		this.root = root;
		this.rightKeyAccess = rightKeyAccess;
		this.leftKeyAccess = leftKeyAccess;
	}

	public DBData getValue(final DBData from, final JoriaAccess asView, final RunEnv env) throws JoriaDataException
	{
		if (from == null || from.isNull())
			return null;
		DBCollectionCache cc = (DBCollectionCache) from;
		final DBCollection ret = cc.getCachedCollectionValue(this);
		if (ret != null)
			return ret;
		// process join as hash join

		final DBData rootVal = root.getValue(null, root, env);
		final DBCollection rightColl;
		if (rightCollAxs instanceof PickAccess)
		{
			rightColl = (DBCollection) rightCollAxs.getValue(rootVal, rightCollAxs, env);
		}
		else
		{
			env.pushToObjectPath(from);
			rightColl = (DBCollection) rightCollAxs.getValue(rootVal, rightCollAxs, env);
			env.popFromObjectPath();
		}
		HashMap<DBData, List<DBObject>> joinMap = new HashMap<DBData, List<DBObject>>();
		rightColl.reset();
		while (rightColl.next())
		{
			final DBObject rightDBObject = rightColl.current();
			final DBData rightKeyVal = rightKeyAccess.getValue(rightDBObject, rightKeyAccess, env);
			List<DBObject> keyedColl = joinMap.get(rightKeyVal);
			if (keyedColl == null)  // maintain multi map
			{
				keyedColl = new ArrayList<DBObject>();
				joinMap.put(rightKeyVal, keyedColl);
			}
			keyedColl.add(rightDBObject);
		}
		ArrayList<DBObject> all = new ArrayList<DBObject>();
		final DBCollection leftColl = (DBCollection) leftCollAxs.getValue(from, leftCollAxs, env);
		leftColl.reset();
		while (leftColl.next())
		{
			final DBObject leftDBObject = leftColl.current();
			final DBData leftKeyVal = leftKeyAccess.getValue(leftDBObject, leftKeyAccess, env);
			final List<DBObject> rightObjects = joinMap.get(leftKeyVal);
			if (rightObjects != null || !inner_join)
			{
				if (grouped)
				{
					FilteredDBCollection group = null;
					if (rightObjects != null)
						group = new FilteredDBCollection(rightObjects, rightCollAxs, rightCollAxs.getType());
					FlattenedDBObject cp = new FlattenedDBObject(this, leftDBObject, group);
					all.add(cp);
				}
				else if (rightObjects == null)
				{
					FlattenedDBObject fv = new FlattenedDBObject(this, leftDBObject, null);
					all.add(fv);
				}
				else
				{
					for (DBObject rightObject : rightObjects)
					{
						FlattenedDBObject fv = new FlattenedDBObject(this, leftDBObject, rightObject);
						all.add(fv);
					}
				}
			}
		}
		final FilteredDBCollection res = new FilteredDBCollection(all, this, getType());
		cc.addCollectionToCache(res, this);
		return res;
	}

	public void collectVariables(final Set<RuntimeParameter> s, final Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectVariables(s, seen);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectVariables(s, seen);
		if(root instanceof VariableProvider)
		    ((VariableProvider)root).collectVariables(s, seen);
		if (type instanceof VariableProvider)
			((VariableProvider) type).collectVariables(s, seen);

	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectI18nKeys2(keySet, seen);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectI18nKeys2(keySet, seen);
		if(root instanceof VariableProvider)
		    ((VariableProvider)root).collectI18nKeys2(keySet, seen);
		if (type instanceof VariableProvider)
			((VariableProvider) type).collectI18nKeys2(keySet, seen);
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectVisiblePickersInScope(collection, visible, pathStack, seen);
		if(leftCollAxs instanceof VariableProvider)
		    ((VariableProvider)leftCollAxs).collectVisiblePickersInScope(collection, visible, pathStack, seen);
		if(root instanceof VariableProvider)
		    ((VariableProvider)root).collectVisiblePickersInScope(collection, visible, pathStack, seen);
		if (type instanceof VariableProvider)
			((VariableProvider) type).collectVisiblePickersInScope(collection, visible, pathStack, seen);
	}
}
