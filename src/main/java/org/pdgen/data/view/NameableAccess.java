// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaType;
import org.pdgen.data.Nameable;

import java.util.Map;

public interface NameableAccess extends JoriaAccess, Nameable
{
    JoriaType getSourceTypeForChildren();
    void setXmlTag(String newTag);
	void setExportingAsXmlAttribute(boolean newValue);
	void makeName();
	String getFormatString();
	void setFormatString(String formatString);
	NameableAccess dup(JoriaClass newParent, Map<Object,Object> alreadyCopied);
	void setDefinedInView(final boolean definedInView);
	boolean isDefinedInView();
}
