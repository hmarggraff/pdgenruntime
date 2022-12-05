// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public interface DBData {
    JoriaAccess getAccess();

    boolean isNull();

    JoriaType getActualType();

    boolean same(DBData theOther);

}
