// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.JoriaClass;
import org.pdgen.oql.JoriaQuery;

public interface ConditionalVisibility {
    String getVisibilityCondition();

    void setVisibilityCondition(String newCondition);

    JoriaQuery makeVisibilityQuery();

    JoriaClass getScope();
}
