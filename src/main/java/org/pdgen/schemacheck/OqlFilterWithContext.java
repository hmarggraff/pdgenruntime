// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.data.JoriaClass;

public interface OqlFilterWithContext {
    String getOqlText();

    void setOqlText(String newText);

    JoriaClass getContextClass();

}
