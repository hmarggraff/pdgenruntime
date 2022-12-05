// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.util.Map;
import java.util.Set;

public class CastAccess extends MutableAccess implements Nameable {
    private static final long serialVersionUID = 7L;
    JoriaType castType;
    protected transient boolean isCentral;

    public CastAccess(JoriaAccess access) {
        super(access);
        makeLongName();
    }

    public CastAccess(JoriaAccess orig, JoriaType newType) {
        super(orig, newType);
        makeLongName();
    }

    public CastAccess(JoriaClass parent, JoriaAccess access) {
        super(parent, access);
        makeLongName();
    }

    public CastAccess(JoriaClass parent, JoriaType type, JoriaAccess access) {
        super(parent, type, access);
    }

    public CastAccess(JoriaClass parent, JoriaType type, JoriaAccess access, String uName) {
        super(parent, type, access, uName);
    }

    public JoriaType getCastType() {
        return castType;
    }

    public void setCastType(JoriaType t) {
        castType = t;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        DBObject d = (DBObject) super.getValue(from, asView, env);// must be a dbObject because they have an actual type
        if (castType != null) {
            //			if (d != null && d.getActualType() == castType)
            if (d != null && JoriaClassHelper.isAssignableFrom(d, castType))
                return d;
            else
                return null;
        } else
            return d;
    }

    public boolean isTransformer() {
        return true;
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied) {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;

        CastAccess ret = new CastAccess(newParent, type, myBaseAccess);
        alreadyCopied.put(this, ret);
        fillDup(ret, alreadyCopied);
        ret.castType = castType;
        return ret;
    }

    public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews) {
        super.collectViewUsage(viewUsage, visitedViews);
        if (castType != null && castType.isView()) {
            Repository.addViewUsage(viewUsage, (MutableView) castType, this);
            ((MutableView) castType).collectViewUsage(viewUsage, visitedViews);
        }
    }

    public void collectUsedViews(Set<MutableView> s) {
        super.collectUsedViews(s);
        if (castType != null && castType.isView())
            ((MutableView) castType).collectUsedViews(s);
    }

    public boolean hasName() {
        return true;
    }

    protected CastAccess(JoriaClass newParent, String name) {
        super(newParent, name);
    }

    public void unbind() {
        super.unbind();
        castType = null;
    }

}
