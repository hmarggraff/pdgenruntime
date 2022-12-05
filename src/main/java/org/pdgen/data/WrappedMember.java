// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.IndirectAccess;
import org.pdgen.data.view.UnboundAccessSentinel;
import org.pdgen.model.run.RunEnv;

import java.io.ObjectStreamException;
import java.util.Set;

public class WrappedMember extends AbstractTypedJoriaMember implements IndirectAccess {
    private static final long serialVersionUID = 7L;
    protected JoriaAccess base;

    public WrappedMember(JoriaClass parent, JoriaType type, JoriaAccess access, String name) {
        super(parent, name, type);
        base = access;
        makeLongName();
    }

    public JoriaAccess getBaseAccess() {
        return base;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        return base.getValue(from, asView, env);
    }

    protected Object readResolve() throws ObjectStreamException {
        Trace.logDebug(Trace.serialize, "Wrapped member in " + definingClass);
        return definingClass.findMember(name);
    }

    public void unbind() {
        base = new UnboundAccessSentinel(base);
    }

    public boolean unbound() {
        return base instanceof UnboundAccessSentinel;
    }

    public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        return ((UnboundAccessSentinel) base).isBindable(newBinding);
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        Trace.check(bindableTo(newBinding, newParentBinding));
        base = newBinding;
    }


    public JoriaAccess getRootAccess() {
        return getBaseAccess();
    }

    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (seen.contains(this))
            return true;
        seen.add(this);
        if (!visitor.visit(base))
            return false;
        if (base instanceof VisitableAccess) {
            return ((VisitableAccess) base).visitAllAccesses(visitor, seen);
        }
        return true;
    }

}
