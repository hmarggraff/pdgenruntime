// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;
import org.pdgen.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class JoriaModifiedAccess extends AbstractTypedJoriaMember implements VariableAccess {
    public static final int classNotFound = 0;
    public static final int memberNotFound = 1;
    public static final int rootNotFound = 2;
    public static final int typeChanged = 3;
    public static final int mutipleParents = 4;
    //public static final int inconsistentSchema = 5;
    static final String[] reasons = {"Class_not_found", "Member_not_found", "Root_not_found", "Type_changed", "Member_appears_in_more_than_one_class", "Error_in_saved_data_structure"};
    protected int reason;
    protected transient JoriaAccess readAccess;
    protected transient JoriaAccess foundAccess;
    private static final long serialVersionUID = 7L;


    private JoriaModifiedAccess(String name, JoriaType definedType, JoriaClass parent, int reason, JoriaAccess ra, JoriaAccess fa) {
        super(parent, name, definedType);
        readAccess = ra;
        foundAccess = fa;
        if (parent != null)
            Trace.logWarn("Modified access: " + parent.getName() + '.' + name + " reason: " + reasons[reason]);
        else
            Trace.logWarn("Modified access: " + "no parent " + '.' + name + " reason: " + reasons[reason]);
        this.reason = reason;
        Env.repoChanged();
    }

    public JoriaModifiedAccess() {
        Trace.logWarn("Internal Problem: Creation of a JoriaModifiedAccess should be prevented.");
    }

    public int getReason() {
        return reason;
    }

    public String getReasonString() {
        if (reason <0 || reason > reasons.length)
            return String.valueOf(reason);
        return reasons[reason];
    }

    public DBData getValue(DBData a, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        throw new JoriaDataException("getValue not possible for JoriaModifiedAccess." + getReasonString() + " in " + getName());
    }

    public boolean isRoot() {
        return definingClass == null;
    }

    public void makeLongName() {
        if (type != null) {
            longName = name + ":" + type.getName();
        } else {
            longName = name + ":" + "unknownType";
        }
    }

    protected Object readResolve() {
        if (definingClass != null) {
            JoriaAccess replace = definingClass.findMember(name);
            if (replace != null) {
                if (replace.getType() == type)
                    return replace;
            }
        }
        Log.ini.warn("Found a JoriaModifiedAccess in save file. This is a remainder of an incomplete schema evolution. If problem persists then contact support@pdgen.org");
        return this;
    }

    public String explainModification() {
        StringBuilder ret = new StringBuilder();
        ret.append(JoriaModifiedAccess.reasons[reason]);
        ret.append(" ");
        if (getDefiningClass() == null)
            ret.append("for entry point");
        else {
            ret.append(getDefiningClass().getName());
            ret.append('.');
        }
        ret.append(getName());
        ret.append(": ");
        ret.append(getType().getName());
        ret.append("\n");
        return ret.toString();
    }

    public JoriaAccess getPlaceHolderIfNeeded() {
        if (reason == typeChanged && foundAccess != null)// found access can be null if a modified access is saved. (Which should not really happen)
        {
            JoriaType readType = type;
            if (readAccess != null) {
                readType = readAccess.getType();
            }
            if (readType.isLiteral() && foundAccess.getType().isLiteral()) {
                //Repository.logFix("Member_"), readAccess, "changed_to") + foundAccess.getType().getName());
                return foundAccess;
            }
            if (readType.isClass() && foundAccess.getType().isClass()) {
                JoriaClass readClass = (JoriaClass) readType;
                JoriaClass foundClass = (JoriaClass) foundAccess.getType();
                if (testBaseClass(readClass, foundClass)) {
                    Env.instance().repo().logFix("Field_", this, "now uses compatible type" + foundClass.getName());
                    return foundAccess;
                }
            }
        }
        StringBuilder orib = new StringBuilder();
        if (definingClass != null)
            orib.append(definingClass.getName()).append('.');
        if (name != null)
            orib.append(name);
        if (type != null)
            orib.append(':').append(getType().getName());
        String ori = orib.toString();
        Env.instance().repo().logFix("Field ", this, "deactivated. Original " + ori);
        return new JoriaPlaceHolderAccess(name, ori);
    }

    boolean testBaseClass(JoriaClass readClass, JoriaClass foundClass) {
        if (readClass == foundClass) {
            return true;
        } else {
            JoriaClass[] baseClasses = foundClass.getBaseClasses();
            if (baseClasses != null) {
                for (JoriaClass baseClass : baseClasses) {
                    if (testBaseClass(readClass, baseClass)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static JoriaAccess createJoriaModifiedAccess(String name, JoriaType definedType, JoriaClass parent, int reason, JoriaAccess ra, JoriaAccess fa) {
        return new JoriaModifiedAccess(name, definedType, parent, reason, ra, fa);
    }

    public void collectVariables(final Set<RuntimeParameter> s, final Set<Object> seen) {
        seen.add(this);
    }

    public void collectI18nKeys2(final HashMap<String, List<I18nKeyHolder>> s, final Set<Object> seen) {
        seen.add(this);

    }

    public void collectVisiblePickersInScope(final List<JoriaAccess[]> collection, final Set<RuntimeParameter> visible, final Stack<JoriaAccess> pathStack, final Set<Object> seen) {
        seen.add(this);
    }
}
