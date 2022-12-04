// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.Filter;
import org.pdgen.data.view.SortOrder;
import org.pdgen.model.run.RunEnv;

public interface JoriaCollection extends JoriaType
{
	JoriaClass getElementMatchType();

	JoriaClass getElementType();

	String getElementXmlTag();

	SortOrder[] getSorting();

	Filter getFilter();

	void setFilter(Filter f);

	boolean isLarge();

	int getMinTopN(RunEnv env) throws JoriaDataException;

    boolean hasFilterOrSortingOrTopN();

}
