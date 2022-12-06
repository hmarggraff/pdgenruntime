// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterator<T> {

    Enumeration<T> e;

    public EnumerationIterator(Enumeration<T> e) {
        this.e = e;
    }

    public void remove() {
    }

    public T next() {
        return e.nextElement();
    }

    public boolean hasNext() {
        return e.hasMoreElements();
    }
}
