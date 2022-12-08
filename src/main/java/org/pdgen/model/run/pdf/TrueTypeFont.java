// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;

import org.pdgen.data.Log;
import org.pdgen.env.Env;
import org.pdgen.env.Settings;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;

/**
 * Class to cache the informations and the streams of a true/open type font
 */
public class TrueTypeFont {
    private static Map<String, List<TrueTypeFont>> cache;
    private static final int ARG_1_AND_2_ARE_WORDS = 1;
    private static final int WE_HAVE_A_SCALE = 8;
    private static final int MORE_COMPONENTS = 32;
    private static final int WE_HAVE_AN_X_AND_Y_SCALE = 64;
    private static final int WE_HAVE_A_TWO_BY_TWO = 128;
    private static final String[] tableNames = {"cmap", "cvt ", "fpgm", "glyf", "head",
            "hhea", "hmtx", "loca", "maxp", "prep"};
    private static final int[] entrySelectors = {0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4};

    private final Map<String, TableInfo> tables;
    private final File fontFile;
    private String name;
    private String familyName;
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private NameRecord[][] allNames;
    private final List<TagRecord> tags = new ArrayList<>();
    private Header header;
    private HorizontalHeader horizontalHeader;
    private WindowsMetrics windowsMetrics;
    private Post post;
    private int[] glyphWidth;
    private Map<Integer, int[]> cmap10;
    private Map<Integer, int[]> cmap31;
    private Map<Integer, int[]> cmap40;
    private boolean fontSpecificMap;
    @SuppressWarnings("FieldCanBeLocal")
    private int[][] bbox;
    private final boolean doNotEmbed;
    private final boolean cffFound;
    private int maxGlyphId;
    private static final String defaultFontFamily = new JLabel().getFont().getName();


    private TrueTypeFont(File fontFile, Map<String, TableInfo> tables, MappedByteBuffer buffer) throws UnsupportedEncodingException {
        this.fontFile = fontFile;
        this.tables = tables;
        name = fontFile.getAbsolutePath();
        familyName = fontFile.getAbsolutePath();
        readNameTable(buffer);
        readHeaderMap(buffer);
        readHorizontalHeader(buffer);
        readWindowsMetric(buffer);
        readPost(buffer);
        readGlyphWidth(buffer);
        readCMaps(buffer);
        //readKerning(buffer);
        readBBox(buffer);
        doNotEmbed = windowsMetrics.fsType == 2;
        //directTextToByte = cmap31 == null && !fontSpecificMap || cmap10 == null && fontSpecificMap;
        cffFound = tables.get("CFF ") != null;
    }

    public boolean isDoNotEmbed() {
        return doNotEmbed;
    }

    private int getUShort(MappedByteBuffer buffer) {
        short value = buffer.getShort();
        if (value >= 0)
            return value;
        else
            return value - 0xffff0000;
    }

    private int getUByte(MappedByteBuffer buffer) {
        byte value = buffer.get();
        if (value >= 0)
            return value;
        else
            return value - 0xffffff00;
    }

    private int getGlyphWith(int glyph) {
        if (glyph >= glyphWidth.length)
            return glyphWidth[glyphWidth.length - 1];
        else
            return glyphWidth[glyph];
    }

    private double getFixed(MappedByteBuffer buffer) {
        short mantissa = buffer.getShort();
        int fraction = getUShort(buffer);
        return mantissa + fraction / 16384.0d;
    }

    public String getFamilyName() {
        return familyName;
    }

    private void readNameTable(MappedByteBuffer buffer) throws UnsupportedEncodingException {
        TableInfo ti = tables.get("name");
        if (ti == null)
            return;
        buffer.position(ti.offset);
        short format = buffer.getShort();
        short count = buffer.getShort();
        short offset = buffer.getShort();
        List<NameRecord> names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            names.add(new NameRecord(buffer));
        }
        if (format >= 1) {
            short countTags = buffer.getShort();
            for (int i = 0; i < countTags; i++) {
                tags.add(new TagRecord(buffer));
            }
        }
        List<List<NameRecord>> namesList = new ArrayList<>();
        for (NameRecord name : names) {
            name.readData(buffer, ti.offset + offset);
            int nameId = name.getName();
            for (int i = namesList.size(); i <= nameId; i++)
                namesList.add(null);
            if (namesList.get(nameId) == null) {
                namesList.set(nameId, new ArrayList<>());
            }
            List<NameRecord> list = namesList.get(nameId);
            list.add(name);
        }
        NameRecord[][] allNames = new NameRecord[namesList.size()][];
        for (int i = 0; i < namesList.size(); i++) {
            List<NameRecord> n = namesList.get(i);
            if (n != null) {
                NameRecord[] na = new NameRecord[n.size()];
                allNames[i] = n.toArray(na);
            }
        }
        this.allNames = allNames;

        if (allNames.length > 6) {
            NameRecord[] psNames = allNames[6];
            for (NameRecord psName : psNames) {
                if (psName.getPlatform() == 1 || psName.getPlatform() == 3) {
                    name = psName.getValue();
                    break;
                }
            }
        }
        if (allNames.length > 1) {
            NameRecord[] psNames = allNames[1];
            for (NameRecord psName : psNames) {
                if (psName.getPlatform() == 1 || psName.getPlatform() == 3) {
                    familyName = psName.getValue();
                    break;
                }
            }

        }
        for (TagRecord tag : tags) {
            tag.readData(buffer, ti.offset + offset);
        }

    }

    private void readHeaderMap(MappedByteBuffer buffer) {
        header = new Header();
        TableInfo ti = tables.get("head");
        if (ti == null)
            throw new NullPointerException("no head table in font");
        buffer.position(ti.offset);
        buffer.position(buffer.position() + 16); // skip 4 * 32 bit
        header.flags = getUShort(buffer);
        header.unitsPerEm = buffer.getShort();
        buffer.position(buffer.position() + 16); // skip 4 * 32 bit
        header.xMin = buffer.getShort();
        header.yMin = buffer.getShort();
        header.xMax = buffer.getShort();
        header.yMax = buffer.getShort();
        header.macStyle = getUShort(buffer);
        buffer.position(buffer.position() + 4);
        header.longLocaFormat = buffer.getShort() != 0;
    }

    private void readHorizontalHeader(MappedByteBuffer buffer) {
        horizontalHeader = new HorizontalHeader();
        TableInfo ti = tables.get("hhea");
        if (ti == null)
            throw new NullPointerException("no hhea table in font");
        buffer.position(ti.offset);
        buffer.position(buffer.position() + 4); // skip 32 bit
        horizontalHeader.ascend = buffer.getShort();
        horizontalHeader.descend = buffer.getShort();
        horizontalHeader.lineGap = buffer.getShort();
        horizontalHeader.advanceWidthMax = buffer.getShort();
        horizontalHeader.minLeftSideBearing = buffer.getShort();
        horizontalHeader.minRightSideBearing = buffer.getShort();
        horizontalHeader.xMaxExent = buffer.getShort();
        horizontalHeader.caretSlopeRise = buffer.getShort();
        horizontalHeader.caretSlopeRun = buffer.getShort();
        buffer.position(buffer.position() + 12); // skip 3 * 32 bit
        horizontalHeader.numberOfMetrics = getUShort(buffer);
    }

    private void readWindowsMetric(MappedByteBuffer buffer) {
        windowsMetrics = new WindowsMetrics();
        TableInfo ti = tables.get("OS/2");
        if (ti == null)
            throw new NullPointerException("no OS/2 table in font");
        buffer.position(ti.offset);
        short version = buffer.getShort();
        windowsMetrics.xAvgCharWidth = buffer.getShort();
        windowsMetrics.weightClass = buffer.getShort();
        windowsMetrics.widthClass = buffer.getShort();
        windowsMetrics.fsType = buffer.getShort();
        windowsMetrics.ySubscriptXSize = buffer.getShort();
        windowsMetrics.ySubscriptYSize = buffer.getShort();
        windowsMetrics.ySubscriptXOffset = buffer.getShort();
        windowsMetrics.ySubscriptYOffset = buffer.getShort();
        windowsMetrics.ySuperscriptXSize = buffer.getShort();
        windowsMetrics.ySuperscriptYSize = buffer.getShort();
        windowsMetrics.ySuperscriptXOffset = buffer.getShort();
        windowsMetrics.ySuperscriptYOffset = buffer.getShort();
        windowsMetrics.yStrikeoutSize = buffer.getShort();
        windowsMetrics.yStrikeoutPosition = buffer.getShort();
        buffer.position(buffer.position() + 2); // skip family class
        windowsMetrics.panose = new byte[10];
        buffer.get(windowsMetrics.panose);
        buffer.position(buffer.position() + 16); // skip 4 * 32 bits
        windowsMetrics.vendID = new byte[4];
        buffer.get(windowsMetrics.vendID);
        windowsMetrics.fsSelection = getUShort(buffer);
        windowsMetrics.firstCharIndex = getUShort(buffer);
        windowsMetrics.lastCharIndex = getUShort(buffer);
        windowsMetrics.typoAscender = buffer.getShort();
        windowsMetrics.typoDescender = buffer.getShort();
        if (windowsMetrics.typoDescender > 0)
            windowsMetrics.typoDescender = (short) -windowsMetrics.typoDescender;
        windowsMetrics.typeLineGap = buffer.getShort();
        windowsMetrics.winAscend = getUShort(buffer);
        windowsMetrics.winDescend = getUShort(buffer);
        windowsMetrics.codePageRange1 = 0;
        windowsMetrics.codePageRange2 = 0;
        if (version > 0) {
            windowsMetrics.codePageRange1 = buffer.getInt();
            windowsMetrics.codePageRange2 = buffer.getInt();
        }
        if (version > 1) {
            buffer.position(buffer.position() + 2); // skip 16 bits
            windowsMetrics.capHeight = buffer.getShort();
        } else {
            windowsMetrics.capHeight = (int) (0.7 * header.unitsPerEm);
        }
    }

    private void readPost(MappedByteBuffer buffer) {
        post = new Post();
        TableInfo ti = tables.get("post");
        if (ti == null) {
            post.italicAngle = -Math.atan2(horizontalHeader.caretSlopeRun, horizontalHeader.caretSlopeRise) * 180 / Math.PI;
        } else {
            buffer.position(ti.offset);
            buffer.position(buffer.position() + 4); // skip 32 bits
            post.italicAngle = getFixed(buffer);
            post.underlinePosition = buffer.getShort();
            post.underlineThickness = buffer.getShort();
            post.isFixedPitch = buffer.getInt() != 0;
        }
    }

    private void readGlyphWidth(MappedByteBuffer buffer) {
        glyphWidth = new int[horizontalHeader.numberOfMetrics];
        TableInfo ti = tables.get("hmtx");
        if (ti == null)
            throw new NullPointerException("no hmtx table in font");
        buffer.position(ti.offset);
        for (int i = 0; i < glyphWidth.length; i++) {
            glyphWidth[i] = getUShort(buffer) * 1000 / header.unitsPerEm;
            buffer.position(buffer.position() + 2); // skip 16 bits
        }
    }

    private void readCMaps(MappedByteBuffer buffer) {
        TableInfo ti = tables.get("cmap");
        if (ti == null)
            throw new NullPointerException("no cmap table in font");
        buffer.position(ti.offset);
        buffer.position(buffer.position() + 2); // skip 16 bit
        short numberOfTables = buffer.getShort();
        int map10Offset = 0;
        int map31Offset = 0;
        int map30Offset = 0;
        int mapExtOffset = 0;

        for (int i = 0; i < numberOfTables; i++) {
            short platform = buffer.getShort();
            short enconding = buffer.getShort();
            int offset = buffer.getInt();
            if (platform == 3 && enconding == 0) {
                fontSpecificMap = true;
                map30Offset = offset;
            } else if (platform == 3 && enconding == 1) {
                map31Offset = offset;
            } else if (platform == 3 && enconding == 10) {
                mapExtOffset = offset;
            } else if (platform == 1 && enconding == 0) {
                map10Offset = offset;
            }
        }

        if (map10Offset != 0) {
            buffer.position(ti.offset + map10Offset);
            short format = buffer.getShort();
            switch (format) {
                case 0:
                    cmap10 = readCMapsFormat0(buffer);
                    break;
                case 4:
                    cmap10 = readCMapsFormat4(buffer);
                    break;
                case 6:
                    cmap10 = readCMapsFormat6(buffer);
                    break;
            }
        }
        if (map31Offset != 0) {
            buffer.position(ti.offset + map31Offset);
            short format = buffer.getShort();
            if (format == 4)
                cmap31 = readCMapsFormat4(buffer);
        }

        if (map30Offset != 0) {
            buffer.position(ti.offset + map30Offset);
            short format = buffer.getShort();
            if (format == 4)
                cmap10 = readCMapsFormat4(buffer);
        }

        if (mapExtOffset != 0) {
            buffer.position(ti.offset + mapExtOffset);
            short format = buffer.getShort();
            switch (format) {
                case 0:
                    cmap40 = readCMapsFormat0(buffer);
                    break;
                case 4:
                    cmap40 = readCMapsFormat4(buffer);
                    break;
                case 6:
                    cmap40 = readCMapsFormat6(buffer);
                    break;
                case 12:
                    cmap40 = readCMapsFormat12(buffer);
                    break;
            }

        }
    }

    private Map<Integer, int[]> readCMapsFormat12(MappedByteBuffer buffer) {
        Map<Integer, int[]> m = new HashMap<>();
        buffer.position(buffer.position() + 10); // skip 16 + 2 * 32bits
        int numberOfGroups = buffer.getInt();
        for (int i = 0; i < numberOfGroups; i++) {
            int startCharCode = buffer.getInt();
            int endCharCode = buffer.getInt();
            int glyphID = buffer.getInt();
            for (int j = startCharCode; j <= endCharCode; j++) {
                int[] v = new int[2];
                v[0] = glyphID;
                v[1] = getGlyphWith(glyphID);
                m.put(j, v);
                glyphID++;
                if (v[0] > maxGlyphId)
                    maxGlyphId = v[0];
            }
        }
        return m;
    }

    private Map<Integer, int[]> readCMapsFormat6(MappedByteBuffer buffer) {
        Map<Integer, int[]> m = new HashMap<>();
        buffer.position(buffer.position() + 4); // skip 32bits
        int startCode = getUShort(buffer);
        int numberOfCodes = getUShort(buffer);
        for (int i = 0; i < numberOfCodes; i++) {
            int[] v = new int[2];
            v[0] = getUShort(buffer);
            v[1] = getGlyphWith(v[0]);
            m.put(startCode + i, v);
            if (v[0] > maxGlyphId)
                maxGlyphId = v[0];
        }
        return m;
    }

    private Map<Integer, int[]> readCMapsFormat4(MappedByteBuffer buffer) {
        Map<Integer, int[]> m = new HashMap<>();
        int lengthOfTable = getUShort(buffer);
        buffer.position(buffer.position() + 2); // skip 16 bits
        int segCount = buffer.getShort() / 2;
        buffer.position(buffer.position() + 6); // skip 3 * 16 bits
        int[] endCount = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            endCount[i] = getUShort(buffer);
        }
        buffer.position(buffer.position() + 2); // skip 16 bits
        int[] startCount = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            startCount[i] = getUShort(buffer);
        }
        int[] idDelta = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            idDelta[i] = getUShort(buffer);
        }
        int[] idRangeOffset = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            idRangeOffset[i] = getUShort(buffer);
        }
        int[] glyphId = new int[lengthOfTable / 2 - 8 - segCount * 4];
        for (int i = 0; i < glyphId.length; i++) {
            glyphId[i] = getUShort(buffer);
        }

        for (int i = 0; i < segCount; i++) {
            for (int j = startCount[i]; j <= endCount[i] && j != 0xffff; j++) {
                int glyph;
                if (idRangeOffset[i] == 0) {
                    glyph = j + idDelta[i] & 0xffff;
                } else {
                    int idx = i + idRangeOffset[i] / 2 - segCount + j - startCount[i];
                    glyph = glyphId[idx] + idDelta[i] & 0xffff;
                }
                int[] v = new int[2];
                v[0] = glyph;
                v[1] = getGlyphWith(glyph);
                int id = fontSpecificMap ? ((j & 0xff00) == 0xf000 ? j & 0xff : j) : j;
                m.put(id, v);
                if (v[0] > maxGlyphId)
                    maxGlyphId = v[0];
            }
        }
        return m;
    }

    private Map<Integer, int[]> readCMapsFormat0(MappedByteBuffer buffer) {
        Map<Integer, int[]> m = new HashMap<>();
        buffer.position(buffer.position() + 4); // skip 32 bits
        for (int i = 0; i < 256; i++) {
            int[] v = new int[2];
            v[0] = getUByte(buffer);
            v[1] = getGlyphWith(v[0]);
            m.put(i, v);
            if (v[0] > maxGlyphId)
                maxGlyphId = v[0];
        }
        return m;
    }

/*
    private void readKerning(MappedByteBuffer buffer)
    {
        kerning = new HashMap<Integer, Integer>();
        TableInfo ti = tables.get("kern");
        if (ti == null)
            return;
        buffer.position(ti.offset);
        buffer.position(buffer.position() + 2); // skip 16 bits
        int numberOfTables = getUShort(buffer);
        int position = buffer.position();
        int length = 0;
        for (int i = 0; i < numberOfTables; i++)
        {
            position += length;
            buffer.position(position);
            short format = buffer.getShort();
            length = getUShort(buffer);
            int coverage = getUShort(buffer);
            if (format == 0 && (coverage & 0xfff7) == 0x0001)
            {
                int nPairs = getUShort(buffer);
                buffer.position(buffer.position() + 6); // skip 3 * 16 bits
                for (int j = 0; j < nPairs; j++)
                {
                    int pair = buffer.getInt();
                    int value = buffer.getShort() * 1000 / header.unitsPerEm;
                    kerning.put(pair, value);
                }
            }

        }

    }
*/

    private void readBBox(MappedByteBuffer buffer) {
        int[] locaTable = readLoca(buffer);
        TableInfo ti = tables.get("glyf");
        if (ti == null)
            return;
        int position = ti.offset;
        assert locaTable != null;
        bbox = new int[locaTable.length - 1][];

        for (int i = 0; i < locaTable.length - 1; i++) {
            if (locaTable[i] != locaTable[i + 1]) {
                buffer.position(position + locaTable[i] + 2); // goto start of glyph and skip 16 bits
                bbox[i] = new int[]{
                        buffer.getShort() * 1000 / header.unitsPerEm,
                        buffer.getShort() * 1000 / header.unitsPerEm,
                        buffer.getShort() * 1000 / header.unitsPerEm,
                        buffer.getShort() * 1000 / header.unitsPerEm,
                };
            }
        }
    }

    private int[] readLoca(MappedByteBuffer buffer) {
        TableInfo ti = tables.get("loca");
        if (ti == null)
            return null;
        buffer.position(ti.offset);
        int numberLocaEntries;
        int[] locaTable;
        if (header.longLocaFormat) {
            numberLocaEntries = ti.length / 4;
            locaTable = new int[numberLocaEntries];
            for (int i = 0; i < numberLocaEntries; i++)
                locaTable[i] = buffer.getInt();
        } else {
            numberLocaEntries = ti.length / 2;
            locaTable = new int[numberLocaEntries];
            for (int i = 0; i < numberLocaEntries; i++)
                locaTable[i] = getUShort(buffer) * 2;
        }
        return locaTable;
    }

    public boolean isBold() {
        return (header.macStyle & 0x1) == 0x1;
    }

    public boolean isItalic() {
        return (header.macStyle & 0x2) == 0x2;
    }


    public static TrueTypeFont findFont(String name, boolean bold, boolean italic) {
        if ("Default".equals(name)) {
            name = defaultFontFamily;
        }
        readAllFontFileHeaders();
        List<TrueTypeFont> fontList = cache.get(name);
        if (fontList == null)
            return null;
        TrueTypeFont foundFont = null;
        for (TrueTypeFont font : fontList) {
            if (bold == font.isBold() && italic == font.isItalic()) {
                return font;
            }
            if (bold != font.isBold() && italic == font.isItalic()) {
                foundFont = font;
            } else if (bold == font.isBold()) {
                if (foundFont == null || bold == foundFont.isBold() || italic != foundFont.isItalic()) {
                    foundFont = font;
                }
            } else if (foundFont == null) {
                foundFont = font;
            }
        }
        return foundFont;
    }

    public static void readAllFontFileHeaders() {
        if (cache != null)
            return;
        String fontPath;
        if (System.getProperty("os.name").startsWith("Windows")) {
            fontPath = "C:\\Windows\\fonts";
        } else {
            fontPath = "/usr/share/fonts";
        }

        File fontDir = new File(fontPath);
        List<File> fontFiles = new ArrayList<>();

        collectFontFiles(fontDir, fontFiles);

        Map<String, List<TrueTypeFont>> fonts = new HashMap<>(fontFiles.size() / 4);

        for (File fontFile : fontFiles) {
            FileChannel fc = null;
            try {
                fc = FileChannel.open(fontFile.toPath(), StandardOpenOption.READ);
                MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fontFile.length());
                int magic = buffer.getInt();
                if (magic == 0x74746366) // ttc collection of fonts
                {
                    buffer.position(buffer.position() + 4); // skip int
                    int numOfFonts = buffer.getInt();
                    int[] fontOffsets = new int[numOfFonts];
                    for (int i = 0; i < numOfFonts; i++)
                        fontOffsets[i] = buffer.getInt();

                    for (int i = 0; i < numOfFonts; i++) {
                        buffer.position(fontOffsets[i]);
                        magic = buffer.getInt();
                        if (magic == 0x00010000 || magic == 0x4f54544f) // first TTF second OTF
                        {
                            TrueTypeFont trueTypeFont = readFontHeader(buffer, fontFile);
                            if (!fonts.containsKey(trueTypeFont.familyName)) {
                                fonts.put(trueTypeFont.familyName, new ArrayList<>(4));
                            }
                            fonts.get(trueTypeFont.familyName).add(trueTypeFont);
                        }
                    }

                } else if (magic == 0x00010000 || magic == 0x4f54544f) // first TTF second OTF
                {
                    TrueTypeFont trueTypeFont = readFontHeader(buffer, fontFile);
                    if (!fonts.containsKey(trueTypeFont.familyName)) {
                        fonts.put(trueTypeFont.familyName, new ArrayList<>(4));
                    }
                    fonts.get(trueTypeFont.familyName).add(trueTypeFont);
                }

            } catch (NegativeArraySizeException ex) {
                Log.ini.warn("TrueType Font " + fontFile.getName() + " could not be loaded, due to incompatible encoding.");
            } catch (Throwable e) {
                Log.ini.error(e);
                //Env.instance().handle(e);
            } finally {
                if (fc != null) {
                    try {
                        fc.close();
                    } catch (IOException e) {
                        Env.instance().handle(e);
                    }
                }
            }
        }
        synchronized (TrueTypeFont.class) {
            if (cache == null)
                cache = fonts;
        }
    }

    private static TrueTypeFont readFontHeader(MappedByteBuffer buffer, File fontFile) throws UnsupportedEncodingException {
        short numOfTables = buffer.getShort();
        buffer.position(buffer.position() + 6); // skip 3 shorts
        Map<String, TableInfo> tables = new HashMap<>(numOfTables);
        for (int i = 0; i < numOfTables; i++) {
            int tag = buffer.getInt();
            int checksum = buffer.getInt();
            int offset = buffer.getInt();
            int length = buffer.getInt();
            TableInfo ti = new TableInfo(tag, checksum, offset, length);
            String tabName = ti.getName();
            tables.put(tabName, ti);
        }
        return new TrueTypeFont(fontFile, tables, buffer);
    }

    private static void collectFontFiles(File fontDir, List<File> fontFiles) {
        File[] subDirs = fontDir.listFiles(File::isDirectory);
        assert subDirs != null;
        for (File subDir : subDirs)
            collectFontFiles(subDir, fontFiles);
        File[] files = fontDir.listFiles((dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".ttf") || name.endsWith(".ttc") || name.endsWith(".otf");
        });
        assert files != null;
        Collections.addAll(fontFiles, files);
    }

    public static void main(String[] args) {
        //SortedMap<String, Charset> charsets = Charset.availableCharsets();
        String[] familyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        readAllFontFileHeaders();
        for (String familyName : familyNames) {
            List<TrueTypeFont> fonts = cache.get(familyName);
            if (fonts == null)
                System.out.println("not a TTF " + familyName);
            else {
                System.out.println("TTFs " + fonts.size() + " for " + familyName);
                if (fonts.size() == 3)
                    System.out.println("break me");
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getNumberOfGlyphs() {
        return maxGlyphId + 1;
    }

    private int[] getGlyphData(char ch) {
        Map<Integer, int[]> map;
        if (cmap40 != null)
            map = cmap40;
        else if (fontSpecificMap)
            map = cmap10;
        else
            map = cmap31;
        if (map == null)
            return null;
        if (fontSpecificMap) {
            if ((ch & 0xffffff00) == 0 || (ch & 0xffffff00) == 0xf000)
                return map.get(ch & 0xff);
            else
                return null;
        } else
            return map.get((int) ch);
    }

    public char getGlyph(char ch) {
        int[] gd = getGlyphData(ch);
        if (gd != null) {
            return (char) gd[0];

        } else {
            return 0;
        }
    }

    public void buildFullFile(CompressedByteBuffer compressedOutput) throws IOException {
        FileChannel fc = null;
        try {
            fc = FileChannel.open(fontFile.toPath(), StandardOpenOption.READ);
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fontFile.length());
            byte[] bytes = new byte[(int) fontFile.length()];
            buffer.get(bytes);
            compressedOutput.add(bytes);
        } finally {
            if (fc != null)
                fc.close();
        }
    }

    public void buildSubSetFile(CompressedByteBuffer compressedOutput, boolean[] usedGlyphs) throws IOException {
        FileChannel fc = null;
        try {
            fc = FileChannel.open(fontFile.toPath(), StandardOpenOption.READ);
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fontFile.length());
            int[] locaTable = readLoca(buffer);
            Set<Integer> used = new HashSet<>();
            for (int i = 0; i < usedGlyphs.length; i++) {
                if (usedGlyphs[i])
                    used.add(i);
            }
            addDependendGlyphs(buffer, used, locaTable);
            assert locaTable != null;
            int[] newLocaTableInt = new int[locaTable.length];
            ByteBuffer newGlyphTable = createNewGlyphTable(buffer, used, locaTable, newLocaTableInt);
            ByteBuffer newLocaTable = createNewLocaTable(newLocaTableInt);
            createSubsetFont(buffer, compressedOutput, newLocaTable, newGlyphTable);

            if ("yes".equalsIgnoreCase(Settings.get("dumpSubSetTrueTypeFont"))) {
                FileOutputStream fos = new FileOutputStream(new File("t.ttf"));
                fos.write(compressedOutput.compressData(), 0, compressedOutput.compress());
                fos.close();
            }
        } finally {
            if (fc != null)
                fc.close();
        }
    }

    private void createSubsetFont(MappedByteBuffer buffer, CompressedByteBuffer compressedOutput, ByteBuffer newLocaTable, ByteBuffer newGlyphTable) {
        int tablesUsed = 2;
        int len = 0;
        for (String name : tableNames) {
            if (name.equals("glyf") || name.equals("loca"))
                continue;
            TableInfo ti = tables.get(name);
            if (ti == null)
                continue;
            ++tablesUsed;
            len += ti.length + 3 & ~3;
        }
        len += newLocaTable.limit();
        len += newGlyphTable.limit();
        int ref = 16 * tablesUsed + 12;
        len += ref;
        ByteBuffer outBuffer = ByteBuffer.allocate(len);
        outBuffer.putInt(0x00010000);
        outBuffer.putShort((short) tablesUsed);
        int selector = entrySelectors[tablesUsed];
        outBuffer.putShort((short) ((1 << selector) * 16));
        outBuffer.putShort((short) selector);
        outBuffer.putShort((short) ((tablesUsed - (1 << selector)) * 16));
        int glyfTableRealSize = newGlyphTable.position();
        int locaTableRealSize = newLocaTable.position();
        for (String name : tableNames) {
            TableInfo ti = tables.get(name);
            if (ti == null)
                continue;
            outBuffer.putInt(ti.tag);
            switch (name) {
                case "glyf":
                    int checksum = calculateChecksum(newGlyphTable);
                    outBuffer.putInt(checksum);
                    len = glyfTableRealSize;
                    break;
                case "loca":
                    int checkSum = calculateChecksum(newLocaTable);
                    outBuffer.putInt(checkSum);
                    len = locaTableRealSize;
                    break;
                default:
                    outBuffer.putInt(ti.checksum);
                    len = ti.length;
                    break;
            }
            outBuffer.putInt(ref);
            outBuffer.putInt(len);
            ref += len + 3 & ~3;
        }
        for (String name : tableNames) {
            TableInfo ti = tables.get(name);
            if (ti == null)
                continue;
            switch (name) {
                case "glyf":
                    assert newGlyphTable != null;
                    outBuffer.put(newGlyphTable.array());
                    newGlyphTable = null;
                    break;
                case "loca":
                    assert newLocaTable != null;
                    outBuffer.put(newLocaTable.array());
                    newLocaTable = null;
                    break;
                default:
                    buffer.position(ti.offset);
                    byte[] t = new byte[ti.length + 3 & ~3];
                    buffer.get(t);
                    buffer.position((ti.offset));
                    outBuffer.put(t);
                    break;
            }
        }
        compressedOutput.add(outBuffer.array());

    }

    private ByteBuffer createNewLocaTable(int[] newLocaTableInt) {
        int locaTableSize;
        if (header.longLocaFormat)
            locaTableSize = newLocaTableInt.length * 4;
        else
            locaTableSize = newLocaTableInt.length * 2;
        ByteBuffer newLocaTable = ByteBuffer.allocate(locaTableSize + 3 & ~3);
        for (int aNewLocaTableInt : newLocaTableInt) {
            if (header.longLocaFormat)
                newLocaTable.putInt(aNewLocaTableInt);
            else
                newLocaTable.putShort((short) (aNewLocaTableInt / 2 & 0xffff));
        }
        return newLocaTable;
    }

    private int calculateChecksum(ByteBuffer buffer) {
        return calculateChecksum(buffer, buffer.limit());
    }

    private int calculateChecksum(ByteBuffer buffer, int len) {
        int intInBuffer = len / 4;
        buffer.position(0);
        int checkSum = 0;
        for (int i = 0; i < intInBuffer; i++) {
            checkSum += buffer.getInt();
        }
        return checkSum;
    }

    private ByteBuffer createNewGlyphTable(MappedByteBuffer buffer, Set<Integer> used, int[] locaTable, int[] newLocaTable) {
        int[] activeGlyphs = new int[used.size()];
        List<Integer> usedList = new ArrayList<>(used);
        for (int k = 0; k < activeGlyphs.length; ++k)
            activeGlyphs[k] = usedList.get(k);
        Arrays.sort(activeGlyphs);
        int glyphSize = 0;
        for (int glyph : activeGlyphs) {
            glyphSize += locaTable[glyph + 1] - locaTable[glyph];
        }
        int glyphSizeAlign = glyphSize + 3 & ~3;
        byte[] newGlyphTable = new byte[glyphSize];
        int glyfPtr = 0;
        int glyphTableOffset = tables.get("glyf").offset;
        for (int i = 0; i < newLocaTable.length; i++) {
            newLocaTable[i] = glyfPtr;
            if (used.contains(i)) {
                int start = locaTable[i];
                int len = locaTable[i + 1] - start;
                if (len > 0) {
                    buffer.position(glyphTableOffset + start);
                    buffer.get(newGlyphTable, glyfPtr, len);
                    glyfPtr += len;
                }
            }
        }
        ByteBuffer gt = ByteBuffer.allocate(glyphSizeAlign);
        gt.put(newGlyphTable);
        gt.position(glyphSize);
        return gt;
    }

    private void addDependendGlyphs(MappedByteBuffer buffer, Set<Integer> used, int[] locaTable) {
        TableInfo glyph = tables.get("glyf");
        Set<Integer> glyphsToCheck = new HashSet<>(used);
        used.add(0);
        while (glyphsToCheck.size() > 0) {
            int glyphId = glyphsToCheck.iterator().next();
            glyphsToCheck.remove(glyphId);
            Set<Integer> dependendGlphys = getDependendGlyphs(buffer, glyph, locaTable, glyphId);
            if (dependendGlphys != null) {
                for (int depId : dependendGlphys) {
                    if (!used.contains(depId)) {
                        used.add(depId);
                        glyphsToCheck.add(depId);
                    }
                }
            }
        }
    }

    private Set<Integer> getDependendGlyphs(MappedByteBuffer buffer, TableInfo glyph, int[] locaTable, int glyphId) {
        int start = locaTable[glyphId];
        if (start == locaTable[glyphId + 1]) // no contour
            return null;
        buffer.position(glyph.offset + start);
        int numContours = buffer.getShort();
        if (numContours >= 0)
            return null;
        buffer.position(buffer.position() + 8);
        Set<Integer> r = new HashSet<>();
        for (; ; ) {
            int flags = getUShort(buffer);
            int dependend = getUShort(buffer);
            r.add(dependend);
            if ((flags & MORE_COMPONENTS) == 0)
                return r;
            int skip;
            if ((flags & ARG_1_AND_2_ARE_WORDS) != 0)
                skip = 4;
            else
                skip = 2;
            if ((flags & WE_HAVE_A_SCALE) != 0)
                skip += 2;
            else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0)
                skip += 4;
            if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0)
                skip += 8;
            buffer.position(buffer.position() + skip);
        }

    }

    public void buildToUnicode(CompressedByteBuffer compressedOutput, boolean[] usedGlyphs, Map<Character, Character> glyphToUnicode) throws UnsupportedEncodingException {
        List<Integer> used = new ArrayList<>();
        for (int i = 0; i < usedGlyphs.length; i++) {
            if (usedGlyphs[i])
                used.add(i);
        }
        if (used.size() == 0)
            return;

        StringBuilder buf = new StringBuilder(
                "/CIDInit /ProcSet findresource begin\n" +
                        "12 dict begin\n" +
                        "begincmap\n" +
                        "/CIDSystemInfo\n" +
                        "<< /Registry (TTX+0)\n" +
                        "/Ordering (T42UV)\n" +
                        "/Supplement 0\n" +
                        ">> def\n" +
                        "/CMapName /TTX+0 def\n" +
                        "/CMapType 2 def\n" +
                        "1 begincodespacerange\n" +
                        "<0000><FFFF>\n" +
                        "endcodespacerange\n");
        int size = 0;
        for (int k = 0; k < used.size(); ++k) {
            if (size == 0) {
                if (k != 0) {
                    buf.append("endbfrange\n");
                }
                size = Math.min(100, used.size() - k);
                buf.append(size).append(" beginbfrange\n");
            }
            --size;
            int glyphId = used.get(k);
            String fromTo = toHex(glyphId);
            String toTO = toHex(glyphToUnicode.get((char) glyphId));
            buf.append('<').append(fromTo).append('>').append('<').append(fromTo).append('>').append('<').append(toTO).append('>').append('\n');
        }
        buf.append(
                "endbfrange\n" +
                        "endcmap\n" +
                        "CMapName currentdict /CMap defineresource pop\n" +
                        "end end\n");
        String s = buf.toString();
        compressedOutput.add(s.getBytes("Cp1252"));
    }

    private String toHex(int glyphId) {
        String s = "0000" + Integer.toHexString(glyphId);
        return s.substring(s.length() - 4);
    }

    public String buildWidthInfo(boolean[] usedGlyphs, Map<Character, Character> glyphIdToUnicode) {
        StringBuilder buf = new StringBuilder("[");
        int lastNumber = -10;
        boolean firstTime = true;
        for (int i = 0; i < usedGlyphs.length; i++) {
            if (!usedGlyphs[i])
                continue;
            char ch = (char) i;
            char unicode = glyphIdToUnicode.get(ch);
            int[] glyphData = getGlyphData(unicode);
            if (glyphData == null)
                glyphData = new int[]{0, getGlyphWith(0)};
            if (glyphData[1] == 1000)
                continue;
            int m = glyphData[0];
            if (m == lastNumber + 1) {
                buf.append(' ').append(glyphData[1]);
            } else {
                if (!firstTime) {
                    buf.append(']');
                }
                firstTime = false;
                buf.append(m).append('[').append(glyphData[1]);
            }
            lastNumber = m;
        }
        if (buf.length() > 1) {
            buf.append("]]");
            return buf.toString();
        } else
            return "[]";
    }

    public float getAscent() {
        return windowsMetrics.typoAscender * 1000 / header.unitsPerEm;
    }

    public float getDescent() {
        return windowsMetrics.typoDescender * 1000 / header.unitsPerEm;
    }

    public float getCapHeight() {
        return windowsMetrics.capHeight * 1000 / header.unitsPerEm;
    }

    public String getBBox() {
        return "[" + header.xMin * 1000 / header.unitsPerEm + " " +
                header.yMin * 1000 / header.unitsPerEm + " " +
                header.xMax * 1000 / header.unitsPerEm + " " +
                header.yMax * 1000 / header.unitsPerEm + "]";
    }

    public int getFlags() {
        int flags = 0;
        if (post.isFixedPitch)
            flags |= 1;
        flags |= fontSpecificMap ? 4 : 32;
        if ((header.macStyle & 2) != 0)
            flags |= 64;
        if ((header.macStyle & 1) != 0)
            flags |= 262144;
        return flags;
    }

    public double getItalicAngle() {
        return post.italicAngle;
    }

    public boolean isCFFFont() {
        return cffFound;
    }

    public void buildCFFFile(CompressedByteBuffer compressedOutput) throws IOException {
        FileChannel fc = null;
        TableInfo ti = tables.get("CFF ");
        try {
            fc = FileChannel.open(fontFile.toPath(), StandardOpenOption.READ);
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fontFile.length());
            byte[] data = new byte[ti.length];
            buffer.position(ti.offset);
            buffer.get(data);
            compressedOutput.add(data);
        } finally {
            if (fc != null)
                fc.close();
        }
    }

    private static class TableInfo {
        int tag;
        int checksum;
        int offset;
        int length;
        private String name;

        private TableInfo(int tag, int checksum, int offset, int length) {
            this.tag = tag;
            this.checksum = checksum;
            this.offset = offset;
            this.length = length;
        }

        public String getName() {
            if (name == null) {
                char[] n = new char[4];
                char c1 = (char) ((tag & 0xff000000) >> 24);
                char c2 = (char) ((tag & 0x00ff0000) >> 16);
                char c3 = (char) ((tag & 0x0000ff00) >> 8);
                char c4 = (char) ((tag & 0x000000ff));
                n[0] = c1;
                n[1] = c2;
                n[2] = c3;
                n[3] = c4;
                name = new String(n);
            }
            return name;
        }
    }

    private static class NameRecord {
        private final short platform;
        private final short encoding;
        private final short language;
        private final short name;
        private final short length;
        private final short offset;
        private String value;

        private NameRecord(MappedByteBuffer buffer) {
            platform = buffer.getShort();
            encoding = buffer.getShort();
            language = buffer.getShort();
            name = buffer.getShort();
            length = buffer.getShort();
            offset = buffer.getShort();
        }

        public short getPlatform() {
            return platform;
        }

        public short getEncoding() {
            return encoding;
        }

        public short getLanguage() {
            return language;
        }

        public short getName() {
            return name;
        }

        void readData(MappedByteBuffer buffer, int offset) throws UnsupportedEncodingException {
            buffer.position(offset + this.offset);
            byte[] data = new byte[length];
            buffer.get(data);
            if (platform == 0 || platform == 3 || platform == 2 && encoding == 1)
                value = new String(data, StandardCharsets.UTF_16BE);
            else
                value = new String(data, "Cp1252");
        }

        public String getValue() {
            return value;
        }

    }

    private static class TagRecord {
        private final short length;
        private final short offset;
        private String value;

        private TagRecord(MappedByteBuffer buffer) {
            length = buffer.getShort();
            offset = buffer.getShort();
        }

        void readData(MappedByteBuffer buffer, int offset) throws UnsupportedEncodingException {
            buffer.position(offset + this.offset);
            byte[] data = new byte[length];
            buffer.get(data);
            value = new String(data, StandardCharsets.UTF_16BE);
        }

        public String getValue() {
            return value;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class Header {
        private int flags;
        private int unitsPerEm;
        private short xMin;
        private short yMin;
        private short xMax;
        private short yMax;
        private int macStyle;
        private boolean longLocaFormat;
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class HorizontalHeader {
        private short ascend;
        private short descend;
        private short lineGap;
        private short advanceWidthMax;
        private short minLeftSideBearing;
        private short minRightSideBearing;
        private short xMaxExent;
        private short caretSlopeRise;
        private short caretSlopeRun;
        private int numberOfMetrics;
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class WindowsMetrics {
        private short xAvgCharWidth;
        private short weightClass;
        private short widthClass;
        private short fsType;
        private short ySubscriptXSize;
        private short ySubscriptYSize;
        private short ySubscriptXOffset;
        private short ySubscriptYOffset;
        private short ySuperscriptXSize;
        private short ySuperscriptYSize;
        private short ySuperscriptXOffset;
        private short ySuperscriptYOffset;
        private short yStrikeoutSize;
        private short yStrikeoutPosition;
        private short familyClass;
        private byte[] panose; // 10 bytes
        private byte[] vendID; // 4 bytes
        private int fsSelection;
        private int firstCharIndex;
        private int lastCharIndex;
        private short typoAscender;
        private short typoDescender;
        private short typeLineGap;
        private int winAscend;
        private int winDescend;
        private int codePageRange1;
        private int codePageRange2;
        private int capHeight;
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class Post {
        double italicAngle;
        short underlinePosition;
        short underlineThickness;
        boolean isFixedPitch;
    }
}
