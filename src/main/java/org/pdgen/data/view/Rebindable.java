// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;

public interface Rebindable
{
	void unbind();
	boolean unbound();
	boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding);

	void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding);
}
