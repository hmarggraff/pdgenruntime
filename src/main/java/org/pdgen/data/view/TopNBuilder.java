// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.model.run.RunEnv;
import org.pdgen.data.DBCollection;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class TopNBuilder
{
	ProjectionComparator comp;
    int topN;
	DBObject min;

	public TopNBuilder(int topN, SortOrder[] so, RunEnv env)
	{
		this.topN = topN;
		if (so != null)
			comp = new ProjectionComparator(so, env);
	}

	/**
	 * extracts the topN froma collection that is already filtered
	 * @param coll
	 * @return sorted collection of topN elements
	 * @throws JoriaDataException
	 */

	public static List<DBObject> extractTopN(DBCollection coll, int topN, ProjectionComparator comp) throws JoriaDataException
	{
		DBObject min;
		ArrayList<DBObject> shortList = new ArrayList<DBObject>(topN);
		int cnt = 0;
		while (cnt < topN && coll.next())
		{
			shortList.add(coll.current());
			cnt++;
		}
		if (cnt < topN) // collection is shorter than topN
		{
			coll.reset();
			Collections.sort(shortList, comp);
			return shortList;
		}
		else
		{
			Collections.sort(shortList, comp);
			min = shortList.get(shortList.size()-1);
		}
		while(coll.next())
		{
			DBObject o = coll.current();
			final int diff = comp.compare(o, min);
			if (diff < 0)
			{
				if (shortList.size() >= topN)
					shortList.remove(shortList.size()-1);  // remove last
				boolean mustAdd = true;
				for (int i = 0; i < shortList.size(); i++) // insert in reverse order
				{
					DBObject old = shortList.get(i);
					final int oComp = comp.compare(o, old);
					if (oComp < 0)
					{
						shortList.add(i, o);
						min = shortList.get(shortList.size()-1);
						mustAdd = false;
						break;
					}
				}
				if (mustAdd)
				{
					shortList.add(o);
					min = o;
				}
			}
		}
		coll.reset();
		return shortList;
	}

	public void addTopN(List<DBObject> shortList, DBObject o)
	{
		final int diff;
		if (shortList.size() < topN)
			diff = -1;  // always add until topN elements are reached
		else
			diff = comp.compare(o, min);
		if (diff < 0)
		{
			if (shortList.size() >= topN)
				shortList.remove(shortList.size()-1);  // remove last
			boolean addAtEnd = true;
			for (int i = 0; i < shortList.size(); i++)
			{
				DBObject old = shortList.get(i);
				final int oComp = comp.compare(o, old);
				if (oComp < 0)
				{
					shortList.add(i, o);
					min = shortList.get(shortList.size()-1);
					addAtEnd = false;
					break;
				}
			}
			if (addAtEnd)
			{
				shortList.add(o);
				min = o;
			}
		}
	}
}
