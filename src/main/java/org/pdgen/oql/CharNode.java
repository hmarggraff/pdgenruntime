// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.DBIntImpl;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class CharNode extends Node {
    protected char value;

    public CharNode(char p0) {
        value = p0;
    }

    public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException {
        return value;
    }

    public long getIntValue(RunEnv env, DBData p0) {
        return value;
    }

    public String getTokenString() {
        return "'" + value + "'";
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append('"');
        collector.append(value);
        collector.append('"');
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBIntImpl(null, value);
    }

    public boolean isCharacter() {
        return true;
    }
}
