// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.Comparator;

public class NamedComparator implements Comparator<Named> {
    public static final NamedComparator caseSensitiveComparator = new NamedComparator(true);
    public static final NamedComparator caseInsensitiveComparator = new NamedComparator(false);
    boolean caseSensitive;

    public NamedComparator(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public int compare(Named x, Named y) {
        if (x == null && y == null)
            return 0;
        if (x == null)
            return 1;
        if (y == null)
            return -1;
        String xn = x.getName();
        String yn = y.getName();
        if (xn == null || yn == null)
            return 0;
        if (caseSensitive)
            return xn.compareTo(yn);
        else
            return xn.compareToIgnoreCase(yn);
    }

    public boolean equals(Object x, Object y) {
        if (x == null || y == null)
            return false;
        if (x instanceof Named && y instanceof Named) {
            String xn = ((Named) x).getName();
            String yn = ((Named) y).getName();
            if (xn == null || yn == null)
                return false;
            if (caseSensitive)
                return xn.equals(yn);
            else
                return xn.equalsIgnoreCase(yn);
        }
        return false;
    }
}
