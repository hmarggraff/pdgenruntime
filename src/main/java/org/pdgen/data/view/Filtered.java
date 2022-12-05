// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.VariableProvider;


public interface Filtered extends VariableProvider {
    Filter getFilter();

    void setFilter(Filter newFilter);
}
