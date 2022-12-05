// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

public class StringPair implements Comparable<StringPair> {
    public String s;
    public String v;

    public StringPair(String s, String v) {
        this.s = s;
        this.v = v;
    }

    public int compareTo(StringPair o) {
        return s.compareToIgnoreCase(o.s);
    }
}
