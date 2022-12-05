// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.view.AggregateDef;
import org.pdgen.model.style.CellStyle;

import java.awt.*;
import java.util.ArrayList;

public class RVString implements RValue {
    String string;

    float width;

    public RVString(final String string, final float width) {
        this.string = string;
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public RVString(String string, CellStyle cs, Graphics2D g) {
        this.string = string;
        width = cs.getWidth(string, g);
    }

    protected RVString() {

    }

    public String get(int iter) {
        return string;
    }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter) {
        collector.accumulate(aggregates, string);
    }
}
