// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.jetbrains.annotations.NotNull;
import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.Nameable;
import org.pdgen.data.VariableProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface RuntimeParameter extends VariableProvider, JoriaAccess, Nameable, Comparable<RuntimeParameter> {

    public static void addAll(Set<RuntimeParameter> res, Set<RuntimeParameter> vars, final Set<Object> seen)
    {
        if (vars == null)
            return;
        for (RuntimeParameter var : vars)
        {
            res.add(var);
            if (seen.contains(var))
                continue;
            seen.add(var);
            if (var instanceof VariableProvider)
            {
                VariableProvider vp = (VariableProvider) var;
                vp.collectVariables(res, seen);
            }
        }
    }

    void getUsedAccessors(Set<JoriaAccess> accessSet);

    void collectVariables(Set<RuntimeParameter> runtimeParameterSet, Set<Object> seen);

    // for extensions where paramaters can use OQL
    Object getOqlEvaluator();

    void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> localizables);

    @Override
    default int compareTo(@NotNull RuntimeParameter o) {
        return getName().compareTo(o.getName());
    }
}
