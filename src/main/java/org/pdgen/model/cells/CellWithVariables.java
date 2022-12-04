// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.data.AccessVisitor;
import org.pdgen.data.JoriaAccess;

import java.util.Set;

public interface CellWithVariables
{
	void collectVariables(Set<RuntimeParameter> v, Set<Object> seen);

    boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen);
}
