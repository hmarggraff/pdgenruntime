// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface JoriaSchema extends Serializable
{
	Collection<JoriaClass> getClasses();

	JoriaClass findClass(String longName);

	SortedNamedVector<JoriaAccess> getRoots();

	void reset();

	String getDatasourceName();

	JoriaType findInternalType(String name);

    @NotNull
    List<String> getAllClassNames();

}
