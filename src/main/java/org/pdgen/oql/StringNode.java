// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.PseudoAccess;


public class StringNode extends Node {
    protected String value;

    public StringNode(String p0) {
        value = p0;
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        return value;
    }

    public String getTokenString() {
        if (value.contains("\"")) {
            StringBuffer b = new StringBuffer(value.length() + 3);
            b.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '"')
                    b.append('\\');
                b.append(c);
            }
            return b.toString();
        } else
            return '"' + value + '"';
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append(getTokenString());
    }

    public boolean isString() {
        return true;
    }

    public String getConstantString() {
        return value;
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBStringImpl(new PseudoAccess(DefaultStringLiteral.instance()), value);
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return searchLabels && value != null && value.toLowerCase().contains(text);
    }
}
