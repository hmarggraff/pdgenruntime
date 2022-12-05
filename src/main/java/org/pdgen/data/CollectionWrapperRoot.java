// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Method;

public class CollectionWrapperRoot extends AbstractJoriaRoot {
    private static final long serialVersionUID = 7L;
    @org.jetbrains.annotations.NotNull
    private final transient Method method;

    public CollectionWrapperRoot(Method m, JoriaType t) {
        super(m.getName(), t);
        method = m;
    }

    public DBData getValue(DBData collCache, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        return new CollectionWrapperValue(asView, collCache, method);
    }

    public void setName(String newName) {
        name = newName;
    }
}
