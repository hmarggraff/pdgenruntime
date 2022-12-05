// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.Nameable;
import org.pdgen.data.NameableListener;
import org.pdgen.data.NameableTracer;
import org.pdgen.util.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Filter implements Serializable, NameableListener<Nameable> {

    private static final long serialVersionUID = 7L;
    protected String oqlString;
    protected Set<RuntimeParameter> variables;
    protected String hostFilterString;
    protected Set<RuntimeParameter> hostVariables;

    public Filter() {
    }

    public Filter(String oqlString, Set<RuntimeParameter> variables) {
        if (variables != null) {
            this.variables = new HashSet<>(variables);
            for (RuntimeParameter pov : variables) {
                NameableTracer.registerForNameable(pov, this);
            }
        } else
            this.variables = new HashSet<>();
        this.oqlString = oqlString;
    }

    public Filter dup() {
        Filter ret = new Filter(oqlString, variables);
        ret.hostFilterString = hostFilterString;
        ret.hostVariables = new HashSet<>(hostVariables);
        return ret;
    }

    public String getOqlString() {
        return oqlString;
    }

    public void setOqlString(String newOqlString) {
        oqlString = StringUtils.trimNull(newOqlString);
    }

    public void setOqlString(String newOqlString, Set<RuntimeParameter> newVariables) {
        for (RuntimeParameter oldVariable : variables) {
            NameableTracer.unregisterForNameable(oldVariable, this);
        }
        oqlString = StringUtils.trimNull(newOqlString);
        variables = newVariables;
        for (RuntimeParameter newVariable : newVariables) {
            NameableTracer.registerForNameable(newVariable, this);
        }
    }

    public Set<RuntimeParameter> getVariables() {
        return variables;
    }

    public void setHostFilterString(String newHostFilterString) {
        hostFilterString = newHostFilterString;
/*
		String[] names =hostFilterString.split(":");
		for(int i = 0; i < names.length / 2; i++)
		{
		}
		SortedNamedVectorBacking<RuntimeParameterLiteral> vars = JoriaStaticStorage.getActive().theRepository.variables;
		Set<RuntimeParameter> foundVariables = new HashSet<>();
		final RuntimeParameterLiteral v = vars.find(names[i * 2 +1]);
		if(v == null) {
			hostFilterString = "";
			return;
		}
		foundVariables.add(v);
		hostVariables = foundVariables;
		*/
    }

    public String getHostFilterString() {
        return hostFilterString;
    }


    public Filter copy(Map<Object, Object> copiedData) {
        Filter ret = new Filter();
        ret.oqlString = oqlString;
        if (variables != null) {
            ret.variables = new HashSet<>();
            for (RuntimeParameter variable : variables) {
                NameableTracer.registerForNameable(variable, ret);
                ret.variables.add(variable);
            }
        }
        ret.hostFilterString = hostFilterString;

        // TODO
        return ret;
    }

    public void nameableWillBeRenamed(Nameable what) {
        oqlString = oqlString.replaceAll("§" + what.getName(), "\\$\\.\\*\\.");
    }

    public void nameableHasBeenRenamed(Nameable what) {
        oqlString = oqlString.replaceAll("\\$\\.\\*\\.", "\\§" + what.getName());
    }

    protected Object readResolve() {
        if (variables != null)
            for (RuntimeParameter runtimeParameter : variables) {
                NameableTracer.registerForNameable(runtimeParameter, this);
            }
        return this;
    }
}
