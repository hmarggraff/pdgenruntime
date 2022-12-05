// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: May 23, 2005
 * Time: 2:51:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoriaSchemaHelper {
    public static Object convertLiteralDBDateToObject(DBData dbVal) throws JoriaDataException {
        if (dbVal instanceof DBStringImpl)
            return ((DBStringImpl) dbVal).getStringValue();
        else if (dbVal instanceof DBIntImpl)
            return ((DBIntImpl) dbVal).getIntValue();
        else if (dbVal instanceof DBRealImpl)
            return ((DBRealImpl) dbVal).getRealValue();
        else if (dbVal instanceof DBBooleanImpl)
            return ((DBBooleanImpl) dbVal).getBooleanValue();
        else if (dbVal instanceof DBDateTime)
            return ((DBDateTime) dbVal).getDate();
        else
            throw new JoriaDataException("unsupported type " + dbVal.getClass().getName());

    }

}
