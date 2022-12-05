// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.MutableView;
import org.pdgen.env.Res;

/**
 * implements the most basic functions for a joria access
 * maintains name, longname and defining class
 * provides default implementations for getCollectionType and getClassType
 */
public abstract class AbstractTypedJoriaMember extends AbstractMember {
    private static final long serialVersionUID = 7L;
    protected JoriaType type;

    public AbstractTypedJoriaMember(JoriaClass definingClass) {
        super(definingClass);
    }

    public AbstractTypedJoriaMember(JoriaClass definingClass, String name) {
        super(definingClass, name);
    }

    public AbstractTypedJoriaMember(JoriaClass definingClass, String name, JoriaType typ) {
        super(definingClass, name);
        Trace.check(typ);
        type = typ;
    }

    public AbstractTypedJoriaMember() {
    }

    public JoriaType getType() {
        return type;
    }

    protected void makeNames(String baseName, String tag) {
        name = baseName + "_" + tag;
        makeLongName();
    }

    protected JoriaAccess checkTypeForSchemaChange() {
        if (type instanceof MutableView && type != definingClass) {
            MutableView mutableView = (MutableView) type;
            if (mutableView.fixAccess()) {
                JoriaPlaceHolderAccess joriaPlaceHolderAccess = new JoriaPlaceHolderAccess(name, type.getName() + Res.stri("affected_by_a_schema_change"));
                return joriaPlaceHolderAccess;
            }
        }
        return null;
    }

    public JoriaAccess getPlaceHolderIfNeeded() {
        return checkTypeForSchemaChange();
    }
}
