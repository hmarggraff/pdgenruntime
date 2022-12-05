// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;


public class RVObjects implements RVAny {
    RVTemplate[] elems;

    public RVObjects(RVTemplate[] elems) {
        this.elems = elems;
    }

    public int elementCount() {
        return elems.length;
    }

    public RVAny get(int elem, int row, int col) {
        return elems[elem].subs[row][col];
    }

    public void put(int at, RVTemplate v) {
        elems[at] = v;
    }
}
