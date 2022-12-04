// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;

public interface ClassView extends ImmutableView, MutableView
{
    int shiftChild(JoriaAccess f, int at);
	int getMemberCount();

	JoriaClass getPhysicalClass();

}
