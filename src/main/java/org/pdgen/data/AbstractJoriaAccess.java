// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.oql.OQLParseException;

import java.util.HashMap;
import java.util.List;

public abstract class AbstractJoriaAccess implements JoriaAccess {
    private static final long serialVersionUID = 7L;
    protected String name;
    protected transient String longName;

    protected AbstractJoriaAccess(String name) {
        this.name = name;
    }

    protected AbstractJoriaAccess() {
    }

    /**
     * ----------------------------------------------------------------------- getDefiningClass
     */
    public JoriaClass getDefiningClass() {
        return null;
    }

    /**
     * ----------------------------------------------------------------------- isRoot
     */
    public boolean isRoot() {
        return true;
    }

    /**
     * ----------------------------------------------------------------------- getLongName
     */
    public String getLongName() {
        if (longName == null)
            makeLongName();
        return longName;
    }

    /**
     * ----------------------------------------------------------------------- getName
     */
    public String getName() {
        return name;
    }

    /**
     * ----------------------------------------------------------------------- getCollectionTypeAsserted
     * return the type as a collection type or throw an exception if it is not a collection type
     */
    public JoriaCollection getCollectionTypeAsserted() {
        if (getType().isCollection())
            return (JoriaCollection) getType();
        else
            throw new JoriaAssertionError("Collection typed access expected, found: " + getType().getName());
    }

    public String getXmlTag() {
        return getName();
    }

    public boolean isExportingAsXmlAttribute() {
        return getType().isLiteral();
    }

    /**
     * return the type as a class type or throw an exception if it is not a class type
     */
    public JoriaClass getClassTypeAsserted() {
        if (getType().isClass())
            return (JoriaClass) getType();
        else
            throw new JoriaAssertionError("Class typed access expected, found: " + getType().getName());
    }

    protected void makeNames(String baseName, String tag) {
        name = baseName + "_" + tag;
        makeLongName();
    }

    public void makeLongName() {
        JoriaType type = getType();
        if (type == null) // this may happen during a copy operation
            return;
        String tName = type.getName();
        if (tName == null) {
            if (type.isView()) {
                if (type.isCollection())
                    type = ((JoriaCollection) type).getElementType();
                if (type.isView() && type.getName() == null) {
                    tName = "viewOf_" + ((ClassView) type).getPhysicalClass().getName();
                } else
                    tName = "viewOf_" + type.getName();
            } else
                tName = "viewOf_" + type.getName();
            //throw new JoriaAssertionError("Physical class without name! Access: " + name);
        }
        longName = getName() + ": " + tName;
    }

    /**
     * ----------------------------------------------------------------------- toString
     */
    public String toString() {
        return getLongName();
    }

    public boolean isTransformer() {
        return false;
    }

    public boolean isAccessTyped() {
        return false;
    }

    public JoriaCollection getSourceCollection() {
        if (getType() instanceof JoriaCollection)
            return (JoriaCollection) getType();
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
        return getOQLFilter();
    }

    public String getCascadedHostFilter() {
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null)
            return p.getFilter().getHostFilterString();
        else
            return null;
    }

    protected OQLNode getOQLFilter() throws OQLParseException {
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null && p.getFilter().getOqlString() != null) {
            String filter = p.getFilter().getOqlString();
            return Env.instance().parseUnparented(filter, p, true);
        } else
            return null;
    }

    public void getCascadedOQLFilterList(List<Filter> collector) {
        JoriaCollection p = getSourceCollection();
        if (p != null && p.getFilter() != null && !collector.contains(p.getFilter()))
            collector.add(p.getFilter());
    }

    public JoriaAccess getPlaceHolderIfNeeded() {
        // null indicates that there is no change and no place holder is needed
        return null;
    }

    public static JoriaType getSourceTypeForChildren(JoriaAccess a) {
        if (a instanceof NameableAccess) {
            NameableAccess na = (NameableAccess) a;
            return na.getSourceTypeForChildren();
        }
        JoriaType type = a.getType();
        if (type.isCollection())
            return a.getCollectionTypeAsserted().getElementType();
        else if (type.isClass())
            return type;
        else
            return a.getDefiningClass();
    }

    public static SortOrder[] getSorting(JoriaCollection t1, RunEnv env) {
        HashMap<JoriaCollection, SortOrder[]> runtimeOverrides = env.getRuntimeOverrides();
        if (runtimeOverrides != null) {
            SortOrder[] override = runtimeOverrides.get(t1);
            if (override != null)
                return override;
        }
        SortOrder[] sortRules;
        for (; ; ) {
            sortRules = t1.getSorting();
            if (sortRules != null || !t1.getElementType().isView())
                break;
            t1 = ((CollectionProjection) t1).getBase();
        }
        return sortRules;
    }
}
