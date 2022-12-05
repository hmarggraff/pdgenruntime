// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.oql.OQLParseException;

import java.util.*;
import java.util.Map.Entry;

public class GroupingAccess extends MutableAccess implements JoriaReportPrivateAccess, GroupValueBaseAccess {
    //protected static final int intType = 0;
    //protected static final int floatType = 1;
    protected static final int stringType = 2;
    //protected static final int booleanType = 3;
    protected static final int objectType = 4;
    private static final long serialVersionUID = 7L;
    //protected static int nameCnt = 0;
    protected JoriaCollection groups;
    protected GroupKeyAccess myGroupKeyAxs;
    protected NameableAccess myGroupValueAxs;
    protected GroupClass groupType;
    protected SortOrder groupingDef;

    private GroupingAccess(JoriaClass parent, GroupingAccess from, Map<Object, Object> alreadyCopied) {
        super(parent, from.dupBaseAccess(parent, alreadyCopied));
        alreadyCopied.put(from, this);
        groupingDef = from.groupingDef.copy();
        MutableView newType = (MutableView) alreadyCopied.get(from.groupType);
        if (newType == null)
            newType = from.groupType.dup(alreadyCopied);
        groupType = (GroupClass) newType;
        type = new GroupCollection(groupType);
        myGroupKeyAxs = groupType.getGroupKeyAxs();
        myGroupValueAxs = groupType.getGroupValueAxs();
        name = from.name;
        longName = from.longName;
        xmlTag = from.xmlTag;
        xmlInline = from.xmlInline;
    }

    protected GroupingAccess(JoriaClass newParent, String name) {
        super(newParent, name);
    }

    public GroupingAccess(JoriaClass parent, JoriaAccess baseCollection, SortOrder grouping) {
        super(parent, baseCollection);
        init(grouping);
    }

    public GroupingAccess(JoriaAccess baseCollection, SortOrder grouping) {
        super(baseCollection);
        init(grouping);
    }

    private void init(SortOrder grouping) {
        groupingDef = grouping;
        makeName();
        JoriaCollection bc = myBaseAccess.getCollectionTypeAsserted();
        JoriaClass elc = bc.getElementType();
        //Trace.check(elc.getSchemaFileName() == groupingDef.baseAccess.getDefiningClass().getCurtain());   // relatives but fails if inheritance is involved
        groupType = new GroupClass(elc, null, true);
        type = new GroupCollection(groupType);
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied) {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;
        GroupingAccess ret = new GroupingAccess(newParent, this, alreadyCopied);
        alreadyCopied.put(this, ret);
        return ret;
    }

    public JoriaAccess copyReportPrivateAccess(Map<Object, Object> copiedData) {
        GroupingAccess ret = new GroupingAccess(null, name);
        ret.groupingDef = groupingDef.copy();
        if (myBaseAccess instanceof JoriaReportPrivateAccess) {
            JoriaReportPrivateAccess jrpa = (JoriaReportPrivateAccess) myBaseAccess;
            ret.myBaseAccess = jrpa.copyReportPrivateAccess(copiedData);
        }
        ret.groupType = groupType.copyReportPrivateGroupClass(copiedData, ret.myBaseAccess);
        ret.type = new GroupCollection(ret.groupType, (GroupCollection) type);
        ret.myGroupKeyAxs = ret.groupType.getGroupKeyAxs();
        ret.myGroupValueAxs = ret.groupType.getGroupValueAxs();
        ret.name = name;
        ret.longName = longName;
        ret.xmlTag = xmlTag;
        ret.xmlInline = xmlInline;
        ret.isNameSet = isNameSet;

        return ret;
    }

	/*
	public void setInnerCol(NameableAccess innerColl)
	{
		myGroupKeyAxs = new GroupKeyAccess(groupType, groupingDef.getBaseAccess());
		myGroupValueAxs = innerColl;
		groupType.setGroupKeyAxs(myGroupKeyAxs);
		groupType.setGroupValueAxs((GroupValueBaseAccess) myGroupValueAxs);
		makeName();
		xmlTag = name;
	}
	*/

    public void setInnerColView(NameableAccess innerColl) {
        if (groupingDef.getBaseAccess().getType().isClass())// we are grouping by objects: provide a view to edit the members
        {
            myGroupKeyAxs = new GroupKeyAccess(groupType, new CastAccess(groupingDef.getBaseAccess(), new ClassProjection(groupingDef.getBaseAccess().getClassTypeAsserted())));            //myGroupKeyAxs = new CastAccess(myGroupKeyAxs, new ClassProjection(groupingDef.baseAccess.getClassTypeAsserted()));
        } else {
            myGroupKeyAxs = new GroupKeyAccess(groupType, groupingDef.getBaseAccess());
        }
        myGroupValueAxs = innerColl;
        groupType.setGroupKeyAxs(myGroupKeyAxs);
        groupType.setGroupValueAxs((GroupValueBaseAccess) myGroupValueAxs);
        makeName();
        xmlTag = name;
    }

    public void makeName() {
        if (myBaseAccess == null)
            return;
        if (!isNameSet) {
            StringBuilder sb = new StringBuilder(getTag());
            sb.append("_");
            sb.append(myBaseAccess.getCollectionTypeAsserted().getElementType().getName());
            if (myBaseAccess != null)
                sb.append('_').append(myBaseAccess.getName());
            if (groupingDef != null)
                sb.append(Res.asis("_by_")).append(groupingDef.getBaseAccess().getName());
            name = sb.toString();
            name = name.replace('<', '_');
            name = name.replace('>', '_');
        }
        final String tname = type.getName();
        longName = name + ": " + tname;
    }

    @Override
    public String getCascadedHostFilter() {
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null)
            return p.getFilter().getHostFilterString();
        else
            return null;
    }

    public String getCascadedOQLFilterString() {
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null)
            return p.getFilter().getOqlString();
        else
            return null;
    }

    public OQLNode getCascadedOQLFilter() throws OQLParseException {
        return getOQLFilter();// this does not access filters defined on the base of this access		// this might cause a problem
    }

    public DBData getValue(DBData from, JoriaAccess asMember, RunEnv env) throws JoriaDataException {
        if (from instanceof DBCollectionCache) {
            DBCollection retVal = ((DBCollectionCache) from).getCachedCollectionValue(asMember);
            if (retVal != null)
                return retVal;
        }
        final DBCollection sc;
        if (from instanceof DBGroup) {
            DBGroup gr = (DBGroup) from;
            sc = gr.getGroupValues();
        } else {
            PickAccess pa = getFilteringAccess();
            sc = (DBCollection) myBaseAccess.getValue(from, pa, env);
        }
        if (sc == null || sc.isNull())
            return new DBGrouping(this, null, asMember.getType());
        HashMap<DBData, ArrayList<DBObject>> h = new HashMap<>();
        JoriaAccess groupingField = groupingDef.getBaseAccess();
        sc.reset();
        while (sc.next()) {
            DBObject o = sc.current();
            if (o == null || o.isNull())
                continue;
            DBData key = groupingField.getValue(o, myGroupKeyAxs, env);
            ArrayList<DBObject> l = h.get(key);
            if (l == null) {
                l = new ArrayList<>();
                h.put(key, l);
            }
            l.add(o);
        }        // now h containsName the groups
        final OQLNode groupLevelOQL;
        try {
            groupLevelOQL = asMember.getCascadedOQLFilter();
        } catch (OQLParseException pe) {
            throw new JoriaDataExceptionWrapped(Res.asis("Found an OQL error when it is too late. ") + pe.getMessage(), pe);
        }
        ArrayList<DBObject> sand = new ArrayList<>();
        Iterator<Entry<DBData, ArrayList<DBObject>>> it = h.entrySet().iterator();
        CollectionValueAccess cva = new CollectionValueAccess(this);
        while (it.hasNext()) {
            Entry<DBData, ArrayList<DBObject>> e = it.next();
            DBData key = e.getKey();
            DBObject v = new DBGroup(cva, key, new DBGroupValues(myGroupValueAxs, e.getValue(), sc.getActualType()));
            if (groupLevelOQL == null || groupLevelOQL.getBooleanValue(env, v))
                sand.add(v);
        }        //exp hmf 050308: does getSourceCollection return the sorting ok?		// was: final SortOrder[] sorting = asMember.getCollectionTypeAsserted().getSorting();
        final SortOrder[] sorting = AbstractJoriaAccess.getSorting(asMember.getSourceCollection(), env);
        final Comparator<DBData> comp;
        if (sorting != null)
            comp = new ProjectionComparator(sorting, env);
        else
            comp = new SingleComparator(groupingDef, env);
        Collections.sort(sand, comp);
        int topN = asMember.getSourceCollection().getMinTopN(env);
        if (topN > 0 && topN < sand.size()) {
            ArrayList<DBObject> trunc = new ArrayList<>(topN);
            for (int j = 0; j < topN; j++) {
                trunc.add(sand.get(j));
            }
            sand = trunc;
        }
        DBGrouping retVal = new DBGrouping(this, sand, sc.getActualType());
        if (from instanceof DBCollectionCache) {
            ((DBCollectionCache) from).addCollectionToCache(retVal, asMember);
        }
        return retVal;
    }

    public PickAccess getFilteringAccess() {
        NameableAccess filterholder = myGroupValueAxs;
        while (filterholder instanceof GroupingAccess) {
            filterholder = ((GroupingAccess) filterholder).myGroupValueAxs;
        }        //return (GroupValueAccess) filterholder;
        return new PickAccess(definingClass, filterholder, (MutableCollection) filterholder.getType());
    }

    public String getTag() {
        return Res.asis("grouping");
    }

    public void setParent(JoriaClass parent) {
        Trace.check(parent);
        definingClass = parent;
    }

    public GroupClass getGroupType() {
        return groupType;
    }

    public GroupKeyAccess getKeyAccess() {
        return myGroupKeyAxs;
    }

    public NameableAccess getValueAccess() {
        return myGroupValueAxs;
    }

    public JoriaAccess getGroupingAccess() {
        return groupingDef.getBaseAccess();
    }

    public void modifyGroupingKey(JoriaAccess newKey) {
        myGroupKeyAxs.modifyBase(newKey);
        groupingDef = new SortOrder(newKey);
        makeName();
        xmlTag = name;
    }

    public SortOrder getGrouping() {
        return groupingDef;
    }

    public JoriaType getSourceTypeForChildren() {
        return myBaseAccess.getType();
    }

    public void setExportingAsXmlAttribute(boolean newValue) {
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {        // may: super will recurse to us		//super.collectVariables(s);
        if (!(myBaseAccess instanceof GroupingAccess) && myBaseAccess instanceof VariableProvider) {
            ((VariableProvider) myBaseAccess).collectVariables(s, seen);
        }
        if (myGroupValueAxs instanceof VariableProvider)
            ((VariableProvider) myGroupValueAxs).collectVariables(s, seen);
        JoriaType tt = myGroupValueAxs.getType();
        if (tt instanceof VariableProvider)
            ((VariableProvider) tt).collectVariables(s, seen);
    }

    public void unbind() {
        super.unbind();
        myGroupKeyAxs.unbind();
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        super.rebind(newBinding, newParentBinding);        //myGroupValueAxs
    }

    public void setDefiningClass(GroupClass groupClass) {
        definingClass = groupClass;
    }

    public void setBaseAccessDirect(JoriaAccess base) {
        myBaseAccess = base;
        makeName();
    }
}
