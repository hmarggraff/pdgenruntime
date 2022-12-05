// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

/**
 * Provides access to the elements of a top level collection (A collection valued root)
 */
public class JavaCollectionWrapperElements extends AbstractTypedJoriaMember {

    private static final long serialVersionUID = 7L;

    public JavaCollectionWrapperElements(JoriaClass definingClass, JoriaCollection typ) {
        super(definingClass, "elements", typ);
        makeLongName();
    }

    public void setType(JoriaType newType) {
        type = newType;
        makeLongName();
    }

    public String getBaseName() {
        return null;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        CollectionWrapperValue f = (CollectionWrapperValue) from;
        if (f == null)
            return null;
        DBCollection d = f.getCachedCollectionValue(asView);
        if (d == null) {
            Object rd = f.getValue();
            if (rd == null)
                return null;
            d = JavaMember.makeCollectionValue(rd, asView, asView.getSourceCollection(), env);
            f.addCollectionToCache(d, asView);
            return d;
        } else
            return d;
    }

    protected Object readResolve() {
        if (definingClass instanceof JoriaUnknownType) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.classNotFound, this, null);
        }
        JoriaAccess mem = definingClass.findMember(name);
        if (mem == null) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.memberNotFound, this, null);
        }
        if (type instanceof JoriaUnknownType || type != mem.getType()) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.typeChanged, this, mem);
        }
        return mem;
    }
}
