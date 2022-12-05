// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.io.PrintStream;

public interface GraphicElement {
    void print(JoriaPrinter pr);

    void dump(PrintStream w);

    float getHeightFloat();

    void translate(float offsetx, float offsety);
}
