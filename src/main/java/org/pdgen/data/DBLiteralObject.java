// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class DBLiteralObject implements DBObject
{
    int index;
    DBLiteralCollectionData myData;
    JoriaAccess asView;

    public DBLiteralObject(DBLiteralCollectionData data, int i, JoriaAccess v)
    {
        index = i;
        myData = data;
        asView = v;
    }
    public JoriaAccess getAccess()
    {
        return asView;
    }

    public boolean isNull()
    {
        return false;
    }

    public JoriaType getActualType()
    {
        return myData.myClass;
    }

    public boolean isAssignableTo(JoriaType t)
    {
        return myData.myClass == t;
    }

    public boolean isValid()
    {
        return true;
    }

    public boolean same(DBData theOther)
    {
        if(!(theOther instanceof DBLiteralObject))
            return false;
        DBLiteralObject other = (DBLiteralObject) theOther;
        return myData.equals(other.myData) && index == other.index;
    }
}
