// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;

import java.io.ObjectStreamException;

public class LiteralCollectionMember extends AbstractTypedJoriaMember {
    private static final long serialVersionUID = 7L;

    public LiteralCollectionMember(LiteralCollectionClass parent, JoriaType type) {
        super(parent, Res.asis("value"), type);
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (!(from instanceof DBLiteralObject))
            throw new JoriaAssertionError("unsupported dbobject " + from.getClass().getName());
        DBLiteralObject f = (DBLiteralObject) from;
        return f.myData.getValue(f.index, asView);
    }

    protected Object readResolve() throws ObjectStreamException {
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
