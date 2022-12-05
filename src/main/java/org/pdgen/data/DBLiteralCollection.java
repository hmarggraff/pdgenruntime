// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public interface DBLiteralCollection extends DBData {
    int getLength() throws JoriaDataException;

    boolean isStrings();

    boolean isInts();

    boolean isFloats();

    boolean isBooleans();

    String getStringAt(int i) throws JoriaDataException;

    long getIntAt(int i) throws JoriaDataException;

    double getFloatAt(int i) throws JoriaDataException;

    boolean getBooleanAt(int i) throws JoriaDataException;

    String pick() throws JoriaDataException;

    boolean contains(DBData el);
}
