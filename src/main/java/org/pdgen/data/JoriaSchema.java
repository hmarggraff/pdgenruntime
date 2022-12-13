// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.jetbrains.annotations.NotNull;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JoriaSchema extends Serializable {
    SortedNamedVector<JoriaAccess> getRoots();

    JoriaClass findClass(String longName);


    Map<String, JoriaType> getTypes();

    Object getSchemaForSave();

    /*



    void reset();

    String getDatasourceName();

    JoriaType findInternalType(String name);

    @NotNull
    List<String> getAllClassNames();

     */

}
