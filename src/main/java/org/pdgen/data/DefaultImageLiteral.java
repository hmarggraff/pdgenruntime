// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import java.io.ObjectStreamException;


public class DefaultImageLiteral extends AbstractJoriaLiteral {
    static final long serialVersionUID = -1L;
    static DefaultImageLiteral instance = new DefaultImageLiteral();

    /* ----------------------------------------------------------------------- instance */

    public static DefaultImageLiteral instance() {
        return instance;
    }


    /* ----------------------------------------------------------------------- getName */

    public String getName() {
        return "Image";
    }

    /* ----------------------------------------------------------------------- getParamString */

    public String getParamString() {
        return "DefaultImageLiteral";
    }

    /* ----------------------------------------------------------------------- isRealLiteral */

    public boolean isImage() {
        return true;
    }

    @Override
    public boolean isBlob() {
        return true;
    }

    /* ----------------------------------------------------------------------- isLiteral */

    public boolean isLiteral() {
        return true;
    }

    /* ----------------------------------------------------------------------- readResolve */

    protected Object readResolve() throws ObjectStreamException {
        return instance;
    }
}
