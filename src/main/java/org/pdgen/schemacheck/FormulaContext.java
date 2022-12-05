// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

public class FormulaContext {
    private final Object formulaHolder;
    Object context;
    String explanation;

    public FormulaContext(final Object formulaHolder, final Object context, final String explanation) {
        this.formulaHolder = formulaHolder;
        this.context = context;
        this.explanation = explanation;
    }

    public int hashCode() {
        return formulaHolder.hashCode();
    }

    public boolean equals(final Object obj) {
        if (obj instanceof FormulaContext) {
            FormulaContext fc = (FormulaContext) obj;

            return formulaHolder.equals(fc.formulaHolder);
        }
        return false;
    }

    public Object getFormulaHolder() {
        return formulaHolder;
    }
}
