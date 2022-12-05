// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import java.util.List;

public class DBGrouping implements DBCollection {

    JoriaAccess myAxs;
    List<?> myValues;
    JoriaType myType;
    int myIndex = -1;

    public DBGrouping(JoriaAccess access, List<?> values, JoriaType type) {
        myAxs = access;
        myValues = values;
        myType = type;
    }

    public boolean next() // implizites freeItem
    {
        /*if(myIndex >= 0 && myIndex < myValues.size())
            myValues.set(myIndex, null);*/
        myIndex++;
        return !isNull() && myIndex < myValues.size();

    }

    public DBObject current() {
        return (DBObject) myValues.get(myIndex);
    }

    public boolean reset() {
        myIndex = -1;
        return true;
    }

    public int getLength() throws JoriaDataException {
        if (myValues == null) {
            return 0;
        }
        return myValues.size();
    }

    public DBData pick() throws JoriaDataException {
        throw new NotYetImplementedError("Dont't pick on me");
    }

    public JoriaAccess getAccess() {
        return myAxs;
    }

    public boolean isNull() {
        return myValues == null;
    }

    public JoriaType getActualType() {
        return myType;
    }

    public boolean same(DBData theOther) {
        return false;
    }


}
