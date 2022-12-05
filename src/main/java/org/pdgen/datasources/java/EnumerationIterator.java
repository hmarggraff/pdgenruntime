// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator implements Iterator {

    Enumeration<?> e;

    public EnumerationIterator(Enumeration<?> e) {
        this.e = e;
    }

    public void remove() {
    }

    public Object next() {
        return e.nextElement();
    }

    public boolean hasNext() {
        return e.hasMoreElements();
    }
}
