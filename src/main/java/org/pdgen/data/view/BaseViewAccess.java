// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.AbstractTypedJoriaMember;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;

/**
 * User: hmf
 * Date: 17.05.2006
 * Time: 00:54:42
 */
public class BaseViewAccess extends AbstractTypedJoriaMember {
    private static final long serialVersionUID = 7L;

    public BaseViewAccess(ClassProjection definingClass) {
        super(definingClass, Res.asis("access_to_base_of_view"), definingClass.getBase());
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        return from;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BaseViewAccess))
            return false;
        BaseViewAccess a = (BaseViewAccess) obj;
        final boolean ret = getType().equals(a.getType()) && getName().equals(a.getName());
        return ret;
    }

    public int hashCode() {
        return type.hashCode();
    }
}
