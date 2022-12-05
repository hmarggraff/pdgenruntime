// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

import java.util.Set;

public interface OQLNode {
    boolean getBooleanValue(RunEnv env, DBData from) throws JoriaDataException;

    boolean isBoolean();

    boolean hasMofifiedAccess();

    void getUsedAccessors(Set<JoriaAccess> ret);

}
