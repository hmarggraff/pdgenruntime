// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


/**
 * Knows how to update a localisation key in a resource.
 * The various resources Styles, reports, variables do not implement  I18nKeyHolder
 * directly, because they might have more than one field that is a localisation key.
 * There must be one I18nKeyHolder for each field.
 */
public interface I18nKeyHolder {
	void setI18nKey(String newVal);
}
