// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;
//MARKER The strings in this file shall not be translated

import org.pdgen.env.Env;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;
import org.pdgen.model.run.PdfImage;
import org.pdgen.model.run.PdfOutputer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.zip.Deflater;

/**
 * User: patrick
 * Date: Nov 15, 2004
 * Time: 9:54:47 AM
 */
public class PdfOutput implements PdfOutputer {
    // the real ouput stream
    private final OutputStream myOutfile;
    // conversion buffer for specialized conversions
    private final byte[] conversionBuffer = new byte[35];
    private int conversionLength;
    private int conversionOffset;
    // offset or positions of objects in the pdf-file. Needed for the xref table
    private int[] offsets = new int[10240];
    private int offsetIx = 1;
    // current offset or position in the pdf-file.
    private int outPos;
    // object numbers of the pages. Needed for the page directory
    private int[] pages = new int[1024];
    private int pageObjCounter;
    // map from fontname to fontobject
    private final Hashtable<String, RegFont> fonts = new Hashtable<>();
    // counter to create unique pdf font name elements
    private int fontCounter = 1;
    // map from image file name or awt.image to to a PDFImage
    private final Hashtable<Object, PdfImage> images = new Hashtable<>();
    private final ArrayList<PdfImage> imageList = new ArrayList<>();
    // counter to create unique pdf image name elements
    private int imageCounter = 1;
    private final PdfGState[] fillGState = new PdfGState[256];
    private final PdfGState[] strokeGState = new PdfGState[256];
    private final ArrayList<PdfGState> gstates = new ArrayList<>();
    private int gstateCounter = 1;
    private final ArrayList<PdfShading> shades = new ArrayList<>();
    private final List<String> fontNamesList = new ArrayList<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final int shadesCounter = 1;
    // to be better able to compress the page, page bytes are buffered here
    // TO DO move the next 3 variables to a CompressedByteBuffer
    //private byte[] pageBytes = new byte[8192];
    //private int pageBytePos = 0;
    //private byte[] outBytes = new byte[8192];
    private final CompressedByteBuffer compressedOutput;
    // buffer to convert a string to a byte array
    private byte[] stringBuffer = new byte[1024];
    private int stringLength;
    // size of the output page
    private Paper paper;
    Graphics2D g2d;
    // the current active font
    private byte[] currentFontName;
    private Font currentFont;
    private float currentFontSize = 12;
    private Color nonStrokeColor;
    private Color strokeColor;
    private Deflater deflater;
    // well known fixed strings and bytes array to output into pdf file
    private static final String header = "%PDF-1.4\n%ÄÖÜß";
    private static final String catalog = "<< /Type /Catalog /Outlines 3 0 R /Pages 4 0 R /OutputIntents [6 0 R] /Metadata 8 0 R >>\nendobj";
    private static final String outlines = "<< /Type /Outlines /Count 0 >>\nendobj";
    private final DecimalFormat format = new DecimalFormat("0000000000");
    private static final byte[] setGS = {' ', 'g', 's', '\n'};
    private static final byte[] moveto = {'m', '\n'};
    private static final byte[] lineto = {'l', '\n'};
    private static final byte[] curveto = {'c', '\n'};
    private static final byte[] close = {'h', '\n'};
    private static final byte[] fill = {'f', '\n'};
    private static final byte[] eoFill = {'f', '*', ' ', '\n'};
    //private static final byte[] fillStroke = {'B', '\n'};
    //private static final byte[] eoFillStroke = {'B', '*', '\n'};
    //private static final byte[] closePathFillStroke = {'b', '\n'};
    //private static final byte[] closePathEoFillStroke = {'b', '*', '\n'};
    private static final byte[] stroke = {'S', '\n'};
    //private static final byte[] closePathStroke = {'s', '\n'};
    private static final byte[] newPath = {'n', ' '};
    private static final byte[] clip = {'W', ' '};
    private static final byte[] eoClip = {'W', '*', ' '};
    //private static final byte[] appto = {'v', '\n'};
    private static final byte[] beginText = {'B', 'T', ' '};
    private static final byte[] positionText = {' ', 'T', 'd', ' '};
    private static final byte[] riseText = {' ', 'T', 's', ' '};
    private static final byte[] beginPaintText = {'('};
    private static final byte[] beginPaintTextHex = {'('};
    private static final byte[] selectFont = {' ', 'T', 'f', ' '};
    //private static final byte[] paintLine = {' ', '\'', ' '};
    private static final byte[] paintText = {')', 'T', 'j', ' '};
    private static final byte[] paintTextHex = {')', 'T', 'j', ' '};
    private static final byte[] endText = {'E', 'T', '\n'};
    private static final byte[] colorRGBNonStroke = {' ', 'r', 'g', '\n'};
    private static final byte[] colorRGBStroke = {' ', 'R', 'G', '\n'};
    //private static final byte[] clipToPath = {'W', ' ', 'n', '\n'};
    //private static final byte[] popPushContext = {'Q', ' ', 'q', ' '};
    private static final byte[] pushContext = {'q', ' '};
    private static final byte[] popContext = {'Q', '\n'};
    private static final byte[] noDash = {' ', '[', ']', ' ', '0', ' ', 'd', ' '};
    private static final byte[] rectangle = {'r', 'e', ' '};
    private static final byte[] z = {'0', ' '};
    private static final byte[] z2 = {'0', ' ', '0', ' '};
    private static final byte[] coordinateMatrix = {'c', 'm', ' '};
    private static final byte[] imagePopContext = {' ', 'D', 'o', ' ', 'Q', '\n'};
    private static final byte[] lineDefault = {'0', ' ', 'J', '\n', '0', ' ', 'j', '\n', '1', '0', ' ', 'M', '\n'};
    private static final byte[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final byte[] outObj = {'0', ' ', 'o', 'b', 'j', '\n'};
    private static final byte[] textMatrix = {'T', 'm', '\n'};
    //private static final byte[] characterSpacing = {'T', 'c'};
    private static final byte[] eol = {'\n'};
    //private Rectangle defaultClip;
    //private Rectangle clipBounds;
    private float lastX;
    private float lastY;
    private final String producer;
    private final String title;

    public PdfOutput(OutputStream outFile, Paper pap, String title, Graphics2D g2d) throws IOException {
        myOutfile = outFile;
        paper = pap;
        this.g2d = g2d;
        this.title = title;
        String compress = Settings.get("compressPDF");
        if (!"no".equalsIgnoreCase(compress))
            deflater = new Deflater();
        compressedOutput = new CompressedByteBuffer(deflater);
        //defaultClip = clipBounds = new Rectangle(0, 0, (int) Math.round(paper.getWidth()), (int) Math.round(paper.getHeight()));
        writeln(header);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        final Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        writeObj("<< /CreationDate (D:");
        write(dateString);
        write(") /Creator (");
        final String creator = "pdgen.org";
        write(creator);
        write(") /Producer (");
        producer = Res.productName() + " v" + Res.version;
        write(Res.productName());
        write("v");
        convertString(Res.version);
        write(stringBuffer, stringLength);
        write(")");
        if (title != null) {
            write(" /Title (");
            convertString(title);
            write(stringBuffer, stringLength);
            write(")");
        }
        writeln(" >>\nendobj");
        writelnObj(catalog);
        writelnObj(outlines);
        reserveObjNum();
        reserveObjNum();
        reserveObjNum(); // intents
        reserveObjNum(); // intentsStream
        reserveObjNum(); //  meta data
    }

    public void end() throws IOException {
        if (pageObjCounter == 0) {
            startPage();
            endPage();
        }
        ArrayList<XObject> xObjects = new ArrayList<>();
        for (PdfImage anImageList : imageList) {
            XObject xo = new XObject(anImageList.getName(), offsetIx);
            xObjects.add(xo);
            anImageList.oid = xo.oid;
            anImageList.writeImage(this);
        }
        writeObj(4, "/Type /Pages ");
        writeAttr("/Resources", "5 0 R");
        writeAttr("/Count", pageObjCounter);
        write("/Kids [ ");
        for (int i = 0; i < pageObjCounter; i++) {
            write(pages[i]);
            write("0 R ");
        }
        writeln("] >>\nendobj");
        writeObj(5, "/ProcSet [ /PDF /Text /ImageC ]");
        if (xObjects.size() > 0) {
            write("\n/XObject << ");
            for (XObject xObject : xObjects) {
                write(xObject.name);
                write(" ");
                write(xObject.oid);
                write("0 R ");
            }
            write(">> ");
        }
        int fontIx = offsetIx;
        write("\n/Font << ");
        for (String fontName : fontNamesList) {
            RegFont rf = fonts.get(fontName);
            write(rf.name);
            write(" ");
            write(fontIx);
            fontIx += rf.isStandardFont ? 1 : 2;
            fontIx += rf.isEmbedFont() ? 3 : 0;
            write("0 R ");
        }
        writeln(">>");
        int gstateIx = fontIx;
        if (gstates.size() > 0) {
            writeln("/ExtGState <<");
            for (PdfGState gstate : gstates) {
                write(gstate.outName);
                write(" ");
                write(gstateIx);
                gstateIx++;
                write("0 R ");
            }
            writeln(">>");
        }
        int shadeIx = gstateIx;
        if (shades.size() > 0) {
            writeln("/Pattern <<");
            for (PdfShading shade : shades) {
                write(shade.outName);
                write(" ");
                write(shadeIx);
                shadeIx++;
                write("0 R ");
            }
            writeln(">>");
        }
        writeln(">>\nendobj");
        writeObj(6, "/Type/OutputIntent/S/GTS_PDFA1/OutputConditionIdentifier(sRGB IEC61966-2.1)/DestOutputProfile 7 0 R>>\nendobj\n");
        writeObj(7, "/N 3/Length 367/Filter/FlateDecode>>\n");
        writeln("stream");
        //byte[] data = new byte[367];
        try (InputStream fis = getClass().getResourceAsStream("destOutputProfile.bin")) {
            if (fis == null) {
                throw new JoriaInternalError("destOutputProfile.bin not found in classpath");
            }
            int available = fis.available();
            byte[] data = fis.readAllBytes();
            if (data.length != 367) {
                throw new JoriaInternalError("destOutputProfile.bin has wrong length");
            }
            write(data);
        }
        writeln("\nendstream");
        writeln("endobj");
        UUID uuid = UUID.randomUUID();
        writeMetaData(uuid);
        for (String fontName : fontNamesList) {
            RegFont rf = fonts.get(fontName);
            writeFont(rf);
        }
        //		for (Map.Entry<String, RegFont> aFontSet1 : fontSet)
        //		{
        //			writeFont(aFontSet1.getValue());
        //		}
        for (PdfGState gstate1 : gstates) {
            writeGState(gstate1);
        }
        for (PdfShading shade1 : shades) {
            writeShade(shade1);
        }
        int xrefPos = outPos;
        write("xref\n0 ");
        writeln(offsetIx);
        writeln("0000000000 65535 f ");
        for (int i = 1; i < offsetIx; i++) {
            write(format.format(offsets[i]));
            writeln(" 00000 n ");
        }
        writeln("trailer\n<<");
        writeAttr("/ID", "[(" + uuid + ") (" + uuid + ")]");
        writeAttr("/Size", offsetIx);
        writeAttr("/Info", "1 0 R");
        writeln("/Root 2 0 R\n>>\nstartxref");
        writeln(xrefPos);
        writeln("%%EOF");
        myOutfile.close();
    }

    public void startPage() throws IOException {
        if (pageObjCounter >= pages.length) {
            int[] newPages = new int[pageObjCounter * 2];
            System.arraycopy(pages, 0, newPages, 0, pages.length);
            pages = newPages;
        }
        int pageIx = offsetIx;
        pages[pageObjCounter++] = pageIx;
        writeObj("<< /Type /Page /Parent 4 0 R /Resources 5 0 R /MediaBox [0 0 ");
        write((float) paper.getWidth());
        write((float) paper.getHeight());
        writeln(" ] /Contents " + (pageIx + 1) + " 0 R >>\nendobj");
        writeToPage('q');
        writeToPage('\n');
        // initialisiere die Linien
        writeToPage(lineDefault);
    }

    public void endPage() throws IOException {
        writeToPage('\n');
        writeToPage('Q');
        compressedOutput.outputToPdf(this);
        //		if (deflater != null)
        //		{
        //			deflater.reset();
        //			deflater.setInput(pageBytes, 0, pageBytePos);
        //			if (outBytes.length < pageBytes.length)
        //			{
        //				outBytes = new byte[pageBytes.length];
        //			}
        //			deflater.finish();
        //			int deflated = deflater.deflate(outBytes);
        //			writelnObj("<< /Filter /FlateDecode /Length " + deflated + " >>\nstream");
        //			writeln(outBytes, deflated);//
        //			writeln("endstream\nendobj");
        //		}
        //		else
        //		{
        //			writelnObj("<< /Length " + pageBytePos + " >>\nstream");
        //			writeln(pageBytes, pageBytePos);//
        //			writeln("endstream\nendobj");
        //		}
        //		//pageBytes = null;
        //		pageBytePos = 0;
    }

    public void writeBeginTextToPage() {
        writeToPage(beginText);
        writeToPage(currentFontName);
        writeToPage(' ');
        writeToPage(currentFontSize);
        writeToPage(selectFont);
        writeToPage(' ');
    }

    public void writeFontToPage() {
        writeToPage(currentFontName);
        writeToPage(' ');
        writeToPage(currentFontSize);
        writeToPage(selectFont);
        writeToPage(' ');
    }

    public void writeTextPositionToPage(float posx, float posy) {
        writeToPage(posx);
        writeToPage(posy);
        writeToPage(positionText);
    }

    public void writeTextRise(float rise) {
        writeToPage(rise);
        writeToPage(riseText);
    }

    public void writeTextWithPositionToPage(float posx, float posy, String txt, RegFont rf) {
        writeToPage(posx);
        writeToPage(posy);
        writeToPage(positionText);
        if (rf.trueTypeFont != null)
            writeToPage(beginPaintTextHex);
        else
            writeToPage(beginPaintText);
        convertString(txt, rf);
        writeToPage(stringBuffer, stringLength);
        if (rf.trueTypeFont != null)
            writeToPage(paintTextHex);
        else
            writeToPage(paintText);
    }

    /*
    public void writeCharacterSpacingToPage(float space)
    {
        writeToPage(space);
        writeToPage(characterSpacing);
    }
    */

    public void writeEndTextToPage() {
        writeToPage(endText);
    }

    public void writeEOLToPage() {
        writeToPage(eol);
    }

    public Font getFont() {
        return currentFont;
    }

    public void fillRectangle(Rectangle2D.Float re, boolean withClip) {
        writeRectangleToPage(re.x, re.y, re.width, re.height);
        if (withClip) {
            writeToPage(clip);
        }
        writeToPage(fill);
    }

    public void writeRectangleToPage(float x, float y, float w, float h) {
        writeToPage(x);
        writeToPage(y);
        writeToPage(w);
        writeToPage(h);
        writeToPage(rectangle);
    }

    private void writeShade(PdfShading pdfShading) throws IOException {
        writelnObj("<< /Type /Pattern");
        writeln("/PatternType 2");
        writeln("/Shading <<");
        writeln("/ShadingType 2");
        writeln("       /ColorSpace /DeviceRGB");
        write("     /Background [ ");
        writeRGBArray(pdfShading.c1);
        writeln(" ]");
        write("     /Coords [ ");
        write((float) pdfShading.p1.getX());
        write(" ");
        write((float) pdfShading.p1.getY());
        write(" ");
        write((float) pdfShading.p2.getX());
        write(" ");
        write((float) pdfShading.p2.getY());
        writeln(" ]");
        writeln("       /Extend [ true true ]");
        write("     /Function <<");
        writeln("           /FunctionType 2");
        writeln("           /Domain [ 0.0 1.0]");
        write("         /C0 [ ");
        writeRGBArray(pdfShading.c1);
        writeln(" ]");
        write("         /C1 [ ");
        writeRGBArray(pdfShading.c2);
        writeln(" ]");
        writeln("           /N 1");
        writeln("       >>");
        writeln("   >>");
        writeln(">>\nendobj");
    }

    private void writeRGBArray(Color c) throws IOException {
        write((float) (c.getRed() / 255.));
        write(" ");
        write((float) (c.getGreen() / 255.));
        write(" ");
        write((float) (c.getBlue() / 255.));
    }

    private void writeGState(@SuppressWarnings("UnusedParameters") PdfGState pdfGState) throws IOException {
        writelnObj("<< /Type /ExtGState");
        //if (pdfGState.fillOpacity != null)
        //{
			/*
						write("/ca ");
						writeln(pdfGState.fillOpacity.floatValue());
						*/
        //}
        //if (pdfGState.strokeOpacity != null)
        //{
			/*
						write("/CA ");
						writeln(pdfGState.strokeOpacity.floatValue());
						*/
        //}
        writeln(">>\nendobj");
    }

    private void writeFont(RegFont rf) throws IOException {
        if (rf.trueTypeFont != null) {
            int fontFileOid = offsetIx + 1;
            int fontDescriptorOid = offsetIx + 2;
            int descendantFontOid = offsetIx + 3;
            int toUnicodeOid = offsetIx + 4;
            StringBuilder s = new StringBuilder();
            for (int k = 0; k < 6; ++k) {
                s.append((char) (Math.random() * 26 + 'A'));
            }
            s.append("+").append(rf.pdfName);
            byte[] pdfName = convertName(s.toString());
            // font object
            writeObj("<< ");
            writeAttr("/Type", "/Font");
            //writeAttr("/Name", rf.name);
            write("/BaseFont ");
            writeln(pdfName);
            writeAttr("/Subtype", "/Type0");
            writeAttr("/DescendantFonts", "[" + descendantFontOid + " 0 R]");
            writeAttr("/Encoding", "/Identity-H");
            writeAttr("/ToUnicode", toUnicodeOid + " 0 R");
            writeln(">>\nendobj");
            // embeded FontFile
            if (rf.trueTypeFont.isCFFFont()) {
                compressedOutput.setSubtype("/CIDFontType0C");
                rf.trueTypeFont.buildCFFFile(compressedOutput);
            } else {
                String ignoreSubForFonts = Settings.get("doNotSubsetTrueTypeFont");
                boolean embedFullFile = false;
                if (ignoreSubForFonts != null) {
                    StringTokenizer tok = new StringTokenizer(ignoreSubForFonts, ",");
                    for (String t = tok.nextToken(); tok.hasMoreTokens(); t = tok.nextToken()) {
                        if (rf.trueTypeFont.getFamilyName().equals(t)) {
                            embedFullFile = true;
                            break;
                        }
                    }
                }
                compressedOutput.setLength1Allways(true);
                if (embedFullFile)
                    rf.trueTypeFont.buildFullFile(compressedOutput);
                else
                    rf.trueTypeFont.buildSubSetFile(compressedOutput, rf.usedGlyphs);
            }
            compressedOutput.outputToPdf(this);
            // font descriptor
            writeObj("<<");
            writeAttr("/Type", "/FontDesciptor");
            write("/FontName ");
            writeln(pdfName);
            writeAttr("/Ascent", rf.trueTypeFont.getAscent());
            writeAttr("/Descent", rf.trueTypeFont.getDescent());
            writeAttr("/CapHeight", rf.trueTypeFont.getCapHeight());
            writeAttr("/Flags", rf.trueTypeFont.getFlags());
            writeAttr("/FontBBox", rf.trueTypeFont.getBBox());
            writeAttr("/ItalicAngle", (float) rf.trueTypeFont.getItalicAngle());
            writeAttr("/StemV", "80");
            if (rf.trueTypeFont.isCFFFont())
                writeAttr("/FontFile3", fontFileOid + " 0 R");
            else
                writeAttr("/FontFile2", fontFileOid + " 0 R");
            writeln(">>\nendobj");
            // descendant font
            writeObj("<<");
            writeAttr("/Type", "/Font");
            write("/BaseFont ");
            writeln(pdfName);
            writeAttr("/CIDSystemInfo", "<</Ordering(Identity)/Registry(Adobe)/Supplement 0>>");
            writeAttr("/W", rf.trueTypeFont.buildWidthInfo(rf.usedGlyphs, rf.glyphIdToUnicode));
            writeAttr("/Subtype", "/CIDFontType2");
            writeAttr("/FontDescriptor", fontDescriptorOid + " 0 R");
            writeAttr("/DW", 1000);
            writeAttr("/CIDToGIDMap", "/Identity");
            writeln(">>\nendobj");
            // toUnicode stream
            rf.trueTypeFont.buildToUnicode(compressedOutput, rf.usedGlyphs, rf.glyphIdToUnicode);
            compressedOutput.outputToPdf(this);
        } else {
            int fontDescriptorOid = offsetIx + 1;
            writeObj("<< ");
            writeAttr("/Type", "/Font");
            writeAttr("/Name", rf.name);
            write("/BaseFont ");
            byte[] pdfName = convertName(rf.pdfName);
            writeln(pdfName);
            if (rf.isStandardFont)
                writeAttr("/Subtype", "/Type1");
            else
                writeAttr("/Subtype", "/TrueType");
            writeAttr("/Encoding", "/WinAnsiEncoding");
            if (rf.isStandardFont)
                writeln(">>\nendobj");
            else {
                writeAttr("/FirstChar", "32");
                writeAttr("/LastChar", "255");
                write("/Widths [ ");
                double ascent = 0;
                double descent = 0;
                double factor = 1000 / rf.font.getSize2D();
                for (int wi = 32; wi < 256; wi++) {
                    String s1 = String.valueOf((char) wi);
                    if (wi == 0x80) // Das Euro Zeichen liegt in Unicode wesentlich höher.
                    {
                        s1 = String.valueOf((char) 0x20AC);
                    }
                    Rectangle2D r = rf.font.getStringBounds(s1, g2d.getFontRenderContext());
                    double width = r.getWidth() * factor;
                    write((float) width);
                    ascent = Math.max(ascent, -r.getY());
                    descent = Math.max(descent, r.getHeight() + r.getY());
                }
                writeln("]");
                writeAttr("/FontDescriptor", fontDescriptorOid + " 0 R");
                writeln(">>\nendobj");
                writeObj("<<");
                writeAttr("/Type", "/FontDesciptor");
                write("/FontName ");
                writeln(pdfName);
                writeAttr("/Ascent", (float) (ascent * factor));
                writeAttr("/Descent", (float) (descent * factor));
                writeAttr("/CapHeight", "0");
                writeAttr("/Flags", "32");
                writeAttr("/FontBBox", "[ 0 0 1000 1000]");
                writeAttr("/ItalicAngle", "0");
                writeAttr("/StemV", "0");
                writeln(">>\nendobj");
            }
        }
    }

    public void writelnObj(String txt) throws IOException {
        int objNum = offsetIx;
        mark();
        write(objNum);
        write("0 obj\n");
        writeln(txt);
    }

    public void writeAttr(String key, String value) throws IOException {
        write(key);
        write(" ");
        write(value);
        write("\n");
    }

    public void writeAttr(String key, long value) throws IOException {
        write(key);
        write(" ");
        write(value);
        write("\n");
    }

    private void writeAttr(String key, float value) throws IOException {
        write(key);
        write(" ");
        write(value);
        write("\n");
    }

    public void writeObj(String txt) throws IOException {
        int objNum = offsetIx;
        mark();
        write(objNum);
        write(outObj);
        write(txt);
    }

    private void write(byte[] bytes) throws IOException {
        write(bytes, bytes.length);
    }

    public void writeln(String txt) throws IOException {
        byte[] bytes = getStringBytes(txt);
        writeln(bytes, bytes.length);
    }

    public void writeln(byte[] bytes) throws IOException {
        writeln(bytes, bytes.length);
    }

    public void write(String txt) throws IOException {
        byte[] bytes = getStringBytes(txt);
        write(bytes, bytes.length);
    }

    public void write(long value) throws IOException {
        ba(value);
        write(conversionBuffer, conversionLength, conversionOffset);
    }

    public void write(float value) throws IOException {
        ba(value);
        write(conversionBuffer, conversionLength, conversionOffset);
    }

    public void write(byte[] bytes, int length, int offset) throws IOException {
        outPos += length;
        myOutfile.write(bytes, offset, length);
    }

    private void writeln(byte[] bytes, int length, int offset) throws IOException {
        outPos += length + 1 - offset;
        myOutfile.write(bytes, offset, length);
        myOutfile.write('\n');
    }

    private void ba(long lconv) {
        long l = lconv;
        boolean minus = false;
        if (l < 0) {
            minus = true;
            l = -l;
        }
        int ins = 34;
        if (l >= 1) {
            while (l >= 1) {
                conversionBuffer[--ins] = (byte) ('0' + (l % 10));
                l = l / 10;
            }
        } else {
            conversionBuffer[--ins] = (byte) '0';
        }
        if (minus) {
            conversionBuffer[--ins] = '-';
        }
        conversionLength = 35 - ins;
        //System.arraycopy(conversionBuffer, ins, conversionBuffer, 0, conversionLength - 1);
        conversionOffset = ins;
        conversionBuffer[conversionOffset + conversionLength - 1] = (byte) ' ';
    }

    private void ba(float fconv) {
        // optimierung fuer 0 und 1 (kommt bei den Farben haeufig vor
        if (fconv == 0.0) {
            //hmf 030312: add blank at end
            conversionBuffer[0] = '0';
            conversionBuffer[1] = ' ';
            conversionLength = 2;
            conversionOffset = 0;
            return;
        } else if (fconv == 1.0) {
            conversionBuffer[0] = '1';
            //hmf 030312: add blank at end
            conversionBuffer[1] = ' ';
            conversionLength = 2;
            conversionOffset = 0;
            return;
        }
        float f = fconv;
        boolean minus = false;
        if (f < 0) {
            minus = true;
            f = -f;
        }
        float ff = f % 1;
        int ins = 30;
        if (f >= 1) {
            while (f >= 1) {
                int mod10 = (int) (f % 10);
                conversionBuffer[--ins] = (byte) ('0' + mod10);
                f = f / 10;
            }
        } else {
            conversionBuffer[--ins] = (byte) '0';
        }
        if (ff >= 0.001) {
            conversionBuffer[30] = (byte) '.';
            for (int i = 31; i < 34; i++) {
                ff = ff * 10;
                conversionBuffer[i] = (byte) ('0' + (ff % 10));
            }
            conversionLength = 35 - ins;
        } else {
            conversionLength = 31 - ins;
        }
        if (minus) {
            conversionLength++;
            conversionBuffer[--ins] = '-';
        }
        conversionOffset = ins;
        conversionBuffer[conversionOffset + conversionLength - 1] = (byte) ' ';
    }

    public void write(byte[] bytes, int length) throws IOException {
        outPos += length;
        myOutfile.write(bytes, 0, length);
    }

    public void writeHex(byte[] bytes) throws IOException {
        for (byte b : bytes) {
            myOutfile.write(hex[(b >> 4) & 15]);
            myOutfile.write(hex[b & 15]);
            myOutfile.write(' ');
        }
        outPos += bytes.length * 3;
    }

    public void writeln(byte[] bytes, int length) throws IOException {
        outPos += length + 1;
        myOutfile.write(bytes, 0, length);
        myOutfile.write('\n');
    }

    private void mark() {
        if (offsets.length <= offsetIx) {
            int[] newOffsets = new int[offsetIx * 2];
            System.arraycopy(offsets, 0, newOffsets, 0, offsets.length);
            offsets = newOffsets;
        }
        offsets[offsetIx] = outPos;
        offsetIx++;
    }

    private byte[] getStringBytes(String txt) {
        try {
            return txt.getBytes("Cp1252");
        } catch (UnsupportedEncodingException e) {
            Env.instance().handle(e);
            return txt.getBytes();
        }
    }
    //	private void reservePageBufferSpace(int bytesToAdd)
    //	{
    //		if (pageBytes.length <= pageBytePos + bytesToAdd)
    //		{
    //			byte[] newBytes = new byte[pageBytes.length * 2];
    //			System.arraycopy(pageBytes, 0, newBytes, 0, pageBytePos);
    //			pageBytes = newBytes;
    //		}
    //	}

    private void writeToPage(char c) {
        //reservePageBufferSpace(1);
        //pageBytes[pageBytePos++] = (byte) c;
        compressedOutput.add(new byte[]{(byte) c});
    }

    private void writeToPage(float f) {
        ba(f);
        writeToPage(conversionBuffer, conversionLength, conversionOffset);
    }

    private void writeToPage(long l) {
        ba(l);
        writeToPage(conversionBuffer, conversionLength, conversionOffset);
    }

    private void writeToPage(byte[] bytes, int length) {
        //reservePageBufferSpace(length);
        //System.arraycopy(bytes, 0, pageBytes, pageBytePos, length);
        //pageBytePos += length;
        compressedOutput.add(bytes, length, 0);
    }

    private void writeToPage(byte[] bytes, int length, int offset) {
        //		reservePageBufferSpace(length);
        //		System.arraycopy(bytes, offset, pageBytes, pageBytePos, length);
        //		//exp hmf 030312: wenn offset > length dann kann pageBytePos negativ werden
        //		//old: pageBytePos += length - offset;
        //		pageBytePos += length;
        compressedOutput.add(bytes, length, offset);
    }

    private void writeToPage(byte[] bytes) {
        writeToPage(bytes, bytes.length);
    }

    private void writeToPage(String txt) {
        convertString(txt, null);
        writeToPage(stringBuffer, stringLength);
    }

    private void convertString(String input) {
        convertString(input, null);
    }

    private void convertString(String input, RegFont rf) {
        try {
            byte[] b;
            if (rf != null && rf.trueTypeFont != null) {
                int bufferlength = input.length() * 4;
                sizeSringBuffer(bufferlength);
                b = rf.convertText(input);
            } else {
                b = input.getBytes("Cp1252");
            }
            int bufferlength = b.length * 4;
            sizeSringBuffer(bufferlength);
            stringLength = 0;
            for (byte aB : b) {
                switch (aB) {
                    case '\n':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = 'n';
                        break;
                    case '\r':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = 'r';
                        break;
                    case '\t':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = 't';
                        break;
                    case '\b':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = 'b';
                        break;
                    case '\f':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = 'f';
                        break;
                    case '(':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = '(';
                        break;
                    case ')':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = ')';
                        break;
                    case '\\':
                        stringBuffer[stringLength++] = '\\';
                        stringBuffer[stringLength++] = '\\';
                        break;
                    default:
                        int by = aB;
                        if (by < 0) {
                            by += 256;
                        }
                        if (by < 32 && (rf == null || rf.trueTypeFont == null)) {
                            stringBuffer[stringLength++] = '\\';
                            stringBuffer[stringLength++] = '0';
                            stringBuffer[stringLength++] = (byte) ('0' + (by / 8));
                            stringBuffer[stringLength++] = (byte) ('0' + (by % 8));
                        } else {
                            stringBuffer[stringLength++] = aB;
                        }
                        break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new JoriaInternalError("Java implementation does not support the required encoding", e);
        }
    }

    private void sizeSringBuffer(int needed) {
        if (stringBuffer.length < needed) {
            int newlength = stringBuffer.length;
            while (newlength < needed) {
                newlength *= 2;
            }
            stringBuffer = new byte[newlength];
        }
    }

    private byte[] convertName(String input) {
        byte[] b = input.getBytes(StandardCharsets.UTF_8);
        int bufferlength = b.length * 3 + 1;
        sizeSringBuffer(bufferlength);
        stringLength = 0;
        stringBuffer[stringLength++] = '/';
        for (byte aB : b) {
            if (aB < 32 || aB > 126) {
                stringBuffer[stringLength++] = '#';
                stringBuffer[stringLength++] = hex[aB / 16];
                stringBuffer[stringLength++] = hex[aB % 16];
            } else if (aB != 32) {
                stringBuffer[stringLength++] = aB;
            }
        }
        byte[] res = new byte[stringLength];
        System.arraycopy(stringBuffer, 0, res, 0, stringLength);
        return res;
    }

    private void reserveObjNum() {
        offsetIx++;
    }

    public RegFont setFont(Font f) {
        RegFont rf = registerFont(f);
        currentFontName = rf.byteName;
        currentFontSize = f.getSize2D();
        currentFont = f;
        return rf;
    }

    private RegFont registerFont(Font f)//throws IOException
    {
        String nfn = f.getName();
        TrueTypeFont trueTypeFont;
        trueTypeFont = TrueTypeFont.findFont(f.getName(), f.isBold(), f.isItalic());
        if (trueTypeFont != null && trueTypeFont.isDoNotEmbed())
            trueTypeFont = null;
        if (f.isBold()) {
            if (f.isItalic())
                nfn += ",BoldItalic";
            else
                nfn += ",Bold";
        } else if (f.isItalic())
            nfn += ",Italic";
        RegFont ff = fonts.get(nfn);
        if (ff == null) {
            String fn = "/F" + Long.toString(fontCounter++);
            RegFont rf = new RegFont(fn, nfn, f, false, trueTypeFont);
            fonts.put(nfn, rf);
            fontNamesList.add(nfn);
            return rf;
        } else
            return ff;
    }

    private void writeObj(int number, String txt) throws IOException {
        offsets[number] = outPos;
        write(number);
        write("0 obj\n<< ");
        write(txt);
        //write(" ");
    }

    private void writeln(long value) throws IOException {
        ba(value);
        writeln(conversionBuffer, conversionLength, conversionOffset);
    }

    /*
	public void writeln(float value) throws IOException
	{
		ba(value);
		writeln(conversionBuffer, conversionLength, conversionOffset);
	}
    */

    public void setNonStrokeColor(Color color) {
        if (color.equals(nonStrokeColor)) {
            return;
        }
        writeToPage((float) color.getRed() / 255f);
        writeToPage((float) color.getGreen() / 255f);
        writeToPage((float) color.getBlue() / 255f);
        writeToPage(colorRGBNonStroke);
        nonStrokeColor = color;
    }

    public void setStrokeColor(Color color) {
        if (color.equals(strokeColor)) {
            return;
        }
        writeToPage((float) color.getRed() / 255f);
        writeToPage((float) color.getGreen() / 255f);
        writeToPage((float) color.getBlue() / 255f);
        writeToPage(colorRGBStroke);
        strokeColor = color;
    }

    public String addImage(PdfImage img) {
        String name = "/I" + imageCounter++;
        imageList.add(img);
        return name;
    }

    public void paintImage(Icon img, Object storedData, float posX, float posY, float scaleWidth, float scaleHeight) {
        PdfImage pi;
        if (storedData != null) {
            pi = images.get(storedData);
        } else
            pi = images.get(img);
        if (pi == null) {
            try {
                String name = "/I" + imageCounter++;
                pi = PdfImage.getInstance(storedData, name, img, this);
                if (pi == null)
                    return;
                if (storedData != null)
                    images.put(storedData, pi);
                else
                    images.put(img, pi);
                imageList.add(pi);
            } catch (IOException ex) {
                throw new JoriaInternalError("An unexpected error occured when drawing an image in pdf export", ex);
            }
        }
        float width = img.getIconWidth();
        float height = img.getIconHeight();
        if (!Float.isNaN(scaleWidth)) {
            width *= scaleWidth;
            height *= scaleHeight;
        }
        writeToPage(pushContext);
        writeToPage(1);
        writeToPage(z2);
        writeToPage(1);
        writeToPage(posX);
        writeToPage(posY);
        writeToPage(coordinateMatrix);
        writeToPage(width);
        writeToPage(z2);
        writeToPage(height);
        writeToPage(z2);
        writeToPage(coordinateMatrix);
        writeToPage(pi.getName());
        writeToPage(imagePopContext);
    }

    public void writeLineDashPatternToPage(float on, float off) {
        if (on == 0.0 || off == 0.0)
            writeToPage(noDash);
        else {
            writeToPage(" [");
            writeToPage(on);
            writeToPage(off);
            writeToPage("] 0 d ");
        }
    }

    public void writeLineDashPatternToPage(float[] pattern, float phase) {
        writeToPage(" [");
        for (float v : pattern) {
            writeToPage(v);
        }
        writeToPage("] ");
        writeToPage(phase);
        writeToPage("d ");
    }

    public void writeLineWidthToPage(float width) {
        writeToPage(width);
        writeToPage('w');
        writeToPage(' ');
    }

    public void writeLineJoinToPage(float width) {
        writeToPage(width);
        writeToPage('j');
        writeToPage(' ');
    }

    public void writeLineCapToPage(float width) {
        writeToPage(width);
        writeToPage('J');
        writeToPage(' ');
    }

    public void writeMilterLimitToPage(float width) {
        writeToPage(width);
        writeToPage('M');
        writeToPage(' ');
    }

    public void writeMoveToToPage(float x, float y) {
        writeToPage(x);
        writeToPage(y);
        writeToPage(moveto);
        lastX = x;
        lastY = y;
    }

    public void writeLineToToPage(float x, float y) {
        writeToPage(x);
        writeToPage(y);
        writeToPage(lineto);
        lastX = x;
        lastY = y;
    }

    public void writeCubeToToPage(float x1, float y1, float x2, float y2, float x3, float y3) {
        writeToPage(x1);
        writeToPage(y1);
        writeToPage(x2);
        writeToPage(y2);
        writeToPage(x3);
        writeToPage(y3);
        writeToPage(curveto);
        lastX = x3;
        lastY = y3;
    }

    public void writeQuadToToPage(float x1, float y1, float x2, float y2) {
        float c1x = lastX + (x1 - lastX) * 2 / 3;
        float c1y = lastY + (y1 - lastY) * 2 / 3;
        float c2x = x2 - (x2 - x1) * 2 / 3;
        float c2y = y2 - (y2 - y1) * 2 / 3;
        writeCubeToToPage(c1x, c1y, c2x, c2y, x2, y2);
    }

    public void writeCloseToPage() {
        writeToPage(close);
    }

    public void writeStrokeToPage() {
        writeToPage(stroke);
    }

    /*
	public void writeClosePathStrokeToPage()
	{
		writeToPage(closePathStroke);
	}
	*/

    public void writeFillToPage() {
        writeToPage(fill);
    }

    public void writeEoFillToPage() {
        writeToPage(eoFill);
    }

    /*
	public void writeFillStrokeToPage()
	{
		writeToPage(fillStroke);
	}

	public void writeEoFillStrokeToPage()
	{
		writeToPage(eoFillStroke);
	}

	public void writeClosePathFillStrokeToPage()
	{
		writeToPage(closePathFillStroke);
	}

	public void writeClosePathEoFillStrokeToPage()
	{
		writeToPage(closePathEoFillStroke);
	}
	*/

    public void writeNewPathToPage() {
        writeToPage(newPath);
    }

    public void writePushContext() {
        writeToPage(pushContext);
    }

    public void writePopContext() {
        writeToPage(popContext);
        strokeColor = null;
        nonStrokeColor = null;
    }

    public void writeEoClipToPage() {
        writeToPage(eoClip);
    }

    public void writeClipToPage() {
        writeToPage(clip);
    }

    public void setFillAlpha(int alpha) {
        PdfGState gstate = fillGState[alpha];
        if (gstate == null) {
            gstate = newGState();
            fillGState[alpha] = gstate;
            gstate.setFillOpacity((float) (alpha / 255.0));
        }
        writeToPage(gstate.outName);
        writeToPage(setGS);
    }

    public void setStrokeAlpha(int alpha) {
        PdfGState gstate = strokeGState[alpha];
        if (gstate == null) {
            gstate = newGState();
            strokeGState[alpha] = gstate;
            gstate.setStrokeOpacity((float) (alpha / 255.0));
        }
        writeToPage(gstate.outName);
        writeToPage(setGS);
    }

    private PdfGState newGState() {
        String name = "/g" + (gstateCounter++);
        byte[] outName = getStringBytes(name);
        PdfGState gstate = new PdfGState(outName);
        gstates.add(gstate);
        return gstate;
    }

    public void setStrokeShade(Color c1, Color c2, Point2D p1, Point2D p2) {
        PdfShading shade = newShading(c1, c2, p1, p2);
        writeToPage("/Pattern CS");
        writeToPage(shade.outName);
        writeToPage(" SCN\n");
    }

    public void setFillShade(Color c1, Color c2, Point2D p1, Point2D p2) {
        PdfShading shade = newShading(c1, c2, p1, p2);
        writeToPage("/Pattern cs ");
        writeToPage(shade.outName);
        writeToPage(" scn\n");
    }

    private PdfShading newShading(Color c1, Color c2, Point2D p1, Point2D p2) {
        String name = "/p" + (shadesCounter);
        byte[] outName = getStringBytes(name);
        PdfShading shade = new PdfShading(outName, c1, c2, p1, p2);
        shades.add(shade);
        return shade;
    }

    public void writeTextMatrixToPage(float a, float b, float c, float d, float e, float f) {
        writeToPage(a);
        writeToPage(b);
        writeToPage(c);
        writeToPage(d);
        writeToPage(e);
        writeToPage(f);
        writeToPage(textMatrix);
    }

    public void writeTextToPage(String txt, RegFont rf) {
        if (rf.trueTypeFont != null)
            writeToPage(beginPaintTextHex);
        else
            writeToPage(beginPaintText);
        convertString(txt, rf);
        writeToPage(stringBuffer, stringLength);
        if (rf.trueTypeFont != null)
            writeToPage(paintTextHex);
        else
            writeToPage(paintText);
    }

    public void startSection(Paper pageFormat) {
        paper = pageFormat;
    }

    public void writeRotate(boolean clockwise, float x, float y) {
        writeToPage(1);
        writeToPage(z2);
        writeToPage(1);
        writeToPage(x);
        writeToPage(y);
        writeToPage(coordinateMatrix);
        writeToPage(z);
        writeToPage(clockwise ? -1 : 1);
        writeToPage(clockwise ? 1 : -1);
        writeToPage(z);
        writeToPage(z2);
        writeToPage(coordinateMatrix);
    }

    public static class XObject {
        XObject(String name, int oid) {
            this.name = name;
            this.oid = oid;
        }

        public String name;
        public int oid;
    }

    public class RegFont {
        RegFont(String name, String pdfName, Font font, boolean isStandardFont, TrueTypeFont trueTypeFont) {
            this.name = name;
            byteName = name.getBytes();
            this.font = font;
            this.pdfName = pdfName;
            this.isStandardFont = isStandardFont;
            this.trueTypeFont = trueTypeFont;
            if (trueTypeFont != null) {
                usedGlyphs = new boolean[trueTypeFont.getNumberOfGlyphs()];
                glyphIdToUnicode = new HashMap<>();
            }
        }

        boolean isEmbedFont() {
            return trueTypeFont != null;
        }

        public String name;
        byte[] byteName;
        String pdfName;
        public Font font;
        boolean isStandardFont;
        TrueTypeFont trueTypeFont;
        boolean[] usedGlyphs;
        Map<Character, Character> glyphIdToUnicode;

        byte[] convertText(String text) throws UnsupportedEncodingException {
            char[] glyphs = new char[text.length()];
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch == '\t')
                    ch = ' ';
                char gl = trueTypeFont.getGlyph(ch);
                usedGlyphs[gl] = true;
                glyphs[i] = gl;
                glyphIdToUnicode.put(gl, ch);
            }
            String n = new String(glyphs);
            return n.getBytes("UnicodeBigUnmarked");
        }
    }

    private void writeMetaData(UUID uuid) throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put("/Subtype", "/XML");
        properties.put("/Type", "/Metadata");
        StringBuilder xml = new StringBuilder();
        xml.append("<?xpacket begin=\"\ufeff\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n");
        xml.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xml.append(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
        xml.append("  <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:about=\"\">\n");
        if (title != null) {
            xml.append("   <dc:title>\n");
            xml.append("    <rdf:Alt>\n");
            xml.append("     <rdf:li xml:lang=\"x-default\">").append(title).append("</rdf:li>\n");
            xml.append("    </rdf:Alt>\n");
            xml.append("   </dc:title>\n");
        }
        String username = Env.instance().getCurrentUserName();
        if (username != null) {
            xml.append("   <dc:creator>\n");
            xml.append("    <rdf:Seq>\n");
            xml.append("     <rdf:li>").append(username).append("</rdf:li>\n");
            xml.append("    </rdf:Seq>\n");
            xml.append("   </dc:creator>\n");
        }
        xml.append("  </rdf:Description>\n");
        Instant now = Instant.now();
        xml.append("  <rdf:Description xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" rdf:about=\"\" xmp:CreateDate=\"").
                append(now.toString()).append("\" xmp:CreatorTool=\"Writer\" xmp:ModifyDate=\"").append(now).
                append("\"/>\n");
        xml.append("  <rdf:Description xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" pdf:Producer=\"").append(producer).append("\" rdf:about=\"\"/>\n");
        xml.append("  <rdf:Description xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" rdf:about=\"\" xmpMM:DocumentID=\"").append(uuid.toString()).
                append("\" xmpMM:VersionID=\"0\"/>\n");
        xml.append("  <rdf:Description xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\" pdfaid:conformance=\"U\" pdfaid:part=\"3\" rdf:about=\"\"/>\n");
        xml.append(" </rdf:RDF>\n");
        xml.append("</x:xmpmeta>\n");
        xml.append("<?xpacket end=\"r\"?>\n");
        writeXmlData(8, properties, xml.toString());
    }

    private void writeXmlData(int id, Map<String, String> properties, String xmlString) throws IOException {
        writeObj(id, "");
        byte[] xmlBytes = xmlString.getBytes(StandardCharsets.UTF_8);
        writeAttr("/Length", xmlBytes.length);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            writeAttr(entry.getKey(), entry.getValue());
        }
        write(">>\nstream\n");
        write(xmlBytes);
        writeln("\nendstream");
        writeln("endobject");
    }
}

