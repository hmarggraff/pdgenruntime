// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated
// $Id$

import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaDictionary;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Env;

public class JavaMap extends JavaList implements JoriaDictionary {

    private static final long serialVersionUID = 7L;

    public JavaMap(Class<?> c, JoriaClass eltype, String name) {
        super(c, eltype);
    }

    public JoriaType getKeyMatchType() {
        return ((JavaSchema) Env.schemaInstance).getObjectType();
    }

    public boolean isDictionary() {
        return true;
    }

    public boolean isLarge() {
        return false;
    }
}
