// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class GroupValueAccess extends MutableAccess implements GroupValueBaseAccess
{
    private static final long serialVersionUID = 7L;

    public GroupValueAccess(JoriaClass parent, MutableCollection type, JoriaAccess base)
	{
		super(parent, type, base);
		makeLongName();
	}

	private GroupValueAccess(JoriaClass newParent, String name)
	{
		super(newParent, name);
	}

    /**
	 * this getValue does not use its base access!. The base access is kept for reference only
	 */
	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		DBGroup gf = (DBGroup) from;
        final SortOrder[] sorting = AbstractJoriaAccess.getSorting(asView.getSourceCollection(), env);
        if (sorting != null)
        {
			final Comparator<DBData> comp = new ProjectionComparator(sorting, env);
			Collections.sort(gf.getGroupValues().values, comp);
        }
        int topN = asView.getSourceCollection().getMinTopN(env);
        if (topN > 0 && topN < gf.getGroupValues().values.size())
        {
            ArrayList<DBObject> trunc = new ArrayList<>(topN);
            for (int j = 0; j < topN; j++)
            {
                trunc.add(gf.getGroupValues().values.get(j));
            }
			gf.getGroupValues().values = trunc;
        }
		return gf.getGroupValues();
	}

	public NameableAccess dup(JoriaClass newParent, Map<Object,Object> alreadyCopied)
	{
		final Object duplicate = alreadyCopied.get(this);
		if (duplicate != null)
			return (NameableAccess) duplicate;

		//type is not duplicated, because ist refers to the underlying collection (physical or view)
        JoriaAccess baseAccess = myBaseAccess;
        if(alreadyCopied.get(myBaseAccess) != null)
            baseAccess = (JoriaAccess) alreadyCopied.get(myBaseAccess);
        GroupValueAccess ret = new GroupValueAccess(newParent, (MutableCollection) type, baseAccess);
		alreadyCopied.put(this, ret);
		fillDup(ret, alreadyCopied);
		return ret;
	}

    public void setDefiningClass(GroupClass groupClass)
    {
        definingClass = groupClass;
    }

	public JoriaAccess copyReportPrivateGroupValueAccess(final Map<Object, Object> copiedData, GroupClass newParent, final MutableCollection newType)
	{
		final Object duplicate = copiedData.get(this);
		if (duplicate != null)
			return (NameableAccess) duplicate;
		//type and base access are not duplicated, because ist refers to the underlying collection (physical or view)
		GroupValueAccess ret = new GroupValueAccess(newParent, newType, myBaseAccess);
		copiedData.put(this, ret);
		ret.setNameInternally(name);
		ret.isNameSet = isNameSet;
		ret.xmlInline = xmlInline;
		ret.xmlTag = xmlTag;
		ret.formatString = formatString;
		return ret;
	}
}
