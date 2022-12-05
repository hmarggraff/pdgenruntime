// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BinMetaFileWriter implements MetaFileWriter {
    protected ByteArrayOutputStream bout;
    protected DataOutputStream out;

    public BinMetaFileWriter() {
        bout = new ByteArrayOutputStream();
        out = new DataOutputStream(bout);
    }

    public void writeByte(byte b) {
        try {
            out.writeByte(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void writeCmd(byte b) {
        try {
            out.writeByte(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void writeInt(int b) {
        try {
            out.writeInt(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void writeFloat(float b) {
        try {
            out.writeFloat(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void writeDouble(double b) {
        try {
            out.writeDouble(b);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    byte[] getBytes() {
        return bout.toByteArray();
    }
}
