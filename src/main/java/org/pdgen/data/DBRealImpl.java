// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public class DBRealImpl extends AbstractDBData implements DBReal {
    private static final long serialVersionUID = 7L;
    double myReal;


    public DBRealImpl(JoriaAccess axs, double value) {
        super(axs);
        myReal = value;
    }


    public boolean equals(Object obj) {
        if (obj instanceof DBInt) return ((DBInt) obj).getIntValue() == myReal;
        if (obj instanceof DBReal) return ((DBReal) obj).getRealValue() == myReal;
        else return false;
    }

    /* ----------------------------------------------------------------------- getRealValue */

    public double getRealValue() {
        return myReal;
    }

    /* ----------------------------------------------------------------------- isNull */

    public boolean isNull() {
        return Double.isNaN(myReal);
    }

    /* ----------------------------------------------------------------------- toString */

    public String toString() {
        return String.valueOf(myReal);
    }

    public int hashCode() {
        return (int) myReal;
    }

    public boolean same(DBData theOther) {
        if (theOther instanceof DBRealImpl) {
            return ((DBRealImpl) theOther).myReal == myReal;
        } else {
            return false;
        }
    }
}
