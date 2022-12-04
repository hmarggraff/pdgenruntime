// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//Based on iText  by Bruno Lowagie: http://www.lowagie.com/iText/

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfGif extends PdfImage
{
	public static final byte[] gifMagic = {'G', 'I', 'F' };
	public PdfGif(byte[] img, String name) throws IOException
	{
		super(PdfImage.GIF, name, img);
		if (!checkBytes(gifMagic))
			throw new IOException("Not a valid " + type + " image");
		width = (rawData[6] & 0xFF) + ((rawData[7] & 0xFF) << 8);
		height = (rawData[8] & 0xFF) + ((rawData[9] & 0xFF) << 8);
        if ((rawData[10] & 0x80) == 0)
        {
            throw new IOException("Not a supported GIF-file (there is no global color table present).");
        }
        int nColors = 1 << ((rawData[10] & 7) + 1);
        int at = 13 + nColors * 3;
        while ((rawData[at] & 0xff) == 0x21) // extension introducer
        {
            at += 2; // skip extension id
            int step;
            do
            {
                step = rawData[at] & 0xff;
                at += step+1;
            } while(step != 0);
        }
        if ((rawData[at] & 0xFF) != 0x2c)
        {
            throw new IOException("Not a supported GIF-file (the image separator '0x2c' is not found after reading the color table).");
        }
        // ignore position and size
        at += 9;
        // Byte 9: Packed Fields
        // Byte 9: bit 1: Local Color Table Flag
        // Byte 9: bit 2: Interlace Flag
        if ((rawData[at] & 0xc0) > 0)
        {
            throw new IOException("Not a supported GIF-file (interlaced gifs or gifs using local color table can't be inserted).");
        }

        // Byte 10: LZW initial code
        if (rawData[++at] != 0x08)
        {
            //byte b = rawData[at];
            throw new IOException("Not a supported GIF-file (initial LZW code not supported).");
        }
        int code;
        int codelength = 9;
        int tablelength = 257;
        int bitsread;
        int bitstowrite = 0;
        int bitsdone = 0;
        int bitsleft = 23;
        int bytesdone;
        int bytesread = 0;
        int byteswritten = 0;
        // read the size of the first Data Block
        int size = (rawData[++at] & 0xFF);
        // Check if there is any data in the GIF
        if (size < 1)
        {
            throw new IOException("Not a supported GIF-file. (no image data found).");
        }
        // if possible, we read the first 24 bits of data
        size--;
        bytesread++;
        bitsread = (rawData[++at] & 0xFF);
        if (size > 0)
        {
            size--;
            bytesread++;
            bitsread += ((rawData[++at] & 0xFF) << 8);
            if (size > 0)
            {
                size--;
                bytesread++;
                bitsread += ((rawData[++at] & 0xFF) << 16);
            }
        }
        while (bytesread > byteswritten)
        {
            tablelength++;
            // we extract a code with length=codelength
            code = (bitsread >> bitsdone) & ((1 << codelength) - 1);
            // we delete the bytesdone in bitsread and append the next byte(s)
            bytesdone = (bitsdone + codelength) / 8;
            bitsdone = (bitsdone + codelength) % 8;
            while (bytesdone > 0)
            {
                bytesdone--;
                bitsread = (bitsread >> 8);
                if (size > 0)
                {
                    size--;
                    bytesread++;
                    bitsread += ((rawData[++at] & 0xFF) << 16);
                }
                else
                {
                    size = (rawData[++at] & 0xFF);
                    if (size > 0)
                    {
                        size--;
                        bytesread++;
                        if (at+1 >= rawData.length)
                        {
                            bitsread += ((0xFF) << 16);
                        }
                        else
                            bitsread += ((rawData[++at] & 0xFF) << 16);
                    }
                }
            }
            // we package all the bits that are done into bytes and write them to the stream
            bitstowrite += (code << (bitsleft - codelength + 1));
            bitsleft -= codelength;
            while (bitsleft < 16)
            {
                //stream.write(bitstowrite >> 16);
                byteswritten++;
                bitstowrite = (bitstowrite & 0xFFFF) << 8;
                bitsleft += 8;
            }
            if (code == 256)
            {
                codelength = 9;
                tablelength = 257;
            }
            if (code == 257)
            {
                break;
            }
            if (tablelength == (1 << codelength))
            {
                codelength++;
            }
        }
        //streamBytes = stream.toByteArray();
        if (bytesread - byteswritten > 2)
        {
            throw new IOException("Not a supported GIF-file (unexpected end of data block).");
        }
	}
	public void writeImage(PdfOutputer pw) throws IOException
	{
		pw.writeObj("<< ");
		super.writeImage(pw);
		pw.writeAttr("/Filter", "/LZWDecode");
		pw.writeAttr("/BitsPerComponent", "8");
		pw.writeln("/DecodeParms << ");
		pw.writeAttr("/EarlyChange", "0");
		pw.writeln(">>");
		pw.write("/ColorSpace [ /Indexed /DeviceRGB ");
		// Byte 10: bit 1: Global Color Table Flag
		if ((rawData[10] & 0x80) == 0)
		{
			throw new IOException("Not a supported GIF-file (there is no global color table present).");
		}
		// Byte 10: bit 6-8: Size of Global Color Table
		int nColors = 1 << ((rawData[10] & 7) + 1);
		pw.write(nColors - 1);
		byte[] colorTable = new byte[nColors * 3];
		// Byte 13-...: Global color table
		System.arraycopy(rawData, 13, colorTable, 0, colorTable.length);
		pw.write(" <");
		pw.writeHex(colorTable);
		pw.writeln(">]");
		int at = 13 + nColors * 3;
		//byte[] env = new byte[100];
		//System.arraycopy(rawData, at, env, 0, 100);
		// IMAGE DESCRIPTOR

		while ((rawData[at] & 0xff) == 0x21) // extension introducer
		{
			at += 2; // skip extension id
			int step;
			do
			{
				step = rawData[at] & 0xff;
				at += step+1;
			} while(step != 0);
		}

		// Byte 0: Image separator
		// only simple gif files with image immediate following global color table are supported
		// 0x2c is a fixed value for the image separator
		if ((rawData[at] & 0xFF) != 0x2c)
		{
			throw new IOException("Not a supported GIF-file (the image separator '0x2c' is not found after reading the color table).");
		}
		// ignore position and size
		at += 9;
		// Byte 9: Packed Fields
		// Byte 9: bit 1: Local Color Table Flag
		// Byte 9: bit 2: Interlace Flag
		if ((rawData[at] & 0xc0) > 0)
		{
			throw new IOException("Not a supported GIF-file (interlaced gifs or gifs using local color table can't be inserted).");
		}

		// Byte 10: LZW initial code
		if (rawData[++at] != 0x08)
		{
			//byte b = rawData[at];
			throw new IOException("Not a supported GIF-file (initial LZW code not supported).");
		}
		// Read the Image Data
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int code;
		int codelength = 9;
		int tablelength = 257;
		int bitsread;
		int bitstowrite = 0;
		int bitsdone = 0;
		int bitsleft = 23;
		int bytesdone;
		int bytesread = 0;
		int byteswritten = 0;
		// read the size of the first Data Block
		int size = (rawData[++at] & 0xFF);
		// Check if there is any data in the GIF
		if (size < 1)
		{
			throw new IOException("Not a supported GIF-file. (no image data found).");
		}
		// if possible, we read the first 24 bits of data
		size--;
		bytesread++;
		bitsread = (rawData[++at] & 0xFF);
		if (size > 0)
		{
			size--;
			bytesread++;
			bitsread += ((rawData[++at] & 0xFF) << 8);
			if (size > 0)
			{
				size--;
				bytesread++;
				bitsread += ((rawData[++at] & 0xFF) << 16);
			}
		}
		while (bytesread > byteswritten)
		{
			tablelength++;
			// we extract a code with length=codelength
			code = (bitsread >> bitsdone) & ((1 << codelength) - 1);
			// we delete the bytesdone in bitsread and append the next byte(s)
			bytesdone = (bitsdone + codelength) / 8;
			bitsdone = (bitsdone + codelength) % 8;
			while (bytesdone > 0)
			{
				bytesdone--;
				bitsread = (bitsread >> 8);
				if (size > 0)
				{
					size--;
					bytesread++;
					bitsread += ((rawData[++at] & 0xFF) << 16);
				}
				else
				{
					size = (rawData[++at] & 0xFF);
					if (size > 0)
					{
						size--;
						bytesread++;
						if (at+1 >= rawData.length)
						{
							bitsread += ((0xFF) << 16);
						}
						else
							bitsread += ((rawData[++at] & 0xFF) << 16);
					}
				}
			}
			// we package all the bits that are done into bytes and write them to the stream
			bitstowrite += (code << (bitsleft - codelength + 1));
			bitsleft -= codelength;
			while (bitsleft < 16)
			{
				stream.write(bitstowrite >> 16);
				byteswritten++;
				bitstowrite = (bitstowrite & 0xFFFF) << 8;
				bitsleft += 8;
			}
			if (code == 256)
			{
				codelength = 9;
				tablelength = 257;
			}
			if (code == 257)
			{
				break;
			}
			if (tablelength == (1 << codelength))
			{
				codelength++;
			}
		}
		streamBytes = stream.toByteArray();
		if (bytesread - byteswritten > 2)
		{
			throw new IOException("Not a supported GIF-file (unexpected end of data block).");
		}
		pw.writeAttr("/Length", streamBytes.length);
		pw.writeln(" >>");
		pw.writeln("stream");
		pw.writeln(streamBytes);
		pw.writeln("endstream");
		pw.writeln("endobj");
	}
}
