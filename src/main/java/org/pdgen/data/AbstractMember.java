// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * implements the most basic functions for a joria access
 * maintains name, longname and defining class
 * provides default implementations for getCollectionType and getClassType
 */

public abstract class AbstractMember extends AbstractJoriaAccess {
    private static final long serialVersionUID = 7L;
    protected JoriaClass definingClass;

    public AbstractMember(JoriaClass definingClass, String name) {
        super(name);
        //Trace.check(definingClass);
        this.definingClass = definingClass;
    }

    public AbstractMember(JoriaClass definingClass) {
        Trace.check(definingClass);
        this.definingClass = definingClass;
    }

    public AbstractMember() {
    }

    public boolean isRoot() {
        return false;
    }

    public JoriaClass getDefiningClass() {
        return definingClass;
    }

    /**
     * This method may only be used for reparing data integrity. A member may not move under normal circumstances.
     *
     * @param newParent where the member should go
     */
    public void reparent(JoriaClass newParent) {
        Trace.logError("reparent " + getName() + " of " + getClass());
        definingClass = newParent;
    }
}
