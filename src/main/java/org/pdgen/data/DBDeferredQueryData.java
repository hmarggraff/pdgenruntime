// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.oql.JoriaDeferedQuery;

public class DBDeferredQueryData extends AbstractDBData implements DBInt, DBReal, DBString, DBBoolean {
    private static final long serialVersionUID = 7L;
    JoriaDeferedQuery query;
    private final String val4String = "pageRel";

    public DBDeferredQueryData(final JoriaDeferedQuery query) {
        this.query = query;
    }

    public boolean isNull() {
        return false;
    }

    public boolean same(final DBData theOther) {
        return this == theOther;
    }

    public JoriaType getActualType() {
        return query.getType();
    }

    public long getIntValue() {
        return 0;
    }

    public double getRealValue() {
        return 0;
    }

    public String getStringValue() {
        return val4String;
    }

    public boolean getBooleanValue() {
        return false;
    }

    public int compareTo(final DBString o) {
        if (!(o instanceof DBString))
            throw new JoriaAssertionError("Trying to compare DBString with " + o.getClass());
        return val4String.compareTo(o.getStringValue());
    }
}
