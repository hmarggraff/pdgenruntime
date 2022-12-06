// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.env.Env;

import java.util.List;

public class JavaLargeListValue extends JavaListValue {
    public JavaLargeListValue(Object o, JoriaAccess a) throws JoriaDataException {
        super(o, a);
    }

    @SuppressWarnings("unchecked")
    protected void freeItem(int index) {
        final ReflectionDelegate reflectionDelegate = ((JavaSchema) Env.schemaInstance).getReflectionDelegate();
        reflectionDelegate.evictObject(((List<Object>) myValue).get(index));
        ((List<Object>) myValue).set(index, null);
    }

    public boolean reset() {
        return index == -1;
    }

}
