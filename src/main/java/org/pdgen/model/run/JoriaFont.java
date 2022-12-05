// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.Trace;
import org.pdgen.util.StringUtils;

import java.io.Serializable;

public class JoriaFont implements Serializable {

    private static final long serialVersionUID = 7L;
    String name;
    boolean italic;
    boolean bold;
    int[] widths;
    float lineSpacing;

    public JoriaFont(String name, boolean bold, boolean italic, float lineSpacing, int[] widths) {
        Trace.check(name);
        this.name = name;
        this.bold = bold;
        this.italic = italic;
        this.lineSpacing = lineSpacing;
        this.widths = widths;
    }

    public float getWidth(char ix, float size) {
        return (widths[ix] * size) / 1000;
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public float getLineSpacing(float fontSize) {
        return (lineSpacing * fontSize) / 1000;
    }

    public float calcWidth(String s, float size) {
        if (StringUtils.isEmpty(s))
            return 0;
        float ret = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            ret = ret + getWidth(c, size);
        }
        return ret;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof JoriaFont))
            return false;
        JoriaFont jf = (JoriaFont) obj;
        return name.equals(jf.name) && bold == jf.bold && italic == jf.italic;
    }

    public static String calcHashName(String name, Boolean boldO, Boolean italicO) {
        String hashName;
        boolean bold = false;
        boolean italic = false;
        if (boldO != null)
            bold = boldO;
        if (italicO != null)
            italic = italicO;

        if (bold) {
            if (italic)
                hashName = name + "BI";
            else
                hashName = name + 'B';
        } else if (italic)
            hashName = name + 'I';
        else
            hashName = name + 'R';
        return hashName;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isBold() {
        return bold;
    }
}
