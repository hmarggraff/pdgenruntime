package org.pdgen.runtimeapi;

import org.pdgen.runtimeapi.beans.GenericTag;
import org.pdgen.data.JoriaDataException;

/**
 * User: patrick
 * Date: Dec 13, 2004
 * Time: 11:29:49 AM
 */
public interface SerializeObjects
{
	/**
	 * Method that wraps an object in a string, so that it can be sent to a remote client for interactive selection.
	 * This method is required, if the user shall pick one or more objects from a list of objects generated from the database.
	 * It is rarely necesary to map and send the whole object. It is usually sufficient, to map the object to its primary key.
	 * @param obj to be serialized
	 * @return str that containsName the text representation of the object (e.g. the primary key, or the object id of the database)
	 */
    String mapObject2String(Object obj);
    Object mapString2Object(String str) throws JoriaDataException;
    void setProperties(GenericTag userProps);
}
