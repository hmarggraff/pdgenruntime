// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import java.util.ArrayList;

public class DBGroupValues implements DBCollection {
    JoriaAccess myAccess;
    ArrayList<DBObject> values; // of DBObject
    JoriaType myType;
    int myIndex = -1;

    public DBGroupValues(JoriaAccess access, ArrayList<DBObject> groupValues, JoriaType type) {
        myAccess = access;
        values = groupValues;
        myType = type;
    }

    public JoriaAccess getAccess() {
        return myAccess;
    }

    /**
     * never null because null groups are not created in the first place
     */
    public boolean isNull() {
        return false; //
    }

    public int getLength() throws JoriaDataException {
        return values.size();
    }

    public DBData pick() throws JoriaDataException {
        throw new NotYetImplementedError("Don't pick on me");
    }

    public JoriaType getActualType() {
        return myType;
    }

    public boolean same(DBData theOther) {
        return false;
    }

    public boolean next() // implizites freeItem
    {
        myIndex++;
        return myIndex < values.size();
    }

    public DBObject current() {
        return values.get(myIndex);
    }

    public boolean reset() {
        myIndex = -1;
        return true;
    }
}
