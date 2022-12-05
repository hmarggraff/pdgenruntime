// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

public class FlattenedElementAccess extends AbstractTypedJoriaMember {
    private static final long serialVersionUID = 7L;
    boolean inner_outer;

    public FlattenedElementAccess(JoriaClass definingClass, String name, JoriaType typ, boolean inner_outer) {
        super(definingClass, name, typ);
        this.inner_outer = inner_outer;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        FlattenedDBObject fo = (FlattenedDBObject) from;

        if (inner_outer)
            return fo.inner;
        else
            return fo.outer;
    }
}
