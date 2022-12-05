// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class PseudoAccess extends AbstractTypedJoriaAccess {

    private static final long serialVersionUID = 7L;

    public PseudoAccess(JoriaType target) {
        super("(" + target.getName() + ")", target);
        makeLongName();
    }

    public PseudoAccess(JoriaType target, String name) {
        super(name, target);
        makeLongName();
    }


    public DBData getValue(DBData d, JoriaAccess asView, RunEnv env) {
        throw new JoriaAssertionError("getValue for PseudoAccess not possible");
    }


    public boolean isRoot() {
        return false;
    }

    public void setType(JoriaType val) {
        type = val;
        makeLongName();
    }

}
