// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import java.io.ObjectStreamException;


public class DefaultRealLiteral extends AbstractJoriaLiteral {
    static final long serialVersionUID = -1L;
    static DefaultRealLiteral instance = new DefaultRealLiteral();


    public static DefaultRealLiteral instance() {
        return instance;
    }

    public String getName() {
        return "float";
    }


    public String getParamString() {
        return "DefaultRealLiteral";
    }


    public boolean isRealLiteral() {
        return true;
    }

    public boolean isLiteral() {
        return true;
    }

    protected Object readResolve() throws ObjectStreamException {
        return instance; //xx
    }
}
