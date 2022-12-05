// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaType;

public interface CollectionView extends MutableCollection, Filtered {
    JoriaType getKeyMatchType();

    void setElementMatchType(JoriaClass newElementMatchType);

    void setKeyMatchType(JoriaType newKeyMatchType);

    void setName(String newName);

    void setElementXmlTag(String newTag);
}
