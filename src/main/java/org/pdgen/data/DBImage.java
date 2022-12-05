// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * User: patrick
 * Date: Nov 24, 2004
 * Time: 2:38:17 PM
 */
public class DBImage implements DBData {
    Object o;
    JoriaAccess axs;

    public DBImage(Object o, JoriaAccess axs) {
        this.o = o;
        this.axs = axs;
    }

    public JoriaAccess getAccess() {
        return axs;
    }

    public boolean isNull() {
        return o == null;
    }

    public JoriaType getActualType() {
        return DefaultImageLiteral.instance();
    }

    public boolean same(DBData theOther) {
        if (theOther instanceof DBImage)
            return o.equals(((DBImage) theOther).o);
        return false;
    }

    public Object getData() {
        return o;
    }

}
