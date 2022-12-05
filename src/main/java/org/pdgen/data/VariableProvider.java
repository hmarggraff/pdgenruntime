// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.RuntimeParameter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public interface VariableProvider extends Serializable {

    void collectVariables(Set<RuntimeParameter> s, Set<Object> seen);

    void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen);

    void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen);

}
