// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Res;

public class CollectionWrapperAccess extends DefaultAccess {

    private static final long serialVersionUID = 7L;

    public CollectionWrapperAccess(JoriaAccess orignalAccess, JoriaType newType) {
        super(null, newType, orignalAccess);
    }

    public void makeName() {
        makeName(Res.asis("Wrap"));
    }
}
