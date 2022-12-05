// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Res;

import java.util.ArrayList;

public class JoriaClassVoid extends AbstractJoriaClass {
    public static final JoriaClassVoid voidType = new JoriaClassVoid();
    private static final long serialVersionUID = 7L;

    private JoriaClassVoid() {
    }

    public JoriaAccess[] getFlatMembers() {
        return noMembers;
    }

    public String getName() {
        return Res.asis("void");
    }

    public String getParamString() {
        return Res.asis("JoriaClassVoid");
    }

    public boolean isVoid() {
        return true;
    }

    public void setDerivedClasses(ArrayList<JoriaClass> subs) {
    }

    protected Object readResolve() {
        return voidType;
    }
}
