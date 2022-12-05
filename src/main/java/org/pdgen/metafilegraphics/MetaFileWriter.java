// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;


public interface MetaFileWriter {
    void writeByte(byte b);

    void writeCmd(byte b);

    void writeInt(int b);

    void writeFloat(float b);

    void writeDouble(double b);

    void close();

}
