// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Method;
import java.util.Date;

public class JavaMethod extends JavaMember {

    private static final long serialVersionUID = 7L;
    transient Method myMethod;

    public JavaMethod(JavaClass a, Method f, JoriaType type) {
        super(a, f.getName(), type);
        myMethod = f;
    }

    public JavaMethod(JavaClass a, Method f, JoriaType type, String name) {
        super(a, name, type);
        myMethod = f;
    }

    public DBData getValue(DBData jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (jv == null)
            throw new JoriaDataException("getValue from Method with null object");
        if (!(jv instanceof JavaValue))
            throw new JoriaDataException("getValue with java method not possible from " + jv.getClass().getName() + " " + myMethod.getName());
        if (getType().isCollection()) {
            DBCollection data = ((JavaValue) jv).getCachedCollectionValue(asView);
            if (data != null)
                return data;
        }
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        DBData data = sch.getReflectionDelegate().getMethodValue(this, (JavaValue) jv, asView, env);
        if (data instanceof DBCollection) {
            //((JavaValue)jv).addCollectionToCache((DBCollection) data, asView);
        }
        return data;
    }

    public Method getMethod() {
        return myMethod;
    }

    public String toString() {
        return getLongName();
    }

    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getIntMethod(this, (JavaValue) from, env);
    }

    public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getFloatMethod(this, (JavaValue) from, env);
    }

    public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getDateMethod(this, (JavaValue) from, env);
    }

    public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getObjectMethod(this, (JavaValue) from, env);
    }

    public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getBooleanMethod(this, (JavaValue) from, env);
    }

    public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getStringMethod(this, (JavaValue) from, env);
    }
}
