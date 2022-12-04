// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import java.awt.*;

public interface TemplateEditorIF {
    void setPreferredSize(Dimension sizeForLayout);

    void revalidate();

    void setPageNo(int size);

    void repaint();

    float getScale();
}
