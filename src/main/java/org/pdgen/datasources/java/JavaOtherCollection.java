// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.JoriaCollection;

import java.util.Iterator;

public interface JavaOtherCollection extends JoriaCollection {
    Iterator<Object> iterator(Object o);
}
