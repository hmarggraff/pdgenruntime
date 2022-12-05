// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public interface DBDictionary extends DBCollection {

    /* ----------------------------------------------------------------------- getKeys */

    DBCollection getKeys() throws JoriaDataException;

    /* ----------------------------------------------------------------------- getValueForKey */

    DBData getValueForKey(DBData key) throws JoriaDataException;

    /* ----------------------------------------------------------------------- getValues */

    DBCollection getValues() throws JoriaDataException;
}
