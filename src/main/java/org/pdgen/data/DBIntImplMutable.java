// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class DBIntImplMutable extends DBIntImpl {
    private static final long serialVersionUID = 7L;

    public DBIntImplMutable(JoriaAccess axs, long value) {
        super(axs, value);
    }

    public void setValue(long newInt) {
        myInt = newInt;
    }
}
