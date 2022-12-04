// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 */
public class CompressedByteBuffer
{
    private final Deflater deflater;
    private byte[] inBuffer = new byte[16*1024];
    private int endOfData;
    private byte[] outBuffer = new byte[16*1024];
    private String subtype;
    private boolean length1Allways;

    public CompressedByteBuffer(Deflater deflater)
    {
        this.deflater = deflater;
    }

    public void add(byte[] data)
    {
        reserveBufferLength(data.length);
        System.arraycopy(data, 0, inBuffer, endOfData, data.length);
        endOfData += data.length;
    }

    private void reserveBufferLength(int length)
    {
        if(endOfData + length > inBuffer.length)
        {
            int bufferLength = inBuffer.length*2;
            while(bufferLength < endOfData + length)
                bufferLength *= 2;
            byte[] buffer = new byte[bufferLength];
            System.arraycopy(inBuffer, 0, buffer, 0, endOfData);
            inBuffer = buffer;
        }
    }

    public void add(byte[] data, int length, int offset)
    {
        reserveBufferLength(length);
        System.arraycopy(data, offset, inBuffer, endOfData, length);
        endOfData += length;
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;
    }

    public void setLength1Allways(boolean length1Allways)
    {
        this.length1Allways = length1Allways;
    }

    public boolean isCompressed()
    {
        return deflater != null;
    }

    public int compress()
    {
        if(deflater != null)
        {
            deflater.reset();
            deflater.setInput(inBuffer, 0, endOfData);
            if (outBuffer.length < inBuffer.length)
            {
                outBuffer = new byte[inBuffer.length];
            }
            deflater.finish();
            return deflater.deflate(outBuffer);
        }
        else
            return endOfData;
    }

    public byte[] compressData()
    {
        if(deflater != null)
            return outBuffer;
        else
            return inBuffer;
    }

    public void reset()
    {
        endOfData = 0;
        subtype = null;
        length1Allways = false;
    }

    public void outputToPdf(PdfOutput output) throws IOException
    {
        if(isCompressed())
        {
            int compressedLength = compress();
            StringBuilder sb = new StringBuilder();
            sb.append("<</Filter/FlateDecode/Length1 ").append(endOfData).append("/Length ").append(compressedLength);
            if(subtype != null)
                sb.append("/Subtype").append(subtype);
            sb.append(">>\nstream");
            output.writelnObj(sb.toString());
            output.writeln(compressData(), compressedLength);
            output.writeln("endstream\nendobj");
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            if(length1Allways)
                sb.append("<</Length1 ").append(endOfData).append("/Length ").append(endOfData);
            else
                sb.append("<</Length ").append(endOfData);
            if(subtype != null)
                sb.append("/Subtype").append(subtype);
            sb.append(">>\nstream");
            output.writelnObj(sb.toString());
            output.writeln(inBuffer, endOfData);
            output.writeln("endstream\nendobj");
        }

        reset();
    }

}
