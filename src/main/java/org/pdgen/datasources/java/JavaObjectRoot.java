// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Method;

public class JavaObjectRoot extends AbstractJoriaRoot {
    private static final long serialVersionUID = 7L;
    private final transient Method method;

    public JavaObjectRoot(Method m, JoriaType t) {
        super(m.getName(), t);
        method = m;
    }

    public DBData getValue(DBData dataProvider, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (env.getRuntimeParameterValue(this) != null)// for server case because makevalue might trigger the Gui
            return env.getRuntimeParameterValue(this);
        try {
            Object invoke = method.invoke(dataProvider);
            return JavaMember.makeValue(dataProvider, invoke, asView, type, env);
        } catch (Throwable e) {
            throw new JoriaDataException(e.getMessage(), e);
        }
    }

    public void makeName() {
        longName = name + ": " + type.getName();
    }

    public void setType(JoriaType newType) {
        type = newType;
    }


}
