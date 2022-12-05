// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.view.AggregateDef;

import java.util.ArrayList;

public interface RValue extends RVAny {
    String get(int at);

    void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter);
}
