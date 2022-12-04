// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//Based on iText  by Bruno Lowagie: http://www.lowagie.com/iText/

import org.pdgen.data.Trace;

import java.io.IOException;

public class PdfJpeg extends PdfImage
{
	// public final static membervariables
	/** This is a type of marker. */
	private static final int NOT_A_MARKER = -1;
	/** This is a type of marker. */
	private static final int VALID_MARKER = 0;
	/** Acceptable Jpeg markers. */
	private static final int[] VALID_MARKERS = {0xC0, 0xC1, 0xC2};
	/** This is a type of marker. */
	private static final int UNSUPPORTED_MARKER = 1;
	/** Unsupported Jpeg markers. */
	private static final int[] UNSUPPORTED_MARKERS = {0xC3, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF};
	/** This is a type of marker. */
	private static final int NOPARAM_MARKER = 2;
	/** Jpeg markers without additional parameters. */
	private static final int[] NOPARAM_MARKERS = {0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0x01};
	private static final byte[] jpegMagic = {-1, -40};
	private int colorspace;

	PdfJpeg(byte[] img, String name) throws IOException
	{
		super(JPEG, name, img);
		if (!checkBytes(jpegMagic))
			throw new IOException("Not a valid " + type + " image");
		processParameters();
	}

	/**	Returns a type of marker.
	 * @param	marker an int	 *
	 * @return	a type: <VAR>VALID_MARKER</CODE>, <VAR>UNSUPPORTED_MARKER</VAR> or <VAR>NOPARAM_MARKER</VAR>
	 */
	private static int marker(int marker)
	{
        for (int aVALID_MARKERS : VALID_MARKERS)
        {
            if (marker == aVALID_MARKERS)
            {
                return VALID_MARKER;
            }
        }
        for (int aNOPARAM_MARKERS : NOPARAM_MARKERS)
        {
            if (marker == aNOPARAM_MARKERS)
            {
                return NOPARAM_MARKER;
            }
        }
        for (int aUNSUPPORTED_MARKERS : UNSUPPORTED_MARKERS)
        {
            if (marker == aUNSUPPORTED_MARKERS)
            {
                return UNSUPPORTED_MARKER;
            }
        }
		return NOT_A_MARKER;
	}
	// private methods
	/**	 * This method checks if the image is a valid JPEG and processes some parameters.	 */
	private void processParameters() throws IOException
	{
		try
		{
			int at = 2;
			while (true)
			{
				if ((rawData[at] & 0xFF) == 0xFF)
				{
					int marker = rawData[at + 1] & 0xFF;
					int markertype = marker(marker);
					if (markertype == VALID_MARKER)
					{
						if ((rawData[at + 4] & 0xFF) != 0x08)
						{
							throw new IOException("Jpeg image must have 8 bits per component.");
						}
						height = (rawData[at + 5] << 8) + (rawData[at + 6] & 0xFF);
						width = (rawData[at + 7] << 8) + (rawData[at + 8] & 0xFF);
						colorspace = rawData[at + 9] & 0xFF;
						return;
					}
					else if (markertype == UNSUPPORTED_MARKER)
					{
						throw new IOException(" unsupported JPEG marker: " + marker);
					}
					else if (markertype != NOPARAM_MARKER)
					{
						Trace.check(((rawData[at + 2] << 8) >= 0), "Negative unsigned byte reading jpeg");
						at = at + (rawData[at + 2] << 8) + (rawData[at + 3] & 0xFF) + 2;
					}
				}
				else
					throw new IOException("Not a valid Jpeg image (marker not found)");
			}
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			throw new IOException("Not a valid Jpeg image (array overflow parsing size)");
		}
	}

	public void writeImage(PdfOutputer pw) throws IOException
	{
		pw.writeObj("<< ");
		super.writeImage(pw);
		pw.writeAttr("/Filter", "/DCTDecode");
		pw.writeAttr("/BitsPerComponent", "8");
		switch (colorspace)
		{
			case 1:
				pw.writeAttr("/ColorSpace", "/DeviceGray");
				break;
			case 3:
				pw.writeAttr("/ColorSpace", "/DeviceRGB");
				break;
			default:
				pw.writeAttr("/ColorSpace", "/DeviceGray");
		}
		pw.writeAttr("/Length", rawData.length);
		pw.writeln(">>\nstream");
		pw.writeln(rawData);
		pw.writeln("endstream");
		pw.writeln("endobj");
	}
}
