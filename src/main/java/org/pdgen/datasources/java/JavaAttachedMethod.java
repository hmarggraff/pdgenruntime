// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;

import java.util.Date;
import java.util.function.Function;

public class JavaAttachedMethod<DeclaredIn, RetType> extends JavaMember {

    private static final long serialVersionUID = 7L;
    transient Function<DeclaredIn, RetType> function;


    public JavaAttachedMethod(JavaClass a, Function<DeclaredIn, RetType> f, JoriaType type, String trueName) {
        super(a, trueName, type);
        function = f;
    }

    public DBData getValue(DBData jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (jv == null)
            throw new JoriaDataException("getValue from Method with null object");
        if (!(jv instanceof JavaValue))
            throw new JoriaDataException("getValue from Method with wrong object type "
                    + jv.getClass().getName());
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getAttachedMethodValue(this, (JavaValue) jv, asView, env);
    }

    public String toString() {
        return getLongName();
    }


    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        return (long) eval(from);
    }

    private RetType eval(DBObject from) {
        DeclaredIn source = (DeclaredIn) ((JavaValue) from).getJavaObject();
        return function.apply(source);
    }

    public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException {
        return (double) eval(from);
    }

    public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException {
        return (int) eval(from);
    }

    public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException {
        return (String) eval(from);
    }

    public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException {
        return (Date) eval(from);
    }

    public Object getPictureValue(DBObject from, RunEnv env) {
        return eval(from);
    }
}
