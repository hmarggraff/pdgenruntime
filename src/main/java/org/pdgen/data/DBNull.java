// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * User: patrick
 * Date: 23.04.2008
 * Time: 14:14:19
 */
public class DBNull implements DBData
{
    private static final DBData instance = new DBNull();

    public JoriaAccess getAccess()
    {
        return null;
    }

    public boolean isNull()
    {
        return true;
    }

    public JoriaType getActualType()
    {
        return null;
    }

    public boolean same(DBData theOther)
    {
        return theOther instanceof DBNull;
    }

    public static DBData getInstance()
    {
        return instance;
    }
}
