// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//Based on iText  by Bruno Lowagie: http://www.lowagie.com/iText/

import org.pdgen.data.Trace;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.run.pdf.PdfOutput;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An <CODE>Image</CODE> is the representation of a graphic element (JPEG, PNG or GIF)
 * that has to be inserted into the document
 *
 * @author bruno@lowagie.com
 * @see Rectangle
 */
public abstract class PdfImage {

    public static final String GIF = "Gif";
    public static final String PNG = "Png";
    public static final String JPEG = "Jpeg";
    protected byte[] rawData;
    protected byte[] streamBytes;
    protected String type;
    protected int height;
    protected int width;
    String name;
    public int oid;


    // constructors
    public PdfImage(String type, String name, byte[] bytes) {
        this.type = type;
        this.name = name;
        rawData = bytes;
    }

    public static PdfImage getInstance(Object storedData, String name, Icon img, PdfOutput output) throws IOException {
        if (storedData != null) {
            byte[] bytes;
            if (storedData instanceof String) {
                String file = (String) storedData;
                try {
                    InputStream is = Env.instance().getFileService().getFileData(file);
                    bytes = readBytes(is, file);
                } catch (FileNotFoundException e) {
                    Trace.logError("Picture file not found: " + file);
                    Trace.log(e);
                    return new PdfPng(Res.class.getResource("pix/pdfmiss.png"), name, output);
                }
            } else if (storedData instanceof byte[]) {
                bytes = (byte[]) storedData;
            } else {
                throw new IOException("unsupport type for image stored data, must be filename or byte[]");
            }

            //noinspection EmptyCatchBlock
            try {
                int c1 = bytes[0];
                int c2 = bytes[1];
                if (c1 == 'G' && c2 == 'I') {
                    return new PdfGif(bytes, name);
                }
                if (c1 == -1 && c2 == -40) {
                    return new PdfJpeg(bytes, name);
                }
                if (c1 == PdfPng.PNGID[0] && c2 == PdfPng.PNGID[1]) {
                    return new PdfPng(bytes, name, output);
                }
            } catch (Throwable e) {
            }
            //noinspection EmptyCatchBlock
            try {
                ImageDetection.ImageHolder ih = ImageDetection.recodeImage(bytes);
                if (ih != null)
                    return new PdfPng(ih.getImageData(), name, output);
            } catch (IOException e) {
            }

        }
        ImageDetection.ImageHolder ih = ImageDetection.recodeIcon(img);
        if (ih.isBadImage())
            return null;
        else
            return new PdfPng(ih.getImageData(), name, output);
    }

    public String type() {
        return type;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }

    protected boolean checkBytes(byte[] check) throws IOException {
        return checkBytes(check, 0);
    }

    protected boolean checkBytes(byte[] check, int at) throws IOException {
        for (int i = 0; i < check.length; i++) {
            if (rawData[at + i] != check[i])
                return false;
        }
        return true;
    }

    public void writeImage(PdfOutputer pw) throws IOException {
        //pw.writeAttr("/Name", name);
        pw.writeAttr("/Type", "/XObject");
        pw.writeAttr("/Subtype", "/Image");
        pw.writeAttr("/Width", width);
        pw.writeAttr("/Height", height);
    }

    protected static byte[] readBytes(InputStream in, String file) throws IOException {
        byte[] bytes = new byte[in.available()];
        int rl = in.read(bytes);
        if (rl < bytes.length || in.read() != -1)
            throw new IOException("Error reading image file " + file);
        return bytes;
    }

    public int getInt(int is) throws IOException {
        return ((rawData[is] & 0xFF) << 24) + ((rawData[is + 1] & 0xFF) << 16) + ((rawData[is + 2] & 0xFF) << 8) + (rawData[is + 3] & 0xFF);
    }

}
