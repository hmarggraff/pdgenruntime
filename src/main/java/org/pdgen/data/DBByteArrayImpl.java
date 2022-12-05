// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.Arrays;

/**
 * User: patrick
 * Date: Feb 27, 2007
 * Time: 10:45:15 AM
 */
public class DBByteArrayImpl extends AbstractDBData implements DBByteArray {
    private final byte[] value;

    public DBByteArrayImpl(JoriaAccess axs, byte[] value) {
        super(axs);
        this.value = value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean same(DBData theOther) {
        if (!(theOther instanceof DBByteArrayImpl))
            return false;
        DBByteArrayImpl other = (DBByteArrayImpl) theOther;
        return Arrays.equals(value, other.value);
    }

    public byte[] getByteArrayValue() {
        return value;
    }


    public boolean equals(Object obj) {
        return same((DBData) obj);
    }
}
