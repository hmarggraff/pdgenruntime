// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//Based on iText  by Bruno Lowagie: http://www.lowagie.com/iText/

import org.pdgen.model.run.pdf.PdfOutput;

import java.io.IOException;
import java.io.FileInputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.DataFormatException;
import java.net.URL;

public class PdfPng extends PdfImage
{
    /** Some PNG specific values. */
    public static final byte[] PNGID = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    /** A PNG marker. */
    public static final byte[] IHDR = {'I','H','D','R'};
    /** A PNG marker. */
    public static final byte[] PLTE = {'P','L','T','E'};
    /** A PNG marker. */
    public static final byte[] IDAT = {'I','D','A','T'};
    /** A PNG marker. */
    public static final byte[] IEND = {'I','E','N','D'};
    /** A PNG marker. */
    public static final byte[] tRNS = {'t','R','N','S'};
    public static final byte[] bitMask = {0, 1, 3, 0, 15, 0, 0, 0, -1};
    public static final byte[] samplesByte = { 0, 8, 4, 0, 2, 0, 0, 0, 1};
    int colorType;
    int bitDepth;
    byte[] colorTable;
    byte[] alphaTable;
    PdfPng maskPicture;
    boolean maskImage;

    public PdfPng(URL url, String name, PdfOutput output) throws IOException
    {
        this(PdfImage.readBytes(url.openStream(), name), name, output);
    }
    public PdfPng(String file, String name, PdfOutput output) throws IOException
    {
        this(PdfImage.readBytes(new FileInputStream(file), file), name, output);
    }
    public PdfPng(byte[] img, String name, PdfOutput output) throws IOException
    {
        super(PdfImage.PNG, name, img);
        if (!checkBytes(PNGID))
            throw new IOException("Not a valid " + type + " image");
        processParameters(output);
    }

    private PdfPng(byte[] mask, int width, int height, PdfOutput output)
    {
        super(PdfImage.PNG, "mask", mask);
        this.width = width;
        this.height = height;
        bitDepth = 8;
        colorType = 0;
        streamBytes = mask;
        name = output.addImage(this);
        maskImage = true;
    }
    private void processParameters(PdfOutput output) throws IOException
    {
        type = PdfImage.PNG;
        try
        {
            int at = PNGID.length;
            boolean needIEND = true;
            while (needIEND)
            {
                int len = getInt(at);
                if (checkBytes(IHDR, at + 4))
                {
                    width = getInt(at + 8);
                    height = getInt(at + 12);
                    bitDepth = rawData[at + 16];
                    if (bitDepth == 16)
                        throw new IOException("Bad Png image (Pdf does not support a bit depth of 16)");
                    colorType = rawData[at + 17];
                    if (!(colorType == 0 || colorType == 2 || colorType == 3 || colorType == 4 || colorType == 6))
                        throw new IOException("Colortype " + colorType + " is not suported for Png.");
                    int compression = rawData[at+18];
                    if(compression != 0)
                        throw new IOException("Compression " + compression + " is not suported for Png.");
                    int filter = rawData[at+19];
                    if(filter != 0)
                        throw new IOException("Filter " + filter+ " is not suported for Png.");
                    int interlace = rawData[at+20];
                    if(interlace != 0)
                        throw new IOException("Interlace " + interlace + " is not suported for Png.");
                }
                else if (checkBytes(IDAT, at + 4))
                {
                    int oldLen = 0;
                    if(streamBytes == null)
                        streamBytes = new byte[len];
                    else
                    {
                        byte[] oldStreamBytes = streamBytes;
                        oldLen = streamBytes.length;
                        streamBytes = new byte[len+oldLen];
                        System.arraycopy(oldStreamBytes, 0, streamBytes, 0, oldLen);
                    }
                    System.arraycopy(rawData, at + 8, streamBytes, oldLen, len);
                }
                else if (checkBytes(PLTE, at + 4) && colorType == 3)
                {
                    colorTable = new byte[len];
                    System.arraycopy(rawData, at + 8, colorTable, 0, len);
                }
                else if(checkBytes(tRNS, at + 4) && colorType == 3)
                {
                    alphaTable = new byte[len];
                    System.arraycopy(rawData, at+8, alphaTable, 0, len);
                }
                else if(checkBytes(IEND, at + 4))
                {
                    needIEND = false;
                }
                at = at + len + 12;
            }

            if(colorType == 6 || colorType == 4) // we have a RGBA PNG and PDF wants one image for RGB and another for an A mask
                                                 // or we have a GA PNG and PDF wants one image for G and another for an A mask
            {
                splitBytes(colorType == 4 ? 1 : 3, output);
            }
            else if(alphaTable != null)
            {
                extractAlphaBytes(output);
            }
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            ex.printStackTrace();
            throw new IOException("Bad Png image (array exception when parsing size)");
        }
    }

    private void extractAlphaBytes(PdfOutput output) throws IOException
    {
        Inflater inf = new Inflater();
        inf.setInput(streamBytes);
        byte[] inflatedData = new byte[height * (((width) * (bitDepth)+7)/8+1)];
        try
        {
            int bytes = inf.inflate(inflatedData);
            if(bytes != inflatedData.length || !inf.finished())
                throw new IOException("unexpected inflated data length");
            inf.end();
            /*for (int i = 0; i < inflatedData.length; i++)
            {
                byte b = inflatedData[i];
                if(b != 0)
                    System.out.println("byte "+i+" is "+b);
            }*/
        }
        catch (DataFormatException e)
        {
            throw new IOException(e.getMessage());
        }
        byte[] maskData = new byte[height * (width+1)];
        int ix = 0;
        int mix = 0;
        byte mask = bitMask[bitDepth];
        int samples = samplesByte[bitDepth];
        int bytes = ((width) * (bitDepth)+7)/8;
        if(mask == 0)
            throw new IOException("unsupported number of color table entries");
        for (int i = 0; i < height; i++)
        {
            // copy filter bytes
            maskData[mix++] = inflatedData[ix++];
            for(int j = 0; j < bytes; j++)
            {
                byte data = inflatedData[ix++];
                for(int k = 0; k < samples; k++)
                {
                    if(j * samples + k >= width)
                        continue;
                    int val = data >> (samples - k -1) * bitDepth;
                    val &= mask;
                    if(val < 0)
                        val += 256;
                    if(val > alphaTable.length-1)
                        maskData[mix++] = -1;
                    else
                        maskData[mix++] = alphaTable[val];
                }
            }
        }
        byte[] temp = new byte[maskData.length];
        Deflater defMask = new Deflater(Deflater.BEST_COMPRESSION);
        defMask.setInput(maskData);
        defMask.finish();
        int compressedLength = defMask.deflate(temp);
        maskData = new byte[compressedLength];
        System.arraycopy(temp, 0, maskData, 0, compressedLength);
        maskPicture = new PdfPng(maskData, width, height, output);
    }

    private void splitBytes(int colorBytes, PdfOutput output) throws IOException
    {
        Inflater inf = new Inflater();
        inf.setInput(streamBytes);
        byte[] inflatedData = new byte[height * ((width) * (colorBytes+1)+1)];
        try
        {
            int bytes = inf.inflate(inflatedData);
            if(bytes != inflatedData.length || !inf.finished())
                throw new IOException("unexpected inflated data length");
            inf.end();
            /*for (int i = 0; i < inflatedData.length; i++)
            {
                byte b = inflatedData[i];
                if(b != 0)
                    System.out.println("byte "+i+" is "+b);
            }*/
        }
        catch (DataFormatException e)
        {
            throw new IOException(e.getMessage());
        }
        byte[] pictureData = new byte[height * (width * colorBytes + 1)];
        byte[] maskData = new byte[height * (width+1)];
        int ix = 0;
        int pix = 0;
        int mix = 0;
        for (int i = 0; i < height; i++)
        {
            // copy filter bytes
            maskData[mix++] = pictureData[pix++] = inflatedData[ix++];
            for(int j = 0; j < width; j++)
            {
                for(int k = 0; k < colorBytes; k++)
                    pictureData[pix++] = inflatedData[ix++];
                maskData[mix++] = inflatedData[ix++];
            }
        }
        Deflater defPicture = new Deflater(Deflater.BEST_COMPRESSION);
        defPicture.setInput(pictureData);
        defPicture.finish();
        byte[] temp = new byte[pictureData.length];
        int compressedLength = defPicture.deflate(temp);
        streamBytes = new byte[compressedLength];
        System.arraycopy(temp, 0, streamBytes, 0, compressedLength);
        Deflater defMask = new Deflater(Deflater.BEST_COMPRESSION);
        defMask.setInput(maskData);
        defMask.finish();
        compressedLength = defMask.deflate(temp);
        maskData = new byte[compressedLength];
        System.arraycopy(temp, 0, maskData, 0, compressedLength);
        maskPicture = new PdfPng(maskData, width, height, output);
        colorType -= 4;
    }

    public void writeImage(PdfOutputer pw) throws IOException
    {
        pw.writeObj("<< ");
        super.writeImage(pw);
        pw.writeAttr("/Filter", "/FlateDecode");
        pw.writeAttr("/BitsPerComponent", bitDepth);
        if (colorType == 0)
            pw.writeAttr("/ColorSpace", "/DeviceGray");
        else if (colorType == 2)
            pw.writeAttr("/ColorSpace", "/DeviceRGB");
        if (colorType == 3)
        {
            pw.write("/ColorSpace [ /Indexed /DeviceRGB ");
            pw.write(colorTable.length / 3 - 1);
            pw.write(" <");
            pw.writeHex(colorTable);
            pw.writeln(">]");
        }
        if(maskPicture != null)
        {
            pw.writeln("/SMask "+maskPicture.oid+ " 0 R");
        }
        pw.writeln("/DecodeParms << ");
        pw.writeAttr("/BitsPerComponent", bitDepth);
        pw.writeAttr("/Colors", ((colorType == 2) ? "3": "1"));
        pw.writeAttr("/Columns", width);
        pw.writeAttr("/Predictor", "15");
        pw.writeln(" >>");
        pw.writeAttr("/Length", streamBytes.length);
        pw.writeln(" >>");
        pw.writeln("stream");
        pw.writeln(streamBytes);
        pw.writeln("endstream");
        pw.writeln("endobj");
    }

}

