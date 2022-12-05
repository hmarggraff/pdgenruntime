// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.oql.OQLParseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class FlatteningAccess extends MutableAccess {
    private static final long serialVersionUID = 7L;
    private JoriaAccess inner;

    public FlatteningAccess(JoriaClass parent, JoriaAccess access, JoriaAccess inner) {
        super(parent, access);
        this.inner = inner;
        FlattenenedElementClass eltype = new FlattenenedElementClass(access, inner);
        type = new DefaultJoriaCollection(eltype);
        ((DefaultJoriaCollection) type).setName(Res.asis("Coll_") + eltype.getName());
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        String filter = asView.getCascadedOQLFilterString();
        OQLNode checker = null;
        if (filter != null) {
            try {
                checker = Env.instance().parseUnparented(filter, asView.getType(), true);
            } catch (OQLParseException ex) {
                ex.printStackTrace();
                throw new JoriaAssertionError("Uncaught OQL Error detected at runtime");
            }
        }
        DBData coll = super.getValue(from, myBaseAccess, env);
        DBCollection outer = (DBCollection) coll;
        ArrayList<DBObject> all = new ArrayList<>();
        outer.reset();
        JoriaType innerType = inner.getCollectionTypeAsserted().getElementType();
        while (innerType != null && innerType.isView()) {
            innerType = ((ImmutableView) innerType).getBase();
        }
        while (outer.next()) {
            DBObject ov = outer.current();
            DBCollection icoll = (DBCollection) inner.getValue(ov, this, env);
            if (icoll == null || icoll.isNull())
                continue;
            icoll.reset();
            while (icoll.next()) {
                DBObject iv = icoll.current();
                if (innerType != null && !iv.isAssignableTo(innerType))
                    continue;
                FlattenedDBObject fv = new FlattenedDBObject(this, ov, iv);
                if (checker == null || checker.getBooleanValue(env, fv))
                    all.add(fv);
            }
            icoll.reset();
        }
        outer.reset();
        JoriaCollection t1 = asView.getSourceCollection();
        Trace.check(t1, "Access of a java collection must have a source collection: " + asView.getLongName());
        SortOrder[] sortRules = AbstractJoriaAccess.getSorting(t1, env);
        if (sortRules != null) {
            Comparator<DBData> comp = new ProjectionComparator(sortRules, env);
            all.sort(comp);
        }
        return new FilteredDBCollection(all, this, getType());
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied) {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;

        FlatteningAccess ret = new FlatteningAccess(newParent, myBaseAccess, inner);
        alreadyCopied.put(this, ret);

        fillDup(ret, alreadyCopied);
        return ret;
    }

    protected FlatteningAccess(JoriaClass parent, String name) {
        super(parent, name);
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {
        super.collectVariables(s, seen);
        if (inner instanceof VariableProvider)
            ((VariableProvider) inner).collectVariables(s, seen);
    }

    public void unbind() {
        super.unbind();
        if (inner instanceof Rebindable) {
            Rebindable rebindable = (Rebindable) inner;
            rebindable.unbind();
        } else
            inner = new UnboundAccessSentinel(inner);
    }

    public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        // this is probably too naive
        if (!((UnboundAccessSentinel) myBaseAccess).isBindable(newParentBinding))
            return false;
        if (inner instanceof UnboundAccessSentinel)
            return ((UnboundAccessSentinel) inner).isBindable(newBinding);
        else
            return ((Rebindable) inner).bindableTo(newBinding, newParentBinding);
    }

    @Override
    public String getCascadedHostFilter() {
        JoriaCollection p = getSourceCollection();
        if (p.getFilter() != null && p.getFilter().getHostFilterString() != null) {
            return p.getFilter().getHostFilterString();
        }
        return null;
    }

    public String getCascadedOQLFilterString() {
        JoriaCollection p = getSourceCollection();
        if (p.getFilter() != null && p.getFilter().getOqlString() != null) {
            return p.getFilter().getOqlString();
        }
        return null;
    }

    public OQLNode getCascadedOQLFilter() throws OQLParseException {
        JoriaCollection p = getSourceCollection();
        if (p.getFilter() != null && p.getFilter().getOqlString() != null) {
            return super.getCascadedOQLFilter();
        } else
            return null;
    }

}
