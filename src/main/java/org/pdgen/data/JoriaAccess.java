// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.Filter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.env.Res;
import org.pdgen.oql.OQLParseException;

import java.io.Serializable;
import java.util.List;

public interface JoriaAccess extends Named, Serializable, PushOnStack
{
	String ACCESSERROR = Res.str("Access_Error");

	String getLongName();

	JoriaClass getDefiningClass();

	JoriaType getType();

	/**
	 * return the type as a collection type or throw an exception if it is not a collection type
     * @return the collection type
     */
    JoriaCollection getCollectionTypeAsserted();

	/**
	 * return the type as a class type or throw an exception if it is not a class type
     * @return the class type
     */
    JoriaClass getClassTypeAsserted();

	DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException;

	boolean isRoot();

	boolean isTransformer();

	boolean isAccessTyped();

	String getXmlTag();

	boolean isExportingAsXmlAttribute();

	/**
	 * If the type is a collection return it otherwise return null; if this is a picking access do not
	 * return the type but rather the source collection from which the element is picked
     * @return the source collection
     */
    JoriaCollection getSourceCollection();

	String getCascadedHostFilter();

	String getCascadedOQLFilterString();

	OQLNode getCascadedOQLFilter() throws OQLParseException;

	void getCascadedOQLFilterList(List<Filter> collector);

	JoriaAccess getPlaceHolderIfNeeded();

	//void collectPhysicalAccessors(HashMap physicalAccessors);
}
