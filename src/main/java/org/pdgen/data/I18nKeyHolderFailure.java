// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

// Created by User: hmf on 04.08.2005
/**
 * Used if a key can't be set because it is computed or stored else where
 */
public class I18nKeyHolderFailure implements I18nKeyHolder
{
	public static final I18nKeyHolderFailure instance = new I18nKeyHolderFailure();

	private I18nKeyHolderFailure()
	{
	}

	public void setI18nKey(String newVal)
	{
		//nothing can be done if a I18KeyHolderFailure is used
	}
}
