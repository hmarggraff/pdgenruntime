// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import java.util.List;

public class FilteredDBCollection implements DBCollection {
    List<DBObject> elements;
    protected JoriaAccess access;
    protected JoriaType type;
    int index = -1;

    public FilteredDBCollection(List<DBObject> elems, JoriaAccess axs, JoriaType t) {
        elements = elems;
        access = axs;
        type = t;
    }

    public JoriaAccess getAccess() {
        return access;
    }

    public int getLength() throws JoriaDataException {
        if (elements != null)
            return elements.size();
        else
            return 0;
    }

    public boolean isNull() {
        return elements == null;
    }

    /**
     * pick return first element or null if there are none
     */
    public DBData pick() throws JoriaDataException {
        if (elements != null && elements.size() > 0)
            return elements.get(0);
        else
            return null;
    }

    /**
     * Returns a String that represents the value of this object.
     *
     * @return a string representation of the receiver
     */
    public String toString() {
        // Insert code to print the receiver here.
        // This implementation forwards the message to super. You may replace or supplement this.
        return super.toString();
    }

    public JoriaType getActualType() {
        return type;
    }

    public boolean same(DBData theOther) {
        return theOther == this;
    }

    protected void freeItem(int index) {
        // ignore
    }

    public boolean next() // implizites freeItem
    {
        if (index >= 0 && index < elements.size())
            freeItem(index);
        index++;
        return index < elements.size();
    }

    public DBObject current() {
        return elements.get(index);
    }

    public boolean reset() {
        index = -1;
        return true;
    }

    public List<DBObject> getElements() {
        return elements;
    }
}
