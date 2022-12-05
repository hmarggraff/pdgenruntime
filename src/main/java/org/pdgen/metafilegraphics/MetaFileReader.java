// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.io.IOException;

public interface MetaFileReader {
    byte nextCmd() throws IOException;

    int readInt() throws IOException;

    byte readByte() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    void close() throws IOException;

    void reset();
}
