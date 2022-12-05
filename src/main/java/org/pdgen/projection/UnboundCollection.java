// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.DefaultJoriaCollection;
import org.pdgen.data.JoriaClass;


public class UnboundCollection extends DefaultJoriaCollection {
    private static final long serialVersionUID = 7L;
    public static UnboundCollection instance = new UnboundCollection();

    private UnboundCollection() {
        name = "Unbound";
    }

    // override this to ensure that UnboundReportClass is initialized and could register itself as element type
    public JoriaClass getElementType() {
        return UnboundMembersClass.instance();
    }

    protected Object readResolve() {
        return instance;
    }


}
