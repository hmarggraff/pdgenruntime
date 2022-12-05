// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public interface DBCollection extends DBData {
    int getLength() throws JoriaDataException;

    DBData pick() throws JoriaDataException;

    boolean next() throws JoriaDataException; // implizites freeItem

    DBObject current() throws JoriaDataException;

    boolean reset();
}
