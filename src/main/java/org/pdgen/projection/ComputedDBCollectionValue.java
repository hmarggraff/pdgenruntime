// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.*;

import java.util.List;


public class ComputedDBCollectionValue implements DBCollection {
    String myFilter;
    List<DBData> elems;
    JoriaAccess myAccess;
    int index = -1;


    public ComputedDBCollectionValue(List<DBData> els, JoriaAccess a) {
        elems = els;
        myAccess = a;
    }


    public JoriaAccess getAccess() {
        return myAccess;
    }


    public JoriaType getActualType() {
        return myAccess.getType();
    }


    public String getFilter() {
        return myFilter;
    }

    protected DBObject getItemInternal(int index) throws JoriaDataException {
        try {
            Object o = elems.get(index);

            return (DBObject) o;
        } catch (IndexOutOfBoundsException ex) {
            throw new JoriaDataException("Index is out of bounds: " + getAccess().getName() + "[" + index + "]");
        }
    }


    public int getLength() throws JoriaDataException {
        return elems.size();
    }


    public List<DBData> getList() {
        return elems;
    }


    public boolean isNull() {
        return elems == null;
    }


    public DBData pick() throws JoriaDataException {
        return getItemInternal(0);
    }


    public void setFilter(String aFilter) {
        myFilter = aFilter;
    }


    public String toString() {
        try {
            return getAccess().getType().getName() + getLength();
        } catch (JoriaDataException ex) {
            return getAccess().getType().getName() + ex.getMessage();
        }
    }

    public boolean same(DBData theOther) {
        return false;
    }

    public boolean next() // implizites freeItem
    {
        index++;
        return index < elems.size();
    }

    public DBObject current() {
        return (DBObject) elems.get(index);
    }

    public boolean reset() {
        index = -1;
        return true;
    }
}
