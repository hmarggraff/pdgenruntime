// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.*;

import java.util.*;

public class DefaultAccess extends AbstractTypedJoriaMember implements IndirectAccess, VariableProvider, JoriaAccessTyped, CollectUsedViewsAccess, JoriaAccess {

    private static final long serialVersionUID = 7L;
    protected JoriaAccess myBaseAccess;

    public DefaultAccess(JoriaClass parent, JoriaType aType, JoriaAccess access) {
        super(parent, access.getName(), aType);
        myBaseAccess = access;
    }

    public DefaultAccess(JoriaClass parent, JoriaAccess base) {
        super(parent, base.getName(), base.getType());
        myBaseAccess = base;
    }

    public DefaultAccess(JoriaAccess base) {
        super(base.getDefiningClass(), base.getName(), base.getType());
        myBaseAccess = base;
    }

    public DefaultAccess(JoriaAccess base, JoriaType typ) {
        super(base.getDefiningClass(), base.getName(), typ);
        myBaseAccess = base;
    }

    public void setBaseAccess(JoriaAccess newBaseAccess) {
        type = newBaseAccess.getType();
        myBaseAccess = newBaseAccess;
        makeName();
    }

    /**
     * this contructor for coping a report. Because of circular references, the baseAccess cannot be set now.
     *
     * @param parent JoriaClass for this access
     * @param name   Name for this access, will be reset when access is set later
     */
    protected DefaultAccess(JoriaClass parent, String name) {
        super(parent, name);
    }

    public void setType(JoriaType newType) {
        type = newType;
        makeName();
    }

    protected void makeName(String tag) {
        String ts = tag != null ? "_" + tag : "";
        name = myBaseAccess.getName() + ts;
        longName = name + ": " + type.getName();
    }

    public JoriaAccess getBaseAccess() {
        return myBaseAccess;
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {
        if (myBaseAccess instanceof VariableProvider)
            ((VariableProvider) myBaseAccess).collectVariables(s, seen);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen) {
        if (myBaseAccess instanceof VariableProvider)
            ((VariableProvider) myBaseAccess).collectI18nKeys2(s, seen);
        if (type instanceof VariableProvider)
            ((VariableProvider) type).collectI18nKeys2(s, seen);
    }

    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
        if (seen.contains(this))
            return;
        else
            seen.add(this);
        if (type instanceof VariableProvider) {
            ((VariableProvider) type).collectVisiblePickersInScope(collection, visible, pathStack, seen);
        }
		/* TODO Eigentlich brauchen wir so etwas
				if (myBaseAccess instanceof VariableProvider)
				{
					((VariableProvider) myBaseAccess).collectVisiblePickersInScope(collection, visible, pathStack, seen);
				}
				*/
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        return myBaseAccess.getValue(from, asView, env);
    }

    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getIntValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d != null && !d.isNull())
            return ((DBInt) d).getIntValue();
        else
            return Long.MIN_VALUE + 1;
    }

    public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getFloatValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d != null && !d.isNull())
            return ((DBReal) d).getRealValue();
        else
            return Double.NaN;
    }

    public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getBooleanValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d == null || d.isNull())
            return JoriaAccessTyped.ValForNull;
        return ((DBBoolean) d).getBooleanValue() ? JoriaAccessTyped.ValForTrue : JoriaAccessTyped.ValForFalse;
    }

    public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getPictureValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d == null || d.isNull())
            return null;
        return ((DBImage) d).getData();
    }

    public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getStringValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d != null && !d.isNull())
            return d.toString();
        else
            return null;
    }

    public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException {
        if (myBaseAccess instanceof JoriaAccessTyped)
            return ((JoriaAccessTyped) myBaseAccess).getDateValue(from, env);
        DBData d = myBaseAccess.getValue(from, this, env);
        if (d == null || d.isNull())
            return null;
        return ((DBDateTime) d).getDate();
    }

    public void setName(String val) {
        name = val;
    }

    public void makeName() {
        name = myBaseAccess.getName();
        makeLongName();
    }

    public OQLNode getCascadedOQLFilter() throws OQLParseException {
        OQLNode bFilter = getBaseAccess().getCascadedOQLFilter();
        JoriaCollection baseCollection = getBaseAccess().getSourceCollection();
        JoriaCollection p = getSourceCollection();
        if (p != baseCollection && p.getFilter() != null && p.getFilter().getOqlString() != null) {
            OQLNode f = super.getCascadedOQLFilter();
            if (f != null && bFilter != null)
                return new AndNode(NodeInterface.booleanType, (Node) bFilter, (Node) f);
            else if (f != null)
                return f;
            else
                return bFilter;
        } else
            return bFilter;
    }

    @Override
    public String getCascadedHostFilter() {
        String bFilter = getBaseAccess().getCascadedHostFilter();
        JoriaCollection p = getSourceCollection();
        if (p.getFilter() != null && p.getFilter().getHostFilterString() != null) {
            String f = p.getFilter().getHostFilterString();
            if (bFilter != null) {
                return "(" + bFilter + Res.asis(")and(") + f + ")";
            } else
                return f;
        }
        return bFilter;
    }

    public String getCascadedOQLFilterString() {
        String bFilter = getBaseAccess().getCascadedOQLFilterString();
        JoriaCollection p = getSourceCollection();
        if (p.getFilter() != null && p.getFilter().getOqlString() != null) {
            String f = p.getFilter().getOqlString();
            if (bFilter != null) {
                return "(" + bFilter + Res.asis(")and(") + f + ")";
            } else
                return f;
        }
        return bFilter;
    }

    public void getCascadedOQLFilterList(List<Filter> collector) {
        getBaseAccess().getCascadedOQLFilterList(collector);
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null && !collector.contains(p.getFilter()))
            collector.add(p.getFilter());
    }

    public JoriaAccess getPlaceHolderIfNeeded() {
        JoriaAccess fixed = myBaseAccess.getPlaceHolderIfNeeded();
        if (fixed instanceof JoriaPlaceHolderAccess) {
            return new JoriaPlaceHolderAccess(name, fixed.getLongName() + "->" + ((JoriaPlaceHolderAccess) fixed).getInfo());
        } else if (fixed != null) {
            myBaseAccess = fixed;
        }
        return checkTypeForSchemaChange();
    }

    public boolean isRoot() {
        return myBaseAccess.isRoot();
    }

    public boolean isAccessTyped() {
        return myBaseAccess.isAccessTyped();
    }

    public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews) {
        if (type instanceof MutableView) {
            Repository.addViewUsage(viewUsage, (MutableView) type, this);
            ((MutableView) type).collectViewUsage(viewUsage, visitedViews);
        }
        if (myBaseAccess instanceof CollectUsedViewsAccess)
            ((CollectUsedViewsAccess) myBaseAccess).collectViewUsage(viewUsage, visitedViews);
    }

    public void collectUsedViews(Set<MutableView> s) {
        if (type instanceof MutableView)
            ((MutableView) type).collectUsedViews(s);
        if (myBaseAccess instanceof CollectUsedViewsAccess)
            ((CollectUsedViewsAccess) myBaseAccess).collectUsedViews(s);
    }

    public void unbind() {
        myBaseAccess = new UnboundAccessSentinel(myBaseAccess);
        if (type instanceof Rebindable) {
            Rebindable rebindable = (Rebindable) type;
            rebindable.unbind();
        } else if (!type.isLiteral())
            Trace.logWarn("DefaultAccess.unbind type not rebindable: " + type);
    }

    public boolean unbound() {
        return myBaseAccess instanceof UnboundAccessSentinel;
    }

    public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        return myBaseAccess instanceof UnboundAccessSentinel && ((UnboundAccessSentinel) myBaseAccess).isBindable(newBinding);
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        Trace.check(bindableTo(newBinding, newParentBinding));
        rebindInner(newBinding, newParentBinding);
    }

    void rebindInner(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        myBaseAccess = newBinding;
        if (type instanceof Rebindable) {
            Rebindable rebindable = (Rebindable) type;
            rebindable.rebind(newBinding, newParentBinding);
        } else
            type = newBinding.getType();
        makeName();
    }

    public JoriaCollection getSourceCollection() {
        if (getType() instanceof JoriaCollection)
            return (JoriaCollection) getType();
        else
            return getBaseAccess().getSourceCollection();
    }

    public JoriaAccess getRootAccess() {
        return getBaseAccess();
    }

    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (seen.contains(this))
            return true;
        seen.add(this);
        return visitor.visit(myBaseAccess) &&
                (!(myBaseAccess instanceof VisitableAccess) ||
                        ((VisitableAccess) myBaseAccess).visitAllAccesses(visitor, seen));
    }

    JoriaAccess dupBaseAccess(JoriaClass newParent, Map<Object, Object> alreadyCopiedViews) {
        if (myBaseAccess instanceof NameableAccess)
            return ((NameableAccess) myBaseAccess).dup(newParent, alreadyCopiedViews);
        return myBaseAccess;
    }

}
