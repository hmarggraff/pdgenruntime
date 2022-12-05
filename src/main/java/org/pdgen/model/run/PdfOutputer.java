// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Jan 17, 2003
 * Time: 8:36:59 AM
 * To change this template use Options | File Templates.
 */
public interface PdfOutputer {
    void writeAttr(String key, String Value) throws IOException;

    void writeAttr(String key, long value) throws IOException;

    void writeObj(String txt) throws IOException;

    void writeln(String txt) throws IOException;

    void writeln(byte[] bytes) throws IOException;

    void write(String txt) throws IOException;

    void write(long value) throws IOException;

    void write(float value) throws IOException;

    void writeHex(byte[] bytes) throws IOException;
}
