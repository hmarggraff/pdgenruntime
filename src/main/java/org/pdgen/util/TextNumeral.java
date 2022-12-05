// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

/**
 * User: patrick
 * Date: Feb 21, 2006
 * Time: 3:48:32 PM
 */
public class TextNumeral {
    public static final char[] characters = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static final char[] charactersUp = {' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    public static final char[] charactersE = {'X', 'a', 'b', 'c', 'd', 'e'};
    String format;
    int val;
    int work;

    public TextNumeral(int val, String format) {
        this.val = val;
        this.format = format;
    }

    public String toString() {
        if (val < 1)
            throw new IllegalArgumentException("the value cannot be less then 1");
        if ("text".equals(format)) {
            char[] chars = characters;
            return buildString(chars);
        } else if ("TEXT".equals(format)) {
            char[] chars = charactersUp;
            return buildString(chars);
        } else if ("e".equals(format)) {
            char[] chars = charactersE;
            return buildString(chars);
        }
        throw new IllegalArgumentException("unsupported format '" + format + "'");
    }

    private String buildString(char[] chars) {
        StringBuffer ret = new StringBuffer();
        work = val;
        while (work > 0) {
            char c = nextChar(chars);
            ret.append(c);
        }
        return ret.reverse().toString();
    }

    private char nextChar(char[] chars) {
        int rem = work % (chars.length - 1);
        work /= chars.length - 1;
        if (rem == 0) {
            rem = chars.length - 1;
            work--;
        }
        return chars[rem];
    }
}
