// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaCollection;

import java.util.Map;

public interface MutableCollection extends JoriaCollection, MutableView
{

	JoriaCollection getBaseCollection();

	void setSorting(SortOrder[] so);

	void setElementType(JoriaClass newElementMatchType);

	void setTopN(int topN);

	int getTopN();

	RuntimeParameter getTopNVariable();

	void setTopNVariable(RuntimeParameter param);

	MutableCollection copyReportPrivate(final Map<Object, Object> copiedData);
}
