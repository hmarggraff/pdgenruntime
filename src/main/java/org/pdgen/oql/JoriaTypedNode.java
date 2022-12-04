// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.JoriaType;

public interface JoriaTypedNode extends NodeInterface
{

	JoriaType getElementType();

	JoriaType getType();
}
