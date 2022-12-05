// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class StyleRunIterator {
    StyledParagraphLayouter src;
    int lineIx;
    int run = -1;
    String text;
    String fontFamily;
    float size = -1;
    Color foregroundColor;
    Color backgroundColor;
    boolean newForegroundColor;
    boolean newBackgroundColor;
    boolean bold;
    boolean italic;
    boolean underlined;
    boolean endOfLine;
    boolean superscript;
    boolean newBold;
    boolean newItalic;
    boolean newUnderlined;
    boolean newSize;
    boolean newFontFamily;
    boolean newSuperscript;
    boolean newWidth;
    float width;

    public StyleRunIterator(StyledParagraphLayouter src) {
        this.src = src;
    }

    public boolean nextRun() {
        if (endOfLine) {
            lineIx++;
            if (lineIx < src.getLineCount()) {
                final QSELine line = src.getLine(lineIx);
                final int start = line.startCharIndex - src.startCharPos;
                int end;
                if (run == src.text.runCount() - 1)
                    end = src.text.length();
                else
                    end = src.text.runStart(run + 1);
                end = Math.min(end, line.endCharIndex() - src.startCharPos);
                text = src.text.get(start, end - start);
                endOfLine = end == line.endCharIndex() - src.startCharPos;
                return true;
            }
            return false;
        } else {
            run++;
            boolean ret = run < src.text.runCount();
            if (ret) {
                final int start = src.text.runStart(run);
                int end;
                if (run == src.text.runCount() - 1)
                    end = src.text.length();
                else
                    end = src.text.runStart(run + 1);
                final QSELine line = src.getLine(lineIx);
                end = Math.min(end, line.endCharIndex() - src.startCharPos);
                text = src.text.get(start, end - start);
                endOfLine = end == line.endCharIndex() - src.startCharPos;
                boolean oldBold = bold;
                boolean oldItalic = italic;
                boolean oldUnderlined = underlined;
                boolean oldSuperscript = superscript;
                Color oldForeground = foregroundColor;
                Color oldBackground = backgroundColor;
                foregroundColor = backgroundColor = null;
                bold = italic = underlined = superscript = false;
                final AttributeRun styleRun = src.text.styleRuns.get(run);
                for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : styleRun.entrySet()) {
                    final AttributedCharacterIterator.Attribute key = entry.getKey();
                    if (key == TextAttribute.FAMILY) {
                        String tFamily = (String) entry.getValue();
                        newFontFamily = fontFamily == null || !fontFamily.equals(tFamily);
                        fontFamily = tFamily;
                    } else if (key == TextAttribute.SIZE) {
                        Number tmp = (Number) entry.getValue();
                        newSize = Math.abs(tmp.floatValue() - size) > 0.01;
                        size = tmp.floatValue();
                    } else if (key == TextAttribute.BACKGROUND) {
                        Color color = (Color) entry.getValue();
                        newBackgroundColor = color == null || !color.equals(backgroundColor);
                        backgroundColor = color;
                    } else if (key == TextAttribute.FOREGROUND) {
                        Color color = (Color) entry.getValue();
                        newForegroundColor = color == null || !color.equals(foregroundColor);
                        foregroundColor = color;
                    } else if (key == TextAttribute.WIDTH) {
                        Number w = (Number) entry.getValue();
                        newWidth = Math.abs(w.floatValue() - width) > 0.01;
                        width = w.floatValue();
                    } else if (key == TextAttribute.WEIGHT) {
                        bold = (TextAttribute.WEIGHT_BOLD.equals(entry.getValue()));
                    } else if (key == TextAttribute.POSTURE) {
                        italic = (TextAttribute.POSTURE_OBLIQUE.equals(entry.getValue()));
                    } else if (key == TextAttribute.UNDERLINE) {
                        underlined = (TextAttribute.UNDERLINE_ON.equals(entry.getValue()));
                    } else if (key == TextAttribute.SUPERSCRIPT) {
                        superscript = (TextAttribute.SUPERSCRIPT_SUPER.equals(entry.getValue()));
                    }
                }
                newBold = oldBold != bold;
                newItalic = oldItalic != italic;
                newUnderlined = oldUnderlined != underlined;
                newSuperscript = oldSuperscript != superscript;
                newForegroundColor = !is(foregroundColor, oldForeground);
                newBackgroundColor = !is(backgroundColor, oldBackground);
            }
            return ret;
        }
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public float getSize() {
        return size;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderlined() {
        return underlined;
    }

    public boolean isEndOfLine() {
        return endOfLine;
    }

    public String getText() {
        return text;
    }

    public boolean isNewBold() {
        return newBold;
    }

    public boolean isNewItalic() {
        return newItalic;
    }

    public boolean isNewUnderlined() {
        return newUnderlined;
    }

    public boolean isNewSize() {
        return newSize;
    }

    public boolean isNewFontFamily() {
        return newFontFamily;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    public boolean isNewSuperscript() {
        return newSuperscript;
    }

    public boolean isSuperscript() {
        return superscript;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(final Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public boolean isNewBackgroundColor() {
        return newBackgroundColor;
    }

    public void setNewBackgroundColor(final boolean newBackgroundColor) {
        this.newBackgroundColor = newBackgroundColor;
    }

    public boolean isNewForegroundColor() {
        return newForegroundColor;
    }

    public void setNewForegroundColor(final boolean newForegroundColor) {
        this.newForegroundColor = newForegroundColor;
    }

    public boolean isNewWidth() {
        return newWidth;
    }

    public float getWidth() {
        return width;
    }

    boolean is(Color c1, Color c2) {
        if (c1 == null)
            return c2 == null;
        return c1.equals(c2);
    }
}
