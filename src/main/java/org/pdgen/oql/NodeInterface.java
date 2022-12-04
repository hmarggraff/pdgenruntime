// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;


import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface NodeInterface
{
	int booleanType = 0;
	int intType = 1;
	int realType = 2;
	int charType = 3;
	int stringType = 4;
	int objectType = 5;
	int collectionType = 6;
	int dictionaryType = 7;
	int dateType = 8;
	int literalCollectionType = 9;
	int blobType = 10;


	boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException;

	char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException;

	DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException;

	double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException;

	long getIntValue(RunEnv env, DBData p0) throws JoriaDataException;

	String getStringValue(RunEnv env, DBData p0) throws JoriaDataException;

	String getTokenString();

	DBData getValue(RunEnv env, DBData p0) throws JoriaDataException;

	DBData getWrappedValue(RunEnv env, DBData from) throws JoriaDataException;

	boolean isBoolean();

	boolean isCharacter();

	boolean isCollection();

	boolean isLiteralCollection();

	boolean isDictionary();

	boolean isInteger();

	boolean isReal();

	boolean isObject();

	boolean isString();

	boolean isDate();

	boolean isBlob();

	boolean hasMofifiedAccess();

	/**
	 * if we are in defered Mode. I.e eval depends on the page context, we must find all field Nodes to cache their values
	 *
	 * @param env
	 * @param from
	 */
	void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException;

	void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect);

	void getUsedAccessors(Set<JoriaAccess> s);

	String getTypeName();

	boolean hasText(final String text, final boolean searchLabels, final boolean searchData);

	/**
	 * generates a token string for the structure tree, where occurences of the access are replaced by newname.
	 * The text of the changed
	 * expression can then be retrieved from the parameter collector.
	 * @param access   The access to be renamed
	 * @param newName  The new name
	 * @param collector The buffer where the new token string is collected (i.e. the return value)
	 * @param bindingLevel
	 */
	void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, StringBuffer collector, final int bindingLevel);

	void collectVariables(Set<RuntimeParameter> set, final Set<Object> seen);
}
