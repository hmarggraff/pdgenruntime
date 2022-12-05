// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.run.AggregateCollector;

public interface EnvValueCell {

    String getFormattedString(AggregateCollector into);
}
