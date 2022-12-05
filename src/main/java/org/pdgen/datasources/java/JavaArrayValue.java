// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.env.Env;

public class JavaArrayValue extends JavaValue implements DBCollection {
    protected CollectionValueAccess elax;
    protected int index;

    public JavaArrayValue(Object[] o, JoriaAccess a) {
        super(o, a);
        Trace.check(o, Object[].class);
        elax = new CollectionValueAccess(a);
        JoriaCollection ja = a.getSourceCollection();
        if (ja.getElementType().isLiteral())
            throw new NotYetImplementedError("Java Arrays of literals.");
    }

    protected DBObject getItemInternal(int index) throws JoriaDataException {
        JoriaAccess jm = getAccess();
        JoriaCollection ja = jm.getSourceCollection();
        if (ja.getElementType().isLiteral()) {
            throw new NotYetImplementedError("Java Arrays of literals.");
        } else {
            Object o = ((Object[]) myValue)[index];
            JavaSchema sch = (JavaSchema) Env.schemaInstance;
            sch.getReflectionDelegate().loadObject(o);
            return JavaMember.makeObjectValue(o, elax, ja.getElementType());
        }
    }

    public int getLength() throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        sch.getReflectionDelegate().loadObject(myValue);
        return java.lang.reflect.Array.getLength(myValue);
    }

    public DBData pick() throws JoriaDataException {
        return getItemInternal(0);
    }

    public boolean next() // implizites freeItem
    {
        index++;
        return index < ((Object[]) myValue).length;
    }

    public DBObject current() throws JoriaDataException {
        return getItemInternal(index);
    }

    public boolean reset() {
        index = -1;
        return true;
    }

    public String toString() {
        final String name = getAccess().getType().getName();
        if (name != null)
            return name + "[]";
        else
            return "JavaArray[" + ((Object[]) myValue).length + "]";
    }
}
